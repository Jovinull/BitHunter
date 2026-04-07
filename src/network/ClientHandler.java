package network;

import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import network.packets.DisconnectPacket;
import network.packets.HandshakePacket;
import network.packets.PingPacket;
import network.packets.PongPacket;

/**
 * Gerencia a conexão TCP de um único cliente no lado do servidor.
 * <p>
 * Cada instância é criada pelo {@link NetworkServer} quando um cliente se conecta
 * e roda sua própria thread de leitura ({@code net-client-rx-<ip>}). Pacotes
 * recebidos são enfileirados em {@link NetworkServer} via
 * {@link NetworkServer#enqueue(ClientHandler, Packet)} para consumo pelo game loop.
 * <p>
 * Handshake e Ping/Pong são tratados diretamente nesta classe, sem passar pelo
 * game loop, para manter a latência mínima.
 */
public final class ClientHandler {

  private static final Logger LOGGER = Logger.getLogger(ClientHandler.class.getName());

  private final Socket socket;
  private final NetworkServer server;
  private final PacketSerializer serializer;
  private final PacketDeserializer deserializer;

  private Thread readThread;
  private volatile boolean running = false;

  // Metadados preenchidos após handshake bem-sucedido
  private String playerName = "";
  private int characterClass = -1;
  private int assignedPlayerId = -1;
  private volatile boolean handshakeCompleted = false;

  /**
   * @param socket socket TCP aceito pelo {@link NetworkServer}
   * @param server referência ao servidor para enfileirar pacotes e notificar saída
   * @throws IOException se não for possível obter os streams do socket
   */
  public ClientHandler(Socket socket, NetworkServer server) throws IOException {
    this.socket = socket;
    this.server = server;
    this.serializer = new PacketSerializer(socket.getOutputStream());
    this.deserializer = new PacketDeserializer(socket.getInputStream());
  }

  /** Inicia a thread de leitura deste cliente. */
  public void start() {
    running = true;
    String threadName = "net-client-rx-" + socket.getInetAddress().getHostAddress();
    readThread = new Thread(this::readLoop, threadName);
    readThread.setDaemon(true);
    readThread.start();
  }

  private void readLoop() {
    try {
      while (running) {
        Packet packet = deserializer.receive();
        dispatchPacket(packet);
      }
    } catch (IOException e) {
      if (running) {
        LOGGER.log(Level.INFO, "Cliente {0} desconectado: {1}",
            new Object[]{socket.getInetAddress().getHostAddress(), e.getMessage()});
      }
    } finally {
      stop();
      server.removeClient(this);
    }
  }

  /**
   * Despacha o pacote recebido: trata Handshake e Ping localmente;
   * demais pacotes são enfileirados no servidor para o game loop.
   */
  private void dispatchPacket(Packet packet) {
    switch (packet.getType()) {
      case HANDSHAKE -> handleHandshake((HandshakePacket) packet);
      case PING      -> handlePing((PingPacket) packet);
      default        -> server.enqueue(this, packet);
    }
  }

  private void handleHandshake(HandshakePacket hp) {
    if (!hp.isVersionCompatible()) {
      LOGGER.log(Level.WARNING,
          "Cliente {0} rejeitado: versão de protocolo {1} incompatível com {2}",
          new Object[]{socket.getInetAddress().getHostAddress(),
              hp.getProtocolVersion(), HandshakePacket.PROTOCOL_VERSION});
      send(new DisconnectPacket(DisconnectPacket.REASON_BAD_VERSION));
      stop();
      return;
    }
    this.playerName = hp.getPlayerName();
    this.characterClass = hp.getCharacterClass();
    this.handshakeCompleted = true;
    LOGGER.log(Level.INFO, "Handshake completo: jogador=\"{0}\" classe={1} ip={2}",
        new Object[]{playerName, characterClass,
            socket.getInetAddress().getHostAddress()});
    // Enfileira para que o game loop atribua um ID de jogador
    server.enqueue(this, hp);
  }

  /** Responde ao Ping com um Pong imediato (sem passar pelo game loop). */
  private void handlePing(PingPacket ping) {
    send(PongPacket.replyTo(ping));
  }

  /**
   * Envia um pacote para este cliente de forma thread-safe.
   * Em caso de erro, encerra a conexão.
   *
   * @param packet pacote a enviar
   */
  public void send(Packet packet) {
    try {
      serializer.send(packet);
    } catch (IOException e) {
      LOGGER.log(Level.WARNING, "Erro ao enviar para cliente {0}: {1}",
          new Object[]{socket.getInetAddress().getHostAddress(), e.getMessage()});
      stop();
    }
  }

  /** Encerra a conexão deste cliente de forma ordenada. */
  public void stop() {
    running = false;
    try {
      if (!socket.isClosed()) {
        socket.close();
      }
    } catch (IOException e) {
      LOGGER.log(Level.FINE, "Erro ao fechar socket de cliente", e);
    }
  }

  // ---- Getters / setters de metadados ----

  public String getPlayerName()          { return playerName; }
  public int getCharacterClass()         { return characterClass; }
  public int getAssignedPlayerId()       { return assignedPlayerId; }
  public void setAssignedPlayerId(int id){ this.assignedPlayerId = id; }
  public boolean isHandshakeCompleted()  { return handshakeCompleted; }
  public boolean isRunning()             { return running; }

  /** Endereço IP do cliente (para logs). */
  public String getRemoteAddress() {
    return socket.getInetAddress().getHostAddress();
  }
}

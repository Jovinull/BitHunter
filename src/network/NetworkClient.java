package network;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import network.packets.HandshakePacket;
import network.packets.PingPacket;
import network.packets.PongPacket;

/**
 * Cliente TCP do BitHunter Multiplayer.
 * <p>
 * Conecta-se a um {@link NetworkServer} (host), envia o {@link HandshakePacket}
 * imediatamente após a conexão e mantém uma thread de leitura em background.
 * Pacotes recebidos do servidor são enfileirados em {@link #incomingQueue}
 * e consumidos pelo game loop via {@link #pollIncoming()}.
 * <p>
 * O envio de pacotes ({@link #send(Packet)}) pode ser chamado de qualquer thread
 * mas é tipicamente chamado pelo game loop a cada frame para enviar
 * {@link network.packets.InputPacket}s.
 * <p>
 * Ciclo de vida: {@link #connect(String, int)} → (jogo em andamento) → {@link #disconnect()}.
 */
public final class NetworkClient {

  private static final Logger LOGGER = Logger.getLogger(NetworkClient.class.getName());

  private final String host;
  private final int port;

  private Socket socket;
  private PacketSerializer serializer;
  private PacketDeserializer deserializer;
  private Thread readThread;

  private volatile boolean running = false;
  private volatile boolean connected = false;

  // Fila thread-safe de pacotes recebidos — game loop consome via pollIncoming()
  private final ConcurrentLinkedQueue<Packet> incomingQueue = new ConcurrentLinkedQueue<>();

  /**
   * @param host endereço IP ou hostname do servidor
   * @param port porta TCP do servidor
   */
  public NetworkClient(String host, int port) {
    this.host = host;
    this.port = port;
  }

  /** Cria um cliente apontando para a porta padrão ({@link NetworkServer#DEFAULT_PORT}). */
  public NetworkClient(String host) {
    this(host, NetworkServer.DEFAULT_PORT);
  }

  /**
   * Estabelece a conexão TCP com o servidor, envia o handshake e inicia
   * a thread de leitura.
   *
   * @param playerName     nome do jogador local
   * @param characterClass índice da classe (0=Guerreiro, 1=Ladrão, 2=Feiticeiro)
   * @throws IOException se não for possível conectar ao servidor
   */
  public void connect(String playerName, int characterClass) throws IOException {
    socket = new Socket(host, port);
    serializer = new PacketSerializer(socket.getOutputStream());
    deserializer = new PacketDeserializer(socket.getInputStream());
    running = true;
    connected = true;

    // Handshake imediato antes de iniciar a thread de leitura
    HandshakePacket handshake = new HandshakePacket(playerName, (byte) characterClass);
    serializer.send(handshake);

    readThread = new Thread(this::readLoop, "net-client-rx");
    readThread.setDaemon(true);
    readThread.start();

    LOGGER.log(Level.INFO, "Conectado ao servidor {0}:{1} como \"{2}\"",
        new Object[]{host, port, playerName});
  }

  private void readLoop() {
    try {
      while (running) {
        Packet packet = deserializer.receive();
        try {
          dispatchPacket(packet);
        } catch (RuntimeException e) {
          // Isolamento: pacote malformado não derruba a conexão inteira
          LOGGER.log(Level.WARNING, "Erro ao processar pacote do servidor: {0}", e.getMessage());
        }
      }
    } catch (IOException e) {
      if (running) {
        LOGGER.log(Level.INFO, "Desconectado do servidor: {0}", e.getMessage());
      }
    } finally {
      connected = false;
      disconnect();
    }
  }

  /**
   * Responde Ping com Pong imediatamente (sem passar pelo game loop).
   * Demais pacotes são enfileirados para o game loop consumir.
   */
  private void dispatchPacket(Packet packet) {
    if (packet.getType() == PacketType.PING) {
      send(PongPacket.replyTo((PingPacket) packet));
    } else {
      incomingQueue.offer(packet);
    }
  }

  /**
   * Envia um pacote ao servidor. Thread-safe via {@link PacketSerializer#send(Packet)}.
   * Em caso de erro de I/O, marca o cliente como desconectado.
   *
   * @param packet pacote a enviar
   */
  public void send(Packet packet) {
    if (!connected) {
      return;
    }
    try {
      serializer.send(packet);
    } catch (IOException e) {
      LOGGER.log(Level.WARNING, "Erro ao enviar pacote ao servidor: {0}", e.getMessage());
      connected = false;
    }
  }

  /**
   * Retorna e remove o próximo pacote enfileirado, ou {@code null} se vazio.
   * Deve ser chamado no início de cada {@code update()} do game loop.
   */
  public Packet pollIncoming() {
    return incomingQueue.poll();
  }

  /** Encerra a conexão com o servidor de forma ordenada. */
  public void disconnect() {
    running = false;
    connected = false;
    try {
      if (socket != null && !socket.isClosed()) {
        socket.close();
      }
    } catch (IOException e) {
      LOGGER.log(Level.FINE, "Erro ao fechar socket de cliente", e);
    }
    LOGGER.log(Level.INFO, "Desconectado do servidor {0}:{1}", new Object[]{host, port});
  }

  // ---- Getters de estado ----

  public boolean isConnected() { return connected; }
  public String getHost()      { return host; }
  public int getPort()         { return port; }
}

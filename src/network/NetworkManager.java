package network;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Facade de rede do BitHunter — ponto único de acesso ao subsistema de rede.
 * <p>
 * Em modo {@link NetworkMode#SINGLEPLAYER}, todos os métodos são no-op seguros:
 * o game loop pode chamá-los sem verificação prévia de modo.
 * <p>
 * <strong>Uso no game loop (Fase 2):</strong>
 * <pre>{@code
 * // No início de update():
 * if (networkManager.isHost()) {
 *   IncomingPacket pkt;
 *   while ((pkt = networkManager.pollServerIncoming()) != null) {
 *     applyIncomingPacket(pkt);
 *   }
 * } else if (networkManager.isClient()) {
 *   Packet pkt;
 *   while ((pkt = networkManager.pollClientIncoming()) != null) {
 *     applyServerPacket(pkt);
 *   }
 * }
 *
 * // Ao final de update() (host → broadcast estado):
 * if (networkManager.isHost()) {
 *   networkManager.broadcast(buildStatePacket());
 * }
 *
 * // A cada frame (cliente → envia input):
 * if (networkManager.isClient()) {
 *   networkManager.sendToServer(captureInput());
 * }
 * }</pre>
 * <p>
 * Ciclo de vida:
 * <ul>
 *   <li>Host: {@link #startAsHost()} → jogo → {@link #stop()}</li>
 *   <li>Cliente: {@link #connectToHost(String, String, int)} → jogo → {@link #stop()}</li>
 *   <li>Singleplayer: nenhuma chamada necessária</li>
 * </ul>
 */
public final class NetworkManager {

  private static final Logger LOGGER = Logger.getLogger(NetworkManager.class.getName());

  /**
   * Modo de operação de rede da instância atual do jogo.
   */
  public enum NetworkMode {
    /** Modo solo — sem rede. Todos os métodos do {@link NetworkManager} são no-op. */
    SINGLEPLAYER,

    /**
     * Este processo é o host da sessão. Executa o {@link NetworkServer} e
     * também é um jogador local com autoridade total sobre o estado de jogo.
     */
    HOST,

    /**
     * Este processo conectou-se a um host remoto. Envia inputs locais e
     * recebe estado autoritativo do servidor.
     */
    CLIENT
  }

  private NetworkMode mode = NetworkMode.SINGLEPLAYER;
  private NetworkServer server;
  private NetworkClient client;

  // ---- Última conexão de cliente (para reconexão automática) ----
  private String lastHost;
  private int lastPort;
  private String lastPlayerName;
  private int lastCharacterClass;

  /** Endereço do último host ao qual este cliente conectou (para retry). */
  public String getLastHost()           { return lastHost; }
  public int getLastPort()              { return lastPort; }
  public String getLastPlayerName()     { return lastPlayerName; }
  public int getLastCharacterClass()    { return lastCharacterClass; }

  // ---- Inicialização ----

  /**
   * Inicia o servidor na porta especificada (modo HOST).
   *
   * @param port porta TCP a escutar
   * @throws IOException          se a porta estiver em uso
   * @throws IllegalStateException se a rede já estiver ativa
   */
  public void startAsHost(int port) throws IOException {
    ensureInactive();
    server = new NetworkServer(port);
    server.start();
    mode = NetworkMode.HOST;
    LOGGER.log(Level.INFO, "NetworkManager: modo HOST na porta {0}", port);
  }

  /** Inicia o servidor na porta padrão ({@link NetworkServer#DEFAULT_PORT}). */
  public void startAsHost() throws IOException {
    startAsHost(NetworkServer.DEFAULT_PORT);
  }

  /**
   * Conecta ao host especificado (modo CLIENT).
   *
   * @param host           endereço IP ou hostname do servidor
   * @param port           porta TCP do servidor
   * @param playerName     nome do jogador local
   * @param characterClass índice da classe (0=Guerreiro, 1=Ladrão, 2=Feiticeiro)
   * @throws IOException          se não for possível conectar
   * @throws IllegalStateException se a rede já estiver ativa
   */
  public void connectToHost(String host, int port, String playerName, int characterClass)
      throws IOException {
    ensureInactive();
    client = new NetworkClient(host, port);
    client.connect(playerName, characterClass);
    mode = NetworkMode.CLIENT;
    this.lastHost = host;
    this.lastPort = port;
    this.lastPlayerName = playerName;
    this.lastCharacterClass = characterClass;
    LOGGER.log(Level.INFO, "NetworkManager: modo CLIENT conectado a {0}:{1}",
        new Object[]{host, port});
  }

  /** Conecta ao host na porta padrão. */
  public void connectToHost(String host, String playerName, int characterClass)
      throws IOException {
    connectToHost(host, NetworkServer.DEFAULT_PORT, playerName, characterClass);
  }

  /**
   * Encerra a rede de forma ordenada e volta ao modo SINGLEPLAYER.
   * No-op se já estiver em modo SINGLEPLAYER.
   */
  public void stop() {
    if (server != null) {
      server.stop();
      server = null;
    }
    if (client != null) {
      client.disconnect();
      client = null;
    }
    mode = NetworkMode.SINGLEPLAYER;
    LOGGER.log(Level.INFO, "NetworkManager: rede encerrada");
  }

  // ---- Envio (no-op em SINGLEPLAYER) ----

  /**
   * Broadcast de um pacote para todos os clientes conectados.
   * No-op se não estiver em modo HOST.
   */
  public void broadcast(Packet packet) {
    if (server != null) {
      server.broadcast(packet);
    }
  }

  /**
   * Broadcast para todos os clientes exceto o especificado.
   * No-op se não estiver em modo HOST.
   */
  public void broadcastExcept(Packet packet, ClientHandler exclude) {
    if (server != null) {
      server.broadcastExcept(packet, exclude);
    }
  }

  /**
   * Envia um pacote ao servidor host.
   * No-op se não estiver em modo CLIENT.
   */
  public void sendToServer(Packet packet) {
    if (client != null) {
      client.send(packet);
    }
  }

  // ---- Recebimento (game loop drena no início de update()) ----

  /**
   * Retorna o próximo pacote recebido de qualquer cliente, ou {@code null}.
   * Deve ser chamado exclusivamente no game loop quando em modo HOST.
   */
  public IncomingPacket pollServerIncoming() {
    if (server != null) {
      return server.pollIncoming();
    }
    return null;
  }

  /**
   * Retorna o próximo pacote recebido do servidor, ou {@code null}.
   * Deve ser chamado exclusivamente no game loop quando em modo CLIENT.
   */
  public Packet pollClientIncoming() {
    if (client != null) {
      return client.pollIncoming();
    }
    return null;
  }

  // ---- Estado ----

  /** Retorna {@code true} se há rede ativa (HOST ou CLIENT). */
  public boolean isMultiplayer() {
    return mode != NetworkMode.SINGLEPLAYER;
  }

  /** Retorna {@code true} se este processo é o host da sessão. */
  public boolean isHost() {
    return mode == NetworkMode.HOST;
  }

  /** Retorna {@code true} se este processo é um cliente conectado a um host. */
  public boolean isClient() {
    return mode == NetworkMode.CLIENT;
  }

  public NetworkMode getMode() {
    return mode;
  }

  /**
   * Número de clientes conectados ao servidor local.
   * Retorna 0 se não estiver em modo HOST.
   */
  public int getConnectedClientCount() {
    return server != null ? server.getClientCount() : 0;
  }

  /**
   * Retorna {@code true} se o cliente ainda está conectado ao servidor.
   * Retorna {@code false} se não estiver em modo CLIENT.
   */
  public boolean isClientConnected() {
    return client != null && client.isConnected();
  }

  // ---- Auxiliar interno ----

  private void ensureInactive() {
    if (mode != NetworkMode.SINGLEPLAYER) {
      throw new IllegalStateException(
          "Rede já ativa no modo " + mode + ". Chame stop() antes de reconfigurar.");
    }
  }
}

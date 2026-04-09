package network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import network.packets.DisconnectPacket;

/**
 * Servidor TCP do BitHunter Multiplayer.
 * <p>
 * Aceita até {@link #MAX_CLIENTS} conexões simultâneas. Cada conexão é gerenciada
 * por um {@link ClientHandler} em sua própria thread de leitura. Pacotes recebidos
 * de qualquer cliente são enfileirados em {@link #incomingQueue} e consumidos pelo
 * game loop via {@link #pollIncoming()}.
 * <p>
 * O envio nunca bloqueia o game loop: {@link #broadcast(Packet)} itera sobre os
 * handlers e chama {@link ClientHandler#send(Packet)}, que tem seu próprio lock
 * de serialização.
 * <p>
 * Ciclo de vida: {@link #start()} → (aceita conexões) → {@link #stop()}.
 */
public final class NetworkServer {

  private static final Logger LOGGER = Logger.getLogger(NetworkServer.class.getName());

  /** Porta padrão do servidor BitHunter. */
  public static final int DEFAULT_PORT = 7777;

  /** Máximo de clientes simultâneos (host + 3 = 4 jogadores). */
  public static final int MAX_CLIENTS = 3;

  private final int port;
  private ServerSocket serverSocket;
  private Thread acceptThread;
  private volatile boolean running = false;

  // CopyOnWriteArrayList: iteração thread-safe sem lock explícito; max 3 clientes
  private final List<ClientHandler> clients = new CopyOnWriteArrayList<>();

  // Fila thread-safe de pacotes recebidos — game loop consome via pollIncoming()
  private final ConcurrentLinkedQueue<IncomingPacket> incomingQueue =
      new ConcurrentLinkedQueue<>();

  /**
   * Cria um servidor na porta especificada.
   *
   * @param port porta TCP a escutar (1024–65535 recomendado)
   */
  public NetworkServer(int port) {
    this.port = port;
  }

  /** Cria um servidor na porta padrão ({@link #DEFAULT_PORT}). */
  public NetworkServer() {
    this(DEFAULT_PORT);
  }

  /**
   * Abre o {@link ServerSocket} e inicia a thread de aceite de conexões.
   *
   * @throws IOException se a porta já estiver em uso ou ocorrer outro erro de I/O
   */
  public void start() throws IOException {
    serverSocket = new ServerSocket(port);
    running = true;
    acceptThread = new Thread(this::acceptLoop, "net-accept");
    acceptThread.setDaemon(true);
    acceptThread.start();
    LOGGER.log(Level.INFO, "Servidor iniciado na porta {0} (max {1} clientes)",
        new Object[]{port, MAX_CLIENTS});
  }

  private void acceptLoop() {
    while (running) {
      try {
        Socket socket = serverSocket.accept();

        if (clients.size() >= MAX_CLIENTS) {
          LOGGER.log(Level.WARNING,
              "Conexão de {0} recusada: limite de {1} clientes atingido",
              new Object[]{socket.getInetAddress().getHostAddress(), MAX_CLIENTS});
          // Informa o cliente antes de fechar
          try {
            PacketSerializer tmp = new PacketSerializer(socket.getOutputStream());
            tmp.send(new DisconnectPacket(DisconnectPacket.REASON_SERVER_FULL));
          } catch (IOException ignored) {
            // Melhor esforço — se falhar, fecha silenciosamente
          }
          socket.close();
          continue;
        }

        ClientHandler handler = new ClientHandler(socket, this);
        clients.add(handler);
        handler.start();
        LOGGER.log(Level.INFO, "Cliente conectado: {0} (total: {1})",
            new Object[]{socket.getInetAddress().getHostAddress(), clients.size()});

      } catch (IOException e) {
        if (running) {
          LOGGER.log(Level.WARNING, "Erro ao aceitar conexão", e);
        }
        // Se !running, é encerramento normal — sai do loop
      }
    }
  }

  /**
   * Encerra o servidor: notifica todos os clientes, fecha conexões e o ServerSocket.
   */
  public void stop() {
    running = false;
    // Notifica clientes antes de fechar
    for (ClientHandler client : clients) {
      client.send(new DisconnectPacket(DisconnectPacket.REASON_HOST_CLOSED));
      client.stop();
    }
    clients.clear();
    try {
      if (serverSocket != null && !serverSocket.isClosed()) {
        serverSocket.close();
      }
    } catch (IOException e) {
      LOGGER.log(Level.WARNING, "Erro ao fechar ServerSocket", e);
    }
    LOGGER.log(Level.INFO, "Servidor encerrado");
  }

  // ---- Chamados por ClientHandler ----

  /**
   * Enfileira um pacote recebido de um cliente para consumo pelo game loop.
   * Chamado pela thread de leitura do {@link ClientHandler}.
   */
  void enqueue(ClientHandler source, Packet packet) {
    incomingQueue.offer(new IncomingPacket(source, packet));
  }

  /**
   * Remove um cliente da lista (chamado quando sua conexão é encerrada).
   * Thread-safe via {@link CopyOnWriteArrayList}.
   */
  void removeClient(ClientHandler client) {
    clients.remove(client);
    // Enfileira um DISCONNECT sintético para que o game loop saiba quem saiu
    enqueue(client, new network.packets.DisconnectPacket("client_disconnected"));
    LOGGER.log(Level.INFO, "Cliente {0} removido (restantes: {1})",
        new Object[]{client.getRemoteAddress(), clients.size()});
  }

  // ---- API para o game loop ----

  /**
   * Retorna e remove o próximo pacote enfileirado, ou {@code null} se vazio.
   * Deve ser chamado no início de cada {@code update()} do game loop.
   */
  public IncomingPacket pollIncoming() {
    return incomingQueue.poll();
  }

  /**
   * Envia um pacote para todos os clientes conectados.
   *
   * @param packet pacote a transmitir
   */
  public void broadcast(Packet packet) {
    for (ClientHandler client : clients) {
      client.send(packet);
    }
  }

  /**
   * Envia um pacote para todos os clientes, exceto o especificado.
   * Útil para re-propagar o input de um jogador para os demais.
   *
   * @param packet  pacote a transmitir
   * @param exclude handler a excluir do envio
   */
  public void broadcastExcept(Packet packet, ClientHandler exclude) {
    for (ClientHandler client : clients) {
      if (client != exclude) {
        client.send(packet);
      }
    }
  }

  // ---- Getters de estado ----

  public boolean isRunning()       { return running; }
  public int getPort()             { return port; }
  public int getClientCount()      { return clients.size(); }
}

package main;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JPanel;

import achievement.AchievementManager;
import achievement.AchievementToast;
import ai.PathFinder;
import bestiary.BestiaryService;
import data.GameStatistics;
import data.PlayTimeTracker;
import data.SaveLoad;
import data.SaveManager;
import entity.Entity;
import entity.Player;
import entity.RemotePlayer;
import environment.EnvironmentManager;
import network.ClientHandler;
import network.IncomingPacket;
import network.NetworkManager;
import network.Packet;
import network.packets.DisconnectPacket;
import network.packets.EntityStatePacket;
import network.packets.EventPacket;
import network.packets.HandshakePacket;
import network.packets.InputPacket;
import network.packets.ItemSpawnPacket;
import network.packets.ChatPacket;
import network.packets.PetSyncPacket;
import network.packets.TradePacket;
import network.ChatManager;
import network.PlayerTradeManager;
import pets.PetIncubator;
import tile.Map;
import tile.TileManager;
import tile_interactive.InteractiveTile;
import ui.UI;

// Painel principal do jogo. Responsável por configurar, atualizar e desenhar o jogo.
// Implementa Runnable para rodar o loop de jogo em uma thread separada.
// this-escape: arquitetura hub-and-spoke — componentes recebem referência ao painel durante
// inicialização. Seguro pois GamePanel não possui subclasses.
@SuppressWarnings("this-escape")
public class GamePanel extends JPanel implements Runnable {

  @Serial
  private static final long serialVersionUID = 1L;

  private static final Logger LOGGER = Logger.getLogger(GamePanel.class.getName());
  private static final boolean DEBUG_RESPAWN = false;

  // ==== CONFIGURAÇÕES DE TELA (visível) ====
  private static final int ORIGINAL_TILE_SIZE = 16; // Tamanho base de um tile (16x16 pixels)
  private static final int SCALE = 3; // Fator de escala aplicado ao tile

  public static final int TILE_SIZE = ORIGINAL_TILE_SIZE * SCALE; // Tile final com escala (48x48 pixels)
  public static final int MAX_SCREEN_COL = 20; // Número de colunas visíveis na tela
  public static final int MAX_SCREEN_ROW = 12; // Número de linhas visíveis na tela
  public static final int SCREEN_WIDTH = TILE_SIZE * MAX_SCREEN_COL; // Largura total da tela (960 px)
  public static final int SCREEN_HEIGHT = TILE_SIZE * MAX_SCREEN_ROW; // Altura total da tela (576 px)

  public static final int MAX_MAP = 10;
  private int currentMap = 0;

  // FOR FULL SCREEN
  int screenWidth2 = SCREEN_WIDTH;
  int screenHeight2 = SCREEN_HEIGHT;
  private transient BufferedImage tempScreen;
  private transient Graphics2D g2;
  private boolean fullScreenOn = false;

  // ==== CONFIGURAÇÕES DO MUNDO ====
  private int maxWorldCol;
  private int maxWorldRow;

  // ==== CONFIGURAÇÃO DE FPS ====
  int fps = 60; // Quadros por segundo

  // ==== COMPONENTES DO JOGO ====
  private final transient TileManager tileM = new TileManager(this); // Gerencia o carregamento e desenho dos tiles
  private final transient InputBindings inputBindings = new InputBindings(); // Mapeamento tecla → ação
  private final transient KeyHandler keyH = new KeyHandler(this); // Captura entrada de teclado
  transient Sound music = new Sound(); // Gerencia a música do jogo
  transient Sound se = new Sound(); // Gerencia os efeitos sonoros do jogo
  private final transient CollisionChecker cChecker = new CollisionChecker(this); // Verifica colisões
  private final transient AssetSetter aSetter = new AssetSetter(this); // Configura os objetos do jogo
  private final transient LanguageManager languageManager = new LanguageManager(); // i18n
  private final transient UI gameUi = new UI(this); // Interface do usuário (HUD, mensagens, etc.)
  private final transient EventHandler eHandler = new EventHandler(this);
  transient Config config = new Config(this);
  private final transient PathFinder pFinder = new PathFinder(this);
  transient EnvironmentManager eManager = new EnvironmentManager(this);
  transient Map map = new Map(this);
  transient SaveLoad saveLoad = new SaveLoad(this);
  private final transient SaveManager saveManager = new SaveManager(this);
  private final transient EntityGenerator eGenerator = new EntityGenerator(this);
  private final transient CutsceneManager csManager = new CutsceneManager(this);
  private final transient PetIncubator petIncubator = new PetIncubator(this);
  private final transient BestiaryService bestiaryService = new BestiaryService(this);
  private final transient AchievementManager achievementManager = new AchievementManager(this);
  private final transient AchievementToast achievementToast = new AchievementToast();
  private final transient CheatCodeManager cheatCodeManager = new CheatCodeManager(this);
  private final transient PlayTimeTracker playTimeTracker = new PlayTimeTracker();
  private final transient GameStatistics gameStatistics = new GameStatistics();
  transient Thread gameThread; // Thread para rodar o loop do jogo

  // ==== REDE (Multiplayer) ====
  private final transient NetworkManager networkManager = new NetworkManager();
  private final transient List<RemotePlayer> remotePlayers = new CopyOnWriteArrayList<>();
  /** Contador monotônico de frames de input enviados ao servidor. */
  private transient short inputSequence = 0;
  /**
   * ID de rede local: {@code -1} = host ou singleplayer;
   * {@code >= 1} = cliente conectado (atribuído pelo host via EVT_PLAYER_ASSIGN).
   */
  private transient int localNetworkId = -1;
  /** Gerenciador de trade P2P entre jogadores. */
  private final transient PlayerTradeManager playerTradeManager = new PlayerTradeManager(this);
  /** Gerenciador de chat multiplayer. */
  private final transient ChatManager chatManager = new ChatManager(this);

  // ENTITY AND OBJECTS
  private final transient Player player = new Player(this, keyH); // Jogador principal do jogo
  private final transient Entity[][] obj = new Entity[MAX_MAP][20]; // Objetos
  private final transient Entity[][] npc = new Entity[MAX_MAP][10]; // NPCs
  private final transient Entity[][] monster = new Entity[MAX_MAP][50];
  private final transient InteractiveTile[][] iTile = new InteractiveTile[MAX_MAP][50];
  private final transient Entity[][] projectile = new Entity[MAX_MAP][20];
  private final transient List<Entity> particleList = new ArrayList<>();
  transient List<Entity> entityList = new ArrayList<>(); // Lista dinâmica para ordenação por profundidade

  // ==== GAME STATE ====
  private int gameState; // Estado atual do jogo
  public static final int TITLE_STATE = 0;
  public static final int PLAY_STATE = 1;
  public static final int PAUSE_STATE = 2;
  public static final int DIALOGUE_STATE = 3;
  public static final int CHARACTER_STATE = 4;
  public static final int OPTIONS_STATE = 5;
  public static final int GAME_OVER_STATE = 6;
  public static final int TRANSITION_STATE = 7;
  public static final int TRADE_STATE = 8;
  public static final int SLEEP_STATE = 9;
  public static final int MAP_STATE = 10;
  public static final int CUTSCENE_STATE = 11;
  public static final int PET_YARD_STATE = 12;
  public static final int BESTIARY_STATE = 13; // Bestiário (Cyclopedia)
  public static final int ACHIEVEMENT_STATE = 14; // Conquistas
  public static final int SKILL_TREE_STATE = 15;  // Árvore de Habilidades
  public static final int LOBBY_STATE = 16; // Lobby multiplayer (espera de jogadores)
  public static final int PLAYER_TRADE_STATE = 17; // Trade P2P entre jogadores

  // others
  private boolean bossBattleOn = false;

  // Debug/profiler (agregado por segundo quando debug está ativo)
  private long debugUpdateNanos = 0L;
  private long debugRenderNanos = 0L;
  private int debugFrameCount = 0;
  private long collisionFrameCounter = 0L;
  private long respawnStartMs = -1L;
  private boolean respawnUpdateLogPending = false;

  // AREA
  private int currentArea;
  private int nextArea;
  public static final int OUTSIDE = 50;
  public static final int INDOOR = 51;
  public static final int DUNGEON = 52;

  // ==== CONSTRUTOR ====
  public GamePanel() {
    this.setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
    this.setBackground(Color.black);
    this.setDoubleBuffered(true);

    this.addKeyListener(keyH);
    this.setFocusable(true);
    // Desabilita focus traversal para que TAB chegue ao KeyListener
    this.setFocusTraversalKeysEnabled(false);
  }

  // Prepara e inicializa elementos principais do jogo antes de começar o loop
  public void setupGame() {
    aSetter.setObject();
    aSetter.setNPC();
    aSetter.setMonster();
    aSetter.setInteractiveTile();
    eManager.setup();

    // Carrega configurações persistidas (volume, fullscreen, idioma)
    config.loadConfig();

    gameState = TITLE_STATE;
    currentArea = OUTSIDE;

    tempScreen = new BufferedImage(SCREEN_WIDTH, SCREEN_HEIGHT, BufferedImage.TYPE_INT_ARGB);
    g2 = (Graphics2D) tempScreen.getGraphics();

    if (fullScreenOn) {
      setFullScreen();
    }
  }

  public void resetGame(boolean restart) {

    stopMusic();
    currentArea = OUTSIDE;
    removeTempEntity();
    bossBattleOn = false;
    player.setDefaultPositions();
    player.restoreStatus();
    player.resetCounter();
    aSetter.setNPC();

    if (restart) {
      ImageCache.clear();
      aSetter.setMonster();
      player.setDefaultValues();
      aSetter.setObject();
      aSetter.setInteractiveTile();
      bestiaryService.reset();
      achievementManager.reset();
      gameStatistics.reset();

      var lighting = eManager.getLighting();
      if (lighting != null) {
        lighting.resetDay();
      }

      var weatherManager = eManager.getWeatherManager();
      if (weatherManager != null) {
        weatherManager.reset();
      }
    } else {
      aSetter.resetMonsters();
    }
  }

  public void setFullScreen() {
    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    GraphicsDevice gd = ge.getDefaultScreenDevice();
    gd.setFullScreenWindow(Main.window);

    screenWidth2 = Main.window.getWidth();
    screenHeight2 = Main.window.getHeight();
  }

  // ==== INICIALIZA A THREAD DO JOGO ====
  public void startGameThread() {
    gameThread = new Thread(this);
    gameThread.start();
  }

  // ==== LOOP PRINCIPAL DO JOGO ====
  @Override
  public void run() {

    double drawInterval = 1_000_000_000.0 / fps;
    double delta = 0;
    long lastTime = System.nanoTime();
    long currentTime;
    long timer = 0;
    int drawCount = 0;

    while (gameThread != null) {
      currentTime = System.nanoTime();

      delta += (currentTime - lastTime) / drawInterval;
      timer += (currentTime - lastTime);
      lastTime = currentTime;

      if (delta >= 1) {
        boolean debugEnabled = keyH.isShowDebugText();
        if (debugEnabled) {
          long updateStart = System.nanoTime();
          update();
          debugUpdateNanos += System.nanoTime() - updateStart;

          long renderStart = System.nanoTime();
          drawToTempScreen();
          drawToScreen();
          debugRenderNanos += System.nanoTime() - renderStart;
          debugFrameCount++;
        } else {
          update();
          drawToTempScreen();
          drawToScreen();
        }
        delta -= 1.0;
        drawCount++;
      }

      if (timer >= 1_000_000_000) {
        LOGGER.log(Level.INFO, "FPS: {0}", drawCount);
        if (keyH.isShowDebugText() && debugFrameCount > 0) {
          double avgUpdateMs = debugUpdateNanos / (double) debugFrameCount / 1_000_000.0;
          double avgRenderMs = debugRenderNanos / (double) debugFrameCount / 1_000_000.0;
          LOGGER.log(
              Level.INFO,
              "Perf avg(ms) update={0} render={1}",
              new Object[] { avgUpdateMs, avgRenderMs });
        }
        drawCount = 0;
        timer = 0;
        debugUpdateNanos = 0L;
        debugRenderNanos = 0L;
        debugFrameCount = 0;
      }
    }
  }

  // ==== ATUALIZA A LÓGICA DO JOGO ====
  public void update() {

    if (gameState == PLAY_STATE) {
      updatePlayState();
    } else if (gameState == PAUSE_STATE) {
      updatePauseState();
    } else if (gameState == LOBBY_STATE) {
      // Lobby também precisa drenar a rede para receber EVT_GAME_START
      drainNetworkQueue();
    } else if (gameState == PLAYER_TRADE_STATE) {
      // Trade P2P também drena rede para receber pacotes de trade
      drainNetworkQueue();
    }
  }

  private void updatePlayState() {
    collisionFrameCounter++;
    if (respawnUpdateLogPending) {
      logRespawnDebug("update.resume");
      respawnUpdateLogPending = false;
    }
    drainNetworkQueue();
    updatePlayer();
    updateNpcs();
    updateMonsters();
    updateProjectiles();
    updateParticles();
    updateInteractiveTiles();
    eManager.update();
    petIncubator.update();
    drainBestiaryNotifications();
    achievementToast.update(achievementManager);
    cheatCodeManager.tick();
  }

  private void updatePlayer() {
    player.update();
    for (RemotePlayer rp : remotePlayers) {
      rp.update();
    }
  }

  private void updateNpcs() {
    // Cliente: NPCs são proxies de rede, sem IA local (Fase 7)
    if (networkManager.isClient()) {
      return;
    }
    for (Entity currentNpc : npc[currentMap]) {
      if (currentNpc != null) {
        currentNpc.update();
      }
    }
  }

  private void updateMonsters() {
    // Cliente: monstros são proxies de rede, sem IA local (Fase 6.2)
    if (networkManager.isClient()) {
      return;
    }
    Entity[] currentMonsters = monster[currentMap];

    for (int i = 0; i < currentMonsters.length; i++) {
      Entity currentMonster = currentMonsters[i];
      if (currentMonster == null) {
        continue;
      }

      // Vivo e não está na animação de morte -> comportamento normal
      if (currentMonster.isAlive() && !currentMonster.isDying()) {
        currentMonster.update();
      } else if (!currentMonster.isAlive()) {
        handleDeadMonster(currentMonster, currentMonsters, i);
      }
    }
  }

  private void handleDeadMonster(Entity monster, Entity[] list, int index) {
    // Garante que o drop acontece só uma vez por morte
    if (!monster.isDropHandled()) {
      monster.checkDrop();
      monster.setDropHandled(true);
    }

    // Se estiver configurado para respawnar, só atualiza o timer
    if (monster.isRespawnEnabled()) {
      monster.updateRespawn();
    } else {
      // Sem respawn -> some pra sempre (comportamento original)
      list[index] = null;
      // Avisa clientes para nulificar o slot (Fase 7 — fix do "monstro fantasma")
      broadcastEntityRemoval(MONSTER_ID_BASE + index);
    }
  }

  private void updateProjectiles() {
    Entity[] currentProjectiles = projectile[currentMap];
    for (int i = 0; i < currentProjectiles.length; i++) {
      Entity currentProjectile = currentProjectiles[i];
      if (currentProjectile != null) {
        if (currentProjectile.isAlive()) {
          currentProjectile.update();
        }
        if (!currentProjectile.isAlive()) {
          currentProjectiles[i] = null;
        }
      }
    }
  }

  private void updateParticles() {
    for (int i = particleList.size() - 1; i >= 0; i--) {
      Entity particle = particleList.get(i);
      if (particle != null) {
        if (particle.isAlive()) {
          particle.update();
        }
        if (!particle.isAlive()) {
          particleList.remove(i);
        }
      }
    }
  }

  private void updateInteractiveTiles() {
    for (InteractiveTile tile : iTile[currentMap]) {
      if (tile != null) {
        tile.update();
      }
    }
  }

  // ==== REDE — Métodos do game loop ====

  /**
   * Ponto de integração de rede no game loop.
   * No-op em modo singleplayer ({@link NetworkManager#isMultiplayer()} = false).
   */
  private void drainNetworkQueue() {
    if (!networkManager.isMultiplayer()) {
      return;
    }
    if (networkManager.isHost()) {
      drainServerQueue();
      broadcastStateIfNeeded();
    } else {
      drainClientQueue();
      sendLocalInput();
    }
  }

  /** Host: processa todos os pacotes recebidos de clientes. */
  private void drainServerQueue() {
    IncomingPacket incoming;
    while ((incoming = networkManager.pollServerIncoming()) != null) {
      applyIncomingPacket(incoming);
    }
  }

  /**
   * Host: faz broadcast do estado das entidades a ~20 Hz.
   * A cada 3 frames de 60 Hz emite um snapshot para todos os clientes.
   */
  private void broadcastStateIfNeeded() {
    if (collisionFrameCounter % 3 != 0) {
      return;
    }
    // Estado do jogador local (host = ID 0)
    networkManager.broadcast(buildEntityState(player, 0));
    // Estado de cada jogador remoto
    for (RemotePlayer rp : remotePlayers) {
      networkManager.broadcast(buildEntityState(rp, rp.getNetworkId()));
    }
    // Estado autoritativo dos monstros do mapa atual (Fase 6.2)
    Entity[] currentMonsters = monster[currentMap];
    for (int i = 0; i < currentMonsters.length; i++) {
      Entity m = currentMonsters[i];
      if (m != null) {
        networkManager.broadcast(buildEntityState(m, MONSTER_ID_BASE + i));
      }
    }
    // Sync de pets a cada ~1s (60 frames) — Fase 12
    if (collisionFrameCounter % 60 == 0) {
      broadcastPetSync();
    }
    // Estado autoritativo dos NPCs do mapa atual (Fase 7)
    Entity[] currentNpcs = npc[currentMap];
    for (int i = 0; i < currentNpcs.length; i++) {
      Entity n = currentNpcs[i];
      if (n != null) {
        networkManager.broadcast(buildEntityState(n, NPC_ID_BASE + i));
      }
    }
  }

  /** Base do espaço de IDs de rede usado para monstros (evita colisão com jogadores). */
  private static final int MONSTER_ID_BASE = 1000;
  /** Base do espaço de IDs de rede usado para NPCs. */
  private static final int NPC_ID_BASE = 2000;
  /** Base do espaço de IDs de rede usado para objetos/itens do mundo. */
  private static final int OBJ_ID_BASE = 3000;
  /** Distância em pixels acima da qual o cliente snap-corrige o jogador local. */
  private static final int RECONCILE_THRESHOLD = 32;

  /**
   * Host: broadcast de evento {@link EventPacket#EVT_ENTITY_REMOVE} avisando
   * clientes para nulificar o slot da entidade removida.
   * @param networkId id da entidade removida (já incluindo a base do tipo)
   */
  public void broadcastEntityRemoval(int networkId) {
    if (!networkManager.isHost()) {
      return;
    }
    networkManager.broadcast(new EventPacket(
        EventPacket.EVT_ENTITY_REMOVE, 0, networkId, networkId, (byte) currentMap));
  }

  /**
   * Host: broadcast de evento de dano com sfx/partículas no momento exato.
   */
  public void broadcastDamage(int sourceId, int targetId, int amount) {
    if (!networkManager.isHost()) {
      return;
    }
    networkManager.broadcast(new EventPacket(
        EventPacket.EVT_DAMAGE, sourceId, targetId, amount, (byte) currentMap));
  }

  /**
   * Host: broadcast de spawn de item recém-dropado.
   * Cliente recriará o item via {@link EntityGenerator#getObject(String)}.
   */
  public void broadcastItemSpawn(int mapIdx, int slot, Entity item) {
    if (!networkManager.isHost()) {
      return;
    }
    networkManager.broadcast(new ItemSpawnPacket(
        (byte) mapIdx, (short) slot,
        item.getWorldX(), item.getWorldY(),
        item.getInternalId()));
  }

  /** Host: broadcast do estado do pet de cada jogador (Fase 12). */
  private void broadcastPetSync() {
    // Pet do host
    var hostPet = player.getPet();
    String hostPetId = hostPet != null ? player.getActivePetId() : "";
    int hpx = hostPet != null ? hostPet.getWorldX() : 0;
    int hpy = hostPet != null ? hostPet.getWorldY() : 0;
    networkManager.broadcast(new PetSyncPacket(0, hpx, hpy, hostPetId));

    // Nota: jogadores remotos no host não têm pets locais (pets são cosméticos
    // para os clientes); se no futuro precisar, basta adicionar aqui.
  }

  /** Host: broadcast de morte de uma entidade. */
  public void broadcastDeath(int targetId) {
    if (!networkManager.isHost()) {
      return;
    }
    networkManager.broadcast(new EventPacket(
        EventPacket.EVT_DEATH, 0, targetId, 0, (byte) currentMap));
  }

  /** Cliente: processa todos os pacotes recebidos do servidor. */
  private void drainClientQueue() {
    Packet pkt;
    while ((pkt = networkManager.pollClientIncoming()) != null) {
      applyServerPacket(pkt);
    }
  }

  /** Cliente: envia o input local a cada frame. */
  private void sendLocalInput() {
    networkManager.sendToServer(keyH.captureCurrentInput(inputSequence++));
  }

  /**
   * Host: aplica um pacote recebido de um cliente específico.
   *
   * @param incoming envelope com o pacote e o handler do cliente remetente
   */
  private void applyIncomingPacket(IncomingPacket incoming) {
    ClientHandler source = incoming.getSource();
    Packet pkt = incoming.getPacket();
    switch (pkt.getType()) {
      case HANDSHAKE -> {
        HandshakePacket hp = (HandshakePacket) pkt;
        int newId = remotePlayers.size() + 1;
        source.setAssignedPlayerId(newId);
        RemotePlayer rp = new RemotePlayer(this, hp.getPlayerName(),
            hp.getCharacterClass(), newId);
        remotePlayers.add(rp);
        // Informa ao cliente seu ID de rede para que ignore o próprio estado
        source.send(new EventPacket(
            EventPacket.EVT_PLAYER_ASSIGN, newId, 0, newId, (byte) currentMap));
        LOGGER.log(Level.INFO, "Jogador \"{0}\" conectado com ID de rede {1}",
            new Object[]{hp.getPlayerName(), newId});
      }
      case INPUT -> {
        InputPacket ip = (InputPacket) pkt;
        RemotePlayer rp = findRemotePlayerById(source.getAssignedPlayerId());
        if (rp != null) {
          rp.applyInput(ip);
        }
      }
      case TRADE -> {
        TradePacket tp = (TradePacket) pkt;
        int targetId = tp.getTargetPlayerId();
        if (targetId == 0) {
          playerTradeManager.handleTradePacket(tp);
        }
        networkManager.broadcast(tp);
      }
      case CHAT -> {
        ChatPacket cp = (ChatPacket) pkt;
        chatManager.handleChatPacket(cp);
        // Retransmite para todos os clientes
        networkManager.broadcast(cp);
      }
      case DISCONNECT -> {
        // Cliente desconectou — remove jogador remoto e cancela trade se aplicável
        int disconnectedId = source.getAssignedPlayerId();
        RemotePlayer rp = findRemotePlayerById(disconnectedId);
        if (rp != null) {
          remotePlayers.remove(rp);
          LOGGER.log(Level.INFO, "Jogador remoto ID {0} removido (desconexão)", disconnectedId);
        }
        if (playerTradeManager.getTradePartnerId() == disconnectedId) {
          playerTradeManager.onPartnerDisconnected();
        }
        // Notifica clientes remanescentes da remoção
        networkManager.broadcast(new EventPacket(
            EventPacket.EVT_ENTITY_REMOVE, 0, disconnectedId, disconnectedId, (byte) currentMap));
      }
      default -> { /* outros tipos tratados nas fases seguintes */ }
    }
  }

  /**
   * Cliente: aplica um pacote recebido do servidor host.
   *
   * @param pkt pacote desserializado recebido do servidor
   */
  private void applyServerPacket(Packet pkt) {
    switch (pkt.getType()) {
      case ENTITY_STATE -> {
        EntityStatePacket esp = (EntityStatePacket) pkt;
        int entityId = esp.getEntityId();

        // Reconciliação do jogador local (Fase 6.3): aplica correção
        // se a divergência ultrapassar o limiar; caso contrário deixa
        // a previsão local rodar.
        if (entityId == localNetworkId) {
          int dx = esp.getWorldX() - player.getWorldX();
          int dy = esp.getWorldY() - player.getWorldY();
          if (dx * dx + dy * dy > RECONCILE_THRESHOLD * RECONCILE_THRESHOLD) {
            player.setWorldX(esp.getWorldX());
            player.setWorldY(esp.getWorldY());
          }
          player.setLife(esp.getLife());
          player.setMaxLife(esp.getMaxLife());
          return;
        }

        // NPCs (Fase 7)
        if (entityId >= NPC_ID_BASE && entityId < OBJ_ID_BASE) {
          applyArrayState(npc, esp, entityId - NPC_ID_BASE);
          return;
        }
        // Monstros: ID >= MONSTER_ID_BASE → aplica direto no slot do array
        if (entityId >= MONSTER_ID_BASE && entityId < NPC_ID_BASE) {
          applyArrayState(monster, esp, entityId - MONSTER_ID_BASE);
          return;
        }

        // Demais: jogadores remotos
        RemotePlayer rp = findRemotePlayerById(entityId);
        if (rp == null) {
          rp = new RemotePlayer(this, "Jogador #" + entityId, 0, entityId);
          remotePlayers.add(rp);
        }
        rp.applyState(esp);
      }
      case EVENT -> {
        EventPacket ep = (EventPacket) pkt;
        if (ep.getEventType() == EventPacket.EVT_PLAYER_ASSIGN) {
          localNetworkId = ep.getValue();
          LOGGER.log(Level.INFO, "ID de rede local atribuído: {0}", localNetworkId);
        } else if (ep.getEventType() == EventPacket.EVT_MAP_CHANGE) {
          // Sincronização de mapa (Fase 6.1): host trocou de área
          int newMap = ep.getValue();
          if (newMap != currentMap) {
            this.currentMap = newMap;
            LOGGER.log(Level.INFO, "Mapa sincronizado: {0}", newMap);
          }
        } else if (ep.getEventType() == EventPacket.EVT_ENTITY_REMOVE) {
          // Host removeu entidade — nulificar slot correspondente (Fase 7)
          handleRemoteRemoval(ep.getValue(), ep.getMapIndex() & 0xFF);
        } else if (ep.getEventType() == EventPacket.EVT_DAMAGE) {
          // SFX de dano no momento exato (Fase 5 — eventos discretos)
          playSE(6);
        } else if (ep.getEventType() == EventPacket.EVT_DEATH) {
          playSE(11);
        } else if (ep.getEventType() == EventPacket.EVT_GAME_START) {
          // Host iniciou a partida — sai do lobby
          if (gameState == LOBBY_STATE) {
            setGameState(PLAY_STATE);
          }
        } else if (ep.getEventType() == EventPacket.EVT_CUTSCENE) {
          // Cutscene sync — só processa se este cliente é o alvo
          int myId = localNetworkId >= 0 ? localNetworkId : 0;
          if (ep.getTargetEntityId() == myId) {
            int sceneId = ep.getValue();
            if (sceneId > 0) {
              setGameState(CUTSCENE_STATE);
              csManager.startSkeletonLordScene();
            } else if (gameState == CUTSCENE_STATE) {
              setGameState(PLAY_STATE);
            }
          }
        }
      }
      case ITEM_SPAWN -> {
        ItemSpawnPacket isp = (ItemSpawnPacket) pkt;
        int mapIdx = isp.getMapIndex() & 0xFF;
        int slot = isp.getSlot() & 0xFFFF;
        if (mapIdx >= 0 && mapIdx < obj.length
            && slot >= 0 && slot < obj[mapIdx].length) {
          Entity item = eGenerator.getObject(isp.getInternalId());
          if (item != null) {
            item.setWorldX(isp.getWorldX());
            item.setWorldY(isp.getWorldY());
            obj[mapIdx][slot] = item;
          }
        }
      }
      case TRADE -> {
        TradePacket tp = (TradePacket) pkt;
        int myId = localNetworkId >= 0 ? localNetworkId : 0;
        // Só processa se o pacote é direcionado a este jogador
        if (tp.getTargetPlayerId() == myId) {
          playerTradeManager.handleTradePacket(tp);
        }
      }
      case CHAT -> {
        ChatPacket cp = (ChatPacket) pkt;
        chatManager.handleChatPacket(cp);
      }
      case PET_SYNC -> {
        PetSyncPacket ps = (PetSyncPacket) pkt;
        RemotePlayer rp = findRemotePlayerById(ps.getOwnerNetworkId());
        if (rp != null) {
          rp.applyPetSync(ps.getPetInternalId(), ps.getPetWorldX(), ps.getPetWorldY());
        }
      }
      case DISCONNECT -> {
        DisconnectPacket dp = (DisconnectPacket) pkt;
        gameUi.addMessage("Desconectado: " + dp.getReason() + " — tentando reconectar…");
        attemptReconnect();
      }
      default -> { /* outros tipos tratados nas fases seguintes */ }
    }
  }

  /**
   * Constrói um {@link EntityStatePacket} a partir de uma entidade do jogo.
   *
   * @param e        entidade a serializar
   * @param entityId identificador de rede da entidade
   * @return pacote de estado pronto para envio
   */
  private EntityStatePacket buildEntityState(Entity e, int entityId) {
    return new EntityStatePacket(
        entityId,
        (byte) currentMap,
        e.getWorldX(), e.getWorldY(),
        EntityStatePacket.encodeDirection(e.getDirection()),
        (byte) e.getSpriteNum(),
        (short) e.getLife(),
        (short) e.getMaxLife(),
        e.isAttacking(), e.isAlive(), e.isDying(),
        e.isInvincible(), e.isGuarding()
    );
  }

  /**
   * Cliente: aplica estado autoritativo recebido do host a uma entidade
   * indexada num array (monstros ou NPCs).
   */
  private void applyArrayState(Entity[][] array, EntityStatePacket esp, int index) {
    int mapIdx = esp.getMapIndex() & 0xFF;
    if (mapIdx < 0 || mapIdx >= array.length) {
      return;
    }
    Entity[] layer = array[mapIdx];
    if (index < 0 || index >= layer.length) {
      return;
    }
    Entity m = layer[index];
    if (m == null) {
      return;
    }
    m.setWorldX(esp.getWorldX());
    m.setWorldY(esp.getWorldY());
    m.setDirection(esp.getDirection());
    m.setSpriteNum(esp.getSpriteNum());
    m.setLife(esp.getLife());
    m.setMaxLife(esp.getMaxLife());
    m.setAlive(esp.isAlive());
    m.setDying(esp.isDying());
  }

  /**
   * Cliente: nulifica o slot da entidade removida pelo host.
   * O networkId carrega a base do tipo (1000=monstro, 2000=NPC, 3000=obj).
   */
  private void handleRemoteRemoval(int networkId, int mapIdx) {
    Entity[][] array;
    int base;
    if (networkId >= OBJ_ID_BASE) {
      array = obj;
      base = OBJ_ID_BASE;
    } else if (networkId >= NPC_ID_BASE) {
      array = npc;
      base = NPC_ID_BASE;
    } else if (networkId >= MONSTER_ID_BASE) {
      array = monster;
      base = MONSTER_ID_BASE;
    } else {
      return;
    }
    if (mapIdx < 0 || mapIdx >= array.length) {
      return;
    }
    int idx = networkId - base;
    Entity[] layer = array[mapIdx];
    if (idx >= 0 && idx < layer.length) {
      layer[idx] = null;
    }
  }

  /**
   * Cliente: tenta reconectar ao último host até 3 vezes (Fase 8).
   * Roda numa thread em background para não travar o game loop.
   * Em caso de falha total, volta ao TITLE_STATE.
   */
  private void attemptReconnect() {
    final String h = networkManager.getLastHost();
    final int p = networkManager.getLastPort();
    final String name = networkManager.getLastPlayerName();
    final int cls = networkManager.getLastCharacterClass();
    networkManager.stop();
    playerTradeManager.onPartnerDisconnected();
    remotePlayers.clear();
    localNetworkId = -1;
    if (h == null) {
      setGameState(TITLE_STATE);
      return;
    }
    Thread retry = new Thread(() -> {
      for (int attempt = 1; attempt <= 3; attempt++) {
        try {
          Thread.sleep(2000L);
        } catch (InterruptedException ie) {
          Thread.currentThread().interrupt();
          return;
        }
        try {
          networkManager.connectToHost(h, p, name, cls);
          gameUi.addMessage("Reconectado a " + h);
          return;
        } catch (java.io.IOException ex) {
          gameUi.addMessage("Tentativa " + attempt + "/3 falhou: " + ex.getMessage());
        }
      }
      gameUi.addMessage("Reconexão falhou — voltando ao título");
      setGameState(TITLE_STATE);
    }, "net-reconnect");
    retry.setDaemon(true);
    retry.start();
  }

  /** Localiza um {@link RemotePlayer} pelo seu ID de rede. */
  private RemotePlayer findRemotePlayerById(int networkId) {
    for (RemotePlayer rp : remotePlayers) {
      if (rp.getNetworkId() == networkId) {
        return rp;
      }
    }
    return null;
  }

  /** Drena notificações do Bestiário e exibe como mensagem na HUD. */
  private void drainBestiaryNotifications() {
    String msg;
    while ((msg = bestiaryService.pollNotification()) != null) {
      gameUi.addMessage(msg);
    }
  }

  private void updatePauseState() {
    // Lógica opcional para o estado de pausa
  }

  public void drawToTempScreen() {

    long drawStart = startDebugTimerIfNeeded();

    switch (gameState) {
      case TITLE_STATE -> drawTitleScreen();
      case MAP_STATE -> drawMapScreen();
      case LOBBY_STATE -> gameUi.draw(g2);
      case PLAYER_TRADE_STATE -> gameUi.draw(g2);
      default -> drawGameScenes();
    }

    if (keyH.isShowDebugText()) {
      gameUi.drawDebugOverlay(drawStart);
    }
  }

  private long startDebugTimerIfNeeded() {
    if (keyH.isShowDebugText()) {
      return System.nanoTime();
    }
    return 0L;
  }

  private void drawTitleScreen() {
    gameUi.draw(g2);
  }

  private void drawMapScreen() {
    map.drawFullMapScreen(g2);
  }

  private void drawGameScenes() {
    drawTiles();
    drawInteractiveTilesGraphics();
    addEntitiesToList();
    sortAndDrawEntities();
    drawEnvironment();
    drawMiniMap();
    drawCutscene();
    drawUi();
  }

  private void drawTiles() {
    tileM.draw(g2);
  }

  private void drawInteractiveTilesGraphics() {
    for (InteractiveTile tile : iTile[currentMap]) {
      if (tile != null) {
        tile.draw(g2);
      }
    }
  }

  private void addEntitiesToList() {
    entityList.add(player);
    entityList.addAll(remotePlayers);
    addEntities(npc[currentMap]);
    addEntities(obj[currentMap]);
    addEntities(monster[currentMap]);
    addEntities(projectile[currentMap]);
    addParticlesToEntityList();
  }

  private void addEntities(Entity[] entities) {
    for (Entity entity : entities) {
      if (entity != null) {
        entityList.add(entity);
      }
    }
  }

  private void addParticlesToEntityList() {
    for (Entity particle : particleList) {
      if (particle != null) {
        entityList.add(particle);
      }
    }
  }

  private void sortAndDrawEntities() {
    insertionSortEntitiesByWorldY(entityList);
    for (Entity entity : entityList) {
      entity.draw(g2);
    }
    entityList.clear();
  }

  private static void insertionSortEntitiesByWorldY(List<Entity> entities) {
    for (int i = 1; i < entities.size(); i++) {
      Entity current = entities.get(i);
      int currentY = current.getWorldY();
      int j = i - 1;

      while (j >= 0 && entities.get(j).getWorldY() > currentY) {
        entities.set(j + 1, entities.get(j));
        j--;
      }
      entities.set(j + 1, current);
    }
  }

  private void drawEnvironment() {
    eManager.draw(g2);
  }

  private void drawMiniMap() {
    map.drawMiniMap(g2);
  }

  private void drawCutscene() {
    csManager.draw(g2);
  }

  private void drawUi() {
    gameUi.draw(g2);
  }

  public void drawToScreen() {
    Graphics g = getGraphics();
    g.drawImage(tempScreen, 0, 0, screenWidth2, screenHeight2, null);
    g.dispose();
  }

  public void playMusic(int index) {
    music.setFile(index);
    music.play();
    music.loop();
  }

  public void stopMusic() {
    music.stop();
  }

  public void playSE(int index) {
    se.setFile(index);
    se.play();
  }

  public void changeArea() {
    if (nextArea != currentArea) {
      stopMusic();

      switch (nextArea) {
        case OUTSIDE -> playMusic(0);
        case INDOOR -> playMusic(18);
        case DUNGEON -> playMusic(19);
        default -> {
          /* área sem música específica */ }
      }

      aSetter.setNPC();
    }

    currentArea = nextArea;
    aSetter.resetMonsters();
  }

  public void removeTempEntity() {
    for (int mapNum = 0; mapNum < MAX_MAP; mapNum++) {
      for (int i = 0; i < obj[mapNum].length; i++) {
        if (obj[mapNum][i] != null && obj[mapNum][i].isTemp()) {
          obj[mapNum][i] = null;
        }
      }
    }
  }

  public void logRespawnDebug(String marker) {
    if (!DEBUG_RESPAWN) {
      return;
    }

    long now = System.currentTimeMillis();
    String thread = Thread.currentThread().getName();

    if ("respawn.start".equals(marker)) {
      respawnStartMs = now;
      respawnUpdateLogPending = true;
    }

    LOGGER.log(Level.INFO, "[Respawn] {0} t={1} thread={2}", new Object[] { marker, now, thread });

    if (respawnStartMs > 0
        && ("respawn.end".equals(marker) || "update.resume".equals(marker))) {
      long elapsed = now - respawnStartMs;
      LOGGER.log(Level.INFO, "[Respawn] {0} elapsedMs={1}", new Object[] { marker, elapsed });
    }
  }

  // ==== CÂMERA (clamping nas bordas do mapa) ====

  /**
   * Posição X da câmera no mundo, clampada para não ultrapassar os limites do
   * mapa.
   * Estilo SNES: a câmera para na borda e o jogador se desloca dentro da tela.
   */
  public int getCameraX() {
    int camX = player.getWorldX() - player.screenX;
    int maxCamX = Math.max(0, maxWorldCol * TILE_SIZE - SCREEN_WIDTH);
    return Math.clamp(camX, 0, maxCamX);
  }

  /**
   * Posição Y da câmera no mundo, clampada para não ultrapassar os limites do
   * mapa.
   */
  public int getCameraY() {
    int camY = player.getWorldY() - player.screenY;
    int maxCamY = Math.max(0, maxWorldRow * TILE_SIZE - SCREEN_HEIGHT);
    return Math.clamp(camY, 0, maxCamY);
  }

  // ==== GETTERS / SETTERS PARA CAMPOS ENCAPSULADOS ====

  public int getCurrentMap() {
    return currentMap;
  }

  public void setCurrentMap(int currentMap) {
    int previous = this.currentMap;
    this.currentMap = currentMap;
    // Sincronização de mapa (Fase 6.1): host avisa todos os clientes
    if (previous != currentMap && networkManager.isHost()) {
      networkManager.broadcast(new EventPacket(
          EventPacket.EVT_MAP_CHANGE, 0, 0, currentMap, (byte) currentMap));
    }
  }

  public boolean isFullScreenOn() {
    return fullScreenOn;
  }

  public void setFullScreenOn(boolean fullScreenOn) {
    this.fullScreenOn = fullScreenOn;
  }

  public int getMaxWorldCol() {
    return maxWorldCol;
  }

  public void setMaxWorldCol(int maxWorldCol) {
    this.maxWorldCol = maxWorldCol;
  }

  public int getMaxWorldRow() {
    return maxWorldRow;
  }

  public void setMaxWorldRow(int maxWorldRow) {
    this.maxWorldRow = maxWorldRow;
  }

  public TileManager getTileM() {
    return tileM;
  }

  public KeyHandler getKeyH() {
    return keyH;
  }

  public InputBindings getInputBindings() {
    return inputBindings;
  }

  public CollisionChecker getcChecker() {
    return cChecker;
  }

  public AssetSetter getaSetter() {
    return aSetter;
  }

  public UI getGameUi() {
    return gameUi;
  }

  public EventHandler geteHandler() {
    return eHandler;
  }

  public PathFinder getpFinder() {
    return pFinder;
  }

  public EntityGenerator geteGenerator() {
    return eGenerator;
  }

  public CutsceneManager getCsManager() {
    return csManager;
  }

  public Player getPlayer() {
    return player;
  }

  public Entity[][] getObj() {
    return obj;
  }

  public Entity[][] getNpc() {
    return npc;
  }

  public Entity[][] getMonster() {
    return monster;
  }

  public InteractiveTile[][] getiTile() {
    return iTile;
  }

  public Entity[][] getProjectile() {
    return projectile;
  }

  public List<Entity> getParticleList() {
    return particleList;
  }

  public int getGameState() {
    return gameState;
  }

  public void setGameState(int newState) {
    int oldState = this.gameState;
    this.gameState = newState;

    if (oldState == PLAY_STATE && newState != PLAY_STATE) {
      playTimeTracker.pause();
    } else if (oldState != PLAY_STATE && newState == PLAY_STATE) {
      playTimeTracker.resume();
    }
  }

  public boolean isBossBattleOn() {
    return bossBattleOn;
  }

  public void setBossBattleOn(boolean bossBattleOn) {
    this.bossBattleOn = bossBattleOn;
  }

  public int getCurrentArea() {
    return currentArea;
  }

  public void setCurrentArea(int currentArea) {
    this.currentArea = currentArea;
  }

  public int getNextArea() {
    return nextArea;
  }

  public void setNextArea(int nextArea) {
    this.nextArea = nextArea;
  }

  public LanguageManager getLanguageManager() {
    return languageManager;
  }

  public Sound getMusic() {
    return music;
  }

  public Sound getSe() {
    return se;
  }

  public Config getConfig() {
    return config;
  }

  public EnvironmentManager getEnvironmentManager() {
    return eManager;
  }

  public SaveLoad getSaveLoad() {
    return saveLoad;
  }

  public SaveManager getSaveManager() {
    return saveManager;
  }

  public PetIncubator getPetIncubator() {
    return petIncubator;
  }

  public BestiaryService getBestiaryService() {
    return bestiaryService;
  }

  public AchievementManager getAchievementManager() {
    return achievementManager;
  }

  public AchievementToast getAchievementToast() {
    return achievementToast;
  }

  public CheatCodeManager getCheatCodeManager() {
    return cheatCodeManager;
  }

  public PlayTimeTracker getPlayTimeTracker() {
    return playTimeTracker;
  }

  public GameStatistics getGameStatistics() {
    return gameStatistics;
  }

  public long getCollisionFrameCounter() {
    return collisionFrameCounter;
  }

  /** Facade de rede — use para iniciar host/cliente e enviar/receber pacotes. */
  public NetworkManager getNetworkManager() {
    return networkManager;
  }

  /** Gerenciador de chat multiplayer. */
  public ChatManager getChatManager() {
    return chatManager;
  }

  /** Gerenciador de trade P2P entre jogadores. */
  public PlayerTradeManager getPlayerTradeManager() {
    return playerTradeManager;
  }

  /** Exibe uma mensagem temporária na HUD. */
  public void addUiMessage(String msg) {
    gameUi.addMessage(msg);
  }

  /** ID de rede local atribuído pelo host (0 = host, -1 = não atribuído). */
  public int getLocalNetworkId() {
    return localNetworkId;
  }

  /** Lista de jogadores remotos visíveis nesta instância do jogo. */
  public List<RemotePlayer> getRemotePlayers() {
    return remotePlayers;
  }
}

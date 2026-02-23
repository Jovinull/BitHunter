package main;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JPanel;

import ai.PathFinder;
import bestiary.BestiaryService;
import data.PlayTimeTracker;
import data.SaveLoad;
import data.SaveManager;
import entity.Entity;
import entity.Player;
import environment.EnvironmentManager;
import pets.PetIncubator;
import tile.Map;
import tile.TileManager;
import tile_interactive.InteractiveTile;
import ui.UI;

// Painel principal do jogo. Responsável por configurar, atualizar e desenhar o jogo.
// Implementa Runnable para rodar o loop de jogo em uma thread separada.
public class GamePanel extends JPanel implements Runnable {

  private static final Logger LOGGER = Logger.getLogger(GamePanel.class.getName());
  private static final boolean DEBUG_RESPAWN = false;
  private static final Comparator<Entity> ENTITY_Y_COMPARATOR =
      Comparator.comparingInt(Entity::getWorldY);

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
  private final transient CheatCodeManager cheatCodeManager = new CheatCodeManager(this);
  private final transient PlayTimeTracker playTimeTracker = new PlayTimeTracker();
  transient Thread gameThread; // Thread para rodar o loop do jogo

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

  // others
  private boolean bossBattleOn = false;

  // Debug/profiler (agregado por segundo quando debug está ativo)
  private long debugUpdateNanos = 0L;
  private long debugRenderNanos = 0L;
  private int debugFrameCount = 0;
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
    aSetter.setMonster();

    if (restart) {
      player.setDefaultValues();
      aSetter.setObject();
      aSetter.setInteractiveTile();
      bestiaryService.reset();

      var lighting = eManager.getLighting();
      if (lighting != null) {
        lighting.resetDay();
      }
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
              new Object[] {avgUpdateMs, avgRenderMs});
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
    }
  }

  private void updatePlayState() {
    if (respawnUpdateLogPending) {
      logRespawnDebug("update.resume");
      respawnUpdateLogPending = false;
    }
    updatePlayer();
    updateNpcs();
    updateMonsters();
    updateProjectiles();
    updateParticles();
    updateInteractiveTiles();
    eManager.update();
    petIncubator.update();
    drainBestiaryNotifications();
    cheatCodeManager.tick();
  }

  private void updatePlayer() {
    player.update();
  }

  private void updateNpcs() {
    for (Entity currentNpc : npc[currentMap]) {
      if (currentNpc != null) {
        currentNpc.update();
      }
    }
  }

  private void updateMonsters() {
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
      default -> drawGameScenes();
    }

    if (keyH.isShowDebugText()) {
      drawDebugInfo(drawStart);
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
    Collections.sort(entityList, ENTITY_Y_COMPARATOR);
    for (Entity entity : entityList) {
      entity.draw(g2);
    }
    entityList.clear();
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

  private void drawDebugInfo(long drawStart) {
    long drawEnd = System.nanoTime();
    long passed = drawEnd - drawStart;

    g2.setFont(gameUi.getDebugFont());
    g2.setColor(Color.white);
    int x = 10;
    int y = 400;
    int lineHeight = 20;

    g2.drawString("WorldX " + player.getWorldX(), x, y);
    y += lineHeight;
    g2.drawString("WorldY " + player.getWorldY(), x, y);
    y += lineHeight;
    g2.drawString("Col " + player.getCol(), x, y);
    y += lineHeight;
    g2.drawString("Row " + player.getRow(), x, y);
    y += lineHeight;
    g2.drawString("Draw Time: " + passed, x, y);
    y += lineHeight;
    boolean god = keyH.isGodModeOn();
    g2.drawString("God Mode (NoClip): " + god, x, y);
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
        default -> { /* área sem música específica */ }
      }

      aSetter.setNPC();
    }

    currentArea = nextArea;
    aSetter.setMonster();
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

    LOGGER.log(Level.INFO, "[Respawn] {0} t={1} thread={2}", new Object[] {marker, now, thread});

    if (respawnStartMs > 0
        && ("respawn.end".equals(marker) || "update.resume".equals(marker))) {
      long elapsed = now - respawnStartMs;
      LOGGER.log(Level.INFO, "[Respawn] {0} elapsedMs={1}", new Object[] {marker, elapsed});
    }
  }

  // ==== CÂMERA (clamping nas bordas do mapa) ====

  /**
   * Posição X da câmera no mundo, clampada para não ultrapassar os limites do mapa.
   * Estilo SNES: a câmera para na borda e o jogador se desloca dentro da tela.
   */
  public int getCameraX() {
    int camX = player.getWorldX() - player.screenX;
    int maxCamX = Math.max(0, maxWorldCol * TILE_SIZE - SCREEN_WIDTH);
    return Math.clamp(camX, 0, maxCamX);
  }

  /**
   * Posição Y da câmera no mundo, clampada para não ultrapassar os limites do mapa.
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
    this.currentMap = currentMap;
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
      if (playTimeTracker.isRunning()) {
        playTimeTracker.resume();
      }
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

  public CheatCodeManager getCheatCodeManager() {
    return cheatCodeManager;
  }

  public PlayTimeTracker getPlayTimeTracker() {
    return playTimeTracker;
  }
}

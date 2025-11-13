package main;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
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
import data.SaveLoad;
import entity.Entity;
import entity.Player;
import environment.EnvironmentManager;
import tile.Map;
import tile.TileManager;
import tile_interactive.InteractiveTile;

// Painel principal do jogo. Responsável por configurar, atualizar e desenhar o jogo.
// Implementa Runnable para rodar o loop de jogo em uma thread separada.
public class GamePanel extends JPanel implements Runnable {

  private static final Logger LOGGER = Logger.getLogger(GamePanel.class.getName());

  // ==== CONFIGURAÇÕES DE TELA (visível) ====
  private static final int originalTileSize = 16; // Tamanho base de um tile (16x16 pixels)
  private static final int scale = 3; // Fator de escala aplicado ao tile

  public static final int tileSize = originalTileSize * scale; // Tile final com escala (48x48 pixels)
  public static final int maxScreenCol = 20; // Número de colunas visíveis na tela
  public static final int maxScreenRow = 12; // Número de linhas visíveis na tela
  public static final int screenWidth = tileSize * maxScreenCol; // Largura total da tela (960 px)
  public static final int screenHeight = tileSize * maxScreenRow; // Altura total da tela (576 px)

  public static final int maxMap = 10;
  private int currentMap = 0;

  // FOR FULL SCREEN
  int screenWidth2 = screenWidth;
  int screenHeight2 = screenHeight;
  private transient BufferedImage tempScreen;
  private transient Graphics2D g2;
  private boolean fullScreenOn = false;

  // ==== CONFIGURAÇÕES DO MUNDO ====
  private int maxWorldCol;
  private int maxWorldRow;

  // ==== CONFIGURAÇÃO DE FPS ====
  int fps = 60; // Quadros por segundo

  // ==== COMPONENTES DO JOGO ====
  private transient TileManager tileM = new TileManager(this); // Gerencia o carregamento e desenho dos tiles
  private transient KeyHandler keyH = new KeyHandler(this); // Captura entrada de teclado
  transient Sound music = new Sound(); // Gerencia a música do jogo
  transient Sound se = new Sound(); // Gerencia os efeitos sonoros do jogo
  private transient CollisionChecker cChecker = new CollisionChecker(this); // Verifica colisões
  private transient AssetSetter aSetter = new AssetSetter(this); // Configura os objetos do jogo
  private transient UI gameUi = new UI(this); // Interface do usuário (HUD, mensagens, etc.)
  private transient EventHandler eHandler = new EventHandler(this);
  transient Config config = new Config(this);
  private transient PathFinder pFinder = new PathFinder(this);
  transient EnvironmentManager eManager = new EnvironmentManager(this);
  transient Map map = new Map(this);
  transient SaveLoad saveLoad = new SaveLoad(this);
  private transient EntityGenerator eGenerator = new EntityGenerator(this);
  private transient CutsceneManager csManager = new CutsceneManager(this);
  transient Thread gameThread; // Thread para rodar o loop do jogo

  // ENTITY AND OBJECTS
  private transient Player player = new Player(this, keyH); // Jogador principal do jogo
  private transient Entity[][] obj = new Entity[maxMap][20]; // Objetos
  private transient Entity[][] npc = new Entity[maxMap][10]; // NPCs
  private transient Entity[][] monster = new Entity[maxMap][20];
  private transient InteractiveTile[][] iTile = new InteractiveTile[maxMap][50];
  private transient Entity[][] projectile = new Entity[maxMap][20];
  private transient List<Entity> particleList = new ArrayList<>();
  transient List<Entity> entityList = new ArrayList<>(); // Lista dinâmica para ordenação por profundidade

  // ==== GAME STATE ====
  private int gameState; // Estado atual do jogo
  public static final int titleState = 0;
  public static final int playState = 1;
  public static final int pauseState = 2;
  public static final int dialogueState = 3;
  public static final int characterState = 4;
  public static final int optionsState = 5;
  public static final int gameOverState = 6;
  public static final int transitionState = 7;
  public static final int tradeState = 8;
  public static final int sleepState = 9;
  public static final int mapState = 10;
  public static final int cutsceneState = 11;

  // others
  private boolean bossBattleOn = false;

  // AREA
  private int currentArea;
  private int nextArea;
  public static final int outside = 50;
  public static final int indoor = 51;
  public static final int dungeon = 52;

  // ==== CONSTRUTOR ====
  public GamePanel() {
    this.setPreferredSize(new Dimension(screenWidth, screenHeight));
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

    gameState = titleState;
    currentArea = outside;

    tempScreen = new BufferedImage(screenWidth, screenHeight, BufferedImage.TYPE_INT_ARGB);
    g2 = (Graphics2D) tempScreen.getGraphics();

    if (fullScreenOn) {
      setFullScreen();
    }
  }

  public void resetGame(boolean restart) {

    stopMusic();
    currentArea = outside;
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
        update();
        drawToTempScreen();
        drawToScreen();
        delta--;
        drawCount++;
      }

      if (timer >= 1_000_000_000) {
        LOGGER.log(Level.INFO, "FPS: {0}", drawCount);
        drawCount = 0;
        timer = 0;
      }
    }
  }

  // ==== ATUALIZA A LÓGICA DO JOGO ====
  public void update() {

    if (gameState == playState) {
      updatePlayState();
    } else if (gameState == pauseState) {
      updatePauseState();
    }
  }

  private void updatePlayState() {
    updatePlayer();
    updateNpcs();
    updateMonsters();
    updateProjectiles();
    updateParticles();
    updateInteractiveTiles();
    eManager.update();
  }

  private void updatePlayer() {
    player.update();
  }

  private void updateNpcs() {
    Entity[] currentNpcs = npc[currentMap];
    for (int i = 0; i < currentNpcs.length; i++) {
      Entity currentNpc = currentNpcs[i];
      if (currentNpc != null) {
        currentNpc.update();
      }
    }
  }

  private void updateMonsters() {
    Entity[] currentMonsters = monster[currentMap];
    for (int i = 0; i < currentMonsters.length; i++) {
      Entity currentMonster = currentMonsters[i];
      if (currentMonster != null) {
        if (currentMonster.isAlive() && !currentMonster.isDying()) {
          currentMonster.update();
        }
        if (!currentMonster.isAlive()) {
          currentMonster.checkDrop();
          currentMonsters[i] = null;
        }
      }
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
    InteractiveTile[] currentTiles = iTile[currentMap];
    for (int i = 0; i < currentTiles.length; i++) {
      InteractiveTile interactiveTile = currentTiles[i];
      if (interactiveTile != null) {
        interactiveTile.update();
      }
    }
  }

  private void updatePauseState() {
    // Lógica opcional para o estado de pausa
  }

  public void drawToTempScreen() {

    long drawStart = startDebugTimerIfNeeded();

    switch (gameState) {
      case titleState -> drawTitleScreen();
      case mapState -> drawMapScreen();
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
    InteractiveTile[] currentTiles = iTile[currentMap];
    for (int i = 0; i < currentTiles.length; i++) {
      InteractiveTile interactiveTile = currentTiles[i];
      if (interactiveTile != null) {
        interactiveTile.draw(g2);
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
    Collections.sort(entityList, Comparator.comparingInt(Entity::getWorldY));
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

    g2.setFont(new Font("Arial", Font.PLAIN, 20));
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
    g2.drawString("God Mode: " + keyH.isGodModeOn(), x, y);
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

      if (nextArea == outside) {
        playMusic(0);
      } else if (nextArea == indoor) {
        playMusic(18);
      } else if (nextArea == dungeon) {
        playMusic(19);
      }

      aSetter.setNPC();
    }

    currentArea = nextArea;
    aSetter.setMonster();
  }

  public void removeTempEntity() {
    for (int mapNum = 0; mapNum < maxMap; mapNum++) {
      for (int i = 0; i < obj[mapNum].length; i++) {
        if (obj[mapNum][i] != null && obj[mapNum][i].isTemp()) {
          obj[mapNum][i] = null;
        }
      }
    }
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

  public void setGameState(int gameState) {
    this.gameState = gameState;
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
}

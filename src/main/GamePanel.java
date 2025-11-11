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

  // ==== CONFIGURAÇÕES DE TELA (visível) ====
  final int originalTileSize = 16; // Tamanho base de um tile (16x16 pixels)
  final int scale = 3; // Fator de escala aplicado ao tile

  public final int tileSize = originalTileSize * scale; // Tile final com escala (48x48 pixels)
  public final int maxScreenCol = 20; // Número de colunas visíveis na tela
  public final int maxScreenRow = 12; // Número de linhas visíveis na tela
  public final int screenWidth = tileSize * maxScreenCol; // Largura total da tela (960 px)
  public final int screenHeight = tileSize * maxScreenRow; // Altura total da tela (576 px)

  public final int maxMap = 10;
  public int currentMap = 0;

  // FOR FULL SCREEN
  int screenWidth2 = screenWidth;
  int screenHeight2 = screenHeight;
  BufferedImage tempScreen;
  Graphics2D g2;
  public boolean fullScreenOn = false;

  // ==== CONFIGURAÇÕES DO MUNDO ====
  public int maxWorldCol;
  public int maxWorldRow;

  // ==== CONFIGURAÇÃO DE FPS ====
  int FPS = 60; // Quadros por segundo

  // ==== COMPONENTES DO JOGO ====
  public TileManager tileM = new TileManager(this); // Gerencia o carregamento e desenho dos tiles
  public KeyHandler keyH = new KeyHandler(this); // Captura entrada de teclado
  Sound music = new Sound(); // Gerencia a música do jogo
  Sound se = new Sound(); // Gerencia os efeitos sonoros do jogo
  public CollisionChecker cChecker = new CollisionChecker(this); // Verifica colisões
  public AssetSetter aSetter = new AssetSetter(this); // Configura os objetos do jogo
  public UI ui = new UI(this); // Interface do usuário (HUD, mensagens, etc.)
  public EventHandler eHandler = new EventHandler(this);
  Config config = new Config(this);
  public PathFinder pFinder = new PathFinder(this);
  EnvironmentManager eManager = new EnvironmentManager(this);
  Map map = new Map(this);
  SaveLoad saveLoad = new SaveLoad(this);
  public EntityGenerator eGenerator = new EntityGenerator(this);
  public CutsceneManager csManager = new CutsceneManager(this);
  Thread gameThread; // Thread para rodar o loop do jogo

  // ENTITY AND OBJECTS
  public Player player = new Player(this, keyH); // Jogador principal do jogo
  public Entity obj[][] = new Entity[maxMap][20]; // Objetos
  public Entity npc[][] = new Entity[maxMap][10]; // NPCs
  public Entity monster[][] = new Entity[maxMap][20];
  public InteractiveTile iTile[][] = new InteractiveTile[maxMap][50];
  public Entity projectile[][] = new Entity[maxMap][20];
  public ArrayList<Entity> particleList = new ArrayList<>();
  ArrayList<Entity> entityList = new ArrayList<>(); // Lista dinâmica para ordenação por profundidade

  // ==== GAME STATE ====
  public int gameState; // Estado atual do jogo
  public final int titleState = 0;
  public final int playState = 1;
  public final int pauseState = 2;
  public final int dialogueState = 3;
  public final int characterState = 4;
  public final int optionsState = 5;
  public final int gameOverState = 6;
  public final int transitionState = 7;
  public final int tradeState = 8;
  public final int sleepState = 9;
  public final int mapState = 10;
  public final int cutsceneState = 11;

  // others
  public boolean bossBattleOn = false;

  // AREA
  public int currentArea;
  public int nextArea;
  public final int outside = 50;
  public final int indoor = 51;
  public final int dungeon = 52;

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

    double drawInterval = 1_000_000_000.0 / FPS;
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
        System.out.println("FPS: " + drawCount);
        drawCount = 0;
        timer = 0;
      }
    }
  }

  // ==== ATUALIZA A LÓGICA DO JOGO ====
  public void update() {

    if (gameState == playState) {
      // Player
      player.update();

      // NPCs
      for (int i = 0; i < npc[1].length; i++) {
        if (npc[currentMap][i] != null) {
          npc[currentMap][i].update();
        }
      }

      // Monstros
      for (int i = 0; i < monster[1].length; i++) {
        Entity m = monster[currentMap][i];
        if (m != null) {
          if (m.isAlive() && !m.isDying()) {
            m.update();
          }
          if (!m.isAlive()) {
            m.checkDrop();
            monster[currentMap][i] = null;
          }
        }
      }

      // Projetis
      for (int i = 0; i < projectile[1].length; i++) {
        Entity p = projectile[currentMap][i];
        if (p != null) {
          if (p.isAlive()) {
            p.update();
          }
          if (!p.isAlive()) {
            projectile[currentMap][i] = null;
          }
        }
      }

      // Partículas
      for (int i = 0; i < particleList.size(); i++) {
        Entity p = particleList.get(i);
        if (p != null) {
          if (p.isAlive()) {
            p.update();
          }
          if (!p.isAlive()) {
            particleList.remove(i);
            i--;
          }
        }
      }

      // Interactive Tiles
      for (int i = 0; i < iTile[1].length; i++) {
        if (iTile[currentMap][i] != null) {
          iTile[currentMap][i].update();
        }
      }

      eManager.update();
    }

    if (gameState == pauseState) {
      // Lógica opcional para o estado de pausa
    }
  }

  public void drawToTempScreen() {

    // DEBUG
    long drawStart = 0;
    if (keyH.isShowDebugText()) {
      drawStart = System.nanoTime();
    }

    // Tela de título
    if (gameState == titleState) {
      ui.draw(g2);
    }

    // MAP SCREEN
    else if (gameState == mapState) {
      map.drawFullMapScreen(g2);
    }

    // OTHERS
    else {
      // TILE
      tileM.draw(g2);

      // INTERACTIVE TILES
      for (int i = 0; i < iTile[1].length; i++) {
        if (iTile[currentMap][i] != null) {
          iTile[currentMap][i].draw(g2);
        }
      }

      // ADD ENTITIES TO THE LIST
      entityList.add(player);

      for (int i = 0; i < npc[1].length; i++) {
        if (npc[currentMap][i] != null) {
          entityList.add(npc[currentMap][i]);
        }
      }

      for (int i = 0; i < obj[1].length; i++) {
        if (obj[currentMap][i] != null) {
          entityList.add(obj[currentMap][i]);
        }
      }

      for (int i = 0; i < monster[1].length; i++) {
        if (monster[currentMap][i] != null) {
          entityList.add(monster[currentMap][i]);
        }
      }

      for (int i = 0; i < projectile[1].length; i++) {
        if (projectile[currentMap][i] != null) {
          entityList.add(projectile[currentMap][i]);
        }
      }

      for (int i = 0; i < particleList.size(); i++) {
        if (particleList.get(i) != null) {
          entityList.add(particleList.get(i));
        }
      }

      // SORT (ordena por worldY para dar efeito de profundidade)
      Collections.sort(entityList, Comparator.comparingInt(Entity::getWorldY));

      // DRAW
      for (int i = 0; i < entityList.size(); i++) {
        entityList.get(i).draw(g2);
      }
      entityList.clear();

      // ENVIRONMENT
      eManager.draw(g2);

      // MINI MAP
      map.drawMiniMap(g2);

      // CUTSCENE
      csManager.draw(g2);

      // UI
      ui.draw(g2);
    }

    // DEBUG
    if (keyH.isShowDebugText()) {
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
      for (int i = 0; i < obj[1].length; i++) {
        if (obj[mapNum][i] != null && obj[mapNum][i].isTemp()) {
          obj[mapNum][i] = null;
        }
      }
    }
  }
}

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
  public CollisionChecker cChecker = new CollisionChecker(this); // Verifica colisões entre entidades e tiles
  public AssetSetter aSetter = new AssetSetter(this); // Configura os objetos do jogo (itens, inimigos, etc.)
  public UI ui = new UI(this); // Interface do usuário (HUD, mensagens, etc.)
  public EventHandler eHandler = new EventHandler(this);
  Config config = new Config(this);
  public PathFinder pFinder = new PathFinder(this);
  EnvironmentManager eManager = new EnvironmentManager(this);
  Map map = new Map(this);
  SaveLoad saveLoad = new SaveLoad(this);
  public EntityGenerator eGenerator = new EntityGenerator(this);
  Thread gameThread; // Thread para rodar o loop do jogo

  // ENTITY AND OBJECTS
  public Player player = new Player(this, keyH); // Jogador principal do jogo
  public Entity obj[][] = new Entity[maxMap][20]; // Array para armazenar objetos do jogo
  public Entity npc[][] = new Entity[maxMap][10]; // Array para armazenar NPCs (personagens não jogáveis)
  public Entity monster[][] = new Entity[maxMap][20];
  public InteractiveTile iTile[][] = new InteractiveTile[maxMap][50];
  public Entity projectile[][] = new Entity[maxMap][20];
  // public ArrayList<Entity> projectileList = new ArrayList<>();
  public ArrayList<Entity> particleList = new ArrayList<>();
  ArrayList<Entity> entityList = new ArrayList<>(); // Lista dinâmica para armazenar entidades

  // ==== GAME STATE ====
  public int gameState; // Estado atual do jogo
  public final int titleState = 0; // Estado do título (menu inicial)
  public final int playState = 1; // Estado de jogo (jogando)
  public final int pauseState = 2; // Estado de pausa
  public final int dialogueState = 3; // Estado de diálogo (interações com NPCs)
  public final int characterState = 4; // Estado de status do personagem
  public final int optionsState = 5; // Estado de opções (configurações do jogo)
  public final int gameOverState = 6; // Estado de game over
  public final int transitionState = 7; // Estado de transição entre cenas
  public final int tradeState = 8; // Estado de comércio com NPCs
  public final int sleepState = 9; // Estado de sono
  public final int mapState = 10;

  // AREA
  public int currentArea;
  public int nextArea;
  public final int outside = 50;
  public final int indoor = 51;
  public final int dungeon = 52;

  // ==== CONSTRUTOR ====
  public GamePanel() {
    this.setPreferredSize(new Dimension(screenWidth, screenHeight)); // Define tamanho da tela
    this.setBackground(Color.black); // Cor de fundo
    this.setDoubleBuffered(true); // Habilita buffer duplo para evitar flickering

    this.addKeyListener(keyH); // Liga o controle de teclado ao painel
    this.setFocusable(true); // Permite que o painel receba foco do teclado
  }

  // Prepara e inicializa elementos principais do jogo antes de começar o loop
  public void setupGame() {
    // Instancia e posiciona todos os objetos (chaves, portas, baús)
    aSetter.setObject();
    aSetter.setNPC();
    aSetter.setMonster();
    aSetter.setInteractiveTile();
    eManager.setup();

    gameState = titleState; // Define o estado inicial do jogo como "menu de título"
    currentArea = outside;

    tempScreen = new BufferedImage(screenWidth, screenHeight, BufferedImage.TYPE_INT_ARGB);
    g2 = (Graphics2D) tempScreen.getGraphics();

    if (fullScreenOn == true) {
      setFullScreen();
    }
  }

  public void resetGame(boolean restart) {

    currentArea = outside;
    player.setDefaultPositions();
    player.restoreStatus();
    player.resetCounter();
    aSetter.setNPC();
    aSetter.setMonster();

    if (restart == true) {
      player.setDefaultValues();
      aSetter.setObject();
      aSetter.setInteractiveTile();
      eManager.lighting.resetDay();
    }
  }

  public void setFullScreen() {

    // GET LOCAL SCREEN DEVICE
    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    GraphicsDevice gd = ge.getDefaultScreenDevice();
    gd.setFullScreenWindow(Main.window);

    // GET FULL SCREEN WIDTH AND HEIGHT
    screenWidth2 = Main.window.getWidth();
    screenHeight2 = Main.window.getHeight();
  }

  // ==== INICIALIZA A THREAD DO JOGO ====
  public void startGameThread() {
    gameThread = new Thread(this); // Cria a thread com o próprio painel como Runnable
    gameThread.start(); // Inicia o método run() na thread
  }

  // ==== LOOP PRINCIPAL DO JOGO ====
  @Override
  public void run() {

    // Controla o tempo entre os frames com base no FPS
    double drawInterval = 1000000000.0 / FPS; // Tempo alvo entre cada frame (em nanossegundos)
    double delta = 0;
    long lastTime = System.nanoTime(); // Marca o tempo do frame anterior
    long currentTime;
    long timer = 0;
    int drawCount = 0;

    while (gameThread != null) {
      currentTime = System.nanoTime();

      delta += (currentTime - lastTime) / drawInterval;
      timer += (currentTime - lastTime);
      lastTime = currentTime;

      if (delta >= 1) {
        update(); // Atualiza lógica do jogo (movimento, eventos, etc.)
        drawToTempScreen(); // Desenha o jogo na tela temporária
        drawToScreen(); // Desenha a tela temporária na tela real
        delta--;
        drawCount++;
      }

      // A cada segundo, mostra o FPS no console e reinicia o contador
      if (timer >= 1000000000) {
        System.out.println("FPS: " + drawCount);
        drawCount = 0;
        timer = 0;
      }
    }
  }

  // ==== ATUALIZA A LÓGICA DO JOGO ====
  public void update() {

    // Verifica se o jogo está em estado de "jogando" (ativo)
    if (gameState == playState) {

      player.update(); // Atualiza o jogador: movimentação, colisões, animações etc.

      // Atualiza todos os NPCs que estão ativos no mapa
      for (int i = 0; i < npc[1].length; i++) {
        if (npc[currentMap][i] != null) {
          npc[currentMap][i].update(); // Executa a lógica do NPC (movimento aleatório, animação, ações)
        }
      }

      for (int i = 0; i < monster[1].length; i++) {
        if (monster[currentMap][i] != null) {
          if (monster[currentMap][i].alive == true && monster[currentMap][i].dying == false) {
            monster[currentMap][i].update();
          }

          if (monster[currentMap][i].alive == false) {
            monster[currentMap][i].checkDrop();
            monster[currentMap][i] = null;
          }
        }
      }

      for (int i = 0; i < projectile[1].length; i++) {
        if (projectile[currentMap][i] != null) {
          if (projectile[currentMap][i].alive == true) {
            projectile[currentMap][i].update();
          }

          if (projectile[currentMap][i].alive == false) {
            projectile[currentMap][i] = null;
          }
        }
      }

      for (int i = 0; i < particleList.size(); i++) {
        if (particleList.get(i) != null) {
          if (particleList.get(i).alive == true) {
            particleList.get(i).update();
          }

          if (particleList.get(i).alive == false) {
            particleList.remove(i);
          }
        }
      }

      for (int i = 0; i < iTile[1].length; i++) {
        if (iTile[currentMap][i] != null) {
          iTile[currentMap][i].update();
        }
      }
      eManager.update();
    }

    // Se o jogo estiver pausado, essa parte é executada
    if (gameState == pauseState) {
      // Lógica para quando o jogo estiver em pausa
      // Ex: animações leves, música ambiente, overlays de menu — se desejar
      // implementar
    }
  }

  public void drawToTempScreen() {

    // DEBUG
    long drawStart = 0;
    if (keyH.showDebugText == true) {
      drawStart = System.nanoTime();
    }

    // Tela de título (menu inicial)
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

      // SORT
      Collections.sort(entityList, new Comparator<Entity>() {

        @Override
        public int compare(Entity e1, Entity e2) {

          int result = Integer.compare(e1.worldY, e2.worldY);
          return result;
        }
      });

      for (int i = 0; i < entityList.size(); i++) {
        entityList.get(i).draw(g2); // Desenha cada entidade na lista
      }

      entityList.clear();

      // ENVIRONMENT
      eManager.draw(g2);

      // MINI MAP
      map.drawMiniMap(g2);

      // UI
      ui.draw(g2);
    }

    // DEBUG
    if (keyH.showDebugText == true) {
      long drawEnd = System.nanoTime();
      long passed = drawEnd - drawStart;

      g2.setFont(new Font("Arial", Font.PLAIN, 20));
      g2.setColor(Color.white);
      int x = 10;
      int y = 400;
      int lineHeight = 20;

      g2.drawString("WorldX " + player.worldX, x, y);
      y += lineHeight;
      g2.drawString("WorldY " + player.worldY, x, y);
      y += lineHeight;
      g2.drawString("Col " + (player.worldX + player.solidArea.x) / tileSize, x, y);
      y += lineHeight;
      g2.drawString("Row " + (player.worldY + player.solidArea.y) / tileSize, x, y);
      y += lineHeight;
      g2.drawString("Draw Time: " + passed, x, y);
      y += lineHeight;
      g2.drawString("God Mode: " + keyH.godModeOn, x, y);
    }
  }

  public void drawToScreen() {

    Graphics g = getGraphics();
    g.drawImage(tempScreen, 0, 0, screenWidth2, screenHeight2, null);
    g.dispose();
  }

  public void playMusic(int index) {

    music.setFile(index); // Define o arquivo de som a ser tocado
    music.play(); // Toca o som
    music.loop(); // Reproduz em loop até ser parado
  }

  public void stopMusic() {

    music.stop(); // Para a música atual
  }

  public void playSE(int index) {

    se.setFile(index); // Define o arquivo de efeito sonoro
    se.play(); // Toca o efeito sonoro
  }

  public void changeArea() {

    if (nextArea != currentArea) {

      stopMusic();

      if (nextArea == outside) {
        playMusic(0);
      }

      if (nextArea == indoor) {
        playMusic(18);
      }

      if (nextArea == dungeon) {
        playMusic(19);
      }

      aSetter.setNPC();
    }

    currentArea = nextArea;
    aSetter.setMonster();
  }
}

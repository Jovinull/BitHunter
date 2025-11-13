package main;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * Lida com eventos de teclado e delega o comportamento conforme o estado atual
 * do jogo. Mantém flags de input para o loop principal (GamePanel) consumir.
 */
public class KeyHandler implements KeyListener {

  // Referência ao painel do jogo para ler/alterar estados
  private final GamePanel gp;

  // Flags de input contínuo (consumidas no update do jogo)
  // Encapsuladas conforme Sonar (S1104)
  private boolean upPressed;
  private boolean downPressed;
  private boolean leftPressed;
  private boolean rightPressed;
  private boolean enterPressed;
  private boolean shotKeyPressed;
  private boolean spacePressed;

  // DEBUG/God mode encapsulados (S1104)
  private boolean showDebugText = false;
  private boolean godModeOn = false;

  // Limites de índices dos menus de título (evita "números mágicos")
  private static final int MENU_MAIN_MAX_INDEX = 2; // [Novo jogo, Carregar, Sair]
  private static final int MENU_CLASS_MAX_INDEX = 3; // [Guerreiro, Ladrão, Feiticeiro, Voltar]

  // Limites do grid de slots na tela de personagem
  private static final int SLOT_ROW_MAX = 3;
  private static final int SLOT_COL_MAX = 4;

  public KeyHandler(GamePanel gp) {
    this.gp = gp;
  }

  /** Não utilizado (entrada de caracteres). */
  @Override
  public void keyTyped(KeyEvent e) {
    // Intencionalmente vazio
  }

  /**
   * Dispara ao pressionar uma tecla. Encaminha para o "estado" atual do jogo.
   */
  @Override
  public void keyPressed(KeyEvent e) {
    final int key = e.getKeyCode();

    if (gp.gameState == gp.titleState) {
      titleState(key);
      return;
    }
    if (gp.gameState == gp.playState) {
      playState(key);
      return;
    }
    if (gp.gameState == gp.pauseState) {
      pauseState(key);
      return;
    }
    if (gp.gameState == gp.dialogueState || gp.gameState == gp.cutsceneState) {
      dialogueState(key);
      return;
    }
    if (gp.gameState == gp.characterState) {
      characterState(key);
    }
    if (gp.gameState == gp.optionsState) {
      optionsState(key);
    }
    if (gp.gameState == gp.gameOverState) {
      gameOverState(key);
    }
    if (gp.gameState == gp.tradeState) {
      tradeState(key);
    }
    if (gp.gameState == gp.mapState) {
      mapState(key);
    }
  }

  /**
   * Estado: Tela de título (menus iniciais).
   * titleScreenState 0 = menu principal; 1 = seleção de classe/personagem.
   */
  public void titleState(int code) {
    switch (gp.ui.getTitleScreenState()) {
      case 0 -> handleTitleMainMenu(code);
      case 1 -> handleTitleClassSelect(code);
      default -> {
        // noop
      }
    }
  }

  private void handleTitleMainMenu(int code) {
    switch (code) {
      case KeyEvent.VK_W -> navigateUp(MENU_MAIN_MAX_INDEX);
      case KeyEvent.VK_S -> navigateDown(MENU_MAIN_MAX_INDEX);
      case KeyEvent.VK_ENTER -> handleTitleMainEnter();
      default -> {
        // noop
      }
    }
  }

  private void handleTitleMainEnter() {
    switch (gp.ui.getCommandNum()) {
      case 0 -> { // Novo jogo -> vai para seleção de classe
        gp.ui.setTitleScreenState(1);
        gp.ui.setCommandNum(0);
      }
      case 1 -> { // Carregar jogo
        gp.saveLoad.load();
        gp.gameState = gp.playState;
        gp.playMusic(0);
      }
      case 2 -> System.exit(0); // Sair
      default -> {
        // noop
      }
    }
  }

  private void handleTitleClassSelect(int code) {
    switch (code) {
      case KeyEvent.VK_W -> navigateUp(MENU_CLASS_MAX_INDEX);
      case KeyEvent.VK_S -> navigateDown(MENU_CLASS_MAX_INDEX);
      case KeyEvent.VK_ENTER -> {
        if (gp.ui.getCommandNum() == 3) { // Voltar
          gp.ui.setTitleScreenState(0);
          gp.ui.setCommandNum(0);
        } else {
          gp.gameState = gp.playState;
          gp.playMusic(0);
        }
      }
      case KeyEvent.VK_ESCAPE -> {
        gp.ui.setTitleScreenState(0);
        gp.ui.setCommandNum(0);
      }
      default -> {
        // noop
      }
    }
  }

  /**
   * Estado: Jogando (movimento, pausa, status, debug, etc).
   */
  public void playState(int key) {
    switch (key) {
      // Movimento (WASD)
      case KeyEvent.VK_W -> upPressed = true;
      case KeyEvent.VK_S -> downPressed = true;
      case KeyEvent.VK_A -> leftPressed = true;
      case KeyEvent.VK_D -> rightPressed = true;

      // Pausa / Status / Ações
      case KeyEvent.VK_P -> gp.gameState = gp.pauseState;
      case KeyEvent.VK_C -> gp.gameState = gp.characterState;
      case KeyEvent.VK_ENTER -> enterPressed = true;
      case KeyEvent.VK_F -> shotKeyPressed = true;
      case KeyEvent.VK_SPACE -> spacePressed = true;

      // Opções / Mapas
      case KeyEvent.VK_ESCAPE -> gp.gameState = gp.optionsState;
      case KeyEvent.VK_M -> gp.gameState = gp.mapState;
      case KeyEvent.VK_X -> gp.map.setMiniMapOn(!gp.map.isMiniMapOn());

      // Dev shortcuts
      case KeyEvent.VK_T -> toggleDebug();
      case KeyEvent.VK_R -> reloadCurrentMap();
      case KeyEvent.VK_G -> godModeOn = !godModeOn;

      default -> {
        // noop
      }
    }
  }

  private void toggleDebug() {
    showDebugText = !showDebugText;
    gp.tileM.setDrawPath(showDebugText);
  }

  private void reloadCurrentMap() {
    switch (gp.currentMap) {
      case 0 -> gp.tileM.loadMap("/maps/world01.txt", 0);
      case 1 -> gp.tileM.loadMap("/maps/interior01.txt", 1);
      default -> {
        // noop
      }
    }
  }

  /**
   * Estado: Pausado.
   */
  public void pauseState(int key) {
    if (key == KeyEvent.VK_P) {
      gp.gameState = gp.playState;
    }
  }

  /**
   * Estado: Diálogo/Cutscene aberto.
   */
  public void dialogueState(int key) {
    if (key == KeyEvent.VK_ENTER) {
      enterPressed = true;
    }
  }

  /**
   * Estado: Tela de status do personagem.
   */
  public void characterState(int code) {
    if (code == KeyEvent.VK_C) {
      gp.gameState = gp.playState;
    } else if (code == KeyEvent.VK_ENTER) {
      gp.player.selectItem();
    }
    // Navegação do inventário com W/A/S/D
    playerInventory(code);
  }

  /**
   * Estado: Opções. Reduzido com helpers e flatten das condicionais (S3776 +
   * S1066).
   */
  public void optionsState(int code) {
    if (code == KeyEvent.VK_ESCAPE) {
      gp.gameState = gp.playState;
      return;
    }
    if (code == KeyEvent.VK_ENTER) {
      enterPressed = true;
      return;
    }

    final int maxCommandNum = getOptionsMaxCommandNum();

    switch (code) {
      case KeyEvent.VK_W -> moveCommandNum(-1, maxCommandNum);
      case KeyEvent.VK_S -> moveCommandNum(+1, maxCommandNum);
      case KeyEvent.VK_A -> adjustVolume(-1);
      case KeyEvent.VK_D -> adjustVolume(+1);
      default -> {
        // noop
      }
    }
  }

  private int getOptionsMaxCommandNum() {
    return switch (gp.ui.subState) {
      case 0 -> 5;
      case 3 -> 1;
      default -> 0;
    };
  }

  private void moveCommandNum(int delta, int max) {
    int commandNum = gp.ui.getCommandNum();
    commandNum += delta;
    if (commandNum < 0) {
      commandNum = max;
    }
    if (commandNum > max) {
      commandNum = 0;
    }
    gp.ui.setCommandNum(commandNum);
    gp.playSE(9);
  }

  private void adjustVolume(int direction) {
    if (gp.ui.subState != 0)
      return;

    switch (gp.ui.getCommandNum()) {
      case 1 -> { // música
        final int next = gp.music.volumeScale + direction;
        if (next >= 0 && next <= 5) {
          gp.music.volumeScale = next;
          gp.music.checkVolume();
          gp.playSE(9);
        }
      }
      case 2 -> { // efeitos
        final int next = gp.se.volumeScale + direction;
        if (next >= 0 && next <= 5) {
          gp.se.volumeScale = next;
          gp.playSE(9);
        }
      }
      default -> {
        // noop
      }
    }
  }

  /**
   * Estado: Game Over.
   */
  public void gameOverState(int code) {
    if (code == KeyEvent.VK_W) {
      int commandNum = gp.ui.getCommandNum();
      commandNum--;
      if (commandNum < 0) {
        commandNum = 1;
      }
      gp.ui.setCommandNum(commandNum);
      gp.playSE(9);
    }

    if (code == KeyEvent.VK_S) {
      int commandNum = gp.ui.getCommandNum();
      commandNum++;
      if (commandNum > 1) {
        commandNum = 0;
      }
      gp.ui.setCommandNum(commandNum);
      gp.playSE(9);
    }

    if (code == KeyEvent.VK_ENTER) {
      int commandNum = gp.ui.getCommandNum();
      if (commandNum == 0) {
        gp.gameState = gp.playState;
        gp.resetGame(false);
        gp.playMusic(0);
      } else if (commandNum == 1) {
        gp.stopMusic();
        gp.resetGame(true);
        gp.ui.setTitleScreenState(0);
        gp.ui.setCommandNum(0);
        gp.ui.subState = 0;
        gp.gameState = gp.titleState;
      }
    }
  }

  /**
   * Estado: Comércio (trade). Reduzido com dispatch por subState (S3776).
   */
  public void tradeState(int code) {
    if (code == KeyEvent.VK_ENTER) {
      enterPressed = true;
      return;
    }

    switch (gp.ui.subState) {
      case 0 -> handleTradeMenuNavigation(code);
      case 1 -> {
        npcInventory(code);
        if (code == KeyEvent.VK_ESCAPE)
          gp.ui.subState = 0;
      }
      case 2 -> {
        playerInventory(code);
        if (code == KeyEvent.VK_ESCAPE)
          gp.ui.subState = 0;
      }
      default -> {
        // noop
      }
    }
  }

  private void handleTradeMenuNavigation(int code) {
    switch (code) {
      case KeyEvent.VK_W -> {
        int commandNum = gp.ui.getCommandNum();
        commandNum--;
        if (commandNum < 0) {
          commandNum = 2;
        }
        gp.ui.setCommandNum(commandNum);
        gp.playSE(9);
      }
      case KeyEvent.VK_S -> {
        int commandNum = gp.ui.getCommandNum();
        commandNum++;
        if (commandNum > 2) {
          commandNum = 0;
        }
        gp.ui.setCommandNum(commandNum);
        gp.playSE(9);
      }
      default -> {
        // noop
      }
    }
  }

  public void mapState(int code) {
    if (code == KeyEvent.VK_M) {
      gp.gameState = gp.playState;
    }
  }

  public void playerInventory(int code) {
    int row = gp.ui.getPlayerSlotRow();
    int col = gp.ui.getPlayerSlotCol();

    if (code == KeyEvent.VK_W && row != 0) {
      gp.ui.setPlayerSlotRow(row - 1);
      gp.playSE(9);
    } else if (code == KeyEvent.VK_A && col != 0) {
      gp.ui.setPlayerSlotCol(col - 1);
      gp.playSE(9);
    } else if (code == KeyEvent.VK_S && row != SLOT_ROW_MAX) {
      gp.ui.setPlayerSlotRow(row + 1);
      gp.playSE(9);
    } else if (code == KeyEvent.VK_D && col != SLOT_COL_MAX) {
      gp.ui.setPlayerSlotCol(col + 1);
      gp.playSE(9);
    }
  }

  public void npcInventory(int code) {
    int row = gp.ui.getNpcSlotRow();
    int col = gp.ui.getNpcSlotCol();

    if (code == KeyEvent.VK_W && row != 0) {
      gp.ui.setNpcSlotRow(row - 1);
      gp.playSE(9);
    } else if (code == KeyEvent.VK_A && col != 0) {
      gp.ui.setNpcSlotCol(col - 1);
      gp.playSE(9);
    } else if (code == KeyEvent.VK_S && row != SLOT_ROW_MAX) {
      gp.ui.setNpcSlotRow(row + 1);
      gp.playSE(9);
    } else if (code == KeyEvent.VK_D && col != SLOT_COL_MAX) {
      gp.ui.setNpcSlotCol(col + 1);
      gp.playSE(9);
    }
  }

  /**
   * Dispara ao SOLTAR uma tecla. Zera flags de movimento correspondentes.
   */
  @Override
  public void keyReleased(KeyEvent e) {
    final int key = e.getKeyCode();
    switch (key) {
      case KeyEvent.VK_W -> upPressed = false;
      case KeyEvent.VK_S -> downPressed = false;
      case KeyEvent.VK_A -> leftPressed = false;
      case KeyEvent.VK_D -> rightPressed = false;
      case KeyEvent.VK_F -> shotKeyPressed = false;
      case KeyEvent.VK_ENTER -> enterPressed = false;
      case KeyEvent.VK_SPACE -> spacePressed = false;
      default -> {
        /* noop */ }
    }
  }

  // -----------------------
  // Helpers privados
  // -----------------------

  /** Move seleção para cima no menu com wrap-around. */
  private void navigateUp(int maxIndex) {
    int commandNum = gp.ui.getCommandNum();
    commandNum--;
    if (commandNum < 0) {
      commandNum = maxIndex;
    }
    gp.ui.setCommandNum(commandNum);
  }

  /** Move seleção para baixo no menu com wrap-around. */
  private void navigateDown(int maxIndex) {
    int commandNum = gp.ui.getCommandNum();
    commandNum++;
    if (commandNum > maxIndex) {
      commandNum = 0;
    }
    gp.ui.setCommandNum(commandNum);
  }

  // -----------------------
  // Accessors (S1104)
  // -----------------------
  public boolean isUpPressed() {
    return upPressed;
  }

  public boolean isDownPressed() {
    return downPressed;
  }

  public boolean isLeftPressed() {
    return leftPressed;
  }

  public boolean isRightPressed() {
    return rightPressed;
  }

  public boolean isEnterPressed() {
    return enterPressed;
  }

  public boolean isShotKeyPressed() {
    return shotKeyPressed;
  }

  public boolean isSpacePressed() {
    return spacePressed;
  }

  public boolean isShowDebugText() {
    return showDebugText;
  }

  public boolean isGodModeOn() {
    return godModeOn;
  }

  // Setter necessário para consumo de ENTER pela UI/Player
  public void setEnterPressed(boolean value) {
    this.enterPressed = value;
  }
}

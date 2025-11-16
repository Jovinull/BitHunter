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
  private boolean upPressed;
  private boolean downPressed;
  private boolean leftPressed;
  private boolean rightPressed;
  private boolean enterPressed;
  private boolean shotKeyPressed;
  private boolean spacePressed;

  // DEBUG/God mode encapsulados
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
    final int state = gp.getGameState();

    if (state == GamePanel.TITLE_STATE) {
      titleState(key);
      return;
    }
    if (state == GamePanel.PLAY_STATE) {
      playState(key);
      return;
    }
    if (state == GamePanel.PAUSE_STATE) {
      pauseState(key);
      return;
    }
    if (state == GamePanel.DIALOGUE_STATE || state == GamePanel.CUTSCENE_STATE) {
      dialogueState(key);
      return;
    }
    if (state == GamePanel.CHARACTER_STATE) {
      characterState(key);
    }
    if (state == GamePanel.OPTIONS_STATE) {
      optionsState(key);
    }
    if (state == GamePanel.GAME_OVER_STATE) {
      gameOverState(key);
    }
    if (state == GamePanel.TRADE_STATE) {
      tradeState(key);
    }
    if (state == GamePanel.MAP_STATE) {
      mapState(key);
    }
  }

  /**
   * Estado: Tela de título (menus iniciais).
   * titleScreenState 0 = menu principal; 1 = seleção de classe/personagem.
   */
  public void titleState(int code) {
    switch (gp.getGameUi().getTitleScreenState()) {
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
    switch (gp.getGameUi().getCommandNum()) {
      case 0 -> { // Novo jogo -> vai para seleção de classe
        gp.getGameUi().setTitleScreenState(1);
        gp.getGameUi().setCommandNum(0);
      }
      case 1 -> { // Carregar jogo
        gp.saveLoad.load();
        gp.setGameState(GamePanel.PLAY_STATE);
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
        if (gp.getGameUi().getCommandNum() == 3) { // Voltar
          gp.getGameUi().setTitleScreenState(0);
          gp.getGameUi().setCommandNum(0);
        } else {
          gp.setGameState(GamePanel.PLAY_STATE);
          gp.playMusic(0);
        }
      }
      case KeyEvent.VK_ESCAPE -> {
        gp.getGameUi().setTitleScreenState(0);
        gp.getGameUi().setCommandNum(0);
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
      case KeyEvent.VK_P -> gp.setGameState(GamePanel.PAUSE_STATE);
      case KeyEvent.VK_C -> gp.setGameState(GamePanel.CHARACTER_STATE);
      case KeyEvent.VK_ENTER -> enterPressed = true;
      case KeyEvent.VK_F -> shotKeyPressed = true;
      case KeyEvent.VK_SPACE -> spacePressed = true;

      // Opções / Mapas
      case KeyEvent.VK_ESCAPE -> {
        gp.getGameUi().resetOptionsMenu();
        gp.setGameState(GamePanel.OPTIONS_STATE);
      }
      case KeyEvent.VK_M -> gp.setGameState(GamePanel.MAP_STATE);
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
    gp.getTileM().setDrawPath(showDebugText);
  }

  private void reloadCurrentMap() {
    switch (gp.getCurrentMap()) {
      case 0 -> gp.getTileM().loadMap("/maps/world01.txt", 0);
      case 1 -> gp.getTileM().loadMap("/maps/interior01.txt", 1);
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
      gp.setGameState(GamePanel.PLAY_STATE);
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
      gp.setGameState(GamePanel.PLAY_STATE);
    } else if (code == KeyEvent.VK_ENTER) {
      gp.getPlayer().selectItem();
    }
    // Navegação do inventário com W/A/S/D
    playerInventory(code);
  }

  /**
   * Estado: Opções.
   */
  public void optionsState(int code) {
    if (code == KeyEvent.VK_ESCAPE) {
      gp.setGameState(GamePanel.PLAY_STATE);
      return;
    }
    if (code == KeyEvent.VK_ENTER) {
      enterPressed = true;
      return;
    }

    // Navegação ESPECIAL para o submenu de idiomas (subState 4)
    if (gp.getGameUi().getSubState() == 4
        && (code == KeyEvent.VK_W || code == KeyEvent.VK_S)) {
      handleLanguageMenuNavigation(code);
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

  /**
   * Navegação específica do submenu de idiomas:
   *
   * - índices 0..(totalLanguages-1): idiomas
   * - índice totalLanguages: "Voltar"
   *
   * Regras:
   * - W em 0 -> último idioma (totalLanguages-1)
   * - W em "Voltar" -> último idioma
   * - W no meio -> idioma anterior normal
   *
   * - S no meio -> próximo idioma
   * - S no último idioma -> "Voltar"
   * - S em "Voltar" -> primeiro idioma (0)
   */
  private void handleLanguageMenuNavigation(int code) {
    var ui = gp.getGameUi();
    var langManager = gp.getLanguageManager();

    int totalLanguages = langManager.getAvailableLanguages().size();
    int backIndex = totalLanguages;
    int commandNum = ui.getCommandNum();

    switch (code) {
      case KeyEvent.VK_W -> {
        if (commandNum == 0 || commandNum == backIndex) {
          // Da primeira opção ou de "Voltar" -> último idioma
          commandNum = totalLanguages - 1;
        } else {
          // Dentro da lista de idiomas
          commandNum--;
        }
      }
      case KeyEvent.VK_S -> {
        if (commandNum < totalLanguages - 1) {
          // Idioma intermediário -> próximo idioma
          commandNum++;
        } else if (commandNum == totalLanguages - 1) {
          // Último idioma -> "Voltar"
          commandNum = backIndex;
        } else {
          // Estava em "Voltar" -> volta para o primeiro idioma
          commandNum = 0;
        }
      }
      default -> {
        return; // não trata outras teclas aqui
      }
    }

    ui.setCommandNum(commandNum);
    gp.playSE(9);
  }

  /**
   * Retorna o índice máximo de comando permitido na tela de opções, de acordo
   * com o subState atual da UI.
   *
   * subState 0 -> menu principal de opções (0..5)
   * subState 1 -> aviso de tela cheia (apenas "Voltar", índice 0)
   * subState 2 -> tela de controles (apenas "Voltar", índice 0)
   * subState 3 -> confirmação de encerrar jogo ("Sim"/"Não", índices 0..1)
   * subState 4 -> seleção de idioma (0..N idiomas, + "Voltar" em N)
   */
  private int getOptionsMaxCommandNum() {
    return switch (gp.getGameUi().getSubState()) {
      case 0 -> 5;
      case 1, 2 -> 0;
      case 3 -> 1;
      case 4 -> gp.getLanguageManager().getAvailableLanguages().size();
      default -> 0;
    };
  }

  private void moveCommandNum(int delta, int max) {
    int commandNum = gp.getGameUi().getCommandNum();
    commandNum += delta;
    if (commandNum < 0) {
      commandNum = max;
    }
    if (commandNum > max) {
      commandNum = 0;
    }
    gp.getGameUi().setCommandNum(commandNum);
    gp.playSE(9);
  }

  private void adjustVolume(int direction) {
    if (gp.getGameUi().getSubState() != 0) {
      return;
    }

    switch (gp.getGameUi().getCommandNum()) {
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
      int commandNum = gp.getGameUi().getCommandNum();
      commandNum--;
      if (commandNum < 0) {
        commandNum = 1;
      }
      gp.getGameUi().setCommandNum(commandNum);
      gp.playSE(9);
    }

    if (code == KeyEvent.VK_S) {
      int commandNum = gp.getGameUi().getCommandNum();
      commandNum++;
      if (commandNum > 1) {
        commandNum = 0;
      }
      gp.getGameUi().setCommandNum(commandNum);
      gp.playSE(9);
    }

    if (code == KeyEvent.VK_ENTER) {
      int commandNum = gp.getGameUi().getCommandNum();
      if (commandNum == 0) {
        gp.setGameState(GamePanel.PLAY_STATE);
        gp.resetGame(false);
        gp.playMusic(0);
      } else if (commandNum == 1) {
        gp.stopMusic();
        gp.resetGame(true);
        gp.getGameUi().setTitleScreenState(0);
        gp.getGameUi().setCommandNum(0);
        gp.getGameUi().setSubState(0);
        gp.setGameState(GamePanel.TITLE_STATE);
      }
    }
  }

  /**
   * Estado: Comércio (trade).
   */
  public void tradeState(int code) {
    if (code == KeyEvent.VK_ENTER) {
      enterPressed = true;
      return;
    }

    switch (gp.getGameUi().getSubState()) {
      case 0 -> handleTradeMenuNavigation(code);
      case 1 -> {
        npcInventory(code);
        if (code == KeyEvent.VK_ESCAPE) {
          gp.getGameUi().setSubState(0);
        }
      }
      case 2 -> {
        playerInventory(code);
        if (code == KeyEvent.VK_ESCAPE) {
          gp.getGameUi().setSubState(0);
        }
      }
      default -> {
        // noop
      }
    }
  }

  private void handleTradeMenuNavigation(int code) {
    switch (code) {
      case KeyEvent.VK_W -> {
        int commandNum = gp.getGameUi().getCommandNum();
        commandNum--;
        if (commandNum < 0) {
          commandNum = 2;
        }
        gp.getGameUi().setCommandNum(commandNum);
        gp.playSE(9);
      }
      case KeyEvent.VK_S -> {
        int commandNum = gp.getGameUi().getCommandNum();
        commandNum++;
        if (commandNum > 2) {
          commandNum = 0;
        }
        gp.getGameUi().setCommandNum(commandNum);
        gp.playSE(9);
      }
      default -> {
        // noop
      }
    }
  }

  public void mapState(int code) {
    if (code == KeyEvent.VK_M) {
      gp.setGameState(GamePanel.PLAY_STATE);
    }
  }

  public void playerInventory(int code) {
    int row = gp.getGameUi().getPlayerSlotRow();
    int col = gp.getGameUi().getPlayerSlotCol();

    if (code == KeyEvent.VK_W && row != 0) {
      gp.getGameUi().setPlayerSlotRow(row - 1);
      gp.playSE(9);
    } else if (code == KeyEvent.VK_A && col != 0) {
      gp.getGameUi().setPlayerSlotCol(col - 1);
      gp.playSE(9);
    } else if (code == KeyEvent.VK_S && row != SLOT_ROW_MAX) {
      gp.getGameUi().setPlayerSlotRow(row + 1);
      gp.playSE(9);
    } else if (code == KeyEvent.VK_D && col != SLOT_COL_MAX) {
      gp.getGameUi().setPlayerSlotCol(col + 1);
      gp.playSE(9);
    }
  }

  public void npcInventory(int code) {
    int row = gp.getGameUi().getNpcSlotRow();
    int col = gp.getGameUi().getNpcSlotCol();

    if (code == KeyEvent.VK_W && row != 0) {
      gp.getGameUi().setNpcSlotRow(row - 1);
      gp.playSE(9);
    } else if (code == KeyEvent.VK_A && col != 0) {
      gp.getGameUi().setNpcSlotCol(col - 1);
      gp.playSE(9);
    } else if (code == KeyEvent.VK_S && row != SLOT_ROW_MAX) {
      gp.getGameUi().setNpcSlotRow(row + 1);
      gp.playSE(9);
    } else if (code == KeyEvent.VK_D && col != SLOT_COL_MAX) {
      gp.getGameUi().setNpcSlotCol(col + 1);
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
    int commandNum = gp.getGameUi().getCommandNum();
    commandNum--;
    if (commandNum < 0) {
      commandNum = maxIndex;
    }
    gp.getGameUi().setCommandNum(commandNum);
  }

  /** Move seleção para baixo no menu com wrap-around. */
  private void navigateDown(int maxIndex) {
    int commandNum = gp.getGameUi().getCommandNum();
    commandNum++;
    if (commandNum > maxIndex) {
      commandNum = 0;
    }
    gp.getGameUi().setCommandNum(commandNum);
  }

  // -----------------------
  // Accessors
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

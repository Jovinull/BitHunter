package main;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import ui.UI;

/**
 * Lida com eventos de teclado e delega o comportamento conforme o estado atual
 * do jogo. Mantém flags de input para o loop principal (GamePanel) consumir.
 */
public class KeyHandler implements KeyListener {

  // -----------------------
  // Constants
  // -----------------------

  /** ID do SFX de navegação (UI). */
  private static final int NAVIGATE_SFX_ID = 9;

  /** Limites de índices dos menus de título. */
  private static final int MENU_MAIN_MAX_INDEX = 2; // [Novo jogo, Carregar, Sair]
  private static final int MENU_CLASS_MAX_INDEX = 3; // [Guerreiro, Ladrão, Feiticeiro, Voltar]

  /** Limites de índices de menus com navegação W/S (wrap). */
  private static final int TRADE_MENU_MAX_INDEX = 2;
  private static final int PET_YARD_MENU_MAX_INDEX = 2;
  private static final int GAME_OVER_MAX_INDEX = 1;

  /** Limites do grid de slots na tela de personagem. */
  private static final int SLOT_ROW_MAX = 3;
  private static final int SLOT_COL_MAX = 4;

  /** Limites de escala de volume (Sound). */
  private static final int VOLUME_SCALE_MIN = 0;
  private static final int VOLUME_SCALE_MAX = 5;

  /**
   * Catálogo mínimo de mapas para o atalho de dev (VK_R).
   *
   * Importante:
   * - O índice deve bater com {@code gp.getCurrentMap()}.
   * - Mantém compatibilidade com os mapas já carregados pelo {@code TileManager}.
   * - Se um índice não tiver path aqui, o reload faz no-op (seguro).
   */
  private static final String[] DEV_RELOAD_MAP_PATHS = {
      "/maps/world01.txt", // 0
      "/maps/indoor01.txt", // 1
      "/maps/dungeon01.txt", // 2
      "/maps/dungeon02.txt", // 3
  };

  // -----------------------
  // Fields
  // -----------------------

  /** Referência ao painel do jogo para ler/alterar estados. */
  private final GamePanel gp;

  /** Flags de input contínuo (consumidas no update do jogo). */
  private boolean upPressed;
  private boolean downPressed;
  private boolean leftPressed;
  private boolean rightPressed;
  private boolean enterPressed;
  private boolean shotKeyPressed;
  private boolean spacePressed;

  /** Flags de debug/dev. */
  private boolean showDebugText;
  private boolean godModeOn;

  public KeyHandler(GamePanel gp) {
    this.gp = gp;
  }

  // -----------------------
  // KeyListener
  // -----------------------

  /** Não utilizado (entrada de caracteres). */
  @Override
  public void keyTyped(KeyEvent e) {
    // Intencionalmente vazio
  }

  /** Dispara ao pressionar uma tecla. Encaminha para o estado atual do jogo. */
  @Override
  public void keyPressed(KeyEvent e) {
    final int code = e.getKeyCode();
    final int state = gp.getGameState();

    switch (state) {
      case GamePanel.TITLE_STATE -> {
        titleState(code);
        return;
      }
      case GamePanel.PLAY_STATE -> {
        playState(code);
        return;
      }
      case GamePanel.PAUSE_STATE -> {
        pauseState(code);
        return;
      }
      case GamePanel.DIALOGUE_STATE, GamePanel.CUTSCENE_STATE -> {
        dialogueState(code);
        return;
      }
      case GamePanel.CHARACTER_STATE -> {
        characterState(code);
        return;
      }
      case GamePanel.OPTIONS_STATE -> {
        optionsState(code);
        return;
      }
      case GamePanel.GAME_OVER_STATE -> {
        gameOverState(code);
        return;
      }
      case GamePanel.TRADE_STATE -> {
        tradeState(code);
        return;
      }
      case GamePanel.PET_YARD_STATE -> {
        petYardState(code);
        return;
      }
      case GamePanel.MAP_STATE -> {
        mapState(code);
        return;
      }
      case GamePanel.BESTIARY_STATE -> {
        bestiaryState(code);
        return;
      }
      default -> {
        // noop
      }
    }
  }

  /** Dispara ao SOLTAR uma tecla. Zera flags de movimento correspondentes. */
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
        // noop
      }
    }
  }

  // -----------------------
  // State handlers
  // -----------------------

  /**
   * Estado: Tela de título (menus iniciais).
   * titleScreenState 0 = menu principal; 1 = seleção de classe/personagem.
   */
  public void titleState(int code) {
    final UI ui = gp.getGameUi();
    switch (ui.getTitleScreenState()) {
      case 0 -> handleTitleMainMenu(code, ui);
      case 1 -> handleTitleClassSelect(code, ui);
      default -> {
        // noop
      }
    }
  }

  private void handleTitleMainMenu(int code, UI ui) {
    switch (code) {
      case KeyEvent.VK_W -> navigateUp(ui, MENU_MAIN_MAX_INDEX);
      case KeyEvent.VK_S -> navigateDown(ui, MENU_MAIN_MAX_INDEX);
      case KeyEvent.VK_ENTER -> handleTitleMainEnter(ui);
      default -> {
        // noop
      }
    }
  }

  private void handleTitleMainEnter(UI ui) {
    switch (ui.getCommandNum()) {
      case 0 -> { // Novo jogo -> vai para seleção de classe
        ui.setTitleScreenState(1);
        ui.setCommandNum(0);
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

  private void handleTitleClassSelect(int code, UI ui) {
    switch (code) {
      case KeyEvent.VK_W -> navigateUp(ui, MENU_CLASS_MAX_INDEX);
      case KeyEvent.VK_S -> navigateDown(ui, MENU_CLASS_MAX_INDEX);
      case KeyEvent.VK_ENTER -> {
        if (ui.getCommandNum() == MENU_CLASS_MAX_INDEX) { // Voltar
          ui.setTitleScreenState(0);
          ui.setCommandNum(0);
        } else {
          gp.setGameState(GamePanel.PLAY_STATE);
          gp.playMusic(0);
        }
      }
      case KeyEvent.VK_ESCAPE -> {
        ui.setTitleScreenState(0);
        ui.setCommandNum(0);
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

      // Bestiário
      case KeyEvent.VK_B -> {
        gp.getGameUi().getBestiaryUi().resetState();
        gp.setGameState(GamePanel.BESTIARY_STATE);
      }

      // Dev shortcuts
      case KeyEvent.VK_T -> toggleDebug();
      case KeyEvent.VK_R -> reloadCurrentMap();
      case KeyEvent.VK_G -> godModeOn = !godModeOn;

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
    final UI ui = gp.getGameUi();
    final int subState = ui.getSubState();

    if (code == KeyEvent.VK_ESCAPE) {
      gp.setGameState(GamePanel.PLAY_STATE);
      return;
    }
    if (code == KeyEvent.VK_ENTER) {
      enterPressed = true;
      return;
    }

    // Navegação ESPECIAL para o submenu de idiomas (subState 4)
    if (subState == 4 && (code == KeyEvent.VK_W || code == KeyEvent.VK_S)) {
      handleLanguageMenuNavigation(code, ui);
      return;
    }

    // Navegação ESPECIAL para a tela de controles (subState 2): troca de página com
    // A/D
    if (subState == 2 && (code == KeyEvent.VK_A || code == KeyEvent.VK_D)) {
      handleControlsPageNavigation(code, ui);
      return;
    }

    final int maxCommandNum = getOptionsMaxCommandNum(subState);

    switch (code) {
      case KeyEvent.VK_W -> moveCommandNum(ui, -1, maxCommandNum);
      case KeyEvent.VK_S -> moveCommandNum(ui, +1, maxCommandNum);
      case KeyEvent.VK_A -> adjustVolume(ui, subState, -1);
      case KeyEvent.VK_D -> adjustVolume(ui, subState, +1);
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
  private void handleLanguageMenuNavigation(int code, UI ui) {
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
    playNavigateSfx();
  }

  /**
   * Navegação de páginas na tela de controles (subState 2):
   *
   * - A -> página anterior
   * - D -> próxima página
   * (com wrap-around)
   */
  private void handleControlsPageNavigation(int code, UI ui) {
    int pageCount = ui.getControlsPageCount();
    if (pageCount <= 1) {
      return;
    }

    int page = ui.getControlsPage();

    if (code == KeyEvent.VK_A) {
      page = wrapDecrement(page, pageCount - 1);
    } else if (code == KeyEvent.VK_D) {
      page = wrapIncrement(page, pageCount - 1);
    }

    ui.setControlsPage(page);
    playNavigateSfx();
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
  private int getOptionsMaxCommandNum(int subState) {
    return switch (subState) {
      case 0 -> 5;
      case 1, 2 -> 0;
      case 3 -> 1;
      case 4 -> gp.getLanguageManager().getAvailableLanguages().size();
      default -> 0;
    };
  }

  private void moveCommandNum(UI ui, int delta, int max) {
    int commandNum = ui.getCommandNum();
    commandNum += delta;
    if (commandNum < 0) {
      commandNum = max;
    }
    if (commandNum > max) {
      commandNum = 0;
    }
    ui.setCommandNum(commandNum);
    playNavigateSfx();
  }

  private void adjustVolume(UI ui, int subState, int direction) {
    // só ajusta volume no topo das opções
    if (subState != 0) {
      return;
    }

    int commandNum = ui.getCommandNum();

    switch (commandNum) {
      case 1 -> { // música
        Sound music = gp.getMusic();
        int current = music.getVolumeScale();
        int next = current + direction;

        if (next >= VOLUME_SCALE_MIN && next <= VOLUME_SCALE_MAX) {
          music.setVolumeScale(next); // checkVolume() é chamado dentro do setter
          playNavigateSfx();
        }
      }
      case 2 -> { // efeitos
        Sound se = gp.getSe();
        int current = se.getVolumeScale();
        int next = current + direction;

        if (next >= VOLUME_SCALE_MIN && next <= VOLUME_SCALE_MAX) {
          se.setVolumeScale(next);
          playNavigateSfx();
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
    final UI ui = gp.getGameUi();

    switch (code) {
      case KeyEvent.VK_W -> setCommandNumAndNavigateSfx(ui, wrapDecrement(ui.getCommandNum(), GAME_OVER_MAX_INDEX));
      case KeyEvent.VK_S -> setCommandNumAndNavigateSfx(ui, wrapIncrement(ui.getCommandNum(), GAME_OVER_MAX_INDEX));
      case KeyEvent.VK_ENTER -> {
        int commandNum = ui.getCommandNum();
        if (commandNum == 0) {
          gp.logRespawnDebug("respawn.start");
          gp.setGameState(GamePanel.PLAY_STATE);
          gp.resetGame(false);
          gp.playMusic(0);
          gp.logRespawnDebug("respawn.end");
        } else if (commandNum == 1) {
          gp.stopMusic();
          gp.resetGame(true);
          ui.setTitleScreenState(0);
          ui.setCommandNum(0);
          ui.setSubState(0);
          gp.setGameState(GamePanel.TITLE_STATE);
        }
      }
      default -> {
        // noop
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

    final UI ui = gp.getGameUi();
    switch (ui.getSubState()) {
      case 0 -> handleTradeMenuNavigation(code, ui);
      case 1 -> {
        npcInventory(code);
        if (code == KeyEvent.VK_ESCAPE) {
          ui.setSubState(0);
        }
      }
      case 2 -> {
        playerInventory(code);
        if (code == KeyEvent.VK_ESCAPE) {
          ui.setSubState(0);
        }
      }
      default -> {
        // noop
      }
    }
  }

  private void handleTradeMenuNavigation(int code, UI ui) {
    switch (code) {
      case KeyEvent.VK_W -> setCommandNumAndNavigateSfx(ui, wrapDecrement(ui.getCommandNum(), TRADE_MENU_MAX_INDEX));
      case KeyEvent.VK_S -> setCommandNumAndNavigateSfx(ui, wrapIncrement(ui.getCommandNum(), TRADE_MENU_MAX_INDEX));
      default -> {
        // noop
      }
    }
  }

  /** Estado: Pet Yard (gerenciador de pets). */
  public void petYardState(int code) {
    if (code == KeyEvent.VK_ENTER) {
      enterPressed = true;
      return;
    }

    final UI ui = gp.getGameUi();

    switch (ui.getSubState()) {
      case 0 -> handlePetYardMenuNavigation(code, ui);
      case 1 -> handlePetListNavigation(code, ui);
      case 2 -> {
        // Mantém a ordem: navega inventário, depois trata ESC.
        playerInventory(code);
        if (code == KeyEvent.VK_ESCAPE) {
          ui.setSubState(0);
        }
      }
      default -> {
        // noop
      }
    }
  }

  private void handlePetYardMenuNavigation(int code, UI ui) {
    switch (code) {
      case KeyEvent.VK_W -> setCommandNumAndNavigateSfx(ui, wrapDecrement(ui.getCommandNum(), PET_YARD_MENU_MAX_INDEX));
      case KeyEvent.VK_S -> setCommandNumAndNavigateSfx(ui, wrapIncrement(ui.getCommandNum(), PET_YARD_MENU_MAX_INDEX));
      case KeyEvent.VK_ESCAPE -> gp.setGameState(GamePanel.PLAY_STATE);
      default -> {
        // noop
      }
    }
  }

  private void handlePetListNavigation(int code, UI ui) {
    var player = gp.getPlayer();
    int petCount = player.getOwnedPets().size();

    if (petCount <= 0) {
      if (code == KeyEvent.VK_ESCAPE) {
        ui.setSubState(0);
      }
      return;
    }

    int maxIndex = petCount - 1;
    switch (code) {
      case KeyEvent.VK_W -> setCommandNumAndNavigateSfx(ui, wrapDecrement(ui.getCommandNum(), maxIndex));
      case KeyEvent.VK_S -> setCommandNumAndNavigateSfx(ui, wrapIncrement(ui.getCommandNum(), maxIndex));
      case KeyEvent.VK_ESCAPE -> ui.setSubState(0);
      default -> {
        // noop
      }
    }
  }

  /**
   * Estado: Bestiário (navegação por classes, criaturas e detalhes).
   */
  public void bestiaryState(int code) {
    final UI ui = gp.getGameUi();
    var bestiaryUi = ui.getBestiaryUi();
    int subState = ui.getSubState();

    switch (subState) {
      case 0 -> { // Lista de classes
        int maxIdx = bestiaryUi.getClassCount() - 1;
        if (maxIdx < 0) maxIdx = 0;
        switch (code) {
          case KeyEvent.VK_W -> {
            moveCommandNum(ui, -1, maxIdx);
          }
          case KeyEvent.VK_S -> {
            moveCommandNum(ui, +1, maxIdx);
          }
          case KeyEvent.VK_ENTER -> {
            bestiaryUi.setSelectedClassIndex(ui.getCommandNum());
            bestiaryUi.enterSelectedClass();
          }
          case KeyEvent.VK_ESCAPE, KeyEvent.VK_B -> {
            gp.setGameState(GamePanel.PLAY_STATE);
          }
          default -> { /* noop */ }
        }
      }
      case 1 -> { // Lista de criaturas
        switch (code) {
          case KeyEvent.VK_W -> {
            bestiaryUi.navigateUp();
            playNavigateSfx();
          }
          case KeyEvent.VK_S -> {
            bestiaryUi.navigateDown();
            playNavigateSfx();
          }
          case KeyEvent.VK_ENTER -> bestiaryUi.enterSelectedCreature();
          case KeyEvent.VK_ESCAPE -> bestiaryUi.goBack();
          default -> { /* noop */ }
        }
      }
      case 2 -> { // Detalhes da criatura
        switch (code) {
          case KeyEvent.VK_W -> bestiaryUi.navigateUp();
          case KeyEvent.VK_S -> bestiaryUi.navigateDown();
          case KeyEvent.VK_ESCAPE -> bestiaryUi.goBack();
          default -> { /* noop */ }
        }
      }
      default -> {
        if (code == KeyEvent.VK_ESCAPE) {
          gp.setGameState(GamePanel.PLAY_STATE);
        }
      }
    }
  }

  public void mapState(int code) {
    if (code == KeyEvent.VK_M) {
      gp.setGameState(GamePanel.PLAY_STATE);
    }
  }

  // -----------------------
  // Inventory navigation
  // -----------------------

  public void playerInventory(int code) {
    final UI ui = gp.getGameUi();
    int row = ui.getPlayerSlotRow();
    int col = ui.getPlayerSlotCol();

    if (code == KeyEvent.VK_W && row != 0) {
      ui.setPlayerSlotRow(row - 1);
      playNavigateSfx();
    } else if (code == KeyEvent.VK_A && col != 0) {
      ui.setPlayerSlotCol(col - 1);
      playNavigateSfx();
    } else if (code == KeyEvent.VK_S && row != SLOT_ROW_MAX) {
      ui.setPlayerSlotRow(row + 1);
      playNavigateSfx();
    } else if (code == KeyEvent.VK_D && col != SLOT_COL_MAX) {
      ui.setPlayerSlotCol(col + 1);
      playNavigateSfx();
    }
  }

  public void npcInventory(int code) {
    final UI ui = gp.getGameUi();
    int row = ui.getNpcSlotRow();
    int col = ui.getNpcSlotCol();

    if (code == KeyEvent.VK_W && row != 0) {
      ui.setNpcSlotRow(row - 1);
      playNavigateSfx();
    } else if (code == KeyEvent.VK_A && col != 0) {
      ui.setNpcSlotCol(col - 1);
      playNavigateSfx();
    } else if (code == KeyEvent.VK_S && row != SLOT_ROW_MAX) {
      ui.setNpcSlotRow(row + 1);
      playNavigateSfx();
    } else if (code == KeyEvent.VK_D && col != SLOT_COL_MAX) {
      ui.setNpcSlotCol(col + 1);
      playNavigateSfx();
    }
  }

  // -----------------------
  // Private helpers (no behavior change)
  // -----------------------

  private void playNavigateSfx() {
    gp.playSE(NAVIGATE_SFX_ID);
  }

  private void setCommandNumAndNavigateSfx(UI ui, int commandNum) {
    ui.setCommandNum(commandNum);
    playNavigateSfx();
  }

  private int wrapDecrement(int value, int max) {
    value--;
    if (value < 0) {
      value = max;
    }
    return value;
  }

  private int wrapIncrement(int value, int max) {
    value++;
    if (value > max) {
      value = 0;
    }
    return value;
  }

  /** Move seleção para cima no menu de título (sem SFX). */
  private void navigateUp(UI ui, int maxIndex) {
    ui.setCommandNum(wrapDecrement(ui.getCommandNum(), maxIndex));
  }

  /** Move seleção para baixo no menu de título (sem SFX). */
  private void navigateDown(UI ui, int maxIndex) {
    ui.setCommandNum(wrapIncrement(ui.getCommandNum(), maxIndex));
  }

  private void toggleDebug() {
    showDebugText = !showDebugText;
    gp.getTileM().setDrawPath(showDebugText);
  }

  private void reloadCurrentMap() {
    int mapIndex = gp.getCurrentMap();
    if (mapIndex < 0 || mapIndex >= DEV_RELOAD_MAP_PATHS.length) {
      return; // no-op seguro para mapas não catalogados
    }

    String path = DEV_RELOAD_MAP_PATHS[mapIndex];
    if (path == null || path.isBlank()) {
      return; // no-op seguro
    }

    gp.getTileM().loadMap(path, mapIndex);
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

  public void setEnterPressed(boolean value) {
    this.enterPressed = value;
  }
}

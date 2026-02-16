package main;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import ui.UI;

/**
 * Lida com eventos de teclado e delega o comportamento conforme o estado atual
 * do jogo. Mantém flags de input para o loop principal (GamePanel) consumir.
 * <p>
 * Todas as verificações de tecla usam {@link InputBindings} para traduzir
 * keyCode → {@link GameAction}, permitindo remapeamento via configuração
 * e pela tela de controles do jogo.
 * </p>
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

  /** Limites de índices de menus com navegação (wrap). */
  private static final int TRADE_MENU_MAX_INDEX = 2;
  private static final int PET_YARD_MENU_MAX_INDEX = 2;
  private static final int GAME_OVER_MAX_INDEX = 1;

  /** Limites do grid de slots na tela de personagem. */
  private static final int SLOT_ROW_MAX = 3;
  private static final int SLOT_COL_MAX = 4;

  /** Controles exibidos por página na tela de remapeamento. */
  private static final int CONTROLS_PER_PAGE = 6;

  /** Limites de escala de volume (Sound). */
  private static final int VOLUME_SCALE_MIN = 0;
  private static final int VOLUME_SCALE_MAX = 5;

  /**
   * Catálogo mínimo de mapas para o atalho de dev (DEBUG_RELOAD_MAP).
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

  /**
   * Processa caracteres digitados. Utilizado pelo {@link CheatCodeManager}
   * para capturar o caractere de ativação (aspas) e as letras do código.
   * Só opera em {@link GamePanel#PLAY_STATE}.
   */
  @Override
  public void keyTyped(KeyEvent e) {
    if (gp.getGameState() == GamePanel.PLAY_STATE) {
      gp.getCheatCodeManager().onCharTyped(e.getKeyChar());
    }
  }

  /** Dispara ao pressionar uma tecla. Encaminha para o estado atual do jogo. */
  @Override
  public void keyPressed(KeyEvent e) {
    final int code = e.getKeyCode();
    final int state = gp.getGameState();

    // Código secreto: durante captura, consome todas as teclas
    // para impedir ações do jogo (WASD, T, R, etc.)
    if (state == GamePanel.PLAY_STATE && gp.getCheatCodeManager().onKeyPressed(code)) {
      return;
    }

    switch (state) {
      case GamePanel.TITLE_STATE -> titleState(code);
      case GamePanel.PLAY_STATE -> playState(code);
      case GamePanel.PAUSE_STATE -> pauseState(code);
      case GamePanel.DIALOGUE_STATE, GamePanel.CUTSCENE_STATE -> dialogueState(code);
      case GamePanel.CHARACTER_STATE -> characterState(code);
      case GamePanel.OPTIONS_STATE -> optionsState(code);
      case GamePanel.GAME_OVER_STATE -> gameOverState(code);
      case GamePanel.TRADE_STATE -> tradeState(code);
      case GamePanel.PET_YARD_STATE -> petYardState(code);
      case GamePanel.MAP_STATE -> mapState(code);
      case GamePanel.BESTIARY_STATE -> bestiaryState(code);
      default -> {
        // noop
      }
    }
  }

  /** Dispara ao SOLTAR uma tecla. Zera flags de input correspondentes. */
  @Override
  public void keyReleased(KeyEvent e) {
    final int key = e.getKeyCode();
    if (is(key, GameAction.MOVE_UP)) upPressed = false;
    if (is(key, GameAction.MOVE_DOWN)) downPressed = false;
    if (is(key, GameAction.MOVE_LEFT)) leftPressed = false;
    if (is(key, GameAction.MOVE_RIGHT)) rightPressed = false;
    if (is(key, GameAction.SHOOT)) shotKeyPressed = false;
    if (is(key, GameAction.CONFIRM)) enterPressed = false;
    if (is(key, GameAction.GUARD)) spacePressed = false;
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
    if (is(code, GameAction.MOVE_UP)) {
      navigateUp(ui, MENU_MAIN_MAX_INDEX);
    } else if (is(code, GameAction.MOVE_DOWN)) {
      navigateDown(ui, MENU_MAIN_MAX_INDEX);
    } else if (is(code, GameAction.CONFIRM)) {
      handleTitleMainEnter(ui);
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
    if (is(code, GameAction.MOVE_UP)) {
      navigateUp(ui, MENU_CLASS_MAX_INDEX);
    } else if (is(code, GameAction.MOVE_DOWN)) {
      navigateDown(ui, MENU_CLASS_MAX_INDEX);
    } else if (is(code, GameAction.CONFIRM)) {
      if (ui.getCommandNum() == MENU_CLASS_MAX_INDEX) { // Voltar
        ui.setTitleScreenState(0);
        ui.setCommandNum(0);
      } else {
        gp.setGameState(GamePanel.PLAY_STATE);
        gp.playMusic(0);
      }
    } else if (is(code, GameAction.OPTIONS)) {
      ui.setTitleScreenState(0);
      ui.setCommandNum(0);
    }
  }

  /**
   * Estado: Jogando (movimento, pausa, status, debug, etc).
   */
  public void playState(int key) {
    // Movimento (flags independentes — permite diagonal)
    if (is(key, GameAction.MOVE_UP)) upPressed = true;
    if (is(key, GameAction.MOVE_DOWN)) downPressed = true;
    if (is(key, GameAction.MOVE_LEFT)) leftPressed = true;
    if (is(key, GameAction.MOVE_RIGHT)) rightPressed = true;

    // Combate (flags independentes)
    if (is(key, GameAction.CONFIRM)) enterPressed = true;
    if (is(key, GameAction.SHOOT)) shotKeyPressed = true;
    if (is(key, GameAction.GUARD)) spacePressed = true;

    // Interface (mutuamente exclusivas)
    if (is(key, GameAction.PAUSE)) {
      gp.getGameUi().resetPauseAnimation();
      gp.setGameState(GamePanel.PAUSE_STATE);
    } else if (is(key, GameAction.CHARACTER)) {
      gp.setGameState(GamePanel.CHARACTER_STATE);
    } else if (is(key, GameAction.OPTIONS)) {
      gp.getGameUi().resetOptionsMenu();
      gp.setGameState(GamePanel.OPTIONS_STATE);
    } else if (is(key, GameAction.MAP)) {
      gp.setGameState(GamePanel.MAP_STATE);
    } else if (is(key, GameAction.MINIMAP)) {
      gp.map.setMiniMapOn(!gp.map.isMiniMapOn());
    } else if (is(key, GameAction.BESTIARY)) {
      gp.getGameUi().getBestiaryUi().resetState();
      gp.setGameState(GamePanel.BESTIARY_STATE);
    }

    // Dev shortcuts (respeitam debug.enabled via InputBindings)
    if (is(key, GameAction.DEBUG_TOGGLE)) toggleDebug();
    if (is(key, GameAction.DEBUG_RELOAD_MAP)) reloadCurrentMap();
  }

  /**
   * Estado: Pausado.
   */
  public void pauseState(int key) {
    if (is(key, GameAction.PAUSE)) {
      gp.setGameState(GamePanel.PLAY_STATE);
    }
  }

  /**
   * Estado: Diálogo/Cutscene aberto.
   */
  public void dialogueState(int key) {
    if (is(key, GameAction.CONFIRM)) {
      enterPressed = true;
    }
  }

  /**
   * Estado: Tela de status do personagem.
   */
  public void characterState(int code) {
    if (is(code, GameAction.CHARACTER)) {
      gp.setGameState(GamePanel.PLAY_STATE);
    } else if (is(code, GameAction.CONFIRM)) {
      gp.getPlayer().selectItem();
    }
    // Navegação do inventário
    playerInventory(code);
  }

  /**
   * Estado: Opções.
   * <p>
   * Trata de forma dedicada a tela de controles (subState 2), incluindo
   * o modo de remapeamento de teclas (rebind).
   * </p>
   */
  public void optionsState(int code) {
    final UI ui = gp.getGameUi();
    final int subState = ui.getSubState();

    // Modo de remapeamento: captura tecla crua (antes de qualquer outro check)
    if (subState == 2 && ui.isWaitingForRebind()) {
      handleRebindCapture(code, ui);
      return;
    }

    // ESC sai das opções (exceto durante rebind)
    if (is(code, GameAction.OPTIONS)) {
      gp.setGameState(GamePanel.PLAY_STATE);
      return;
    }

    // Tela de controles: navegação dedicada com seleção e rebind
    if (subState == 2) {
      handleControlsNavigation(code, ui);
      return;
    }

    // ENTER genérico (usado pelos outros subStates)
    if (is(code, GameAction.CONFIRM)) {
      enterPressed = true;
      return;
    }

    // Navegação ESPECIAL para o submenu de idiomas (subState 4)
    if (subState == 4
        && (is(code, GameAction.MOVE_UP) || is(code, GameAction.MOVE_DOWN))) {
      handleLanguageMenuNavigation(code, ui);
      return;
    }

    final int maxCommandNum = getOptionsMaxCommandNum(subState);

    if (is(code, GameAction.MOVE_UP)) {
      moveCommandNum(ui, -1, maxCommandNum);
    } else if (is(code, GameAction.MOVE_DOWN)) {
      moveCommandNum(ui, +1, maxCommandNum);
    } else if (is(code, GameAction.MOVE_LEFT)) {
      adjustVolume(ui, subState, -1);
    } else if (is(code, GameAction.MOVE_RIGHT)) {
      adjustVolume(ui, subState, +1);
    }
  }

  // -----------------------
  // Controls screen (rebind)
  // -----------------------

  /**
   * Captura uma tecla crua durante o modo de remapeamento.
   * ESC cancela; qualquer outra tecla atribui o novo binding e salva.
   */
  private void handleRebindCapture(int code, UI ui) {
    // ESC sempre cancela o rebind (usando keyCode cru, não o binding)
    if (code == KeyEvent.VK_ESCAPE) {
      ui.setWaitingForRebind(false);
      return;
    }

    var remappable = GameAction.getRemappable();
    int idx = ui.getRebindControlIndex();
    if (idx < 0 || idx >= remappable.size()) {
      ui.setWaitingForRebind(false);
      return;
    }

    GameAction action = remappable.get(idx);
    gp.getInputBindings().rebind(action, code);
    gp.getInputBindings().saveToFile();
    ui.setWaitingForRebind(false);
    playNavigateSfx();
  }

  /**
   * Navegação e seleção na tela de controles (subState 2).
   * W/S navegam pelas ações; ENTER inicia rebind ou ativa "Voltar".
   */
  private void handleControlsNavigation(int code, UI ui) {
    var remappable = GameAction.getRemappable();
    int totalEntries = remappable.size();
    int maxCmd = totalEntries; // "Voltar" fica neste índice

    if (is(code, GameAction.CONFIRM)) {
      if (ui.getCommandNum() < totalEntries) {
        // Inicia o modo de remapeamento para a ação selecionada
        ui.setRebindControlIndex(ui.getCommandNum());
        ui.setWaitingForRebind(true);
      } else {
        // "Voltar" → retorna ao menu de opções principal
        ui.setControlsPage(0);
        ui.setWaitingForRebind(false);
        ui.setSubState(0);
        ui.setCommandNum(3); // índice do item "Controles" no menu de opções
      }
      return;
    }

    if (is(code, GameAction.MOVE_UP)) {
      moveCommandNum(ui, -1, maxCmd);
      autoScrollControlsPage(ui, totalEntries);
    } else if (is(code, GameAction.MOVE_DOWN)) {
      moveCommandNum(ui, +1, maxCmd);
      autoScrollControlsPage(ui, totalEntries);
    }
  }

  /**
   * Ajusta a página da tela de controles para manter a seleção visível.
   */
  private void autoScrollControlsPage(UI ui, int totalEntries) {
    int cmd = ui.getCommandNum();
    if (cmd < totalEntries) {
      ui.setControlsPage(cmd / CONTROLS_PER_PAGE);
    }
    // Quando em "Voltar", mantém a página atual
  }

  // -----------------------
  // Language & other options
  // -----------------------

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

    if (is(code, GameAction.MOVE_UP)) {
      if (commandNum == 0 || commandNum == backIndex) {
        // Da primeira opção ou de "Voltar" -> último idioma
        commandNum = totalLanguages - 1;
      } else {
        // Dentro da lista de idiomas
        commandNum--;
      }
    } else if (is(code, GameAction.MOVE_DOWN)) {
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
    } else {
      return; // não trata outras teclas aqui
    }

    ui.setCommandNum(commandNum);
    playNavigateSfx();
  }

  /**
   * Retorna o índice máximo de comando permitido na tela de opções, de acordo
   * com o subState atual da UI.
   *
   * subState 0 -> menu principal de opções (0..5)
   * subState 1 -> aviso de tela cheia (apenas "Voltar", índice 0)
   * subState 2 -> tela de controles (gerenciado por handleControlsNavigation)
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

    if (is(code, GameAction.MOVE_UP)) {
      setCommandNumAndNavigateSfx(ui,
          wrapDecrement(ui.getCommandNum(), GAME_OVER_MAX_INDEX));
    } else if (is(code, GameAction.MOVE_DOWN)) {
      setCommandNumAndNavigateSfx(ui,
          wrapIncrement(ui.getCommandNum(), GAME_OVER_MAX_INDEX));
    } else if (is(code, GameAction.CONFIRM)) {
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
  }

  /**
   * Estado: Comércio (trade).
   */
  public void tradeState(int code) {
    if (is(code, GameAction.CONFIRM)) {
      enterPressed = true;
      return;
    }

    final UI ui = gp.getGameUi();
    switch (ui.getSubState()) {
      case 0 -> handleTradeMenuNavigation(code, ui);
      case 1 -> {
        npcInventory(code);
        if (is(code, GameAction.OPTIONS)) {
          ui.setSubState(0);
        }
      }
      case 2 -> {
        playerInventory(code);
        if (is(code, GameAction.OPTIONS)) {
          ui.setSubState(0);
        }
      }
      default -> {
        // noop
      }
    }
  }

  private void handleTradeMenuNavigation(int code, UI ui) {
    if (is(code, GameAction.MOVE_UP)) {
      setCommandNumAndNavigateSfx(ui,
          wrapDecrement(ui.getCommandNum(), TRADE_MENU_MAX_INDEX));
    } else if (is(code, GameAction.MOVE_DOWN)) {
      setCommandNumAndNavigateSfx(ui,
          wrapIncrement(ui.getCommandNum(), TRADE_MENU_MAX_INDEX));
    }
  }

  /** Estado: Pet Yard (gerenciador de pets). */
  public void petYardState(int code) {
    if (is(code, GameAction.CONFIRM)) {
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
        if (is(code, GameAction.OPTIONS)) {
          ui.setSubState(0);
        }
      }
      default -> {
        // noop
      }
    }
  }

  private void handlePetYardMenuNavigation(int code, UI ui) {
    if (is(code, GameAction.MOVE_UP)) {
      setCommandNumAndNavigateSfx(ui,
          wrapDecrement(ui.getCommandNum(), PET_YARD_MENU_MAX_INDEX));
    } else if (is(code, GameAction.MOVE_DOWN)) {
      setCommandNumAndNavigateSfx(ui,
          wrapIncrement(ui.getCommandNum(), PET_YARD_MENU_MAX_INDEX));
    } else if (is(code, GameAction.OPTIONS)) {
      gp.setGameState(GamePanel.PLAY_STATE);
    }
  }

  private void handlePetListNavigation(int code, UI ui) {
    var player = gp.getPlayer();
    int petCount = player.getOwnedPets().size();

    if (petCount <= 0) {
      if (is(code, GameAction.OPTIONS)) {
        ui.setSubState(0);
      }
      return;
    }

    int maxIndex = petCount - 1;
    if (is(code, GameAction.MOVE_UP)) {
      setCommandNumAndNavigateSfx(ui,
          wrapDecrement(ui.getCommandNum(), maxIndex));
    } else if (is(code, GameAction.MOVE_DOWN)) {
      setCommandNumAndNavigateSfx(ui,
          wrapIncrement(ui.getCommandNum(), maxIndex));
    } else if (is(code, GameAction.OPTIONS)) {
      ui.setSubState(0);
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
        if (maxIdx < 0)
          maxIdx = 0;
        if (is(code, GameAction.MOVE_UP)) {
          moveCommandNum(ui, -1, maxIdx);
        } else if (is(code, GameAction.MOVE_DOWN)) {
          moveCommandNum(ui, +1, maxIdx);
        } else if (is(code, GameAction.CONFIRM)) {
          bestiaryUi.setSelectedClassIndex(ui.getCommandNum());
          bestiaryUi.enterSelectedClass();
        } else if (is(code, GameAction.OPTIONS)
            || is(code, GameAction.BESTIARY)) {
          gp.setGameState(GamePanel.PLAY_STATE);
        }
      }
      case 1 -> { // Lista de criaturas
        if (is(code, GameAction.MOVE_UP)) {
          bestiaryUi.navigateUp();
          playNavigateSfx();
        } else if (is(code, GameAction.MOVE_DOWN)) {
          bestiaryUi.navigateDown();
          playNavigateSfx();
        } else if (is(code, GameAction.CONFIRM)) {
          bestiaryUi.enterSelectedCreature();
        } else if (is(code, GameAction.OPTIONS)) {
          bestiaryUi.goBack();
        }
      }
      case 2 -> { // Detalhes da criatura
        if (is(code, GameAction.MOVE_UP)) {
          bestiaryUi.navigateUp();
        } else if (is(code, GameAction.MOVE_DOWN)) {
          bestiaryUi.navigateDown();
        } else if (is(code, GameAction.OPTIONS)) {
          bestiaryUi.goBack();
        }
      }
      default -> {
        if (is(code, GameAction.OPTIONS)) {
          gp.setGameState(GamePanel.PLAY_STATE);
        }
      }
    }
  }

  public void mapState(int code) {
    if (is(code, GameAction.MAP)) {
      gp.setGameState(GamePanel.PLAY_STATE);
    } else if (is(code, GameAction.MAP_LAYER_NPC)) {
      gp.map.toggleLayer(tile.MapMarkerType.NPC);
    } else if (is(code, GameAction.MAP_LAYER_MONSTER)) {
      gp.map.toggleLayer(tile.MapMarkerType.MONSTER);
      gp.map.toggleLayer(tile.MapMarkerType.BOSS);
    } else if (is(code, GameAction.MAP_LAYER_INTERACTIVE)) {
      gp.map.toggleLayer(tile.MapMarkerType.INTERACTIVE);
    } else if (is(code, GameAction.MAP_LAYER_DROP)) {
      gp.map.toggleLayer(tile.MapMarkerType.DROP);
    }
  }

  // -----------------------
  // Inventory navigation
  // -----------------------

  public void playerInventory(int code) {
    final UI ui = gp.getGameUi();
    int row = ui.getPlayerSlotRow();
    int col = ui.getPlayerSlotCol();

    if (is(code, GameAction.MOVE_UP) && row != 0) {
      ui.setPlayerSlotRow(row - 1);
      playNavigateSfx();
    } else if (is(code, GameAction.MOVE_LEFT) && col != 0) {
      ui.setPlayerSlotCol(col - 1);
      playNavigateSfx();
    } else if (is(code, GameAction.MOVE_DOWN) && row != SLOT_ROW_MAX) {
      ui.setPlayerSlotRow(row + 1);
      playNavigateSfx();
    } else if (is(code, GameAction.MOVE_RIGHT) && col != SLOT_COL_MAX) {
      ui.setPlayerSlotCol(col + 1);
      playNavigateSfx();
    }
  }

  public void npcInventory(int code) {
    final UI ui = gp.getGameUi();
    int row = ui.getNpcSlotRow();
    int col = ui.getNpcSlotCol();

    if (is(code, GameAction.MOVE_UP) && row != 0) {
      ui.setNpcSlotRow(row - 1);
      playNavigateSfx();
    } else if (is(code, GameAction.MOVE_LEFT) && col != 0) {
      ui.setNpcSlotCol(col - 1);
      playNavigateSfx();
    } else if (is(code, GameAction.MOVE_DOWN) && row != SLOT_ROW_MAX) {
      ui.setNpcSlotRow(row + 1);
      playNavigateSfx();
    } else if (is(code, GameAction.MOVE_RIGHT) && col != SLOT_COL_MAX) {
      ui.setNpcSlotCol(col + 1);
      playNavigateSfx();
    }
  }

  // -----------------------
  // Private helpers (no behavior change)
  // -----------------------

  /** Consulta o binding para verificar se keyCode corresponde à ação. */
  private boolean is(int code, GameAction action) {
    return gp.getInputBindings().isAction(code, action);
  }

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

  /**
   * Alterna o Modo Deus (god mode) programaticamente.
   * <p>Chamado pelo {@link CheatCodeManager} (código secreto "finalclass").</p>
   */
  public void toggleGodMode() {
    godModeOn = !godModeOn;
  }

  public void setEnterPressed(boolean value) {
    this.enterPressed = value;
  }
}

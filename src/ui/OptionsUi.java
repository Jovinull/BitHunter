package ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.util.List;

import main.GameAction;
import main.GamePanel;
import main.KeyHandler;
import main.LanguageManager;

class OptionsUi {

  private static final int CONTROLS_PER_PAGE = 6;
  private static final int OPTIONS_FRAME_X_TILES = 6;
  private static final int OPTIONS_FRAME_Y_TILES = 1;
  private static final int OPTIONS_FRAME_WIDTH_TILES = 8;
  private static final int OPTIONS_FRAME_HEIGHT_TILES = 10;
  private static final int CURSOR_X_OFFSET = 25;
  private static final int FULLSCREEN_CHECKBOX_X_TILES = 4;
  private static final double FULLSCREEN_CHECKBOX_X_HALF_TILE = 0.5;
  private static final int FULLSCREEN_CHECKBOX_Y_OFFSET = 22;
  private static final int CHECKBOX_SIZE = 24;
  private static final int CHECKBOX_INNER_OFFSET = 2;
  private static final int CHECKBOX_INNER_SIZE = 20;
  private static final int VOLUME_BAR_WIDTH = 120;
  private static final int VOLUME_BAR_HEIGHT = 24;
  private static final int VOLUME_FILL_INSET = 1;
  private static final int VOLUME_FILL_REDUCTION = 2;
  private static final int VOLUME_FILL_HEIGHT = 22;
  private static final int VOLUME_SCALE_MULTIPLIER = 24;
  private static final int DIALOG_LINE_HEIGHT = 32;
  private static final double CONTROLS_KEYS_X_TILES = 5.5;
  private static final double CONTROLS_PAGE_LABEL_Y_TILES = 8.2;
  private static final double CONTROLS_HINT_Y_TILES = 8.5;
  private static final int LANGUAGE_ARROW_X_TILES = 7;
  private static final int LANGUAGE_UP_ARROW_Y_TILES = 4;

  private final UI ui;

  OptionsUi(UI ui) {
    this.ui = ui;
  }

  void drawOptionsScreen() {
    var g2 = ui.g2;
    g2.setColor(UI.COL_TEXT);
    g2.setFont(ui.fonts.pixelPlain(32F));

    int frameX = GamePanel.TILE_SIZE * OPTIONS_FRAME_X_TILES;
    int frameY = GamePanel.TILE_SIZE * OPTIONS_FRAME_Y_TILES;
    int frameWidth = GamePanel.TILE_SIZE * OPTIONS_FRAME_WIDTH_TILES;
    int frameHeight = GamePanel.TILE_SIZE * OPTIONS_FRAME_HEIGHT_TILES;
    ui.drawSubWindow(frameX, frameY, frameWidth, frameHeight);

    switch (ui.getSubState()) {
      case 0 -> optionsTop(frameX, frameY);
      case 1 -> optionsFullScreenNotification(frameX, frameY);
      case 2 -> optionsControl(frameX, frameY);
      case 3 -> optionsEndGameConfirmation(frameX, frameY);
      case 4 -> optionsLanguage(frameX, frameY);
      default -> {
        // subState fora do intervalo esperado; intencionalmente não desenha nada extra
      }
    }

    ui.getKeyHandler().setEnterPressed(false);
  }

  private void optionsTop(int frameX, int frameY) {
    var g2 = ui.g2;
    var gp = ui.getGamePanel();

    int textX;
    int textY;

    String text = ui.t("ui.options.title");
    textX = ui.getXforCenteredText(text);
    textY = frameY + GamePanel.TILE_SIZE;
    ui.drawTextShadow(text, textX, textY, UI.COL_TEXT, Color.black, 2);

    var keyH = gp.getKeyH();

    // TELA CHEIA
    textX = frameX + GamePanel.TILE_SIZE;
    textY = frameY + GamePanel.TILE_SIZE * 2;
    g2.setColor(UI.COL_TEXT);
    g2.drawString(ui.t("ui.options.fullscreen"), textX, textY);
    if (ui.commandNum == 0) {
      g2.drawString(">", textX - CURSOR_X_OFFSET, textY);
      if (keyH.isEnterPressed()) {
        gp.setFullScreenOn(!gp.isFullScreenOn());
        ui.setSubState(1);
      }
    }

    // MÚSICA
    textY += GamePanel.TILE_SIZE;
    g2.drawString(ui.t("ui.options.music"), textX, textY);
    if (ui.commandNum == 1) {
      g2.drawString(">", textX - CURSOR_X_OFFSET, textY);
    }

    // EFEITOS SONOROS
    textY += GamePanel.TILE_SIZE;
    g2.drawString(ui.t("ui.options.effects"), textX, textY);
    if (ui.commandNum == 2) {
      g2.drawString(">", textX - CURSOR_X_OFFSET, textY);
    }

    // CONTROLES
    textY += GamePanel.TILE_SIZE;
    g2.drawString(ui.t("ui.options.controls"), textX, textY);
    if (ui.commandNum == 3) {
      g2.drawString(">", textX - CURSOR_X_OFFSET, textY);
      if (keyH.isEnterPressed()) {
        ui.setSubState(2);
        ui.commandNum = 0;
        ui.setControlsPage(0);
        ui.waitingForRebind = false;
        ui.rebindControlIndex = -1;
      }
    }

    // IDIOMA
    textY += GamePanel.TILE_SIZE;
    String currentLangName = ui.getLanguageManager().getCurrentLanguageDisplayName();
    String languageLine = ui.t("ui.options.language") + ": " + currentLangName;
    g2.drawString(languageLine, textX, textY);
    if (ui.commandNum == 4) {
      g2.drawString(">", textX - CURSOR_X_OFFSET, textY);
      if (keyH.isEnterPressed()) {
        ui.setSubState(4);
        ui.commandNum = 0;
        ui.languageScrollOffset = 0;
      }
    }

    // ENCERRAR JOGO
    textY += GamePanel.TILE_SIZE;
    g2.drawString(ui.t("ui.options.endGame"), textX, textY);
    if (ui.commandNum == 5) {
      g2.drawString(">", textX - CURSOR_X_OFFSET, textY);
      if (keyH.isEnterPressed()) {
        ui.setSubState(3);
        ui.commandNum = 0;
      }
    }

    // Dica de voltar
    textY += GamePanel.TILE_SIZE * 2;
    g2.setColor(UI.COL_TEXT_DIM);
    g2.drawString(ui.t(UI.KEY_TRADE_HINT_BACK), textX, textY);

    // CHECKBOX TELA CHEIA
    textX = frameX + GamePanel.TILE_SIZE * FULLSCREEN_CHECKBOX_X_TILES
        + (int) (GamePanel.TILE_SIZE * FULLSCREEN_CHECKBOX_X_HALF_TILE);
    textY = frameY + GamePanel.TILE_SIZE * 2 - FULLSCREEN_CHECKBOX_Y_OFFSET;
    g2.setStroke(new BasicStroke(3));
    g2.setColor(UI.COL_PANEL_BORDER);
    g2.drawRect(textX, textY, CHECKBOX_SIZE, CHECKBOX_SIZE);
    if (gp.isFullScreenOn()) {
      g2.setColor(UI.COL_ACCENT);
      g2.fillRect(textX + CHECKBOX_INNER_OFFSET, textY + CHECKBOX_INNER_OFFSET, CHECKBOX_INNER_SIZE, CHECKBOX_INNER_SIZE);
    }

    // VOLUME MÚSICA
    textY += GamePanel.TILE_SIZE;
    g2.setColor(UI.COL_PANEL_BORDER);
    g2.drawRect(textX, textY, VOLUME_BAR_WIDTH, VOLUME_BAR_HEIGHT);
    g2.setColor(UI.COL_ACCENT);
    int volumeWidth = VOLUME_SCALE_MULTIPLIER * gp.getMusic().getVolumeScale();
    g2.fillRect(textX + VOLUME_FILL_INSET, textY + VOLUME_FILL_INSET,
        Math.max(0, volumeWidth - VOLUME_FILL_REDUCTION), VOLUME_FILL_HEIGHT);

    // VOLUME EFEITOS
    textY += GamePanel.TILE_SIZE;
    g2.setColor(UI.COL_PANEL_BORDER);
    g2.drawRect(textX, textY, VOLUME_BAR_WIDTH, VOLUME_BAR_HEIGHT);
    g2.setColor(UI.COL_ACCENT);
    volumeWidth = VOLUME_SCALE_MULTIPLIER * gp.getSe().getVolumeScale();
    g2.fillRect(textX + VOLUME_FILL_INSET, textY + VOLUME_FILL_INSET,
        Math.max(0, volumeWidth - VOLUME_FILL_REDUCTION), VOLUME_FILL_HEIGHT);

    gp.getConfig().saveConfig();
  }

  private void optionsLanguage(int frameX, int frameY) {
    var gp = ui.getGamePanel();
    LanguageManager langManager = gp.getLanguageManager();
    List<LanguageManager.LanguageOption> langs = langManager.getAvailableLanguages();

    int textX = frameX + GamePanel.TILE_SIZE;
    int textY = drawLanguageHeader(frameX, frameY, langManager);

    updateLanguageScrollState(langs.size());

    KeyHandler keyH = gp.getKeyH();
    textY = drawLanguageOptionsList(langs, textX, textY, keyH, langManager);

    drawScrollIndicators(frameX, frameY, textY, langs.size());
    drawBackOption(textX, textY, keyH, langs.size());
  }

  private int drawLanguageHeader(int frameX, int frameY, LanguageManager langManager) {
    var g2 = ui.g2;

    String title = ui.t("ui.options.languageTitle");
    int titleX = ui.getXforCenteredText(title);
    int titleY = frameY + GamePanel.TILE_SIZE;
    ui.drawTextShadow(title, titleX, titleY, UI.COL_TEXT, Color.black, 2);

    int textX = frameX + GamePanel.TILE_SIZE;
    int currentLabelY = titleY + GamePanel.TILE_SIZE * 2;
    String currentLabel = ui.t("ui.options.languageCurrent") + ": " + langManager.getCurrentLanguageDisplayName();

    g2.setColor(UI.COL_TEXT_DIM);
    g2.drawString(currentLabel, textX, currentLabelY);

    return currentLabelY + GamePanel.TILE_SIZE;
  }

  private void updateLanguageScrollState(int totalLanguages) {
    int lastIndex = totalLanguages; // último índice é o "Voltar"

    if (ui.commandNum < 0) {
      ui.commandNum = 0;
    } else if (ui.commandNum > lastIndex) {
      ui.commandNum = lastIndex;
    }

    int visibleCount = Math.min(UI.MAX_VISIBLE_LANG_OPTIONS, totalLanguages);
    int maxOffset = Math.max(0, totalLanguages - visibleCount);

    if (ui.commandNum < totalLanguages) {
      if (ui.commandNum < ui.languageScrollOffset) {
        ui.languageScrollOffset = ui.commandNum;
      } else if (ui.commandNum > ui.languageScrollOffset + visibleCount - 1) {
        ui.languageScrollOffset = ui.commandNum - visibleCount + 1;
      }
    }

    if (ui.languageScrollOffset < 0) {
      ui.languageScrollOffset = 0;
    } else if (ui.languageScrollOffset > maxOffset) {
      ui.languageScrollOffset = maxOffset;
    }
  }

  private int drawLanguageOptionsList(
      List<LanguageManager.LanguageOption> langs,
      int textX,
      int textY,
      KeyHandler keyH,
      LanguageManager langManager) {

    var g2 = ui.g2;
    var gp = ui.getGamePanel();

    g2.setColor(UI.COL_TEXT);

    int totalLanguages = langs.size();
    int visibleCount = Math.min(UI.MAX_VISIBLE_LANG_OPTIONS, totalLanguages);
    int start = ui.languageScrollOffset;
    int end = Math.min(ui.languageScrollOffset + visibleCount, totalLanguages);

    for (int i = start; i < end; i++) {
      LanguageManager.LanguageOption opt = langs.get(i);
      String name = opt.displayName();

      g2.drawString(name, textX, textY);
      if (ui.commandNum == i) {
        g2.drawString(">", textX - CURSOR_X_OFFSET, textY);
        if (keyH.isEnterPressed()) {
          langManager.setCurrentLanguage(opt.code());
          gp.getConfig().saveConfig();
          gp.playSE(9);
        }
      }
      textY += GamePanel.TILE_SIZE;
    }

    return textY;
  }

  private void drawScrollIndicators(int frameX, int frameY, int textY, int totalLanguages) {
    var g2 = ui.g2;

    int visibleCount = Math.min(UI.MAX_VISIBLE_LANG_OPTIONS, totalLanguages);
    int maxOffset = Math.max(0, totalLanguages - visibleCount);
    int arrowX = frameX + GamePanel.TILE_SIZE * LANGUAGE_ARROW_X_TILES;

    if (ui.languageScrollOffset > 0) {
      g2.setColor(UI.COL_TEXT_DIM);
      g2.drawString("↑", arrowX, frameY + GamePanel.TILE_SIZE * LANGUAGE_UP_ARROW_Y_TILES);
    }
    if (ui.languageScrollOffset < maxOffset) {
      g2.setColor(UI.COL_TEXT_DIM);
      g2.drawString("↓", arrowX, textY - GamePanel.TILE_SIZE / 2);
    }
    g2.setColor(UI.COL_TEXT);
  }

  private void drawBackOption(int textX, int textY, KeyHandler keyH, int totalLanguages) {
    var g2 = ui.g2;

    int backIndex = totalLanguages;
    String backLabel = ui.t(UI.KEY_COMMON_BACK);

    g2.drawString(backLabel, textX, textY);
    if (ui.commandNum == backIndex) {
      g2.drawString(">", textX - CURSOR_X_OFFSET, textY);
      if (keyH.isEnterPressed()) {
        ui.setSubState(0);
        ui.commandNum = 4;
        keyH.setEnterPressed(false);
      }
    }
  }

  private void optionsFullScreenNotification(int frameX, int frameY) {
    var g2 = ui.g2;
    var gp = ui.getGamePanel();

    int textX = frameX + GamePanel.TILE_SIZE;
    int textY = frameY + GamePanel.TILE_SIZE * 3;

    ui.currentDialogue = ui.t("ui.options.fullscreenNotice");

    g2.setColor(UI.COL_TEXT);
    for (String line : ui.currentDialogue.split("\n")) {
      g2.drawString(line, textX, textY);
      textY += DIALOG_LINE_HEIGHT;
    }

    textY = frameY + GamePanel.TILE_SIZE * 9;
    g2.drawString(ui.t(UI.KEY_COMMON_BACK), textX, textY);
    if (ui.commandNum == 0) {
      g2.drawString(">", textX - CURSOR_X_OFFSET, textY);
      if (gp.getKeyH().isEnterPressed()) {
        ui.setSubState(0);
      }
    }
  }

  /**
   * Rótulos i18n para cada ação remapeável.
   * Deve ter a MESMA ordem que {@link GameAction#getRemappable()}.
   */
  private static final String[] CONTROL_LABEL_KEYS = {
      "ui.controls.moveUp",
      "ui.controls.moveDown",
      "ui.controls.moveLeft",
      "ui.controls.moveRight",
      "ui.controls.confirmAttack",
      "ui.controls.shootCast",
      "ui.controls.guard",
      "ui.controls.pause",
      "ui.controls.characterScreen",
      "ui.controls.options",
      "ui.controls.minimap",
      "ui.controls.map",
      "ui.controls.bestiary",
  };

  /**
   * Tela de controles interativa: o jogador navega pelas ações,
   * pressiona ENTER para remapear e a tecla é capturada em tempo real.
   */
  private void optionsControl(int frameX, int frameY) {
    var g2 = ui.g2;
    var gp = ui.getGamePanel();
    var bindings = gp.getInputBindings();
    var remappable = GameAction.getRemappable();

    // ── Título ──
    int titleY = frameY + GamePanel.TILE_SIZE;
    String title = ui.t("ui.controls.title");
    int titleX = ui.getXforCenteredText(title);
    ui.drawTextShadow(title, titleX, titleY, UI.COL_TEXT, Color.black, 2);

    int totalControls = remappable.size();
    int perPage = CONTROLS_PER_PAGE;

    // ── Paginação ──
    int pageCount = calculatePageCount(totalControls, perPage);
    ui.setControlsPageCount(pageCount);

    // Auto-ajusta página com base na seleção atual
    int currentPage = resolveCurrentControlsPage(totalControls, perPage, pageCount);
    ui.setControlsPage(currentPage);

    int startIndex = currentPage * perPage;
    int endIndex = Math.min(startIndex + perPage, totalControls);

    // ── Entradas da página atual ──
    int textX = frameX + GamePanel.TILE_SIZE;
    int keysX = frameX + (int) (GamePanel.TILE_SIZE * CONTROLS_KEYS_X_TILES);
    int textY = titleY + GamePanel.TILE_SIZE;

    for (int i = startIndex; i < endIndex; i++) {
      GameAction action = remappable.get(i);
      boolean isSelected = (ui.commandNum == i);
      boolean isRebinding = ui.waitingForRebind && ui.rebindControlIndex == i;

      // Rótulo da ação
      g2.setColor(isSelected ? UI.COL_ACCENT : UI.COL_TEXT);
      g2.drawString(ui.t(CONTROL_LABEL_KEYS[i]), textX, textY);

      // Tecla atribuída (ou "..." durante rebind)
      String keyDisplay;
      if (isRebinding) {
        keyDisplay = "...";
        g2.setColor(UI.COL_ACCENT);
      } else {
        g2.setColor(UI.COL_TEXT);
        keyDisplay = bindings.getKeyDisplayName(action);
      }
      g2.drawString(keyDisplay, keysX, textY);

      // Cursor de seleção
      if (isSelected) {
        g2.setColor(UI.COL_ACCENT);
        g2.drawString(">", textX - CURSOR_X_OFFSET, textY);
      }

      textY += GamePanel.TILE_SIZE;
    }

    // ── Indicador de página ──
    if (pageCount > 1) {
      String pageLabel = (currentPage + 1) + "/" + pageCount;
      int pageX = frameX + GamePanel.TILE_SIZE * 6;
      int pageY = frameY + (int) (GamePanel.TILE_SIZE * CONTROLS_PAGE_LABEL_Y_TILES);
      g2.setColor(UI.COL_TEXT_DIM);
      g2.drawString(pageLabel, pageX, pageY);
    }

    // ── Dica de remapeamento ──
    if (ui.waitingForRebind) {
      g2.setColor(UI.COL_ACCENT);
      g2.setFont(ui.fonts.pixelPlain(24F));
      String hint = ui.t("ui.controls.pressKey");
      int hintX = frameX + GamePanel.TILE_SIZE;
      int hintY = frameY + (int) (GamePanel.TILE_SIZE * CONTROLS_HINT_Y_TILES);
      g2.drawString(hint, hintX, hintY);
      g2.setFont(ui.fonts.pixelPlain(32F)); // restaura fonte
    }

    // ── Botão "Voltar" ──
    textX = frameX + GamePanel.TILE_SIZE;
    textY = frameY + GamePanel.TILE_SIZE * 9;
    g2.setColor(UI.COL_TEXT);
    g2.drawString(ui.t(UI.KEY_COMMON_BACK), textX, textY);
    if (ui.commandNum == totalControls) {
      g2.setColor(UI.COL_ACCENT);
      g2.drawString(">", textX - CURSOR_X_OFFSET, textY);
    }
  }

  private int calculatePageCount(int totalControls, int perPage) {
    int pageCount = (totalControls + perPage - 1) / perPage;
    return Math.max(1, pageCount);
  }

  private int resolveCurrentControlsPage(int totalControls, int perPage, int pageCount) {
    int currentPage = ui.getControlsPage();
    if (ui.commandNum >= 0 && ui.commandNum < totalControls) {
      currentPage = ui.commandNum / perPage;
    }
    if (currentPage < 0) {
      return 0;
    }
    if (currentPage > pageCount - 1) {
      return pageCount - 1;
    }
    return currentPage;
  }

  private void optionsEndGameConfirmation(int frameX, int frameY) {
    var g2 = ui.g2;
    var gp = ui.getGamePanel();

    int textX = frameX + GamePanel.TILE_SIZE;
    int textY = frameY + GamePanel.TILE_SIZE * 3;

    ui.currentDialogue = ui.t("ui.endGame.confirmation");

    g2.setColor(UI.COL_TEXT);
    for (String line : ui.currentDialogue.split("\n")) {
      g2.drawString(line, textX, textY);
      textY += DIALOG_LINE_HEIGHT;
    }

    String text = ui.t("ui.common.yes");
    textX = ui.getXforCenteredText(text);
    textY += GamePanel.TILE_SIZE * 2;
    g2.drawString(text, textX, textY);
    var keyH = gp.getKeyH();
    if (ui.commandNum == 0) {
      g2.drawString(">", textX - CURSOR_X_OFFSET, textY);
      if (keyH.isEnterPressed()) {
        gp.getPlayTimeTracker().stop();
        gp.getSaveManager().save();
        ui.setSubState(0);
        ui.commandNum = 0;
        ui.setTitleScreenState(0);
        gp.stopMusic();
        gp.setGameState(GamePanel.TITLE_STATE);
        keyH.setEnterPressed(false);
        gp.resetGame(true);
      }
    }

    text = ui.t("ui.common.no");
    textX = ui.getXforCenteredText(text);
    textY += GamePanel.TILE_SIZE;
    g2.drawString(text, textX, textY);
    if (ui.commandNum == 1) {
      g2.drawString(">", textX - CURSOR_X_OFFSET, textY);
      if (keyH.isEnterPressed()) {
        ui.setSubState(0);
        ui.commandNum = 4;
        keyH.setEnterPressed(false);
      }
    }
  }
}

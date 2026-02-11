package ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.util.List;

import main.GamePanel;
import main.KeyHandler;
import main.LanguageManager;

class OptionsUi {

  private static final int CONTROLS_PER_PAGE = 6;

  private final UI ui;

  OptionsUi(UI ui) {
    this.ui = ui;
  }

  void drawOptionsScreen() {
    var g2 = ui.g2;
    g2.setColor(UI.COL_TEXT);
    g2.setFont(ui.fonts.pixelPlain(32F));

    int frameX = GamePanel.TILE_SIZE * 6;
    int frameY = GamePanel.TILE_SIZE;
    int frameWidth = GamePanel.TILE_SIZE * 8;
    int frameHeight = GamePanel.TILE_SIZE * 10;
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
      g2.drawString(">", textX - 25, textY);
      if (keyH.isEnterPressed()) {
        gp.setFullScreenOn(!gp.isFullScreenOn());
        ui.setSubState(1);
      }
    }

    // MÚSICA
    textY += GamePanel.TILE_SIZE;
    g2.drawString(ui.t("ui.options.music"), textX, textY);
    if (ui.commandNum == 1) {
      g2.drawString(">", textX - 25, textY);
    }

    // EFEITOS SONOROS
    textY += GamePanel.TILE_SIZE;
    g2.drawString(ui.t("ui.options.effects"), textX, textY);
    if (ui.commandNum == 2) {
      g2.drawString(">", textX - 25, textY);
    }

    // CONTROLES
    textY += GamePanel.TILE_SIZE;
    g2.drawString(ui.t("ui.options.controls"), textX, textY);
    if (ui.commandNum == 3) {
      g2.drawString(">", textX - 25, textY);
      if (keyH.isEnterPressed()) {
        ui.setSubState(2);
        ui.commandNum = 0;
        ui.setControlsPage(0);
      }
    }

    // IDIOMA
    textY += GamePanel.TILE_SIZE;
    String currentLangName = ui.getLanguageManager().getCurrentLanguageDisplayName();
    String languageLine = ui.t("ui.options.language") + ": " + currentLangName;
    g2.drawString(languageLine, textX, textY);
    if (ui.commandNum == 4) {
      g2.drawString(">", textX - 25, textY);
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
      g2.drawString(">", textX - 25, textY);
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
    textX = frameX + (int) (GamePanel.TILE_SIZE * 4.5);
    textY = frameY + GamePanel.TILE_SIZE * 2 - 22;
    g2.setStroke(new BasicStroke(3));
    g2.setColor(UI.COL_PANEL_BORDER);
    g2.drawRect(textX, textY, 24, 24);
    if (gp.isFullScreenOn()) {
      g2.setColor(UI.COL_ACCENT);
      g2.fillRect(textX + 2, textY + 2, 20, 20);
    }

    // VOLUME MÚSICA
    textY += GamePanel.TILE_SIZE;
    g2.setColor(UI.COL_PANEL_BORDER);
    g2.drawRect(textX, textY, 120, 24);
    g2.setColor(UI.COL_ACCENT);
    int volumeWidth = 24 * gp.getMusic().getVolumeScale();
    g2.fillRect(textX + 1, textY + 1, Math.max(0, volumeWidth - 2), 22);

    // VOLUME EFEITOS
    textY += GamePanel.TILE_SIZE;
    g2.setColor(UI.COL_PANEL_BORDER);
    g2.drawRect(textX, textY, 120, 24);
    g2.setColor(UI.COL_ACCENT);
    volumeWidth = 24 * gp.getSe().getVolumeScale();
    g2.fillRect(textX + 1, textY + 1, Math.max(0, volumeWidth - 2), 22);

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
        g2.drawString(">", textX - 25, textY);
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
    int arrowX = frameX + GamePanel.TILE_SIZE * 7;

    if (ui.languageScrollOffset > 0) {
      g2.setColor(UI.COL_TEXT_DIM);
      g2.drawString("↑", arrowX, frameY + GamePanel.TILE_SIZE * 4);
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
      g2.drawString(">", textX - 25, textY);
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
      textY += 32;
    }

    textY = frameY + GamePanel.TILE_SIZE * 9;
    g2.drawString(ui.t(UI.KEY_COMMON_BACK), textX, textY);
    if (ui.commandNum == 0) {
      g2.drawString(">", textX - 25, textY);
      if (gp.getKeyH().isEnterPressed()) {
        ui.setSubState(0);
      }
    }
  }

  private void optionsControl(int frameX, int frameY) {
    var g2 = ui.g2;
    var gp = ui.getGamePanel();

    // Título
    int titleY = frameY + GamePanel.TILE_SIZE;
    String title = ui.t("ui.controls.title");
    int titleX = ui.getXforCenteredText(title);
    ui.drawTextShadow(title, titleX, titleY, UI.COL_TEXT, Color.black, 2);

    final String[] labelKeys = {
        "ui.controls.move",
        "ui.controls.confirmAttack",
        "ui.controls.shootCast",
        "ui.controls.characterScreen",
        "ui.controls.pause",
        "ui.controls.options",
        "ui.controls.minimap",
        "ui.controls.map"
    };

    final String[] keyLabels = {
        "WASD",
        "ENTER",
        "F",
        "C",
        "P",
        "ESC",
        "X",
        "M"
    };

    int totalControls = labelKeys.length;
    int perPage = CONTROLS_PER_PAGE;

    // Quantidade total de páginas
    int pageCount = (totalControls + perPage - 1) / perPage;
    if (pageCount <= 0) {
      pageCount = 1;
    }
    ui.setControlsPageCount(pageCount);

    int currentPage = ui.getControlsPage();
    if (currentPage < 0) {
      currentPage = 0;
    }
    if (currentPage > pageCount - 1) {
      currentPage = pageCount - 1;
    }
    ui.setControlsPage(currentPage);

    int startIndex = currentPage * perPage;
    int endIndex = Math.min(startIndex + perPage, totalControls);

    int textX = frameX + GamePanel.TILE_SIZE;
    int textY = titleY + GamePanel.TILE_SIZE;
    g2.setColor(UI.COL_TEXT);

    for (int i = startIndex; i < endIndex; i++) {
      g2.drawString(ui.t(labelKeys[i]), textX, textY);
      textY += GamePanel.TILE_SIZE;
    }

    int keysX = frameX + GamePanel.TILE_SIZE * 6;
    int keysY = titleY + GamePanel.TILE_SIZE;

    for (int i = startIndex; i < endIndex; i++) {
      g2.drawString(keyLabels[i], keysX, keysY);
      keysY += GamePanel.TILE_SIZE;
    }

    if (pageCount > 1) {
      String pageLabel = (currentPage + 1) + "/" + pageCount;
      int pageX = frameX + GamePanel.TILE_SIZE * 6;
      int pageY = frameY + GamePanel.TILE_SIZE * 9;
      g2.setColor(UI.COL_TEXT_DIM);
      g2.drawString(pageLabel, pageX, pageY);
    }

    textX = frameX + GamePanel.TILE_SIZE;
    textY = frameY + GamePanel.TILE_SIZE * 9;
    g2.setColor(UI.COL_TEXT);
    g2.drawString(ui.t(UI.KEY_COMMON_BACK), textX, textY);
    if (ui.commandNum == 0) {
      g2.drawString(">", textX - 25, textY);
      if (gp.getKeyH().isEnterPressed()) {
        ui.setControlsPage(0);
        ui.setSubState(0);
        ui.commandNum = 3;
      }
    }
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
      textY += 32;
    }

    String text = ui.t("ui.common.yes");
    textX = ui.getXforCenteredText(text);
    textY += GamePanel.TILE_SIZE * 2;
    g2.drawString(text, textX, textY);
    var keyH = gp.getKeyH();
    if (ui.commandNum == 0) {
      g2.drawString(">", textX - 25, textY);
      if (keyH.isEnterPressed()) {
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
      g2.drawString(">", textX - 25, textY);
      if (keyH.isEnterPressed()) {
        ui.setSubState(0);
        ui.commandNum = 4;
        keyH.setEnterPressed(false);
      }
    }
  }
}

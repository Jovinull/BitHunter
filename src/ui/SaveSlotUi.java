package ui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import data.PlayTimeTracker;
import data.SaveManager;
import data.SaveManager.SlotInfo;
import main.GamePanel;

/**
 * UI reutilizável para a tela de seleção de slots de save.
 * Usada tanto por "Novo Jogo" quanto por "Continuar".
 */
class SaveSlotUi {

  static final int MODE_NEW_GAME = 0;
  static final int MODE_CONTINUE = 1;

  private static final int MAX_NAME_LENGTH = 12;
  private static final double SLOT_CARD_WIDTH_RATIO = 0.7;
  private static final int SLOT_CARD_HEIGHT_TILES = 2;
  private static final double SLOT_START_Y_TILES = 2.8;
  private static final int SLOT_CARD_GAP = 10;
  private static final int SLOT_CARD_ROUND = 10;
  private static final int SLOT_TEXT_X_OFFSET = 16;
  private static final int SLOT_LINE_HEIGHT = 24;
  private static final int SLOT_FIRST_LINE_Y_OFFSET = 6;
  private static final int SLOT_INFO_X_OFFSET = 170;
  private static final int SLOT_RIGHT_PADDING = 16;
  private static final int SLOT_TRIANGLE_X_OFFSET = 20;
  private static final int SLOT_TRIANGLE_Y_OFFSET = 10;
  private static final int SLOT_TRIANGLE_WIDTH = 14;
  private static final int SLOT_TRIANGLE_MID_Y_OFFSET = 10;
  private static final int SLOT_TRIANGLE_BOTTOM_Y_OFFSET = 20;
  private static final double DIALOG_WIDTH_RATIO = 0.85;
  private static final int DIALOG_HEIGHT_TILES = 5;
  private static final int DIALOG_TEXT_Y_OFFSET = 10;
  private static final int DIALOG_SECOND_LINE_Y_OFFSET = 30;
  private static final int DIALOG_BUTTON_Y_OFFSET = 90;
  private static final double NAME_FIELD_WIDTH_RATIO = 0.5;
  private static final int NAME_FIELD_HEIGHT_EXTRA = 8;
  private static final int NAME_TEXT_X_OFFSET = 16;
  private static final int NAME_TEXT_Y_OFFSET = 12;
  private static final int CURSOR_BLINK_MS = 500;
  private static final int CURSOR_X_OFFSET = 2;
  private static final int CURSOR_Y_OFFSET = 24;
  private static final int CURSOR_WIDTH = 3;
  private static final int CURSOR_HEIGHT = 28;

  private static final Color COL_DISABLED_BG = new Color(0x08, 0x0A, 0x18);
  private static final Color COL_DISABLED_BORDER = new Color(0x18, 0x1E, 0x30);
  private static final Color COL_DISABLED_TEXT = new Color(0x60, 0x68, 0x80);
  private static final Color COL_CORRUPTED = new Color(0xFF, 0x55, 0x55);
  private static final Color COL_OVERLAY = new Color(0, 0, 0, 180);

  private final UI ui;
  private SlotInfo[] slotInfos;

  SaveSlotUi(UI ui) {
    this.ui = ui;
  }

  void refreshSlotInfos() {
    slotInfos = ui.getGamePanel().getSaveManager().getAllSlotInfos();
  }

  SlotInfo[] getSlotInfos() {
    if (slotInfos == null) {
      refreshSlotInfos();
    }
    return slotInfos;
  }

  int getMaxNameLength() {
    return MAX_NAME_LENGTH;
  }

  // ============================================================
  // Tela de seleção de slots
  // ============================================================

  void drawSlotSelection(int mode) {
    Graphics2D g2 = ui.g2;

    g2.setColor(UI.COL_BG);
    g2.fillRect(0, 0, GamePanel.SCREEN_WIDTH, GamePanel.SCREEN_HEIGHT);

    String title = (mode == MODE_NEW_GAME)
        ? ui.t("ui.save.titleNewGame")
        : ui.t("ui.save.titleContinue");
    g2.setFont(ui.fonts.displayBold(48F));
    ui.drawTextShadowCentered(title, GamePanel.TILE_SIZE * 2,
        UI.COL_TEXT, Color.black, 2);

    SlotInfo[] infos = getSlotInfos();
    int cardW = (int) (GamePanel.SCREEN_WIDTH * SLOT_CARD_WIDTH_RATIO);
    int cardH = GamePanel.TILE_SIZE * SLOT_CARD_HEIGHT_TILES;
    int cardX = (GamePanel.SCREEN_WIDTH - cardW) / 2;
    int startY = (int) (GamePanel.TILE_SIZE * SLOT_START_Y_TILES);
    int gap = SLOT_CARD_GAP;

    g2.setFont(ui.fonts.displayPlain(28F));
    for (int i = 0; i < SaveManager.MAX_SLOTS; i++) {
      int y = startY + i * (cardH + gap);
      boolean isDialogActive = (ui.getTitleScreenState() == 3 || ui.getTitleScreenState() == 4);
      boolean selected;
      if (isDialogActive) {
        // If a dialog is open on top, keep the selection fixed on the slot that
        // triggered it
        selected = ((ui.getSelectedSaveSlot() - 1) == i);
      } else {
        selected = (ui.getCommandNum() == i);
      }

      boolean disabled = (mode == MODE_CONTINUE)
          && (!infos[i].occupied() || infos[i].corrupt());
      drawSlotCard(new Rectangle(cardX, y, cardW, cardH), i + 1, infos[i],
          selected, disabled);
    }

    int backY = startY + SaveManager.MAX_SLOTS * (cardH + gap) + gap;
    g2.setFont(ui.fonts.displayBold(36F));
    ui.drawMenuItem(ui.t("ui.common.back"), backY,
        ui.getCommandNum() == SaveManager.MAX_SLOTS);

    ui.useCrispPixelText(g2);
    g2.setFont(ui.fonts.pixelPlain(16F));
    ui.drawTextShadowCentered(
        ui.t("ui.save.hintNavigation"),
        GamePanel.SCREEN_HEIGHT - GamePanel.TILE_SIZE / 2,
        UI.COL_TEXT_DIM, Color.black, 2);
  }

  private void drawSlotCard(Rectangle bounds,
      int slotNum, SlotInfo info, boolean selected, boolean disabled) {
    int x = bounds.x;
    int y = bounds.y;
    int w = bounds.width;
    int h = bounds.height;

    Graphics2D g2 = ui.g2;

    Color bgColor = disabled ? COL_DISABLED_BG : UI.COL_PANEL;
    if (selected && !disabled) {
      bgColor = UI.COL_HIGHLIGHT_BG;
    }
    g2.setColor(bgColor);
    g2.fillRoundRect(x, y, w, h, SLOT_CARD_ROUND, SLOT_CARD_ROUND);

    Color borderColor = selected ? UI.COL_ACCENT : UI.COL_PANEL_BORDER;
    if (disabled) {
      borderColor = COL_DISABLED_BORDER;
    }
    g2.setColor(borderColor);
    g2.drawRoundRect(x, y, w, h, SLOT_CARD_ROUND, SLOT_CARD_ROUND);

    int textX = x + SLOT_TEXT_X_OFFSET;
    int lineH = SLOT_LINE_HEIGHT;
    int firstLineY = y + lineH + SLOT_FIRST_LINE_Y_OFFSET;

    g2.setFont(ui.fonts.displayBold(28F));
    g2.setColor(disabled ? COL_DISABLED_TEXT : UI.COL_ACCENT);
    g2.drawString(ui.t("ui.save.slotPrefix") + " " + slotNum,
        textX, firstLineY);

    int infoX = textX + SLOT_INFO_X_OFFSET;
    int rightPad = x + w - SLOT_RIGHT_PADDING;

    drawSlotInfo(g2, info, infoX, firstLineY, lineH, rightPad, disabled);

    if (selected && !disabled) {
      int triX = x - SLOT_TRIANGLE_X_OFFSET;
      int triY = y + h / 2 - SLOT_TRIANGLE_Y_OFFSET;
      int[] xs = { triX, triX + SLOT_TRIANGLE_WIDTH, triX };
      int[] ys = { triY, triY + SLOT_TRIANGLE_MID_Y_OFFSET, triY + SLOT_TRIANGLE_BOTTOM_Y_OFFSET };
      g2.setColor(UI.COL_ACCENT);
      g2.fillPolygon(xs, ys, 3);
    }
  }

  private void drawSlotInfo(Graphics2D g2, SlotInfo info, int infoX, int firstLineY, int lineH, int rightPad,
      boolean disabled) {
    g2.setFont(ui.fonts.displayPlain(22F));
    String timeLabel = ui.t("ui.save.playTime") + " ";

    if (info.corrupt()) {
      g2.setColor(COL_CORRUPTED);
      g2.drawString(ui.t("ui.save.corrupted"), infoX, firstLineY);

      String timeText = timeLabel + ui.t("ui.save.playTimeUnavailable");
      g2.setFont(ui.fonts.displayPlain(18F));
      g2.setColor(COL_CORRUPTED);
      int tw = (int) g2.getFontMetrics().getStringBounds(timeText, g2).getWidth();
      g2.drawString(timeText, rightPad - tw, firstLineY);
      g2.setFont(ui.fonts.displayPlain(22F));
    } else if (info.occupied()) {
      String pName = info.playerName();
      if (pName == null || pName.isBlank()) {
        pName = "???";
      }
      g2.setColor(disabled ? COL_DISABLED_TEXT : UI.COL_TEXT);
      g2.drawString(pName, infoX, firstLineY);

      String className = getClassDisplayName(info.characterClass());
      g2.setColor(UI.COL_TEXT_DIM);
      g2.drawString(className, infoX, firstLineY + lineH);

      String timeText = timeLabel
          + PlayTimeTracker.formatPlayTime(info.totalPlayTimeSeconds());
      g2.setFont(ui.fonts.displayPlain(18F));
      g2.setColor(UI.COL_TEXT_DIM);
      int tw = (int) g2.getFontMetrics().getStringBounds(timeText, g2).getWidth();
      g2.drawString(timeText, rightPad - tw, firstLineY + lineH);
      g2.setFont(ui.fonts.displayPlain(22F));
    } else {
      g2.setColor(UI.COL_TEXT_DIM);
      g2.drawString(ui.t("ui.save.empty"), infoX, firstLineY);

      String timeText = timeLabel + "--";
      g2.setFont(ui.fonts.displayPlain(18F));
      g2.setColor(UI.COL_TEXT_DIM);
      int tw = (int) g2.getFontMetrics().getStringBounds(timeText, g2).getWidth();
      g2.drawString(timeText, rightPad - tw, firstLineY);
      g2.setFont(ui.fonts.displayPlain(22F));
    }
  }

  private String getClassDisplayName(int classIndex) {
    return switch (classIndex) {
      case 0 -> ui.t("ui.title.class.warrior");
      case 1 -> ui.t("ui.title.class.thief");
      case 2 -> ui.t("ui.title.class.wizard");
      default -> "???";
    };
  }

  // ============================================================
  // Confirmação de sobrescrita
  // ============================================================

  void drawOverwriteConfirm() {
    Graphics2D g2 = ui.g2;

    g2.setColor(COL_OVERLAY);
    g2.fillRect(0, 0, GamePanel.SCREEN_WIDTH, GamePanel.SCREEN_HEIGHT);

    int w = (int) (GamePanel.SCREEN_WIDTH * DIALOG_WIDTH_RATIO);
    int h = GamePanel.TILE_SIZE * DIALOG_HEIGHT_TILES;
    int x = (GamePanel.SCREEN_WIDTH - w) / 2;
    int y = (GamePanel.SCREEN_HEIGHT - h) / 2;
    ui.drawSubWindow(x, y, w, h);

    g2.setFont(ui.fonts.displayPlain(24F));
    int textY = y + GamePanel.TILE_SIZE + DIALOG_TEXT_Y_OFFSET;

    // Instead of hardcoding two lines, dynamically measure and potentially wrap or
    // fit the text across the wider box
    ui.drawTextShadowCentered(ui.t("ui.save.overwrite.line1"),
        textY, UI.COL_TEXT, Color.black, 2);
    ui.drawTextShadowCentered(ui.t("ui.save.overwrite.line2"),
        textY + DIALOG_SECOND_LINE_Y_OFFSET, UI.COL_TEXT, Color.black, 2);

    g2.setFont(ui.fonts.displayBold(32F));
    int btnY = textY + DIALOG_BUTTON_Y_OFFSET;
    ui.drawMenuItem(ui.t("ui.save.cancel"), btnY,
        ui.getCommandNum() == 0);
    ui.drawMenuItem(ui.t("ui.save.overwrite"), btnY + UI.MENU_LINE,
        ui.getCommandNum() == 1);
  }

  // ============================================================
  // Confirmação de exclusão
  // ============================================================

  void drawDeleteConfirm() {
    Graphics2D g2 = ui.g2;

    g2.setColor(COL_OVERLAY);
    g2.fillRect(0, 0, GamePanel.SCREEN_WIDTH, GamePanel.SCREEN_HEIGHT);

    int w = (int) (GamePanel.SCREEN_WIDTH * DIALOG_WIDTH_RATIO);
    int h = GamePanel.TILE_SIZE * DIALOG_HEIGHT_TILES;
    int x = (GamePanel.SCREEN_WIDTH - w) / 2;
    int y = (GamePanel.SCREEN_HEIGHT - h) / 2;
    ui.drawSubWindow(x, y, w, h);

    g2.setFont(ui.fonts.displayPlain(24F));
    int textY = y + GamePanel.TILE_SIZE + DIALOG_TEXT_Y_OFFSET;

    ui.drawTextShadowCentered(ui.t("ui.save.delete.line1"),
        textY, UI.COL_TEXT, Color.black, 2);
    ui.drawTextShadowCentered(ui.t("ui.save.delete.line2"),
        textY + DIALOG_SECOND_LINE_Y_OFFSET, UI.COL_TEXT, Color.black, 2);

    g2.setFont(ui.fonts.displayBold(32F));
    int btnY = textY + DIALOG_BUTTON_Y_OFFSET;
    ui.drawMenuItem(ui.t("ui.save.cancel"), btnY,
        ui.getCommandNum() == 0);
    ui.drawMenuItem(ui.t("ui.save.deleteConfirm"), btnY + UI.MENU_LINE,
        ui.getCommandNum() == 1);
  }

  // ============================================================
  // Input do nome do jogador
  // ============================================================

  void drawNameInput() {
    Graphics2D g2 = ui.g2;

    g2.setColor(UI.COL_BG);
    g2.fillRect(0, 0, GamePanel.SCREEN_WIDTH, GamePanel.SCREEN_HEIGHT);

    g2.setFont(ui.fonts.displayBold(48F));
    ui.drawTextShadowCentered(
        ui.t("ui.save.enterName"),
        GamePanel.TILE_SIZE * 3,
        UI.COL_TEXT, Color.black, 2);

    int fieldW = (int) (GamePanel.SCREEN_WIDTH * NAME_FIELD_WIDTH_RATIO);
    int fieldH = GamePanel.TILE_SIZE + NAME_FIELD_HEIGHT_EXTRA;
    int fieldX = (GamePanel.SCREEN_WIDTH - fieldW) / 2;
    int fieldY = GamePanel.SCREEN_HEIGHT / 2 - fieldH / 2;

    ui.drawSubWindow(fieldX, fieldY, fieldW, fieldH);

    String text = ui.getPlayerNameText();
    g2.setFont(ui.fonts.displayPlain(36F));
    g2.setColor(UI.COL_TEXT);

    int textWidth = (int) g2.getFontMetrics()
        .getStringBounds(text, g2).getWidth();
    int tx = fieldX + NAME_TEXT_X_OFFSET;
    int ty = fieldY + fieldH / 2 + NAME_TEXT_Y_OFFSET;
    g2.drawString(text, tx, ty);

    if ((System.currentTimeMillis() / CURSOR_BLINK_MS) % 2 == 0) {
      g2.setColor(UI.COL_ACCENT);
      g2.fillRect(tx + textWidth + CURSOR_X_OFFSET, ty - CURSOR_Y_OFFSET, CURSOR_WIDTH, CURSOR_HEIGHT);
    }

    ui.useCrispPixelText(g2);
    g2.setFont(ui.fonts.pixelPlain(16F));
    ui.drawTextShadowCentered(
        ui.t("ui.save.nameHint"),
        GamePanel.SCREEN_HEIGHT - GamePanel.TILE_SIZE,
        UI.COL_TEXT_DIM, Color.black, 2);
  }
}

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
    int cardW = (int) (GamePanel.SCREEN_WIDTH * 0.7);
    int cardH = GamePanel.TILE_SIZE * 2;
    int cardX = (GamePanel.SCREEN_WIDTH - cardW) / 2;
    int startY = (int) (GamePanel.TILE_SIZE * 2.8);
    int gap = 10;

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
    g2.fillRoundRect(x, y, w, h, 10, 10);

    Color borderColor = selected ? UI.COL_ACCENT : UI.COL_PANEL_BORDER;
    if (disabled) {
      borderColor = COL_DISABLED_BORDER;
    }
    g2.setColor(borderColor);
    g2.drawRoundRect(x, y, w, h, 10, 10);

    int textX = x + 16;
    int lineH = 24;
    int firstLineY = y + lineH + 6;

    g2.setFont(ui.fonts.displayBold(28F));
    g2.setColor(disabled ? COL_DISABLED_TEXT : UI.COL_ACCENT);
    g2.drawString(ui.t("ui.save.slotPrefix") + " " + slotNum,
        textX, firstLineY);

    int infoX = textX + 170;
    int rightPad = x + w - 16;

    drawSlotInfo(g2, info, infoX, firstLineY, lineH, rightPad, disabled);

    if (selected && !disabled) {
      int triX = x - 20;
      int triY = y + h / 2 - 10;
      int[] xs = { triX, triX + 14, triX };
      int[] ys = { triY, triY + 10, triY + 20 };
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

    int w = (int) (GamePanel.SCREEN_WIDTH * 0.85);
    int h = GamePanel.TILE_SIZE * 5;
    int x = (GamePanel.SCREEN_WIDTH - w) / 2;
    int y = (GamePanel.SCREEN_HEIGHT - h) / 2;
    ui.drawSubWindow(x, y, w, h);

    g2.setFont(ui.fonts.displayPlain(24F));
    int textY = y + GamePanel.TILE_SIZE + 10;

    // Instead of hardcoding two lines, dynamically measure and potentially wrap or
    // fit the text across the wider box
    ui.drawTextShadowCentered(ui.t("ui.save.overwrite.line1"),
        textY, UI.COL_TEXT, Color.black, 2);
    ui.drawTextShadowCentered(ui.t("ui.save.overwrite.line2"),
        textY + 30, UI.COL_TEXT, Color.black, 2);

    g2.setFont(ui.fonts.displayBold(32F));
    int btnY = textY + 90;
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

    int w = (int) (GamePanel.SCREEN_WIDTH * 0.85);
    int h = GamePanel.TILE_SIZE * 5;
    int x = (GamePanel.SCREEN_WIDTH - w) / 2;
    int y = (GamePanel.SCREEN_HEIGHT - h) / 2;
    ui.drawSubWindow(x, y, w, h);

    g2.setFont(ui.fonts.displayPlain(24F));
    int textY = y + GamePanel.TILE_SIZE + 10;

    ui.drawTextShadowCentered(ui.t("ui.save.delete.line1"),
        textY, UI.COL_TEXT, Color.black, 2);
    ui.drawTextShadowCentered(ui.t("ui.save.delete.line2"),
        textY + 30, UI.COL_TEXT, Color.black, 2);

    g2.setFont(ui.fonts.displayBold(32F));
    int btnY = textY + 90;
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

    int fieldW = (int) (GamePanel.SCREEN_WIDTH * 0.5);
    int fieldH = GamePanel.TILE_SIZE + 8;
    int fieldX = (GamePanel.SCREEN_WIDTH - fieldW) / 2;
    int fieldY = GamePanel.SCREEN_HEIGHT / 2 - fieldH / 2;

    ui.drawSubWindow(fieldX, fieldY, fieldW, fieldH);

    String text = ui.getPlayerNameText();
    g2.setFont(ui.fonts.displayPlain(36F));
    g2.setColor(UI.COL_TEXT);

    int textWidth = (int) g2.getFontMetrics()
        .getStringBounds(text, g2).getWidth();
    int tx = fieldX + 16;
    int ty = fieldY + fieldH / 2 + 12;
    g2.drawString(text, tx, ty);

    if ((System.currentTimeMillis() / 500) % 2 == 0) {
      g2.setColor(UI.COL_ACCENT);
      g2.fillRect(tx + textWidth + 2, ty - 24, 3, 28);
    }

    ui.useCrispPixelText(g2);
    g2.setFont(ui.fonts.pixelPlain(16F));
    ui.drawTextShadowCentered(
        ui.t("ui.save.nameHint"),
        GamePanel.SCREEN_HEIGHT - GamePanel.TILE_SIZE,
        UI.COL_TEXT_DIM, Color.black, 2);
  }
}

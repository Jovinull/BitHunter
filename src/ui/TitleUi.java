package ui;

import java.awt.Color;
import java.awt.Graphics2D;

import main.GamePanel;

class TitleUi {

  private final UI ui;

  TitleUi(UI ui) {
    this.ui = ui;
  }

  void drawTitleScreen() {
    Graphics2D g2 = ui.g2;
    var gp = ui.getGamePanel();

    int state = ui.getTitleScreenState();

    switch (state) {
      case 0 -> drawMainMenu(g2, gp);
      case 1 -> drawClassSelection(g2);
      case 2 -> ui.getSaveSlotUi().drawSlotSelection(ui.getSaveSlotMode());
      case 3 -> {
        ui.getSaveSlotUi().drawSlotSelection(ui.getSaveSlotMode());
        ui.getSaveSlotUi().drawOverwriteConfirm();
      }
      case 4 -> {
        ui.getSaveSlotUi().drawSlotSelection(ui.getSaveSlotMode());
        ui.getSaveSlotUi().drawDeleteConfirm();
      }
      case 5 -> ui.getSaveSlotUi().drawNameInput();
      case 6 -> {
        ui.getSaveSlotUi().drawSlotSelection(ui.getSaveSlotMode());
        ui.getStatsUi().drawStatsOverlay(ui.getSelectedSaveSlot() - 1);
      }
      default -> { /* noop */ }
    }
  }

  private void drawMainMenu(Graphics2D g2, GamePanel gp) {
    g2.setColor(UI.COL_BG);
    g2.fillRect(0, 0, GamePanel.SCREEN_WIDTH, GamePanel.SCREEN_HEIGHT);

    g2.setFont(ui.fonts.displayBold(112F));
    ui.drawTextShadowCentered(
        ui.t("ui.title.gameName"),
        GamePanel.TILE_SIZE * 3,
        UI.COL_TEXT,
        Color.black,
        3);

    int xImg = GamePanel.SCREEN_WIDTH / 2 - (GamePanel.TILE_SIZE * 2) / 2;
    int yImg = GamePanel.TILE_SIZE * 3 + GamePanel.TILE_SIZE * 2;
    g2.drawImage(
        gp.getPlayer().getDown1(),
        xImg,
        yImg,
        GamePanel.TILE_SIZE * 2,
        GamePanel.TILE_SIZE * 2,
        null);

    g2.setFont(ui.fonts.displayBold(48F));
    int yMenuStart = yImg + (int) (GamePanel.TILE_SIZE * 3.2);
    ui.drawMenuItem(ui.t("ui.title.newGame"), yMenuStart, ui.getCommandNum() == 0);
    ui.drawMenuItem(ui.t("ui.title.loadGame"), yMenuStart + UI.MENU_LINE, ui.getCommandNum() == 1);
    ui.drawMenuItem(ui.t("ui.title.host"), yMenuStart + UI.MENU_LINE * 2, ui.getCommandNum() == 2);
    ui.drawMenuItem(ui.t("ui.title.join"), yMenuStart + UI.MENU_LINE * 3, ui.getCommandNum() == 3);
    ui.drawMenuItem(ui.t("ui.title.exit"), yMenuStart + UI.MENU_LINE * 4, ui.getCommandNum() == 4);

    ui.useCrispPixelText(g2);
    g2.setFont(ui.fonts.pixelPlain(16F));
    String hint = ui.t("ui.title.hintNavigation");
    ui.drawTextShadowCentered(
        hint,
        GamePanel.SCREEN_HEIGHT - GamePanel.TILE_SIZE,
        UI.COL_TEXT_DIM,
        Color.black,
        2);
  }

  private void drawClassSelection(Graphics2D g2) {
    g2.setColor(UI.COL_BG);
    g2.fillRect(0, 0, GamePanel.SCREEN_WIDTH, GamePanel.SCREEN_HEIGHT);

    g2.setFont(ui.fonts.displayBold(36F));
    ui.drawSubWindowCenter(GamePanel.TILE_SIZE * 10, GamePanel.TILE_SIZE * 8);
    int cy = GamePanel.SCREEN_HEIGHT / 2 - GamePanel.TILE_SIZE;

    ui.drawTextShadowCentered(
        ui.t("ui.title.classSelection"),
        cy - UI.MENU_LINE,
        UI.COL_TEXT,
        Color.black,
        2);

    ui.drawMenuItem(ui.t("ui.title.class.warrior"), cy, ui.getCommandNum() == 0);
    ui.drawMenuItem(ui.t("ui.title.class.thief"), cy + UI.MENU_LINE, ui.getCommandNum() == 1);
    ui.drawMenuItem(ui.t("ui.title.class.wizard"), cy + UI.MENU_LINE * 2, ui.getCommandNum() == 2);
    ui.drawMenuItem(ui.t(UI.KEY_COMMON_BACK), cy + UI.MENU_LINE * 4, ui.getCommandNum() == 3);
  }
}

package ui;

import java.awt.Color;
import java.awt.Graphics2D;

import main.GamePanel;

class GameOverUi {

  private static final Color COL_OVERLAY = new Color(0, 0, 0, 140);

  private final UI ui;

  GameOverUi(UI ui) {
    this.ui = ui;
  }

  void drawGameOverScreen() {
    Graphics2D g2 = ui.g2;

    g2.setColor(COL_OVERLAY);
    g2.fillRect(0, 0, GamePanel.SCREEN_WIDTH, GamePanel.SCREEN_HEIGHT);

    g2.setFont(ui.fonts.displayBold(96F));
    String text = ui.t("ui.gameover.title");
    ui.drawTextShadowCentered(text, GamePanel.TILE_SIZE * 4, UI.COL_TEXT, Color.black, 4);

    g2.setFont(ui.fonts.displayBold(44F));
    int y = GamePanel.TILE_SIZE * 8;

    ui.drawMenuItem(ui.t("ui.gameover.retry"), y, ui.getCommandNum() == 0);
    ui.drawMenuItem(ui.t("ui.gameover.exit"), y + UI.MENU_LINE, ui.getCommandNum() == 1);
  }
}

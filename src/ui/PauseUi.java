package ui;

import java.awt.Color;
import java.awt.Graphics2D;

import main.GamePanel;

class PauseUi {

  private final UI ui;

  PauseUi(UI ui) {
    this.ui = ui;
  }

  void drawPauseScreen() {
    Graphics2D g2 = ui.g2;

    int w = (int) (GamePanel.SCREEN_WIDTH * 0.6);
    int h = GamePanel.TILE_SIZE * 4;
    int x = (GamePanel.SCREEN_WIDTH - w) / 2;
    int y = (GamePanel.SCREEN_HEIGHT - h) / 2;

    ui.drawSubWindow(x, y, w, h);

    g2.setFont(ui.fonts.displayPlain(80F));
    String text = ui.t("ui.pause.title");
    ui.drawTextShadowCentered(text, y + (int) (h * 0.62), UI.COL_TEXT, Color.black, 3);
  }
}

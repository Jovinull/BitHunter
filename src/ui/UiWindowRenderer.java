package ui;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;

import main.GamePanel;

/**
 * Renderiza janelas de UI no estilo pixelado SNES.
 */
final class UiWindowRenderer {

  private static final int OUTER_BORDER = 3;
  private static final int INNER_BORDER = 1;
  private static final int SHADOW_OFFSET = 1;

  void drawWindow(Graphics2D g2, int x, int y, int width, int height) {
    Graphics2D g = (Graphics2D) g2.create();

    drawDropShadow(g, x, y, width, height);
    fillGradientPanel(g, x, y, width, height);
    drawPixelBorders(g, x, y, width, height);

    g.dispose();
  }

  private void drawDropShadow(Graphics2D g, int x, int y, int width, int height) {
    g.setColor(new Color(0, 0, 0, 90));
    g.fillRect(x + SHADOW_OFFSET * 2, y + SHADOW_OFFSET * 2, width, height);
  }

  private void fillGradientPanel(Graphics2D g, int x, int y, int width, int height) {
    float gradientEndY = y + (float) Math.max(height, GamePanel.TILE_SIZE);
    var gradient = new GradientPaint(
        x, y, UiTheme.PANEL_MID,
        x, gradientEndY, UiTheme.PANEL_DARK);
    g.setPaint(gradient);
    g.fillRect(x, y, width, height);
  }

  private void drawPixelBorders(Graphics2D g, int x, int y, int width, int height) {
    g.setColor(UiTheme.BORDER_LIGHT);
    for (int i = 0; i < OUTER_BORDER; i++) {
      g.drawRect(x + i, y + i, width - 1 - i * 2, height - 1 - i * 2);
    }

    g.setColor(UiTheme.BORDER_SHADOW);
    for (int i = 0; i < INNER_BORDER; i++) {
      int borderOffset = OUTER_BORDER + i;
      g.drawRect(
          x + borderOffset,
          y + borderOffset,
          width - 1 - borderOffset * 2,
          height - 1 - borderOffset * 2);
    }
  }
}

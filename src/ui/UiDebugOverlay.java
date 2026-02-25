package ui;

import java.awt.Color;
import java.awt.Graphics2D;

/**
 * Overlay de debug renderizado dentro da camada de UI.
 */
final class UiDebugOverlay {

  private static final int START_X = 10;
  private static final int START_Y = 400;
  private static final int LINE_HEIGHT = 20;

  private final UI ui;

  UiDebugOverlay(UI ui) {
    this.ui = ui;
  }

  void draw(long drawStartNanos) {
    var gp = ui.getGamePanel();
    var player = gp.getPlayer();
    Graphics2D g2 = ui.g2;

    long drawTimeNanos = System.nanoTime() - drawStartNanos;

    g2.setFont(ui.getDebugFont());
    g2.setColor(Color.white);

    int x = START_X;
    int y = START_Y;

    g2.drawString("WorldX " + player.getWorldX(), x, y);
    y += LINE_HEIGHT;
    g2.drawString("WorldY " + player.getWorldY(), x, y);
    y += LINE_HEIGHT;
    g2.drawString("Col " + player.getCol(), x, y);
    y += LINE_HEIGHT;
    g2.drawString("Row " + player.getRow(), x, y);
    y += LINE_HEIGHT;
    g2.drawString("Draw Time: " + drawTimeNanos, x, y);
    y += LINE_HEIGHT;
    g2.drawString("God Mode (NoClip): " + gp.getKeyH().isGodModeOn(), x, y);

    var weatherManager = gp.getEnvironmentManager().getWeatherManager();
    if (weatherManager != null) {
      y += LINE_HEIGHT;
      g2.drawString("Weather: " + weatherManager.getCurrentType().name(), x, y);
      y += LINE_HEIGHT;
      g2.drawString("W.Duration: " + weatherManager.getDurationRemaining() / 60 + "s", x, y);
    }
  }
}

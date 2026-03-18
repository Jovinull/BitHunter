package environment;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.image.BufferedImage;

import main.GamePanel;

public class Lighting {

  private static final Color[] LIGHT_GRADIENT_COLORS = {
      new Color(0, 0, 0.1f, 0.10f),
      new Color(0, 0, 0.1f, 0.42f),
      new Color(0, 0, 0.1f, 0.52f),
      new Color(0, 0, 0.1f, 0.61f),
      new Color(0, 0, 0.1f, 0.69f),
      new Color(0, 0, 0.1f, 0.76f),
      new Color(0, 0, 0.1f, 0.82f),
      new Color(0, 0, 0.1f, 0.87f),
      new Color(0, 0, 0.1f, 0.91f),
      new Color(0, 0, 0.1f, 0.92f),
      new Color(0, 0, 0.1f, 0.93f),
      new Color(0, 0, 0.1f, 0.94f)
  };
  private static final float[] LIGHT_GRADIENT_FRACTIONS = {
      0f,
      0.4f,
      0.5f,
      0.6f,
      0.65f,
      0.7f,
      0.75f,
      0.8f,
      0.85f,
      0.9f,
      0.95f,
      1f
  };

  private final GamePanel gp;
  private BufferedImage darknessFilter;

  // Estados internos do ciclo
  private int dayCounter;
  private float filterAlpha = 0f;

  private enum DayPhase {
    DAY, DUSK, NIGHT, DAWN
  }

  private DayPhase dayPhase = DayPhase.DAY;

  /**
   * Cached derived font for the debug day-phase overlay (avoids per-frame
   * allocation).
   */
  private Font debugPhaseFont;

  private int lastLightCenterX = -1;
  private int lastLightCenterY = -1;

  public Lighting(GamePanel gp) {
    this.gp = gp;
    setLightSource();
  }

  private void setLightSource() {
    if (darknessFilter == null) {
      darknessFilter = new BufferedImage(
          GamePanel.SCREEN_WIDTH,
          GamePanel.SCREEN_HEIGHT,
          BufferedImage.TYPE_INT_ARGB);
    }

    Graphics2D g2 = (Graphics2D) darknessFilter.getGraphics();

    g2.setComposite(AlphaComposite.Clear);
    g2.fillRect(0, 0, GamePanel.SCREEN_WIDTH, GamePanel.SCREEN_HEIGHT);
    g2.setComposite(AlphaComposite.SrcOver);

    var player = gp.getPlayer();

    int centerX = player.getWorldX() - gp.getCameraX() + GamePanel.TILE_SIZE / 2;
    int centerY = player.getWorldY() - gp.getCameraY() + GamePanel.TILE_SIZE / 2;

    if (player.getCurrentLight() == null) {
      g2.setColor(new Color(0, 0, 0.1f, 0.97f));
    } else {
      int radius = player.getCurrentLight().getLightRadius();
      RadialGradientPaint gPaint = new RadialGradientPaint(
          centerX, centerY, radius, LIGHT_GRADIENT_FRACTIONS, LIGHT_GRADIENT_COLORS);
      g2.setPaint(gPaint);
    }

    g2.fillRect(0, 0, GamePanel.SCREEN_WIDTH, GamePanel.SCREEN_HEIGHT);
    g2.dispose();

    lastLightCenterX = centerX;
    lastLightCenterY = centerY;
  }

  public void resetDay() {
    dayPhase = DayPhase.DAY;
    filterAlpha = 0f;
    dayCounter = 0;
  }

  public void update() {
    var player = gp.getPlayer();

    int effectiveCenterX = player.getWorldX() - gp.getCameraX() + GamePanel.TILE_SIZE / 2;
    int effectiveCenterY = player.getWorldY() - gp.getCameraY() + GamePanel.TILE_SIZE / 2;
    boolean centerMoved = effectiveCenterX != lastLightCenterX || effectiveCenterY != lastLightCenterY;

    if (player.isLightUpdated() || centerMoved) {
      setLightSource();
      player.setLightUpdated(false);
    }

    switch (dayPhase) {
      case DAY -> {
        dayCounter++;
        if (dayCounter > 18000) {
          dayPhase = DayPhase.DUSK;
          dayCounter = 0;
        }
      }
      case DUSK -> {
        filterAlpha += 1f / 1800f;
        if (filterAlpha > 1f) {
          filterAlpha = 1f;
          dayPhase = DayPhase.NIGHT;
        }
      }
      case NIGHT -> {
        dayCounter++;
        if (dayCounter > 10800) {
          dayPhase = DayPhase.DAWN;
          dayCounter = 0;
        }
      }
      case DAWN -> {
        filterAlpha -= 1f / 1800f;
        if (filterAlpha < 0f) {
          filterAlpha = 0f;
          dayPhase = DayPhase.DAY;
        }
        dayCounter++;
      }
    }
  }

  public void draw(Graphics2D g2) {
    int currentArea = gp.getCurrentArea();

    if (currentArea == GamePanel.OUTSIDE) {
      g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, filterAlpha));
    }

    if (currentArea == GamePanel.OUTSIDE || currentArea == GamePanel.DUNGEON) {
      g2.drawImage(darknessFilter, 0, 0, null);
    }

    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));

    // DEBUG
    var keyH = gp.getKeyH();
    if (keyH != null && keyH.isShowDebugText()) {
      String situation;
      switch (dayPhase) {
        case DAY -> situation = "Day";
        case DUSK -> situation = "Dusk";
        case NIGHT -> situation = "Night";
        case DAWN -> situation = "Dawn";
        default -> situation = "";
      }
      g2.setColor(Color.white);
      if (debugPhaseFont == null) {
        debugPhaseFont = g2.getFont().deriveFont(50f);
      }
      g2.setFont(debugPhaseFont);
      g2.drawString(situation, 800, 500);
    }
  }

  public float getFilterAlpha() {
    return filterAlpha;
  }

  public void setFilterAlpha(float alpha) {
    if (alpha < 0f) {
      alpha = 0f;
    }
    if (alpha > 1f) {
      alpha = 1f;
    }
    this.filterAlpha = alpha;
  }

  public int getDayCounter() {
    return dayCounter;
  }

  public void setDayCounter(int value) {
    this.dayCounter = Math.max(0, value);
  }

  public void setDayStateToDay() {
    this.dayPhase = DayPhase.DAY;
  }

  public String getDayPhaseName() {
    return dayPhase.name();
  }

  public String getFormattedTime() {
    int totalFrames = 0;
    switch (dayPhase) {
      case DAY -> totalFrames = dayCounter;
      case DUSK -> totalFrames = 18000 + (int) (filterAlpha * 1800f);
      case NIGHT -> totalFrames = 19800 + dayCounter;
      case DAWN -> totalFrames = 30600 + dayCounter;
    }

    // 32400 frames = 24 hours = 1440 minutes
    // 1 frame = 1440 / 32400 minutes = 1 / 22.5 minutes
    int totalMinutes = (int) (totalFrames / 22.5f);

    // Assume DAY starts at 06:00
    totalMinutes += 360;

    int hour = (totalMinutes / 60) % 24;
    int minute = totalMinutes % 60;

    return String.format("%02d:%02d", hour, minute);
  }
}

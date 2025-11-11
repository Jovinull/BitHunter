package environment;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.image.BufferedImage;

import main.GamePanel;

public class Lighting {

  private final GamePanel gp;
  private BufferedImage darknessFilter;

  // Estados internos do ciclo
  private int dayCounter;
  private float filterAlpha = 0f;

  private enum DayPhase {
    DAY, DUSK, NIGHT, DAWN
  }

  private DayPhase dayPhase = DayPhase.DAY;

  public Lighting(GamePanel gp) {
    this.gp = gp;
    setLightSource();
  }

  public void setLightSource() {
    darknessFilter = new BufferedImage(gp.screenWidth, gp.screenHeight, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = (Graphics2D) darknessFilter.getGraphics();

    if (gp.player.getCurrentLight() == null) {
      g2.setColor(new Color(0, 0, 0.1f, 0.97f));
    } else {
      int centerX = gp.player.screenX + (gp.tileSize) / 2;
      int centerY = gp.player.screenY + (gp.tileSize) / 2;

      Color[] color = new Color[12];
      float[] fraction = new float[12];

      color[0] = new Color(0, 0, 0.1f, 0.10f);
      color[1] = new Color(0, 0, 0.1f, 0.42f);
      color[2] = new Color(0, 0, 0.1f, 0.52f);
      color[3] = new Color(0, 0, 0.1f, 0.61f);
      color[4] = new Color(0, 0, 0.1f, 0.69f);
      color[5] = new Color(0, 0, 0.1f, 0.76f);
      color[6] = new Color(0, 0, 0.1f, 0.82f);
      color[7] = new Color(0, 0, 0.1f, 0.87f);
      color[8] = new Color(0, 0, 0.1f, 0.91f);
      color[9] = new Color(0, 0, 0.1f, 0.92f);
      color[10] = new Color(0, 0, 0.1f, 0.93f);
      color[11] = new Color(0, 0, 0.1f, 0.94f);

      fraction[0] = 0f;
      fraction[1] = 0.4f;
      fraction[2] = 0.5f;
      fraction[3] = 0.6f;
      fraction[4] = 0.65f;
      fraction[5] = 0.7f;
      fraction[6] = 0.75f;
      fraction[7] = 0.8f;
      fraction[8] = 0.85f;
      fraction[9] = 0.9f;
      fraction[10] = 0.95f;
      fraction[11] = 1f;

      int radius = gp.player.getCurrentLight().getLightRadius();
      RadialGradientPaint gPaint = new RadialGradientPaint(centerX, centerY, radius, fraction, color);
      g2.setPaint(gPaint);
    }

    g2.fillRect(0, 0, gp.screenWidth, gp.screenHeight);
    g2.dispose();
  }

  public void resetDay() {
    dayPhase = DayPhase.DAY;
    filterAlpha = 0f;
    dayCounter = 0;
  }

  public void update() {
    // Atualiza quando a iluminação do player muda (usar accessors)
    if (gp.player.isLightUpdated()) {
      setLightSource();
      gp.player.setLightUpdated(false);
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
    if (gp.currentArea == gp.outside) {
      g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, filterAlpha));
    }

    if (gp.currentArea == gp.outside || gp.currentArea == gp.dungeon) {
      g2.drawImage(darknessFilter, 0, 0, null);
    }

    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));

    // DEBUG
    if (gp.keyH != null && gp.keyH.isShowDebugText()) {
      String situation;
      switch (dayPhase) {
        case DAY -> situation = "Day";
        case DUSK -> situation = "Dusk";
        case NIGHT -> situation = "Night";
        case DAWN -> situation = "Dawn";
        default -> situation = "";
      }
      g2.setColor(Color.white);
      g2.setFont(g2.getFont().deriveFont(50f));
      g2.drawString(situation, 800, 500);
    }
  }

  public float getFilterAlpha() {
    return filterAlpha;
  }

  public void setFilterAlpha(float alpha) {
    if (alpha < 0f)
      alpha = 0f;
    if (alpha > 1f)
      alpha = 1f;
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
}

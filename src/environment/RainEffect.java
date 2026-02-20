package environment;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Random;

/**
 * Efeito de chuva renderizado por partículas (linhas inclinadas).
 * Parametrizável para representar tanto garoa ({@link WeatherType#DRIZZLE})
 * quanto chuva normal ({@link WeatherType#RAIN}).
 *
 * <p>Cada partícula é uma linha curta com velocidade vertical variável
 * e leve componente horizontal (vento). Opcionalmente desenha "respingos"
 * no chão como pequenos círculos.</p>
 */
public class RainEffect implements WeatherEffect {

  private static final Color RAIN_COLOR = new Color(170, 190, 220);
  private static final Color SPLASH_COLOR = new Color(170, 190, 220, 120);
  private static final float RAIN_ALPHA = 0.6f;
  private static final float SPLASH_ALPHA = 0.4f;

  private final int count;
  private final float overlayDarkness;
  private final float windX;
  private final int lineLength;
  private final boolean showSplash;
  private final int screenWidth;
  private final int screenHeight;

  private final float[] px;
  private final float[] py;
  private final float[] pSpeed;

  /**
   * @param count           quantidade de partículas
   * @param overlayDarkness alpha do overlay escuro (0.0–1.0)
   * @param speedMin        velocidade mínima de queda (pixels/frame)
   * @param speedMax        velocidade máxima de queda (pixels/frame)
   * @param lineLength      comprimento da linha de cada gota (pixels)
   * @param windX           deslocamento horizontal por frame (simula vento)
   * @param showSplash      se deve desenhar respingos no chão
   * @param screenWidth     largura da tela
   * @param screenHeight    altura da tela
   */
  public RainEffect(int count, float overlayDarkness,
                    float speedMin, float speedMax, int lineLength,
                    float windX, boolean showSplash,
                    int screenWidth, int screenHeight) {
    this.count = count;
    this.overlayDarkness = overlayDarkness;
    this.windX = windX;
    this.lineLength = lineLength;
    this.showSplash = showSplash;
    this.screenWidth = screenWidth;
    this.screenHeight = screenHeight;

    this.px = new float[count];
    this.py = new float[count];
    this.pSpeed = new float[count];

    Random rng = new Random();
    for (int i = 0; i < count; i++) {
      px[i] = rng.nextFloat() * screenWidth;
      py[i] = rng.nextFloat() * screenHeight;
      pSpeed[i] = speedMin + rng.nextFloat() * (speedMax - speedMin);
    }
  }

  @Override
  public void update() {
    for (int i = 0; i < count; i++) {
      py[i] += pSpeed[i];
      px[i] += windX;

      // Recicla partícula que saiu da tela
      if (py[i] > screenHeight) {
        py[i] = -(lineLength + (int) (pSpeed[i] * 4));
        px[i] = (float) (Math.random() * (screenWidth + 100)) - 50;
      }
      if (px[i] > screenWidth + 50) {
        px[i] = -30;
      }
    }
  }

  @Override
  public void render(Graphics2D g2, int screenWidth, int screenHeight, float intensity) {
    renderDarknessOverlay(g2, screenWidth, screenHeight, intensity);
    renderRaindrops(g2, screenWidth, screenHeight, intensity);
    if (showSplash) {
      renderSplashes(g2, screenHeight, intensity);
    }
    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
  }

  private void renderDarknessOverlay(Graphics2D g2, int sw, int sh, float intensity) {
    if (overlayDarkness <= 0) return;

    float alpha = clamp(overlayDarkness * intensity);
    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
    g2.setColor(Color.BLACK);
    g2.fillRect(0, 0, sw, sh);
  }

  private void renderRaindrops(Graphics2D g2, int sw, int sh, float intensity) {
    float alpha = clamp(RAIN_ALPHA * intensity);
    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
    g2.setColor(RAIN_COLOR);

    int windOffset = (int) (windX * 2);
    for (int i = 0; i < count; i++) {
      int x = (int) px[i];
      int y = (int) py[i];
      if (x < -20 || x > sw + 20 || y < -lineLength || y > sh) continue;

      g2.drawLine(x, y, x + windOffset, y + lineLength);
    }
  }

  private void renderSplashes(Graphics2D g2, int sh, float intensity) {
    float alpha = clamp(SPLASH_ALPHA * intensity);
    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
    g2.setColor(SPLASH_COLOR);

    for (int i = 0; i < count; i++) {
      if (py[i] >= sh - 30 && py[i] <= sh) {
        int sx = (int) px[i];
        int splashY = sh - 5 - (i % 10);
        g2.fillOval(sx - 1, splashY, 3, 2);
      }
    }
  }

  private static float clamp(float value) {
    return Math.max(0f, Math.min(1f, value));
  }
}

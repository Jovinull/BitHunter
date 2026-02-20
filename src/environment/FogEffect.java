package environment;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Random;

/**
 * Efeito de neblina renderizado por camadas translúcidas.
 * Combina um overlay uniforme com "manchas" ovais flutuantes
 * que se deslocam lentamente, criando textura sem alterar tiles.
 */
public class FogEffect implements WeatherEffect {

  private static final int PATCH_COUNT = 8;
  private static final Color FOG_COLOR = new Color(180, 185, 195);
  private static final float BASE_LAYER_RATIO = 0.5f;
  private static final float PATCH_ALPHA_SCALE = 0.15f;
  private static final int PATCH_LAYERS = 3;

  private final float maxAlpha;
  private final int screenWidth;

  private final float[] patchX;
  private final float[] patchY;
  private final float[] patchSpeedX;
  private final int[] patchWidth;
  private final int[] patchHeight;

  /**
   * @param maxAlpha     alpha máximo da neblina (0.0–1.0)
   * @param screenWidth  largura da tela
   * @param screenHeight altura da tela
   */
  public FogEffect(float maxAlpha, int screenWidth, int screenHeight) {
    this.maxAlpha = maxAlpha;
    this.screenWidth = screenWidth;

    this.patchX = new float[PATCH_COUNT];
    this.patchY = new float[PATCH_COUNT];
    this.patchSpeedX = new float[PATCH_COUNT];
    this.patchWidth = new int[PATCH_COUNT];
    this.patchHeight = new int[PATCH_COUNT];

    Random rng = new Random();
    for (int i = 0; i < PATCH_COUNT; i++) {
      patchX[i] = rng.nextFloat() * screenWidth;
      patchY[i] = rng.nextFloat() * screenHeight;
      patchSpeedX[i] = 0.1f + rng.nextFloat() * 0.4f;
      if (rng.nextBoolean()) patchSpeedX[i] *= -1;
      patchWidth[i] = 200 + rng.nextInt(400);
      patchHeight[i] = 100 + rng.nextInt(200);
    }
  }

  @Override
  public void update() {
    for (int i = 0; i < PATCH_COUNT; i++) {
      patchX[i] += patchSpeedX[i];

      // Reaparece do lado oposto ao sair da tela
      if (patchX[i] > screenWidth + patchWidth[i]) {
        patchX[i] = -patchWidth[i];
      } else if (patchX[i] < -patchWidth[i]) {
        patchX[i] = screenWidth + patchWidth[i];
      }
    }
  }

  @Override
  public void render(Graphics2D g2, int screenWidth, int screenHeight, float intensity) {
    float baseAlpha = maxAlpha * intensity;

    // Camada base uniforme
    float uniformAlpha = clamp(baseAlpha * BASE_LAYER_RATIO);
    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, uniformAlpha));
    g2.setColor(FOG_COLOR);
    g2.fillRect(0, 0, screenWidth, screenHeight);

    // Manchas flutuantes para dar textura e movimento
    for (int i = 0; i < PATCH_COUNT; i++) {
      float pAlpha = clamp(baseAlpha * PATCH_ALPHA_SCALE * ((i % PATCH_LAYERS + 1f) / PATCH_LAYERS));
      g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, pAlpha));
      g2.fillOval(
          (int) (patchX[i] - patchWidth[i] / 2),
          (int) (patchY[i] - patchHeight[i] / 2),
          patchWidth[i],
          patchHeight[i]);
    }

    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
  }

  private static float clamp(float value) {
    return Math.max(0f, Math.min(1f, value));
  }
}

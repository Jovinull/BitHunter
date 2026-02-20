package environment;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;

/**
 * Efeito de céu nublado — apenas escurecimento sutil do cenário.
 * Não utiliza partículas; aplica um overlay escuro com alpha configurável.
 */
public class OvercastEffect implements WeatherEffect {

  private static final Color OVERCAST_COLOR = new Color(20, 25, 40);

  private final float darkness;

  /**
   * @param darkness alpha máximo do overlay (0.0–1.0)
   */
  public OvercastEffect(float darkness) {
    this.darkness = darkness;
  }

  @Override
  public void update() {
    // Nublado é estático — sem partículas para atualizar
  }

  @Override
  public void render(Graphics2D g2, int screenWidth, int screenHeight, float intensity) {
    float alpha = clamp(darkness * intensity);
    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
    g2.setColor(OVERCAST_COLOR);
    g2.fillRect(0, 0, screenWidth, screenHeight);
    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
  }

  private static float clamp(float value) {
    return Math.max(0f, Math.min(1f, value));
  }
}

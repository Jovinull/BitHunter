package environment;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Random;

/**
 * Efeito de trovoada — compõe chuva intensa com flashes de relâmpago.
 * Reutiliza {@link RainEffect} para as partículas de chuva pesada
 * e adiciona flashes brancos esporádicos sobre toda a tela.
 *
 * <p>O áudio de trovão é disparado via callback opcional:
 * se um índice de SFX estiver configurado, o callback é invocado
 * a cada flash; caso contrário, o efeito é apenas visual (fallback silencioso).</p>
 */
public class StormEffect implements WeatherEffect {

  private static final int MIN_LIGHTNING_INTERVAL = 120;
  private static final int MAX_LIGHTNING_INTERVAL = 480;
  private static final int LIGHTNING_FLASH_FRAMES = 8;
  private static final float LIGHTNING_PEAK_ALPHA = 0.8f;

  private final RainEffect rain;
  private final Random random = new Random();
  private final Runnable thunderCallback;

  private int lightningCooldown;
  private int lightningFramesLeft;
  private float lightningAlpha;

  /**
   * @param particleCount   quantidade de partículas de chuva
   * @param overlayDarkness alpha do overlay escuro
   * @param thunderCallback callback para SFX de trovão (pode ser null)
   * @param screenWidth     largura da tela
   * @param screenHeight    altura da tela
   */
  public StormEffect(int particleCount, float overlayDarkness,
                     Runnable thunderCallback,
                     int screenWidth, int screenHeight) {
    this.rain = new RainEffect(
        particleCount, overlayDarkness,
        8f, 14f, 14, 2.0f, true,
        screenWidth, screenHeight);
    this.thunderCallback = thunderCallback;
    this.lightningCooldown = rollCooldown();
  }

  @Override
  public void update() {
    rain.update();
    updateLightning();
  }

  @Override
  public void render(Graphics2D g2, int screenWidth, int screenHeight, float intensity) {
    rain.render(g2, screenWidth, screenHeight, intensity);

    // Flash de relâmpago — sobre a chuva para simular iluminação momentânea
    if (lightningAlpha > 0) {
      float alpha = clamp(lightningAlpha * intensity);
      g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
      g2.setColor(Color.WHITE);
      g2.fillRect(0, 0, screenWidth, screenHeight);
      g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
    }
  }

  private void updateLightning() {
    if (lightningFramesLeft > 0) {
      lightningFramesLeft--;
      // Decaimento rápido: pico forte que enfraquece linearmente
      lightningAlpha = LIGHTNING_PEAK_ALPHA * ((float) lightningFramesLeft / LIGHTNING_FLASH_FRAMES);
    } else {
      lightningAlpha = 0;
      lightningCooldown--;
      if (lightningCooldown <= 0) {
        lightningFramesLeft = LIGHTNING_FLASH_FRAMES;
        lightningAlpha = LIGHTNING_PEAK_ALPHA;
        lightningCooldown = rollCooldown();

        if (thunderCallback != null) {
          thunderCallback.run();
        }
      }
    }
  }

  private int rollCooldown() {
    return MIN_LIGHTNING_INTERVAL
        + random.nextInt(MAX_LIGHTNING_INTERVAL - MIN_LIGHTNING_INTERVAL + 1);
  }

  private static float clamp(float value) {
    return Math.max(0f, Math.min(1f, value));
  }
}

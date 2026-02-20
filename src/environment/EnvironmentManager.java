package environment;

import java.awt.Graphics2D;

import main.GamePanel;

public class EnvironmentManager {

  private final GamePanel gp;
  private Lighting lighting;
  private WeatherManager weatherManager;

  public EnvironmentManager(GamePanel gp) {
    this.gp = gp;
  }

  public void setup() {
    lighting = new Lighting(gp);
    weatherManager = new WeatherManager(gp, lighting);
  }

  public void update() {
    if (lighting != null) {
      lighting.update();
    }
    if (weatherManager != null) {
      weatherManager.update();
    }
  }

  public void draw(Graphics2D g2) {
    // Iluminação (dia/noite) — desenhada primeiro
    if (lighting != null) {
      lighting.draw(g2);
    }
    // Clima (overlay sobre a iluminação para manter visibilidade dos efeitos)
    if (weatherManager != null) {
      weatherManager.draw(g2);
    }
  }

  public Lighting getLighting() {
    return lighting;
  }

  public WeatherManager getWeatherManager() {
    return weatherManager;
  }
}

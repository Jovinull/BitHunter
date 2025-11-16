package environment;

import java.awt.Graphics2D;

import main.GamePanel;

public class EnvironmentManager {

  private final GamePanel gp;
  private Lighting lighting;

  public EnvironmentManager(GamePanel gp) {
    this.gp = gp;
  }

  public void setup() {
    lighting = new Lighting(gp);
  }

  public void update() {
    if (lighting != null) {
      lighting.update();
    }
  }

  public void draw(Graphics2D g2) {
    if (lighting != null) {
      lighting.draw(g2);
    }
  }

  public Lighting getLighting() {
    return lighting;
  }
}

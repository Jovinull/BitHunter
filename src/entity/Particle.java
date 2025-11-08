package entity;

import java.awt.Color;
import java.awt.Graphics2D;

import main.GamePanel;

public class Particle extends Entity {

  private final Color color;
  private final int size;
  private int xd;
  private int yd;

  public Particle(GamePanel gp, Entity generator, Color color, int size, int speed, int maxLife, int xd, int yd) {
    super(gp);

    this.color = color;
    this.size = size;
    this.xd = xd;
    this.yd = yd;

    setSpeed(speed);
    setMaxLife(maxLife);
    setLife(maxLife);

    int offset = (gp.tileSize / 2) - (size / 2);
    setWorldX(generator.getWorldX() + offset);
    setWorldY(generator.getWorldY() + offset);
  }

  @Override
  public void update() {

    setLife(getLife() - 1);

    if (getLife() < getMaxLife() / 3) {
      yd++;
    }

    setWorldX(getWorldX() + xd * getSpeed());
    setWorldY(getWorldY() + yd * getSpeed());

    if (getLife() == 0) {
      setAlive(false);
    }
  }

  @Override
  public void draw(Graphics2D g2) {
    int screenX = getScreenX();
    int screenY = getScreenY();

    g2.setColor(color);
    g2.fillRect(screenX, screenY, size, size);
  }
}

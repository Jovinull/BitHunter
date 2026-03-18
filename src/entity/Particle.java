package entity;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;

import main.GamePanel;

public final class Particle extends Entity {

  private static final int LOW_LIFE_DIVISOR = 3;

  private final Color color;
  private final int size;
  private int xd;
  private int yd;

  public Particle(
      GamePanel gp,
      Entity generator,
      Color color,
      int size,
      int speed,
      int maxLife,
      Point velocity) {
    super(gp);

    this.color = color;
    this.size = size;
    this.xd = velocity.x;
    this.yd = velocity.y;

    setSpeed(speed);
    setMaxLife(maxLife);
    setLife(maxLife);

    int offset = (GamePanel.TILE_SIZE / 2) - (size / 2);
    setWorldX(generator.getWorldX() + offset);
    setWorldY(generator.getWorldY() + offset);
  }

  @Override
  public void update() {
    setLife(getLife() - 1);

    if (getLife() < getMaxLife() / LOW_LIFE_DIVISOR) {
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

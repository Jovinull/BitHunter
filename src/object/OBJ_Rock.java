package object;

import java.awt.Color;

import entity.Entity;
import entity.Projectile;
import main.GamePanel;

public class OBJ_Rock extends Projectile {

  public static final String objName = "Pedra";

  public OBJ_Rock(GamePanel gp) {
    super(gp);

    setName(objName);
    setSpeed(8);
    setMaxLife(80);
    setLife(getMaxLife());
    setAttack(2);
    setUseCost(1);
    setAlive(false);

    loadSprites();
  }

  private void loadSprites() {
    // As sprites da pedra usam a mesma imagem para todas as direções
    up1 = setup("/projectile/rock_down_1", gp.tileSize, gp.tileSize);
    up2 = setup("/projectile/rock_down_1", gp.tileSize, gp.tileSize);
    down1 = setup("/projectile/rock_down_1", gp.tileSize, gp.tileSize);
    down2 = setup("/projectile/rock_down_1", gp.tileSize, gp.tileSize);
    left1 = setup("/projectile/rock_down_1", gp.tileSize, gp.tileSize);
    left2 = setup("/projectile/rock_down_1", gp.tileSize, gp.tileSize);
    right1 = setup("/projectile/rock_down_1", gp.tileSize, gp.tileSize);
    right2 = setup("/projectile/rock_down_1", gp.tileSize, gp.tileSize);
  }

  @Override
  public boolean haveResource(Entity user) {
    return user.getAmmo() >= getUseCost();
  }

  @Override
  public void subtractResource(Entity user) {
    user.setAmmo(user.getAmmo() - getUseCost());
  }

  @Override
  public Color getParticleColor() {
    return new Color(40, 50, 0);
  }

  @Override
  public int getParticleSize() {
    return 10;
  }

  @Override
  public int getParticleSpeed() {
    return 1;
  }

  @Override
  public int getParticleMaxLife() {
    return 20;
  }
}

package object;

import java.awt.Color;

import entity.Entity;
import entity.Projectile;
import main.GamePanel;

public class OBJ_Fireball extends Projectile {

  public static final String objName = "Bola de Fogo";

  public OBJ_Fireball(GamePanel gp) {
    super(gp);

    setName(objName);
    setSpeed(5);
    setMaxLife(80);
    setLife(getMaxLife());
    setAttack(2);
    setKnockBackPower(5);
    setUseCost(1);
    setAlive(false);

    loadSprites();
  }

  private void loadSprites() {
    up1 = setup("/projectile/fireball_up_1", gp.tileSize, gp.tileSize);
    up2 = setup("/projectile/fireball_up_2", gp.tileSize, gp.tileSize);
    down1 = setup("/projectile/fireball_down_1", gp.tileSize, gp.tileSize);
    down2 = setup("/projectile/fireball_down_2", gp.tileSize, gp.tileSize);
    left1 = setup("/projectile/fireball_left_1", gp.tileSize, gp.tileSize);
    left2 = setup("/projectile/fireball_left_2", gp.tileSize, gp.tileSize);
    right1 = setup("/projectile/fireball_right_1", gp.tileSize, gp.tileSize);
    right2 = setup("/projectile/fireball_right_2", gp.tileSize, gp.tileSize);
  }

  @Override
  public boolean haveResource(Entity user) {
    return user.getMana() >= getUseCost();
  }

  @Override
  public void subtractResource(Entity user) {
    user.setMana(user.getMana() - getUseCost());
  }

  @Override
  public Color getParticleColor() {
    return new Color(240, 50, 0);
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

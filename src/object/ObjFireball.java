package object;

import java.awt.Color;

import entity.Entity;
import entity.Projectile;
import main.GamePanel;

public class ObjFireball extends Projectile {

  public static final String OBJ_NAME = "Bola de Fogo";

  public ObjFireball(GamePanel gp) {
    super(gp);

    setName(OBJ_NAME);
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
    up1 = setup("/projectile/fireball_up_1", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);
    up2 = setup("/projectile/fireball_up_2", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);
    down1 = setup("/projectile/fireball_down_1", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);
    down2 = setup("/projectile/fireball_down_2", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);
    left1 = setup("/projectile/fireball_left_1", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);
    left2 = setup("/projectile/fireball_left_2", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);
    right1 = setup("/projectile/fireball_right_1", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);
    right2 = setup("/projectile/fireball_right_2", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);
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

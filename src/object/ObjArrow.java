package object;

import java.awt.Color;

import entity.Entity;
import entity.Projectile;
import main.GamePanel;

public final class ObjArrow extends Projectile {

  public static final String OBJ_ID = "item.arrow";

  private static final Color PARTICLE_COLOR = new Color(139, 90, 43);

  public ObjArrow(GamePanel gp) {
    super(gp);

    setInternalId(OBJ_ID);
    setTranslationKey(OBJ_ID);

    setSpeed(6);
    setMaxLife(80);
    setLife(getMaxLife());
    setAttack(2);
    setKnockBackPower(2);
    setUseCost(0);
    setAlive(false);

    loadSprites();
  }

  private void loadSprites() {
    up1 = setup("/projectile/arrow_up_1", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);
    up2 = setup("/projectile/arrow_up_2", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);
    down1 = setup("/projectile/arrow_down_1", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);
    down2 = setup("/projectile/arrow_down_2", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);
    left1 = setup("/projectile/arrow_left_1", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);
    left2 = setup("/projectile/arrow_left_2", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);
    right1 = setup("/projectile/arrow_right_1", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);
    right2 = setup("/projectile/arrow_right_2", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);
  }

  @Override
  public boolean haveResource(Entity user) {
    return true;
  }

  @Override
  public void subtractResource(Entity user) {
    // Flechas não consomem recurso
  }

  @Override
  public Color getParticleColor() {
    return PARTICLE_COLOR;
  }

  @Override
  public int getParticleSize() {
    return 6;
  }

  @Override
  public int getParticleSpeed() {
    return 1;
  }

  @Override
  public int getParticleMaxLife() {
    return 15;
  }
}

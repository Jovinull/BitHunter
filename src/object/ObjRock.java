package object;

import java.awt.Color;

import entity.Entity;
import entity.Projectile;
import main.GamePanel;

public class ObjRock extends Projectile {

  public static final String OBJ_ID = "item.rock";
  private static final String ROCK_SPRITE_PATH = "/projectile/rock_down_1";

  public ObjRock(GamePanel gp) {
    super(gp);

    setInternalId(OBJ_ID);
    setTranslationKey(OBJ_ID);
    setName(OBJ_ID);

    setSpeed(8);
    setMaxLife(80);
    setLife(getMaxLife());
    setAttack(2);
    setUseCost(1);
    setAlive(false);

    loadSprites();
  }

  private void loadSprites() {
    // mesma sprite para todas as direções
    up1 = setup(ROCK_SPRITE_PATH, GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);
    up2 = setup(ROCK_SPRITE_PATH, GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);
    down1 = setup(ROCK_SPRITE_PATH, GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);
    down2 = setup(ROCK_SPRITE_PATH, GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);
    left1 = setup(ROCK_SPRITE_PATH, GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);
    left2 = setup(ROCK_SPRITE_PATH, GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);
    right1 = setup(ROCK_SPRITE_PATH, GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);
    right2 = setup(ROCK_SPRITE_PATH, GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);
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

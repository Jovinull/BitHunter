package monster;

import java.util.concurrent.ThreadLocalRandom;

import entity.Entity;
import main.GamePanel;
import object.ObjCoinBronze;
import object.ObjHeart;
import object.ObjManaCrystal;
import object.ObjRock;

public class MonRedSlime extends Entity {

  public static final String MON_ID = "monster.redSlime";

  private static final String SLIME_DOWN_1 = "/monster/redslime_down_1";
  private static final String SLIME_DOWN_2 = "/monster/redslime_down_2";

  public MonRedSlime(GamePanel gp) {
    super(gp);

    // Atributos base
    setType(TYPE_MONSTER);
    setInternalId(MON_ID);
    setTranslationKey(MON_ID);

    setDefaultSpeed(2);
    setSpeed(getDefaultSpeed());
    setMaxLife(8);
    setLife(getMaxLife());
    setAttack(7);
    setDefense(1);
    setExp(5);
    setProjectile(new ObjRock(gp));

    // Colisão
    getSolidArea().x = 3;
    getSolidArea().y = 18;
    getSolidArea().width = 42;
    getSolidArea().height = 30;
    setSolidAreaDefaultX(getSolidArea().x);
    setSolidAreaDefaultY(getSolidArea().y);

    getImage();
  }

  @Override
  public void getImage() {
    up1 = setup(SLIME_DOWN_1, GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);
    up2 = setup(SLIME_DOWN_2, GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);
    down1 = setup(SLIME_DOWN_1, GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);
    down2 = setup(SLIME_DOWN_2, GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);
    left1 = setup(SLIME_DOWN_1, GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);
    left2 = setup(SLIME_DOWN_2, GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);
    right1 = setup(SLIME_DOWN_1, GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);
    right2 = setup(SLIME_DOWN_2, GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);
  }

  @Override
  public void setAction() {
    var player = gp.getPlayer();

    if (isOnPath()) {
      checkStopChasingOrNot(player, 15, 100);
      searchPath(getGoalCol(player), getGoalRow(player));
      checkShootOrNot(150, 30);
    } else {
      checkStartChasingOrNot(player, 5, 100);
      getRandomDirection(120);
    }
  }

  @Override
  public void damageReaction() {
    setActionLockCounter(0);
    setOnPath(true);
  }

  @Override
  public void checkDrop() {
    // (S2119) Reusar Random -> ThreadLocalRandom
    int i = ThreadLocalRandom.current().nextInt(1, 101);

    if (i < 40) {
      dropItem(new ObjCoinBronze(gp));
    } else if (i < 70) {
      dropItem(new ObjHeart(gp));
    } else {
      dropItem(new ObjManaCrystal(gp));
    }
  }
}

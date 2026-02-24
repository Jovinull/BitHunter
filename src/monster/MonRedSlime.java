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
  private static final int FRAMES_PER_SECOND = 60;
  private static final int RESPAWN_DELAY_SECONDS = 30;
  private static final int CHASE_STOP_DISTANCE = 15;
  private static final int CHASE_STOP_RATE = 100;
  private static final int SHOOT_RATE = 150;
  private static final int SHOT_INTERVAL = 30;
  private static final int CHASE_START_DISTANCE = 5;
  private static final int CHASE_START_RATE = 100;
  private static final int RANDOM_DIRECTION_INTERVAL = 120;
  private static final int DROP_COIN_THRESHOLD = 40;
  private static final int DROP_HEART_THRESHOLD = 70;

  public MonRedSlime(GamePanel gp) {
    super(gp);

    // Atributos base
    setType(TYPE_MONSTER);
    setInternalId(MON_ID);
    setTranslationKey(MON_ID);

    setDefaultSpeed(2);
    setSpeed(getDefaultSpeed());
    setMaxLife(16);
    setLife(getMaxLife());
    setAttack(7);
    setDefense(2);
    setExp(10);
    enableRespawn(FRAMES_PER_SECOND * RESPAWN_DELAY_SECONDS);
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
      checkStopChasingOrNot(player, CHASE_STOP_DISTANCE, CHASE_STOP_RATE);
      searchPath(getGoalCol(player), getGoalRow(player));
      checkShootOrNot(SHOOT_RATE, SHOT_INTERVAL);
    } else {
      checkStartChasingOrNot(player, CHASE_START_DISTANCE, CHASE_START_RATE);
      getRandomDirection(RANDOM_DIRECTION_INTERVAL);
    }
  }

  @Override
  public void damageReaction() {
    setActionLockCounter(0);
    setOnPath(true);
  }

  @Override
  public void checkDrop() {
    int i = ThreadLocalRandom.current().nextInt(1, 101);

    if (i < DROP_COIN_THRESHOLD) {
      dropItem(new ObjCoinBronze(gp));
    } else if (i < DROP_HEART_THRESHOLD) {
      dropItem(new ObjHeart(gp));
    } else {
      dropItem(new ObjManaCrystal(gp));
    }
  }
}

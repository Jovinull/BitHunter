package monster;

import java.util.concurrent.ThreadLocalRandom;

import entity.Entity;
import main.GamePanel;
import object.ObjCoinBronze;
import object.ObjHeart;
import object.ObjManaCrystal;

public class MonBat extends Entity {

  public static final String MON_ID = "monster.bat";

  private static final String BAT_DOWN_1 = "/monster/bat_down_1";
  private static final String BAT_DOWN_2 = "/monster/bat_down_2";
  private static final int FRAMES_PER_SECOND = 60;
  private static final int RESPAWN_DELAY_SECONDS = 10;
  private static final int RANDOM_DIRECTION_INTERVAL = 10;
  private static final int DROP_COIN_THRESHOLD = 50;
  private static final int DROP_HEART_THRESHOLD = 75;

  public MonBat(GamePanel gp) {
    super(gp);

    setType(TYPE_MONSTER);
    setInternalId(MON_ID);
    setTranslationKey(MON_ID);

    setDefaultSpeed(4);
    setSpeed(getDefaultSpeed());
    setMaxLife(10);
    setLife(getMaxLife());
    setAttack(4);
    setDefense(0);
    setExp(5);
    enableRespawn(FRAMES_PER_SECOND * RESPAWN_DELAY_SECONDS);

    // solidArea (usa o objeto exposto por getSolidArea())
    getSolidArea().x = 3;
    getSolidArea().y = 15;
    getSolidArea().width = 42;
    getSolidArea().height = 21;
    setSolidAreaDefaultX(getSolidArea().x);
    setSolidAreaDefaultY(getSolidArea().y);

    // carrega sprites
    getImage();
  }

  /** Carrega sprites do morcego (padrão antigo: manter nome getImage()). */
  @Override
  public void getImage() {
    up1 = setup(BAT_DOWN_1, GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);
    up2 = setup(BAT_DOWN_2, GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);
    down1 = setup(BAT_DOWN_1, GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);
    down2 = setup(BAT_DOWN_2, GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);
    left1 = setup(BAT_DOWN_1, GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);
    left2 = setup(BAT_DOWN_2, GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);
    right1 = setup(BAT_DOWN_1, GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);
    right2 = setup(BAT_DOWN_2, GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);
  }

  @Override
  public void setAction() {
    getRandomDirection(RANDOM_DIRECTION_INTERVAL);
  }

  @Override
  public void damageReaction() {
    // Reseta o contador de ações para reagir imediatamente
    setActionLockCounter(0);
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

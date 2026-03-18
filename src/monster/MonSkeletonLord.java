package monster;

import java.util.concurrent.ThreadLocalRandom;

import data.Progress;
import entity.Entity;
import main.GamePanel;
import object.ObjCoinBronze;
import object.ObjDoorIron;
import object.ObjHeart;
import object.ObjManaCrystal;

public final class MonSkeletonLord extends Entity {

  public static final String MON_ID = "monster.skeletonLord";
  private static final int BOSS_SCALE = 5;
  private static final int SOLID_AREA_BASE_OFFSET = 48;
  private static final int ATTACK_AREA_SIZE = 170;
  private static final int MOTION_1_DURATION = 25;
  private static final int MOTION_2_DURATION = 50;
  private static final int RAGE_TRIGGER_DIVISOR = 2;
  private static final int RAGE_SPEED_BONUS = 1;
  private static final int RAGE_ATTACK_VALUE = 15;
  private static final int CHASE_DISTANCE = 10;
  private static final int MOVE_TOWARD_PLAYER_INTERVAL = 60;
  private static final int RANDOM_DIRECTION_INTERVAL = 120;
  private static final int ATTACK_RATE = 60;
  private static final int ATTACK_STRAIGHT_RANGE_TILES = 7;
  private static final int ATTACK_HORIZONTAL_RANGE_TILES = 5;
  private static final int DROP_COIN_THRESHOLD = 50;
  private static final int DROP_HEART_THRESHOLD = 75;

  public MonSkeletonLord(GamePanel gp) {
    super(gp);

    // Atributos-base
    setType(TYPE_MONSTER);
    setBoss(true);
    setInternalId(MON_ID);
    setTranslationKey(MON_ID);

    setDefaultSpeed(1);
    setSpeed(getDefaultSpeed());
    setMaxLife(80);
    setLife(getMaxLife());
    setAttack(10);
    setDefense(4);
    setExp(100);
    setKnockBackPower(5);
    setSleep(true);

    int size = GamePanel.TILE_SIZE * BOSS_SCALE;

    // Área sólida
    getSolidArea().x = SOLID_AREA_BASE_OFFSET;
    getSolidArea().y = SOLID_AREA_BASE_OFFSET;
    getSolidArea().width = size - SOLID_AREA_BASE_OFFSET * 2;
    getSolidArea().height = size - SOLID_AREA_BASE_OFFSET;
    setSolidAreaDefaultX(getSolidArea().x);
    setSolidAreaDefaultY(getSolidArea().y);

    // Área de ataque
    getAttackArea().width = ATTACK_AREA_SIZE;
    getAttackArea().height = ATTACK_AREA_SIZE;

    // Durações das animações de ataque
    setMotion1Duration(MOTION_1_DURATION);
    setMotion2Duration(MOTION_2_DURATION);

    getImage();
    getAttackImage();
    setDialogue();
  }

  @Override
  public void getImage() {
    int i = BOSS_SCALE;

    if (!isInRage()) {
      up1 = setup("/monster/skeletonlord_up_1", GamePanel.TILE_SIZE * i, GamePanel.TILE_SIZE * i);
      up2 = setup("/monster/skeletonlord_up_2", GamePanel.TILE_SIZE * i, GamePanel.TILE_SIZE * i);
      down1 = setup("/monster/skeletonlord_down_1", GamePanel.TILE_SIZE * i, GamePanel.TILE_SIZE * i);
      down2 = setup("/monster/skeletonlord_down_2", GamePanel.TILE_SIZE * i, GamePanel.TILE_SIZE * i);
      left1 = setup("/monster/skeletonlord_left_1", GamePanel.TILE_SIZE * i, GamePanel.TILE_SIZE * i);
      left2 = setup("/monster/skeletonlord_left_2", GamePanel.TILE_SIZE * i, GamePanel.TILE_SIZE * i);
      right1 = setup("/monster/skeletonlord_right_1", GamePanel.TILE_SIZE * i, GamePanel.TILE_SIZE * i);
      right2 = setup("/monster/skeletonlord_right_2", GamePanel.TILE_SIZE * i, GamePanel.TILE_SIZE * i);
    } else {
      up1 = setup("/monster/skeletonlord_phase2_up_1", GamePanel.TILE_SIZE * i, GamePanel.TILE_SIZE * i);
      up2 = setup("/monster/skeletonlord_phase2_up_2", GamePanel.TILE_SIZE * i, GamePanel.TILE_SIZE * i);
      down1 = setup("/monster/skeletonlord_phase2_down_1", GamePanel.TILE_SIZE * i, GamePanel.TILE_SIZE * i);
      down2 = setup("/monster/skeletonlord_phase2_down_2", GamePanel.TILE_SIZE * i, GamePanel.TILE_SIZE * i);
      left1 = setup("/monster/skeletonlord_phase2_left_1", GamePanel.TILE_SIZE * i, GamePanel.TILE_SIZE * i);
      left2 = setup("/monster/skeletonlord_phase2_left_2", GamePanel.TILE_SIZE * i, GamePanel.TILE_SIZE * i);
      right1 = setup("/monster/skeletonlord_phase2_right_1", GamePanel.TILE_SIZE * i, GamePanel.TILE_SIZE * i);
      right2 = setup("/monster/skeletonlord_phase2_right_2", GamePanel.TILE_SIZE * i, GamePanel.TILE_SIZE * i);
    }
  }

  public void getAttackImage() {
    int i = BOSS_SCALE;

    if (!isInRage()) {
      attackUp1 = setup("/monster/skeletonlord_attack_up_1", GamePanel.TILE_SIZE * i, GamePanel.TILE_SIZE * i * 2);
      attackUp2 = setup("/monster/skeletonlord_attack_up_2", GamePanel.TILE_SIZE * i, GamePanel.TILE_SIZE * i * 2);
      attackDown1 = setup("/monster/skeletonlord_attack_down_1", GamePanel.TILE_SIZE * i,
          GamePanel.TILE_SIZE * i * 2);
      attackDown2 = setup("/monster/skeletonlord_attack_down_2", GamePanel.TILE_SIZE * i,
          GamePanel.TILE_SIZE * i * 2);
      attackLeft1 = setup("/monster/skeletonlord_attack_left_1", GamePanel.TILE_SIZE * i * 2,
          GamePanel.TILE_SIZE * i);
      attackLeft2 = setup("/monster/skeletonlord_attack_left_2", GamePanel.TILE_SIZE * i * 2,
          GamePanel.TILE_SIZE * i);
      attackRight1 = setup("/monster/skeletonlord_attack_right_1", GamePanel.TILE_SIZE * i * 2,
          GamePanel.TILE_SIZE * i);
      attackRight2 = setup("/monster/skeletonlord_attack_right_2", GamePanel.TILE_SIZE * i * 2,
          GamePanel.TILE_SIZE * i);
    } else {
      attackUp1 = setup("/monster/skeletonlord_phase2_attack_up_1", GamePanel.TILE_SIZE * i,
          GamePanel.TILE_SIZE * i * 2);
      attackUp2 = setup("/monster/skeletonlord_phase2_attack_up_2", GamePanel.TILE_SIZE * i,
          GamePanel.TILE_SIZE * i * 2);
      attackDown1 = setup("/monster/skeletonlord_phase2_attack_down_1", GamePanel.TILE_SIZE * i,
          GamePanel.TILE_SIZE * i * 2);
      attackDown2 = setup("/monster/skeletonlord_phase2_attack_down_2", GamePanel.TILE_SIZE * i,
          GamePanel.TILE_SIZE * i * 2);
      attackLeft1 = setup("/monster/skeletonlord_phase2_attack_left_1", GamePanel.TILE_SIZE * i * 2,
          GamePanel.TILE_SIZE * i);
      attackLeft2 = setup("/monster/skeletonlord_phase2_attack_left_2", GamePanel.TILE_SIZE * i * 2,
          GamePanel.TILE_SIZE * i);
      attackRight1 = setup("/monster/skeletonlord_phase2_attack_right_1", GamePanel.TILE_SIZE * i * 2,
          GamePanel.TILE_SIZE * i);
      attackRight2 = setup("/monster/skeletonlord_phase2_attack_right_2", GamePanel.TILE_SIZE * i * 2,
          GamePanel.TILE_SIZE * i);
    }
  }

  public void setDialogue() {
    String baseKey = getTranslationKey() + ".dialog.intro";
    fillDialogueSet(0, baseKey, 3);
  }

  @Override
  public void setAction() {
    var player = gp.getPlayer();

    // Entra em fúria quando a vida cai abaixo de 50%
    if (!isInRage() && getLife() < getMaxLife() / RAGE_TRIGGER_DIVISOR) {
      setInRage(true);
      getImage();
      getAttackImage();
      setDefaultSpeed(getDefaultSpeed() + RAGE_SPEED_BONUS);
      setSpeed(getDefaultSpeed());
      setAttack(RAGE_ATTACK_VALUE);
    }

    if (getTileDistance(player) < CHASE_DISTANCE) {
      moveTowardPlayer(MOVE_TOWARD_PLAYER_INTERVAL);
    } else {
      getRandomDirection(RANDOM_DIRECTION_INTERVAL);
    }

    if (!isAttacking()) {
      checkAttackOrNot(
          ATTACK_RATE,
          GamePanel.TILE_SIZE * ATTACK_STRAIGHT_RANGE_TILES,
          GamePanel.TILE_SIZE * ATTACK_HORIZONTAL_RANGE_TILES);
    }
  }

  @Override
  public void damageReaction() {
    setActionLockCounter(0);
  }

  @Override
  public void checkDrop() {
    gp.setBossBattleOn(false);
    Progress.setSkeletonLordDefeated(true);
    gp.getAchievementManager().onBossDefeated();

    gp.stopMusic();
    gp.playMusic(19);

    // Remove portas de ferro do mapa atual
    Entity[] objects = gp.getObj()[gp.getCurrentMap()];
    for (int i = 0; i < objects.length; i++) {
      Entity obj = objects[i];
      if (obj != null && ObjDoorIron.OBJ_ID.equals(obj.getInternalId())) {
        gp.playSE(21);
        objects[i] = null;
      }
    }

    int roll = ThreadLocalRandom.current().nextInt(1, 101);

    if (roll < DROP_COIN_THRESHOLD) {
      dropItem(new ObjCoinBronze(gp));
    } else if (roll < DROP_HEART_THRESHOLD) {
      dropItem(new ObjHeart(gp));
    } else {
      dropItem(new ObjManaCrystal(gp));
    }
  }
}

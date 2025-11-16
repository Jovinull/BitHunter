package monster;

import java.util.concurrent.ThreadLocalRandom;

import data.Progress;
import entity.Entity;
import main.GamePanel;
import object.ObjCoinBronze;
import object.ObjDoorIron;
import object.ObjHeart;
import object.ObjManaCrystal;

public class MonSkeletonLord extends Entity {

  public static final String MON_ID = "monster.skeletonLord";

  public MonSkeletonLord(GamePanel gp) {
    super(gp);

    // Atributos-base
    setType(TYPE_MONSTER);
    setBoss(true);
    setInternalId(MON_ID);
    setTranslationKey(MON_ID);

    setDefaultSpeed(1);
    setSpeed(getDefaultSpeed());
    setMaxLife(50);
    setLife(getMaxLife());
    setAttack(10);
    setDefense(2);
    setExp(50);
    setKnockBackPower(5);
    setSleep(true);

    int size = GamePanel.TILE_SIZE * 5;

    // Área sólida
    getSolidArea().x = 48;
    getSolidArea().y = 48;
    getSolidArea().width = size - 48 * 2;
    getSolidArea().height = size - 48;
    setSolidAreaDefaultX(getSolidArea().x);
    setSolidAreaDefaultY(getSolidArea().y);

    // Área de ataque
    getAttackArea().width = 170;
    getAttackArea().height = 170;

    // Durações das animações de ataque
    setMotion1Duration(25);
    setMotion2Duration(50);

    getImage();
    getAttackImage();
    setDialogue();
  }

  @Override
  public void getImage() {
    int i = 5;

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
    int i = 5;

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
    if (!isInRage() && getLife() < getMaxLife() / 2) {
      setInRage(true);
      getImage();
      getAttackImage();
      setDefaultSpeed(getDefaultSpeed() + 1);
      setSpeed(getDefaultSpeed());
      setAttack(getAttack() * 2);
    }

    if (getTileDistance(player) < 10) {
      moveTowardPlayer(60);
    } else {
      getRandomDirection(120);
    }

    if (!isAttacking()) {
      checkAttackOrNot(60, GamePanel.TILE_SIZE * 7, GamePanel.TILE_SIZE * 5);
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

    if (roll < 50) {
      dropItem(new ObjCoinBronze(gp));
    } else if (roll < 75) {
      dropItem(new ObjHeart(gp));
    } else {
      dropItem(new ObjManaCrystal(gp));
    }
  }
}

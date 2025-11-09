package monster;

import java.util.concurrent.ThreadLocalRandom;

import entity.Entity;
import main.GamePanel;
import object.ObjCoinBronze;
import object.ObjHeart;
import object.ObjManaCrystal;

public class MonOrc extends Entity {

  public MonOrc(GamePanel gp) {
    super(gp);

    // Atributos base
    setType(TYPE_MONSTER);
    setName("Orc");
    setDefaultSpeed(1);
    setSpeed(getDefaultSpeed());
    setMaxLife(12);
    setLife(getMaxLife());
    setAttack(8);
    setDefense(2);
    setExp(10);
    setKnockBackPower(5);

    // Colisão / Ataque
    getSolidArea().x = 4;
    getSolidArea().y = 4;
    getSolidArea().width = 40;
    getSolidArea().height = 44;
    setSolidAreaDefaultX(getSolidArea().x);
    setSolidAreaDefaultY(getSolidArea().y);

    getAttackArea().width = 48;
    getAttackArea().height = 48;

    setMotion1Duration(40);
    setMotion2Duration(85);

    getImage();
    getAttackImage();
  }

  @Override
  public void getImage() {
    up1 = setup("/monster/orc_up_1", gp.tileSize, gp.tileSize);
    up2 = setup("/monster/orc_up_2", gp.tileSize, gp.tileSize);
    down1 = setup("/monster/orc_down_1", gp.tileSize, gp.tileSize);
    down2 = setup("/monster/orc_down_2", gp.tileSize, gp.tileSize);
    left1 = setup("/monster/orc_left_1", gp.tileSize, gp.tileSize);
    left2 = setup("/monster/orc_left_2", gp.tileSize, gp.tileSize);
    right1 = setup("/monster/orc_right_1", gp.tileSize, gp.tileSize);
    right2 = setup("/monster/orc_right_2", gp.tileSize, gp.tileSize);
  }

  public void getAttackImage() {
    attackUp1 = setup("/monster/orc_attack_up_1", gp.tileSize, gp.tileSize * 2);
    attackUp2 = setup("/monster/orc_attack_up_2", gp.tileSize, gp.tileSize * 2);
    attackDown1 = setup("/monster/orc_attack_down_1", gp.tileSize, gp.tileSize * 2);
    attackDown2 = setup("/monster/orc_attack_down_2", gp.tileSize, gp.tileSize * 2);
    attackLeft1 = setup("/monster/orc_attack_left_1", gp.tileSize * 2, gp.tileSize);
    attackLeft2 = setup("/monster/orc_attack_left_2", gp.tileSize * 2, gp.tileSize);
    attackRight1 = setup("/monster/orc_attack_right_1", gp.tileSize * 2, gp.tileSize);
    attackRight2 = setup("/monster/orc_attack_right_2", gp.tileSize * 2, gp.tileSize);
  }

  @Override
  public void setAction() {
    if (isOnPath()) {
      checkStopChasingOrNot(gp.player, 15, 100);
      searchPath(getGoalCol(gp.player), getGoalRow(gp.player));
    } else {
      checkStartChasingOrNot(gp.player, 5, 100);
      getRandomDirection(120);
    }

    if (!isAttacking()) {
      checkAttackOrNot(30, gp.tileSize * 4, gp.tileSize);
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

    if (i < 50) {
      dropItem(new ObjCoinBronze(gp));
    } else if (i < 75) {
      dropItem(new ObjHeart(gp));
    } else {
      dropItem(new ObjManaCrystal(gp));
    }
  }
}

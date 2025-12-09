package monster;

import java.util.concurrent.ThreadLocalRandom;

import entity.Entity;
import main.GamePanel;
import object.ObjCoinBronze;
import object.ObjHeart;
import object.ObjManaCrystal;

public class MonOrc extends Entity {

  public static final String MON_ID = "monster.orc";

  public MonOrc(GamePanel gp) {
    super(gp);

    // Atributos base
    setType(TYPE_MONSTER);
    setInternalId(MON_ID);
    setTranslationKey(MON_ID);

    setDefaultSpeed(1);
    setSpeed(getDefaultSpeed());
    setMaxLife(30);
    setLife(getMaxLife());
    setAttack(9);
    setDefense(3);
    setExp(20);
    enableRespawn(60 * 40);
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
    up1 = setup("/monster/orc_up_1", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);
    up2 = setup("/monster/orc_up_2", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);
    down1 = setup("/monster/orc_down_1", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);
    down2 = setup("/monster/orc_down_2", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);
    left1 = setup("/monster/orc_left_1", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);
    left2 = setup("/monster/orc_left_2", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);
    right1 = setup("/monster/orc_right_1", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);
    right2 = setup("/monster/orc_right_2", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);
  }

  public void getAttackImage() {
    attackUp1 = setup("/monster/orc_attack_up_1", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE * 2);
    attackUp2 = setup("/monster/orc_attack_up_2", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE * 2);
    attackDown1 = setup("/monster/orc_attack_down_1", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE * 2);
    attackDown2 = setup("/monster/orc_attack_down_2", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE * 2);
    attackLeft1 = setup("/monster/orc_attack_left_1", GamePanel.TILE_SIZE * 2, GamePanel.TILE_SIZE);
    attackLeft2 = setup("/monster/orc_attack_left_2", GamePanel.TILE_SIZE * 2, GamePanel.TILE_SIZE);
    attackRight1 = setup("/monster/orc_attack_right_1", GamePanel.TILE_SIZE * 2, GamePanel.TILE_SIZE);
    attackRight2 = setup("/monster/orc_attack_right_2", GamePanel.TILE_SIZE * 2, GamePanel.TILE_SIZE);
  }

  @Override
  public void setAction() {
    var player = gp.getPlayer();

    if (isOnPath()) {
      checkStopChasingOrNot(player, 15, 100);
      searchPath(getGoalCol(player), getGoalRow(player));
    } else {
      checkStartChasingOrNot(player, 5, 100);
      getRandomDirection(120);
    }

    if (!isAttacking()) {
      checkAttackOrNot(30, GamePanel.TILE_SIZE * 4, GamePanel.TILE_SIZE);
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

    if (i < 50) {
      dropItem(new ObjCoinBronze(gp));
    } else if (i < 75) {
      dropItem(new ObjHeart(gp));
    } else {
      dropItem(new ObjManaCrystal(gp));
    }
  }
}

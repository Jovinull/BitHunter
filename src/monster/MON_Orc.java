package monster;

import java.util.Random;

import entity.Entity;
import main.GamePanel;
import object.OBJ_Coin_Bronze;
import object.OBJ_Heart;
import object.OBJ_ManaCrystal;

public class MON_Orc extends Entity {

  public MON_Orc(GamePanel gp) {
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
      // Para de perseguir?
      checkStopChasingOrNot(gp.player, 15, 100);
      // Vai até a posição do jogador
      searchPath(getGoalCol(gp.player), getGoalRow(gp.player));
    } else {
      // Começa a perseguir?
      checkStartChasingOrNot(gp.player, 5, 100);
      // Direção aleatória
      getRandomDirection(120);
    }

    // Ataca se estiver apto
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
    // 1..100
    int i = new Random().nextInt(100) + 1;

    if (i < 50) {
      dropItem(new OBJ_Coin_Bronze(gp));
    } else if (i < 75) {
      dropItem(new OBJ_Heart(gp));
    } else {
      dropItem(new OBJ_ManaCrystal(gp));
    }
  }
}

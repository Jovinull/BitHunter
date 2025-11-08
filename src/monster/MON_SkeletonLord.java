package monster;

import java.util.Random;

import data.Progress;
import entity.Entity;
import main.GamePanel;
import object.OBJ_Coin_Bronze;
import object.OBJ_Door_Iron;
import object.OBJ_Heart;
import object.OBJ_ManaCrystal;

public class MON_SkeletonLord extends Entity {

  public static final String monName = "Lorde Esqueleto";

  public MON_SkeletonLord(GamePanel gp) {
    super(gp);

    // Atributos-base
    setType(TYPE_MONSTER);
    setBoss(true);
    setName(monName);
    setDefaultSpeed(1);
    setSpeed(getDefaultSpeed());
    setMaxLife(50);
    setLife(getMaxLife());
    setAttack(10);
    setDefense(2);
    setExp(50);
    setKnockBackPower(5);
    setSleep(true);

    int size = gp.tileSize * 5;

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
      up1 = setup("/monster/skeletonlord_up_1", gp.tileSize * i, gp.tileSize * i);
      up2 = setup("/monster/skeletonlord_up_2", gp.tileSize * i, gp.tileSize * i);
      down1 = setup("/monster/skeletonlord_down_1", gp.tileSize * i, gp.tileSize * i);
      down2 = setup("/monster/skeletonlord_down_2", gp.tileSize * i, gp.tileSize * i);
      left1 = setup("/monster/skeletonlord_left_1", gp.tileSize * i, gp.tileSize * i);
      left2 = setup("/monster/skeletonlord_left_2", gp.tileSize * i, gp.tileSize * i);
      right1 = setup("/monster/skeletonlord_right_1", gp.tileSize * i, gp.tileSize * i);
      right2 = setup("/monster/skeletonlord_right_2", gp.tileSize * i, gp.tileSize * i);
    } else {
      up1 = setup("/monster/skeletonlord_phase2_up_1", gp.tileSize * i, gp.tileSize * i);
      up2 = setup("/monster/skeletonlord_phase2_up_2", gp.tileSize * i, gp.tileSize * i);
      down1 = setup("/monster/skeletonlord_phase2_down_1", gp.tileSize * i, gp.tileSize * i);
      down2 = setup("/monster/skeletonlord_phase2_down_2", gp.tileSize * i, gp.tileSize * i);
      left1 = setup("/monster/skeletonlord_phase2_left_1", gp.tileSize * i, gp.tileSize * i);
      left2 = setup("/monster/skeletonlord_phase2_left_2", gp.tileSize * i, gp.tileSize * i);
      right1 = setup("/monster/skeletonlord_phase2_right_1", gp.tileSize * i, gp.tileSize * i);
      right2 = setup("/monster/skeletonlord_phase2_right_2", gp.tileSize * i, gp.tileSize * i);
    }
  }

  public void getAttackImage() {
    int i = 5;

    if (!isInRage()) {
      attackUp1 = setup("/monster/skeletonlord_attack_up_1", gp.tileSize * i, gp.tileSize * i * 2);
      attackUp2 = setup("/monster/skeletonlord_attack_up_2", gp.tileSize * i, gp.tileSize * i * 2);
      attackDown1 = setup("/monster/skeletonlord_attack_down_1", gp.tileSize * i, gp.tileSize * i * 2);
      attackDown2 = setup("/monster/skeletonlord_attack_down_2", gp.tileSize * i, gp.tileSize * i * 2);
      attackLeft1 = setup("/monster/skeletonlord_attack_left_1", gp.tileSize * i * 2, gp.tileSize * i);
      attackLeft2 = setup("/monster/skeletonlord_attack_left_2", gp.tileSize * i * 2, gp.tileSize * i);
      attackRight1 = setup("/monster/skeletonlord_attack_right_1", gp.tileSize * i * 2, gp.tileSize * i);
      attackRight2 = setup("/monster/skeletonlord_attack_right_2", gp.tileSize * i * 2, gp.tileSize * i);
    } else {
      attackUp1 = setup("/monster/skeletonlord_phase2_attack_up_1", gp.tileSize * i, gp.tileSize * i * 2);
      attackUp2 = setup("/monster/skeletonlord_phase2_attack_up_2", gp.tileSize * i, gp.tileSize * i * 2);
      attackDown1 = setup("/monster/skeletonlord_phase2_attack_down_1", gp.tileSize * i, gp.tileSize * i * 2);
      attackDown2 = setup("/monster/skeletonlord_phase2_attack_down_2", gp.tileSize * i, gp.tileSize * i * 2);
      attackLeft1 = setup("/monster/skeletonlord_phase2_attack_left_1", gp.tileSize * i * 2, gp.tileSize * i);
      attackLeft2 = setup("/monster/skeletonlord_phase2_attack_left_2", gp.tileSize * i * 2, gp.tileSize * i);
      attackRight1 = setup("/monster/skeletonlord_phase2_attack_right_1", gp.tileSize * i * 2, gp.tileSize * i);
      attackRight2 = setup("/monster/skeletonlord_phase2_attack_right_2", gp.tileSize * i * 2, gp.tileSize * i);
    }
  }

  public void setDialogue() {
    getDialogues()[0][0] = "Ninguém vai tocar no meu tesouro!";
    getDialogues()[0][1] = "Você vai morrer aqui, insignificante!";
    getDialogues()[0][2] = "BEM-VINDO AO SEU FIM!";
  }

  @Override
  public void setAction() {

    // Entra em fúria quando a vida cai abaixo de 50%
    if (!isInRage() && getLife() < getMaxLife() / 2) {
      setInRage(true);
      getImage();
      getAttackImage();
      setDefaultSpeed(getDefaultSpeed() + 1);
      setSpeed(getDefaultSpeed());
      setAttack(getAttack() * 2);
    }

    // Aproxima se o jogador estiver por perto
    if (getTileDistance(gp.player) < 10) {
      moveTowardPlayer(60);
    } else {
      // Direção aleatória
      getRandomDirection(120);
    }

    // Ataque
    if (!isAttacking()) {
      checkAttackOrNot(60, gp.tileSize * 7, gp.tileSize * 5);
    }
  }

  @Override
  public void damageReaction() {
    setActionLockCounter(0);
  }

  @Override
  public void checkDrop() {
    gp.bossBattleOn = false;
    Progress.setSkeletonLordDefeated(true);

    // Música
    gp.stopMusic();
    gp.playMusic(19);

    // Remove portas de ferro do mapa atual
    for (int i = 0; i < gp.obj[gp.currentMap].length; i++) {
      if (gp.obj[gp.currentMap][i] != null
          && OBJ_Door_Iron.objName.equals(gp.obj[gp.currentMap][i].getName())) {
        gp.playSE(21);
        gp.obj[gp.currentMap][i] = null;
      }
    }

    // Sorteio de drop
    int roll = new Random().nextInt(100) + 1;

    if (roll < 50) {
      dropItem(new OBJ_Coin_Bronze(gp));
    } else if (roll < 75) {
      dropItem(new OBJ_Heart(gp));
    } else {
      dropItem(new OBJ_ManaCrystal(gp));
    }
  }
}

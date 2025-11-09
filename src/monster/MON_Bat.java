package monster;

import java.util.Random;

import entity.Entity;
import main.GamePanel;
import object.ObjCoinBronze;
import object.ObjHeart;
import object.ObjManaCrystal;

public class MON_Bat extends Entity {

  public MON_Bat(GamePanel gp) {
    super(gp);

    setType(TYPE_MONSTER);
    setName("Morcego");
    setDefaultSpeed(4);
    setSpeed(getDefaultSpeed());
    setMaxLife(7);
    setLife(getMaxLife());
    setAttack(7);
    setDefense(0);
    setExp(7);

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
  public void getImage() {
    up1 = setup("/monster/bat_down_1", gp.tileSize, gp.tileSize);
    up2 = setup("/monster/bat_down_2", gp.tileSize, gp.tileSize);
    down1 = setup("/monster/bat_down_1", gp.tileSize, gp.tileSize);
    down2 = setup("/monster/bat_down_2", gp.tileSize, gp.tileSize);
    left1 = setup("/monster/bat_down_1", gp.tileSize, gp.tileSize);
    left2 = setup("/monster/bat_down_2", gp.tileSize, gp.tileSize);
    right1 = setup("/monster/bat_down_1", gp.tileSize, gp.tileSize);
    right2 = setup("/monster/bat_down_2", gp.tileSize, gp.tileSize);
  }

  @Override
  public void setAction() {
    getRandomDirection(10);
  }

  @Override
  public void damageReaction() {
    // Reseta o contador de ações para reagir imediatamente
    setActionLockCounter(0);
    // Se quiser perseguir o jogador ao ser atingido, você pode:
    // setOnPath(true);
  }

  @Override
  public void checkDrop() {
    // 1..100
    int i = new Random().nextInt(100) + 1;

    if (i < 50) {
      dropItem(new ObjCoinBronze(gp));
    } else if (i < 75) {
      dropItem(new ObjHeart(gp));
    } else {
      dropItem(new ObjManaCrystal(gp));
    }
  }
}

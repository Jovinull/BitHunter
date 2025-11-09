package monster;

import java.util.Random;

import entity.Entity;
import main.GamePanel;
import object.ObjCoinBronze;
import object.ObjHeart;
import object.ObjManaCrystal;
import object.ObjRock;

public class MON_GreenSlime extends Entity {

  public MON_GreenSlime(GamePanel gp) {
    super(gp);

    setType(TYPE_MONSTER);
    setName("Slime Verde");
    setDefaultSpeed(1);
    setSpeed(getDefaultSpeed());
    setMaxLife(6);
    setLife(getMaxLife());
    setAttack(5);
    setDefense(0);
    setExp(2);
    setProjectile(new ObjRock(gp));

    // solidArea (usar o objeto via getter, Rectangle tem campos públicos)
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
    // Sprites em todas as direções (slime usa os mesmos frames do "down")
    up1 = setup("/monster/greenslime_down_1", gp.tileSize, gp.tileSize);
    up2 = setup("/monster/greenslime_down_2", gp.tileSize, gp.tileSize);
    down1 = setup("/monster/greenslime_down_1", gp.tileSize, gp.tileSize);
    down2 = setup("/monster/greenslime_down_2", gp.tileSize, gp.tileSize);
    left1 = setup("/monster/greenslime_down_1", gp.tileSize, gp.tileSize);
    left2 = setup("/monster/greenslime_down_2", gp.tileSize, gp.tileSize);
    right1 = setup("/monster/greenslime_down_1", gp.tileSize, gp.tileSize);
    right2 = setup("/monster/greenslime_down_2", gp.tileSize, gp.tileSize);
  }

  @Override
  public void setAction() {

    if (isOnPath()) {
      // Para de perseguir?
      checkStopChasingOrNot(gp.player, 15, 100);

      // Vai até a posição do jogador
      searchPath(getGoalCol(gp.player), getGoalRow(gp.player));

      // Tentar atirar projetil
      checkShootOrNot(200, 30);

    } else {
      // Começa a perseguir?
      checkStartChasingOrNot(gp.player, 5, 100);

      // Anda aleatoriamente
      getRandomDirection(120);
    }
  }

  @Override
  public void damageReaction() {
    setActionLockCounter(0); // Reseta o contador de ações
    // direction = gp.player.getDirection(); // se quiser reagir olhando p/ jogador
    setOnPath(true);
  }

  @Override
  public void checkDrop() {
    // Dado 1..100
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

package monster;

import java.util.Random;

import entity.Entity;
import main.GamePanel;
import object.OBJ_Coin_Bronze;
import object.OBJ_Heart;
import object.OBJ_ManaCrystal;
import object.OBJ_Rock;

public class MON_RedSlime extends Entity {

  public MON_RedSlime(GamePanel gp) {
    super(gp);

    // Atributos base
    setType(TYPE_MONSTER);
    setName("Slime Vermelho");
    setDefaultSpeed(2);
    setSpeed(getDefaultSpeed());
    setMaxLife(8);
    setLife(getMaxLife());
    setAttack(7);
    setDefense(1);
    setExp(5);
    setProjectile(new OBJ_Rock(gp));

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
    up1 = setup("/monster/redslime_down_1", gp.tileSize, gp.tileSize);
    up2 = setup("/monster/redslime_down_2", gp.tileSize, gp.tileSize);
    down1 = setup("/monster/redslime_down_1", gp.tileSize, gp.tileSize);
    down2 = setup("/monster/redslime_down_2", gp.tileSize, gp.tileSize);
    left1 = setup("/monster/redslime_down_1", gp.tileSize, gp.tileSize);
    left2 = setup("/monster/redslime_down_2", gp.tileSize, gp.tileSize);
    right1 = setup("/monster/redslime_down_1", gp.tileSize, gp.tileSize);
    right2 = setup("/monster/redslime_down_2", gp.tileSize, gp.tileSize);
  }

  @Override
  public void setAction() {
    if (isOnPath()) {
      // Verifica se para de perseguir
      checkStopChasingOrNot(gp.player, 15, 100);
      // Persegue o jogador
      searchPath(getGoalCol(gp.player), getGoalRow(gp.player));
      // Dispara projétil com mais frequência
      checkShootOrNot(150, 30);
    } else {
      // Verifica se começa a perseguir
      checkStartChasingOrNot(gp.player, 5, 100);
      // Direção aleatória
      getRandomDirection(120);
    }
  }

  @Override
  public void damageReaction() {
    setActionLockCounter(0);
    setOnPath(true); // Começa a perseguir ao ser atingido
  }

  @Override
  public void checkDrop() {
    int i = new Random().nextInt(100) + 1;

    if (i < 40) {
      dropItem(new OBJ_Coin_Bronze(gp));
    } else if (i < 70) {
      dropItem(new OBJ_Heart(gp));
    } else {
      dropItem(new OBJ_ManaCrystal(gp));
    }
  }
}

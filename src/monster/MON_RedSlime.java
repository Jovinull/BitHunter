package monster;

import java.util.Random;

import entity.Entity;
import main.GamePanel;
import object.OBJ_Coin_Bronze;
import object.OBJ_Heart;
import object.OBJ_ManaCrystal;
import object.OBJ_Rock;

public class MON_RedSlime extends Entity {

  GamePanel gp;

  public MON_RedSlime(GamePanel gp) {
    super(gp);

    this.gp = gp;

    type = type_monster;
    name = "Slime Vermelho";
    defaultSpeed = 2;
    speed = defaultSpeed;
    maxLife = 8;
    life = maxLife;
    attack = 7;
    defense = 1;
    exp = 5;
    projectile = new OBJ_Rock(gp);

    solidArea.x = 3;
    solidArea.y = 18;
    solidArea.width = 42;
    solidArea.height = 30;
    solidAreaDefaultX = solidArea.x;
    solidAreaDefaultY = solidArea.y;

    getImage();
  }

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

  public void setAction() {

    if (onPath == true) {

      // Verifica se para de perseguir
      checkStopChasingOrNot(gp.player, 15, 100);

      // Faz o monstro perseguir o jogador
      searchPath(getGoalCol(gp.player), getGoalRow(gp.player));

      // Verifica se atira projétil
      checkShootOrNot(150, 30); // Atira com mais frequência
    }

    else {
      // Verifica se começa a perseguir
      checkStartChasingOrNot(gp.player, 5, 100);

      // Escolhe uma direção aleatória
      getRandomDirection();
    }
  }

  public void damageReaction() {
    actionLockCounter = 0;
    onPath = true; // Começa a perseguir ao ser atingido
  }

  public void checkDrop() {
    int i = new Random().nextInt(100) + 1;

    if (i < 40) {
      dropItem(new OBJ_Coin_Bronze(gp));
    }

    if (i >= 40 && i < 70) {
      dropItem(new OBJ_Heart(gp));
    }

    if (i >= 70 && i <= 100) {
      dropItem(new OBJ_ManaCrystal(gp));
    }
  }
}

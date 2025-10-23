package monster;

import java.util.Random;

import entity.Entity;
import main.GamePanel;
import object.OBJ_Coin_Bronze;
import object.OBJ_Heart;
import object.OBJ_ManaCrystal;
import object.OBJ_Rock;

public class MON_GreenSlime extends Entity {

  GamePanel gp;

  public MON_GreenSlime(GamePanel gp) {
    super(gp);

    this.gp = gp;

    type = type_monster;
    name = "Green Slime";
    defaultSpeed = 1;
    speed = defaultSpeed;
    maxLife = 6;
    life = maxLife;
    attack = 5;
    defense = 0;
    exp = 2;
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
    up1 = setup("/monster/greenslime_down_1", gp.tileSize, gp.tileSize);
    up2 = setup("/monster/greenslime_down_2", gp.tileSize, gp.tileSize);
    down1 = setup("/monster/greenslime_down_1", gp.tileSize, gp.tileSize);
    down2 = setup("/monster/greenslime_down_2", gp.tileSize, gp.tileSize);
    left1 = setup("/monster/greenslime_down_1", gp.tileSize, gp.tileSize);
    left2 = setup("/monster/greenslime_down_2", gp.tileSize, gp.tileSize);
    right1 = setup("/monster/greenslime_down_1", gp.tileSize, gp.tileSize);
    right2 = setup("/monster/greenslime_down_2", gp.tileSize, gp.tileSize);
  }

  public void update() {

    super.update();

    int xDistance = Math.abs(worldX - gp.player.worldX);
    int yDistance = Math.abs(worldY - gp.player.worldY);
    int tileDistance = (xDistance + yDistance) / gp.tileSize;

    if (onPath == false && tileDistance < 5) {

      int i = new Random().nextInt(100) + 1;
      if (i > 50) {
        onPath = true;
      }
    }

    if (onPath == true && tileDistance > 10) {
      onPath = false;
    }
  }

  public void setAction() {

    if (onPath == true) {

      // DEFINE PARA O NPC IR ATÉ A POSIÇÃO DO JOGADOR
      int goalCol = (gp.player.worldX + gp.player.solidArea.x) / gp.tileSize;
      int goalRow = (gp.player.worldY + gp.player.solidArea.y) / gp.tileSize;

      searchPath(goalCol, goalRow);

      int i = new Random().nextInt(200) + 1;
      if (i > 197 && projectile.alive == false && shotAvailableCounter == 30) {

        projectile.set(worldX, worldY, direction, true, this);

        // CHECK VACANCY
        for (int j = 0; j < gp.projectile[1].length; j++) {
          if (gp.projectile[gp.currentMap][j] == null) {
            gp.projectile[gp.currentMap][j] = projectile;
            break;
          }
        }

        shotAvailableCounter = 0;
      }
    }

    else {

      actionLockCounter++;

      // A cada 120 frames (2 segundos em 60 FPS), o NPC escolhe uma nova direção
      if (actionLockCounter == 120) {

        Random random = new Random();
        int i = random.nextInt(4); // Gera um número de 0 a 3

        // Altera a direção com base no número gerado
        switch (i) {
          case 0:
            direction = "up";
            break;
          case 1:
            direction = "down";
            break;
          case 2:
            direction = "left";
            break;
          case 3:
            direction = "right";
            break;
        }

        actionLockCounter = 0; // Reinicia o contador para o próximo intervalo
      }
    }
  }

  public void damageReaction() {

    actionLockCounter = 0; // Reseta o contador de ações
    // direction = gp.player.direction; // O monstro reage na direção do jogador
    onPath = true;
  }

  public void checkDrop() {

    // CAST A DIE (sorteia um número de 1 a 100)
    int i = new Random().nextInt(100) + 1;

    // SET THE MONSTER DROP (define o item que o monstro irá dropar)
    if (i < 50) {
      dropItem(new OBJ_Coin_Bronze(gp));
    }

    if (i >= 50 && i < 75) {
      dropItem(new OBJ_Heart(gp));
    }

    if (i >= 75 && i <= 100) {
      dropItem(new OBJ_ManaCrystal(gp));
    }
  }
}

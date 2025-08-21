package monster;

import java.util.Random;

import entity.Entity;
import main.GamePanel;

public class MON_GreenSlime extends Entity {

    GamePanel gp;

    public MON_GreenSlime(GamePanel gp) {
        super(gp);

        this.gp = gp;

        type = 2;
        name = "Green Slime";
        speed = 1;
        maxLife = 6;
        life = maxLife;

        solidArea.x = 3;
        solidArea.y = 18;
        solidArea.width = 42;
        solidArea.height = 30;
        solidAreaDefaultX = solidArea.x;
        solidAreaDefaultY = solidArea.y;

        getImage();
    }

    public void getImage() {
        up1 = setup("/resource/monster/greenslime_down_1", gp.tileSize, gp.tileSize);
        up2 = setup("/resource/monster/greenslime_down_2", gp.tileSize, gp.tileSize);
        down1 = setup("/resource/monster/greenslime_down_1", gp.tileSize, gp.tileSize);
        down2 = setup("/resource/monster/greenslime_down_2", gp.tileSize, gp.tileSize);
        left1 = setup("/resource/monster/greenslime_down_1", gp.tileSize, gp.tileSize);
        left2 = setup("/resource/monster/greenslime_down_2", gp.tileSize, gp.tileSize);
        right1 = setup("/resource/monster/greenslime_down_1", gp.tileSize, gp.tileSize);
        right2 = setup("/resource/monster/greenslime_down_2", gp.tileSize, gp.tileSize);
    }

    public void setAction() {

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

    public void damageReaction() {

        actionLockCounter = 0; // Reseta o contador de ações
        direction = gp.player.direction; // O monstro reage na direção do jogador
    }
}

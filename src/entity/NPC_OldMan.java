package entity;

import java.awt.Rectangle;
import java.util.Random;

import main.GamePanel;

// Representa o NPC "Old Man", herda da classe Entity
public class NPC_OldMan extends Entity {

  // Construtor: define direção inicial, velocidade e carrega os sprites
  public NPC_OldMan(GamePanel gp) {
    super(gp); // Chama o construtor da superclasse Entity

    direction = "down"; // Direção inicial
    speed = 1; // Velocidade fixa para o NPC

    // Hitbox voltada para os pés (consistente com player/objetos)
    solidArea = new Rectangle();
    solidArea.x = 8;
    solidArea.y = 16;
    solidAreaDefaultX = solidArea.x;
    solidAreaDefaultY = solidArea.y;
    solidArea.width = 30;
    solidArea.height = 30;

    dialogueSet = -1;

    getImage(); // Carrega as imagens de sprite para animação
    setDialogue(); // Define os diálogos do NPC
  }

  // Carrega as imagens do NPC em todas as direções (2 frames por direção)
  public void getImage() {
    up1 = setup("/npc/oldman/oldman_up_1", gp.tileSize, gp.tileSize);
    up2 = setup("/npc/oldman/oldman_up_2", gp.tileSize, gp.tileSize);
    down1 = setup("/npc/oldman/oldman_down_1", gp.tileSize, gp.tileSize);
    down2 = setup("/npc/oldman/oldman_down_2", gp.tileSize, gp.tileSize);
    left1 = setup("/npc/oldman/oldman_left_1", gp.tileSize, gp.tileSize);
    left2 = setup("/npc/oldman/oldman_left_2", gp.tileSize, gp.tileSize);
    right1 = setup("/npc/oldman/oldman_right_1", gp.tileSize, gp.tileSize);
    right2 = setup("/npc/oldman/oldman_right_2", gp.tileSize, gp.tileSize);
  }

  public void setDialogue() {
    dialogues[0][0] = "Olá, rapaz.";
    dialogues[0][1] = "Então você veio até esta ilha em busca do \ntesouro?";
    dialogues[0][2] = "Eu costumava ser um grande mago, mas agora... \nestou um pouco velho para aventuras.";
    dialogues[0][3] = "Bem, boa sorte para você!";

    dialogues[1][0] = "Se você ficar cansado, descanse na água.";
    dialogues[1][1] = "Porém, os monstros reaparecem quando você descansa.\nNão sei por quê, mas é assim que funciona.";
    dialogues[1][2] = "De qualquer forma, não se esforce demais.";

    dialogues[2][0] = "Eu me pergunto como abrir aquela porta...";
  }

  // Define o comportamento do NPC — neste caso, movimentação aleatória
  @Override
  public void setAction() {

    if (onPath == true) {

      int goalCol = 12;
      int goalRow = 9;

      searchPath(goalCol, goalRow);
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

  public void speak() {

    facePlayer();
    startDialogue(this, dialogueSet);

    dialogueSet++;

    // if (dialogues[dialogueSet][0] == null) {

    // // dialogueSet = 0;
    // dialogueSet--;
    // }

    if (gp.player.life < gp.player.maxLife / 3) {
      dialogueSet = 1;
    }

    // onPath = true;
  }
}

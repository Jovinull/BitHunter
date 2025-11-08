package entity;

import java.awt.Rectangle;

import main.GamePanel;

public class NpcOldMan extends Entity {

  private static final int GOAL_COL = 23;
  private static final int GOAL_ROW = 13;

  private boolean startedWalkToGoal = false;

  public NpcOldMan(GamePanel gp) {
    super(gp);

    direction = DIR_DOWN;
    speed = 1;
    defaultSpeed = speed;

    // solidArea é final -> apenas setar
    solidArea.x = 8;
    solidArea.y = 16;
    solidAreaDefaultX = solidArea.x;
    solidAreaDefaultY = solidArea.y;
    solidArea.width = 30;
    solidArea.height = 30;

    dialogueSet = -1;

    getImage();
    setDialogue();
  }

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

  private boolean atGoal() {
    return getCol() == GOAL_COL && getRow() == GOAL_ROW;
  }

  @Override
  public void setAction() {
    if (startedWalkToGoal) {
      if (atGoal()) {
        startedWalkToGoal = false;
        onPath = false;
      } else {
        onPath = true;
        searchPath(GOAL_COL, GOAL_ROW);
      }
      return;
    }
    getRandomDirection(120);
  }

  @Override
  public void speak() {
    facePlayer();
    startDialogue(this, dialogueSet);
    dialogueSet++;

    if (gp.player.life < gp.player.maxLife / 3) {
      dialogueSet = 1;
      startDialogue(this, dialogueSet);
    }

    if (dialogueSet == 1 && !startedWalkToGoal && !atGoal()) {
      startedWalkToGoal = true;
      onPath = true;
    }

    if (dialogues[dialogueSet][0] == null) {
      dialogueSet = Math.max(0, dialogueSet - 1);
    }
  }
}

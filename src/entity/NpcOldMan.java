package entity;

import java.awt.Rectangle;

import main.GamePanel;

public class NpcOldMan extends Entity {

  private static final int GOAL_COL = 23;
  private static final int GOAL_ROW = 13;

  private boolean startedWalkToGoal = false;

  public NpcOldMan(GamePanel gp) {
    super(gp);

    setDirection(DIR_DOWN);
    setSpeed(1);
    setDefaultSpeed(getSpeed());

    // solidArea é final -> configurar via getter
    Rectangle sa = getSolidArea();
    sa.x = 8;
    sa.y = 16;
    setSolidAreaDefaultX(sa.x);
    setSolidAreaDefaultY(sa.y);
    sa.width = 30;
    sa.height = 30;

    setDialogueSet(-1);

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
    String[][] d = getDialogues();
    d[0][0] = "Olá, rapaz.";
    d[0][1] = "Então você veio até esta ilha em busca do \ntesouro?";
    d[0][2] = "Eu costumava ser um grande mago, mas agora... \nestou um pouco velho para aventuras.";
    d[0][3] = "Bem, boa sorte para você!";

    d[1][0] = "Se você ficar cansado, descanse na água.";
    d[1][1] = "Porém, os monstros reaparecem quando você descansa.\nNão sei por quê, mas é assim que funciona.";
    d[1][2] = "De qualquer forma, não se esforce demais.";

    d[2][0] = "Eu me pergunto como abrir aquela porta...";
  }

  private boolean atGoal() {
    return getCol() == GOAL_COL && getRow() == GOAL_ROW;
  }

  @Override
  public void setAction() {
    if (startedWalkToGoal) {
      if (atGoal()) {
        startedWalkToGoal = false;
        setOnPath(false);
      } else {
        setOnPath(true);
        searchPath(GOAL_COL, GOAL_ROW);
      }
      return;
    }
    getRandomDirection(120);
  }

  @Override
  public void speak() {
    facePlayer();
    startDialogue(this, getDialogueSet());
    setDialogueSet(getDialogueSet() + 1);

    // Reage ao HP do player (encapsulado)
    if (gp.player.getLife() < gp.player.getMaxLife() / 3) {
      setDialogueSet(1);
      startDialogue(this, getDialogueSet());
    }

    // Inicia caminhada ao objetivo quando entra no diálogo 1
    if (getDialogueSet() == 1 && !startedWalkToGoal && !atGoal()) {
      startedWalkToGoal = true;
      setOnPath(true);
    }

    String[][] d = getDialogues();
    if (d[getDialogueSet()][0] == null) {
      setDialogueSet(Math.max(0, getDialogueSet() - 1));
    }
  }
}

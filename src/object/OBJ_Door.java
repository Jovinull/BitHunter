package object;

import java.awt.Rectangle;

import entity.Entity;
import main.GamePanel;

// Representa a porta do jogo; bloqueia passagem se collision=true
public class OBJ_Door extends Entity {

  public static final String objName = "Porta";

  public OBJ_Door(GamePanel gp) {
    super(gp);

    setType(TYPE_OBSTACLE);
    setName(objName);

    down1 = setup("/objects/door", gp.tileSize, gp.tileSize);
    setCollision(true);

    Rectangle sa = getSolidArea();
    sa.x = 0;
    sa.y = 16;
    sa.width = 48;
    sa.height = 32;
    setSolidAreaDefaultX(sa.x);
    setSolidAreaDefaultY(sa.y);

    setDialogue();
  }

  public void setDialogue() {
    String[][] d = getDialogues();
    d[0][0] = "Uma porta trancada. Você precisa de uma \nchave para abri-la.";
  }

  @Override
  public void interact() {
    startDialogue(this, 0);
  }
}

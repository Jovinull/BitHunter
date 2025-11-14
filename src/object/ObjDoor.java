package object;

import java.awt.Rectangle;

import entity.Entity;
import main.GamePanel;

public class ObjDoor extends Entity {

  public static final String OBJ_NAME = "Porta";

  public ObjDoor(GamePanel gp) {
    super(gp);

    setType(TYPE_OBSTACLE);
    setName(OBJ_NAME);

    down1 = setup("/objects/door", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);
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

package object;

import java.awt.Rectangle;

import entity.Entity;
import main.GamePanel;

public class OBJ_Door_Iron extends Entity {

  public static final String objName = "Porta de Ferro";

  public OBJ_Door_Iron(GamePanel gp) {
    super(gp);

    setType(TYPE_OBSTACLE);
    setName(objName);

    down1 = setup("/objects/door_iron", gp.tileSize, gp.tileSize);
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
    d[0][0] = "Isso não vai se mexer.";
  }

  @Override
  public void interact() {
    startDialogue(this, 0);
  }
}

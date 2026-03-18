package object;

import java.awt.Rectangle;

import entity.Entity;
import main.GamePanel;

public final class ObjDoorIron extends Entity {

  public static final String OBJ_ID = "item.doorIron";

  public ObjDoorIron(GamePanel gp) {
    super(gp);

    setType(TYPE_OBSTACLE);

    // Identidade e i18n
    setInternalId(OBJ_ID);
    setTranslationKey(OBJ_ID);

    // Sprite e colisão
    down1 = setup("/objects/door_iron", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);
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

  private void setDialogue() {
    String[][] d = getDialogues();
    d[0][0] = t("item.doorIron.dialog.immovable");
  }

  @Override
  public void interact() {
    startDialogue(this, 0);
  }
}

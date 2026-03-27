package object;

import entity.Entity;
import main.GamePanel;

public final class ObjGlovesIron extends Entity {

  public static final String OBJ_ID = "item.glovesIron";

  public ObjGlovesIron(GamePanel gp) {
    super(gp);

    setType(TYPE_GLOVES);
    setInternalId(OBJ_ID);
    setTranslationKey(OBJ_ID);

    down1 = setup("/objects/gloves_iron", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);

    setAttackValue(1);
    setDefenseValue(2);
    setPrice(350);
  }
}

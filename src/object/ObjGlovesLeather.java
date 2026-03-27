package object;

import entity.Entity;
import main.GamePanel;

public final class ObjGlovesLeather extends Entity {

  public static final String OBJ_ID = "item.glovesLeather";

  public ObjGlovesLeather(GamePanel gp) {
    super(gp);

    setType(TYPE_GLOVES);
    setInternalId(OBJ_ID);
    setTranslationKey(OBJ_ID);

    down1 = setup("/objects/gloves_leather", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);

    setDefenseValue(1);
    setPrice(120);
  }
}

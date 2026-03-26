package object;

import entity.Entity;
import main.GamePanel;

public final class ObjChestLeather extends Entity {

  public static final String OBJ_ID = "item.chestLeather";

  public ObjChestLeather(GamePanel gp) {
    super(gp);

    setType(TYPE_CHEST);
    setInternalId(OBJ_ID);
    setTranslationKey(OBJ_ID);

    down1 = setup("/objects/leather_chestplate", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);

    setDefenseValue(2);
    setPrice(150);
  }
}

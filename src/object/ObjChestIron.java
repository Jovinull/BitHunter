package object;

import entity.Entity;
import main.GamePanel;

public final class ObjChestIron extends Entity {

  public static final String OBJ_ID = "item.chestIron";

  public ObjChestIron(GamePanel gp) {
    super(gp);

    setType(TYPE_CHEST);
    setInternalId(OBJ_ID);
    setTranslationKey(OBJ_ID);

    down1 = setup("/objects/iron_chestplate", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);

    setDefenseValue(4);
    setPrice(450);
  }
}

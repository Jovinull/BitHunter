package object;

import entity.Entity;
import main.GamePanel;

public final class ObjChestObsidian extends Entity {

  public static final String OBJ_ID = "item.chestObsidian";

  public ObjChestObsidian(GamePanel gp) {
    super(gp);

    setType(TYPE_CHEST);
    setInternalId(OBJ_ID);
    setTranslationKey(OBJ_ID);

    down1 = setup("/objects/obsidian_chestplate", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);

    setDefenseValue(7);
    setPrice(2500);
  }
}

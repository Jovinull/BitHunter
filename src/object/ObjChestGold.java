package object;

import entity.Entity;
import main.GamePanel;

public final class ObjChestGold extends Entity {

  public static final String OBJ_ID = "item.chestGold";

  public ObjChestGold(GamePanel gp) {
    super(gp);

    setType(TYPE_CHEST);
    setInternalId(OBJ_ID);
    setTranslationKey(OBJ_ID);

    down1 = setup("/objects/gold_chestplate", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);

    setDefenseValue(5);
    setPrice(1000);
  }
}

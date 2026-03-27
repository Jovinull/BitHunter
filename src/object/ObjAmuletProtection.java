package object;

import entity.Entity;
import main.GamePanel;

public final class ObjAmuletProtection extends Entity {

  public static final String OBJ_ID = "item.amuletProtection";

  public ObjAmuletProtection(GamePanel gp) {
    super(gp);

    setType(TYPE_AMULET);
    setInternalId(OBJ_ID);
    setTranslationKey(OBJ_ID);

    down1 = setup("/objects/amulet_protection", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);

    setDefenseValue(3);
    setPrice(500);
  }
}

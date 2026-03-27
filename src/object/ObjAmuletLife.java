package object;

import entity.Entity;
import main.GamePanel;

public final class ObjAmuletLife extends Entity {

  public static final String OBJ_ID = "item.amuletLife";

  public ObjAmuletLife(GamePanel gp) {
    super(gp);

    setType(TYPE_AMULET);
    setInternalId(OBJ_ID);
    setTranslationKey(OBJ_ID);

    down1 = setup("/objects/amulet_life", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);

    setDefenseValue(1);
    setPrice(200);
  }
}

package object;

import entity.Entity;
import main.GamePanel;

public final class ObjAmuletObsidian extends Entity {

  public static final String OBJ_ID = "item.amuletObsidian";

  public ObjAmuletObsidian(GamePanel gp) {
    super(gp);

    setType(TYPE_AMULET);
    setInternalId(OBJ_ID);
    setTranslationKey(OBJ_ID);

    down1 = setup("/objects/amulet_obsidian", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);

    setAttackValue(1);
    setDefenseValue(4);
    setPrice(1200);
  }
}

package object;

import entity.Entity;
import main.GamePanel;

public final class ObjRingObsidian extends Entity {

  public static final String OBJ_ID = "item.ringObsidian";

  public ObjRingObsidian(GamePanel gp) {
    super(gp);

    setType(TYPE_RING);
    setInternalId(OBJ_ID);
    setTranslationKey(OBJ_ID);

    down1 = setup("/objects/ring_obsidian", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);

    setAttackValue(2);
    setDefenseValue(2);
    setPrice(900);
  }
}

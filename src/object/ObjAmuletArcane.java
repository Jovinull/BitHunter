package object;

import entity.Entity;
import main.GamePanel;

public final class ObjAmuletArcane extends Entity {

  public static final String OBJ_ID = "item.amuletArcane";

  public ObjAmuletArcane(GamePanel gp) {
    super(gp);

    setType(TYPE_AMULET);
    setInternalId(OBJ_ID);
    setTranslationKey(OBJ_ID);

    down1 = setup("/objects/amulet_arcane", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);

    setAttackValue(3);
    setPrice(600);
  }
}

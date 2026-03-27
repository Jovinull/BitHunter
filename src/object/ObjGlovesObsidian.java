package object;

import entity.Entity;
import main.GamePanel;

public final class ObjGlovesObsidian extends Entity {

  public static final String OBJ_ID = "item.glovesObsidian";

  public ObjGlovesObsidian(GamePanel gp) {
    super(gp);

    setType(TYPE_GLOVES);
    setInternalId(OBJ_ID);
    setTranslationKey(OBJ_ID);

    down1 = setup("/objects/gloves_obsidian", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);

    setAttackValue(3);
    setDefenseValue(5);
    setPrice(2000);
  }
}

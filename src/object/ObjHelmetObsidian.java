package object;

import entity.Entity;
import main.GamePanel;

public final class ObjHelmetObsidian extends Entity {

  public static final String OBJ_ID = "item.helmetObsidian";

  public ObjHelmetObsidian(GamePanel gp) {
    super(gp);

    setType(TYPE_HELMET);
    setInternalId(OBJ_ID);
    setTranslationKey(OBJ_ID);

    down1 = setup("/objects/obsidian_helmet", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);

    setDefenseValue(4);
    setPrice(1500);
  }
}

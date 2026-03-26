package object;

import entity.Entity;
import main.GamePanel;

public final class ObjLegsObsidian extends Entity {

  public static final String OBJ_ID = "item.legsObsidian";

  public ObjLegsObsidian(GamePanel gp) {
    super(gp);

    setType(TYPE_LEGS);
    setInternalId(OBJ_ID);
    setTranslationKey(OBJ_ID);

    down1 = setup("/objects/obsidian_leggings", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);

    setDefenseValue(4);
    setPrice(1800);
  }
}

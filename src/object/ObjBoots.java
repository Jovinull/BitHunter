package object;

import entity.Entity;
import main.GamePanel;

public final class ObjBoots extends Entity {

  public static final String OBJ_ID = "item.boots";

  public ObjBoots(GamePanel gp) {
    super(gp);

    setType(TYPE_BOOTS);
    setInternalId(OBJ_ID);
    setTranslationKey(OBJ_ID);

    down1 = setup("/objects/boots", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);

    setSpeedValue(1);
    setDefenseValue(1);
    setPrice(400);
  }
}

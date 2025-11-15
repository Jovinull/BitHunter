package object;

import entity.Entity;
import main.GamePanel;

public class ObjShieldWood extends Entity {

  public static final String OBJ_ID = "item.shieldWood";

  public ObjShieldWood(GamePanel gp) {
    super(gp);

    setType(TYPE_SHIELD);
    setInternalId(OBJ_ID);
    setTranslationKey(OBJ_ID);

    down1 = setup("/objects/shield_wood", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);

    setDefenseValue(1);
    setPrice(35);
  }
}

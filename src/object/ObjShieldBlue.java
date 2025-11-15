package object;

import entity.Entity;
import main.GamePanel;

public class ObjShieldBlue extends Entity {

  public static final String OBJ_ID = "item.shieldBlue";

  public ObjShieldBlue(GamePanel gp) {
    super(gp);

    setType(TYPE_SHIELD);
    setInternalId(OBJ_ID);
    setTranslationKey(OBJ_ID);

    down1 = setup("/objects/shield_blue", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);

    setDefenseValue(2);
    setPrice(250);
  }
}

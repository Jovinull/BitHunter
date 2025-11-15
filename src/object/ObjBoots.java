package object;

import entity.Entity;
import main.GamePanel;

public class ObjBoots extends Entity {

  public static final String OBJ_ID = "item.boots";

  public ObjBoots(GamePanel gp) {
    super(gp);

    setType(TYPE_BOOTS);
    setInternalId(OBJ_ID);
    setTranslationKey(OBJ_ID);
    setName(OBJ_ID);

    down1 = setup("/objects/boots", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);

    setSpeedValue(2);
    setDefenseValue(2);
    setPrice(720);
  }
}

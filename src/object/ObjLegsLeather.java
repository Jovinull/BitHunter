package object;

import entity.Entity;
import main.GamePanel;

public final class ObjLegsLeather extends Entity {

  public static final String OBJ_ID = "item.legsLeather";

  public ObjLegsLeather(GamePanel gp) {
    super(gp);

    setType(TYPE_LEGS);
    setInternalId(OBJ_ID);
    setTranslationKey(OBJ_ID);

    down1 = setup("/objects/leather_leggings", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);

    setDefenseValue(1);
    setPrice(100);
  }
}

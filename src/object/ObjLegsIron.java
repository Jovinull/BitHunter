package object;

import entity.Entity;
import main.GamePanel;

public final class ObjLegsIron extends Entity {

  public static final String OBJ_ID = "item.legsIron";

  public ObjLegsIron(GamePanel gp) {
    super(gp);

    setType(TYPE_LEGS);
    setInternalId(OBJ_ID);
    setTranslationKey(OBJ_ID);

    down1 = setup("/objects/iron_leggings", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);

    setDefenseValue(2);
    setPrice(300);
  }
}

package object;

import entity.Entity;
import main.GamePanel;

public final class ObjLegsGold extends Entity {

  public static final String OBJ_ID = "item.legsGold";

  public ObjLegsGold(GamePanel gp) {
    super(gp);

    setType(TYPE_LEGS);
    setInternalId(OBJ_ID);
    setTranslationKey(OBJ_ID);

    down1 = setup("/objects/gold_leggings", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);

    setDefenseValue(3);
    setPrice(700);
  }
}

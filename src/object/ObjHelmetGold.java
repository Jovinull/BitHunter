package object;

import entity.Entity;
import main.GamePanel;

public final class ObjHelmetGold extends Entity {

  public static final String OBJ_ID = "item.helmetGold";

  public ObjHelmetGold(GamePanel gp) {
    super(gp);

    setType(TYPE_HELMET);
    setInternalId(OBJ_ID);
    setTranslationKey(OBJ_ID);

    down1 = setup("/objects/gold_helmet", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);

    setDefenseValue(3);
    setPrice(600);
  }
}

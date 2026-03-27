package object;

import entity.Entity;
import main.GamePanel;

public final class ObjGlovesGold extends Entity {

  public static final String OBJ_ID = "item.glovesGold";

  public ObjGlovesGold(GamePanel gp) {
    super(gp);

    setType(TYPE_GLOVES);
    setInternalId(OBJ_ID);
    setTranslationKey(OBJ_ID);

    down1 = setup("/objects/gloves_gold", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);

    setAttackValue(2);
    setDefenseValue(3);
    setPrice(750);
  }
}

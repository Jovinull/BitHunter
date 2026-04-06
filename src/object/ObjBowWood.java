package object;

import entity.Entity;
import main.GamePanel;

public final class ObjBowWood extends Entity {

  public static final String OBJ_ID = "item.bowWood";

  public ObjBowWood(GamePanel gp) {
    super(gp);

    setType(TYPE_BOW);
    setInternalId(OBJ_ID);
    setTranslationKey(OBJ_ID);

    down1 = setup("/objects/bow_wood", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);

    setAttackValue(2);
    getAttackArea().width = 36;
    getAttackArea().height = 36;

    setPrice(100);
    setKnockBackPower(1);

    setMotion1Duration(15);
    setMotion2Duration(40);
  }
}

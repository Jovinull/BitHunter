package object;

import entity.Entity;
import main.GamePanel;

public final class ObjBowElven extends Entity {

  public static final String OBJ_ID = "item.bowElven";

  public ObjBowElven(GamePanel gp) {
    super(gp);

    setType(TYPE_BOW);
    setInternalId(OBJ_ID);
    setTranslationKey(OBJ_ID);

    down1 = setup("/objects/bow_elven", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);

    setAttackValue(6);
    getAttackArea().width = 36;
    getAttackArea().height = 36;

    setPrice(800);
    setKnockBackPower(3);

    setMotion1Duration(8);
    setMotion2Duration(28);
  }
}

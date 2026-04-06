package object;

import entity.Entity;
import main.GamePanel;

public final class ObjBowHunter extends Entity {

  public static final String OBJ_ID = "item.bowHunter";

  public ObjBowHunter(GamePanel gp) {
    super(gp);

    setType(TYPE_BOW);
    setInternalId(OBJ_ID);
    setTranslationKey(OBJ_ID);

    down1 = setup("/objects/bow_hunter", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);

    setAttackValue(4);
    getAttackArea().width = 36;
    getAttackArea().height = 36;

    setPrice(400);
    setKnockBackPower(2);

    setMotion1Duration(12);
    setMotion2Duration(35);
  }
}

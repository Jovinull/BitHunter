package object;

import entity.Entity;
import main.GamePanel;

public final class ObjBowObsidian extends Entity {

  public static final String OBJ_ID = "item.bowObsidian";

  public ObjBowObsidian(GamePanel gp) {
    super(gp);

    setType(TYPE_BOW);
    setInternalId(OBJ_ID);
    setTranslationKey(OBJ_ID);

    down1 = setup("/objects/bow_obsidian", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);

    setAttackValue(8);
    getAttackArea().width = 36;
    getAttackArea().height = 36;

    setPrice(1500);
    setKnockBackPower(4);

    setMotion1Duration(18);
    setMotion2Duration(48);
  }
}

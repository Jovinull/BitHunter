package object;

import entity.Entity;
import main.GamePanel;

public final class ObjStaffObsidian extends Entity {

  public static final String OBJ_ID = "item.staffObsidian";

  public ObjStaffObsidian(GamePanel gp) {
    super(gp);

    setType(TYPE_STAFF);
    setInternalId(OBJ_ID);
    setTranslationKey(OBJ_ID);

    down1 = setup("/objects/staff_obsidian", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);

    setAttackValue(8);
    getAttackArea().width = 36;
    getAttackArea().height = 36;

    setPrice(1500);
    setKnockBackPower(4);

    setMotion1Duration(5);
    setMotion2Duration(25);
  }
}

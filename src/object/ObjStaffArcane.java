package object;

import entity.Entity;
import main.GamePanel;

public final class ObjStaffArcane extends Entity {

  public static final String OBJ_ID = "item.staffArcane";

  public ObjStaffArcane(GamePanel gp) {
    super(gp);

    setType(TYPE_STAFF);
    setInternalId(OBJ_ID);
    setTranslationKey(OBJ_ID);

    down1 = setup("/objects/staff_arcane", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);

    setAttackValue(6);
    getAttackArea().width = 36;
    getAttackArea().height = 36;

    setPrice(600);
    setKnockBackPower(3);

    setMotion1Duration(5);
    setMotion2Duration(25);
  }
}

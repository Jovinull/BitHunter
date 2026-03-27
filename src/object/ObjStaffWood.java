package object;

import entity.Entity;
import main.GamePanel;

public final class ObjStaffWood extends Entity {

  public static final String OBJ_ID = "item.staffWood";

  public ObjStaffWood(GamePanel gp) {
    super(gp);

    setType(TYPE_STAFF);
    setInternalId(OBJ_ID);
    setTranslationKey(OBJ_ID);

    down1 = setup("/objects/staff_wood", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);

    setAttackValue(2);
    getAttackArea().width = 36;
    getAttackArea().height = 36;

    setPrice(80);
    setKnockBackPower(1);

    setMotion1Duration(5);
    setMotion2Duration(25);
  }
}

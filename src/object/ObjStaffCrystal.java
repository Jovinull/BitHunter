package object;

import entity.Entity;
import main.GamePanel;

public final class ObjStaffCrystal extends Entity {

  public static final String OBJ_ID = "item.staffCrystal";

  public ObjStaffCrystal(GamePanel gp) {
    super(gp);

    setType(TYPE_STAFF);
    setInternalId(OBJ_ID);
    setTranslationKey(OBJ_ID);

    down1 = setup("/objects/staff_crystal", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);

    setAttackValue(4);
    getAttackArea().width = 36;
    getAttackArea().height = 36;

    setPrice(250);
    setKnockBackPower(2);

    setMotion1Duration(5);
    setMotion2Duration(25);
  }
}

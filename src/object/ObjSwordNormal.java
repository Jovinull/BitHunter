package object;

import entity.Entity;
import main.GamePanel;

public class ObjSwordNormal extends Entity {

  public static final String OBJ_ID = "item.swordNormal";

  public ObjSwordNormal(GamePanel gp) {
    super(gp);

    setType(TYPE_SWORD);
    setInternalId(OBJ_ID);
    setTranslationKey(OBJ_ID);

    down1 = setup("/objects/sword_normal", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);

    setAttackValue(1);
    getAttackArea().width = 36;
    getAttackArea().height = 36;

    setPrice(20);
    setKnockBackPower(2);

    setMotion1Duration(5);
    setMotion2Duration(25);
  }
}

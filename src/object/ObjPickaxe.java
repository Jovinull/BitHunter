package object;

import entity.Entity;
import main.GamePanel;

public class ObjPickaxe extends Entity {

  public static final String OBJ_ID = "item.pickaxe";

  public ObjPickaxe(GamePanel gp) {
    super(gp);

    setType(TYPE_PICKAXE);
    setInternalId(OBJ_ID);
    setTranslationKey(OBJ_ID);

    down1 = setup("/objects/pickaxe", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);

    setAttackValue(2);
    getAttackArea().width = 30;
    getAttackArea().height = 30;

    setPrice(75);
    setKnockBackPower(10);

    setMotion1Duration(10);
    setMotion2Duration(20);
  }
}

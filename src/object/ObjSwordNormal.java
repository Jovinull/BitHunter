package object;

import entity.Entity;
import main.GamePanel;

public class ObjSwordNormal extends Entity {

  public static final String OBJ_NAME = "Espada Comum";

  public ObjSwordNormal(GamePanel gp) {
    super(gp);

    setType(TYPE_SWORD);
    setName(OBJ_NAME);

    down1 = setup("/objects/sword_normal", gp.tileSize, gp.tileSize);

    setAttackValue(1);
    getAttackArea().width = 36;
    getAttackArea().height = 36;

    setDescription("[" + getName() + "]\nUma espada antiga.");
    setPrice(20);
    setKnockBackPower(2);

    setMotion1Duration(5);
    setMotion2Duration(25);
  }
}

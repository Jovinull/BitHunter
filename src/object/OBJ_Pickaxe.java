package object;

import entity.Entity;
import main.GamePanel;

public class OBJ_Pickaxe extends Entity {

  public static final String objName = "Picareta";

  public OBJ_Pickaxe(GamePanel gp) {
    super(gp);

    setType(TYPE_PICKAXE);
    setName(objName);

    down1 = setup("/objects/pickaxe", gp.tileSize, gp.tileSize);

    setAttackValue(2);
    getAttackArea().width = 30;
    getAttackArea().height = 30;

    setDescription("[Picareta]\nVocê vai adorar cavar \ncom isso!.");
    setPrice(75);
    setKnockBackPower(10);

    setMotion1Duration(10);
    setMotion2Duration(20);
  }
}

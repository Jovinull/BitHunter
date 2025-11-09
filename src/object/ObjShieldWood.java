package object;

import entity.Entity;
import main.GamePanel;

public class ObjShieldWood extends Entity {

  public static final String OBJ_NAME = "Escudo de Madeira";

  public ObjShieldWood(GamePanel gp) {
    super(gp);

    setType(TYPE_SHIELD);
    setName(OBJ_NAME);

    down1 = setup("/objects/shield_wood", gp.tileSize, gp.tileSize);

    setDefenseValue(1);
    setDescription("[" + getName() + "]\nUm escudo simples de \nmadeira.");
    setPrice(35);
  }
}

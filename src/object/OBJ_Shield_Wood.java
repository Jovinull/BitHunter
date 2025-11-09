package object;

import entity.Entity;
import main.GamePanel;

public class OBJ_Shield_Wood extends Entity {

  public static final String objName = "Escudo de Madeira";

  public OBJ_Shield_Wood(GamePanel gp) {
    super(gp);

    setType(TYPE_SHIELD);
    setName(objName);

    down1 = setup("/objects/shield_wood", gp.tileSize, gp.tileSize);

    setDefenseValue(1);
    setDescription("[" + getName() + "]\nUm escudo simples de \nmadeira.");
    setPrice(35);
  }
}

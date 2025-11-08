package object;

import entity.Entity;
import main.GamePanel;

public class OBJ_Shield_Wood extends Entity {

  public static final String objName = "Escudo de Madeira";

  public OBJ_Shield_Wood(GamePanel gp) {
    super(gp);

    type = TYPE_SHIELD;
    name = objName;
    down1 = setup("/objects/shield_wood", gp.tileSize, gp.tileSize);
    defenseValue = 1;
    description = "[" + name + "]\nUm escudo simples de \nmadeira.";
    price = 35;
  }
}

package object;

import entity.Entity;
import main.GamePanel;

public class OBJ_Shield_Blue extends Entity {

  public static final String objName = "Escudo Azul";

  public OBJ_Shield_Blue(GamePanel gp) {
    super(gp);

    setType(TYPE_SHIELD);
    setName(objName);

    down1 = setup("/objects/shield_blue", gp.tileSize, gp.tileSize);

    setDefenseValue(2);
    setDescription("[" + getName() + "]\nUm escudo azul reluzente.");
    setPrice(250);
  }
}

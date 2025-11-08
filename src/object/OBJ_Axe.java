package object;

import entity.Entity;
import main.GamePanel;

public class OBJ_Axe extends Entity {

  public static final String objName = "Machado de Lenhador";

  public OBJ_Axe(GamePanel gp) {
    super(gp);

    type = TYPE_AXE;
    name = objName;
    down1 = setup("/objects/axe", gp.tileSize, gp.tileSize);
    attackValue = 2;
    attackArea.width = 30;
    attackArea.height = 30;
    description = "[" + name + "]\nUm pouco enferrujado, \nmas ainda corta algumas \nárvores.";
    price = 75;
    knockBackPower = 10;
    motion1Duration = 20;
    motion2Duration = 40;
  }
}

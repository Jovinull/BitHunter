package object;

import entity.Entity;
import main.GamePanel;

public class OBJ_Sword_Normal extends Entity {

  public static final String objName = "Espada Comum";

  public OBJ_Sword_Normal(GamePanel gp) {
    super(gp);

    type = TYPE_SWORD;
    name = objName;
    down1 = setup("/objects/sword_normal", gp.tileSize, gp.tileSize);
    attackValue = 1;
    attackArea.width = 36;
    attackArea.height = 36;
    description = "[" + name + "]\nUma espada antiga.";
    price = 20;
    knockBackPower = 2;
    motion1Duration = 5;
    motion2Duration = 25;
  }
}

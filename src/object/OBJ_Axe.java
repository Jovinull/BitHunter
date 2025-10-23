package object;

import entity.Entity;
import main.GamePanel;

public class OBJ_Axe extends Entity {

  public OBJ_Axe(GamePanel gp) {
    super(gp);

    type = type_axe;
    name = "Machado de Lenhador";
    down1 = setup("/objects/axe", gp.tileSize, gp.tileSize);
    attackValue = 2;
    attackArea.width = 30;
    attackArea.height = 30;
    description = "[Machado de Lenhador]\nUm pouco enferrujado, mas ainda\ncorta algumas árvores.";
    price = 75;
    knockBackPower = 10;
  }
}

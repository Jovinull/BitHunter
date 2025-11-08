package object;

import entity.Entity;
import main.GamePanel;

public class OBJ_Pickaxe extends Entity {

  public static final String objName = "Picareta";

  public OBJ_Pickaxe(GamePanel gp) {
    super(gp);

    type = TYPE_PICKAXE;
    name = objName;
    down1 = setup("/objects/pickaxe", gp.tileSize, gp.tileSize);
    attackValue = 2;
    attackArea.width = 30;
    attackArea.height = 30;
    description = "[Picareta]\nVocê vai adorar cavar \ncom isso!.";
    price = 75;
    knockBackPower = 10;
    motion1Duration = 10;
    motion2Duration = 20;
  }
}

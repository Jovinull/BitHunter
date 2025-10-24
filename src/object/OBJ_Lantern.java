package object;

import entity.Entity;
import main.GamePanel;

public class OBJ_Lantern extends Entity {

  public OBJ_Lantern(GamePanel gp) {
    super(gp);

    type = type_light;
    name = "Lanterna";
    down1 = setup("/objects/lantern", gp.tileSize, gp.tileSize);
    description = "[" + name + "]\nIlumina seus arredores.";
    price = 200;
    lightRadius = 250;
  }
}

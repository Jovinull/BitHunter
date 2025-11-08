package object;

import entity.Entity;
import main.GamePanel;

public class OBJ_Lantern extends Entity {

  public static final String objName = "Lanterna";

  public OBJ_Lantern(GamePanel gp) {
    super(gp);

    setType(TYPE_LIGHT);
    setName(objName);
    down1 = setup("/objects/lantern", gp.tileSize, gp.tileSize);
    setDescription("[" + getName() + "]\nIlumina seus arredores.");
    setPrice(200);
    setLightRadius(250);
  }
}

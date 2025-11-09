package object;

import entity.Entity;
import main.GamePanel;

public class ObjLantern extends Entity {

  public static final String OBJ_NAME = "Lanterna";

  public ObjLantern(GamePanel gp) {
    super(gp);

    setType(TYPE_LIGHT);
    setName(OBJ_NAME);
    down1 = setup("/objects/lantern", gp.tileSize, gp.tileSize);
    setDescription("[" + getName() + "]\nIlumina seus arredores.");
    setPrice(200);
    setLightRadius(250);
  }
}

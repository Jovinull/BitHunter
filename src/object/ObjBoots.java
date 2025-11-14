package object;

import entity.Entity;
import main.GamePanel;

public class ObjBoots extends Entity {

  public static final String OBJ_NAME = "Botas";

  public ObjBoots(GamePanel gp) {
    super(gp);

    setName(OBJ_NAME);
    down1 = setup("/objects/boots", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);
  }
}

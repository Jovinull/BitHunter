package object;

import entity.Entity;
import main.GamePanel;

public class ObjBoots extends Entity {

  public static final String OBJ_NAME = "Botas";

  public ObjBoots(GamePanel gp) {
    super(gp);

    setType(TYPE_BOOTS);
    setName(OBJ_NAME);

    down1 = setup("/objects/boots", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);

    setSpeedValue(2);

    setDescription("[" + getName() + "]\nBotas leves que \naumentam a velocidade \nem +2.");
    setPrice(20);
  }
}

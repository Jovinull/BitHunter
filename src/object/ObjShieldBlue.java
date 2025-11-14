package object;

import entity.Entity;
import main.GamePanel;

public class ObjShieldBlue extends Entity {

  public static final String OBJ_NAME = "Escudo Azul";

  public ObjShieldBlue(GamePanel gp) {
    super(gp);

    setType(TYPE_SHIELD);
    setName(OBJ_NAME);

    down1 = setup("/objects/shield_blue", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);

    setDefenseValue(2);
    setDescription("[" + getName() + "]\nUm escudo azul reluzente.");
    setPrice(250);
  }
}

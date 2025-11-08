package object;

import entity.Entity;
import main.GamePanel;

// Representa as botas coletáveis no jogo (herda de Entity)
public class OBJ_Boots extends Entity {

  public static final String objName = "Botas";

  public OBJ_Boots(GamePanel gp) {
    super(gp);

    setName(objName);
    down1 = setup("/objects/boots", gp.tileSize, gp.tileSize);
  }
}

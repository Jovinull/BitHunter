package object;

import entity.Entity;
import main.GamePanel;

public class OBJ_Coin_Bronze extends Entity {

  GamePanel gp;
  public static final String objName = "Moeda de Bronze";

  public OBJ_Coin_Bronze(GamePanel gp) {
    super(gp);
    this.gp = gp;

    type = TYPE_PICKUP_ONLY;
    name = objName;
    value = 1;
    down1 = setup("/objects/coin_bronze", gp.tileSize, gp.tileSize);
  }

  public boolean use(Entity entity) {

    gp.playSE(1);
    gp.ui.addMessage("Moedas +" + value);
    gp.player.coin += value;
    return true;
  }
}

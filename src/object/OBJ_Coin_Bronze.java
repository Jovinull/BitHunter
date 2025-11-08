package object;

import entity.Entity;
import main.GamePanel;

public class OBJ_Coin_Bronze extends Entity {

  public static final String objName = "Moeda de Bronze";

  public OBJ_Coin_Bronze(GamePanel gp) {
    super(gp);

    setType(TYPE_PICKUP_ONLY);
    setName(objName);
    setValue(1);
    down1 = setup("/objects/coin_bronze", gp.tileSize, gp.tileSize);
  }

  @Override
  public boolean use(Entity entity) {
    gp.playSE(1);
    gp.ui.addMessage("Moedas +" + getValue());
    gp.player.setCoin(gp.player.getCoin() + getValue());
    return true;
  }
}

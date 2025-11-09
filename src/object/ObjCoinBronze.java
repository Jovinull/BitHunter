package object;

import entity.Entity;
import main.GamePanel;

public class ObjCoinBronze extends Entity {

  public static final String OBJ_NAME = "Moeda de Bronze";

  public ObjCoinBronze(GamePanel gp) {
    super(gp);

    setType(TYPE_PICKUP_ONLY);
    setName(OBJ_NAME);
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

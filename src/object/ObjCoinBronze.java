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
    down1 = setup("/objects/coin_bronze", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);
  }

  @Override
  public boolean use(Entity entity) {
    gp.playSE(1);

    var ui = gp.getGameUi();
    var player = gp.getPlayer();

    ui.addMessage("Moedas +" + getValue());
    player.setCoin(player.getCoin() + getValue());
    return true;
  }
}

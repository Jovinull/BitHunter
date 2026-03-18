package object;

import entity.Entity;
import main.GamePanel;

public final class ObjCoinBronze extends Entity {

  public static final String OBJ_ID = "item.coin.bronze";

  public ObjCoinBronze(GamePanel gp) {
    super(gp);

    setType(TYPE_PICKUP_ONLY);
    setInternalId(OBJ_ID);
    setTranslationKey(OBJ_ID);

    setValue(1);
    down1 = setup("/objects/coin_bronze", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);
  }

  @Override
  public boolean use(Entity entity) {
    gp.playSE(1);

    var ui = gp.getGameUi();
    var player = gp.getPlayer();

    String msg = String.format(t("item.coin.bronze.pickup"), getValue());
    ui.addMessage(msg);

    player.setCoin(player.getCoin() + getValue());
    return true;
  }

}

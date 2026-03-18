package object;

import entity.Entity;
import main.GamePanel;

public final class ObjTent extends Entity {

  public static final String OBJ_ID = "item.tent";

  public ObjTent(GamePanel gp) {
    super(gp);

    setType(TYPE_CONSUMABLE);
    setInternalId(OBJ_ID);
    setTranslationKey(OBJ_ID);

    down1 = setup("/objects/tent", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);

    setPrice(300);
    setStackable(true);
  }

  @Override
  public boolean use(Entity entity) {
    gp.setGameState(GamePanel.SLEEP_STATE);
    gp.playSE(14);

    var player = gp.getPlayer();
    player.setLife(player.getMaxLife());
    player.setMana(player.getMaxMana());

    player.getSleepingImage(down1);
    return true;
  }
}

package object;

import entity.Entity;
import main.GamePanel;

public class ObjTent extends Entity {

  public static final String OBJ_NAME = "Tenda";

  public ObjTent(GamePanel gp) {
    super(gp);

    setType(TYPE_CONSUMABLE);
    setName(OBJ_NAME);

    down1 = setup("/objects/tent", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);

    String displayName = t("item.tent.displayName");
    String description = "[" + displayName + "]\n" + t("item.tent.desc");
    setDescription(description);

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

    // mantém o comportamento existente
    player.getSleepingImage(down1);
    return true;
  }

  private String t(String key) {
    return gp.getLanguageManager().text(key);
  }
}

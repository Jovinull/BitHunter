package object;

import entity.Entity;
import main.GamePanel;

public class ObjShieldWood extends Entity {

  public static final String OBJ_NAME = "Escudo de Madeira";

  public ObjShieldWood(GamePanel gp) {
    super(gp);

    setType(TYPE_SHIELD);
    setName(OBJ_NAME);

    down1 = setup("/objects/shield_wood", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);

    setDefenseValue(1);

    String displayName = t("item.shieldWood.displayName");
    String description = "[" + displayName + "]\n" + t("item.shieldWood.desc");
    setDescription(description);

    setPrice(35);
  }

  private String t(String key) {
    return gp.getLanguageManager().text(key);
  }
}

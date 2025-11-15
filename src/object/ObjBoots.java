package object;

import entity.Entity;
import main.GamePanel;

public class ObjBoots extends Entity {

  public static final String OBJ_NAME = "Botas";

  public ObjBoots(GamePanel gp) {
    super(gp);

    setType(TYPE_BOOTS);
    setName(OBJ_NAME);

    down1 = setup("/objects/boots", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);

    setSpeedValue(2);
    setDefenseValue(2);

    // descrição com i18n
    String displayName = t("item.boots.displayName");
    String description = "[" + displayName + "]\n" + t("item.boots.desc");
    setDescription(description);

    setPrice(720);
  }

  private String t(String key) {
    return gp.getLanguageManager().text(key);
  }
}

package object;

import entity.Entity;
import main.GamePanel;

public class ObjLantern extends Entity {

  public static final String OBJ_NAME = "Lanterna";

  public ObjLantern(GamePanel gp) {
    super(gp);

    setType(TYPE_LIGHT);
    setName(OBJ_NAME);
    down1 = setup("/objects/lantern", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);

    String displayName = t("item.lantern.displayName");
    String description = "[" + displayName + "]\n" + t("item.lantern.desc");
    setDescription(description);

    setPrice(200);
    setLightRadius(250);
  }

  private String t(String key) {
    return gp.getLanguageManager().text(key);
  }
}

package object;

import entity.Entity;
import main.GamePanel;

public final class ObjLantern extends Entity {

  public static final String OBJ_ID = "item.lantern";

  public ObjLantern(GamePanel gp) {
    super(gp);

    setType(TYPE_LIGHT);
    setInternalId(OBJ_ID);
    setTranslationKey(OBJ_ID);

    down1 = setup("/objects/lantern", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);

    setPrice(200);
    setLightRadius(250);
  }
}

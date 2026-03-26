package object;

import entity.Entity;
import main.GamePanel;

public final class ObjHelmetLeather extends Entity {

  public static final String OBJ_ID = "item.helmetLeather";

  public ObjHelmetLeather(GamePanel gp) {
    super(gp);

    setType(TYPE_HELMET);
    setInternalId(OBJ_ID);
    setTranslationKey(OBJ_ID);

    down1 = setup("/objects/leather_helmet", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);

    setDefenseValue(1);
    setPrice(80);
  }
}

package object;

import entity.Entity;
import main.GamePanel;

public final class ObjHelmetIron extends Entity {

  public static final String OBJ_ID = "item.helmetIron";

  public ObjHelmetIron(GamePanel gp) {
    super(gp);

    setType(TYPE_HELMET);
    setInternalId(OBJ_ID);
    setTranslationKey(OBJ_ID);

    down1 = setup("/objects/iron_helmet", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);

    setDefenseValue(2);
    setPrice(250);
  }
}

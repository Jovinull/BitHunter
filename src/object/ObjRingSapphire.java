package object;

import entity.Entity;
import main.GamePanel;

public final class ObjRingSapphire extends Entity {

  public static final String OBJ_ID = "item.ringSapphire";

  public ObjRingSapphire(GamePanel gp) {
    super(gp);

    setType(TYPE_RING);
    setInternalId(OBJ_ID);
    setTranslationKey(OBJ_ID);

    down1 = setup("/objects/ring_sapphire", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);

    setDefenseValue(2);
    setPrice(300);
  }
}

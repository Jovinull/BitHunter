package object;

import entity.Entity;
import main.GamePanel;

public final class ObjRingRuby extends Entity {

  public static final String OBJ_ID = "item.ringRuby";

  public ObjRingRuby(GamePanel gp) {
    super(gp);

    setType(TYPE_RING);
    setInternalId(OBJ_ID);
    setTranslationKey(OBJ_ID);

    down1 = setup("/objects/ring_ruby", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);

    setAttackValue(2);
    setPrice(350);
  }
}

package object;

import entity.Entity;
import main.GamePanel;

public final class ObjRingEmerald extends Entity {

  public static final String OBJ_ID = "item.ringEmerald";

  public ObjRingEmerald(GamePanel gp) {
    super(gp);

    setType(TYPE_RING);
    setInternalId(OBJ_ID);
    setTranslationKey(OBJ_ID);

    down1 = setup("/objects/ring_emerald", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);

    setAttackValue(1);
    setDefenseValue(1);
    setPrice(320);
  }
}

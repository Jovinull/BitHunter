package pets;

import main.GamePanel;

/**
 * Ovo do Caracol Amarelo.
 * Sprite: /eggs/yellow_snail_egg.png
 */
public final class YellowSnailEgg extends PetEgg {

  public static final String OBJ_ID = "egg.yellowSnail";

  private static final int HATCH_TIME_SECONDS = 600;
  private static final int HATCH_TIME_TICKS = HATCH_TIME_SECONDS * 60;
  private static final int HATCH_COST = 400;

  public YellowSnailEgg(GamePanel gp) {
    super(gp, PetFactory.PET_YELLOW_SNAIL, HATCH_TIME_TICKS, HATCH_COST);

    setInternalId(OBJ_ID);
    setTranslationKey(OBJ_ID);

    int size = GamePanel.TILE_SIZE;
    var eggImg = setup("/eggs/yellow_snail_egg", size, size);

    down1 = eggImg;
    up1 = eggImg;
    left1 = eggImg;
    right1 = eggImg;
  }
}

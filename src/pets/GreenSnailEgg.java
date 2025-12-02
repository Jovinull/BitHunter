package pets;

import main.GamePanel;

/**
 * Ovo do Caracol Verde.
 * Sprite: /eggs/green_snail_egg.png
 */
public class GreenSnailEgg extends PetEgg {

  public static final String OBJ_ID = "egg.greenSnail";

  private static final int HATCH_TIME_SECONDS = 600;
  private static final int HATCH_TIME_TICKS = HATCH_TIME_SECONDS * 60;
  private static final int HATCH_COST = 400;

  public GreenSnailEgg(GamePanel gp) {
    super(gp, PetFactory.PET_GREEN_SNAIL, HATCH_TIME_TICKS, HATCH_COST);

    setInternalId(OBJ_ID);
    setTranslationKey(OBJ_ID);

    int size = GamePanel.TILE_SIZE;
    var eggImg = setup("/eggs/green_snail_egg", size, size);

    down1 = eggImg;
    up1 = eggImg;
    left1 = eggImg;
    right1 = eggImg;
  }
}

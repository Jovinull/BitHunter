package pets;

import main.GamePanel;

/**
 * Ovo do Caracol Verde.
 * Sprite: /eggs/green_snail_egg.png
 */
public class GreenSnailEgg extends PetEgg {

  // 20 segundos para chocar, a 60 FPS
  private static final int HATCH_TIME_SECONDS = 2;
  private static final int HATCH_TIME_TICKS = HATCH_TIME_SECONDS * 60;
  private static final int HATCH_COST = 80;

  public GreenSnailEgg(GamePanel gp) {
    super(gp, PetFactory.PET_GREEN_SNAIL, HATCH_TIME_TICKS, HATCH_COST);

    setInternalId("egg.greenSnail");
    setTranslationKey("egg.greenSnail");

    int size = GamePanel.TILE_SIZE;
    var eggImg = setup("/eggs/green_snail_egg", size, size);

    down1 = eggImg;
    up1 = eggImg;
    left1 = eggImg;
    right1 = eggImg;
  }
}

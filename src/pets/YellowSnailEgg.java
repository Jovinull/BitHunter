package pets;

import main.GamePanel;

/**
 * Ovo do Caracol Amarelo.
 * Sprite: /eggs/yellow_snail_egg.png
 */
public class YellowSnailEgg extends PetEgg {

  // 20 segundos para chocar, a 60 FPS
  private static final int HATCH_TIME_SECONDS = 20;
  private static final int HATCH_TIME_TICKS = HATCH_TIME_SECONDS * 60;
  private static final int HATCH_COST = 80;

  public YellowSnailEgg(GamePanel gp) {
    super(gp, PetFactory.PET_YELLOW_SNAIL, HATCH_TIME_TICKS, HATCH_COST);

    setInternalId("egg.yellowSnail");
    setTranslationKey("egg.yellowSnail");

    int size = GamePanel.TILE_SIZE;
    var eggImg = setup("/eggs/yellow_snail_egg", size, size);

    down1 = eggImg;
    up1 = eggImg;
    left1 = eggImg;
    right1 = eggImg;
  }
}

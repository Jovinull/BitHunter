package pets;

import entity.Player;
import main.GamePanel;

/**
 * Yellow Snail:
 * - Segue o player.
 * - Cura +1 de HP periodicamente (buff de regeneração simples).
 */
public class YellowSnail extends PetEntity {

  private static final int BASE_SPEED = 4;

  // "Geração" de vida: 1 de HP a cada X ticks
  // 1800 frames ~ 30 segundos em 60 FPS (ajusta depois se quiser)
  private static final int HEAL_INTERVAL_TICKS = 1800;
  private static final int HEAL_AMOUNT = 1;

  private int healCounter = 0;

  public YellowSnail(GamePanel gp, Player owner) {
    super(gp, owner);

    setInternalId("pet.yellowSnail");
    setTranslationKey("pet.yellowSnail");

    setSpeed(BASE_SPEED);
    setDefaultSpeed(BASE_SPEED);

    getImage();
  }

  @Override
  public void getImage() {
    int size = GamePanel.TILE_SIZE;

    // Dois frames de animação
    var frame1 = setup("/pets/Yellow Snail/yellow_snail_1", size, size);
    var frame2 = setup("/pets/Yellow Snail/yellow_snail_2", size, size);

    // Frame 1 (spriteNum == 1)
    up1 = frame1;
    down1 = frame1;
    left1 = frame1;
    right1 = frame1;

    // Frame 2 (spriteNum == 2)
    up2 = frame2;
    down2 = frame2;
    left2 = frame2;
    right2 = frame2;
  }

  @Override
  protected void applyPassiveEffect() {
    var player = getOwner();

    if (player.getLife() >= player.getMaxLife()) {
      // vida cheia, não acumula contador
      healCounter = 0;
      return;
    }

    healCounter++;
    if (healCounter >= HEAL_INTERVAL_TICKS) {
      healCounter = 0;
      int newLife = Math.min(player.getMaxLife(), player.getLife() + HEAL_AMOUNT);
      player.setLife(newLife);
    }
  }
}

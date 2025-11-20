package pets;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import entity.Player;
import main.GamePanel;

/**
 * Yellow Snail:
 * - Segue o player.
 * - Cura periodicamente.
 * - Ao curar: gera partículas verdes, brilha e mostra mensagem no log.
 */
public class YellowSnail extends PetEntity {

  private static final int BASE_SPEED = 4;

  // Cura: 1 de HP a cada X ticks
  private static final int HEAL_INTERVAL_TICKS = 1800;
  private static final int HEAL_AMOUNT = 1;

  // Duração do brilho verde (frames)
  private static final int HEAL_GLOW_DURATION_TICKS = 30;

  // Partículas de cura (cor e parâmetros)
  private static final Color HEAL_PARTICLE_COLOR = new Color(80, 255, 120);

  private int healCounter = 0;
  private int healGlowCounter = 0;

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

    var frame1 = setup("/pets/Yellow Snail/yellow_snail_1", size, size);
    var frame2 = setup("/pets/Yellow Snail/yellow_snail_2", size, size);

    // Frame 1
    up1 = frame1;
    down1 = frame1;
    left1 = frame1;
    right1 = frame1;

    // Frame 2
    up2 = frame2;
    down2 = frame2;
    left2 = frame2;
    right2 = frame2;
  }

  @Override
  public void update() {
    // segue o comportamento padrão do Pet (seguir dono, animar, etc.)
    super.update();

    // Conta decrescente do brilho verde
    if (healGlowCounter > 0) {
      healGlowCounter--;
    }
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
    if (healCounter < HEAL_INTERVAL_TICKS) {
      return;
    }

    healCounter = 0;

    int currentLife = player.getLife();
    int maxLife = player.getMaxLife();

    int effectiveHeal = Math.min(HEAL_AMOUNT, maxLife - currentLife);
    if (effectiveHeal <= 0) {
      return;
    }

    // Aplica cura
    player.setLife(currentLife + effectiveHeal);

    // Brilho verde por alguns frames
    healGlowCounter = HEAL_GLOW_DURATION_TICKS;

    // Mensagem no log (genérica, mas usando o nome do pet)
    String petName = getDisplayName();
    String msg = String.format(t("pet.message.heal"), petName, effectiveHeal);
    gp.getGameUi().addMessage(msg);
  }

  // -------- Partículas específicas da YellowSnail --------

  @Override
  public Color getParticleColor() {
    return HEAL_PARTICLE_COLOR;
  }

  @Override
  public int getParticleSize() {
    return 8;
  }

  @Override
  public int getParticleSpeed() {
    return 1;
  }

  @Override
  public int getParticleMaxLife() {
    return 20;
  }

  // -------- Desenho com brilho verde quando cura --------

  @Override
  public void draw(Graphics2D g2) {
    if (!inCamera()) {
      return;
    }

    int screenX = getScreenX();
    int screenY = getScreenY();

    BufferedImage frame = resolveFrame();

    if (!isDrawing() || frame == null) {
      return;
    }

    // Halo verde quando a habilidade acabou de ser usada
    if (healGlowCounter > 0) {
      int padding = 4;
      int haloX = screenX - padding;
      int haloY = screenY - padding;
      int haloSize = GamePanel.TILE_SIZE + padding * 2;

      g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
      g2.setColor(new Color(80, 255, 120, 180));
      g2.fillOval(haloX, haloY, haloSize, haloSize);
      g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
    }

    // Desenha o sprite normalmente
    g2.drawImage(frame, screenX, screenY, null);
  }

  private BufferedImage resolveFrame() {
    boolean first = getSpriteNum() == 1;
    String dir = getDirection();
    if (dir == null) {
      dir = DIR_DOWN;
    }

    BufferedImage up = first ? up1 : up2;
    BufferedImage down = first ? down1 : down2;
    BufferedImage left = first ? left1 : left2;
    BufferedImage right = first ? right1 : right2;

    return switch (dir) {
      case DIR_UP -> up;
      case DIR_LEFT -> left;
      case DIR_RIGHT -> right;
      case DIR_DOWN -> down;
      default -> down;
    };
  }
}

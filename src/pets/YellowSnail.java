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
public final class YellowSnail extends PetEntity {

  /**
   * ID lógico estável do PET.
   *
   * Este ID é usado como:
   * - internalId da entidade (save/load, Player, DataStorage)
   * - chave em PetFactory.PET_YELLOW_SNAIL
   * - chave em ownedPets / petLevels / petExps
   */
  public static final String ID = "pet.yellowSnail";

  private static final int BASE_SPEED = 4;

  // Intervalo base e mínimo (em ticks)
  private static final int BASE_HEAL_INTERVAL_TICKS = 60 * 60; // 60s no nível 1
  private static final int MIN_HEAL_INTERVAL_TICKS = 15 * 60; // 15s no nível máximo

  // Cura base por proc
  private static final int BASE_HEAL_AMOUNT = 1;

  // Duração do brilho verde (frames)
  private static final int HEAL_GLOW_DURATION_TICKS = 30;

  // Partículas de cura (cor e parâmetros)
  private static final Color HEAL_PARTICLE_COLOR = new Color(80, 255, 120);

  private int healCounter = 0;
  private int healGlowCounter = 0;

  public YellowSnail(GamePanel gp, Player owner) {
    super(gp, owner);

    // Usa sempre a constante ID, nada de string solta
    setInternalId(ID);
    setTranslationKey(ID);

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
    int interval = getEffectiveHealIntervalTicks();
    if (healCounter < interval) {
      return;
    }

    healCounter = 0;

    int currentLife = player.getLife();
    int maxLife = player.getMaxLife();

    int healAmount = getEffectiveHealAmount();
    int effectiveHeal = Math.min(healAmount, maxLife - currentLife);
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

  /**
   * Intervalo efetivo da cura, dependente do nível do pet (em ticks).
   *
   * Exposto publicamente para a UI mostrar os valores.
   */
  public int getEffectiveHealIntervalTicks() {
    int level = getPetLevel();
    int maxLvl = getPetMaxLevel();

    int range = BASE_HEAL_INTERVAL_TICKS - MIN_HEAL_INTERVAL_TICKS;
    int steps = Math.max(1, maxLvl - 1);

    int reduction = range * (level - 1) / steps;
    return BASE_HEAL_INTERVAL_TICKS - reduction;
  }

  /**
   * Quantidade de HP curada por proc.
   *
   * Exposto publicamente para a UI mostrar os valores.
   */
  public int getEffectiveHealAmount() {
    // 1 HP nos níveis 1–4, 2 nos 5–9, 3 nos 10–14, 4 nos 15+
    return BASE_HEAL_AMOUNT + (getPetLevel() - 1) / 5;
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

  @Override
  public String buildPetStatsLineForUi() {
    int healAmount = getEffectiveHealAmount();
    int healIntervalSec = getEffectiveHealIntervalTicks() / 60;

    String template = t("ui.petYard.stats.heal");
    return String.format(template, healAmount, healIntervalSec);
  }
}

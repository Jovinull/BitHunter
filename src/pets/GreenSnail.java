package pets;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import entity.Player;
import main.GamePanel;

/**
 * Green Snail:
 * - Segue o player.
 * - Recupera mana periodicamente.
 * - Ao recuperar mana: gera partículas azul-esverdeadas, brilha e mostra
 * mensagem no log.
 */
public class GreenSnail extends PetEntity {

  /**
   * ID lógico estável do PET.
   *
   * Este ID é usado como:
   * - internalId da entidade (save/load, Player, DataStorage)
   * - chave em PetFactory.PET_GREEN_SNAIL
   * - chave em ownedPets / petLevels / petExps
   */
  public static final String ID = "pet.greenSnail";

  private static final int BASE_SPEED = 4;

  // Intervalo base e mínimo (em ticks)
  private static final int BASE_MANA_INTERVAL_TICKS = 60 * 60; // 60s no nível 1
  private static final int MIN_MANA_INTERVAL_TICKS = 15 * 60; // 15s no nível máximo

  // Mana base por proc
  private static final int BASE_MANA_AMOUNT = 1;

  // Duração do brilho (frames)
  private static final int MANA_GLOW_DURATION_TICKS = 30;

  // Partículas de mana (cor e parâmetros)
  private static final Color MANA_PARTICLE_COLOR = new Color(80, 180, 255);

  private int manaCounter = 0;
  private int manaGlowCounter = 0;

  public GreenSnail(GamePanel gp, Player owner) {
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

    var frame1 = setup("/pets/Green Snail/green_snail_1", size, size);
    var frame2 = setup("/pets/Green Snail/green_snail_2", size, size);

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

    // Conta decrescente do brilho
    if (manaGlowCounter > 0) {
      manaGlowCounter--;
    }
  }

  @Override
  protected void applyPassiveEffect() {
    Player player = getOwner();

    if (player.getMana() >= player.getMaxMana()) {
      manaCounter = 0;
      return;
    }

    manaCounter++;
    int interval = getEffectiveManaIntervalTicks();
    if (manaCounter < interval) {
      return;
    }

    manaCounter = 0;

    int currentMana = player.getMana();
    int maxMana = player.getMaxMana();

    int restoreAmount = getEffectiveManaAmount();
    int effectiveRestore = Math.min(restoreAmount, maxMana - currentMana);
    if (effectiveRestore <= 0) {
      return;
    }

    player.setMana(currentMana + effectiveRestore);

    manaGlowCounter = MANA_GLOW_DURATION_TICKS;

    String petName = getDisplayName();
    String msg = String.format(t("pet.message.mana"), petName, effectiveRestore);
    gp.getGameUi().addMessage(msg);
  }

  /**
   * Intervalo efetivo do proc de mana, dependente do nível do pet (em ticks).
   *
   * Exposto publicamente para a UI.
   */
  public int getEffectiveManaIntervalTicks() {
    int level = getPetLevel(); // vem de PetEntity
    int maxLvl = getPetMaxLevel();

    int range = BASE_MANA_INTERVAL_TICKS - MIN_MANA_INTERVAL_TICKS;
    int steps = Math.max(1, maxLvl - 1);

    int reduction = range * (level - 1) / steps;
    return BASE_MANA_INTERVAL_TICKS - reduction;
  }

  /**
   * Quantidade de mana restaurada por proc.
   *
   * Exposto publicamente para a UI.
   */
  public int getEffectiveManaAmount() {
    // 1 MP nos níveis 1–4, 2 nos 5–9, 3 nos 10–14, 4 nos 15+
    return BASE_MANA_AMOUNT + (getPetLevel() - 1) / 5;
  }

  // -------- Partículas específicas da GreenSnail --------

  @Override
  public Color getParticleColor() {
    return MANA_PARTICLE_COLOR;
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

  // -------- Desenho com brilho quando regenera mana --------

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

    // Halo quando a habilidade acaba de ser usada
    if (manaGlowCounter > 0) {
      int padding = 4;
      int haloX = screenX - padding;
      int haloY = screenY - padding;
      int haloSize = GamePanel.TILE_SIZE + padding * 2;

      g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
      g2.setColor(new Color(80, 180, 255, 180));
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
    int manaAmount = getEffectiveManaAmount();
    int manaIntervalSec = getEffectiveManaIntervalTicks() / 60;

    String template = t("ui.petYard.stats.mana"); // ex.: "Recupera %d MP a cada %ds"
    return String.format(template, manaAmount, manaIntervalSec);
  }
}

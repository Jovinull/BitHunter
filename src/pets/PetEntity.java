package pets;

import java.util.Objects;

import entity.Entity;
import entity.Player;
import main.GamePanel;

/**
 * Base genérica para pets.
 *
 * - Segue o player.
 * - Não usa colisão/ataque da Entity.
 * - Cada pet aplica seu efeito passivo em applyPassiveEffect().
 */
public abstract class PetEntity extends Entity {

  protected final Player owner;

  // Distância mínima até o player antes de começar a seguir (em pixels)
  private static final int FOLLOW_RADIUS = GamePanel.TILE_SIZE * 2;

  // Intervalo simples de animação
  private static final int ANIM_INTERVAL = 40;

  // ===== PROGRESSÃO DO PET =====
  private int petLevel = 1;
  private int petExp = 0;
  private int petNextLevelExp = 50;
  private static final int PET_MAX_LEVEL = 20;

  public int getPetLevel() {
    return petLevel;
  }

  public int getPetExp() {
    return petExp;
  }

  public int getPetNextLevelExp() {
    return petNextLevelExp;
  }

  public int getPetMaxLevel() {
    return PET_MAX_LEVEL;
  }

  public void addPetExp(int amount) {
    if (amount <= 0 || petLevel >= PET_MAX_LEVEL) {
      return;
    }

    petExp += amount;

    // Loop de level up (pode subir mais de um nível de uma vez)
    while (petExp >= petNextLevelExp && petLevel < PET_MAX_LEVEL) {
      petExp -= petNextLevelExp;
      petLevel++;
      petNextLevelExp = computeNextPetLevelExp(petLevel);
      onPetLevelUp();
    }
    syncPetProgressToOwner();
  }

  /** Curva de XP do pet – quadrática (demora BEM mais nos níveis altos). */
  protected int computeNextPetLevelExp(int level) {
    // lvl 1->2: 55, lvl 5->6: 215, lvl 10->11: 1.640, etc.
    return 40 + level * level * 15;
  }

  /** Mensagem de level up do pet. */
  protected void onPetLevelUp() {
    String petName = getDisplayName();
    String msg = String.format(t("pet.message.levelUp"), petName, petLevel);
    gp.getGameUi().addMessage(msg);
  }

  /** Usado pelo Player para espelhar o progresso em estruturas de save. */
  protected void syncPetProgressToOwner() {
    if (getOwner() != null) {
      getOwner().onPetProgressUpdated(this);
    }
  }

  /** Permite restaurar level/XP do save ou do Player. */
  public void setPetProgress(int level, int exp) {
    // Faz clamp explícito para garantir que o nível fique entre 1 e PET_MAX_LEVEL
    this.petLevel = Math.clamp(level, 1, PET_MAX_LEVEL);
    this.petExp = Math.max(0, exp);
    this.petNextLevelExp = computeNextPetLevelExp(this.petLevel);
  }

  protected PetEntity(GamePanel gp, Player owner) {
    super(gp);
    this.owner = Objects.requireNonNull(owner, "owner");
    setType(TYPE_PET);
    setDirection(DIR_DOWN);

    // posição inicial: levemente atrás do player
    setWorldX(owner.getWorldX() - GamePanel.TILE_SIZE);
    setWorldY(owner.getWorldY());
  }

  @Override
  public void update() {
    followOwner();
    animate();
    applyPassiveEffect();
  }

  private void followOwner() {
    int dx = owner.getWorldX() - getWorldX();
    int dy = owner.getWorldY() - getWorldY();

    int distanceSq = dx * dx + dy * dy;
    int radius = FOLLOW_RADIUS;
    if (distanceSq <= radius * radius) {
      return; // já está perto o suficiente
    }

    int stepX = Integer.compare(dx, 0);
    int stepY = Integer.compare(dy, 0);

    int s = Math.max(1, getSpeed());
    setWorldX(getWorldX() + stepX * s);
    setWorldY(getWorldY() + stepY * s);

    // Atualiza direção para bater com a posição do player
    if (Math.abs(dx) > Math.abs(dy)) {
      setDirection(dx > 0 ? DIR_RIGHT : DIR_LEFT);
    } else {
      setDirection(dy > 0 ? DIR_DOWN : DIR_UP);
    }
  }

  private void animate() {
    setSpriteCounter(getSpriteCounter() + 1);
    if (getSpriteCounter() > ANIM_INTERVAL) {
      setSpriteNum(getSpriteNum() == 1 ? 2 : 1);
      setSpriteCounter(0);
    }
  }

  /**
   * Efeito passivo do pet (cura, buff, etc).
   */
  protected abstract void applyPassiveEffect();

  /**
   * Reposiciona o pet instantaneamente ao lado do owner.
   * Usado durante teleportes para evitar que o pet "ande" pelo mapa inteiro.
   */
  public void snapToOwner() {
    setWorldX(owner.getWorldX() - GamePanel.TILE_SIZE);
    setWorldY(owner.getWorldY());
    setDirection(owner.getDirection());
    setSpriteCounter(0);
  }

  public Player getOwner() {
    return owner;
  }

  /**
   * Texto de stats para exibição em telas de UI (por exemplo, Pet Yard).
   *
   * Implementações concretas podem sobrescrever para retornar uma linha
   * como "Cura 3 HP a cada 20s" ou "Recupera 2 MP a cada 15s".
   *
   * Retorne null ou string vazia quando não houver stats específicos.
   */
  public String buildPetStatsLineForUi() {
    return null;
  }
}

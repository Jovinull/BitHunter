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

  public Player getOwner() {
    return owner;
  }
}

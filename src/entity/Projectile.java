package entity;

import java.util.Objects;

import main.GamePanel;

public class Projectile extends Entity {

  private Entity user;

  public Projectile(GamePanel gp) {
    super(gp);
  }

  public void set(int worldX, int worldY, String direction, boolean alive, Entity user) {
    setWorldX(worldX);
    setWorldY(worldY);
    setDirection(direction);
    setAlive(alive);
    this.user = Objects.requireNonNull(user, "user");
    setLife(getMaxLife());
  }

  @Override
  public void update() {
    // Colisão (player -> monstros) ou (inimigo -> player)
    if (user == gp.getPlayer()) {
      handleHitMonster();
    } else {
      handleHitPlayer();
    }

    // Movimento, vida e animação
    moveByDirection();
    tickLifeAndMaybeDie();
    updateAnimation();
  }

  // ===== Helpers para reduzir complexidade (S3776) =====

  /** Projétil do player acerta monstros. */
  private void handleHitMonster() {
    var collisionChecker = gp.getcChecker();
    Entity[][] monsters = gp.getMonster();
    int currentMapIndex = gp.getCurrentMap();

    int monsterIndex = collisionChecker.checkEntity(this, monsters);
    if (monsterIndex < 0) {
      return;
    }

    var player = gp.getPlayer();

    player.damageMonster(
        monsterIndex,
        this,
        getAttack() * Math.max(1, player.getLevel() / 2),
        getKnockBackPower());

    // partículas usando o projétil do "user" (comportamento anterior)
    if (user.getProjectile() != null) {
      generateParticle(user.getProjectile(), monsters[currentMapIndex][monsterIndex]);
    }
    setAlive(false);
  }

  /** Projétil de inimigo acerta o player. */
  private void handleHitPlayer() {
    var collisionChecker = gp.getcChecker();
    var player = gp.getPlayer();

    boolean contactPlayer = collisionChecker.checkPlayer(this);
    if (player.isInvincible() || !contactPlayer) {
      return;
    }

    damagePlayer(getAttack());

    if (user.getProjectile() != null) {
      generateParticle(user.getProjectile(), user.getProjectile());
    }
    setAlive(false);
  }

  /** Aplica deslocamento conforme direção atual. */
  private void moveByDirection() {
    switch (getDirection()) {
      case DIR_UP:
        setWorldY(getWorldY() - getSpeed());
        break;
      case DIR_DOWN:
        setWorldY(getWorldY() + getSpeed());
        break;
      case DIR_LEFT:
        setWorldX(getWorldX() - getSpeed());
        break;
      case DIR_RIGHT:
        setWorldX(getWorldX() + getSpeed());
        break;
      default:
        break;
    }
  }

  /** Decrementa vida e marca como morto quando expira. */
  private void tickLifeAndMaybeDie() {
    setLife(getLife() - 1);
    if (getLife() <= 0) {
      setAlive(false);
    }
  }

  /** Atualiza animação (spriteNum alternando a cada 12 ticks). */
  private void updateAnimation() {
    setSpriteCounter(getSpriteCounter() + 1);
    if (getSpriteCounter() > 12) {
      setSpriteNum(getSpriteNum() == 1 ? 2 : 1);
      setSpriteCounter(0);
    }
  }

  /**
   * Template method: por padrão, projéteis genéricos não têm custo.
   * Subclasses (ex.: Fireball) devem sobrescrever.
   */
  public boolean haveResource(Entity user) {
    Objects.requireNonNull(user, "user");
    return false;
  }

  /**
   * Template method: por padrão, não consome nada.
   * Subclasses (ex.: Fireball) devem sobrescrever.
   */
  public void subtractResource(Entity user) {
    Objects.requireNonNull(user, "user");
    // no-op por padrão
  }

  public Entity getUser() {
    return user;
  }
}

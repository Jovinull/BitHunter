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

    // Se o dono do projétil for o player, colide em monstros
    if (user == gp.player) {
      int monsterIndex = gp.cChecker.checkEntity(this, gp.monster);
      if (monsterIndex != 999) {
        gp.player.damageMonster(
            monsterIndex,
            this,
            getAttack() * Math.max(1, gp.player.getLevel() / 2),
            getKnockBackPower());
        // partículas usando o projétil do "user" (mesmo comportamento antigo)
        if (user.getProjectile() != null) {
          generateParticle(user.getProjectile(), gp.monster[gp.currentMap][monsterIndex]);
        }
        setAlive(false);
      }
    }

    // Se o dono NÃO for o player, colide no player
    if (user != gp.player) {
      boolean contactPlayer = gp.cChecker.checKPlayer(this);
      if (!gp.player.isInvincible() && contactPlayer) {
        damagePlayer(getAttack());
        if (user.getProjectile() != null) {
          generateParticle(user.getProjectile(), user.getProjectile());
        }
        setAlive(false);
      }
    }

    // Movimento
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

    // Vida do projétil
    setLife(getLife() - 1);
    if (getLife() <= 0) {
      setAlive(false);
    }

    // Animação
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

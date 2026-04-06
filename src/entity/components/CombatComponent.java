package entity.components;

import entity.Entity;
import entity.Player;
import object.ObjArrow;

public class CombatComponent {

  private static final int PERFECT_GUARD_WINDOW_FRAMES = 10;
  private static final int GUARD_STUN_PENALTY_FRAMES = 60;
  private static final int BLOCKED_DAMAGE_DIVISOR = 3;

  private final Entity entity;

  public CombatComponent(Entity entity) {
    this.entity = entity;
  }

  public void performAttack() {
    entity.setSpriteCounter(entity.getSpriteCounter() + 1);

    updateAttackSprite();

    if (isInDamageWindow() && !entity.isAttackDamageApplied()) {
      if (isBowAttack()) {
        fireArrow();
      } else {
        applyAttackDamage();
      }
    }

    if (entity.getSpriteCounter() > entity.getMotion2Duration()) {
      endAttack();
    }
  }

  private void updateAttackSprite() {
    if (entity.getSpriteCounter() <= entity.getMotion1Duration()) {
      entity.setSpriteNum(1);
    } else if (entity.getSpriteCounter() <= entity.getMotion2Duration()) {
      entity.setSpriteNum(2);
    }
  }

  private boolean isInDamageWindow() {
    return entity.getSpriteCounter() > entity.getMotion1Duration()
        && entity.getSpriteCounter() <= entity.getMotion2Duration();
  }

  private void applyAttackDamage() {
    int currentWorldX = entity.getWorldX();
    int currentWorldY = entity.getWorldY();
    int solidAreaWidth = entity.getSolidArea().width;
    int solidAreaHeight = entity.getSolidArea().height;

    moveAttackHitboxForward();

    entity.getSolidArea().width = entity.getAttackArea().width;
    entity.getSolidArea().height = entity.getAttackArea().height;

    applyAttackCollision();

    // restaura posição / hitbox
    entity.setWorldX(currentWorldX);
    entity.setWorldY(currentWorldY);
    entity.getSolidArea().width = solidAreaWidth;
    entity.getSolidArea().height = solidAreaHeight;

    entity.setAttackDamageApplied(true);
  }

  private void moveAttackHitboxForward() {
    switch (entity.getDirection()) {
      case Entity.DIR_UP:
        entity.setWorldY(entity.getWorldY() - entity.getAttackArea().height);
        break;
      case Entity.DIR_DOWN:
        entity.setWorldY(entity.getWorldY() + entity.getAttackArea().height);
        break;
      case Entity.DIR_LEFT:
        entity.setWorldX(entity.getWorldX() - entity.getAttackArea().width);
        break;
      case Entity.DIR_RIGHT:
        entity.setWorldX(entity.getWorldX() + entity.getAttackArea().width);
        break;
      default:
        break;
    }
  }

  private void applyAttackCollision() {
    var collisionChecker = entity.getGp().getcChecker();
    var player = entity.getGp().getPlayer();

    if (entity.getType() == Entity.TYPE_MONSTER) {
      if (collisionChecker.checkPlayer(entity)) {
        damagePlayer(entity.getAttack());
      }
    } else {
      int monsterIndex = collisionChecker.checkEntity(entity, entity.getGp().getMonster());
      player.damageMonster(monsterIndex, entity, entity.getAttack(), entity.getCurrentWeapon().getKnockBackPower());

      int iTileIndex = collisionChecker.checkEntity(entity, entity.getGp().getiTile());
      player.damageInteractiveTile(iTileIndex);

      int projectileIndex = collisionChecker.checkEntity(entity, entity.getGp().getProjectile());
      player.damageProjectile(projectileIndex);
    }
  }

  private boolean isBowAttack() {
    return entity.getCurrentWeapon() != null
        && entity.getCurrentWeapon().getType() == Entity.TYPE_BOW;
  }

  private void fireArrow() {
    var gp = entity.getGp();
    var arrow = new ObjArrow(gp);
    arrow.setAttack(entity.getCurrentWeapon().getAttackValue());
    arrow.setKnockBackPower(entity.getCurrentWeapon().getKnockBackPower());
    arrow.set(entity.getWorldX(), entity.getWorldY(), entity.getDirection(), true, entity);

    Entity[][] projectiles = gp.getProjectile();
    int mapIndex = gp.getCurrentMap();
    for (int i = 0; i < projectiles[mapIndex].length; i++) {
      if (projectiles[mapIndex][i] == null) {
        projectiles[mapIndex][i] = arrow;
        break;
      }
    }

    gp.playSE(10);
    entity.setAttackDamageApplied(true);
  }

  private void endAttack() {
    entity.setSpriteNum(1);
    entity.setSpriteCounter(0);
    entity.setAttacking(false);
    entity.setAttackDamageApplied(false);
  }

  public void damagePlayer(int attack) {
    Player player = entity.getGp().getPlayer();

    if (player.isInvincible()) {
      return;
    }

    int damage = attack - player.getDefense();
    String canGuardDirection = entity.getOppositeDirection(entity.getDirection());

    boolean hasActiveShield = player.getCurrentShield() != null;
    boolean hasGuardStamina = player.getMaxGuardStamina() > 0 && player.getGuardStamina() > 0;

    boolean isGuardingWithShield = hasActiveShield
        && hasGuardStamina
        && player.isGuarding()
        && player.getDirection().equals(canGuardDirection);

    if (isGuardingWithShield) {

      player.onGuardHit(attack);

      if (player.getGuardCounter() < PERFECT_GUARD_WINDOW_FRAMES) {
        damage = 0;
        entity.getGp().playSE(16);
        setKnockBack(entity, player, entity.getKnockBackPower());
        entity.setOffBalance(true);
        entity.setSpriteCounter(entity.getSpriteCounter() - GUARD_STUN_PENALTY_FRAMES);
      } else {
        damage = damage / BLOCKED_DAMAGE_DIVISOR;
        entity.getGp().playSE(15);
      }
    } else {
      entity.getGp().playSE(6);
      if (damage < 1) {
        damage = 1;
      }
    }

    if (damage != 0) {
      player.setTransparent(true);
      setKnockBack(player, entity, entity.getKnockBackPower());
    }

    // Usa receiveFinalDamage para integrar mecânicas do Escudo do Compilador
    // (absorção GC, try/catch, contagem de hits)
    player.receiveFinalDamage(damage);
    player.setInvincible(true);
  }

  public void setKnockBack(Entity target, Entity attacker, int knockBackPower) {
    entity.setAttacker(attacker);
    target.setKnockBackDirection(attacker.getDirection());
    target.setSpeed(target.getSpeed() + knockBackPower);
    target.setKnockBack(true);
  }
}

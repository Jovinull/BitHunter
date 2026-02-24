package entity.components;

import entity.Entity;
public class MovementComponent {

  private static final int KNOCKBACK_DURATION_FRAMES = 10;
  private static final int SPRITE_ANIMATION_THRESHOLD = 24;
  private static final int INVINCIBILITY_DURATION_FRAMES = 20;
  private static final int SHOT_COOLDOWN_FRAMES = 30;
  private static final int OFF_BALANCE_DURATION_FRAMES = 60;

  private final Entity entity;

  public MovementComponent(Entity entity) {
    this.entity = entity;
  }

  public void handleKnockBack() {
    entity.checkCollision();

    if (entity.isCollisionOn()) {
      entity.setKnockBackCounter(0);
      entity.setKnockBack(false);
      entity.setSpeed(entity.getDefaultSpeed());
    } else {
      switch (entity.getKnockBackDirection()) {
        case Entity.DIR_UP:
          entity.setWorldY(entity.getWorldY() - entity.getSpeed());
          break;
        case Entity.DIR_DOWN:
          entity.setWorldY(entity.getWorldY() + entity.getSpeed());
          break;
        case Entity.DIR_LEFT:
          entity.setWorldX(entity.getWorldX() - entity.getSpeed());
          break;
        case Entity.DIR_RIGHT:
          entity.setWorldX(entity.getWorldX() + entity.getSpeed());
          break;
        default:
          break;
      }
    }

    entity.setKnockBackCounter(entity.getKnockBackCounter() + 1);
    if (entity.getKnockBackCounter() == KNOCKBACK_DURATION_FRAMES) {
      entity.setKnockBackCounter(0);
      entity.setKnockBack(false);
      entity.setSpeed(entity.getDefaultSpeed());
    }
  }

  public void handleActionAndMovement() {
    entity.setAction();
    entity.checkCollision();

    if (!entity.isCollisionOn()) {
      switch (entity.getDirection()) {
        case Entity.DIR_UP:
          entity.setWorldY(entity.getWorldY() - entity.getSpeed());
          break;
        case Entity.DIR_DOWN:
          entity.setWorldY(entity.getWorldY() + entity.getSpeed());
          break;
        case Entity.DIR_LEFT:
          entity.setWorldX(entity.getWorldX() - entity.getSpeed());
          break;
        case Entity.DIR_RIGHT:
          entity.setWorldX(entity.getWorldX() + entity.getSpeed());
          break;
        default:
          break;
      }
    }

    entity.setSpriteCounter(entity.getSpriteCounter() + 1);
    if (entity.getSpriteCounter() > SPRITE_ANIMATION_THRESHOLD) {
      entity.setSpriteNum(entity.getSpriteNum() == 1 ? 2 : 1);
      entity.setSpriteCounter(0);
    }
  }

  public void handleInvincibility() {
    if (entity.isInvincible()) {
      entity.setInvincibleCounter(entity.getInvincibleCounter() + 1);
      if (entity.getInvincibleCounter() > INVINCIBILITY_DURATION_FRAMES) {
        entity.setInvincible(false);
        entity.setInvincibleCounter(0);
      }
    }
  }

  public void handleShotCooldown() {
    if (entity.getShotAvailableCounter() < SHOT_COOLDOWN_FRAMES) {
      entity.setShotAvailableCounter(entity.getShotAvailableCounter() + 1);
    }
  }

  public void handleOffBalance() {
    if (entity.isOffBalance()) {
      entity.setOffBalanceCounter(entity.getOffBalanceCounter() + 1);
      if (entity.getOffBalanceCounter() > OFF_BALANCE_DURATION_FRAMES) {
        entity.setOffBalance(false);
        entity.setOffBalanceCounter(0);
      }
    }
  }

  public void checkAttackOrNot(int rate, int straight, int horizontal) {
    if (isTargetInFront(entity.getGp().getPlayer(), straight, horizontal) && entity.randomInt(rate) == 0) {
      entity.setAttacking(true);
      entity.setSpriteNum(1);
      entity.setSpriteCounter(0);
      entity.setShotAvailableCounter(0);
    }
  }

  private boolean isTargetInFront(Entity target, int straight, int horizontal) {
    int dx = target.getCenterX() - entity.getCenterX();
    int dy = target.getCenterY() - entity.getCenterY();

    switch (entity.getDirection()) {
      case Entity.DIR_UP:
        return dy < 0 && Math.abs(dy) < straight && Math.abs(dx) < horizontal;
      case Entity.DIR_DOWN:
        return dy > 0 && Math.abs(dy) < straight && Math.abs(dx) < horizontal;
      case Entity.DIR_LEFT:
        return dx < 0 && Math.abs(dx) < straight && Math.abs(dy) < horizontal;
      case Entity.DIR_RIGHT:
        return dx > 0 && Math.abs(dx) < straight && Math.abs(dy) < horizontal;
      default:
        return false;
    }
  }

  public void checkShootOrNot(int rate, int shotInterval) {
    if (entity.randomInt(rate) == 0
        && entity.getProjectile() != null
        && !entity.getProjectile().isAlive()
        && entity.getShotAvailableCounter() == shotInterval) {

      entity.getProjectile().set(entity.getWorldX(), entity.getWorldY(), entity.getDirection(), true, entity);

      Entity[][] projectiles = entity.getGp().getProjectile();
      int currentMapIndex = entity.getGp().getCurrentMap();

      for (int j = 0; j < projectiles[currentMapIndex].length; j++) {
        if (projectiles[currentMapIndex][j] == null) {
          projectiles[currentMapIndex][j] = entity.getProjectile();
          break;
        }
      }
      entity.setShotAvailableCounter(0);
    }
  }

  public void checkStartChasingOrNot(Entity target, int distance, int rate) {
    if (entity.getTileDistance(target) < distance && entity.randomInt(rate) == 0) {
      entity.setOnPath(true);
    }
  }

  public void checkStopChasingOrNot(Entity target, int distance, int rate) {
    if (entity.getTileDistance(target) > distance && entity.randomInt(rate) == 0) {
      entity.setOnPath(false);
    }
  }

  public void getRandomDirection(int interval) {
    entity.setActionLockCounter(entity.getActionLockCounter() + 1);
    if (entity.getActionLockCounter() > interval) {
      switch (entity.randomInt(4)) {
        case 0:
          entity.setDirection(Entity.DIR_UP);
          break;
        case 1:
          entity.setDirection(Entity.DIR_DOWN);
          break;
        case 2:
          entity.setDirection(Entity.DIR_LEFT);
          break;
        case 3:
          entity.setDirection(Entity.DIR_RIGHT);
          break;
        default:
          break;
      }
      entity.setActionLockCounter(0);
    }
  }

  public void moveTowardPlayer(int interval) {
    entity.setActionLockCounter(entity.getActionLockCounter() + 1);
    if (entity.getActionLockCounter() > interval) {
      var player = entity.getGp().getPlayer();
      if (entity.getXdistance(player) > entity.getYdistance(player)) {
        entity.setDirection(player.getCenterX() < entity.getCenterX() ? Entity.DIR_LEFT : Entity.DIR_RIGHT);
      } else if (entity.getXdistance(player) < entity.getYdistance(player)) {
        entity.setDirection(player.getCenterY() < entity.getCenterY() ? Entity.DIR_UP : Entity.DIR_DOWN);
      }
      entity.setActionLockCounter(0);
    }
  }
}

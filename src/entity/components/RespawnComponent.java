package entity.components;

import java.awt.Rectangle;

import entity.Entity;

public class RespawnComponent {

  private static final int SPAWN_BLINK_DURATION_FRAMES = 60;

  private final Entity entity;

  private int spawnWorldX;
  private int spawnWorldY;
  private boolean respawnEnabled;
  private int respawnDelayFrames;
  private int respawnCounter;
  private boolean dropHandled;

  private boolean spawning;
  private int spawnBlinkCounter;

  public RespawnComponent(Entity entity) {
    this.entity = entity;
  }

  public void setSpawnPoint(int worldX, int worldY) {
    this.spawnWorldX = worldX;
    this.spawnWorldY = worldY;
  }

  public void enableRespawn(int delayFrames) {
    this.respawnEnabled = true;
    this.respawnDelayFrames = delayFrames;
    this.respawnCounter = 0;
  }

  public void disableRespawn() {
    this.respawnEnabled = false;
    this.respawnCounter = 0;
  }

  public boolean isRespawnEnabled() {
    return respawnEnabled;
  }

  public boolean isSpawning() {
    return spawning;
  }

  public int getSpawnBlinkCounter() {
    return spawnBlinkCounter;
  }

  public void updateRespawn() {
    if (!respawnEnabled) {
      return;
    }

    // Se, por algum motivo, já voltou à vida, zera estado de respawn e sai
    if (entity.isAlive()) {
      respawnCounter = 0;
      spawning = false;
      spawnBlinkCounter = 0;
      return;
    }

    // Fase 2: já está piscando no ponto de spawn, esperando "nascer"
    if (spawning) {
      spawnBlinkCounter++;

      if (spawnBlinkCounter >= SPAWN_BLINK_DURATION_FRAMES) {
        spawning = false;
        spawnBlinkCounter = 0;
        respawnCounter = 0;

        // Garante que está exatamente no ponto de spawn
        entity.setWorldX(spawnWorldX);
        entity.setWorldY(spawnWorldY);

        // Reseta status básico e "nasce" de verdade
        entity.setLife(entity.getMaxLife());
        entity.setDying(false);
        entity.setAlive(true);
        // Drop deve ser liberado novamente para a próxima morte (é "1x por morte", não
        // "1x pra sempre")
        dropHandled = false;
        entity.setInvincible(false);
        entity.setTransparent(false);
        entity.resetCounter();

        entity.setKnockBack(false);
        entity.setKnockBackDirection(null);
        entity.setCollisionOn(false);
        entity.setSpeed(entity.getDefaultSpeed());
      }

      return;
    }

    // Fase 1: aguardando o tempo de respawn "invisível"
    if (respawnCounter < respawnDelayFrames) {
      respawnCounter++;
      return;
    }

    if (entity.getGp() != null) {
      var player = entity.getGp().getPlayer();
      if (player != null) {
        Rectangle spawnRect = new Rectangle(
            spawnWorldX + entity.getSolidArea().x,
            spawnWorldY + entity.getSolidArea().y,
            entity.getSolidArea().width,
            entity.getSolidArea().height);

        Rectangle playerRect = new Rectangle(
            player.getWorldX() + player.getSolidArea().x,
            player.getWorldY() + player.getSolidArea().y,
            player.getSolidArea().width,
            player.getSolidArea().height);

        // Player ainda está ocupando o tile de respawn -> segura
        if (spawnRect.intersects(playerRect)) {
          respawnCounter = respawnDelayFrames;
          return;
        }
      }
    }

    // Player saiu da área -> começa animação de spawn (fantasma piscando)
    entity.setWorldX(spawnWorldX);
    entity.setWorldY(spawnWorldY);
    spawning = true;
    spawnBlinkCounter = 0;
  }

  public boolean isDropHandled() {
    return dropHandled;
  }

  public void setDropHandled(boolean dropHandled) {
    this.dropHandled = dropHandled;
  }
}

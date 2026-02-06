package entity;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

final class EntityRenderer {

  void draw(Entity entity, Graphics2D g2) {
    if (!entity.isAlive() && !entity.isDying() && !entity.isSpawning()) {
      return;
    }

    if (!entity.inCamera()) {
      return;
    }

    int x = entity.getScreenX();
    int y = entity.getScreenY();

    Frame f = resolveFrameAndOffsets(entity, x, y);

    // Se está em animação de respawn (antes de "nascer"), pisca no lugar
    if (entity.isSpawning()) {
      int blinkInterval = 6; // alterna visível/invisível a cada 6 frames
      boolean visible = ((entity.getSpawnBlinkCounter() / blinkInterval) % 2) == 0;
      if (!visible) {
        return; // neste frame não desenha nada
      }
    }

    if (entity.isInvincible()) {
      entity.setHpBarOn(true);
      entity.setHpBarCounter(0);
      entity.changeAlpha(g2, 0.4f);
    }

    if (entity.isDying()) {
      // Roda animação de morte
      entity.dyingAnimation(g2);

      // Se a animação terminou e já marcou como morto, não desenha o frame normal
      if (!entity.isAlive()) {
        entity.changeAlpha(g2, 1f);
        return;
      }
    }

    g2.drawImage(f.img, f.x, f.y, null);
    entity.changeAlpha(g2, 1f);
  }

  private Frame resolveFrameAndOffsets(Entity entity, int baseX, int baseY) {
    BufferedImage img = null;
    int x = baseX;
    int y = baseY;

    switch (entity.getDirection()) {
      case Entity.DIR_UP:
        if (entity.isAttacking() && entity.up1 != null) {
          y = baseY - entity.up1.getHeight();
        }
        img = pickFrame(entity.attackUp1, entity.attackUp2, entity.up1, entity.up2, entity.isAttacking(),
            entity.getSpriteNum());
        break;

      case Entity.DIR_DOWN:
        img = pickFrame(entity.attackDown1, entity.attackDown2, entity.down1, entity.down2, entity.isAttacking(),
            entity.getSpriteNum());
        break;

      case Entity.DIR_LEFT:
        if (entity.isAttacking() && entity.left1 != null) {
          x = baseX - entity.left1.getWidth();
        }
        img = pickFrame(entity.attackLeft1, entity.attackLeft2, entity.left1, entity.left2, entity.isAttacking(),
            entity.getSpriteNum());
        break;

      case Entity.DIR_RIGHT:
        img = pickFrame(entity.attackRight1, entity.attackRight2, entity.right1, entity.right2, entity.isAttacking(),
            entity.getSpriteNum());
        break;

      default:
        img = entity.down1;
        break;
    }
    return new Frame(img, x, y);
  }

  private BufferedImage pickFrame(BufferedImage atk1, BufferedImage atk2, BufferedImage idle1, BufferedImage idle2,
      boolean attacking, int spriteNum) {
    if (attacking) {
      return (spriteNum == 1) ? atk1 : atk2;
    }
    return (spriteNum == 1) ? idle1 : idle2;
  }

  private static final class Frame {
    final BufferedImage img;
    final int x;
    final int y;

    Frame(BufferedImage i, int x, int y) {
      this.img = i;
      this.x = x;
      this.y = y;
    }
  }
}

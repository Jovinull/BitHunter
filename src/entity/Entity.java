package entity;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import javax.imageio.ImageIO;

import main.GamePanel;
import main.UtilityTool;

// Muitos avisos da IDE/analizador reclamam de campos públicos herdados.
// Vamos tratar isso por etapas. Por enquanto, suprimimos regras do Sonar
// específicas para "public fields" até migrarmos para getters/setters.
@SuppressWarnings({ "java:S1104" })
public class Entity {

  // =========================
  // Constantes de Direção
  // =========================
  public static final String DIR_UP = "up";
  public static final String DIR_DOWN = "down";
  public static final String DIR_LEFT = "left";
  public static final String DIR_RIGHT = "right";

  // =========================
  // Constantes de Tipo
  // =========================
  public static final int TYPE_PLAYER = 0;
  public static final int TYPE_NPC = 1;
  public static final int TYPE_MONSTER = 2;
  public static final int TYPE_SWORD = 3;
  public static final int TYPE_AXE = 4;
  public static final int TYPE_SHIELD = 5;
  public static final int TYPE_CONSUMABLE = 6;
  public static final int TYPE_PICKUP_ONLY = 7;
  public static final int TYPE_OBSTACLE = 8;
  public static final int TYPE_LIGHT = 9;
  public static final int TYPE_PICKAXE = 10;

  // =========================
  // Outros
  // =========================
  public static final int MAX_INVENTORY_SIZE = 20;

  // Reutilize um único Random
  private static final Random RNG = new Random();

  // Acesso por subclasses
  protected final GamePanel gp;

  // Sprites de movimento
  public BufferedImage up1;
  public BufferedImage up2;
  public BufferedImage down1;
  public BufferedImage down2;
  public BufferedImage left1;
  public BufferedImage left2;
  public BufferedImage right1;
  public BufferedImage right2;

  // Sprites de ataque/guarda
  public BufferedImage attackUp1;
  public BufferedImage attackUp2;
  public BufferedImage attackDown1;
  public BufferedImage attackDown2;
  public BufferedImage attackLeft1;
  public BufferedImage attackLeft2;
  public BufferedImage attackRight1;
  public BufferedImage attackRight2;
  public BufferedImage guardUp;
  public BufferedImage guardDown;
  public BufferedImage guardLeft;
  public BufferedImage guardRight;

  public BufferedImage image;
  public BufferedImage image2;
  public BufferedImage image3;

  public final Rectangle solidArea = new Rectangle(0, 0, 48, 48);
  public final Rectangle attackArea = new Rectangle(0, 0, 0, 0);

  public int solidAreaDefaultX;
  public int solidAreaDefaultY;

  public boolean collision = false;
  public String[][] dialogues = new String[20][20];

  public Entity attacker;
  public Entity linkedEntity;
  public boolean temp = false;

  // STATE
  public int worldX;
  public int worldY;
  public String direction = DIR_DOWN;
  public int spriteNum = 1;
  public int dialogueSet = 0;
  public int dialogueIndex = 0;
  public boolean collisionOn = false;
  public boolean invincible = false;
  public boolean attacking = false;
  public boolean alive = true;
  public boolean dying = false;
  public boolean hpBarOn = false;
  public boolean onPath = false;
  public boolean knockBack = false;
  public String knockBackDirection;
  public boolean guarding = false;
  public boolean transparent = false;
  public boolean offBalance = false;
  public Entity loot;
  public boolean opened = false;
  public boolean inRage = false;
  public boolean sleep = false;
  public boolean drawing = true;

  // COUNTERS
  public int spriteCounter = 0;
  public int actionLockCounter = 0;
  public int invincibleCounter = 0;
  public int shotAvailableCounter = 0;
  int dyingCounter = 0;
  public int hpBarCounter = 0;
  int knockBackCounter = 0;
  public int guardCounter = 0;
  int offBalanceCounter = 0;

  // CHARACTER ATTRIBUTES
  public String name;
  public int defaultSpeed;
  public int speed;
  public int maxLife;
  public int life;
  public int maxMana;
  public int mana;
  public int ammo;
  public int level;
  public int strength;
  public int dexterity;
  public int attack;
  public int defense;
  public int exp;
  public int nextLevelExp;
  public int coin;
  public int motion1_duration;
  public int motion2_duration;
  public Entity currentWeapon;
  public Entity currentShield;
  public Entity currentLight;
  public Projectile projectile;
  public boolean boss;

  // ITEM ATRIBUTES
  public ArrayList<Entity> inventory = new ArrayList<>();
  public int value;
  public int attackValue;
  public int defenseValue;
  public String description = "";
  public int useCost;
  public int price;
  public int knockBackPower = 0;
  public boolean stackable = false;
  public int amount = 1;
  public int lightRadius;

  // tipo atual da entidade
  public int type; // usar TYPE_*

  public Entity(GamePanel gp) {
    this.gp = gp;
  }

  public int getScreenX() {
    return worldX - gp.player.worldX + gp.player.screenX;
  }

  public int getScreenY() {
    return worldY - gp.player.worldY + gp.player.screenY;
  }

  public int getLeftX() {
    return worldX + solidArea.x;
  }

  public int getRightX() {
    return worldX + solidArea.x + solidArea.width;
  }

  public int getTopY() {
    return worldY + solidArea.y;
  }

  public int getBottomY() {
    return worldY + solidArea.y + solidArea.height;
  }

  public int getCol() {
    return (worldX + solidArea.x) / gp.tileSize;
  }

  public int getRow() {
    return (worldY + solidArea.y) / gp.tileSize;
  }

  public int getCenterX() {
    return worldX + left1.getWidth() / 2;
  }

  public int getCenterY() {
    return worldY + up1.getHeight() / 2;
  }

  public int getXdistance(Entity target) {
    return Math.abs(getCenterX() - target.getCenterX());
  }

  public int getYdistance(Entity target) {
    return Math.abs(getCenterY() - target.getCenterY());
  }

  public int getTileDistance(Entity target) {
    return (getXdistance(target) + getYdistance(target)) / gp.tileSize;
  }

  public int getGoalCol(@SuppressWarnings("unused") Entity target) {
    return (gp.player.worldX + gp.player.solidArea.x) / gp.tileSize;
  }

  public int getGoalRow(@SuppressWarnings("unused") Entity target) {
    return (gp.player.worldY + gp.player.solidArea.y) / gp.tileSize;
  }

  public void resetCounter() {
    actionLockCounter = 0;
    invincibleCounter = 0;
    shotAvailableCounter = 0;
    dyingCounter = 0;
    hpBarCounter = 0;
    knockBackCounter = 0;
    guardCounter = 0;
    offBalanceCounter = 0;
  }

  public void setLoot(Entity loot) {
    this.loot = loot;
  }

  public void setAction() {
  } // hook

  public void move(@SuppressWarnings("unused") String direction) {
  } // hook

  public void damageReaction() {
  } // hook

  public void speak() {
  } // hook

  public void interact() {
  } // hook

  public void startDialogue(Entity entity, int setNum) {
    gp.gameState = gp.dialogueState;
    gp.ui.npc = entity;
    dialogueSet = setNum;
  }

  public boolean use(@SuppressWarnings("unused") Entity entity) {
    return false;
  }

  public void checkDrop() {
  }

  public void dropItem(Entity droppedItem) {
    for (int i = 0; i < gp.obj[1].length; i++) {
      if (gp.obj[gp.currentMap][i] == null) {
        gp.obj[gp.currentMap][i] = droppedItem;

        int centerX = worldX + solidArea.x + solidArea.width / 2;
        int centerY = worldY + solidArea.y + solidArea.height / 2;

        int itemHalf = gp.tileSize / 2;
        gp.obj[gp.currentMap][i].worldX = centerX - itemHalf;
        gp.obj[gp.currentMap][i].worldY = centerY - itemHalf;
        break;
      }
    }
  }

  public Color getParticleColor() {
    return null;
  }

  public int getParticleSize() {
    return 0;
  }

  public int getParticleSpeed() {
    return 0;
  }

  public int getParticleMaxLife() {
    return 0;
  }

  public void generateParticle(Entity generator, Entity target) {
    Color color = generator.getParticleColor();
    int size = generator.getParticleSize();
    int speedVal = generator.getParticleSpeed();
    int maxLifeVal = generator.getParticleMaxLife();

    Particle p1 = new Particle(gp, target, color, size, speedVal, maxLifeVal, -2, -1);
    Particle p2 = new Particle(gp, target, color, size, speedVal, maxLifeVal, 2, -1);
    Particle p3 = new Particle(gp, target, color, size, speedVal, maxLifeVal, -2, 1);
    Particle p4 = new Particle(gp, target, color, size, speedVal, maxLifeVal, 2, 1);

    gp.particleList.add(p1);
    gp.particleList.add(p2);
    gp.particleList.add(p3);
    gp.particleList.add(p4);
  }

  public void checkCollision() {
    collisionOn = false;
    gp.cChecker.checkTile(this);
    gp.cChecker.checkObject(this, false);
    gp.cChecker.checkEntity(this, gp.npc);
    gp.cChecker.checkEntity(this, gp.monster);
    gp.cChecker.checkEntity(this, gp.iTile);
    boolean contactPlayer = gp.cChecker.checKPlayer(this);

    if (this.type == TYPE_MONSTER && contactPlayer) {
      damagePlayer(attack);
    }
  }

  public void update() {
    if (sleep)
      return;

    if (knockBack) {
      handleKnockBack();
    } else if (attacking) {
      performAttack();
    } else {
      handleActionAndMovement();
    }

    handleInvincibility();
    handleShotCooldown();
    handleOffBalance();
  }

  private void handleKnockBack() {
    checkCollision();

    if (collisionOn) {
      knockBackCounter = 0;
      knockBack = false;
      speed = defaultSpeed;
    } else {
      switch (knockBackDirection) {
        case DIR_UP:
          worldY -= speed;
          break;
        case DIR_DOWN:
          worldY += speed;
          break;
        case DIR_LEFT:
          worldX -= speed;
          break;
        case DIR_RIGHT:
          worldX += speed;
          break;
        default:
          break;
      }
    }

    if (++knockBackCounter == 10) {
      knockBackCounter = 0;
      knockBack = false;
      speed = defaultSpeed;
    }
  }

  private void handleActionAndMovement() {
    setAction();
    checkCollision();

    if (!collisionOn) {
      switch (direction) {
        case DIR_UP:
          worldY -= speed;
          break;
        case DIR_DOWN:
          worldY += speed;
          break;
        case DIR_LEFT:
          worldX -= speed;
          break;
        case DIR_RIGHT:
          worldX += speed;
          break;
        default:
          break;
      }
    }

    if (++spriteCounter > 24) {
      spriteNum = (spriteNum == 1) ? 2 : 1;
      spriteCounter = 0;
    }
  }

  private void handleInvincibility() {
    if (invincible && ++invincibleCounter > 20) {
      invincible = false;
      invincibleCounter = 0;
    }
  }

  private void handleShotCooldown() {
    if (shotAvailableCounter < 30)
      shotAvailableCounter++;
  }

  private void handleOffBalance() {
    if (offBalance && ++offBalanceCounter > 60) {
      offBalance = false;
      offBalanceCounter = 0;
    }
  }

  public void checkAttackOrNot(int rate, int straight, int horizontal) {
    if (isTargetInFront(gp.player, straight, horizontal)) {
      // chance de atacar
      if (RNG.nextInt(rate) == 0) {
        attacking = true;
        spriteNum = 1;
        spriteCounter = 0;
        shotAvailableCounter = 0;
      }
    }
  }

  private boolean isTargetInFront(Entity target, int straight, int horizontal) {
    int dx = target.getCenterX() - getCenterX();
    int dy = target.getCenterY() - getCenterY();

    switch (direction) {
      case DIR_UP:
        return dy < 0 && Math.abs(dy) < straight && Math.abs(dx) < horizontal;
      case DIR_DOWN:
        return dy > 0 && Math.abs(dy) < straight && Math.abs(dx) < horizontal;
      case DIR_LEFT:
        return dx < 0 && Math.abs(dx) < straight && Math.abs(dy) < horizontal;
      case DIR_RIGHT:
        return dx > 0 && Math.abs(dx) < straight && Math.abs(dy) < horizontal;
      default:
        return false;
    }
  }

  public void checkShootOrNot(int rate, int shotInterval) {
    if (RNG.nextInt(rate) == 0 && !projectile.alive && shotAvailableCounter == shotInterval) {
      projectile.set(worldX, worldY, direction, true, this);

      for (int j = 0; j < gp.projectile[1].length; j++) {
        if (gp.projectile[gp.currentMap][j] == null) {
          gp.projectile[gp.currentMap][j] = projectile;
          break;
        }
      }
      shotAvailableCounter = 0;
    }
  }

  public void checkStartChasingOrNot(Entity target, int distance, int rate) {
    if (getTileDistance(target) < distance && RNG.nextInt(rate) == 0)
      onPath = true;
  }

  public void checkStopChasingOrNot(Entity target, int distance, int rate) {
    if (getTileDistance(target) > distance && RNG.nextInt(rate) == 0)
      onPath = false;
  }

  public void getRandomDirection(int interval) {
    if (++actionLockCounter > interval) {
      switch (RNG.nextInt(4)) {
        case 0:
          direction = DIR_UP;
          break;
        case 1:
          direction = DIR_DOWN;
          break;
        case 2:
          direction = DIR_LEFT;
          break;
        case 3:
          direction = DIR_RIGHT;
          break;
        default:
          break;
      }
      actionLockCounter = 0;
    }
  }

  public void moveTowardPlayer(int interval) {
    if (++actionLockCounter > interval) {
      if (getXdistance(gp.player) > getYdistance(gp.player)) {
        direction = (gp.player.getCenterX() < getCenterX()) ? DIR_LEFT : DIR_RIGHT;
      } else if (getXdistance(gp.player) < getYdistance(gp.player)) {
        direction = (gp.player.getCenterY() < getCenterY()) ? DIR_UP : DIR_DOWN;
      }
      actionLockCounter = 0;
    }
  }

  public String getOppositeDireciton(String dir) {
    switch (dir) {
      case DIR_UP:
        return DIR_DOWN;
      case DIR_DOWN:
        return DIR_UP;
      case DIR_LEFT:
        return DIR_RIGHT;
      case DIR_RIGHT:
        return DIR_LEFT;
      default:
        return dir;
    }
  }

  protected void performAttack() {
    spriteCounter++;

    if (spriteCounter <= motion1_duration) {
      spriteNum = 1;
    }

    if (spriteCounter > motion1_duration && spriteCounter <= motion2_duration) {
      spriteNum = 2;

      int currentWorldX = worldX;
      int currentWorldY = worldY;
      int solidAreaWidth = solidArea.width;
      int solidAreaHeight = solidArea.height;

      switch (direction) {
        case DIR_UP:
          worldY -= attackArea.height;
          break;
        case DIR_DOWN:
          worldY += attackArea.height;
          break;
        case DIR_LEFT:
          worldX -= attackArea.width;
          break;
        case DIR_RIGHT:
          worldX += attackArea.width;
          break;
        default:
          break;
      }

      solidArea.width = attackArea.width;
      solidArea.height = attackArea.height;

      if (type == TYPE_MONSTER) {
        if (gp.cChecker.checKPlayer(this))
          damagePlayer(attack);
      } else {
        int monsterIndex = gp.cChecker.checkEntity(this, gp.monster);
        gp.player.damageMonster(monsterIndex, this, attack, currentWeapon.knockBackPower);

        int iTileIndex = gp.cChecker.checkEntity(this, gp.iTile);
        gp.player.damageInteractiveTile(iTileIndex);

        int projectileIndex = gp.cChecker.checkEntity(this, gp.projectile);
        gp.player.damageProjectile(projectileIndex);
      }

      worldX = currentWorldX;
      worldY = currentWorldY;
      solidArea.width = solidAreaWidth;
      solidArea.height = solidAreaHeight;
    }

    if (spriteCounter > motion2_duration) {
      spriteNum = 1;
      spriteCounter = 0;
      attacking = false;
    }
  }

  public void damagePlayer(int attack) {
    if (gp.player.invincible)
      return;

    int damage = attack - gp.player.defense;
    String canGuardDirection = getOppositeDireciton(direction);

    if (gp.player.guarding && gp.player.direction.equals(canGuardDirection)) {
      if (gp.player.guardCounter < 10) {
        damage = 0;
        gp.playSE(16);
        setKnockBack(this, gp.player, knockBackPower);
        offBalance = true;
        spriteCounter -= 60;
      } else {
        damage = damage / 3;
        gp.playSE(15);
      }
    } else {
      gp.playSE(6);
      if (damage < 1)
        damage = 1;
    }

    if (damage != 0) {
      gp.player.transparent = true;
      setKnockBack(gp.player, this, knockBackPower);
    }

    gp.player.life -= damage;
    gp.player.invincible = true;
  }

  public void setKnockBack(Entity target, Entity attacker, int knockBackPower) {
    this.attacker = attacker;
    target.knockBackDirection = attacker.direction;
    target.speed += knockBackPower;
    target.knockBack = true;
  }

  public boolean inCamera() {
    return (worldX + gp.tileSize * 5 > gp.player.worldX - gp.player.screenX &&
        worldX - gp.tileSize < gp.player.worldX + gp.player.screenX &&
        worldY + gp.tileSize * 5 > gp.player.worldY - gp.player.screenY &&
        worldY - gp.tileSize < gp.player.worldY + gp.player.screenY);
  }

  // -------- DRAW --------

  public void draw(Graphics2D g2) {
    if (!inCamera())
      return;

    int x = getScreenX();
    int y = getScreenY();

    Frame f = resolveFrameAndOffsets(x, y);

    if (invincible) {
      hpBarOn = true;
      hpBarCounter = 0;
      changeAlpha(g2, 0.4f);
    }

    if (dying) {
      dyingAnimation(g2);
    }

    g2.drawImage(f.img, f.x, f.y, null);
    changeAlpha(g2, 1f);
  }

  // Reduz complexidade do draw: encapsula escolha de sprite e deslocamentos
  private Frame resolveFrameAndOffsets(int baseX, int baseY) {
    BufferedImage img = null;
    int x = baseX;
    int y = baseY;

    switch (direction) {
      case DIR_UP:
        if (attacking) {
          y = baseY - up1.getHeight();
          img = (spriteNum == 1) ? attackUp1 : attackUp2;
        } else {
          img = (spriteNum == 1) ? up1 : up2;
        }
        break;
      case DIR_DOWN:
        img = attacking ? ((spriteNum == 1) ? attackDown1 : attackDown2)
            : ((spriteNum == 1) ? down1 : down2);
        break;
      case DIR_LEFT:
        if (attacking) {
          x = baseX - left1.getWidth();
          img = (spriteNum == 1) ? attackLeft1 : attackLeft2;
        } else {
          img = (spriteNum == 1) ? left1 : left2;
        }
        break;
      case DIR_RIGHT:
        img = attacking ? ((spriteNum == 1) ? attackRight1 : attackRight2)
            : ((spriteNum == 1) ? right1 : right2);
        break;
      default:
        break;
    }
    return new Frame(img, x, y);
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

  public void dyingAnimation(Graphics2D g2) {
    // Pisca a cada 5 frames; após 8 fases, marca como morto.
    dyingCounter++;
    int phase = dyingCounter / 5; // 0..?
    boolean visible = (phase % 2) == 1; // alterna visibilidade

    changeAlpha(g2, visible ? 1f : 0f);

    if (phase > 8) {
      alive = false;
    }
  }

  public void changeAlpha(Graphics2D g2, float alphaValue) {
    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alphaValue));
  }

  public BufferedImage setup(String imagePath, int width, int height) {
    UtilityTool uTool = new UtilityTool();
    BufferedImage img = null;

    try {
      img = ImageIO.read(getClass().getResourceAsStream(imagePath + ".png"));
      img = uTool.scaleImage(img, width, height);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return img;
  }

  public void searchPath(int goalCol, int goalRow) {
    int startCol = (worldX + solidArea.x) / gp.tileSize;
    int startRow = (worldY + solidArea.y) / gp.tileSize;

    gp.pFinder.setNodes(startCol, startRow, goalCol, goalRow);

    if (!gp.pFinder.search())
      return;

    int nextX = gp.pFinder.pathList.get(0).getCol() * gp.tileSize;
    int nextY = gp.pFinder.pathList.get(0).getRow() * gp.tileSize;

    int enLeftX = worldX + solidArea.x;
    int enRightX = worldX + solidArea.x + solidArea.width;
    int enTopY = worldY + solidArea.y;
    int enBottomY = worldY + solidArea.y + solidArea.height;

    boolean alignedVertical = (enLeftX >= nextX) && (enRightX < nextX + gp.tileSize);
    boolean alignedHorizontal = (enTopY >= nextY) && (enBottomY < nextY + gp.tileSize);

    if (enTopY > nextY && alignedVertical) {
      direction = DIR_UP;
    } else if (enTopY < nextY && alignedVertical) {
      direction = DIR_DOWN;
    } else if (enLeftX > nextX && alignedHorizontal) {
      direction = DIR_LEFT;
    } else if (enLeftX < nextX && alignedHorizontal) {
      direction = DIR_RIGHT;
    } else {
      // Diagonal: escolhe eixo dominante e usa fallback se colidir
      int centerX = (enLeftX + enRightX) / 2;
      int centerY = (enTopY + enBottomY) / 2;
      int targetCenterX = nextX + gp.tileSize / 2;
      int targetCenterY = nextY + gp.tileSize / 2;

      int dx = centerX - targetCenterX;
      int dy = centerY - targetCenterY;

      if (Math.abs(dx) > Math.abs(dy)) {
        direction = (dx > 0) ? DIR_LEFT : DIR_RIGHT;
        checkCollision();
        if (collisionOn)
          direction = (dy > 0) ? DIR_UP : DIR_DOWN;
      } else {
        direction = (dy > 0) ? DIR_UP : DIR_DOWN;
        checkCollision();
        if (collisionOn)
          direction = (dx > 0) ? DIR_LEFT : DIR_RIGHT;
      }
    }

    int nextCol = gp.pFinder.pathList.get(0).getCol();
    int nextRow = gp.pFinder.pathList.get(0).getRow();
    if (nextCol == goalCol && nextRow == goalRow)
      onPath = false;
  }

  public int getDetected(Entity user, Entity[][] target, String targetName) {
    int nextWorldX = user.getLeftX();
    int nextWorldY = user.getTopY();

    switch (user.direction) {
      case DIR_UP:
        nextWorldY = user.getTopY() - gp.player.speed;
        break;
      case DIR_DOWN:
        nextWorldY = user.getBottomY() + gp.player.speed;
        break;
      case DIR_LEFT:
        nextWorldX = user.getLeftX() - gp.player.speed;
        break;
      case DIR_RIGHT:
        nextWorldX = user.getRightX() + gp.player.speed;
        break;
      default:
        break;
    }

    int col = nextWorldX / gp.tileSize;
    int row = nextWorldY / gp.tileSize;

    for (int i = 0; i < target[gp.currentMap].length; i++) {
      Entity t = target[gp.currentMap][i];
      if (t != null && t.getCol() == col && t.getRow() == row && targetName.equals(t.name)) {
        return i;
      }
    }
    return 999;
  }
}

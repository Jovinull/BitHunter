package entity;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import javax.imageio.ImageIO;

import main.GamePanel;
import main.UtilityTool;

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

  public static final int MAX_INVENTORY_SIZE = 20;

  private static final Random RNG = new Random();

  protected final GamePanel gp;

  // Sprites de movimento (visíveis para subclasses, não públicos)
  protected BufferedImage up1;
  protected BufferedImage up2;
  protected BufferedImage down1;
  protected BufferedImage down2;
  protected BufferedImage left1;
  protected BufferedImage left2;
  protected BufferedImage right1;
  protected BufferedImage right2;

  // Sprites de ataque/guarda
  protected BufferedImage attackUp1;
  protected BufferedImage attackUp2;
  protected BufferedImage attackDown1;
  protected BufferedImage attackDown2;
  protected BufferedImage attackLeft1;
  protected BufferedImage attackLeft2;
  protected BufferedImage attackRight1;
  protected BufferedImage attackRight2;
  protected BufferedImage guardUp;
  protected BufferedImage guardDown;
  protected BufferedImage guardLeft;
  protected BufferedImage guardRight;

  // Sprites genéricos
  protected BufferedImage image;
  protected BufferedImage image2;
  protected BufferedImage image3;

  // Áreas de colisão/ataque
  private final Rectangle solidArea = new Rectangle(0, 0, 48, 48);
  private final Rectangle attackArea = new Rectangle(0, 0, 0, 0);

  private int solidAreaDefaultX;
  private int solidAreaDefaultY;

  private boolean collision = false;

  // Diálogos (expostos via getter para UI/consumo externo)
  private final String[][] dialogues = new String[20][20];

  private Entity attacker;
  private Entity linkedEntity;
  private boolean temp = false;

  // STATE
  private int worldX;
  private int worldY;
  private String direction = DIR_DOWN;
  private int spriteNum = 1;
  private int dialogueSet = 0;
  private int dialogueIndex = 0;
  private boolean collisionOn = false;
  private boolean invincible = false;
  private boolean attacking = false;
  private boolean alive = true;
  private boolean dying = false;
  private boolean hpBarOn = false;
  private boolean onPath = false;
  private boolean knockBack = false;
  private String knockBackDirection;
  private boolean guarding = false;
  private boolean transparent = false;
  private boolean offBalance = false;
  private Entity loot;
  private boolean opened = false;
  private boolean inRage = false;
  private boolean sleep = false;
  private boolean drawing = true;

  // COUNTERS
  private int spriteCounter = 0;
  private int actionLockCounter = 0;
  private int invincibleCounter = 0;
  private int shotAvailableCounter = 0;
  private int dyingCounter = 0;
  private int hpBarCounter = 0;
  private int knockBackCounter = 0;
  private int guardCounter = 0;
  private int offBalanceCounter = 0;

  // CHARACTER ATTRIBUTES
  private String name;
  private int defaultSpeed;
  private int speed;
  private int maxLife;
  private int life;
  private int maxMana;
  private int mana;
  private int ammo;
  private int level;
  private int strength;
  private int dexterity;
  private int attack;
  private int defense;
  private int exp;
  private int nextLevelExp;
  private int coin;

  private int motion1Duration;
  private int motion2Duration;

  private Entity currentWeapon;
  private Entity currentShield;
  private Entity currentLight;
  private Projectile projectile;
  private boolean boss;

  // ITEM ATRIBUTES
  private final List<Entity> inventory = new ArrayList<>();
  private int value;
  private int attackValue;
  private int defenseValue;
  private String description = "";
  private int useCost;
  private int price;
  private int knockBackPower = 0;
  private boolean stackable = false;
  private int amount = 1;
  private int lightRadius;

  private int type; // usar TYPE_*

  public Entity(GamePanel gp) {
    this.gp = gp;
  }

  // =========================
  // Helpers de posição/tela
  // =========================

  public int getScreenX() {
    return worldX - gp.player.getWorldX() + gp.player.screenX;
  }

  public int getScreenY() {
    return worldY - gp.player.getWorldY() + gp.player.screenY;
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
    return worldX + (left1 != null ? left1.getWidth() : gp.tileSize) / 2;
  }

  public int getCenterY() {
    return worldY + (up1 != null ? up1.getHeight() : gp.tileSize) / 2;
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

  public int getGoalCol(Entity target) {
    Entity t = (target != null) ? target : gp.player;
    return (t.worldX + t.solidArea.x) / gp.tileSize;
  }

  public int getGoalRow(Entity target) {
    Entity t = (target != null) ? target : gp.player;
    return (t.worldY + t.solidArea.y) / gp.tileSize;
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

  /** hook: sobrescreva em subclasses para definir ações por frame */
  public void setAction() {
    // no-op
  }

  /** hook: atualização direcional direta; padrão apenas ajusta a direção atual */
  public void move(String direction) {
    if (direction != null) {
      this.direction = direction;
    }
  }

  /** hook: reação de dano em subclasses (knockback custom etc.) */
  public void damageReaction() {
    // no-op
  }

  /** hook: diálogo em NPCs/Jogador */
  public void speak() {
    // no-op
  }

  /** hook: interação genérica (abrir baú, alavanca, etc.) */
  public void interact() {
    // no-op
  }

  public void facePlayer() {
    switch (gp.player.getDirection()) {
      case DIR_UP:
        direction = DIR_DOWN;
        break;
      case DIR_DOWN:
        direction = DIR_UP;
        break;
      case DIR_LEFT:
        direction = DIR_RIGHT;
        break;
      case DIR_RIGHT:
        direction = DIR_LEFT;
        break;
      default:
        break;
    }
  }

  public void startDialogue(Entity entity, int setNum) {
    gp.gameState = gp.dialogueState;
    gp.ui.npc = entity;
    dialogueSet = setNum;
  }

  /**
   * hook: uso/aplicação de item/entidade sobre outra entidade.
   *
   * @param entity
   *          alvo do uso (não pode ser null)
   * @return true se houve uso bem-sucedido
   * @throws NullPointerException
   *           se entity for null
   * @throws UnsupportedOperationException
   *           se esta entidade não for usável
   */
  public boolean use(Entity entity) {
    Objects.requireNonNull(entity, "entity");
    throw new UnsupportedOperationException(getClass().getSimpleName() + " não é usável.");
  }

  /** hook: lógica de drop em morte/abertura */
  public void checkDrop() {
    // no-op
  }

  public void dropItem(Entity droppedItem) {
    for (int i = 0; i < gp.obj[gp.currentMap].length; i++) {
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
    if (isTargetInFront(gp.player, straight, horizontal) && RNG.nextInt(rate) == 0) {
      attacking = true;
      spriteNum = 1;
      spriteCounter = 0;
      shotAvailableCounter = 0;
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
    if (RNG.nextInt(rate) == 0
        && projectile != null
        && !projectile.isAlive()
        && shotAvailableCounter == shotInterval) {

      projectile.set(worldX, worldY, direction, true, this);

      for (int j = 0; j < gp.projectile[gp.currentMap].length; j++) {
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

  public String getOppositeDirection(String dir) {
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

    if (spriteCounter <= motion1Duration) {
      spriteNum = 1;
    }

    if (spriteCounter > motion1Duration && spriteCounter <= motion2Duration) {
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

    if (spriteCounter > motion2Duration) {
      spriteNum = 1;
      spriteCounter = 0;
      attacking = false;
    }
  }

  public void damagePlayer(int attack) {
    if (gp.player.isInvincible()) {
      return;
    }

    int damage = attack - gp.player.getDefense();
    String canGuardDirection = getOppositeDirection(direction);

    if (gp.player.isGuarding() && gp.player.getDirection().equals(canGuardDirection)) {
      if (gp.player.getGuardCounter() < 10) {
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
      gp.player.setTransparent(true);
      setKnockBack(gp.player, this, knockBackPower);
    }

    gp.player.setLife(gp.player.getLife() - damage);
    gp.player.setInvincible(true);
  }

  public void setKnockBack(Entity target, Entity attacker, int knockBackPower) {
    this.attacker = attacker;
    target.knockBackDirection = attacker.direction;
    target.speed += knockBackPower;
    target.knockBack = true;
  }

  public boolean inCamera() {
    return (worldX + gp.tileSize * 5 > gp.player.getWorldX() - gp.player.screenX &&
        worldX - gp.tileSize < gp.player.getWorldX() + gp.player.screenX &&
        worldY + gp.tileSize * 5 > gp.player.getWorldY() - gp.player.screenY &&
        worldY - gp.tileSize < gp.player.getWorldY() + gp.player.screenY);
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

  private Frame resolveFrameAndOffsets(int baseX, int baseY) {
    BufferedImage img = null;
    int x = baseX;
    int y = baseY;

    switch (direction) {
      case DIR_UP:
        if (attacking && up1 != null) {
          y = baseY - up1.getHeight();
        }
        img = pickFrame(attackUp1, attackUp2, up1, up2);
        break;

      case DIR_DOWN:
        img = pickFrame(attackDown1, attackDown2, down1, down2);
        break;

      case DIR_LEFT:
        if (attacking && left1 != null) {
          x = baseX - left1.getWidth();
        }
        img = pickFrame(attackLeft1, attackLeft2, left1, left2);
        break;

      case DIR_RIGHT:
        img = pickFrame(attackRight1, attackRight2, right1, right2);
        break;

      default:
        img = down1;
        break;
    }
    return new Frame(img, x, y);
  }

  private BufferedImage pickFrame(BufferedImage atk1, BufferedImage atk2, BufferedImage idle1, BufferedImage idle2) {
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

  // =========================
  // PATHFINDING (refatorado)
  // =========================
  public void searchPath(int goalCol, int goalRow) {
    int startCol = getCol();
    int startRow = getRow();

    gp.pFinder.setNodes(startCol, startRow, goalCol, goalRow);

    if (!gp.pFinder.search() || gp.pFinder.pathList.isEmpty()) {
      onPath = false;
      return;
    }

    // Primeiro passo do caminho
    int nextCol = gp.pFinder.pathList.get(0).getCol();
    int nextRow = gp.pFinder.pathList.get(0).getRow();

    int nextX = nextCol * gp.tileSize;
    int nextY = nextRow * gp.tileSize;

    int enLeftX = getLeftX();
    int enRightX = getRightX();
    int enTopY = getTopY();
    int enBottomY = getBottomY();

    boolean alignedVertical = isAlignedVertical(enLeftX, enRightX, nextX);
    boolean alignedHorizontal = isAlignedHorizontal(enTopY, enBottomY, nextY);

    // Movimentos retos
    if (alignedVertical && moveVerticallyToward(enTopY, nextY)) {
      checkGoalReached(goalCol, goalRow, nextCol, nextRow);
      return;
    }
    if (alignedHorizontal && moveHorizontallyToward(enLeftX, nextX)) {
      checkGoalReached(goalCol, goalRow, nextCol, nextRow);
      return;
    }

    // Movimento diagonal com fallback
    moveDiagonallyTowards(nextX, nextY, enLeftX, enRightX, enTopY, enBottomY);

    checkGoalReached(goalCol, goalRow, nextCol, nextRow);
  }

  private boolean isAlignedVertical(int leftX, int rightX, int nextX) {
    return leftX >= nextX && rightX < nextX + gp.tileSize;
  }

  private boolean isAlignedHorizontal(int topY, int bottomY, int nextY) {
    return topY >= nextY && bottomY < nextY + gp.tileSize;
  }

  private boolean moveVerticallyToward(int enTopY, int nextY) {
    if (enTopY > nextY) {
      direction = DIR_UP;
      return true;
    } else if (enTopY < nextY) {
      direction = DIR_DOWN;
      return true;
    }
    return false;
  }

  private boolean moveHorizontallyToward(int enLeftX, int nextX) {
    if (enLeftX > nextX) {
      direction = DIR_LEFT;
      return true;
    } else if (enLeftX < nextX) {
      direction = DIR_RIGHT;
      return true;
    }
    return false;
  }

  private void moveDiagonallyTowards(int nextX, int nextY, int enLeftX, int enRightX, int enTopY, int enBottomY) {
    final int centerX = (enLeftX + enRightX) / 2;
    final int centerY = (enTopY + enBottomY) / 2;
    final int targetCenterX = nextX + gp.tileSize / 2;
    final int targetCenterY = nextY + gp.tileSize / 2;

    final int dx = centerX - targetCenterX;
    final int dy = centerY - targetCenterY;

    final boolean horizontalPrimary = Math.abs(dx) > Math.abs(dy);

    String primaryDir;
    String fallbackDir;

    if (horizontalPrimary) {
      primaryDir = horizontalDir(dx);
      fallbackDir = verticalDir(dy);
    } else {
      primaryDir = verticalDir(dy);
      fallbackDir = horizontalDir(dx);
    }

    setDirectionAndCheck(primaryDir);
    if (collisionOn) {
      direction = fallbackDir;
    }
  }

  private String horizontalDir(int dx) {
    return (dx > 0) ? DIR_LEFT : DIR_RIGHT;
  }

  private String verticalDir(int dy) {
    return (dy > 0) ? DIR_UP : DIR_DOWN;
  }

  private void setDirectionAndCheck(String dir) {
    direction = dir;
    checkCollision();
  }

  private void checkGoalReached(int goalCol, int goalRow, int nextCol, int nextRow) {
    if (nextCol == goalCol && nextRow == goalRow) {
      onPath = false;
    }
  }

  public int getDetected(Entity user, Entity[][] target, String targetName) {
    int nextWorldX = user.getLeftX();
    int nextWorldY = user.getTopY();

    switch (user.direction) {
      case DIR_UP:
        nextWorldY = user.getTopY() - gp.player.getSpeed();
        break;
      case DIR_DOWN:
        nextWorldY = user.getBottomY() + gp.player.getSpeed();
        break;
      case DIR_LEFT:
        nextWorldX = user.getLeftX() - gp.player.getSpeed();
        break;
      case DIR_RIGHT:
        nextWorldX = user.getRightX() + gp.player.getSpeed();
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

  // =========================
  // Getters/Setters essenciais
  // =========================

  public Rectangle getSolidArea() {
    return solidArea;
  }

  public Rectangle getAttackArea() {
    return attackArea;
  }

  public int getSolidAreaDefaultX() {
    return solidAreaDefaultX;
  }

  public void setSolidAreaDefaultX(int solidAreaDefaultX) {
    this.solidAreaDefaultX = solidAreaDefaultX;
  }

  public int getSolidAreaDefaultY() {
    return solidAreaDefaultY;
  }

  public void setSolidAreaDefaultY(int solidAreaDefaultY) {
    this.solidAreaDefaultY = solidAreaDefaultY;
  }

  public boolean isCollision() {
    return collision;
  }

  public void setCollision(boolean collision) {
    this.collision = collision;
  }

  public String[][] getDialogues() {
    return dialogues;
  }

  public Entity getAttacker() {
    return attacker;
  }

  public void setAttacker(Entity attacker) {
    this.attacker = attacker;
  }

  public Entity getLinkedEntity() {
    return linkedEntity;
  }

  public void setLinkedEntity(Entity linkedEntity) {
    this.linkedEntity = linkedEntity;
  }

  public boolean isTemp() {
    return temp;
  }

  public void setTemp(boolean temp) {
    this.temp = temp;
  }

  public int getWorldX() {
    return worldX;
  }

  public void setWorldX(int worldX) {
    this.worldX = worldX;
  }

  public int getWorldY() {
    return worldY;
  }

  public void setWorldY(int worldY) {
    this.worldY = worldY;
  }

  public String getDirection() {
    return direction;
  }

  public void setDirection(String direction) {
    this.direction = direction;
  }

  public int getSpriteNum() {
    return spriteNum;
  }

  public void setSpriteNum(int spriteNum) {
    this.spriteNum = spriteNum;
  }

  public int getDialogueSet() {
    return dialogueSet;
  }

  public void setDialogueSet(int dialogueSet) {
    this.dialogueSet = dialogueSet;
  }

  public int getDialogueIndex() {
    return dialogueIndex;
  }

  public void setDialogueIndex(int dialogueIndex) {
    this.dialogueIndex = dialogueIndex;
  }

  public boolean isCollisionOn() {
    return collisionOn;
  }

  public void setCollisionOn(boolean collisionOn) {
    this.collisionOn = collisionOn;
  }

  public boolean isInvincible() {
    return invincible;
  }

  public void setInvincible(boolean invincible) {
    this.invincible = invincible;
  }

  public boolean isAttacking() {
    return attacking;
  }

  public void setAttacking(boolean attacking) {
    this.attacking = attacking;
  }

  public boolean isAlive() {
    return alive;
  }

  public void setAlive(boolean alive) {
    this.alive = alive;
  }

  public boolean isDying() {
    return dying;
  }

  public void setDying(boolean dying) {
    this.dying = dying;
  }

  public boolean isHpBarOn() {
    return hpBarOn;
  }

  public void setHpBarOn(boolean hpBarOn) {
    this.hpBarOn = hpBarOn;
  }

  public boolean isOnPath() {
    return onPath;
  }

  public void setOnPath(boolean onPath) {
    this.onPath = onPath;
  }

  public boolean isKnockBack() {
    return knockBack;
  }

  public void setKnockBack(boolean knockBack) {
    this.knockBack = knockBack;
  }

  public String getKnockBackDirection() {
    return knockBackDirection;
  }

  public void setKnockBackDirection(String knockBackDirection) {
    this.knockBackDirection = knockBackDirection;
  }

  public boolean isGuarding() {
    return guarding;
  }

  public void setGuarding(boolean guarding) {
    this.guarding = guarding;
  }

  public boolean isTransparent() {
    return transparent;
  }

  public void setTransparent(boolean transparent) {
    this.transparent = transparent;
  }

  public boolean isOffBalance() {
    return offBalance;
  }

  public void setOffBalance(boolean offBalance) {
    this.offBalance = offBalance;
  }

  public Entity getLoot() {
    return loot;
  }

  public boolean isOpened() {
    return opened;
  }

  public void setOpened(boolean opened) {
    this.opened = opened;
  }

  public boolean isInRage() {
    return inRage;
  }

  public void setInRage(boolean inRage) {
    this.inRage = inRage;
  }

  public boolean isSleep() {
    return sleep;
  }

  public void setSleep(boolean sleep) {
    this.sleep = sleep;
  }

  public boolean isDrawing() {
    return drawing;
  }

  public void setDrawing(boolean drawing) {
    this.drawing = drawing;
  }

  public int getSpriteCounter() {
    return spriteCounter;
  }

  public void setSpriteCounter(int spriteCounter) {
    this.spriteCounter = spriteCounter;
  }

  public int getActionLockCounter() {
    return actionLockCounter;
  }

  public void setActionLockCounter(int actionLockCounter) {
    this.actionLockCounter = actionLockCounter;
  }

  public int getInvincibleCounter() {
    return invincibleCounter;
  }

  public void setInvincibleCounter(int invincibleCounter) {
    this.invincibleCounter = invincibleCounter;
  }

  public int getShotAvailableCounter() {
    return shotAvailableCounter;
  }

  public void setShotAvailableCounter(int shotAvailableCounter) {
    this.shotAvailableCounter = shotAvailableCounter;
  }

  public int getHpBarCounter() {
    return hpBarCounter;
  }

  public void setHpBarCounter(int hpBarCounter) {
    this.hpBarCounter = hpBarCounter;
  }

  public int getKnockBackCounter() {
    return knockBackCounter;
  }

  public void setKnockBackCounter(int knockBackCounter) {
    this.knockBackCounter = knockBackCounter;
  }

  public int getGuardCounter() {
    return guardCounter;
  }

  public void setGuardCounter(int guardCounter) {
    this.guardCounter = guardCounter;
  }

  public int getOffBalanceCounter() {
    return offBalanceCounter;
  }

  public void setOffBalanceCounter(int offBalanceCounter) {
    this.offBalanceCounter = offBalanceCounter;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getDefaultSpeed() {
    return defaultSpeed;
  }

  public void setDefaultSpeed(int defaultSpeed) {
    this.defaultSpeed = defaultSpeed;
  }

  public int getSpeed() {
    return speed;
  }

  public void setSpeed(int speed) {
    this.speed = speed;
  }

  public int getMaxLife() {
    return maxLife;
  }

  public void setMaxLife(int maxLife) {
    this.maxLife = maxLife;
  }

  public int getLife() {
    return life;
  }

  public void setLife(int life) {
    this.life = life;
  }

  public int getMaxMana() {
    return maxMana;
  }

  public void setMaxMana(int maxMana) {
    this.maxMana = maxMana;
  }

  public int getMana() {
    return mana;
  }

  public void setMana(int mana) {
    this.mana = mana;
  }

  public int getAmmo() {
    return ammo;
  }

  public void setAmmo(int ammo) {
    this.ammo = ammo;
  }

  public int getLevel() {
    return level;
  }

  public void setLevel(int level) {
    this.level = level;
  }

  public int getStrength() {
    return strength;
  }

  public void setStrength(int strength) {
    this.strength = strength;
  }

  public int getDexterity() {
    return dexterity;
  }

  public void setDexterity(int dexterity) {
    this.dexterity = dexterity;
  }

  public int getAttack() {
    return attack;
  }

  public void setAttack(int attack) {
    this.attack = attack;
  }

  public int getDefense() {
    return defense;
  }

  public void setDefense(int defense) {
    this.defense = defense;
  }

  public int getExp() {
    return exp;
  }

  public void setExp(int exp) {
    this.exp = exp;
  }

  public int getNextLevelExp() {
    return nextLevelExp;
  }

  public void setNextLevelExp(int nextLevelExp) {
    this.nextLevelExp = nextLevelExp;
  }

  public int getCoin() {
    return coin;
  }

  public void setCoin(int coin) {
    this.coin = coin;
  }

  public int getMotion1Duration() {
    return motion1Duration;
  }

  public void setMotion1Duration(int motion1Duration) {
    this.motion1Duration = motion1Duration;
  }

  public int getMotion2Duration() {
    return motion2Duration;
  }

  public void setMotion2Duration(int motion2Duration) {
    this.motion2Duration = motion2Duration;
  }

  public Entity getCurrentWeapon() {
    return currentWeapon;
  }

  public void setCurrentWeapon(Entity currentWeapon) {
    this.currentWeapon = currentWeapon;
  }

  public Entity getCurrentShield() {
    return currentShield;
  }

  public void setCurrentShield(Entity currentShield) {
    this.currentShield = currentShield;
  }

  public Entity getCurrentLight() {
    return currentLight;
  }

  public void setCurrentLight(Entity currentLight) {
    this.currentLight = currentLight;
  }

  public Projectile getProjectile() {
    return projectile;
  }

  public void setProjectile(Projectile projectile) {
    this.projectile = projectile;
  }

  public boolean isBoss() {
    return boss;
  }

  public void setBoss(boolean boss) {
    this.boss = boss;
  }

  public List<Entity> getInventory() {
    return inventory;
  }

  public int getValue() {
    return value;
  }

  public void setValue(int value) {
    this.value = value;
  }

  public int getAttackValue() {
    return attackValue;
  }

  public void setAttackValue(int attackValue) {
    this.attackValue = attackValue;
  }

  public int getDefenseValue() {
    return defenseValue;
  }

  public void setDefenseValue(int defenseValue) {
    this.defenseValue = defenseValue;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public int getUseCost() {
    return useCost;
  }

  public void setUseCost(int useCost) {
    this.useCost = useCost;
  }

  public int getPrice() {
    return price;
  }

  public void setPrice(int price) {
    this.price = price;
  }

  public int getKnockBackPower() {
    return knockBackPower;
  }

  public void setKnockBackPower(int knockBackPower) {
    this.knockBackPower = knockBackPower;
  }

  public boolean isStackable() {
    return stackable;
  }

  public void setStackable(boolean stackable) {
    this.stackable = stackable;
  }

  public int getAmount() {
    return amount;
  }

  public void setAmount(int amount) {
    this.amount = amount;
  }

  public int getLightRadius() {
    return lightRadius;
  }

  public void setLightRadius(int lightRadius) {
    this.lightRadius = lightRadius;
  }

  public int getType() {
    return type;
  }

  public void setType(int type) {
    this.type = type;
  }
}

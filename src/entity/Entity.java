package entity;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import javax.imageio.ImageIO;

import entity.components.CombatComponent;
import entity.components.MovementComponent;
import entity.components.PathfindingComponent;
import entity.components.RespawnComponent;
import main.GamePanel;
import main.LanguageManager;
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
  public static final int TYPE_BOOTS = 11;
  public static final int TYPE_HELMET = 12;
  public static final int TYPE_CHEST = 13;
  public static final int TYPE_LEGS = 14;
  public static final int TYPE_PET = 15;
  public static final int TYPE_EGG = 16;

  // =========================
  // RESPAWN
  // =========================
  private final RespawnComponent respawn;

  public static final int MAX_INVENTORY_SIZE = 20;

  private static final Random RNG = new Random();

  protected final GamePanel gp;
  private final MovementComponent movement;
  private final CombatComponent combat;
  private final PathfindingComponent pathfinding;
  private final EntityRenderer renderer;

  // Sprites de movimento
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

  // Diálogos
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
  private boolean attackDamageApplied = false;

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
  private Entity currentBoots;
  private Entity currentHelmet;
  private Entity currentChest;
  private Entity currentLegs;

  private Projectile projectile;
  private boolean boss;

  // ITEM ATRIBUTES
  private final List<Entity> inventory = new ArrayList<>();
  private int value;
  private int attackValue;
  private int defenseValue;
  private int speedValue;
  private String description = "";
  private int useCost;
  private int price;
  private int knockBackPower = 0;
  private boolean stackable = false;
  private int amount = 1;
  private int lightRadius;
  private int guardStaminaCapacity;

  private String internalId;
  private String translationKey;

  private int type; // usar TYPE_*

  public Entity(GamePanel gp) {
    this.gp = gp;
    this.respawn = new RespawnComponent(this);
    this.movement = new MovementComponent(this);
    this.combat = new CombatComponent(this);
    this.pathfinding = new PathfindingComponent(this);
    this.renderer = new EntityRenderer();
  }

  // =========================
  // Helpers de posição/tela
  // =========================

  public int getScreenX() {
    var player = gp.getPlayer();
    return worldX - player.getWorldX() + player.screenX;
  }

  public int getScreenY() {
    var player = gp.getPlayer();
    return worldY - player.getWorldY() + player.screenY;
  }

  public GamePanel getGp() {
    return gp;
  }

  public int randomInt(int bound) {
    return RNG.nextInt(bound);
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
    return (worldX + solidArea.x) / GamePanel.TILE_SIZE;
  }

  public int getRow() {
    return (worldY + solidArea.y) / GamePanel.TILE_SIZE;
  }

  public int getCenterX() {
    int width = (left1 != null ? left1.getWidth() : GamePanel.TILE_SIZE);
    return worldX + width / 2;
  }

  public int getCenterY() {
    int height = (up1 != null ? up1.getHeight() : GamePanel.TILE_SIZE);
    return worldY + height / 2;
  }

  public int getXdistance(Entity target) {
    return Math.abs(getCenterX() - target.getCenterX());
  }

  public int getYdistance(Entity target) {
    return Math.abs(getCenterY() - target.getCenterY());
  }

  public int getTileDistance(Entity target) {
    return (getXdistance(target) + getYdistance(target)) / GamePanel.TILE_SIZE;
  }

  public int getGoalCol(Entity target) {
    Entity t = (target != null) ? target : gp.getPlayer();
    return (t.worldX + t.solidArea.x) / GamePanel.TILE_SIZE;
  }

  public int getGoalRow(Entity target) {
    Entity t = (target != null) ? target : gp.getPlayer();
    return (t.worldY + t.solidArea.y) / GamePanel.TILE_SIZE;
  }

  public BufferedImage getBaseImage() {
    return image;
  }

  public BufferedImage getAltImage1() {
    return image2;
  }

  public BufferedImage getAltImage2() {
    return image3;
  }

  public BufferedImage getDown1() {
    return down1;
  }

  public void getImage() {
    // no-op
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
    // no-op
  }

  public void move(String direction) {
    if (direction != null) {
      this.direction = direction;
    }
  }

  public void damageReaction() {
    // no-op
  }

  public void speak() {
    // no-op
  }

  public void interact() {
    // no-op
  }

  public void facePlayer() {
    String playerDir = gp.getPlayer().getDirection();
    switch (playerDir) {
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
    gp.setGameState(GamePanel.DIALOGUE_STATE);
    gp.getGameUi().setNpc(entity);
    dialogueSet = setNum;
  }

  public boolean use(Entity entity) {
    Objects.requireNonNull(entity, "entity");
    throw new UnsupportedOperationException(getClass().getSimpleName() + " não é usável.");
  }

  public void checkDrop() {
    // no-op
  }

  public void dropItem(Entity droppedItem) {
    Entity[][] objects = gp.getObj();
    int currentMapIndex = gp.getCurrentMap();

    for (int i = 0; i < objects[currentMapIndex].length; i++) {
      if (objects[currentMapIndex][i] == null) {
        objects[currentMapIndex][i] = droppedItem;

        int centerX = worldX + solidArea.x + solidArea.width / 2;
        int centerY = worldY + solidArea.y + solidArea.height / 2;

        int itemHalf = GamePanel.TILE_SIZE / 2;
        droppedItem.setWorldX(centerX - itemHalf);
        droppedItem.setWorldY(centerY - itemHalf);
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

    Particle p1 = new Particle(gp, target, color, size, speedVal, maxLifeVal, new Point(-2, -1));
    Particle p2 = new Particle(gp, target, color, size, speedVal, maxLifeVal, new Point(2, -1));
    Particle p3 = new Particle(gp, target, color, size, speedVal, maxLifeVal, new Point(-2, 1));
    Particle p4 = new Particle(gp, target, color, size, speedVal, maxLifeVal, new Point(2, 1));

    var particles = gp.getParticleList();
    particles.add(p1);
    particles.add(p2);
    particles.add(p3);
    particles.add(p4);
  }

  public void checkCollision() {
    collisionOn = false;
    var collisionChecker = gp.getcChecker();

    collisionChecker.checkTile(this);
    collisionChecker.checkObject(this);
    collisionChecker.checkEntity(this, gp.getNpc());
    collisionChecker.checkEntity(this, gp.getMonster());
    collisionChecker.checkEntity(this, gp.getiTile());
    boolean contactPlayer = collisionChecker.checKPlayer(this);

    if (this.type == TYPE_MONSTER && contactPlayer) {
      damagePlayer(attack);
    }
  }

  public void update() {
    if (sleep) {
      return;
    }

    if (knockBack) {
      movement.handleKnockBack();
    } else if (attacking) {
      combat.performAttack();
    } else {
      movement.handleActionAndMovement();
    }

    movement.handleInvincibility();
    movement.handleShotCooldown();
    movement.handleOffBalance();
  }

  /**
   * Nome para exibição, já traduzido.
   * Por padrão usa translationKey + ".name".
   */
  public String getDisplayName() {
    LanguageManager lm = gp != null ? gp.getLanguageManager() : null;

    String baseKey = null;
    if (translationKey != null && !translationKey.isEmpty()) {
      baseKey = translationKey;
    } else if (internalId != null && !internalId.isEmpty()) {
      baseKey = internalId;
    }

    if (lm != null && baseKey != null) {
      return lm.text(baseKey + ".name");
    }

    return name != null ? name : "";
  }

  /**
   * Descrição para exibição, já traduzida.
   * Por padrão usa translationKey + ".desc".
   */
  public String getDisplayDescription() {
    LanguageManager lm = gp != null ? gp.getLanguageManager() : null;

    String baseKey = null;
    if (translationKey != null && !translationKey.isEmpty()) {
      baseKey = translationKey;
    } else if (internalId != null && !internalId.isEmpty()) {
      baseKey = internalId;
    }

    if (lm != null && baseKey != null) {
      return lm.text(baseKey + ".desc");
    }

    // Fallback: usa a descrição que eventualmente foi preenchida manualmente
    return description != null ? description : "";
  }

  /**
   * Helper básico de tradução para subclasses de Entity.
   */
  protected String t(String key) {
    LanguageManager lm = gp != null ? gp.getLanguageManager() : null;
    if (lm == null) {
      return key;
    }
    return lm.text(key);
  }

  protected void fillDialogueSet(int setIndex, String baseKey, int totalLines) {
    if (baseKey == null || baseKey.isBlank()) {
      return;
    }

    for (int i = 0; i < totalLines; i++) {
      dialogues[setIndex][i] = t(baseKey + ".line" + (i + 1));
    }
  }

  public void checkAttackOrNot(int rate, int straight, int horizontal) {
    movement.checkAttackOrNot(rate, straight, horizontal);
  }

  public void checkShootOrNot(int rate, int shotInterval) {
    movement.checkShootOrNot(rate, shotInterval);
  }

  public void checkStartChasingOrNot(Entity target, int distance, int rate) {
    movement.checkStartChasingOrNot(target, distance, rate);
  }

  public void checkStopChasingOrNot(Entity target, int distance, int rate) {
    movement.checkStopChasingOrNot(target, distance, rate);
  }

  public void getRandomDirection(int interval) {
    movement.getRandomDirection(interval);
  }

  public void moveTowardPlayer(int interval) {
    movement.moveTowardPlayer(interval);
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
    combat.performAttack();
  }

  public void applyOpenedAppearance() {
    if (this.image2 != null) {
      this.down1 = this.image2;
    }
  }

  public void damagePlayer(int attack) {
    combat.damagePlayer(attack);
  }

  public void setKnockBack(Entity target, Entity attacker, int knockBackPower) {
    combat.setKnockBack(target, attacker, knockBackPower);
  }

  public boolean inCamera() {
    var player = gp.getPlayer();
    int tileSize = GamePanel.TILE_SIZE;

    return (worldX + tileSize * 5 > player.getWorldX() - player.screenX &&
        worldX - tileSize < player.getWorldX() + player.screenX &&
        worldY + tileSize * 5 > player.getWorldY() - player.screenY &&
        worldY - tileSize < player.getWorldY() + player.screenY);
  }

  public void draw(Graphics2D g2) {
    renderer.draw(this, g2);
  }

  public void dyingAnimation(Graphics2D g2) {
    dyingCounter++;
    int phase = dyingCounter / 5;
    boolean visible = (phase % 2) == 1;

    changeAlpha(g2, visible ? 1f : 0f);

    if (phase > 8) {
      // Fim da animação de morte
      alive = false;
      dying = false;
      dyingCounter = 0;
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
  // PATHFINDING
  // =========================
  public void searchPath(int goalCol, int goalRow) {
    pathfinding.searchPath(goalCol, goalRow);
  }

  public int getDetected(Entity user, Entity[][] target, String targetInternalId) {
    if (targetInternalId == null || targetInternalId.isEmpty()) {
      return 999;
    }

    int nextWorldX = user.getLeftX();
    int nextWorldY = user.getTopY();

    var player = gp.getPlayer();
    int playerSpeed = player.getSpeed();

    switch (user.direction) {
      case DIR_UP:
        nextWorldY = user.getTopY() - playerSpeed;
        break;
      case DIR_DOWN:
        nextWorldY = user.getBottomY() + playerSpeed;
        break;
      case DIR_LEFT:
        nextWorldX = user.getLeftX() - playerSpeed;
        break;
      case DIR_RIGHT:
        nextWorldX = user.getRightX() + playerSpeed;
        break;
      default:
        break;
    }

    int col = nextWorldX / GamePanel.TILE_SIZE;
    int row = nextWorldY / GamePanel.TILE_SIZE;

    int currentMapIndex = gp.getCurrentMap();
    Entity[] mapTargets = target[currentMapIndex];

    return findTargetIndex(mapTargets, targetInternalId, col, row);
  }

  private int findTargetIndex(Entity[] mapTargets, String targetInternalId, int col, int row) {
    for (int i = 0; i < mapTargets.length; i++) {
      if (isMatchingTarget(mapTargets[i], targetInternalId, col, row)) {
        return i;
      }
    }
    return 999;
  }

  private boolean isMatchingTarget(Entity t, String targetInternalId, int col, int row) {
    if (t == null) {
      return false;
    }

    // Garante que está no tile certo
    if (t.getCol() != col || t.getRow() != row) {
      return false;
    }

    String candidateId = t.getInternalId();
    return candidateId != null && candidateId.equals(targetInternalId);
  }

  /**
   * Define o ponto de spawn (usado para respawn).
   */
  public void setSpawnPoint(int worldX, int worldY) {
    respawn.setSpawnPoint(worldX, worldY);
  }

  /**
   * Ativa respawn para esta entidade e define o tempo em frames.
   * Ex.: 60 * 10 = 10 segundos se o jogo roda a 60 FPS.
   */
  public void enableRespawn(int delayFrames) {
    respawn.enableRespawn(delayFrames);
  }

  public boolean isRespawnEnabled() {
    return respawn.isRespawnEnabled();
  }

  boolean isSpawning() {
    return respawn.isSpawning();
  }

  int getSpawnBlinkCounter() {
    return respawn.getSpawnBlinkCounter();
  }

  /**
   * Atualiza apenas a lógica de respawn (chamar quando alive == false).
   */
  public void updateRespawn() {
    respawn.updateRespawn();
  }

  // =========================
  // Getters/Setters
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

  public boolean isAttackDamageApplied() {
    return attackDamageApplied;
  }

  public void setAttackDamageApplied(boolean attackDamageApplied) {
    this.attackDamageApplied = attackDamageApplied;
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

  public Entity getCurrentBoots() {
    return currentBoots;
  }

  public void setCurrentBoots(Entity currentBoots) {
    this.currentBoots = currentBoots;
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

  public int getSpeedValue() {
    return speedValue;
  }

  public void setSpeedValue(int speedValue) {
    this.speedValue = speedValue;
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

  public Entity getCurrentHelmet() {
    return currentHelmet;
  }

  public void setCurrentHelmet(Entity currentHelmet) {
    this.currentHelmet = currentHelmet;
  }

  public Entity getCurrentChest() {
    return currentChest;
  }

  public void setCurrentChest(Entity currentChest) {
    this.currentChest = currentChest;
  }

  public Entity getCurrentLegs() {
    return currentLegs;
  }

  public void setCurrentLegs(Entity currentLegs) {
    this.currentLegs = currentLegs;
  }

  public String getInternalId() {
    return internalId;
  }

  public void setInternalId(String internalId) {
    this.internalId = internalId;
  }

  public String getTranslationKey() {
    return translationKey;
  }

  public void setTranslationKey(String translationKey) {
    this.translationKey = translationKey;
  }

  public int getGuardStaminaCapacity() {
    return guardStaminaCapacity;
  }

  public void setGuardStaminaCapacity(int guardStaminaCapacity) {
    this.guardStaminaCapacity = guardStaminaCapacity;
  }

  public boolean isDropHandled() {
    return respawn.isDropHandled();
  }

  public void setDropHandled(boolean dropHandled) {
    respawn.setDropHandled(dropHandled);
  }
}

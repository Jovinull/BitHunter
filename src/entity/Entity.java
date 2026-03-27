package entity;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import javax.imageio.ImageIO;

import entity.components.CombatComponent;
import entity.components.DialogueComponent;
import entity.components.EquipmentComponent;
import entity.components.InventoryComponent;
import entity.components.ItemAttributesComponent;
import entity.components.MovementComponent;
import entity.components.PathfindingComponent;
import entity.components.RespawnComponent;
import entity.components.StuckDetectorComponent;
import entity.services.ParticleService;
import main.GamePanel;
import main.UtilityTool;

// this-escape: componentes (Combat, Movement, etc.) recebem referência à entidade
// durante inicialização. Padrão component-based intencional da arquitetura.
@SuppressWarnings("this-escape")
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
  public static final int TYPE_RING = 17;
  public static final int TYPE_AMULET = 18;
  public static final int TYPE_GLOVES = 19;
  public static final int TYPE_STAFF = 20;

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
  private final StuckDetectorComponent stuckDetector;
  private final EntityRenderer renderer;
  private final InventoryComponent inventoryComponent;
  private final EquipmentComponent equipment;
  private final ItemAttributesComponent itemAttributes;
  private final DialogueComponent dialogue;
  private final ParticleService particleService;

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

  private Entity attacker;
  private Entity linkedEntity;
  private boolean temp = false;

  // STATE
  private int worldX;
  private int worldY;
  private String direction = DIR_DOWN;
  private int spriteNum = 1;
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

  private Projectile projectile;
  private boolean boss;

  // ITEM ATRIBUTES
  // mantido via componentes dedicados

  private String internalId;
  private String translationKey;

  private int type; // usar TYPE_*

  public Entity(GamePanel gp) {
    this.gp = gp;
    this.respawn = new RespawnComponent(this);
    this.movement = new MovementComponent(this);
    this.combat = new CombatComponent(this);
    this.pathfinding = new PathfindingComponent(this);
    this.stuckDetector = new StuckDetectorComponent(this);
    this.renderer = new EntityRenderer();
    this.inventoryComponent = new InventoryComponent();
    this.equipment = new EquipmentComponent();
    this.itemAttributes = new ItemAttributesComponent();
    this.dialogue = new DialogueComponent(this);
    this.particleService = new ParticleService(this);
  }

  // =========================
  // Helpers de posição/tela
  // =========================

  public final int getScreenX() {
    return worldX - gp.getCameraX();
  }

  public final int getScreenY() {
    return worldY - gp.getCameraY();
  }

  public final GamePanel getGp() {
    return gp;
  }

  public int randomInt(int bound) {
    return RNG.nextInt(bound);
  }

  public final int getLeftX() {
    return worldX + solidArea.x;
  }

  public final int getRightX() {
    return worldX + solidArea.x + solidArea.width;
  }

  public final int getTopY() {
    return worldY + solidArea.y;
  }

  public final int getBottomY() {
    return worldY + solidArea.y + solidArea.height;
  }

  public final int getCol() {
    return (worldX + solidArea.x) / GamePanel.TILE_SIZE;
  }

  public final int getRow() {
    return (worldY + solidArea.y) / GamePanel.TILE_SIZE;
  }

  public final int getCenterX() {
    int width = (left1 != null ? left1.getWidth() : GamePanel.TILE_SIZE);
    return worldX + width / 2;
  }

  public final int getCenterY() {
    int height = (up1 != null ? up1.getHeight() : GamePanel.TILE_SIZE);
    return worldY + height / 2;
  }

  public final int getXdistance(Entity target) {
    return Math.abs(getCenterX() - target.getCenterX());
  }

  public final int getYdistance(Entity target) {
    return Math.abs(getCenterY() - target.getCenterY());
  }

  public final int getTileDistance(Entity target) {
    return (getXdistance(target) + getYdistance(target)) / GamePanel.TILE_SIZE;
  }

  public final int getGoalCol(Entity target) {
    Entity t = (target != null) ? target : gp.getPlayer();
    return (t.worldX + t.solidArea.x) / GamePanel.TILE_SIZE;
  }

  public final int getGoalRow(Entity target) {
    Entity t = (target != null) ? target : gp.getPlayer();
    return (t.worldY + t.solidArea.y) / GamePanel.TILE_SIZE;
  }

  public final BufferedImage getBaseImage() {
    return image;
  }

  public final BufferedImage getAltImage1() {
    return image2;
  }

  public final BufferedImage getAltImage2() {
    return image3;
  }

  public final BufferedImage getDown1() {
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
    dialogue.startDialogue(entity, setNum);
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
    particleService.generateParticle(generator, target);
  }

  public void checkCollision() {
    collisionOn = false;
    var collisionChecker = gp.getcChecker();

    collisionChecker.checkTile(this);
    collisionChecker.checkObject(this);
    collisionChecker.checkEntity(this, gp.getNpc());
    collisionChecker.checkEntity(this, gp.getMonster());
    collisionChecker.checkEntity(this, gp.getiTile());
    boolean contactPlayer = collisionChecker.checkPlayer(this);

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
    return dialogue.getDisplayName();
  }

  /**
   * Descrição para exibição, já traduzida.
   * Por padrão usa translationKey + ".desc".
   */
  public String getDisplayDescription() {
    return dialogue.getDisplayDescription();
  }

  /**
   * Helper básico de tradução para subclasses de Entity.
   */
  protected String t(String key) {
    return dialogue.t(key);
  }

  protected void fillDialogueSet(int setIndex, String baseKey, int totalLines) {
    dialogue.fillDialogueSet(setIndex, baseKey, totalLines);
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
    int tileSize = GamePanel.TILE_SIZE;
    int camX = gp.getCameraX();
    int camY = gp.getCameraY();

    return (worldX + tileSize * 5 > camX &&
        worldX - tileSize < camX + GamePanel.SCREEN_WIDTH &&
        worldY + tileSize * 5 > camY &&
        worldY - tileSize < camY + GamePanel.SCREEN_HEIGHT);
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
    String cacheKey = imagePath + "_" + width + "x" + height;
    BufferedImage cachedImage = main.ImageCache.get(cacheKey);

    if (cachedImage != null) {
      return cachedImage;
    }

    UtilityTool uTool = new UtilityTool();
    BufferedImage img = null;

    try {
      img = ImageIO.read(getClass().getResourceAsStream(imagePath + ".png"));
      img = uTool.scaleImage(img, width, height);

      main.ImageCache.put(cacheKey, img);
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

  public final StuckDetectorComponent getStuckDetector() {
    return stuckDetector;
  }

  public final int getDetected(Entity user, Entity[][] target, String targetInternalId) {
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

  public final int getSpawnWorldX() {
    return respawn.getSpawnWorldX();
  }

  public final int getSpawnWorldY() {
    return respawn.getSpawnWorldY();
  }

  /**
   * Ativa respawn para esta entidade e define o tempo em frames.
   * Ex.: 60 * 10 = 10 segundos se o jogo roda a 60 FPS.
   */
  public void enableRespawn(int delayFrames) {
    if (respawn != null) {
      respawn.enableRespawn(delayFrames);
    }
  }

  public void disableRespawn() {
    if (respawn != null) {
      respawn.disableRespawn();
    }
  }

  public final boolean isRespawnEnabled() {
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

  public final Rectangle getSolidArea() {
    return solidArea;
  }

  public final Rectangle getAttackArea() {
    return attackArea;
  }

  public final int getSolidAreaDefaultX() {
    return solidAreaDefaultX;
  }

  public final void setSolidAreaDefaultX(int solidAreaDefaultX) {
    this.solidAreaDefaultX = solidAreaDefaultX;
  }

  public final int getSolidAreaDefaultY() {
    return solidAreaDefaultY;
  }

  public final void setSolidAreaDefaultY(int solidAreaDefaultY) {
    this.solidAreaDefaultY = solidAreaDefaultY;
  }

  public final boolean isCollision() {
    return collision;
  }

  public final void setCollision(boolean collision) {
    this.collision = collision;
  }

  public final String[][] getDialogues() {
    return dialogue.getDialogues();
  }

  public final Entity getAttacker() {
    return attacker;
  }

  public final void setAttacker(Entity attacker) {
    this.attacker = attacker;
  }

  public final Entity getLinkedEntity() {
    return linkedEntity;
  }

  public final void setLinkedEntity(Entity linkedEntity) {
    this.linkedEntity = linkedEntity;
  }

  public final boolean isTemp() {
    return temp;
  }

  public final void setTemp(boolean temp) {
    this.temp = temp;
  }

  public final int getWorldX() {
    return worldX;
  }

  public final void setWorldX(int worldX) {
    this.worldX = worldX;
  }

  public final int getWorldY() {
    return worldY;
  }

  public final void setWorldY(int worldY) {
    this.worldY = worldY;
  }

  public final String getDirection() {
    return direction;
  }

  public final void setDirection(String direction) {
    this.direction = direction;
  }

  public final int getSpriteNum() {
    return spriteNum;
  }

  public final void setSpriteNum(int spriteNum) {
    this.spriteNum = spriteNum;
  }

  public final int getDialogueSet() {
    return dialogue.getDialogueSet();
  }

  public final void setDialogueSet(int dialogueSet) {
    dialogue.setDialogueSet(dialogueSet);
  }

  public final int getDialogueIndex() {
    return dialogue.getDialogueIndex();
  }

  public final void setDialogueIndex(int dialogueIndex) {
    dialogue.setDialogueIndex(dialogueIndex);
  }

  public final boolean isCollisionOn() {
    return collisionOn;
  }

  public final void setCollisionOn(boolean collisionOn) {
    this.collisionOn = collisionOn;
  }

  public final boolean isInvincible() {
    return invincible;
  }

  public final void setInvincible(boolean invincible) {
    this.invincible = invincible;
  }

  public final boolean isAttacking() {
    return attacking;
  }

  public final void setAttacking(boolean attacking) {
    this.attacking = attacking;
  }

  public final boolean isAlive() {
    return alive;
  }

  public final void setAlive(boolean alive) {
    this.alive = alive;
  }

  public final boolean isDying() {
    return dying;
  }

  public final void setDying(boolean dying) {
    this.dying = dying;
  }

  public final boolean isHpBarOn() {
    return hpBarOn;
  }

  public final void setHpBarOn(boolean hpBarOn) {
    this.hpBarOn = hpBarOn;
  }

  public final boolean isOnPath() {
    return onPath;
  }

  public final void setOnPath(boolean onPath) {
    this.onPath = onPath;
  }

  public final boolean isKnockBack() {
    return knockBack;
  }

  public final void setKnockBack(boolean knockBack) {
    this.knockBack = knockBack;
  }

  public final String getKnockBackDirection() {
    return knockBackDirection;
  }

  public final void setKnockBackDirection(String knockBackDirection) {
    this.knockBackDirection = knockBackDirection;
  }

  public final boolean isGuarding() {
    return guarding;
  }

  public final void setGuarding(boolean guarding) {
    this.guarding = guarding;
  }

  public final boolean isTransparent() {
    return transparent;
  }

  public final void setTransparent(boolean transparent) {
    this.transparent = transparent;
  }

  public final boolean isOffBalance() {
    return offBalance;
  }

  public final void setOffBalance(boolean offBalance) {
    this.offBalance = offBalance;
  }

  public final Entity getLoot() {
    return loot;
  }

  public final boolean isOpened() {
    return opened;
  }

  public final void setOpened(boolean opened) {
    this.opened = opened;
  }

  public final boolean isInRage() {
    return inRage;
  }

  public final void setInRage(boolean inRage) {
    this.inRage = inRage;
  }

  public final boolean isSleep() {
    return sleep;
  }

  public final void setSleep(boolean sleep) {
    this.sleep = sleep;
  }

  public final boolean isDrawing() {
    return drawing;
  }

  public final void setDrawing(boolean drawing) {
    this.drawing = drawing;
  }

  public final boolean isAttackDamageApplied() {
    return attackDamageApplied;
  }

  public final void setAttackDamageApplied(boolean attackDamageApplied) {
    this.attackDamageApplied = attackDamageApplied;
  }

  public final int getSpriteCounter() {
    return spriteCounter;
  }

  public final void setSpriteCounter(int spriteCounter) {
    this.spriteCounter = spriteCounter;
  }

  public final int getActionLockCounter() {
    return actionLockCounter;
  }

  public final void setActionLockCounter(int actionLockCounter) {
    this.actionLockCounter = actionLockCounter;
  }

  public final int getInvincibleCounter() {
    return invincibleCounter;
  }

  public final void setInvincibleCounter(int invincibleCounter) {
    this.invincibleCounter = invincibleCounter;
  }

  public final int getShotAvailableCounter() {
    return shotAvailableCounter;
  }

  public final void setShotAvailableCounter(int shotAvailableCounter) {
    this.shotAvailableCounter = shotAvailableCounter;
  }

  public final int getHpBarCounter() {
    return hpBarCounter;
  }

  public final void setHpBarCounter(int hpBarCounter) {
    this.hpBarCounter = hpBarCounter;
  }

  public final int getKnockBackCounter() {
    return knockBackCounter;
  }

  public final void setKnockBackCounter(int knockBackCounter) {
    this.knockBackCounter = knockBackCounter;
  }

  public final int getGuardCounter() {
    return guardCounter;
  }

  public final void setGuardCounter(int guardCounter) {
    this.guardCounter = guardCounter;
  }

  public final int getOffBalanceCounter() {
    return offBalanceCounter;
  }

  public final void setOffBalanceCounter(int offBalanceCounter) {
    this.offBalanceCounter = offBalanceCounter;
  }

  public final String getName() {
    return name;
  }

  public final void setName(String name) {
    this.name = name;
  }

  public final int getDefaultSpeed() {
    return defaultSpeed;
  }

  public final void setDefaultSpeed(int defaultSpeed) {
    this.defaultSpeed = defaultSpeed;
  }

  public final int getSpeed() {
    return speed;
  }

  public final void setSpeed(int speed) {
    this.speed = speed;
  }

  public final int getMaxLife() {
    return maxLife;
  }

  public final void setMaxLife(int maxLife) {
    this.maxLife = maxLife;
  }

  public final int getLife() {
    return life;
  }

  public final void setLife(int life) {
    this.life = life;
  }

  public final int getMaxMana() {
    return maxMana;
  }

  public final void setMaxMana(int maxMana) {
    this.maxMana = maxMana;
  }

  public final int getMana() {
    return mana;
  }

  public final void setMana(int mana) {
    this.mana = mana;
  }

  public final int getAmmo() {
    return ammo;
  }

  public final void setAmmo(int ammo) {
    this.ammo = ammo;
  }

  public final int getLevel() {
    return level;
  }

  public final void setLevel(int level) {
    this.level = level;
  }

  public final int getStrength() {
    return strength;
  }

  public final void setStrength(int strength) {
    this.strength = strength;
  }

  public final int getDexterity() {
    return dexterity;
  }

  public final void setDexterity(int dexterity) {
    this.dexterity = dexterity;
  }

  public final int getAttack() {
    return attack;
  }

  public final void setAttack(int attack) {
    this.attack = attack;
  }

  public int getDefense() {
    return defense;
  }

  public final void setDefense(int defense) {
    this.defense = defense;
  }

  public final int getExp() {
    return exp;
  }

  public final void setExp(int exp) {
    this.exp = exp;
  }

  public final int getNextLevelExp() {
    return nextLevelExp;
  }

  public final void setNextLevelExp(int nextLevelExp) {
    this.nextLevelExp = nextLevelExp;
  }

  public final int getCoin() {
    return coin;
  }

  public final void setCoin(int coin) {
    this.coin = coin;
  }

  public final int getMotion1Duration() {
    return motion1Duration;
  }

  public final void setMotion1Duration(int motion1Duration) {
    this.motion1Duration = motion1Duration;
  }

  public final int getMotion2Duration() {
    return motion2Duration;
  }

  public final void setMotion2Duration(int motion2Duration) {
    this.motion2Duration = motion2Duration;
  }

  public final Entity getCurrentWeapon() {
    return equipment.getCurrentWeapon();
  }

  public final void setCurrentWeapon(Entity currentWeapon) {
    equipment.setCurrentWeapon(currentWeapon);
  }

  public final Entity getCurrentShield() {
    return equipment.getCurrentShield();
  }

  public final void setCurrentShield(Entity currentShield) {
    equipment.setCurrentShield(currentShield);
  }

  public final Entity getCurrentBoots() {
    return equipment.getCurrentBoots();
  }

  public final void setCurrentBoots(Entity currentBoots) {
    equipment.setCurrentBoots(currentBoots);
  }

  public final Entity getCurrentLight() {
    return equipment.getCurrentLight();
  }

  public final void setCurrentLight(Entity currentLight) {
    equipment.setCurrentLight(currentLight);
  }

  public final Projectile getProjectile() {
    return projectile;
  }

  public final void setProjectile(Projectile projectile) {
    this.projectile = projectile;
  }

  public final boolean isBoss() {
    return boss;
  }

  public final void setBoss(boolean boss) {
    this.boss = boss;
  }

  public final List<Entity> getInventory() {
    return inventoryComponent.getInventory();
  }

  public final int getValue() {
    return itemAttributes.getValue();
  }

  public final void setValue(int value) {
    itemAttributes.setValue(value);
  }

  public final int getAttackValue() {
    return itemAttributes.getAttackValue();
  }

  public final void setAttackValue(int attackValue) {
    itemAttributes.setAttackValue(attackValue);
  }

  public final int getDefenseValue() {
    return itemAttributes.getDefenseValue();
  }

  public final void setDefenseValue(int defenseValue) {
    itemAttributes.setDefenseValue(defenseValue);
  }

  public final int getSpeedValue() {
    return itemAttributes.getSpeedValue();
  }

  public final void setSpeedValue(int speedValue) {
    itemAttributes.setSpeedValue(speedValue);
  }

  public final String getDescription() {
    return itemAttributes.getDescription();
  }

  public final void setDescription(String description) {
    itemAttributes.setDescription(description);
  }

  public final int getUseCost() {
    return itemAttributes.getUseCost();
  }

  public final void setUseCost(int useCost) {
    itemAttributes.setUseCost(useCost);
  }

  public final int getPrice() {
    return itemAttributes.getPrice();
  }

  public final void setPrice(int price) {
    itemAttributes.setPrice(price);
  }

  public final int getKnockBackPower() {
    return itemAttributes.getKnockBackPower();
  }

  public final void setKnockBackPower(int knockBackPower) {
    itemAttributes.setKnockBackPower(knockBackPower);
  }

  public final boolean isStackable() {
    return itemAttributes.isStackable();
  }

  public final void setStackable(boolean stackable) {
    itemAttributes.setStackable(stackable);
  }

  public final int getAmount() {
    return itemAttributes.getAmount();
  }

  public final void setAmount(int amount) {
    itemAttributes.setAmount(amount);
  }

  public final int getLightRadius() {
    return itemAttributes.getLightRadius();
  }

  public final void setLightRadius(int lightRadius) {
    itemAttributes.setLightRadius(lightRadius);
  }

  public final int getType() {
    return type;
  }

  public final void setType(int type) {
    this.type = type;
  }

  public final Entity getCurrentHelmet() {
    return equipment.getCurrentHelmet();
  }

  public final void setCurrentHelmet(Entity currentHelmet) {
    equipment.setCurrentHelmet(currentHelmet);
  }

  public final Entity getCurrentChest() {
    return equipment.getCurrentChest();
  }

  public final void setCurrentChest(Entity currentChest) {
    equipment.setCurrentChest(currentChest);
  }

  public final Entity getCurrentLegs() {
    return equipment.getCurrentLegs();
  }

  public final void setCurrentLegs(Entity currentLegs) {
    equipment.setCurrentLegs(currentLegs);
  }

  public final Entity getCurrentRing() {
    return equipment.getCurrentRing();
  }

  public final void setCurrentRing(Entity currentRing) {
    equipment.setCurrentRing(currentRing);
  }

  public final Entity getCurrentAmulet() {
    return equipment.getCurrentAmulet();
  }

  public final void setCurrentAmulet(Entity currentAmulet) {
    equipment.setCurrentAmulet(currentAmulet);
  }

  public final Entity getCurrentGloves() {
    return equipment.getCurrentGloves();
  }

  public final void setCurrentGloves(Entity currentGloves) {
    equipment.setCurrentGloves(currentGloves);
  }

  public final String getInternalId() {
    return internalId;
  }

  public final void setInternalId(String internalId) {
    this.internalId = internalId;
  }

  public final String getTranslationKey() {
    return translationKey;
  }

  public final void setTranslationKey(String translationKey) {
    this.translationKey = translationKey;
  }

  public final int getGuardStaminaCapacity() {
    return itemAttributes.getGuardStaminaCapacity();
  }

  public final void setGuardStaminaCapacity(int guardStaminaCapacity) {
    itemAttributes.setGuardStaminaCapacity(guardStaminaCapacity);
  }

  public final boolean isDropHandled() {
    return respawn.isDropHandled();
  }

  public final void setDropHandled(boolean dropHandled) {
    respawn.setDropHandled(dropHandled);
  }
}

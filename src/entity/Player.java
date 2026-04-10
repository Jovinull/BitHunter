package entity;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import data.GameStatistics;
import main.GamePanel;
import main.KeyHandler;
import skill.SkillTree;
import object.ObjBowWood;
import object.ObjFireball;
import object.ObjKey;
import object.ObjLantern;
import object.ObjShieldJava;
import object.ObjShieldWood;
import object.ObjSwordNormal;
import pets.GreenSnailEgg;
import pets.PetEntity;
import pets.PetFactory;
import pets.YellowSnailEgg;
import tile_interactive.InteractiveTile;

// this-escape: construtor acessa getSolidArea() de Entity.
// Seguro pois Player não possui subclasses.
@SuppressWarnings("this-escape")
public class Player extends Entity {

  private final KeyHandler keyH;
  private final SkillTree skillTree = new SkillTree();

  public final int screenX;
  public final int screenY;
  private int standCounter = 0;
  private int bowCooldown = 0;

  private boolean attackCanceled = false;
  private boolean lightUpdated = false;
  private boolean debugCollision = false;
  private int stepPixelAccumulator = 0;

  /** Nome do jogador definido na criação do save. */
  private String playerName = "";
  /** Índice da classe (0=Guerreiro, 1=Ladrão, 2=Feiticeiro). */
  private int characterClass = 0;

  private PetEntity pet;

  private final List<String> ownedPets = new ArrayList<>();
  private String activePetId;

  // progresso dos pets por ID
  private final Map<String, Integer> petLevels = new HashMap<>();
  private final Map<String, Integer> petExps = new HashMap<>();

  private int guardStamina;
  private int maxGuardStamina;
  private int guardStaminaRecoveryCounter = 0;
  private int guardReleaseCounter = 0;

  // % da XP do monstro que vai para o pet (2% na sua config)
  private static final int PET_XP_PERCENT = 2;

  // contador para XP passivo do pet com o tempo
  private int petTimeCounter = 0;

  private static final int GUARD_STAMINA_DRAIN_INTERVAL = 4;
  private static final int GUARD_STAMINA_DRAIN_AMOUNT = 1;
  private static final int GUARD_STAMINA_RECOVERY_INTERVAL = 6;
  private static final int GUARD_STAMINA_RECOVERY_AMOUNT = 1;
  private static final int GUARD_STAMINA_RECOVERY_DELAY = 30;

  // ====================================================================
  // Escudo do Compilador (Java) — estado de runtime (transiente)
  // Estes contadores NÃO precisam ser salvos: resetam ao carregar o jogo,
  // o que é comportamento padrão para cooldowns de equipamento.
  // ====================================================================
  /** Contador de hits recebidos para o Garbage Collector. */
  private int javaShieldGcHitCounter = 0;
  /** Cooldown restante do try/catch, em frames. */
  private int javaShieldTryCatchCooldown = 0;
  /** HP restante do escudo temporário gerado pelo GC. */
  private int javaShieldTempShieldHp = 0;
  /** Timer restante do escudo temporário, em frames. */
  private int javaShieldTempShieldTimer = 0;

  private int currentWeaponSlotCache = 0;
  private int currentShieldSlotCache = 0;
  private int currentBootsSlotCache = -1;

  public Player(GamePanel gp, KeyHandler keyH) {
    super(gp);
    this.keyH = keyH;

    screenX = GamePanel.SCREEN_WIDTH / 2;
    screenY = GamePanel.SCREEN_HEIGHT / 2;

    // solidArea é final -> configurar via getter
    Rectangle sa = getSolidArea();
    sa.x = 8;
    sa.y = 16;
    setSolidAreaDefaultX(sa.x);
    setSolidAreaDefaultY(sa.y);
    sa.width = 32;
    sa.height = 32;

    setDefaultValues();
  }

  public final void setDefaultValues() {
    setWorldX(GamePanel.TILE_SIZE * 50);
    setWorldY(GamePanel.TILE_SIZE * 50);

    setDefaultSpeed(3);
    recalculateSpeedFromEquipment();
    setDirection(DIR_DOWN);

    // PLAYER STATUS
    setLevel(1);
    setMaxLife(12);
    setLife(getMaxLife());
    setMaxMana(6);
    setMana(getMaxMana());
    setAmmo(10);

    setStrength(2);
    setDexterity(1);

    setExp(0);
    setNextLevelExp(10);
    setCoin(200);

    setCurrentWeapon(new ObjSwordNormal(gp));
    setCurrentShield(new ObjShieldWood(gp));
    setCurrentLight(null);
    setCurrentBoots(null);
    setProjectile(new ObjFireball(gp));
    setCurrentHelmet(null);
    setCurrentChest(null);
    setCurrentLegs(null);

    recalculateAttackStats();
    setDefense(getDefense());

    updateMaxGuardStaminaFromShield(true);

    getImage();
    getAttackImage();
    getGuardImage();
    setItems();
    setDialogue();
    initDefaultPet();
  }

  public void setDefaultPositions() {
    gp.setCurrentMap(0);
    setWorldX(GamePanel.TILE_SIZE * 50);
    setWorldY(GamePanel.TILE_SIZE * 50);
    setDirection(DIR_DOWN);
    snapPetToSelf();
  }

  /**
   * Reposiciona o pet junto do player (se houver pet ativo).
   * Deve ser chamado após qualquer teleporte instantâneo do player.
   */
  public void snapPetToSelf() {
    if (pet != null) {
      pet.snapToOwner();
    }
  }

  public void setDialogue() {
    String[][] d = getDialogues();
    d[0][0] = String.format(t("player.levelUp.dialog.0.line1"), getLevel());
  }

  public void restoreStatus() {
    setLife(getMaxLife());
    setMana(getMaxMana());
    recalculateSpeedFromEquipment();
    setInvincible(false);
    setTransparent(false);
    setAttacking(false);
    setGuarding(false);
    setKnockBack(false);
    setLightUpdated(true);

    updateMaxGuardStaminaFromShield(true);
  }

  public void setItems() {
    var inv = getInventory();
    inv.clear();
    inv.add(getCurrentWeapon());
    inv.add(getCurrentShield());
    inv.add(new ObjKey(gp));
    inv.add(new ObjLantern(gp));
    inv.add(new YellowSnailEgg(gp));
    inv.add(new GreenSnailEgg(gp));
    inv.add(new ObjShieldJava(gp));
    inv.add(new ObjBowWood(gp));
  }

  public void recalculateSpeedFromEquipment() {
    int bonus = 0;

    if (getCurrentBoots() != null) {
      bonus += getCurrentBoots().getSpeedValue();
    }

    int finalSpeed = getDefaultSpeed() + bonus + skillTree.getSkillSpeedBonus();
    if (finalSpeed < 1) {
      finalSpeed = 1;
    }

    setSpeed(finalSpeed);
  }

  public void recalculateAttackStats() {
    Rectangle a = getAttackArea();
    a.width = getCurrentWeapon().getAttackArea().width;
    a.height = getCurrentWeapon().getAttackArea().height;

    setMotion1Duration(getCurrentWeapon().getMotion1Duration());
    setMotion2Duration(getCurrentWeapon().getMotion2Duration());

    int bonus = 0;
    if (getCurrentRing() != null) bonus += getCurrentRing().getAttackValue();
    if (getCurrentGloves() != null) bonus += getCurrentGloves().getAttackValue();
    if (getCurrentAmulet() != null) bonus += getCurrentAmulet().getAttackValue();

    int value = getStrength() * getCurrentWeapon().getAttackValue() + bonus
        + skillTree.getSkillAttackBonus();
    setAttack(value);
  }

  @Override
  public int getDefense() {
    int value;

    // defesa base vinda do escudo (mantém a lógica antiga)
    // Destreza via habilidades já está incorporada em getDexterity() (aplicada no desbloqueio)
    if (getCurrentShield() != null) {
      value = getDexterity() * getCurrentShield().getDefenseValue();
    } else {
      // fallback mínimo se estiver sem escudo
      value = getDexterity();
    }

    // bônus das demais peças de armadura
    if (getCurrentBoots() != null) {
      value += getCurrentBoots().getDefenseValue();
    }
    if (getCurrentHelmet() != null) {
      value += getCurrentHelmet().getDefenseValue();
    }
    if (getCurrentChest() != null) {
      value += getCurrentChest().getDefenseValue();
    }
    if (getCurrentLegs() != null) {
      value += getCurrentLegs().getDefenseValue();
    }
    if (getCurrentRing() != null) {
      value += getCurrentRing().getDefenseValue();
    }
    if (getCurrentAmulet() != null) {
      value += getCurrentAmulet().getDefenseValue();
    }
    if (getCurrentGloves() != null) {
      value += getCurrentGloves().getDefenseValue();
    }

    value += skillTree.getSkillDefenseBonus();

    setDefense(value);
    return value;
  }

  public int getCurrentWeaponSlot() {
    currentWeaponSlotCache = resolveEquipmentSlot(getCurrentWeapon(), currentWeaponSlotCache, 0);
    return currentWeaponSlotCache;
  }

  public int getCurrentShieldSlot() {
    currentShieldSlotCache = resolveEquipmentSlot(getCurrentShield(), currentShieldSlotCache, 0);
    return currentShieldSlotCache;
  }

  public int getCurrentBootsSlot() {
    currentBootsSlotCache = resolveEquipmentSlot(getCurrentBoots(), currentBootsSlotCache, -1);
    return currentBootsSlotCache;
  }

  private int resolveEquipmentSlot(Entity equippedItem, int cachedSlot, int fallbackSlot) {
    if (equippedItem == null) {
      return fallbackSlot;
    }

    var inv = getInventory();
    if (cachedSlot >= 0 && cachedSlot < inv.size() && inv.get(cachedSlot) == equippedItem) {
      return cachedSlot;
    }

    for (int i = 0; i < inv.size(); i++) {
      if (inv.get(i) == equippedItem) {
        return i;
      }
    }
    return fallbackSlot;
  }

  @Override
  public void getImage() {
    up1 = setup("/player/Walking sprites/boy_up_1", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);
    up2 = setup("/player/Walking sprites/boy_up_2", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);
    down1 = setup("/player/Walking sprites/boy_down_1", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);
    down2 = setup("/player/Walking sprites/boy_down_2", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);
    left1 = setup("/player/Walking sprites/boy_left_1", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);
    left2 = setup("/player/Walking sprites/boy_left_2", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);
    right1 = setup("/player/Walking sprites/boy_right_1", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);
    right2 = setup("/player/Walking sprites/boy_right_2", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);
  }

  public void getSleepingImage(BufferedImage image) {
    up1 = image;
    up2 = image;
    down1 = image;
    down2 = image;
    left1 = image;
    left2 = image;
    right1 = image;
    right2 = image;
  }

  public void getAttackImage() {
    if (getCurrentWeapon().getType() == TYPE_SWORD) {
      attackUp1 = setup("/player/Attacking sprites/boy_attack_up_1", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE * 2);
      attackUp2 = setup("/player/Attacking sprites/boy_attack_up_2", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE * 2);
      attackDown1 = setup("/player/Attacking sprites/boy_attack_down_1", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE * 2);
      attackDown2 = setup("/player/Attacking sprites/boy_attack_down_2", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE * 2);
      attackLeft1 = setup("/player/Attacking sprites/boy_attack_left_1", GamePanel.TILE_SIZE * 2, GamePanel.TILE_SIZE);
      attackLeft2 = setup("/player/Attacking sprites/boy_attack_left_2", GamePanel.TILE_SIZE * 2, GamePanel.TILE_SIZE);
      attackRight1 = setup("/player/Attacking sprites/boy_attack_right_1", GamePanel.TILE_SIZE * 2,
          GamePanel.TILE_SIZE);
      attackRight2 = setup("/player/Attacking sprites/boy_attack_right_2", GamePanel.TILE_SIZE * 2,
          GamePanel.TILE_SIZE);
    }

    if (getCurrentWeapon().getType() == TYPE_AXE) {
      attackUp1 = setup("/player/Attacking sprites/boy_axe_up_1", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE * 2);
      attackUp2 = setup("/player/Attacking sprites/boy_axe_up_2", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE * 2);
      attackDown1 = setup("/player/Attacking sprites/boy_axe_down_1", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE * 2);
      attackDown2 = setup("/player/Attacking sprites/boy_axe_down_2", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE * 2);
      attackLeft1 = setup("/player/Attacking sprites/boy_axe_left_1", GamePanel.TILE_SIZE * 2, GamePanel.TILE_SIZE);
      attackLeft2 = setup("/player/Attacking sprites/boy_axe_left_2", GamePanel.TILE_SIZE * 2, GamePanel.TILE_SIZE);
      attackRight1 = setup("/player/Attacking sprites/boy_axe_right_1", GamePanel.TILE_SIZE * 2, GamePanel.TILE_SIZE);
      attackRight2 = setup("/player/Attacking sprites/boy_axe_right_2", GamePanel.TILE_SIZE * 2, GamePanel.TILE_SIZE);
    }

    if (getCurrentWeapon().getType() == TYPE_PICKAXE) {
      attackUp1 = setup("/player/Attacking sprites/boy_pick_up_1", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE * 2);
      attackUp2 = setup("/player/Attacking sprites/boy_pick_up_2", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE * 2);
      attackDown1 = setup("/player/Attacking sprites/boy_pick_down_1", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE * 2);
      attackDown2 = setup("/player/Attacking sprites/boy_pick_down_2", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE * 2);
      attackLeft1 = setup("/player/Attacking sprites/boy_pick_left_1", GamePanel.TILE_SIZE * 2, GamePanel.TILE_SIZE);
      attackLeft2 = setup("/player/Attacking sprites/boy_pick_left_2", GamePanel.TILE_SIZE * 2, GamePanel.TILE_SIZE);
      attackRight1 = setup("/player/Attacking sprites/boy_pick_right_1", GamePanel.TILE_SIZE * 2, GamePanel.TILE_SIZE);
      attackRight2 = setup("/player/Attacking sprites/boy_pick_right_2", GamePanel.TILE_SIZE * 2, GamePanel.TILE_SIZE);
    }

    if (getCurrentWeapon().getType() == TYPE_STAFF) {
      attackUp1 = setup("/player/Attacking sprites/boy_staff_up_1", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE * 2);
      attackUp2 = setup("/player/Attacking sprites/boy_staff_up_2", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE * 2);
      attackDown1 = setup("/player/Attacking sprites/boy_staff_down_1", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE * 2);
      attackDown2 = setup("/player/Attacking sprites/boy_staff_down_2", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE * 2);
      attackLeft1 = setup("/player/Attacking sprites/boy_staff_left_1", GamePanel.TILE_SIZE * 2, GamePanel.TILE_SIZE);
      attackLeft2 = setup("/player/Attacking sprites/boy_staff_left_2", GamePanel.TILE_SIZE * 2, GamePanel.TILE_SIZE);
      attackRight1 = setup("/player/Attacking sprites/boy_staff_right_1", GamePanel.TILE_SIZE * 2, GamePanel.TILE_SIZE);
      attackRight2 = setup("/player/Attacking sprites/boy_staff_right_2", GamePanel.TILE_SIZE * 2, GamePanel.TILE_SIZE);
    }

    if (getCurrentWeapon().getType() == TYPE_BOW) {
      attackUp1 = setup("/player/Attacking sprites/boy_bow_up_1", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE * 2);
      attackUp2 = setup("/player/Attacking sprites/boy_bow_up_2", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE * 2);
      attackDown1 = setup("/player/Attacking sprites/boy_bow_down_1", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE * 2);
      attackDown2 = setup("/player/Attacking sprites/boy_bow_down_2", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE * 2);
      attackLeft1 = setup("/player/Attacking sprites/boy_bow_left_1", GamePanel.TILE_SIZE * 2, GamePanel.TILE_SIZE);
      attackLeft2 = setup("/player/Attacking sprites/boy_bow_left_2", GamePanel.TILE_SIZE * 2, GamePanel.TILE_SIZE);
      attackRight1 = setup("/player/Attacking sprites/boy_bow_right_1", GamePanel.TILE_SIZE * 2, GamePanel.TILE_SIZE);
      attackRight2 = setup("/player/Attacking sprites/boy_bow_right_2", GamePanel.TILE_SIZE * 2, GamePanel.TILE_SIZE);
    }
  }

  public void getGuardImage() {
    guardUp = setup("/player/Guarding sprites/boy_guard_up", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);
    guardDown = setup("/player/Guarding sprites/boy_guard_down", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);
    guardLeft = setup("/player/Guarding sprites/boy_guard_left", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);
    guardRight = setup("/player/Guarding sprites/boy_guard_right", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);
  }

  @Override
  public void update() {
    if (bowCooldown > 0) {
      bowCooldown--;
    }

    if (isKnockBack()) {
      processKnockBack();
    } else if (isAttacking()) {
      boolean wasBowAttack = getCurrentWeapon().getType() == TYPE_BOW;
      performAttack();
      if (wasBowAttack && !isAttacking()) {
        int reduction = skillTree.getSkillBowCooldownReduction();
        bowCooldown = Math.max(0, getCurrentWeapon().getMotion2Duration() - reduction);
      }
    } else if (keyH.isSpacePressed() && canGuard()) {
      handleGuard();
    } else if (isMovementOrActionPressed()) {
      handleMovementAndInteractions();
    } else {
      handleIdle();
    }

    handleShooting();
    handleInvincibilityFrames();
    updateShotCounter();
    clampVitals();
    handleDeath();
    handleGuardStaminaRegeneration();
    handlePetTimeExp();
    updateJavaShieldTimers();

    if (pet != null) {
      pet.update();
    }
  }

  private boolean hasActiveShield() {
    return getCurrentShield() != null;
  }

  private boolean isMovementOrActionPressed() {
    return keyH.isUpPressed() || keyH.isDownPressed() || keyH.isLeftPressed()
        || keyH.isRightPressed() || keyH.isEnterPressed();
  }

  private void processKnockBack() {
    setCollisionOn(false);

    var collisionChecker = gp.getcChecker();
    collisionChecker.checkTile(this);
    collisionChecker.checkObject(this);
    collisionChecker.checkEntity(this, gp.getNpc());
    collisionChecker.checkEntity(this, gp.getMonster());
    collisionChecker.checkEntity(this, gp.getiTile());

    if (isCollisionOn()) {
      setKnockBackCounter(0);
      setKnockBack(false);
      recalculateSpeedFromEquipment();
    } else {
      switch (getKnockBackDirection()) {
        case DIR_UP -> setWorldY(getWorldY() - getSpeed());
        case DIR_DOWN -> setWorldY(getWorldY() + getSpeed());
        case DIR_LEFT -> setWorldX(getWorldX() - getSpeed());
        case DIR_RIGHT -> setWorldX(getWorldX() + getSpeed());
        default -> {
          /* no-op: direção desconhecida ou nula */ }
      }
    }

    setKnockBackCounter(getKnockBackCounter() + 1);
    if (getKnockBackCounter() == 10) {
      setKnockBackCounter(0);
      setKnockBack(false);
      recalculateSpeedFromEquipment();
    }
  }

  private void handleGuard() {
    if (!canGuard()) {
      setGuarding(false);
      setGuardCounter(0);
      return;
    }

    setGuarding(true);
    setGuardCounter(getGuardCounter() + 1);

    if (getGuardCounter() % GUARD_STAMINA_DRAIN_INTERVAL == 0) {
      spendGuardStamina(GUARD_STAMINA_DRAIN_AMOUNT);
    }
  }

  private void handleMovementAndInteractions() {
    if (keyH.isUpPressed()) {
      setDirection(DIR_UP);
    } else if (keyH.isDownPressed()) {
      setDirection(DIR_DOWN);
    } else if (keyH.isLeftPressed()) {
      setDirection(DIR_LEFT);
    } else if (keyH.isRightPressed()) {
      setDirection(DIR_RIGHT);
    }

    setCollisionOn(false);

    var collisionChecker = gp.getcChecker();
    collisionChecker.checkTile(this);

    int objIndex = collisionChecker.checkObject(this);
    // Cliente: pickup de itens é autoritativo do host (Fase 7 — itens)
    if (!gp.getNetworkManager().isClient()) {
      pickUpObject(objIndex);
    }

    int npcIndex = collisionChecker.checkEntity(this, gp.getNpc());
    interactNPC(npcIndex);

    int monsterIndex = collisionChecker.checkEntity(this, gp.getMonster());
    // Cliente: contato com monstro só vira dano via reconciliação do host
    if (!gp.getNetworkManager().isClient()) {
      contactMonster(monsterIndex);
    }

    collisionChecker.checkEntity(this, gp.getiTile());
    gp.geteHandler().checkEvent();

    // Noclip: god mode (via cheat "finalclass") ignora toda colisão sólida,
    // permitindo atravessar tiles, objetos e entidades. As checagens acima
    // ainda rodam para detectar pickups, NPCs e monstros (side-effects).
    if (keyH.isGodModeOn()) {
      setCollisionOn(false);
    }

    if (!isCollisionOn() && !keyH.isEnterPressed()) {
      switch (getDirection()) {
        case DIR_UP -> setWorldY(getWorldY() - getSpeed());
        case DIR_DOWN -> setWorldY(getWorldY() + getSpeed());
        case DIR_LEFT -> setWorldX(getWorldX() - getSpeed());
        case DIR_RIGHT -> setWorldX(getWorldX() + getSpeed());
        default -> {
          /* no-op intencional: direção desconhecida/nula não move */ }
      }
      stepPixelAccumulator += getSpeed();
      if (stepPixelAccumulator >= GamePanel.TILE_SIZE) {
        gp.getGameStatistics().increment(GameStatistics.STEPS);
        stepPixelAccumulator -= GamePanel.TILE_SIZE;
      }
    }

    if (keyH.isEnterPressed() && !attackCanceled) {
      if (getCurrentWeapon().getType() == TYPE_BOW && bowCooldown > 0) {
        attackCanceled = true;
      } else {
        gp.playSE(7);
        setAttacking(true);
        setSpriteCounter(0);
      }
    }

    attackCanceled = false;
    keyH.setEnterPressed(false);

    setGuarding(false);
    setGuardCounter(0);

    setSpriteCounter(getSpriteCounter() + 1);
    if (getSpriteCounter() > 12) {
      setSpriteNum(getSpriteNum() == 1 ? 2 : 1);
      setSpriteCounter(0);
    }
  }

  private void handleIdle() {
    standCounter++;
    if (standCounter >= 20) {
      setSpriteNum(1);
      standCounter = 0;
    }
    setGuarding(false);
    setGuardCounter(0);
  }

  private void handleShooting() {
    if (keyH.isShotKeyPressed()
        && getProjectile() != null
        && !getProjectile().isAlive()
        && getShotAvailableCounter() == 30
        && getProjectile().haveResource(this)) {

      getProjectile().set(getWorldX(), getWorldY(), getDirection(), true, this);
      getProjectile().subtractResource(this);

      Entity[][] projectiles = gp.getProjectile();
      int currentMapIndex = gp.getCurrentMap();

      for (int i = 0; i < projectiles[currentMapIndex].length; i++) {
        if (projectiles[currentMapIndex][i] == null) {
          projectiles[currentMapIndex][i] = getProjectile();
          break;
        }
      }

      setShotAvailableCounter(0);
      gp.playSE(10);

      // Broadcast do projétil para jogadores remotos
      if (gp.getNetworkManager().isMultiplayer()) {
        gp.broadcastProjectileShot(getWorldX(), getWorldY(), getDirection(),
            getProjectile().getInternalId());
      }
    }
  }

  private void handleInvincibilityFrames() {
    if (isInvincible()) {
      setInvincibleCounter(getInvincibleCounter() + 1);
      if (getInvincibleCounter() > 60) {
        setInvincible(false);
        setTransparent(false);
        setInvincibleCounter(0);
      }
    }
  }

  private void updateShotCounter() {
    if (getShotAvailableCounter() < 30) {
      setShotAvailableCounter(getShotAvailableCounter() + 1);
    }
  }

  private void clampVitals() {
    if (getLife() > getMaxLife()) {
      setLife(getMaxLife());
    }
    if (getMana() > getMaxMana()) {
      setMana(getMaxMana());
    }
  }

  private void handleDeath() {
    if (!keyH.isGodModeOn() && getLife() <= 0) {
      gp.logRespawnDebug("death.start");
      gp.getGameStatistics().increment(GameStatistics.DEATHS);
      gp.stopMusic();
      gp.setGameState(GamePanel.GAME_OVER_STATE);
      gp.getGameUi().setCommandNum(-1);
      gp.playSE(12);
    }
  }

  public void pickUpObject(int i) {
    if (i < 0) {
      return;
    }

    Entity[][] objects = gp.getObj();
    int currentMapIndex = gp.getCurrentMap();
    Entity obj = objects[currentMapIndex][i];

    boolean removed = false;
    switch (obj.getType()) {
      case TYPE_PICKUP_ONLY -> {
        obj.use(this);
        objects[currentMapIndex][i] = null;
        removed = true;
        gp.getGameStatistics().increment(GameStatistics.COINS_COLLECTED);
        gp.getAchievementManager().onCoinChange(getCoin());
      }
      case TYPE_OBSTACLE -> interactObstacle(obj);
      default -> {
        String text;
        if (canObtainItem(obj)) {
          gp.playSE(1);
          gp.getGameStatistics().increment(GameStatistics.ITEMS_COLLECTED);
          String itemName = obj.getDisplayName(); // já vem traduzido
          text = String.format(t("player.message.itemObtained"), itemName);
          gp.getAchievementManager().onItemPickup(obj.getInternalId());
        } else {
          text = t("player.message.cannotCarryMore");
        }
        gp.getGameUi().addMessage(text);
        objects[currentMapIndex][i] = null;
        removed = true;
      }
    }

    // Sincroniza remoção do item para clientes (Fase 7 — itens)
    if (removed) {
      gp.broadcastEntityRemoval(3000 + i);
    }
  }

  public void interactNPC(int i) {
    if (i < 0) {
      return;
    }

    Entity[][] npcs = gp.getNpc();
    int currentMapIndex = gp.getCurrentMap();
    Entity npc = npcs[currentMapIndex][i];

    if (keyH.isEnterPressed()) {
      attackCanceled = true;
      npc.speak();
    }
    npc.move(getDirection());

    // Se o NPC foi empurrado com sucesso, libera o jogador para mover junto
    // no mesmo frame, evitando jitter de câmera por movimento alternado
    if (!npc.isCollisionOn()) {
      setCollisionOn(false);
    }
  }

  private void interactObstacle(Entity obj) {
    if (keyH.isEnterPressed()) {
      attackCanceled = true;
      obj.interact();
    }
  }

  public void contactMonster(int i) {
    if (i < 0) {
      return;
    }

    Entity[][] monsters = gp.getMonster();
    int currentMapIndex = gp.getCurrentMap();
    Entity mon = monsters[currentMapIndex][i];

    if (mon == null) {
      return;
    }

    boolean deadOrDying = !mon.isAlive()
        || mon.isDying()
        || (mon.getType() == TYPE_MONSTER && mon.getLife() <= 0);

    if (!isInvincible() && !deadOrDying) {
      gp.playSE(6);
      int damage = mon.getAttack() - getDefense();
      if (damage < 1) {
        damage = 1;
      }
      receiveFinalDamage(damage);
      setInvincible(true);
      setTransparent(true);
    }
  }

  public void damageMonster(int i, Entity attacker, int attack, int knockBackPower) {
    if (i < 0) {
      return;
    }

    Entity[][] monsters = gp.getMonster();
    int currentMapIndex = gp.getCurrentMap();
    Entity mon = monsters[currentMapIndex][i];

    if (!mon.isInvincible()) {
      gp.playSE(5);

      if (knockBackPower > 0) {
        setKnockBack(mon, attacker, knockBackPower);
      }

      if (mon.isOffBalance()) {
        attack *= 5;
      }

      int damage = attack - mon.getDefense();
      if (damage < 0) {
        damage = 0;
      }

      mon.setLife(mon.getLife() - damage);

      // --- Estatísticas: dano causado ---
      gp.getGameStatistics().add(GameStatistics.DAMAGE_DEALT, damage);

      // Mensagem de dano
      String damageMsg = String.format(t("player.message.damageDealt"), damage);
      gp.getGameUi().addMessage(damageMsg);

      mon.setInvincible(true);
      mon.damageReaction();

      if (mon.getLife() <= 0) {
        mon.setDying(true);

        // --- Estatísticas: kill ---
        gp.getGameStatistics().increment(GameStatistics.KILLS);

        String monsterName = mon.getDisplayName();

        String killMsg = String.format(t("player.message.monsterKilled"), monsterName);
        gp.getGameUi().addMessage(killMsg);

        String expMsg = String.format(t("player.message.expGained"), mon.getExp());
        gp.getGameUi().addMessage(expMsg);

        setExp(getExp() + mon.getExp());
        checkLevelUp();
        grantPetExp(mon.getExp());

        // Multiplayer: compartilha XP com jogadores remotos próximos (Fase 14)
        if (gp.getNetworkManager().isHost()) {
          gp.shareExpWithNearbyPlayers(mon.getExp(), getWorldX(), getWorldY());
        }

        // --- Bestiário: registrar kill ---
        gp.getBestiaryService().recordKill(mon.getInternalId());

        // --- Conquistas: registrar kill ---
        gp.getAchievementManager().onMonsterKill(mon.getInternalId());
      }
    }
  }

  public void damageInteractiveTile(int i) {
    if (i < 0) {
      return;
    }

    InteractiveTile[][] tiles = gp.getiTile();
    int currentMapIndex = gp.getCurrentMap();
    InteractiveTile tile = tiles[currentMapIndex][i];

    if (tile != null
        && tile.isDestructible()
        && tile.isCorrectItem(this)
        && !tile.isInvincible()) {

      tile.playSE();
      tile.setLife(tile.getLife() - 1);
      tile.setInvincible(true);

      generateParticle(tile, tile);

      if (tile.getLife() == 0) {
        tile.checkDrop();
        InteractiveTile destroyed = tile.getDestroyedForm();
        tiles[currentMapIndex][i] = destroyed;
        gp.getAchievementManager().onInteractiveTileDestroyed(tile.getInternalId());
      }
    }
  }

  public void damageProjectile(int i) {
    if (i < 0) {
      return;
    }

    Entity[][] projectiles = gp.getProjectile();
    int currentMapIndex = gp.getCurrentMap();
    Entity proj = projectiles[currentMapIndex][i];

    proj.setAlive(false);
    generateParticle(proj, proj);
  }

  public void checkLevelUp() {
    boolean leveledUp = false;

    while (getExp() >= getNextLevelExp()) {
      leveledUp = true;

      int requiredExp = getNextLevelExp();

      setExp(getExp() - requiredExp);
      setLevel(getLevel() + 1);

      int nextRequiredExp = requiredExp + 5 + (getLevel() * 3);
      setNextLevelExp(nextRequiredExp);

      setMaxLife(getMaxLife() + 3);

      setStrength(getStrength() + 1);
      if (getLevel() % 2 == 0) {
        setDexterity(getDexterity() + 1);
      }

      // Concede pontos de habilidade: 2 a cada múltiplo de 5 níveis, 1 nos demais
      int pts = (getLevel() % 5 == 0) ? 2 : 1;
      skillTree.addSkillPoints(pts);

      recalculateAttackStats();
      setDefense(getDefense());
      setLife(getMaxLife());
    }

    if (leveledUp) {
      gp.playSE(8);
      gp.getGameStatistics().increment(GameStatistics.LEVEL_UPS);
      gp.setGameState(GamePanel.DIALOGUE_STATE);
      gp.getAchievementManager().onLevelUp(getLevel());

      setDialogue();
      startDialogue(this, 0);
    }
  }

  // SELECT ITEM
  public void selectItem() {
    var ui = gp.getGameUi();
    int itemIndex = ui.getItemIndexOnSlot(
        ui.getPlayerSlotCol(),
        ui.getPlayerSlotRow());

    var inv = getInventory();
    if (itemIndex >= inv.size()) {
      return;
    }

    Entity selectedItem = inv.get(itemIndex);
    handleItemSelection(selectedItem, itemIndex);
  }

  private void handleItemSelection(Entity selectedItem, int itemIndex) {
    switch (selectedItem.getType()) {
      case TYPE_SWORD, TYPE_AXE, TYPE_PICKAXE, TYPE_STAFF, TYPE_BOW -> handleWeaponSelection(selectedItem);
      case TYPE_SHIELD -> handleShieldSelection(selectedItem);
      case TYPE_LIGHT -> handleLightSelection(selectedItem);
      case TYPE_BOOTS -> handleBootsSelection(selectedItem);
      case TYPE_HELMET -> handleHelmetSelection(selectedItem);
      case TYPE_CHEST -> handleChestSelection(selectedItem);
      case TYPE_LEGS -> handleLegsSelection(selectedItem);
      case TYPE_RING -> handleRingSelection(selectedItem);
      case TYPE_AMULET -> handleAmuletSelection(selectedItem);
      case TYPE_GLOVES -> handleGlovesSelection(selectedItem);
      case TYPE_CONSUMABLE -> handleConsumableSelection(selectedItem, itemIndex);
      default -> {
        // tipo não suportado -> ignora
      }
    }
  }

  private void handleWeaponSelection(Entity selectedItem) {
    // Não estou permitindo "desarmar" ainda – várias coisas assumem arma válida
    setCurrentWeapon(selectedItem);
    recalculateAttackStats();
    getAttackImage();
  }

  private void handleShieldSelection(Entity selectedItem) {
    if (getCurrentShield() == selectedItem) {
      setCurrentShield(null);
    } else {
      setCurrentShield(selectedItem);
    }
    setDefense(getDefense());
    updateMaxGuardStaminaFromShield(true);
  }

  private void handleLightSelection(Entity selectedItem) {
    setCurrentLight(getCurrentLight() == selectedItem ? null : selectedItem);
    setLightUpdated(true);
  }

  private void handleBootsSelection(Entity selectedItem) {
    if (getCurrentBoots() == selectedItem) {
      setCurrentBoots(null);
    } else {
      setCurrentBoots(selectedItem);
    }
    recalculateSpeedFromEquipment();
    setDefense(getDefense());
  }

  private void handleHelmetSelection(Entity selectedItem) {
    if (getCurrentHelmet() == selectedItem) {
      setCurrentHelmet(null);
    } else {
      setCurrentHelmet(selectedItem);
    }
    setDefense(getDefense());
  }

  private void handleChestSelection(Entity selectedItem) {
    if (getCurrentChest() == selectedItem) {
      setCurrentChest(null);
    } else {
      setCurrentChest(selectedItem);
    }
    setDefense(getDefense());
  }

  private void handleLegsSelection(Entity selectedItem) {
    if (getCurrentLegs() == selectedItem) {
      setCurrentLegs(null);
    } else {
      setCurrentLegs(selectedItem);
    }
    setDefense(getDefense());
  }

  private void handleRingSelection(Entity selectedItem) {
    if (getCurrentRing() == selectedItem) {
      setCurrentRing(null);
    } else {
      setCurrentRing(selectedItem);
    }
    recalculateAttackStats();
    setDefense(getDefense());
  }

  private void handleAmuletSelection(Entity selectedItem) {
    if (getCurrentAmulet() == selectedItem) {
      setCurrentAmulet(null);
    } else {
      setCurrentAmulet(selectedItem);
    }
    setDefense(getDefense());
  }

  private void handleGlovesSelection(Entity selectedItem) {
    if (getCurrentGloves() == selectedItem) {
      setCurrentGloves(null);
    } else {
      setCurrentGloves(selectedItem);
    }
    recalculateAttackStats();
    setDefense(getDefense());
  }

  private void handleConsumableSelection(Entity selectedItem, int itemIndex) {
    if (!selectedItem.use(this)) {
      return;
    }

    gp.getAchievementManager().onConsumableUsed(selectedItem.getInternalId());

    if (selectedItem.getAmount() > 1) {
      selectedItem.setAmount(selectedItem.getAmount() - 1);
    } else {
      getInventory().remove(itemIndex);
    }
  }

  public int searchItemInInventory(String itemName) {
    var inv = getInventory();
    for (int i = 0; i < inv.size(); i++) {
      if (inv.get(i).getInternalId().equals(itemName)) {
        return i;
      }
    }
    return 999;
  }

  public boolean canObtainItem(Entity item) {
    boolean canObtain = false;
    Entity newItem = gp.geteGenerator().getObject(item.getInternalId());
    var inv = getInventory();

    if (newItem.isStackable()) {
      int index = searchItemInInventory(newItem.getInternalId());
      if (index != 999) {
        inv.get(index).setAmount(inv.get(index).getAmount() + 1);
        canObtain = true;
      } else if (inv.size() != MAX_INVENTORY_SIZE) {
        inv.add(newItem);
        canObtain = true;
      }
    } else {
      if (inv.size() != MAX_INVENTORY_SIZE) {
        inv.add(newItem);
        canObtain = true;
      }
    }
    return canObtain;
  }

  @Override
  public void draw(Graphics2D g2) {
    int drawX = getWorldX() - gp.getCameraX();
    int drawY = getWorldY() - gp.getCameraY();

    if (pet != null) {
      pet.draw(g2);
    }

    // Ajuste de posição para ataques UP/LEFT
    if (isAttacking()) {
      if (DIR_UP.equals(getDirection())) {
        drawY -= GamePanel.TILE_SIZE;
      }
      if (DIR_LEFT.equals(getDirection())) {
        drawX -= GamePanel.TILE_SIZE;
      }
    }

    BufferedImage sprite = resolveSprite(getDirection(), isAttacking(), isGuarding(), getSpriteNum());

    if (isTransparent()) {
      g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
    }

    if (isDrawing()) {
      g2.drawImage(sprite, drawX, drawY, null);
    }

    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));

    if (isDebugCollision()) {
      Rectangle sa = getSolidArea();
      int baseX = getWorldX() - gp.getCameraX();
      int baseY = getWorldY() - gp.getCameraY();
      g2.setColor(Color.red);
      g2.drawRect(baseX + sa.x, baseY + sa.y, sa.width, sa.height);
    }
  }

  private BufferedImage byDir(String dir, BufferedImage up, BufferedImage down, BufferedImage left,
      BufferedImage right) {
    if (dir == null) {
      return down; // fallback seguro
    }
    return switch (dir) {
      case DIR_UP -> up;
      case DIR_DOWN -> down;
      case DIR_LEFT -> left;
      case DIR_RIGHT -> right;
      default -> down;
    };
  }

  private BufferedImage resolveSprite(String dir, boolean attacking, boolean guarding, int spriteNum) {
    if (guarding && hasActiveShield()) {
      return byDir(dir, guardUp, guardDown, guardLeft, guardRight);
    }

    final boolean first = (spriteNum == 1);

    if (attacking) {
      BufferedImage firstImg = byDir(dir, attackUp1, attackDown1, attackLeft1, attackRight1);
      BufferedImage secondImg = byDir(dir, attackUp2, attackDown2, attackLeft2, attackRight2);
      return first ? firstImg : secondImg;
    }

    BufferedImage firstImg = byDir(dir, up1, down1, left1, right1);
    BufferedImage secondImg = byDir(dir, up2, down2, left2, right2);
    return first ? firstImg : secondImg;
  }

  private int getShieldGuardCapacity() {
    Entity shield = getCurrentShield();
    if (shield == null) {
      return 0;
    }
    return shield.getGuardStaminaCapacity();
  }

  private void updateMaxGuardStaminaFromShield(boolean refill) {
    int newMax = getShieldGuardCapacity();
    setMaxGuardStamina(newMax);

    if (refill || getGuardStamina() > newMax) {
      setGuardStamina(newMax);
    }

    if (newMax <= 0) {
      setGuarding(false);
    }
  }

  private void spendGuardStamina(int amount) {
    if (amount <= 0 || getMaxGuardStamina() <= 0) {
      return;
    }

    int newValue = getGuardStamina() - amount;
    if (newValue <= 0) {
      newValue = 0;
      setGuarding(false);
    }
    setGuardStamina(newValue);
  }

  public void onGuardHit(int monsterAttack) {
    if (monsterAttack <= 0 || getMaxGuardStamina() <= 0) {
      return;
    }

    int cost = Math.max(4, monsterAttack);
    spendGuardStamina(cost);
  }

  private void handleGuardStaminaRegeneration() {
    if (getMaxGuardStamina() <= 0) {
      guardStaminaRecoveryCounter = 0;
      guardReleaseCounter = 0;
      return;
    }

    if (getGuardStamina() >= getMaxGuardStamina()) {
      guardStaminaRecoveryCounter = 0;
      guardReleaseCounter = 0;
      return;
    }

    if (keyH.isSpacePressed()) {
      guardReleaseCounter = 0;
      guardStaminaRecoveryCounter = 0;
      return;
    }

    if (isGuarding()) {
      guardStaminaRecoveryCounter = 0;
      guardReleaseCounter = 0;
      return;
    }

    if (guardReleaseCounter < GUARD_STAMINA_RECOVERY_DELAY) {
      guardReleaseCounter++;
      guardStaminaRecoveryCounter = 0;
      return;
    }

    guardStaminaRecoveryCounter++;
    if (guardStaminaRecoveryCounter >= GUARD_STAMINA_RECOVERY_INTERVAL) {
      guardStaminaRecoveryCounter = 0;
      setGuardStamina(
          Math.min(getMaxGuardStamina(), getGuardStamina() + GUARD_STAMINA_RECOVERY_AMOUNT));
    }
  }

  private boolean canGuard() {
    return hasActiveShield() && getGuardStamina() > 0;
  }

  private void handlePetTimeExp() {
    if (pet == null) {
      petTimeCounter = 0;
      return;
    }

    petTimeCounter++;
    // 1 XP de tempo a cada ~10s (600 ticks em 60 FPS)
    if (petTimeCounter >= 600) {
      petTimeCounter = 0;
      pet.addPetExp(1);
    }
  }

  private void initDefaultPet() {
    // garante que o Caracol Amarelo esteja na coleção inicial do jogador
    if (ownedPets.isEmpty()) {
      addOwnedPet(PetFactory.PET_YELLOW_SNAIL);
    }

    // se ainda não existir um pet ativo, equipa o padrão
    if (activePetId == null) {
      equipPetById(PetFactory.PET_YELLOW_SNAIL);
    } else {
      // reconstroi a instância a partir do id salvo (savegame, etc.)
      equipPetById(activePetId);
    }
  }

  // ===== Accessors =====
  public boolean isAttackCanceled() {
    return attackCanceled;
  }

  public void setAttackCanceled(boolean attackCanceled) {
    this.attackCanceled = attackCanceled;
  }

  public boolean isLightUpdated() {
    return lightUpdated;
  }

  public void setLightUpdated(boolean lightUpdated) {
    this.lightUpdated = lightUpdated;
  }

  public boolean isDebugCollision() {
    return debugCollision;
  }

  public void setDebugCollision(boolean debugCollision) {
    this.debugCollision = debugCollision;
  }

  public int getGuardStamina() {
    return guardStamina;
  }

  public void setGuardStamina(int guardStamina) {
    this.guardStamina = guardStamina;
  }

  public int getMaxGuardStamina() {
    return maxGuardStamina;
  }

  public void setMaxGuardStamina(int maxGuardStamina) {
    this.maxGuardStamina = maxGuardStamina;
  }

  public PetEntity getPet() {
    return pet;
  }

  public List<String> getOwnedPets() {
    return ownedPets;
  }

  public void addOwnedPet(String petId) {
    if (petId == null || petId.isBlank()) {
      return;
    }
    if (!ownedPets.contains(petId)) {
      ownedPets.add(petId);
    }
    // valores padrão de progresso, caso ainda não exista
    petLevels.putIfAbsent(petId, 1);
    petExps.putIfAbsent(petId, 0);
  }

  public String getActivePetId() {
    return activePetId;
  }

  /**
   * Equipa um pet pelo seu id estável (PetFactory.PET_*).
   * Retorna true em caso de sucesso.
   */
  public boolean equipPetById(String petId) {
    if (petId == null || petId.isBlank()) {
      unequipPet();
      return true;
    }

    // garante que este pet está registrado na coleção do jogador
    addOwnedPet(petId);

    PetEntity newPet = PetFactory.createPet(petId, gp, this);
    if (newPet == null) {
      return false;
    }

    int lvl = getPetLevelFor(petId);
    int xp = getPetExpFor(petId);
    newPet.setPetProgress(lvl, xp);

    // sincroniza para garantir que os mapas estejam coerentes
    onPetProgressUpdated(newPet);

    this.activePetId = petId;
    this.pet = newPet;

    pet.snapToOwner();
    return true;
  }

  public void unequipPet() {
    this.activePetId = null;
    this.pet = null;
  }

  private void grantPetExp(int monsterExp) {
    if (pet == null || monsterExp <= 0) {
      return;
    }

    int petXp = Math.max(1, monsterExp * PET_XP_PERCENT / 100);
    pet.addPetExp(petXp);
  }

  /**
   * Chamado pelo PetEntity sempre que level/XP muda.
   */
  public void onPetProgressUpdated(PetEntity petEntity) {
    if (petEntity == null) {
      return;
    }

    // Sempre que possível, usamos o internalId do pet,
    // que é o mesmo usado em PetFactory.PET_*, ownedPets e DataStorage.
    String key = petEntity.getInternalId();

    // fallback em caso de alguma configuração antiga
    if (key == null || key.isBlank()) {
      key = activePetId;
    }

    if (key == null || key.isBlank()) {
      return;
    }

    petLevels.put(key, petEntity.getPetLevel());
    petExps.put(key, petEntity.getPetExp());
  }

  public int getPetLevelFor(String petId) {
    if (petId == null || petId.isBlank()) {
      return 1;
    }
    return petLevels.getOrDefault(petId, 1);
  }

  public int getPetExpFor(String petId) {
    if (petId == null || petId.isBlank()) {
      return 0;
    }
    return petExps.getOrDefault(petId, 0);
  }

  public void clearPetProgress() {
    petLevels.clear();
    petExps.clear();
  }

  public void restorePetProgress(String petId, int level, int exp) {
    if (petId == null || petId.isBlank()) {
      return;
    }
    petLevels.put(petId, Math.max(1, level));
    petExps.put(petId, Math.max(0, exp));
  }

  /**
   * Força "pura" do personagem.
   */
  public int getAttackBaseStat() {
    return getStrength();
  }

  /**
   * Bônus de ataque vindo dos equipamentos.
   * Aqui usamos diretamente o attackValue da arma,
   * sem tentar reproduzir o dano real.
   */
  public int getAttackEquipBonusStat() {
    int bonus = 0;
    if (getCurrentWeapon() != null) bonus += getCurrentWeapon().getAttackValue();
    if (getCurrentRing() != null) bonus += getCurrentRing().getAttackValue();
    if (getCurrentGloves() != null) bonus += getCurrentGloves().getAttackValue();
    if (getCurrentAmulet() != null) bonus += getCurrentAmulet().getAttackValue();
    return bonus;
  }

  /**
   * Destreza "pura" do personagem.
   */
  public int getDefenseBaseStat() {
    return getDexterity();
  }

  /**
   * Bônus de defesa vindo das peças equipadas.
   * Também usamos somente os defenseValue dos itens.
   */
  public int getDefenseEquipBonusStat() {
    int bonus = 0;

    if (getCurrentShield() != null) {
      bonus += getCurrentShield().getDefenseValue();
    }
    if (getCurrentBoots() != null) {
      bonus += getCurrentBoots().getDefenseValue();
    }
    if (getCurrentHelmet() != null) {
      bonus += getCurrentHelmet().getDefenseValue();
    }
    if (getCurrentChest() != null) {
      bonus += getCurrentChest().getDefenseValue();
    }
    if (getCurrentLegs() != null) {
      bonus += getCurrentLegs().getDefenseValue();
    }
    if (getCurrentRing() != null) {
      bonus += getCurrentRing().getDefenseValue();
    }
    if (getCurrentAmulet() != null) {
      bonus += getCurrentAmulet().getDefenseValue();
    }
    if (getCurrentGloves() != null) {
      bonus += getCurrentGloves().getDefenseValue();
    }

    return bonus;
  }

  // ====================================================================
  // Escudo do Compilador (Java) — mecânicas especiais
  // ====================================================================

  /**
   * Verifica se o Escudo do Compilador (Java) está equipado.
   */
  public boolean isCompilerShieldEquipped() {
    Entity shield = getCurrentShield();
    return shield != null && ObjShieldJava.OBJ_ID.equals(shield.getInternalId());
  }

  /**
   * Método central para aplicar dano final ao player.
   * Integra as mecânicas especiais do Escudo do Compilador:
   * - Absorção via escudo temporário do GC
   * - try/catch (previne morte)
   * - Contagem de hits para o GC
   */
  public void receiveFinalDamage(int damage) {
    if (damage <= 0) {
      return;
    }

    // 1) Escudo temporário do GC absorve dano primeiro
    if (javaShieldTempShieldHp > 0) {
      if (damage <= javaShieldTempShieldHp) {
        javaShieldTempShieldHp -= damage;
        gp.getGameUi().addMessage(t("item.shieldJava.gc.absorbed"));
        return; // dano totalmente absorvido
      } else {
        damage -= javaShieldTempShieldHp;
        javaShieldTempShieldHp = 0;
      }
    }

    // 2) Verifica try/catch antes de aplicar dano fatal
    if (isCompilerShieldEquipped()
        && javaShieldTryCatchCooldown <= 0
        && getLife() - damage <= 0) {
      // try/catch ativado! Player sobrevive com 1 HP
      setLife(1);
      setInvincible(true);
      setTransparent(true);
      // Estende invulnerabilidade: o counter normal termina em >60,
      // então definimos um valor negativo para durar mais frames.
      setInvincibleCounter(60 - ObjShieldJava.TRY_CATCH_INVINCIBILITY_FRAMES);
      javaShieldTryCatchCooldown = ObjShieldJava.TRY_CATCH_COOLDOWN_FRAMES;
      gp.getGameUi().addMessage(t("item.shieldJava.tryCatch.activated"));
      gp.playSE(4);
      return;
    }

    // 3) Aplica dano normal
    gp.getGameStatistics().add(GameStatistics.DAMAGE_RECEIVED, damage);
    setLife(getLife() - damage);

    // 4) Conta hit para o Garbage Collector
    if (isCompilerShieldEquipped()) {
      javaShieldGcHitCounter++;
      if (javaShieldGcHitCounter >= ObjShieldJava.GC_HITS_THRESHOLD) {
        activateGarbageCollector();
      }
    }
  }

  /**
   * Ativa a passiva "Garbage Collector":
   * cura pequena + escudo temporário.
   */
  private void activateGarbageCollector() {
    javaShieldGcHitCounter = 0;

    // Cura pequena
    int heal = ObjShieldJava.GC_HEAL_AMOUNT;
    setLife(Math.min(getLife() + heal, getMaxLife()));

    // Escudo temporário
    javaShieldTempShieldHp = ObjShieldJava.GC_TEMP_SHIELD_HP;
    javaShieldTempShieldTimer = ObjShieldJava.GC_TEMP_SHIELD_DURATION_FRAMES;

    gp.getGameUi().addMessage(t("item.shieldJava.gc.activated"));
  }

  /**
   * Atualiza timers do Escudo do Compilador (chamado a cada frame).
   */
  private void updateJavaShieldTimers() {
    // Cooldown do try/catch decrementa mesmo sem o escudo equipado
    // (evita exploit de trocar equipamento para resetar cooldown)
    if (javaShieldTryCatchCooldown > 0) {
      javaShieldTryCatchCooldown--;
    }

    // Timer do escudo temporário
    if (javaShieldTempShieldTimer > 0) {
      javaShieldTempShieldTimer--;
      if (javaShieldTempShieldTimer <= 0) {
        javaShieldTempShieldHp = 0;
      }
    }
  }

  /**
   * Retorna o cooldown restante do try/catch (para exibição na UI/tooltip).
   */
  public int getJavaShieldTryCatchCooldown() {
    return javaShieldTryCatchCooldown;
  }

  /**
   * Retorna o HP restante do escudo temporário do GC.
   */
  public int getJavaShieldTempShieldHp() {
    return javaShieldTempShieldHp;
  }

  /**
   * Adiciona o Escudo do Compilador (Java) ao inventário do player.
   * Chamado pelo {@link main.CheatCodeManager} (código secreto "trycatch").
   */
  public void addJavaShieldToInventory() {
    Entity shield = new ObjShieldJava(gp);
    if (canObtainItem(shield)) {
      gp.getGameUi().addMessage(
          String.format(t("player.message.itemObtained"), shield.getDisplayName()));
    } else {
      gp.getGameUi().addMessage(t("player.message.cannotCarryMore"));
    }
  }

  // ===== Árvore de Habilidades =====

  public SkillTree getSkillTree() {
    return skillTree;
  }

  // ===== Identidade do jogador (slots de save) =====

  public String getPlayerName() {
    return playerName;
  }

  public void setPlayerName(String playerName) {
    this.playerName = playerName != null ? playerName : "";
  }

  public int getCharacterClass() {
    return characterClass;
  }

  public void setCharacterClass(int characterClass) {
    this.characterClass = characterClass;
  }
}

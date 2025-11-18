package entity;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import main.GamePanel;
import main.KeyHandler;
import object.ObjFireball;
import object.ObjKey;
import object.ObjLantern;
import object.ObjShieldWood;
import object.ObjSwordNormal;
import tile_interactive.InteractiveTile;

public class Player extends Entity {

  private final KeyHandler keyH;

  public final int screenX;
  public final int screenY;
  private int standCounter = 0;

  private boolean attackCanceled = false;
  private boolean lightUpdated = false;
  private boolean debugCollision = false;

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

  public void setDefaultValues() {
    setWorldX(GamePanel.TILE_SIZE * 23);
    setWorldY(GamePanel.TILE_SIZE * 21);

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

    setAttack(getAttack());
    setDefense(getDefense());

    getImage();
    getAttackImage();
    getGuardImage();
    setItems();
    setDialogue();
  }

  public void setDefaultPositions() {
    gp.setCurrentMap(0);
    setWorldX(GamePanel.TILE_SIZE * 23);
    setWorldY(GamePanel.TILE_SIZE * 21);
    setDirection(DIR_DOWN);
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
  }

  public void setItems() {
    var inv = getInventory();
    inv.clear();
    inv.add(getCurrentWeapon());
    inv.add(getCurrentShield());
    inv.add(new ObjKey(gp));
    inv.add(new ObjLantern(gp));
  }

  public void recalculateSpeedFromEquipment() {
    int bonus = 0;

    if (getCurrentBoots() != null) {
      bonus += getCurrentBoots().getSpeedValue();
    }

    int finalSpeed = getDefaultSpeed() + bonus;
    if (finalSpeed < 1) {
      finalSpeed = 1;
    }

    setSpeed(finalSpeed);
  }

  @Override
  public int getAttack() {
    Rectangle a = getAttackArea();
    a.width = getCurrentWeapon().getAttackArea().width;
    a.height = getCurrentWeapon().getAttackArea().height;

    setMotion1Duration(getCurrentWeapon().getMotion1Duration());
    setMotion2Duration(getCurrentWeapon().getMotion2Duration());

    int value = getStrength() * getCurrentWeapon().getAttackValue();
    setAttack(value);
    return value;
  }

  @Override
  public int getDefense() {
    int value = 0;

    // defesa base vinda do escudo (mantém a lógica antiga)
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

    setDefense(value);
    return value;
  }

  public int getCurrentWeaponSlot() {
    int currentWeaponSlot = 0;
    var inv = getInventory();
    for (int i = 0; i < inv.size(); i++) {
      if (inv.get(i) == getCurrentWeapon()) {
        currentWeaponSlot = i;
      }
    }
    return currentWeaponSlot;
  }

  public int getCurrentShieldSlot() {
    int currentShieldSlot = 0;
    var inv = getInventory();
    for (int i = 0; i < inv.size(); i++) {
      if (inv.get(i) == getCurrentShield()) {
        currentShieldSlot = i;
      }
    }
    return currentShieldSlot;
  }

  public int getCurrentBootsSlot() {
    int currentBootsSlot = -1;
    var inv = getInventory();
    for (int i = 0; i < inv.size(); i++) {
      if (inv.get(i) == getCurrentBoots()) {
        currentBootsSlot = i;
        break;
      }
    }
    return currentBootsSlot;
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
  }

  public void getGuardImage() {
    guardUp = setup("/player/Guarding sprites/boy_guard_up", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);
    guardDown = setup("/player/Guarding sprites/boy_guard_down", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);
    guardLeft = setup("/player/Guarding sprites/boy_guard_left", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);
    guardRight = setup("/player/Guarding sprites/boy_guard_right", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);
  }

  @Override
  public void update() {
    if (isKnockBack()) {
      processKnockBack();
    } else if (isAttacking()) {
      performAttack();
    } else if (keyH.isSpacePressed()) {
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
  }

  private boolean isMovementOrActionPressed() {
    return keyH.isUpPressed() || keyH.isDownPressed() || keyH.isLeftPressed()
        || keyH.isRightPressed() || keyH.isEnterPressed();
  }

  // renomeado (S2177)
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
    setGuarding(true);
    setGuardCounter(getGuardCounter() + 1);
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
    pickUpObject(objIndex);

    int npcIndex = collisionChecker.checkEntity(this, gp.getNpc());
    interactNPC(npcIndex);

    int monsterIndex = collisionChecker.checkEntity(this, gp.getMonster());
    contactMonster(monsterIndex);

    collisionChecker.checkEntity(this, gp.getiTile());
    gp.geteHandler().checkEvent();

    if (!isCollisionOn() && !keyH.isEnterPressed()) {
      switch (getDirection()) {
        case DIR_UP -> setWorldY(getWorldY() - getSpeed());
        case DIR_DOWN -> setWorldY(getWorldY() + getSpeed());
        case DIR_LEFT -> setWorldX(getWorldX() - getSpeed());
        case DIR_RIGHT -> setWorldX(getWorldX() + getSpeed());
        default -> {
          /* no-op intencional: direção desconhecida/nula não move */ }
      }
    }

    if (keyH.isEnterPressed() && !attackCanceled) {
      gp.playSE(7);
      setAttacking(true);
      setSpriteCounter(0);
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
      gp.stopMusic();
      gp.setGameState(GamePanel.GAME_OVER_STATE);
      gp.getGameUi().setCommandNum(-1);
      gp.playSE(12);
    }
  }

  public void pickUpObject(int i) {
    if (i == 999) {
      return;
    }

    Entity[][] objects = gp.getObj();
    int currentMapIndex = gp.getCurrentMap();
    Entity obj = objects[currentMapIndex][i];

    if (obj.getType() == TYPE_PICKUP_ONLY) {
      obj.use(this);
      objects[currentMapIndex][i] = null;
    } else if (obj.getType() == TYPE_OBSTACLE) {
      if (keyH.isEnterPressed()) {
        attackCanceled = true;
        obj.interact();
      }
    } else {
      String text;
      if (canObtainItem(obj)) {
        gp.playSE(1);
        String itemName = obj.getDisplayName(); // já vem traduzido
        text = String.format(t("player.message.itemObtained"), itemName);
      } else {
        text = t("player.message.cannotCarryMore");
      }
      gp.getGameUi().addMessage(text);
      objects[currentMapIndex][i] = null;
    }
  }

  public void interactNPC(int i) {
    if (i == 999) {
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
  }

  public void contactMonster(int i) {
    if (i == 999) {
      return;
    }

    Entity[][] monsters = gp.getMonster();
    int currentMapIndex = gp.getCurrentMap();
    Entity mon = monsters[currentMapIndex][i];

    if (!isInvincible() && !mon.isDying()) {
      gp.playSE(6);
      int damage = mon.getAttack() - getDefense();
      if (damage < 1) {
        damage = 1;
      }
      setLife(getLife() - damage);
      setInvincible(true);
      setTransparent(true);
    }
  }

  public void damageMonster(int i, Entity attacker, int attack, int knockBackPower) {
    if (i == 999) {
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

      // Mensagem de dano
      String damageMsg = String.format(t("player.message.damageDealt"), damage);
      gp.getGameUi().addMessage(damageMsg);

      mon.setInvincible(true);
      mon.damageReaction();

      if (mon.getLife() <= 0) {
        mon.setDying(true);

        String monsterName = mon.getDisplayName();

        String killMsg = String.format(t("player.message.monsterKilled"), monsterName);
        gp.getGameUi().addMessage(killMsg);

        String expMsg = String.format(t("player.message.expGained"), mon.getExp());
        gp.getGameUi().addMessage(expMsg);

        setExp(getExp() + mon.getExp());
        checkLevelUp();
      }
    }
  }

  public void damageInteractiveTile(int i) {
    if (i == 999) {
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
      }
    }
  }

  public void damageProjectile(int i) {
    if (i == 999) {
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

      setAttack(getAttack());
      setDefense(getDefense());
      setLife(getMaxLife());
    }

    if (leveledUp) {
      gp.playSE(8);
      gp.setGameState(GamePanel.DIALOGUE_STATE);

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

    switch (selectedItem.getType()) {
      case TYPE_SWORD, TYPE_AXE, TYPE_PICKAXE -> {
        setCurrentWeapon(selectedItem);
        setAttack(getAttack());
        getAttackImage();
      }
      case TYPE_SHIELD -> {
        setCurrentShield(selectedItem);
        setDefense(getDefense());
      }
      case TYPE_LIGHT -> {
        setCurrentLight(getCurrentLight() == selectedItem ? null : selectedItem);
        setLightUpdated(true);
      }
      case TYPE_BOOTS -> {
        setCurrentBoots(selectedItem);
        recalculateSpeedFromEquipment();
        setDefense(getDefense()); // boots podem dar defesa também
      }
      case TYPE_HELMET -> {
        setCurrentHelmet(selectedItem);
        setDefense(getDefense());
      }
      case TYPE_CHEST -> {
        setCurrentChest(selectedItem);
        setDefense(getDefense());
      }
      case TYPE_LEGS -> {
        setCurrentLegs(selectedItem);
        setDefense(getDefense());
      }
      case TYPE_CONSUMABLE -> handleConsumableSelection(selectedItem, itemIndex);
      default -> {
        /* tipo de item não suportado, ignora */
      }
    }
  }

  private void handleConsumableSelection(Entity selectedItem, int itemIndex) {
    if (!selectedItem.use(this)) {
      return;
    }

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
    int tempScreenX = screenX;
    int tempScreenY = screenY;

    // Ajuste de posição para ataques UP/LEFT
    if (isAttacking()) {
      if (DIR_UP.equals(getDirection())) {
        tempScreenY = screenY - GamePanel.TILE_SIZE;
      }
      if (DIR_LEFT.equals(getDirection())) {
        tempScreenX = screenX - GamePanel.TILE_SIZE;
      }
    }

    BufferedImage image = resolveSprite(getDirection(), isAttacking(), isGuarding(), getSpriteNum());

    if (isTransparent()) {
      g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
    }

    if (isDrawing()) {
      g2.drawImage(image, tempScreenX, tempScreenY, null);
    }

    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));

    if (isDebugCollision()) {
      Rectangle sa = getSolidArea();
      g2.setColor(Color.red);
      g2.drawRect(screenX + sa.x, screenY + sa.y, sa.width, sa.height);
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
    if (guarding) {
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
}

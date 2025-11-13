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

    screenX = gp.screenWidth / 2;
    screenY = gp.screenHeight / 2;

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
    setWorldX(gp.tileSize * 23);
    setWorldY(gp.tileSize * 21);

    setDefaultSpeed(3);
    setSpeed(getDefaultSpeed());
    setDirection(DIR_DOWN);

    // PLAYER STATUS
    setLevel(1);
    setMaxLife(6);
    setLife(getMaxLife());
    setMaxMana(4);
    setMana(getMaxMana());
    setAmmo(10);
    setStrength(1);
    setDexterity(1);
    setExp(0);
    setNextLevelExp(5);
    setCoin(500);

    setCurrentWeapon(new ObjSwordNormal(gp));
    setCurrentShield(new ObjShieldWood(gp));
    setCurrentLight(null);
    setProjectile(new ObjFireball(gp));

    setAttack(getAttack());
    setDefense(getDefense());

    getImage();
    getAttackImage();
    getGuardImage();
    setItems();
    setDialogue();
  }

  public void setDefaultPositions() {
    gp.currentMap = 0;
    setWorldX(gp.tileSize * 23);
    setWorldY(gp.tileSize * 21);
    setDirection(DIR_DOWN);
  }

  public void setDialogue() {
    String[][] d = getDialogues();
    d[0][0] = "Você subiu de level!\n" + "Seu level agora é " + getLevel() + "!\n"
        + "Vida máxima aumentada!\n" + "Força e Destreza aumentadas!";
  }

  public void restoreStatus() {
    setLife(getMaxLife());
    setMana(getMaxMana());
    setSpeed(getDefaultSpeed());
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
    int value = getDexterity() * getCurrentShield().getDefenseValue();
    setDefense(value);
    return value;
  }

  public int getCurrentWeaponSlot() {
    int currentWeaponSlot = 0;
    var inv = getInventory();
    for (int i = 0; i < inv.size(); i++) {
      if (inv.get(i) == getCurrentWeapon())
        currentWeaponSlot = i;
    }
    return currentWeaponSlot;
  }

  public int getCurrentShieldSlot() {
    int currentShieldSlot = 0;
    var inv = getInventory();
    for (int i = 0; i < inv.size(); i++) {
      if (inv.get(i) == getCurrentShield())
        currentShieldSlot = i;
    }
    return currentShieldSlot;
  }

  @Override
  public void getImage() {
    up1 = setup("/player/Walking sprites/boy_up_1", gp.tileSize, gp.tileSize);
    up2 = setup("/player/Walking sprites/boy_up_2", gp.tileSize, gp.tileSize);
    down1 = setup("/player/Walking sprites/boy_down_1", gp.tileSize, gp.tileSize);
    down2 = setup("/player/Walking sprites/boy_down_2", gp.tileSize, gp.tileSize);
    left1 = setup("/player/Walking sprites/boy_left_1", gp.tileSize, gp.tileSize);
    left2 = setup("/player/Walking sprites/boy_left_2", gp.tileSize, gp.tileSize);
    right1 = setup("/player/Walking sprites/boy_right_1", gp.tileSize, gp.tileSize);
    right2 = setup("/player/Walking sprites/boy_right_2", gp.tileSize, gp.tileSize);
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
      attackUp1 = setup("/player/Attacking sprites/boy_attack_up_1", gp.tileSize, gp.tileSize * 2);
      attackUp2 = setup("/player/Attacking sprites/boy_attack_up_2", gp.tileSize, gp.tileSize * 2);
      attackDown1 = setup("/player/Attacking sprites/boy_attack_down_1", gp.tileSize, gp.tileSize * 2);
      attackDown2 = setup("/player/Attacking sprites/boy_attack_down_2", gp.tileSize, gp.tileSize * 2);
      attackLeft1 = setup("/player/Attacking sprites/boy_attack_left_1", gp.tileSize * 2, gp.tileSize);
      attackLeft2 = setup("/player/Attacking sprites/boy_attack_left_2", gp.tileSize * 2, gp.tileSize);
      attackRight1 = setup("/player/Attacking sprites/boy_attack_right_1", gp.tileSize * 2, gp.tileSize);
      attackRight2 = setup("/player/Attacking sprites/boy_attack_right_2", gp.tileSize * 2, gp.tileSize);
    }

    if (getCurrentWeapon().getType() == TYPE_AXE) {
      attackUp1 = setup("/player/Attacking sprites/boy_axe_up_1", gp.tileSize, gp.tileSize * 2);
      attackUp2 = setup("/player/Attacking sprites/boy_axe_up_2", gp.tileSize, gp.tileSize * 2);
      attackDown1 = setup("/player/Attacking sprites/boy_axe_down_1", gp.tileSize, gp.tileSize * 2);
      attackDown2 = setup("/player/Attacking sprites/boy_axe_down_2", gp.tileSize, gp.tileSize * 2);
      attackLeft1 = setup("/player/Attacking sprites/boy_axe_left_1", gp.tileSize * 2, gp.tileSize);
      attackLeft2 = setup("/player/Attacking sprites/boy_axe_left_2", gp.tileSize * 2, gp.tileSize);
      attackRight1 = setup("/player/Attacking sprites/boy_axe_right_1", gp.tileSize * 2, gp.tileSize);
      attackRight2 = setup("/player/Attacking sprites/boy_axe_right_2", gp.tileSize * 2, gp.tileSize);
    }

    if (getCurrentWeapon().getType() == TYPE_PICKAXE) {
      attackUp1 = setup("/player/Attacking sprites/boy_pick_up_1", gp.tileSize, gp.tileSize * 2);
      attackUp2 = setup("/player/Attacking sprites/boy_pick_up_2", gp.tileSize, gp.tileSize * 2);
      attackDown1 = setup("/player/Attacking sprites/boy_pick_down_1", gp.tileSize, gp.tileSize * 2);
      attackDown2 = setup("/player/Attacking sprites/boy_pick_down_2", gp.tileSize, gp.tileSize * 2);
      attackLeft1 = setup("/player/Attacking sprites/boy_pick_left_1", gp.tileSize * 2, gp.tileSize);
      attackLeft2 = setup("/player/Attacking sprites/boy_pick_left_2", gp.tileSize * 2, gp.tileSize);
      attackRight1 = setup("/player/Attacking sprites/boy_pick_right_1", gp.tileSize * 2, gp.tileSize);
      attackRight2 = setup("/player/Attacking sprites/boy_pick_right_2", gp.tileSize * 2, gp.tileSize);
    }
  }

  public void getGuardImage() {
    guardUp = setup("/player/Guarding sprites/boy_guard_up", gp.tileSize, gp.tileSize);
    guardDown = setup("/player/Guarding sprites/boy_guard_down", gp.tileSize, gp.tileSize);
    guardLeft = setup("/player/Guarding sprites/boy_guard_left", gp.tileSize, gp.tileSize);
    guardRight = setup("/player/Guarding sprites/boy_guard_right", gp.tileSize, gp.tileSize);
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
    gp.cChecker.checkTile(this);
    gp.cChecker.checkObject(this);
    gp.cChecker.checkEntity(this, gp.npc);
    gp.cChecker.checkEntity(this, gp.monster);
    gp.cChecker.checkEntity(this, gp.iTile);

    if (isCollisionOn()) {
      setKnockBackCounter(0);
      setKnockBack(false);
      setSpeed(getDefaultSpeed());
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
      setSpeed(getDefaultSpeed());
    }
  }

  private void handleGuard() {
    setGuarding(true);
    setGuardCounter(getGuardCounter() + 1);
  }

  private void handleMovementAndInteractions() {
    if (keyH.isUpPressed())
      setDirection(DIR_UP);
    else if (keyH.isDownPressed())
      setDirection(DIR_DOWN);
    else if (keyH.isLeftPressed())
      setDirection(DIR_LEFT);
    else if (keyH.isRightPressed())
      setDirection(DIR_RIGHT);

    setCollisionOn(false);
    gp.cChecker.checkTile(this);

    int objIndex = gp.cChecker.checkObject(this);
    pickUpObject(objIndex);

    int npcIndex = gp.cChecker.checkEntity(this, gp.npc);
    interactNPC(npcIndex);

    int monsterIndex = gp.cChecker.checkEntity(this, gp.monster);
    contactMonster(monsterIndex);

    gp.cChecker.checkEntity(this, gp.iTile);
    gp.eHandler.checkEvent();

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
    gp.keyH.setEnterPressed(false);

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
    if (gp.keyH.isShotKeyPressed()
        && getProjectile() != null
        && !getProjectile().isAlive()
        && getShotAvailableCounter() == 30
        && getProjectile().haveResource(this)) {

      getProjectile().set(getWorldX(), getWorldY(), getDirection(), true, this);
      getProjectile().subtractResource(this);

      for (int i = 0; i < gp.projectile[gp.currentMap].length; i++) {
        if (gp.projectile[gp.currentMap][i] == null) {
          gp.projectile[gp.currentMap][i] = getProjectile();
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
    if (getLife() > getMaxLife())
      setLife(getMaxLife());
    if (getMana() > getMaxMana())
      setMana(getMaxMana());
  }

  private void handleDeath() {
    if (!keyH.isGodModeOn() && getLife() <= 0) {
      gp.stopMusic();
      gp.gameState = gp.gameOverState;
      gp.ui.setCommandNum(-1);
      gp.playSE(12);
    }
  }

  public void pickUpObject(int i) {
    if (i == 999)
      return;

    Entity obj = gp.obj[gp.currentMap][i];

    if (obj.getType() == TYPE_PICKUP_ONLY) {
      obj.use(this);
      gp.obj[gp.currentMap][i] = null;
    } else if (obj.getType() == TYPE_OBSTACLE) {
      if (keyH.isEnterPressed()) {
        attackCanceled = true;
        obj.interact();
      }
    } else {
      String text;
      if (canObtainItem(obj)) {
        gp.playSE(1);
        text = "Você obteve " + obj.getName() + "!";
      } else {
        text = "Você não pode carregar mais nada!";
      }
      gp.ui.addMessage(text);
      gp.obj[gp.currentMap][i] = null;
    }
  }

  public void interactNPC(int i) {
    if (i == 999)
      return;

    Entity npc = gp.npc[gp.currentMap][i];
    if (gp.keyH.isEnterPressed()) {
      attackCanceled = true;
      npc.speak();
    }
    npc.move(getDirection());
  }

  public void contactMonster(int i) {
    if (i == 999)
      return;

    Entity mon = gp.monster[gp.currentMap][i];

    if (!isInvincible() && !mon.isDying()) {
      gp.playSE(6);
      int damage = mon.getAttack() - getDefense();
      if (damage < 1)
        damage = 1;
      setLife(getLife() - damage);
      setInvincible(true);
      setTransparent(true);
    }
  }

  public void damageMonster(int i, Entity attacker, int attack, int knockBackPower) {
    if (i == 999)
      return;

    Entity mon = gp.monster[gp.currentMap][i];

    if (!mon.isInvincible()) {
      gp.playSE(5);

      if (knockBackPower > 0) {
        setKnockBack(mon, attacker, knockBackPower);
      }

      if (mon.isOffBalance()) {
        attack *= 5;
      }

      int damage = attack - mon.getDefense();
      if (damage < 0)
        damage = 0;

      mon.setLife(mon.getLife() - damage);
      gp.ui.addMessage(damage + " de dano!");

      mon.setInvincible(true);
      mon.damageReaction();

      if (mon.getLife() <= 0) {
        mon.setDying(true);
        gp.ui.addMessage("Matou o " + mon.getName() + "!");
        gp.ui.addMessage("Exp + " + mon.getExp());
        setExp(getExp() + mon.getExp());
        checkLevelUp();
      }
    }
  }

  public void damageInteractiveTile(int i) {
    if (i == 999)
      return;

    InteractiveTile tile = gp.iTile[gp.currentMap][i];
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
        gp.iTile[gp.currentMap][i] = destroyed;
      }
    }
  }

  public void damageProjectile(int i) {
    if (i == 999)
      return;
    Entity proj = gp.projectile[gp.currentMap][i];
    proj.setAlive(false);
    generateParticle(proj, proj);
  }

  public void checkLevelUp() {
    if (getExp() >= getNextLevelExp()) {
      setLevel(getLevel() + 1);
      setNextLevelExp(getNextLevelExp() * 2);
      setMaxLife(getMaxLife() + 2);
      setStrength(getStrength() + 1);
      setDexterity(getDexterity() + 1);
      setAttack(getAttack());
      setDefense(getDefense());
      setLife(getMaxLife());

      gp.playSE(8);
      gp.gameState = gp.dialogueState;

      setDialogue();
      startDialogue(this, 0);
    }
  }

  // SELECT ITEM
  // SELECT ITEM
  public void selectItem() {
    int itemIndex = gp.ui.getItemIndexOnSlot(
        gp.ui.getPlayerSlotCol(),
        gp.ui.getPlayerSlotRow());
    var inv = getInventory();
    if (itemIndex >= inv.size())
      return;

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
      case TYPE_CONSUMABLE -> handleConsumableSelection(selectedItem, itemIndex);
      default -> {
        /* no-op: tipo de item não suportado */ }
    }
  }

  private void handleConsumableSelection(Entity selectedItem, int itemIndex) {
    if (!selectedItem.use(this))
      return;

    if (selectedItem.getAmount() > 1) {
      selectedItem.setAmount(selectedItem.getAmount() - 1);
    } else {
      getInventory().remove(itemIndex);
    }
  }

  public int searchItemInInventory(String itemName) {
    var inv = getInventory();
    for (int i = 0; i < inv.size(); i++) {
      if (inv.get(i).getName().equals(itemName))
        return i;
    }
    return 999;
  }

  public boolean canObtainItem(Entity item) {
    boolean canObtain = false;
    Entity newItem = gp.eGenerator.getObject(item.getName());
    var inv = getInventory();

    if (newItem.isStackable()) {
      int index = searchItemInInventory(newItem.getName());
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
      if (DIR_UP.equals(getDirection()))
        tempScreenY = screenY - gp.tileSize;
      if (DIR_LEFT.equals(getDirection()))
        tempScreenX = screenX - gp.tileSize;
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
    if (dir == null)
      return down; // fallback seguro
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

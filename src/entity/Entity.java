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

// Classe base para todas as entidades do jogo (jogador, NPCs, inimigos, etc.)
public class Entity {

  // Reutilize um único Random (evita new Random() repetidos)
  private static final Random RNG = new Random();

  // gp é usado por subclasses (Player/NPC/Monster), então mantenho protegido
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

  public Rectangle solidArea = new Rectangle(0, 0, 48, 48);
  public Rectangle attackArea = new Rectangle(0, 0, 0, 0);

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
  public String direction = "down";
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

  // COUNTER
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
  public ArrayList<Entity> inventory = new ArrayList<>(); // Inventário do jogador
  public static final int maxInventorySize = 20; // constante compartilhada
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

  // TIPOS (constantes – podem ser acessadas via instância sem quebrar código)
  public static final int type_player = 0;
  public static final int type_npc = 1;
  public static final int type_monster = 2;
  public static final int type_sword = 3;
  public static final int type_axe = 4;
  public static final int type_shield = 5;
  public static final int type_consumable = 6;
  public static final int type_pickupOnly = 7;
  public static final int type_obstacle = 8;
  public static final int type_light = 9;
  public static final int type_pickaxe = 10;

  // tipo atual da entidade
  public int type; // 0 = player, 1 = npc, 2 = monster

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

  public int getGoalCol(Entity target /* intencionalmente não usado */) {
    // Parâmetro mantido para compatibilidade com chamadas existentes.
    // (Usamos o jogador como alvo de pathfinding aqui.)
    if (target != null) {
      /* no-op */ }
    return (gp.player.worldX + gp.player.solidArea.x) / gp.tileSize;
  }

  public int getGoalRow(Entity target /* intencionalmente não usado */) {
    if (target != null) {
      /* no-op */ }
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
    // hook para subclasses
    this.loot = loot;
  }

  public void setAction() {
    // Implementação padrão vazia, deve ser sobrescrita por subclasses
  }

  public void move(String direction /* parâmetro proposital: API de hook */) {
    // hook opcional para subclasses
  }

  public void damageReaction() {
    // Implementação padrão vazia, deve ser sobrescrita por subclasses
  }

  public void speak() {
    // hook de fala; sobrescrito por NPCs
  }

  public void facePlayer() {
    // Faz o NPC olhar para o jogador, invertendo sua direção
    switch (gp.player.direction) {
      case "up":
        direction = "down";
        break;
      case "down":
        direction = "up";
        break;
      case "left":
        direction = "right";
        break;
      case "right":
        direction = "left";
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

  public void interact() {
    // Método intencionalmente vazio: algumas entidades não têm interação.
  }

  public boolean use(Entity entity) {
    // Por padrão, entidades não são "usáveis".
    return false;
  }

  public void checkDrop() {
    // Método intencionalmente vazio: subclasses definem os drops.
  }

  public void dropItem(Entity droppedItem) {
    for (int i = 0; i < gp.obj[1].length; i++) {
      if (gp.obj[gp.currentMap][i] == null) {
        gp.obj[gp.currentMap][i] = droppedItem;

        // Centraliza o drop no centro da hitbox (solidArea) da entidade que morreu
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
    // Reinicia flag de colisão e verifica colisões com tiles, objetos e o jogador
    collisionOn = false;
    gp.cChecker.checkTile(this);
    gp.cChecker.checkObject(this, false);
    gp.cChecker.checkEntity(this, gp.npc);
    gp.cChecker.checkEntity(this, gp.monster);
    gp.cChecker.checkEntity(this, gp.iTile);
    boolean contactPlayer = gp.cChecker.checKPlayer(this);

    if (this.type == type_monster && contactPlayer) {
      damagePlayer(attack);
    }
  }

  // Atualiza a lógica da entidade a cada frame
  public void update() {
    if (!sleep) {
      if (knockBack) {
        handleKnockBack();
      } else if (attacking) {
        performAttack(); // método renomeado para evitar choque com o campo "attacking"
      } else {
        handleActionAndMovement();
      }

      handleInvincibility();
      handleShotCooldown();
      handleOffBalance();
    }
  }

  private void handleKnockBack() {
    checkCollision();

    if (collisionOn) {
      knockBackCounter = 0;
      knockBack = false;
      speed = defaultSpeed;
    } else {
      switch (knockBackDirection) {
        case "up":
          worldY -= speed;
          break;
        case "down":
          worldY += speed;
          break;
        case "left":
          worldX -= speed;
          break;
        case "right":
          worldX += speed;
          break;
        default:
          break;
      }
    }

    knockBackCounter++;
    if (knockBackCounter == 10) {
      knockBackCounter = 0;
      knockBack = false;
      speed = defaultSpeed;
    }
  }

  private void handleActionAndMovement() {
    setAction(); // Define a ação atual (movimento, idle, etc.)
    checkCollision(); // Verifica colisões com o ambiente e outras entidades

    if (!collisionOn) {
      switch (direction) {
        case "up":
          worldY -= speed;
          break;
        case "down":
          worldY += speed;
          break;
        case "left":
          worldX -= speed;
          break;
        case "right":
          worldX += speed;
          break;
        default:
          break;
      }
    }

    // Alterna entre os sprites (1 e 2) para criar efeito de movimento animado
    spriteCounter++;
    if (spriteCounter > 24) {
      spriteNum = (spriteNum == 1) ? 2 : 1;
      spriteCounter = 0;
    }
  }

  private void handleInvincibility() {
    if (invincible) {
      invincibleCounter++;
      if (invincibleCounter > 20) {
        invincible = false;
        invincibleCounter = 0;
      }
    }
  }

  private void handleShotCooldown() {
    if (shotAvailableCounter < 30) {
      shotAvailableCounter++;
    }
  }

  private void handleOffBalance() {
    if (offBalance) {
      offBalanceCounter++;
      if (offBalanceCounter > 60) {
        offBalance = false;
        offBalanceCounter = 0;
      }
    }
  }

  public void checkAttackOrNot(int rate, int straight, int horizontal) {
    boolean targetInRange = false;
    int xDis = getXdistance(gp.player);
    int yDis = getYdistance(gp.player);

    switch (direction) {
      case "up":
        if (gp.player.getCenterY() < getCenterY() && yDis < straight && xDis < horizontal) {
          targetInRange = true;
        }
        break;
      case "down":
        if (gp.player.getCenterY() > getCenterY() && yDis < straight && xDis < horizontal) {
          targetInRange = true;
        }
        break;
      case "left":
        if (gp.player.getCenterX() < getCenterX() && xDis < straight && yDis < horizontal) {
          targetInRange = true;
        }
        break;
      case "right":
        if (gp.player.getCenterX() > getCenterX() && xDis < straight && yDis < horizontal) {
          targetInRange = true;
        }
        break;
      default:
        break;
    }

    if (targetInRange) {
      // Check if it initiates an attack
      int i = RNG.nextInt(rate);
      if (i == 0) {
        attacking = true;
        spriteNum = 1;
        spriteCounter = 0;
        shotAvailableCounter = 0;
      }
    }
  }

  public void checkShootOrNot(int rate, int shotInterval) {
    int i = RNG.nextInt(rate);
    if (i == 0 && !projectile.alive && shotAvailableCounter == shotInterval) {
      projectile.set(worldX, worldY, direction, true, this);

      // CHECK VACANCY
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
    if (getTileDistance(target) < distance) {
      int i = RNG.nextInt(rate);
      if (i == 0) {
        onPath = true;
      }
    }
  }

  public void checkStopChasingOrNot(Entity target, int distance, int rate) {
    if (getTileDistance(target) > distance) {
      int i = RNG.nextInt(rate);
      if (i == 0) {
        onPath = false;
      }
    }
  }

  public void getRandomDirection(int interval) {
    actionLockCounter++;

    // A cada "interval" frames, o NPC escolhe uma nova direção
    if (actionLockCounter > interval) {
      int i = RNG.nextInt(4); // 0..3

      switch (i) {
        case 0:
          direction = "up";
          break;
        case 1:
          direction = "down";
          break;
        case 2:
          direction = "left";
          break;
        case 3:
          direction = "right";
          break;
        default:
          break;
      }
      actionLockCounter = 0;
    }
  }

  public void moveTowardPlayer(int interval) {
    actionLockCounter++;

    if (actionLockCounter > interval) {
      if (getXdistance(gp.player) > getYdistance(gp.player)) {
        if (gp.player.getCenterX() < getCenterX()) {
          direction = "left";
        } else {
          direction = "right";
        }
      } else if (getXdistance(gp.player) < getYdistance(gp.player)) {
        if (gp.player.getCenterY() < getCenterY()) {
          direction = "up";
        } else {
          direction = "down";
        }
      }
      actionLockCounter = 0;
    }
  }

  public String getOppositeDireciton(String direction) {
    String oppositeDirection = "";

    switch (direction) {
      case "up":
        oppositeDirection = "down";
        break;
      case "down":
        oppositeDirection = "up";
        break;
      case "left":
        oppositeDirection = "right";
        break;
      case "right":
        oppositeDirection = "left";
        break;
      default:
        break;
    }
    return oppositeDirection;
  }

  // Método renomeado para evitar choque semântico com o campo boolean "attacking"
  protected void performAttack() {
    spriteCounter++;

    if (spriteCounter <= motion1_duration) {
      spriteNum = 1;
    }

    if (spriteCounter > motion1_duration && spriteCounter <= motion2_duration) {
      spriteNum = 2;

      // Save the current worldX, worldY, solidArea
      int currentWorldX = worldX;
      int currentWorldY = worldY;
      int solidAreaWidth = solidArea.width;
      int solidAreaHeight = solidArea.height;

      // Adjust player's worldX/Y for the attackArea
      switch (direction) {
        case "up":
          worldY -= attackArea.height;
          break;
        case "down":
          worldY += attackArea.height;
          break;
        case "left":
          worldX -= attackArea.width;
          break;
        case "right":
          worldX += attackArea.width;
          break;
        default:
          break;
      }

      // attackArea becomes solidArea
      solidArea.width = attackArea.width;
      solidArea.height = attackArea.height;

      if (type == type_monster) {
        if (gp.cChecker.checKPlayer(this)) {
          damagePlayer(attack);
        }
      } else { // Player
        // Check monster collision with the updated worldX, worldY and solidArea
        int monsterIndex = gp.cChecker.checkEntity(this, gp.monster);
        gp.player.damageMonster(monsterIndex, this, attack, currentWeapon.knockBackPower);

        int iTileIndex = gp.cChecker.checkEntity(this, gp.iTile);
        gp.player.damageInteractiveTile(iTileIndex);

        int projectileIndex = gp.cChecker.checkEntity(this, gp.projectile);
        gp.player.damageProjectile(projectileIndex);
      }

      // After checking collision, restore the original data
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
    if (!gp.player.invincible) {
      int damage = attack - gp.player.defense;

      // Get an opposite direction of this attacker
      String canGuardDIrection = getOppositeDireciton(direction);

      if (gp.player.guarding && gp.player.direction.equals(canGuardDIrection)) {

        // Parry
        if (gp.player.guardCounter < 10) {
          damage = 0;
          gp.playSE(16);
          setKnockBack(this, gp.player, knockBackPower);
          offBalance = true;
          spriteCounter -= 60;
        } else {
          // Normal Guard
          damage /= 3;
          gp.playSE(15);
        }
      } else {
        // Not guarding
        gp.playSE(6);
        if (damage < 1) {
          damage = 1;
        }
      }

      if (damage != 0) {
        gp.player.transparent = true;
        setKnockBack(gp.player, this, knockBackPower);
      }

      gp.player.life -= damage;
      gp.player.invincible = true; // Ativa invencibilidade temporária
    }
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

  // Renderiza a entidade na tela, com base na posição relativa ao jogador
  // (scroll/câmera)
  public void draw(Graphics2D g2) {
    BufferedImage img = null;

    if (inCamera()) {
      int tempScreenX = getScreenX();
      int tempScreenY = getScreenY();

      // Escolhe o sprite correto com base na direção e no frame da animação
      switch (direction) {
        case "up":
          if (attacking) {
            tempScreenY = getScreenY() - up1.getHeight();
            img = (spriteNum == 1) ? attackUp1 : attackUp2;
          } else {
            img = (spriteNum == 1) ? up1 : up2;
          }
          break;

        case "down":
          if (attacking) {
            img = (spriteNum == 1) ? attackDown1 : attackDown2;
          } else {
            img = (spriteNum == 1) ? down1 : down2;
          }
          break;

        case "left":
          if (attacking) {
            tempScreenX = getScreenX() - left1.getWidth();
            img = (spriteNum == 1) ? attackLeft1 : attackLeft2;
          } else {
            img = (spriteNum == 1) ? left1 : left2;
          }
          break;

        case "right":
          if (attacking) {
            img = (spriteNum == 1) ? attackRight1 : attackRight2;
          } else {
            img = (spriteNum == 1) ? right1 : right2;
          }
          break;

        default:
          break;
      }

      if (invincible) {
        hpBarOn = true;
        hpBarCounter = 0;
        changeAlpha(g2, 0.4f);
      }

      if (dying) {
        dyingAnimation(g2);
      }

      g2.drawImage(img, tempScreenX, tempScreenY, null);
      changeAlpha(g2, 1f);
    }
  }

  public void dyingAnimation(Graphics2D g2) {
    dyingCounter++;

    int i = 5;

    if (dyingCounter <= i) {
      changeAlpha(g2, 0f);
    }
    if (dyingCounter > i && dyingCounter <= i * 2) {
      changeAlpha(g2, 1f);
    }
    if (dyingCounter > i * 2 && dyingCounter <= i * 3) {
      changeAlpha(g2, 0f);
    }
    if (dyingCounter > i * 3 && dyingCounter <= i * 4) {
      changeAlpha(g2, 1f);
    }
    if (dyingCounter > i * 4 && dyingCounter <= i * 5) {
      changeAlpha(g2, 0f);
    }
    if (dyingCounter > i * 5 && dyingCounter <= i * 6) {
      changeAlpha(g2, 1f);
    }
    if (dyingCounter > i * 6 && dyingCounter <= i * 7) {
      changeAlpha(g2, 0f);
    }
    if (dyingCounter > i * 7 && dyingCounter <= i * 8) {
      changeAlpha(g2, 1f);
    }
    if (dyingCounter > i * 8) {
      alive = false;
    }
  }

  public void changeAlpha(Graphics2D g2, float alphaValue) {
    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alphaValue));
  }

  // Carrega e redimensiona uma imagem PNG a partir do caminho fornecido
  public BufferedImage setup(String imagePath, int width, int height) {
    UtilityTool uTool = new UtilityTool(); // Ferramenta auxiliar para redimensionamento de imagem
    BufferedImage img = null;

    try {
      img = ImageIO.read(getClass().getResourceAsStream(imagePath + ".png")); // Carrega imagem
      img = uTool.scaleImage(img, width, height); // Redimensiona para tamanho padrão
    } catch (IOException e) {
      e.printStackTrace(); // Mostra erro no console caso a imagem falhe ao carregar
    }

    return img;
  }

  public void searchPath(int goalCol, int goalRow) {
    int startCol = (worldX + solidArea.x) / gp.tileSize;
    int startRow = (worldY + solidArea.y) / gp.tileSize;

    gp.pFinder.setNodes(startCol, startRow, goalCol, goalRow);

    if (gp.pFinder.search()) {
      // Next worldX & worldY
      int nextX = gp.pFinder.pathList.get(0).getCol() * gp.tileSize;
      int nextY = gp.pFinder.pathList.get(0).getRow() * gp.tileSize;

      // Entity's solidArea position
      int enLeftX = worldX + solidArea.x;
      int enRightX = worldX + solidArea.x + solidArea.width;
      int enTopY = worldY + solidArea.y;
      int enBottomY = worldY + solidArea.y + solidArea.height;

      if (enTopY > nextY && enLeftX >= nextX && enRightX < nextX + gp.tileSize) {
        direction = "up";
      } else if (enTopY < nextY && enLeftX >= nextX && enRightX < nextX + gp.tileSize) {
        direction = "down";
      } else if (enTopY >= nextY && enBottomY < nextY + gp.tileSize) {
        // left or right
        if (enLeftX > nextX) {
          direction = "left";
        }
        if (enLeftX < nextX) {
          direction = "right";
        }
      } else if (enTopY > nextY && enLeftX > nextX) {
        // up or left
        direction = "up";
        checkCollision();
        if (collisionOn) {
          direction = "left";
        }
      } else if (enTopY > nextY && enLeftX < nextX) {
        // up or right
        direction = "up";
        checkCollision();
        if (collisionOn) {
          direction = "right";
        }
      } else if (enTopY < nextY && enLeftX >= nextX) {
        // down or left
        direction = "down";
        checkCollision();
        if (collisionOn) {
          direction = "left";
        }
      } else if (enTopY < nextY && enLeftX < nextX) {
        // down or right
        direction = "down";
        checkCollision();
        if (collisionOn) {
          direction = "right";
        }
      }

      // Se atingir o objetivo, pare a busca
      int nextCol = gp.pFinder.pathList.get(0).getCol();
      int nextRow = gp.pFinder.pathList.get(0).getRow();
      if (nextCol == goalCol && nextRow == goalRow) {
        onPath = false;
      }
    }
  }

  public int getDetected(Entity user, Entity[][] target, String targetName) {
    int index = 999;

    // Check the surrounding object
    int nextWorldX = user.getLeftX();
    int nextWorldY = user.getTopY();

    switch (user.direction) {
      case "up":
        nextWorldY = user.getTopY() - gp.player.speed;
        break;
      case "down":
        nextWorldY = user.getBottomY() + gp.player.speed;
        break;
      case "left":
        nextWorldX = user.getLeftX() - gp.player.speed;
        break;
      case "right":
        nextWorldX = user.getRightX() + gp.player.speed;
        break;
      default:
        break;
    }

    int col = nextWorldX / gp.tileSize;
    int row = nextWorldY / gp.tileSize;

    for (int i = 0; i < target[gp.currentMap].length; i++) {
      if (target[gp.currentMap][i] != null) {
        if (target[gp.currentMap][i].getCol() == col &&
            target[gp.currentMap][i].getRow() == row &&
            target[gp.currentMap][i].name.equals(targetName)) {

          index = i;
          break;
        }
      }
    }

    return index;
  }
}

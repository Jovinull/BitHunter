package entity;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import main.GamePanel;
import main.KeyHandler;
import object.OBJ_Fireball;
import object.OBJ_Key;
import object.OBJ_Lantern;
import object.OBJ_Shield_Wood;
import object.OBJ_Sword_Normal;

// Classe Player representa o personagem controlado pelo jogador
public class Player extends Entity {

  KeyHandler keyH; // Referência ao manipulador de teclado

  public final int screenX; // Posição fixa do jogador na tela no eixo X (centro da tela)
  public final int screenY; // Posição fixa do jogador na tela no eixo Y (centro da tela)
  int standCounter = 0; // Contador para animação de parada
  public boolean attackCanceled = false; // Flag para indicar se o jogador está atacando
  public boolean lightUpdated = false;

  public boolean debugCollision = false; // Flag para depuração de colisão

  // Construtor: recebe o painel e o handler de teclado
  public Player(GamePanel gp, KeyHandler keyH) {
    super(gp); // Chama o construtor da classe base Entity
    this.keyH = keyH;

    // Define o ponto fixo do jogador na tela (centro da tela)
    screenX = gp.screenWidth / 2;
    screenY = gp.screenHeight / 2;

    solidArea = new Rectangle();
    solidArea.x = 8; // Posição X da área sólida (colisão)
    solidArea.y = 16; // Posição Y da área sólida
    solidAreaDefaultX = solidArea.x; // Posição padrão X da área sólida
    solidAreaDefaultY = solidArea.y; // Posição padrão Y da área sólida
    solidArea.width = 32; // Largura da área sólida
    solidArea.height = 32; // Altura da área sólida

    setDefaultValues(); // Define a posição inicial no mundo e outras propriedades
  }

  // Define valores iniciais do jogador no mundo
  public void setDefaultValues() {
    worldX = gp.tileSize * 23; // Posição inicial em X no mundo (tile 23)
    worldY = gp.tileSize * 21; // Posição inicial em Y no mundo (tile 21)

    defaultSpeed = 3;
    speed = defaultSpeed; // Velocidade do jogador
    direction = "down"; // Direção inicial

    // PLAYER STATUS
    level = 1;
    maxLife = 6;
    life = maxLife;
    maxMana = 4;
    mana = maxMana;
    ammo = 10;
    strength = 1;
    dexterity = 1;
    exp = 0;
    nextLevelExp = 5;
    coin = 500;
    currentWeapon = new OBJ_Sword_Normal(gp);
    currentShield = new OBJ_Shield_Wood(gp);
    currentLight = null;
    projectile = new OBJ_Fireball(gp);
    attack = getAttack();
    defense = getDefense();

    getImage(); // Carrega os sprites do personagem
    getAttackImage(); // Carrega os sprites de ataque do personagem
    getGuardImage();
    setItems(); // Adiciona itens iniciais ao inventário
    setDialogue();
  }

  public void setDefaultPositions() {
    worldX = gp.tileSize * 23; // Posição inicial em X no mundo (tile 23)
    worldY = gp.tileSize * 21; // Posição inicial em Y no mundo (tile 21)
    direction = "down"; // Direção inicial
  }

  public void setDialogue() {

    dialogues[0][0] = "Você subiu de level!\n" + "Seu level agora é " + level + "!\n"
        + "Vida máxima aumentada!\n" + "Força e Destreza aumentadas!";
  }

  public void restoreStatus() {
    life = maxLife;
    mana = maxMana;
    speed = defaultSpeed;
    invincible = false;
    transparent = false;
    attacking = false;
    guarding = false;
    knockBack = false;
    lightUpdated = true;
  }

  public void setItems() {

    inventory.clear();
    inventory.add(currentWeapon);
    inventory.add(currentShield);
    inventory.add(new OBJ_Key(gp));
    inventory.add(new OBJ_Lantern(gp));
  }

  public int getAttack() {
    attackArea = currentWeapon.attackArea;
    motion1_duration = currentWeapon.motion1_duration;
    motion2_duration = currentWeapon.motion2_duration;
    return attack = strength * currentWeapon.attackValue;
  }

  public int getDefense() {
    return defense = dexterity * currentShield.defenseValue;
  }

  public int getCurrentWeaponSlot() {
    int currentWeaponSlot = 0;
    for (int i = 0; i < inventory.size(); i++) {
      if (inventory.get(i) == currentWeapon) {
        currentWeaponSlot = i;
      }
    }
    return currentWeaponSlot;
  }

  public int getCurrentShieldSlot() {
    int currentShieldSlot = 0;
    for (int i = 0; i < inventory.size(); i++) {
      if (inventory.get(i) == currentShield) {
        currentShieldSlot = i;
      }
    }
    return currentShieldSlot;
  }

  // Carrega os sprites de animação para cada direção
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

    if (currentWeapon.type == type_sword) {
      attackUp1 = setup("/player/Attacking sprites/boy_attack_up_1", gp.tileSize, gp.tileSize * 2);
      attackUp2 = setup("/player/Attacking sprites/boy_attack_up_2", gp.tileSize, gp.tileSize * 2);
      attackDown1 = setup("/player/Attacking sprites/boy_attack_down_1", gp.tileSize, gp.tileSize * 2);
      attackDown2 = setup("/player/Attacking sprites/boy_attack_down_2", gp.tileSize, gp.tileSize * 2);
      attackLeft1 = setup("/player/Attacking sprites/boy_attack_left_1", gp.tileSize * 2, gp.tileSize);
      attackLeft2 = setup("/player/Attacking sprites/boy_attack_left_2", gp.tileSize * 2, gp.tileSize);
      attackRight1 = setup("/player/Attacking sprites/boy_attack_right_1", gp.tileSize * 2, gp.tileSize);
      attackRight2 = setup("/player/Attacking sprites/boy_attack_right_2", gp.tileSize * 2, gp.tileSize);
    }

    if (currentWeapon.type == type_axe) {
      attackUp1 = setup("/player/Attacking sprites/boy_axe_up_1", gp.tileSize, gp.tileSize * 2);
      attackUp2 = setup("/player/Attacking sprites/boy_axe_up_2", gp.tileSize, gp.tileSize * 2);
      attackDown1 = setup("/player/Attacking sprites/boy_axe_down_1", gp.tileSize, gp.tileSize * 2);
      attackDown2 = setup("/player/Attacking sprites/boy_axe_down_2", gp.tileSize, gp.tileSize * 2);
      attackLeft1 = setup("/player/Attacking sprites/boy_axe_left_1", gp.tileSize * 2, gp.tileSize);
      attackLeft2 = setup("/player/Attacking sprites/boy_axe_left_2", gp.tileSize * 2, gp.tileSize);
      attackRight1 = setup("/player/Attacking sprites/boy_axe_right_1", gp.tileSize * 2, gp.tileSize);
      attackRight2 = setup("/player/Attacking sprites/boy_axe_right_2", gp.tileSize * 2, gp.tileSize);
    }
  }

  public void getGuardImage() {

    guardUp = setup("/player/Guarding sprites/boy_guard_up", gp.tileSize, gp.tileSize);
    guardDown = setup("/player/Guarding sprites/boy_guard_down", gp.tileSize, gp.tileSize);
    guardLeft = setup("/player/Guarding sprites/boy_guard_left", gp.tileSize, gp.tileSize);
    guardRight = setup("/player/Guarding sprites/boy_guard_right", gp.tileSize, gp.tileSize);
  }

  // Atualiza a lógica do jogador: movimentação e animação
  public void update() {

    if (knockBack == true) {

      collisionOn = false;
      gp.cChecker.checkTile(this);
      gp.cChecker.checkObject(this, true);
      gp.cChecker.checkEntity(this, gp.npc);
      gp.cChecker.checkEntity(this, gp.monster);
      gp.cChecker.checkEntity(this, gp.iTile);

      if (collisionOn == true) {
        knockBackCounter = 0;
        knockBack = false;
        speed = defaultSpeed;
      }

      else if (collisionOn == false) {
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
        }
      }

      knockBackCounter++;
      if (knockBackCounter == 10) {
        knockBackCounter = 0;
        knockBack = false;
        speed = defaultSpeed;
      }
    }

    else if (attacking == true) {
      attacking();
    }

    else if (keyH.spacePressed == true) {
      guarding = true;
      guardCounter++;
    }

    // Só atualiza se alguma tecla de movimento estiver pressionada
    else if (keyH.upPressed || keyH.downPressed || keyH.leftPressed || keyH.rightPressed || keyH.enterPressed) {

      // Atualiza a direção e a posição no mundo
      if (keyH.upPressed) {
        direction = "up";
      } else if (keyH.downPressed) {
        direction = "down";
      } else if (keyH.leftPressed) {
        direction = "left";
      } else if (keyH.rightPressed) {
        direction = "right";
      }

      // CHECK TILE COLLISION
      collisionOn = false;
      gp.cChecker.checkTile(this); // Verifica colisão com tiles do mapa

      // CHECK OBJECT COLLISION
      int objIndex = gp.cChecker.checkObject(this, true);
      pickUpObject(objIndex); // Tenta pegar o objeto se houver colisão

      // CHECK NPC COLLISION
      int npcIndex = gp.cChecker.checkEntity(this, gp.npc);
      interactNPC(npcIndex); // Interage com NPC se houver colisão

      // CHECK MONSTER COLLISION
      int monsterIndex = gp.cChecker.checkEntity(this, gp.monster);
      contactMonster(monsterIndex);

      gp.cChecker.checkEntity(this, gp.iTile);

      // CHECK EVENT
      gp.eHandler.checkEvent(); // Verifica eventos no mapa

      if (collisionOn == false && keyH.enterPressed == false) {
        // Atualiza a posição do jogador com base na direção
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
        }
      }

      if (keyH.enterPressed == true && attackCanceled == false) {
        gp.playSE(7);
        attacking = true;
        spriteCounter = 0;
      }

      attackCanceled = false;
      gp.keyH.enterPressed = false; // Reseta a flag de enter após a interação
      guarding = false;
      guardCounter = 0;

      // Troca de sprite a cada 12 frames (animação)
      spriteCounter++;
      if (spriteCounter > 12) {
        spriteNum = (spriteNum == 1) ? 2 : 1;
        spriteCounter = 0;
      }
    } else {

      standCounter++;

      // Se nenhuma tecla de movimento estiver pressionada, o jogador fica parado
      if (standCounter >= 20) {
        spriteNum = 1; // Define o sprite de parada
        standCounter = 0; // Reseta o contador de parada
      }
      guarding = false;
      guardCounter = 0;
    }

    if (gp.keyH.shotKeyPressed == true && projectile.alive == false && shotAvailableCounter == 30
        && projectile.haveResource(this)) {

      projectile.set(worldX, worldY, direction, true, this);

      projectile.subtractResource(this);

      for (int i = 0; i < gp.projectile[1].length; i++) {
        if (gp.projectile[gp.currentMap][i] == null) {
          gp.projectile[gp.currentMap][i] = projectile;
          break;
        }
      }

      shotAvailableCounter = 0;

      gp.playSE(10);
    }

    if (invincible == true) {
      invincibleCounter++;
      if (invincibleCounter > 60) {
        invincible = false;
        transparent = false;
        invincibleCounter = 0;
      }
    }

    if (shotAvailableCounter < 30) {
      shotAvailableCounter++;
    }

    if (life > maxLife) {
      life = maxLife;
    }

    if (mana > maxMana) {
      mana = maxMana;
    }

    if (life <= 0) {
      gp.stopMusic();
      gp.gameState = gp.gameOverState;
      gp.ui.commandNum = 0;
      gp.playSE(12);
    }
  }

  // Método responsável por tratar a coleta/interação com um objeto baseado no
  // índice passado
  public void pickUpObject(int i) {

    // Se o índice for válido (999 significa "nenhum objeto")
    if (i != 999) {
      // PICKUP ONLY ITEMS
      if (gp.obj[gp.currentMap][i].type == type_pickupOnly) {

        gp.obj[gp.currentMap][i].use(this);
        gp.obj[gp.currentMap][i] = null;
      }

      // OBSTACLE
      else if (gp.obj[gp.currentMap][i].type == type_obstacle) {
        if (keyH.enterPressed == true) {
          attackCanceled = true;
          gp.obj[gp.currentMap][i].interact();
        }
      }

      // INVENTORY ITEMS
      else {

        String text;
        if (canObtainItem(gp.obj[gp.currentMap][i]) == true) {
          gp.playSE(1);
          text = "Você obteve " + gp.obj[gp.currentMap][i].name + "!";
        } else {
          text = "Você não pode carregar mais nada!";
        }
        gp.ui.addMessage(text);
        gp.obj[gp.currentMap][i] = null;
      }
    }
  }

  // Método responsável por interagir com um NPC baseado no índice passado
  public void interactNPC(int i) {
    if (gp.keyH.enterPressed == true) {

      if (i != 999) {
        attackCanceled = true;
        gp.npc[gp.currentMap][i].speak();
      }
    }
  }

  public void contactMonster(int i) {

    if (i != 999) {

      if (invincible == false && gp.monster[gp.currentMap][i].dying == false) {
        gp.playSE(6);

        int damage = gp.monster[gp.currentMap][i].attack - defense;

        if (damage < 1) {
          damage = 1;
        }

        life -= damage;
        invincible = true;
        transparent = true;
      }
    }
  }

  public void damageMonster(int i, Entity attacker, int attack, int knockBackPower) {

    if (i != 999) {
      if (gp.monster[gp.currentMap][i].invincible == false) {

        gp.playSE(5);

        if (knockBackPower > 0) {
          setKnockBack(gp.monster[gp.currentMap][i], attacker, knockBackPower);
        }

        if (gp.monster[gp.currentMap][i].offBalance == true) {
          attack *= 5;
        }

        int damage = attack - gp.monster[gp.currentMap][i].defense;

        if (damage < 0) {
          damage = 0;
        }

        gp.monster[gp.currentMap][i].life -= damage;
        gp.ui.addMessage(damage + " de dano!");

        gp.monster[gp.currentMap][i].invincible = true;
        gp.monster[gp.currentMap][i].damageReaction();

        if (gp.monster[gp.currentMap][i].life <= 0) {
          gp.monster[gp.currentMap][i].dying = true;
          gp.ui.addMessage("Matou o " + gp.monster[gp.currentMap][i].name + "!");
          gp.ui.addMessage("Exp + " + gp.monster[gp.currentMap][i].exp);
          exp += gp.monster[gp.currentMap][i].exp;
          checkLevelUp();
        }
      }
    }
  }

  public void damageInteractiveTile(int i) {

    if (i != 999
        && gp.iTile[gp.currentMap][i].destructible == true
        && gp.iTile[gp.currentMap][i].isCorrectItem(this) == true
        && gp.iTile[gp.currentMap][i].invincible == false) {

      gp.iTile[gp.currentMap][i].playSE();
      gp.iTile[gp.currentMap][i].life--;
      gp.iTile[gp.currentMap][i].invincible = true;

      // Generate particle
      generateParticle(gp.iTile[gp.currentMap][i], gp.iTile[gp.currentMap][i]);

      if (gp.iTile[gp.currentMap][i].life == 0) {
        gp.iTile[gp.currentMap][i] = gp.iTile[gp.currentMap][i].getDestroyedForm();
      }
    }
  }

  public void damageProjectile(int i) {

    if (i != 999) {

      Entity projectile = gp.projectile[gp.currentMap][i];
      projectile.alive = false;
      generateParticle(projectile, projectile);
    }
  }

  public void checkLevelUp() {

    if (exp >= nextLevelExp) {
      level++;
      nextLevelExp = nextLevelExp * 2;
      maxLife += 2;
      strength++;
      dexterity++;
      attack = getAttack();
      defense = getDefense();
      life = maxLife;

      gp.playSE(8);
      gp.gameState = gp.dialogueState;

      startDialogue(this, 0);
    }
  }

  public void selectItem() {

    int itemIndex = gp.ui.getItemIndexOnSlot(gp.ui.playerSlotCol, gp.ui.playerSlotRow);
    if (itemIndex < gp.player.inventory.size()) {
      Entity selectedItem = gp.player.inventory.get(itemIndex);

      if (selectedItem.type == type_sword || selectedItem.type == type_axe) {
        currentWeapon = selectedItem;
        attack = getAttack();
        getAttackImage();
      }
      if (selectedItem.type == type_shield) {
        currentShield = selectedItem;
        defense = getDefense();
      }

      if (selectedItem.type == type_light) {

        if (currentLight == selectedItem) {
          currentLight = null;
        } else {
          currentLight = selectedItem;
        }
        lightUpdated = true;
      }

      if (selectedItem.type == type_consumable) {

        if (selectedItem.use(this) == true) {
          if (selectedItem.amount > 1) {
            selectedItem.amount--;
          } else {
            inventory.remove(itemIndex);
          }
        }
      }
    }
  }

  public int searchItemInInventory(String itemName) {
    int itemIndex = 999;

    for (int i = 0; i < inventory.size(); i++) {
      if (inventory.get(i).name.equals(itemName)) {
        itemIndex = i;
        break;
      }
    }
    return itemIndex;
  }

  public boolean canObtainItem(Entity item) {

    boolean canObtain = false;

    // CHECK IF STACKABLE
    if (item.stackable == true) {

      int index = searchItemInInventory(item.name);

      if (index != 999) {
        inventory.get(index).amount++;
        canObtain = true;
      } else { // New item so need to check vacancy
        if (inventory.size() != maxInventorySize) {
          inventory.add(item);
          canObtain = true;
        }
      }

    } else { // NOT STACKABLE so check vacancy
      if (inventory.size() != maxInventorySize) {
        inventory.add(item);
        canObtain = true;
      }
    }

    return canObtain;
  }

  // Desenha o jogador na tela
  public void draw(Graphics2D g2) {
    BufferedImage image = null;
    int tempScreenX = screenX;
    int tempScreenY = screenY;

    // Escolhe o sprite correto com base na direção e no frame da animação
    switch (direction) {
      case "up":
        if (attacking) {
          tempScreenY = screenY - gp.tileSize;
          image = (spriteNum == 1) ? attackUp1 : attackUp2;
        } else {
          image = (spriteNum == 1) ? up1 : up2;
        }
        if (guarding == true) {
          image = guardUp;
        }
        break;

      case "down":
        if (attacking) {
          image = (spriteNum == 1) ? attackDown1 : attackDown2;
        } else {
          image = (spriteNum == 1) ? down1 : down2;
        }
        if (guarding == true) {
          image = guardDown;
        }
        break;

      case "left":
        if (attacking) {
          tempScreenX = screenX - gp.tileSize;
          image = (spriteNum == 1) ? attackLeft1 : attackLeft2;
        } else {
          image = (spriteNum == 1) ? left1 : left2;
        }
        if (guarding == true) {
          image = guardLeft;
        }
        break;

      case "right":
        if (attacking) {
          image = (spriteNum == 1) ? attackRight1 : attackRight2;
        } else {
          image = (spriteNum == 1) ? right1 : right2;
        }
        if (guarding == true) {
          image = guardRight;
        }
        break;
    }

    if (transparent == true) {
      g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f)); // Aplica transparência
    }

    // Desenha o sprite na posição fixa do centro da tela, com o tamanho padrão do
    // tile
    g2.drawImage(image, tempScreenX, tempScreenY, null);

    // Reset alpha
    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));

    // Se a depuração de colisão estiver ativada, desenha a área sólida do jogador
    if (debugCollision) {
      g2.setColor(Color.red);
      g2.drawRect(screenX + solidArea.x, screenY + solidArea.y, solidArea.width, solidArea.height);
    }
  }
}

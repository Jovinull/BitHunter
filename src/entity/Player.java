package entity;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import main.GamePanel;
import main.KeyHandler;
import object.OBJ_Key;
import object.OBJ_Shield_Wood;
import object.OBJ_Sword_Normal;

// Classe Player representa o personagem controlado pelo jogador
public class Player extends Entity {

    KeyHandler keyH; // Referência ao manipulador de teclado

    public final int screenX; // Posição fixa do jogador na tela no eixo X (centro da tela)
    public final int screenY; // Posição fixa do jogador na tela no eixo Y (centro da tela)
    int standCounter = 0; // Contador para animação de parada
    public boolean attackCanceled = false; // Flag para indicar se o jogador está atacando
    public ArrayList<Entity> inventory = new ArrayList<>(); // Inventário do jogador
    public final int maxInventorySize = 20; // Tamanho máximo do inventário

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

        attackArea.width = 36;
        attackArea.height = 36;

        setDefaultValues(); // Define a posição inicial no mundo e outras propriedades
        getPlayerImage(); // Carrega os sprites do personagem
        getPlayerAttackImage(); // Carrega os sprites de ataque do personagem
        setItems(); // Adiciona itens iniciais ao inventário
    }

    // Define valores iniciais do jogador no mundo
    public void setDefaultValues() {
        worldX = gp.tileSize * 23; // Posição inicial em X no mundo (tile 23)
        worldY = gp.tileSize * 21; // Posição inicial em Y no mundo (tile 21)
        speed = 4; // Velocidade do jogador
        direction = "down"; // Direção inicial

        // PLAYER STATUS
        level = 1;
        maxLife = 6;
        life = maxLife;
        strength = 1;
        dexterity = 1;
        exp = 0;
        nextLevelExp = 5;
        coin = 0;
        currentWeapon = new OBJ_Sword_Normal(gp);
        currentShield = new OBJ_Shield_Wood(gp);
        attack = getAttack();
        defense = getDefense();
    }

    public void setItems() {

        inventory.add(currentWeapon);
        inventory.add(currentShield);
        inventory.add(new OBJ_Key(gp));
    }

    public int getAttack() {
        return attack = strength * currentWeapon.attackValue;
    }

    public int getDefense() {
        return defense = dexterity * currentShield.defenseValue;
    }

    // Carrega os sprites de animação para cada direção
    public void getPlayerImage() {

        up1 = setup("/resource/player/boy_up_1", gp.tileSize, gp.tileSize);
        up2 = setup("/resource/player/boy_up_2", gp.tileSize, gp.tileSize);
        down1 = setup("/resource/player/boy_down_1", gp.tileSize, gp.tileSize);
        down2 = setup("/resource/player/boy_down_2", gp.tileSize, gp.tileSize);
        left1 = setup("/resource/player/boy_left_1", gp.tileSize, gp.tileSize);
        left2 = setup("/resource/player/boy_left_2", gp.tileSize, gp.tileSize);
        right1 = setup("/resource/player/boy_right_1", gp.tileSize, gp.tileSize);
        right2 = setup("/resource/player/boy_right_2", gp.tileSize, gp.tileSize);
    }

    public void getPlayerAttackImage() {
        attackUp1 = setup("/resource/player/boy_attack_up_1", gp.tileSize, gp.tileSize * 2);
        attackUp2 = setup("/resource/player/boy_attack_up_2", gp.tileSize, gp.tileSize * 2);
        attackDown1 = setup("/resource/player/boy_attack_down_1", gp.tileSize, gp.tileSize * 2);
        attackDown2 = setup("/resource/player/boy_attack_down_2", gp.tileSize, gp.tileSize * 2);
        attackLeft1 = setup("/resource/player/boy_attack_left_1", gp.tileSize * 2, gp.tileSize);
        attackLeft2 = setup("/resource/player/boy_attack_left_2", gp.tileSize * 2, gp.tileSize);
        attackRight1 = setup("/resource/player/boy_attack_right_1", gp.tileSize * 2, gp.tileSize);
        attackRight2 = setup("/resource/player/boy_attack_right_2", gp.tileSize * 2, gp.tileSize);
    }

    // Atualiza a lógica do jogador: movimentação e animação
    public void update() {

        if (attacking == true) {
            attacking();
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
        }

        if (invincible == true) {
            invincibleCounter++;
            if (invincibleCounter > 60) {
                invincible = false;
                invincibleCounter = 0;
            }
        }
    }

    public void attacking() {

        spriteCounter++;

        if (spriteCounter <= 5) {
            spriteNum = 1;
        }

        if (spriteCounter > 5 && spriteCounter <= 25) {
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
            }

            // attackArea becomes solidArea
            solidArea.width = attackArea.width;
            solidArea.height = attackArea.height;

            // Check monster collision with the updated worldX, worldY and solidArea
            int monsterIndex = gp.cChecker.checkEntity(this, gp.monster);
            damageMonster(monsterIndex);

            // After checking collision, restore the original data
            worldX = currentWorldX;
            worldY = currentWorldY;
            solidArea.width = solidAreaWidth;
            solidArea.height = solidAreaHeight;
        }

        if (spriteCounter > 25) {
            spriteNum = 1;
            spriteCounter = 0;
            attacking = false;
        }
    }

    // Método responsável por tratar a coleta/interação com um objeto baseado no
    // índice passado
    public void pickUpObject(int index) {

        // Se o índice for válido (999 significa "nenhum objeto")
        if (index != 999) {

        }
    }

    // Método responsável por interagir com um NPC baseado no índice passado
    public void interactNPC(int i) {
        if (gp.keyH.enterPressed == true) {

            if (i != 999) {
                attackCanceled = true;
                gp.gameState = gp.dialogueState;
                gp.npc[i].speak();
            }
        }
    }

    public void contactMonster(int i) {

        if (i != 999) {

            if (invincible == false) {
                gp.playSE(6);

                int damage = gp.monster[i].attack - defense;

                if (damage < 0) {
                    damage = 0;
                }

                life -= damage;
                invincible = true;
            }
        }
    }

    public void damageMonster(int i) {

        if (i != 999) {
            if (gp.monster[i].invincible == false) {

                gp.playSE(5);

                int damage = attack - gp.monster[i].defense;

                if (damage < 0) {
                    damage = 0;
                }

                gp.monster[i].life -= damage;
                gp.ui.addMessage(damage + " de dano!");

                gp.monster[i].invincible = true;
                gp.monster[i].damageReaction();

                if (gp.monster[i].life <= 0) {
                    gp.monster[i].dying = true;
                    gp.ui.addMessage("Matou o " + gp.monster[i].name + "!");
                    gp.ui.addMessage("Exp + " + gp.monster[i].exp);
                    exp += gp.monster[i].exp;
                    checkLevelUp();
                }
            }
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
            gp.ui.currentDialogue = "Você subiu de level!\n" + "Seu level agora é " + level + "!\n"
                    + "Vida máxima aumentada!\n" + "Força e Destreza aumentadas!";
        }
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
                break;

            case "down":
                if (attacking) {
                    image = (spriteNum == 1) ? attackDown1 : attackDown2;
                } else {
                    image = (spriteNum == 1) ? down1 : down2;
                }
                break;

            case "left":
                if (attacking) {
                    tempScreenX = screenX - gp.tileSize;
                    image = (spriteNum == 1) ? attackLeft1 : attackLeft2;
                } else {
                    image = (spriteNum == 1) ? left1 : left2;
                }
                break;

            case "right":
                if (attacking) {
                    image = (spriteNum == 1) ? attackRight1 : attackRight2;
                } else {
                    image = (spriteNum == 1) ? right1 : right2;
                }
                break;
        }

        if (invincible == true) {
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

package entity;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import main.GamePanel;
import main.KeyHandler;

// Classe Player representa o personagem controlado pelo jogador
public class Player extends Entity {

    KeyHandler keyH; // Referência ao manipulador de teclado

    public final int screenX; // Posição fixa do jogador na tela no eixo X (centro da tela)
    public final int screenY; // Posição fixa do jogador na tela no eixo Y (centro da tela)
    int standCounter = 0; // Contador para animação de parada

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
    }

    // Define valores iniciais do jogador no mundo
    public void setDefaultValues() {
        worldX = gp.tileSize * 23; // Posição inicial em X no mundo (tile 23)
        worldY = gp.tileSize * 21; // Posição inicial em Y no mundo (tile 21)
        speed = 4; // Velocidade do jogador
        direction = "down"; // Direção inicial

        // Jogador Status
        maxLife = 6;
        life = maxLife;
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
                gp.gameState = gp.dialogueState;
                gp.npc[i].speak();
            } else {
                attacking = true;
            }
        }
    }

    public void contactMonster(int i) {

        if (i != 999) {

            if (invincible == false) {
                life -= 1;
                invincible = true;
            }
        }
    }

    public void damageMonster(int i) {

        if (i != 999) {
            if (gp.monster[i].invincible == false) {

                gp.monster[i].life -= 1;
                gp.monster[i].invincible = true;

                if (gp.monster[i].life <= 0) {
                    gp.monster[i] = null;
                }
            }
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

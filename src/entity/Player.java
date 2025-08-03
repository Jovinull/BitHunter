package entity;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import main.GamePanel;
import main.KeyHandler;
import main.UtilityTool;

// Classe Player representa o personagem controlado pelo jogador
public class Player extends Entity {

    GamePanel gp; // Referência ao painel principal do jogo
    KeyHandler keyH; // Referência ao manipulador de teclado

    public final int screenX; // Posição fixa do jogador na tela no eixo X (centro da tela)
    public final int screenY; // Posição fixa do jogador na tela no eixo Y (centro da tela)
    int standCounter = 0; // Contador para animação de parada

    public boolean debugCollision = false; // Flag para depuração de colisão

    // Construtor: recebe o painel e o handler de teclado
    public Player(GamePanel gp, KeyHandler keyH) {
        this.gp = gp;
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
        getPlayerImage(); // Carrega os sprites do personagem
    }

    // Define valores iniciais do jogador no mundo
    public void setDefaultValues() {
        worldX = gp.tileSize * 23; // Posição inicial em X no mundo (tile 23)
        worldY = gp.tileSize * 21; // Posição inicial em Y no mundo (tile 21)
        speed = 4; // Velocidade do jogador
        direction = "down"; // Direção inicial
    }

    // Carrega os sprites de animação para cada direção
    public void getPlayerImage() {

        up1 = setup("boy_up_1");
        up2 = setup("boy_up_2");
        down1 = setup("boy_down_1");
        down2 = setup("boy_down_2");
        left1 = setup("boy_left_1");
        left2 = setup("boy_left_2");
        right1 = setup("boy_right_1");
        right2 = setup("boy_right_2");
    }

    public BufferedImage setup(String imageName) {

        UtilityTool uTool = new UtilityTool();
        BufferedImage image = null;

        try {
            image = ImageIO.read(getClass().getResourceAsStream("/resource/player/" + imageName + ".png"));
            image = uTool.scaleImage(image, gp.tileSize, gp.tileSize);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return image;
    }

    // Atualiza a lógica do jogador: movimentação e animação
    public void update() {

        // Só atualiza se alguma tecla de movimento estiver pressionada
        if (keyH.upPressed || keyH.downPressed || keyH.leftPressed || keyH.rightPressed) {

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

            // IF COLLISION IS FALSE, PLAYER CAN MOVE
            if (collisionOn == false) {
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
    }

    // Método responsável por tratar a coleta/interação com um objeto baseado no
    // índice passado
    public void pickUpObject(int index) {

        // Se o índice for válido (999 significa "nenhum objeto")
        if (index != 999) {
            
        }
    }

    // Desenha o jogador na tela
    public void draw(Graphics2D g2) {
        BufferedImage image = null;

        // Escolhe o sprite correto com base na direção e no frame da animação
        switch (direction) {
            case "up":
                image = (spriteNum == 1) ? up1 : up2;
                break;
            case "down":
                image = (spriteNum == 1) ? down1 : down2;
                break;
            case "left":
                image = (spriteNum == 1) ? left1 : left2;
                break;
            case "right":
                image = (spriteNum == 1) ? right1 : right2;
                break;
        }

        // Desenha o sprite na posição fixa do centro da tela, com o tamanho padrão do
        // tile
        g2.drawImage(image, screenX, screenY, null);

        // Se a depuração de colisão estiver ativada, desenha a área sólida do jogador
        if (debugCollision) {
            g2.setColor(Color.red);
            g2.drawRect(screenX + solidArea.x, screenY + solidArea.y, solidArea.width, solidArea.height);
        }
    }
}

package entity;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import main.GamePanel;
import main.KeyHandler;

// Classe Player representa o personagem controlado pelo jogador
public class Player extends Entity {

    GamePanel gp; // Referência ao painel principal do jogo
    KeyHandler keyH; // Referência ao manipulador de teclado

    public final int screenX; // Posição fixa do jogador na tela no eixo X (centro da tela)
    public final int screenY; // Posição fixa do jogador na tela no eixo Y (centro da tela)
    int hasKey = 0; // Contador de chaves coletadas

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
        try {
            up1 = ImageIO.read(getClass().getResourceAsStream("/resource/player/boy_up_1.png"));
            up2 = ImageIO.read(getClass().getResourceAsStream("/resource/player/boy_up_2.png"));

            down1 = ImageIO.read(getClass().getResourceAsStream("/resource/player/boy_down_1.png"));
            down2 = ImageIO.read(getClass().getResourceAsStream("/resource/player/boy_down_2.png"));

            left1 = ImageIO.read(getClass().getResourceAsStream("/resource/player/boy_left_1.png"));
            left2 = ImageIO.read(getClass().getResourceAsStream("/resource/player/boy_left_2.png"));

            right1 = ImageIO.read(getClass().getResourceAsStream("/resource/player/boy_right_1.png"));
            right2 = ImageIO.read(getClass().getResourceAsStream("/resource/player/boy_right_2.png"));
        } catch (IOException e) {
            e.printStackTrace(); // Mostra erro se as imagens não forem carregadas
        }
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
        }
    }

    public void pickUpObject(int index) {

        if (index != 999) {

            String objectName = gp.obj[index].name;
            switch (objectName) {
                case "Key":
                    hasKey++; // Incrementa contador de chaves
                    gp.obj[index] = null; // Remove o objeto do jogo
                    System.out.println("Chave coletada! Total de chaves: " + hasKey);
                    break;
                case "Door":
                    if (hasKey > 0) {
                        hasKey--; // Usa uma chave
                        gp.obj[index] = null; // Remove a porta do jogo
                        System.out.println("Porta aberta! Chaves restantes: " + hasKey);
                    } else {
                        System.out.println("Você precisa de uma chave para abrir esta porta!");
                    }
                    break;
                default:
                    System.out.println("Objeto desconhecido: " + objectName);
            }
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
        g2.drawImage(image, screenX, screenY, gp.tileSize, gp.tileSize, null);
    }
}

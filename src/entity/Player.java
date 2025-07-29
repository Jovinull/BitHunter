package entity;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import main.GamePanel;
import main.KeyHandler;

// Classe Player representa o personagem controlado pelo jogador
public class Player extends Entity {

    GamePanel gp; // Referência ao painel principal do jogo
    KeyHandler keyH; // Referência ao manipulador de teclas

    // Construtor: recebe o painel e o handler de teclado
    public Player(GamePanel gp, KeyHandler keyH) {
        this.gp = gp;
        this.keyH = keyH;

        setDefaultValues(); // Define posição e direção inicial
        getPlayerImage(); // Carrega os sprites do personagem
    }

    // Define valores padrão de início do jogador
    public void setDefaultValues() {
        x = 100;
        y = 100;
        speed = 4;
        direction = "down";
    }

    // Carrega as imagens dos sprites do personagem
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
            e.printStackTrace(); // Em caso de erro ao carregar imagens
        }
    }

    // Atualiza a lógica do jogador (movimento + animação)
    public void update() {

        // Verifica se alguma tecla de movimento está pressionada
        if (keyH.upPressed || keyH.downPressed || keyH.leftPressed || keyH.rightPressed) {

            // Atualiza a direção e posição com base nas teclas
            if (keyH.upPressed) {
                direction = "up";
                y -= speed;
            } else if (keyH.downPressed) {
                direction = "down";
                y += speed;
            } else if (keyH.leftPressed) {
                direction = "left";
                x -= speed;
            } else if (keyH.rightPressed) {
                direction = "right";
                x += speed;
            }

            // Lógica de troca de sprite (animação simples)
            spriteCounter++;
            if (spriteCounter > 12) {
                spriteNum = (spriteNum == 1) ? 2 : 1;
                spriteCounter = 0;
            }
        }
    }

    // Desenha o jogador na tela com o sprite correto
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

        // Desenha a imagem na posição atual do jogador com o tamanho do tile
        g2.drawImage(image, x, y, gp.tileSize, gp.tileSize, null);
    }
}

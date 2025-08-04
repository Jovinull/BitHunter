package entity;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import main.GamePanel;
import main.UtilityTool;

// Classe base para todas as entidades do jogo (jogador, NPCs, inimigos, etc.)
public class Entity {

    GamePanel gp; // Referência ao GamePanel para acessar dados globais como mapa, player,
                  // colisões

    // Posição da entidade no mundo (em coordenadas absolutas no mapa)
    public int worldX, worldY;

    // Velocidade de movimentação em pixels por atualização
    public int speed;

    // Sprites usados para animar a entidade em cada direção (dois quadros por
    // direção)
    public BufferedImage up1, up2;
    public BufferedImage down1, down2;
    public BufferedImage left1, left2;
    public BufferedImage right1, right2;

    // Direção atual em que a entidade está se movendo
    public String direction;

    // Controle de animação: alterna entre os sprites a cada X quadros
    public int spriteCounter = 0;
    public int spriteNum = 1;

    // Área de colisão (bounding box) usada para detectar interações e bloqueios
    public Rectangle solidArea = new Rectangle(0, 0, 48, 48);
    public int solidAreaDefaultX, solidAreaDefaultY; // Usado para restaurar a posição da área após verificação

    public boolean collisionOn = false; // Indica se houve colisão na verificação atual

    public int actionLockCounter = 0; // Controla frequência de ações automáticas (ex: movimento aleatório de NPC)

    public Entity(GamePanel gp) {
        this.gp = gp;
    }

    // Método genérico para definir ações (sobrescrito por NPCs ou inimigos, por
    // exemplo)
    public void setAction() {
    }

    // Atualiza a lógica da entidade a cada frame
    public void update() {
        setAction(); // Define a ação atual (movimento, idle, etc.)

        // Reinicia flag de colisão e verifica colisões com tiles, objetos e o jogador
        collisionOn = false;
        gp.cChecker.checkTile(this);
        gp.cChecker.checkObject(this, false);
        gp.cChecker.checKPlayer(this);

        // Se não houve colisão, move a entidade conforme a direção
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
            }
        }

        // Alterna entre os sprites (1 e 2) para criar efeito de movimento animado
        spriteCounter++;
        if (spriteCounter > 12) { // Troca a cada 12 frames (~5x por segundo em 60FPS)
            spriteNum = (spriteNum == 1) ? 2 : 1;
            spriteCounter = 0;
        }
    }

    // Renderiza a entidade na tela, com base na posição relativa ao jogador
    // (scroll/câmera)
    public void draw(Graphics2D g2) {
        BufferedImage image = null;

        // Calcula a posição relativa à tela, com base na posição do jogador (scroll)
        int screenX = worldX - gp.player.worldX + gp.player.screenX;
        int screenY = worldY - gp.player.worldY + gp.player.screenY;

        // Otimização: desenha apenas se a entidade estiver dentro da área visível da
        // tela
        if (worldX + gp.tileSize > gp.player.worldX - gp.player.screenX &&
                worldX - gp.tileSize < gp.player.worldX + gp.player.screenX &&
                worldY + gp.tileSize > gp.player.worldY - gp.player.screenY &&
                worldY - gp.tileSize < gp.player.worldY + gp.player.screenY) {

            // Seleciona a imagem correspondente à direção e frame atual
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

            // Desenha a sprite ajustada ao tamanho do tile (em pixels)
            g2.drawImage(image, screenX, screenY, gp.tileSize, gp.tileSize, null);
        }
    }

    // Carrega e redimensiona uma imagem PNG a partir do caminho fornecido
    public BufferedImage setup(String imagePath) {
        UtilityTool uTool = new UtilityTool(); // Ferramenta auxiliar para redimensionamento de imagem
        BufferedImage image = null;

        try {
            image = ImageIO.read(getClass().getResourceAsStream(imagePath + ".png")); // Carrega imagem
            image = uTool.scaleImage(image, gp.tileSize, gp.tileSize); // Redimensiona para tamanho padrão
        } catch (IOException e) {
            e.printStackTrace(); // Mostra erro no console caso a imagem falhe ao carregar
        }

        return image;
    }
}

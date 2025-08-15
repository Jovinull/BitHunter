package object;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import main.GamePanel;
import main.UtilityTool;

// Classe base para todos os objetos do jogo (chave, porta, baú, etc.)
public class SuperObject {

    public BufferedImage image, image2, image3; // Imagem que representa o objeto
    public String name; // Nome/descritivo do objeto
    public boolean collision = false; // Define se o objeto bloqueia movimento
    public int worldX, worldY; // Posição no mundo (coordenadas em pixels)
    public Rectangle solidArea = new Rectangle(0, 0, 48, 48); // Área sólida para colisão
    public int solidAreaDefaultX = 0; // Posição padrão X da área sólida
    public int solidAreaDefaultY = 0; // Posição padrão Y da área sólida
    UtilityTool uTool = new UtilityTool(); // Ferramenta utilitária para manipulação de imagens

    /**
     * Desenha o objeto na tela considerando a posição do jogador (câmera)
     * 
     * @param g2 contexto gráfico para desenhar
     * @param gp referência ao GamePanel, fornece dados de câmera e tileSize
     */
    public void draw(Graphics2D g2, GamePanel gp) {

        // Converte posição no mundo para posição na tela levando em conta scroll/câmera
        int screenX = worldX - gp.player.worldX + gp.player.screenX;
        int screenY = worldY - gp.player.worldY + gp.player.screenY;

        // Verifica se o objeto está dentro dos limites visíveis da tela
        if (worldX + gp.tileSize > gp.player.worldX - gp.player.screenX &&
                worldX - gp.tileSize < gp.player.worldX + gp.player.screenX &&
                worldY + gp.tileSize > gp.player.worldY - gp.player.screenY &&
                worldY - gp.tileSize < gp.player.worldY + gp.player.screenY) {

            // Desenha a imagem redimensionada para o tamanho do tile
            g2.drawImage(image, screenX, screenY, gp.tileSize, gp.tileSize, null);
        }
    }
}

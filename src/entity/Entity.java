package entity;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;

// Classe base para todas as entidades do jogo (ex: jogador, NPCs, inimigos, objetos)
public class Entity {

    // Posição da entidade no mundo (coordenadas absolutas no mapa)
    public int worldX, worldY;

    // Velocidade de movimentação da entidade (quantos pixels por atualização)
    public int speed;

    // Sprites para animação da entidade em cada direção (2 frames por direção)
    public BufferedImage up1, up2;
    public BufferedImage down1, down2;
    public BufferedImage left1, left2;
    public BufferedImage right1, right2;

    // Direção atual da entidade (ex: "up", "down", "left", "right")
    public String direction;

    // Controle da animação dos sprites
    public int spriteCounter = 0; // Conta os frames até alternar entre sprite 1 e 2
    public int spriteNum = 1; // Número do sprite atual (1 ou 2)

    public Rectangle solidArea; // Área sólida para colisão (usada para detectar interações com o mundo)
    public int solidAreaDefaultX, solidAreaDefaultY; // Posições padrão da área sólida (para resetar após colisões)
    public boolean collisionOn = false; // Flag para indicar se a entidade está colidindo com algo
}

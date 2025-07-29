package entity;

import java.awt.image.BufferedImage;

// Classe base para entidades do jogo (ex: jogador, NPCs, inimigos, etc.)
public class Entity {

    // Posição da entidade no mundo/tela
    public int x, y;

    // Velocidade de movimentação
    public int speed;

    // Sprites (imagens) para animação em cada direção
    public BufferedImage up1, up2;
    public BufferedImage down1, down2;
    public BufferedImage left1, left2;
    public BufferedImage right1, right2;

    // Direção atual (ex: "up", "down", "left", "right")
    public String direction;

    // Controle da animação
    public int spriteCounter = 0; // Contador para controlar o tempo de troca de sprite
    public int spriteNum = 1; // Sprite atual (1 ou 2)
}

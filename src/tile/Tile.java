package tile;

import java.awt.image.BufferedImage;

// Classe Tile representa um bloco (tile) do mapa do jogo
public class Tile {

  // Imagem que será desenhada na tela para representar esse tile
  public BufferedImage image;

  // Define se esse tile bloqueia a passagem (true = colisão ativa)
  public boolean collision = false;
}

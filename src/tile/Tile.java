package tile;

import java.awt.image.BufferedImage;

/**
 * Representa um bloco (tile) do mapa do jogo.
 * Fornece getters/setters para evitar acesso direto quando possível.
 */
public class Tile {

  // Ainda públicos para compatibilidade com código legado
  public BufferedImage image;
  public boolean collision = false;

  // --- Getters/Setters preferenciais ---

  public BufferedImage getImage() {
    return image;
  }

  public void setImage(BufferedImage image) {
    this.image = image;
  }

  public boolean isCollision() {
    return collision;
  }

  public void setCollision(boolean collision) {
    this.collision = collision;
  }
}

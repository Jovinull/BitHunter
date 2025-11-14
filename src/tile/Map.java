package tile;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import main.GamePanel;

public class Map extends TileManager {

  // Estado do mini-mapa
  private BufferedImage[] worldMap;
  private boolean miniMapOn = false;

  public Map(GamePanel gp) {
    super(gp);
    createWorldMap();
  }

  // Getters/Setters públicos
  public boolean isMiniMapOn() {
    return miniMapOn;
  }

  public void setMiniMapOn(boolean miniMapOn) {
    this.miniMapOn = miniMapOn;
  }

  public BufferedImage[] getWorldMap() {
    return worldMap;
  }

  public void createWorldMap() {
    worldMap = new BufferedImage[GamePanel.MAX_MAP];

    int maxWorldCol = gp.getMaxWorldCol();
    int maxWorldRow = gp.getMaxWorldRow();
    int worldMapWidth = GamePanel.TILE_SIZE * maxWorldCol;
    int worldMapHeight = GamePanel.TILE_SIZE * maxWorldRow;

    for (int i = 0; i < GamePanel.MAX_MAP; i++) {

      worldMap[i] = new BufferedImage(worldMapWidth, worldMapHeight, BufferedImage.TYPE_INT_ARGB);
      Graphics2D g2 = worldMap[i].createGraphics();

      int col = 0;
      int row = 0;

      while (col < maxWorldCol && row < maxWorldRow) {

        int tileNum = getMapTileNum()[i][col][row];
        int x = GamePanel.TILE_SIZE * col;
        int y = GamePanel.TILE_SIZE * row;

        g2.drawImage(getTiles()[tileNum].getImage(), x, y, null);

        col++;
        if (col == maxWorldCol) {
          col = 0;
          row++;
        }
      }
      g2.dispose();
    }
  }

  public void drawFullMapScreen(Graphics2D g2) {

    // Background Color
    g2.setColor(Color.black);
    g2.fillRect(0, 0, GamePanel.SCREEN_WIDTH, GamePanel.SCREEN_HEIGHT);

    // Draw Map
    int width = 600;
    int height = 600;
    int x = GamePanel.SCREEN_WIDTH / 2 - width / 2;
    int y = GamePanel.SCREEN_HEIGHT / 2 - height / 2;

    int currentMapIndex = gp.getCurrentMap();
    g2.drawImage(worldMap[currentMapIndex], x, y, width, height, null);

    // Draw Player
    int maxWorldCol = gp.getMaxWorldCol();
    double scale = (double) (GamePanel.TILE_SIZE * maxWorldCol) / width;

    var player = gp.getPlayer();
    int playerX = (int) (x + player.getWorldX() / scale);
    int playerY = (int) (y + player.getWorldY() / scale);
    int playerSize = (int) (GamePanel.TILE_SIZE / 2.0);

    g2.drawImage(player.getDown1(), playerX, playerY, playerSize, playerSize, null);
  }

  public void drawMiniMap(Graphics2D g2) {

    if (!miniMapOn) {
      return;
    }

    int width = 300;
    int height = 300;
    int x = GamePanel.SCREEN_WIDTH - width - 50;
    int y = 50;

    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.8f));

    int currentMapIndex = gp.getCurrentMap();
    g2.drawImage(worldMap[currentMapIndex], x, y, width, height, null);

    int maxWorldCol = gp.getMaxWorldCol();
    double scale = (double) (GamePanel.TILE_SIZE * maxWorldCol) / width;

    var player = gp.getPlayer();
    int playerX = (int) (x + player.getWorldX() / scale);
    int playerY = (int) (y + player.getWorldY() / scale);
    int playerSize = (int) (GamePanel.TILE_SIZE / 3.0);

    g2.drawImage(player.getDown1(), playerX - 6, playerY - 6, playerSize, playerSize, null);

    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
  }
}

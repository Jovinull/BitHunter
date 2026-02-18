package tile;

import java.awt.Color;
import java.awt.Graphics2D;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import main.GamePanel;
import main.UtilityTool;

/**
 * Classe responsável por gerenciar os tiles (blocos do mapa)
 */
public class TileManager {

  private static final Logger LOGGER = Logger.getLogger(TileManager.class.getName());
  private static final Color PATH_COLOR = new Color(255, 0, 0, 70);

  // Referência ao painel do jogo (agora protegida, não pública – S1104)
  protected final GamePanel gp;

  // Encapsulados (S1104)
  private Tile[] tile; // Vetor de tipos de tiles disponíveis
  private int[][][] mapTileNum; // Matriz do mapa (IDs de tiles)
  private boolean drawPath = false; // Flag para desenhar pathfinding

  private final List<String> fileNames = new ArrayList<>();
  private final List<String> collisionStatus = new ArrayList<>();

  /**
   * Construtor: carrega metadados de tiles e inicializa o mundo
   */
  public TileManager(GamePanel gp) {
    this.gp = gp;

    // Lê nomes e colisões dos tiles
    try (InputStream is = getClass().getResourceAsStream("/maps/tiledata.txt");
        BufferedReader br = new BufferedReader(new InputStreamReader(is))) {

      String line;
      while ((line = br.readLine()) != null) {
        fileNames.add(line);
        collisionStatus.add(br.readLine());
      }
    } catch (IOException ex) {
      LOGGER.log(Level.SEVERE, "Falha ao ler /maps/tiledata.txt", ex);
    }

    // Inicializa o array de tiles com base no arquivo de metadados
    tile = new Tile[fileNames.size()];
    getTileImage();

    // Obtém dimensões iniciais do mundo a partir do primeiro mapa
    try (InputStream is = getClass().getResourceAsStream("/maps/world01.txt");
        BufferedReader br = new BufferedReader(new InputStreamReader(is))) {

      String firstLine = br.readLine();
      if (firstLine == null || firstLine.isEmpty()) {
        LOGGER.severe("Arquivo de mapa vazio: /maps/world01.txt");
      } else {
        String[] maxTile = firstLine.split(" "); // S1197 ok
        int worldSize = maxTile.length;

        gp.setMaxWorldCol(worldSize);
        gp.setMaxWorldRow(worldSize); // Mantido como no original

        mapTileNum = new int[GamePanel.MAX_MAP][worldSize][worldSize];
      }
    } catch (Exception ex) {
      LOGGER.severe("Erro ao obter maxWorldCol/maxWorldRow de /maps/world01.txt");
    }

    // Carrega mapas
    loadMap("/maps/world01.txt", 0);
    loadMap("/maps/indoor01.txt", 1);
    loadMap("/maps/dungeon01.txt", 2);
    loadMap("/maps/dungeon02.txt", 3);
    loadMap("/maps/indoor02.txt", 4);
  }

  /**
   * Carrega as imagens e a flag de colisão de cada tile.
   * Visibilidade private: evita chamada de método sobrescrevível no construtor.
   */
  private void getTileImage() {
    for (int i = 0; i < fileNames.size(); i++) {
      String fileName = fileNames.get(i);
      boolean collision = "true".equals(collisionStatus.get(i));
      setup(i, fileName, collision);
    }
  }

  /**
   * Configura um tile individual
   */
  public void setup(int index, String imageName, boolean collision) {
    UtilityTool uTool = new UtilityTool();
    try {
      Tile t = new Tile();
      t.setImage(ImageIO.read(getClass().getResourceAsStream("/tiles/" + imageName)));
      t.setImage(uTool.scaleImage(t.getImage(), GamePanel.TILE_SIZE, GamePanel.TILE_SIZE));
      t.setCollision(collision);
      tile[index] = t;
    } catch (IOException ex) {
      LOGGER.log(Level.SEVERE, ex, () -> "Falha ao carregar tile '" + imageName + "'");
    }
  }

  /**
   * Lê o layout do mapa de um arquivo de texto e preenche a matriz mapTileNum.
   * Marcado como final: evita chamada de método sobrescrevível no construtor.
   */
  public final void loadMap(String filePath, int map) {
    try (InputStream is = getClass().getResourceAsStream(filePath);
        BufferedReader br = new BufferedReader(new InputStreamReader(is))) {

      int col = 0;
      int row = 0;
      int maxWorldCol = gp.getMaxWorldCol();
      int maxWorldRow = gp.getMaxWorldRow();

      // Percorre o arquivo linha a linha
      while (col < maxWorldCol && row < maxWorldRow) {
        String line = br.readLine();
        if (line == null) {
          break;
        }

        String[] numbers = line.split(" "); // S1197 ok

        while (col < maxWorldCol) {
          int num = (col < numbers.length) ? Integer.parseInt(numbers[col]) : 0;
          mapTileNum[map][col][row] = num;
          col++;
        }

        if (col == maxWorldCol) {
          col = 0;
          row++;
        }
      }
    } catch (Exception ex) {
      LOGGER.log(Level.SEVERE, ex, () -> "Erro ao carregar mapa: " + filePath);
    }
  }

  /**
   * Desenha apenas os tiles visíveis na tela com base na posição do jogador
   */
  public void draw(Graphics2D g2) {

    int worldCol = 0;
    int worldRow = 0;
    int maxWorldCol = gp.getMaxWorldCol();
    int maxWorldRow = gp.getMaxWorldRow();

    var player = gp.getPlayer();
    int currentMapIndex = gp.getCurrentMap();

    while (worldCol < maxWorldCol && worldRow < maxWorldRow) {

      int tileNum = mapTileNum[currentMapIndex][worldCol][worldRow];

      int worldX = worldCol * GamePanel.TILE_SIZE;
      int worldY = worldRow * GamePanel.TILE_SIZE;

      int screenX = worldX - player.getWorldX() + player.screenX;
      int screenY = worldY - player.getWorldY() + player.screenY;

      if (worldX + GamePanel.TILE_SIZE > player.getWorldX() - player.screenX &&
          worldX - GamePanel.TILE_SIZE < player.getWorldX() + player.screenX &&
          worldY + GamePanel.TILE_SIZE > player.getWorldY() - player.screenY &&
          worldY - GamePanel.TILE_SIZE < player.getWorldY() + player.screenY) {

        g2.drawImage(tile[tileNum].getImage(), screenX, screenY, null);
      }

      worldCol++;
      if (worldCol == maxWorldCol) {
        worldCol = 0;
        worldRow++;
      }
    }

    var pathFinder = gp.getpFinder();
    var pathList = (pathFinder != null) ? pathFinder.getPathList() : null;

    if (drawPath && pathList != null) {
      g2.setColor(PATH_COLOR);
      for (var node : pathList) {
        int pathWorldX = node.getCol() * GamePanel.TILE_SIZE;
        int pathWorldY = node.getRow() * GamePanel.TILE_SIZE;
        int pathScreenX = pathWorldX - player.getWorldX() + player.screenX;
        int pathScreenY = pathWorldY - player.getWorldY() + player.screenY;
        g2.fillRect(pathScreenX, pathScreenY, GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);
      }
    }
  }

  // ====== ACESSORES ======

  public Tile[] getTiles() {
    return tile;
  }

  public int[][][] getMapTileNum() {
    return mapTileNum;
  }

  public boolean isDrawPath() {
    return drawPath;
  }

  public void setDrawPath(boolean drawPath) {
    this.drawPath = drawPath;
  }
}

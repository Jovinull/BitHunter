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

  // Mantido público (final) pois é uma dependência externa compartilhada
  public final GamePanel gp;

  // S1104: encapsular campos em vez de expor como públicos
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
      // S106: usar logger em vez de System.out/printStackTrace
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
        // S1197: colchetes no tipo, não no nome da variável
        String[] maxTile = firstLine.split(" ");
        gp.maxWorldCol = maxTile.length;
        gp.maxWorldRow = maxTile.length; // Mantido como no original
        mapTileNum = new int[gp.maxMap][gp.maxWorldCol][gp.maxWorldRow];
      }
    } catch (Exception _) { // S7467: padrão não nomeado, exceção não utilizada
      LOGGER.severe("Erro ao obter maxWorldCol/maxWorldRow de /maps/world01.txt");
    }

    // Carrega mapas
    loadMap("/maps/world01.txt", 0);
    loadMap("/maps/indoor01.txt", 1);
    loadMap("/maps/dungeon01.txt", 2);
    loadMap("/maps/dungeon02.txt", 3);
  }

  /**
   * Carrega as imagens e a flag de colisão de cada tile
   */
  public void getTileImage() {
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
      t.setImage(uTool.scaleImage(t.getImage(), gp.tileSize, gp.tileSize));
      t.setCollision(collision);
      tile[index] = t;
    } catch (IOException ex) {
      // S106: logger
      LOGGER.log(Level.SEVERE, "Falha ao carregar tile '" + imageName + "'", ex);
    }
  }

  /**
   * Lê o layout do mapa de um arquivo de texto e preenche a matriz mapTileNum
   */
  public void loadMap(String filePath, int map) {
    try (InputStream is = getClass().getResourceAsStream(filePath);
        BufferedReader br = new BufferedReader(new InputStreamReader(is))) {

      int col = 0;
      int row = 0;

      // Percorre o arquivo linha a linha
      while (col < gp.maxWorldCol && row < gp.maxWorldRow) {
        String line = br.readLine();
        if (line == null) {
          break; // Linha ausente: encerra para evitar NPE
        }

        // S1197: colchetes no tipo
        String[] numbers = line.split(" ");

        while (col < gp.maxWorldCol) {
          // Evita ArrayIndexOutOfBounds se a linha tiver menos colunas
          int num = (col < numbers.length) ? Integer.parseInt(numbers[col]) : 0;
          mapTileNum[map][col][row] = num;
          col++;
        }

        if (col == gp.maxWorldCol) {
          col = 0;
          row++;
        }
      }
    } catch (Exception ex) {
      // S106: logger
      LOGGER.log(Level.SEVERE, "Erro ao carregar mapa: " + filePath, ex);
    }
  }

  /**
   * Desenha apenas os tiles visíveis na tela com base na posição do jogador
   */
  public void draw(Graphics2D g2) {

    int worldCol = 0;
    int worldRow = 0;

    while (worldCol < gp.maxWorldCol && worldRow < gp.maxWorldRow) {

      int tileNum = mapTileNum[gp.currentMap][worldCol][worldRow];

      int worldX = worldCol * gp.tileSize;
      int worldY = worldRow * gp.tileSize;

      int screenX = worldX - gp.player.getWorldX() + gp.player.screenX;
      int screenY = worldY - gp.player.getWorldY() + gp.player.screenY;

      if (worldX + gp.tileSize > gp.player.getWorldX() - gp.player.screenX &&
          worldX - gp.tileSize < gp.player.getWorldX() + gp.player.screenX &&
          worldY + gp.tileSize > gp.player.getWorldY() - gp.player.screenY &&
          worldY - gp.tileSize < gp.player.getWorldY() + gp.player.screenY) {

        g2.drawImage(tile[tileNum].getImage(), screenX, screenY, null);
      }

      worldCol++;
      if (worldCol == gp.maxWorldCol) {
        worldCol = 0;
        worldRow++;
      }
    }

    if (drawPath && gp.pFinder != null && gp.pFinder.pathList != null) {
      g2.setColor(new Color(255, 0, 0, 70));
      for (int i = 0; i < gp.pFinder.pathList.size(); i++) {
        int pathWorldX = gp.pFinder.pathList.get(i).getCol() * gp.tileSize;
        int pathWorldY = gp.pFinder.pathList.get(i).getRow() * gp.tileSize;
        int pathScreenX = pathWorldX - gp.player.getWorldX() + gp.player.screenX;
        int pathScreenY = pathWorldY - gp.player.getWorldY() + gp.player.screenY;
        g2.fillRect(pathScreenX, pathScreenY, gp.tileSize, gp.tileSize);
      }
    }
  }

  // ====== ACESSORES (encapsulamento para S1104) ======

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

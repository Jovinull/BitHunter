package tile;

import java.awt.Color;
import java.awt.Graphics2D;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.imageio.ImageIO;

import main.GamePanel;
import main.UtilityTool;

// Classe responsável por gerenciar os tiles (blocos do mapa)
public class TileManager {

  GamePanel gp; // Referência ao painel principal do jogo
  public Tile[] tile; // Vetor que armazena os tipos de tiles disponíveis (grama, parede, etc.)
  public int mapTileNum[][][]; // Matriz que representa o mapa (cada número corresponde a um tile)
  public boolean drawPath = false;

  // Construtor: carrega as imagens dos tiles e o mapa a partir de um arquivo .txt
  public TileManager(GamePanel gp) {
    this.gp = gp;

    tile = new Tile[50]; // Inicializa o array com espaço para até 10 tipos de tiles
    mapTileNum = new int[gp.maxMap][gp.maxWorldCol][gp.maxWorldRow]; // Matriz do mapa com base no mundo total

    getTileImage(); // Carrega as imagens dos tiles
    loadMap("/maps/world01.txt", 0);
    loadMap("/maps/interior01.txt", 1);
  }

  // Carrega as imagens e solidez correspondentes a cada tipo de tile
  public void getTileImage() {

    // PLACEHOLDER
    setup(0, "void", true);
    setup(1, "void", true);
    setup(2, "void", true);
    setup(3, "void", true);
    setup(4, "void", true);
    setup(5, "void", true);
    setup(6, "void", true);
    setup(7, "void", true);
    setup(8, "void", true);
    setup(9, "void", true);

    // GRASS TILES
    setup(10, "grass00", false);
    setup(11, "grass01", false);

    // WATER TILES
    setup(12, "water00", true);
    setup(13, "water01", true);
    setup(14, "water02", true);
    setup(15, "water03", true);
    setup(16, "water04", true);
    setup(17, "water05", true);
    setup(18, "water06", true);
    setup(19, "water07", true);
    setup(20, "water08", true);
    setup(21, "water09", true);
    setup(22, "water10", true);
    setup(23, "water11", true);
    setup(24, "water12", true);
    setup(25, "water13", true);

    // ROAD TILES
    setup(26, "road00", false);
    setup(27, "road01", false);
    setup(28, "road02", false);
    setup(29, "road03", false);
    setup(30, "road04", false);
    setup(31, "road05", false);
    setup(32, "road06", false);
    setup(33, "road07", false);
    setup(34, "road08", false);
    setup(35, "road09", false);
    setup(36, "road10", false);
    setup(37, "road11", false);
    setup(38, "road12", false);

    // EARTH TILES
    setup(39, "earth", false);

    // WALL TILES
    setup(40, "wall", true);

    // TREE TILES
    setup(41, "tree", true);

    // TILES SEM USO
    setup(42, "hut", false);
    setup(43, "floor01", false);
    setup(44, "table01", true);
  }

  public void setup(int index, String imageName, boolean collision) {

    UtilityTool uTool = new UtilityTool();

    try {
      tile[index] = new Tile();
      tile[index].image = ImageIO.read(getClass().getResourceAsStream("/tiles/" + imageName + ".png"));
      tile[index].image = uTool.scaleImage(tile[index].image, gp.tileSize, gp.tileSize);
      tile[index].collision = collision;

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  // Lê o layout do mapa de um arquivo de texto e preenche a matriz mapTileNum
  public void loadMap(String filePath, int map) {
    try {
      InputStream is = getClass().getResourceAsStream(filePath); // Abre o arquivo
      BufferedReader br = new BufferedReader(new InputStreamReader(is));

      int col = 0;
      int row = 0;

      // Lê o arquivo linha por linha e converte os números em IDs de tiles
      while (col < gp.maxWorldCol && row < gp.maxWorldRow) {
        String line = br.readLine(); // Lê uma linha do mapa
        String numbers[] = line.split(" "); // Divide a linha em números

        while (col < gp.maxWorldCol) {
          int num = Integer.parseInt(numbers[col]); // Converte para inteiro
          mapTileNum[map][col][row] = num; // Armazena na posição [col][row]
          col++;
        }

        // Avança para a próxima linha
        if (col == gp.maxWorldCol) {
          col = 0;
          row++;
        }
      }

      br.close(); // Fecha o arquivo após leitura

    } catch (Exception e) {
      e.printStackTrace(); // Exibe erro se houver falha na leitura
    }
  }

  // Desenha apenas os tiles visíveis na tela com base na posição do jogador
  // (câmera)
  public void draw(Graphics2D g2) {

    int worldCol = 0;
    int worldRow = 0;

    // Percorre cada tile do mundo
    while (worldCol < gp.maxWorldCol && worldRow < gp.maxWorldRow) {

      int tileNum = mapTileNum[gp.currentMap][worldCol][worldRow]; // Identifica o tipo de tile naquela posição

      // Calcula a posição absoluta do tile no mundo
      int worldX = worldCol * gp.tileSize;
      int worldY = worldRow * gp.tileSize;

      // Converte a posição do mundo para posição relativa na tela (scrolling/câmera)
      int screenX = worldX - gp.player.worldX + gp.player.screenX;
      int screenY = worldY - gp.player.worldY + gp.player.screenY;

      // Verifica se o tile está dentro da área visível da tela
      if (worldX + gp.tileSize > gp.player.worldX - gp.player.screenX &&
          worldX - gp.tileSize < gp.player.worldX + gp.player.screenX &&
          worldY + gp.tileSize > gp.player.worldY - gp.player.screenY &&
          worldY - gp.tileSize < gp.player.worldY + gp.player.screenY) {

        // Se estiver visível, desenha o tile na tela
        g2.drawImage(tile[tileNum].image, screenX, screenY, null);
      }

      worldCol++;

      // Avança para a próxima linha ao fim da coluna
      if (worldCol == gp.maxWorldCol) {
        worldCol = 0;
        worldRow++;
      }
    }

    if (drawPath == true) {

      g2.setColor(new Color(255, 0, 0, 70));

      for (int i = 0; i < gp.pFinder.pathList.size(); i++) {

        int worldX = gp.pFinder.pathList.get(i).col * gp.tileSize;
        int worldY = gp.pFinder.pathList.get(i).row * gp.tileSize;
        int screenX = worldX - gp.player.worldX + gp.player.screenX;
        int screenY = worldY - gp.player.worldY + gp.player.screenY;

        g2.fillRect(screenX, screenY, gp.tileSize, gp.tileSize);
      }
    }
  }
}

package tile;

import java.awt.Color;
import java.awt.Graphics2D;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import main.GamePanel;
import main.UtilityTool;

// Classe responsável por gerenciar os tiles (blocos do mapa)
public class TileManager {

  GamePanel gp; // Referência ao painel principal do jogo
  public Tile[] tile; // Vetor que armazena os tipos de tiles disponíveis (grama, parede, etc.)
  public int mapTileNum[][][]; // Matriz que representa o mapa (cada número corresponde a um tile)
  public boolean drawPath = false;
  ArrayList<String> fileNames = new ArrayList<>();
  ArrayList<String> collisionStatus = new ArrayList<>();

  // Construtor: carrega as imagens dos tiles e o mapa a partir de um arquivo .txt
  public TileManager(GamePanel gp) {
    this.gp = gp;

    // READ TILE DATA FILE
    InputStream is = getClass().getResourceAsStream("/maps/tiledata.txt");
    BufferedReader br = new BufferedReader(new InputStreamReader(is));

    // GETTING TILE NAMES AND COLLISION INFO FROM THE FILE
    String line;

    try {
      while ((line = br.readLine()) != null) {
        fileNames.add(line);
        collisionStatus.add(br.readLine());
      }
      br.close();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    // INITIALIZE THE TILE ARRAY BASED ON THE fileNames SIZE
    tile = new Tile[fileNames.size()];
    getTileImage(); // Carrega as imagens dos tiles

    // GET THE maxWorldCol & Row
    is = getClass().getResourceAsStream("/maps/world01.txt");
    br = new BufferedReader(new InputStreamReader(is));

    try {
      String line2 = br.readLine();
      String maxTile[] = line2.split(" ");

      gp.maxWorldCol = maxTile.length;
      gp.maxWorldRow = maxTile.length;
      mapTileNum = new int[gp.maxMap][gp.maxWorldCol][gp.maxWorldRow];

      br.close();
    } catch (Exception e) {
      System.out.println("Erro ao ler o arquivo do mapa para obter maxWorldCol e maxWorldRow");
    }

    loadMap("/maps/world01.txt", 0);
    loadMap("/maps/indoor01.txt", 1);
  }

  // Carrega as imagens e solidez correspondentes a cada tipo de tile
  public void getTileImage() {

    for (int i = 0; i < fileNames.size(); i++) {
      String fileName;
      boolean collision;

      // Get a file name
      fileName = fileNames.get(i);

      // Get a collision status
      if (collisionStatus.get(i).equals("true")) {
        collision = true;
      } else {
        collision = false;
      }

      setup(i, fileName, collision);
    }
  }

  public void setup(int index, String imageName, boolean collision) {

    UtilityTool uTool = new UtilityTool();

    try {
      tile[index] = new Tile();
      tile[index].image = ImageIO.read(getClass().getResourceAsStream("/tiles/" + imageName));
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

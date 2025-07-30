package tile;

import java.awt.Graphics2D;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.imageio.ImageIO;

import main.GamePanel;

// Classe responsável por gerenciar os tiles (blocos do mapa)
public class TileManager {

    GamePanel gp; // Referência ao painel principal do jogo
    Tile[] tile; // Vetor que armazena os tipos de tiles disponíveis (grama, parede, etc.)
    int mapTileNum[][]; // Matriz que representa o mapa (cada número corresponde a um tile)

    // Construtor: carrega as imagens dos tiles e o mapa a partir de um arquivo .txt
    public TileManager(GamePanel gp) {
        this.gp = gp;

        tile = new Tile[10]; // Inicializa o array com espaço para até 10 tipos de tiles
        mapTileNum = new int[gp.maxWorldCol][gp.maxWorldRow]; // Matriz do mapa com base no mundo total

        getTileImage(); // Carrega as imagens dos tiles
        loadMap("/resource/maps/world01.txt"); // Carrega o layout do mapa do arquivo
    }

    // Carrega as imagens correspondentes a cada tipo de tile
    public void getTileImage() {
        try {
            tile[0] = new Tile();
            tile[0].image = ImageIO.read(getClass().getResourceAsStream("/resource/tiles/grass.png"));

            tile[1] = new Tile();
            tile[1].image = ImageIO.read(getClass().getResourceAsStream("/resource/tiles/wall.png"));

            tile[2] = new Tile();
            tile[2].image = ImageIO.read(getClass().getResourceAsStream("/resource/tiles/water.png"));

            tile[3] = new Tile();
            tile[3].image = ImageIO.read(getClass().getResourceAsStream("/resource/tiles/earth.png"));

            tile[4] = new Tile();
            tile[4].image = ImageIO.read(getClass().getResourceAsStream("/resource/tiles/tree.png"));

            tile[5] = new Tile();
            tile[5].image = ImageIO.read(getClass().getResourceAsStream("/resource/tiles/sand.png"));

        } catch (IOException e) {
            e.printStackTrace(); // Exibe erro se não conseguir carregar alguma imagem
        }
    }

    // Lê o layout do mapa de um arquivo de texto e preenche a matriz mapTileNum
    public void loadMap(String filePath) {
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
                    mapTileNum[col][row] = num; // Armazena na posição [col][row]
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

            int tileNum = mapTileNum[worldCol][worldRow]; // Identifica o tipo de tile naquela posição

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
                g2.drawImage(tile[tileNum].image, screenX, screenY, gp.tileSize, gp.tileSize, null);
            }

            worldCol++;

            // Avança para a próxima linha ao fim da coluna
            if (worldCol == gp.maxWorldCol) {
                worldCol = 0;
                worldRow++;
            }
        }
    }
}

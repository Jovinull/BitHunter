package tile;

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
    public int mapTileNum[][]; // Matriz que representa o mapa (cada número corresponde a um tile)

    // Construtor: carrega as imagens dos tiles e o mapa a partir de um arquivo .txt
    public TileManager(GamePanel gp) {
        this.gp = gp;

        tile = new Tile[50]; // Inicializa o array com espaço para até 10 tipos de tiles
        mapTileNum = new int[gp.maxWorldCol][gp.maxWorldRow]; // Matriz do mapa com base no mundo total

        getTileImage(); // Carrega as imagens dos tiles
        loadMap("/resource/maps/world01.txt"); // Carrega o layout do mapa do arquivo
    }

    // Carrega as imagens e solidez correspondentes a cada tipo de tile
    public void getTileImage() {

        // PLACEHOLDER
        setup(0, "000", true);
        setup(1, "000", true);
        setup(2, "000", true);
        setup(3, "000", true);
        setup(4, "000", true);
        setup(5, "000", true);
        setup(6, "000", true);
        setup(7, "000", true);
        setup(8, "000", true);
        setup(9, "000", true);

        // GRASS TILES
        setup(10, "001", false);
        setup(11, "002", false);

        // WATER TILES
        setup(12, "018", true);
        setup(13, "019", true);
        setup(14, "020", true);
        setup(15, "021", true);
        setup(16, "022", true);
        setup(17, "023", true);
        setup(18, "024", true);
        setup(19, "025", true);
        setup(20, "026", true);
        setup(21, "027", true);
        setup(22, "028", true);
        setup(23, "029", true);
        setup(24, "030", true);
        setup(25, "031", true);

        // ROAD TILES
        setup(26, "003", false);
        setup(27, "004", false);
        setup(28, "005", false);
        setup(29, "006", false);
        setup(30, "007", false);
        setup(31, "008", false);
        setup(32, "009", false);
        setup(33, "010", false);
        setup(34, "011", false);
        setup(35, "012", false);
        setup(36, "013", false);
        setup(37, "014", false);
        setup(38, "015", false);

        // EARTH TILES
        setup(39, "017", false);

        // WALL TILES
        setup(40, "032", true);

        // TREE TILES
        setup(41, "016", true);

        // TILES SEM USO
        setup(42, "033", false);
        setup(43, "034", false);
        setup(44, "035", false);
        setup(45, "036", false);
        setup(46, "037", false);
    }

    public void setup(int index, String imageName, boolean collision) {

        UtilityTool uTool = new UtilityTool();

        try {
            tile[index] = new Tile();
            tile[index].image = ImageIO.read(getClass().getResourceAsStream("/resource/tiles/" + imageName + ".png"));
            tile[index].image = uTool.scaleImage(tile[index].image, gp.tileSize, gp.tileSize);
            tile[index].collision = collision;

        } catch (IOException e) {
            e.printStackTrace();
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
                g2.drawImage(tile[tileNum].image, screenX, screenY, null);
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

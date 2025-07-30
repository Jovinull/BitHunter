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

    GamePanel gp; // Referência ao painel do jogo
    Tile[] tile; // Vetor de tipos de tiles (ex: grama, água, muro...)
    int mapTileNum[][]; // Mapa: matriz que armazena o número do tile em cada posição da tela

    // Construtor: carrega imagens dos tiles e o mapa a partir de um arquivo
    public TileManager(GamePanel gp) {
        this.gp = gp;

        tile = new Tile[10]; // Cria espaço para até 10 tipos diferentes de tiles (ajustável)
        mapTileNum = new int[gp.maxScreenCol][gp.maxScreenRow]; // Mapa baseado nas dimensões da tela

        getTileImage(); // Carrega imagens dos tiles
        loadMap("/resource/maps/world01.txt"); // Carrega o layout do mapa
    }

    // Carrega as imagens de cada tipo de tile
    public void getTileImage() {
        try {
            tile[0] = new Tile();
            tile[0].image = ImageIO.read(getClass().getResourceAsStream("/resource/tiles/grass.png"));

            tile[1] = new Tile();
            tile[1].image = ImageIO.read(getClass().getResourceAsStream("/resource/tiles/wall.png"));

            tile[2] = new Tile();
            tile[2].image = ImageIO.read(getClass().getResourceAsStream("/resource/tiles/water.png"));

            tile[3] = new Tile();
            tile[3].image = ImageIO.read(getClass().getResourceAsStream("/resource/tiles/sand.png"));

            tile[4] = new Tile();
            tile[4].image = ImageIO.read(getClass().getResourceAsStream("/resource/tiles/tree.png"));

            tile[5] = new Tile();
            tile[5].image = ImageIO.read(getClass().getResourceAsStream("/resource/tiles/earth.png"));
        } catch (IOException e) {
            e.printStackTrace(); // Exibe erro se alguma imagem falhar ao carregar
        }
    }

    // Lê o layout do mapa a partir de um arquivo de texto e popula a matriz
    // mapTileNum
    public void loadMap(String filePath) {
        try {
            InputStream is = getClass().getResourceAsStream(filePath);
            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            int col = 0;
            int row = 0;

            // Lê linha por linha até preencher todas as colunas e linhas visíveis da tela
            while (col < gp.maxScreenCol && row < gp.maxScreenRow) {
                String line = br.readLine(); // Lê uma linha do arquivo
                String numbers[] = line.split(" "); // Divide a linha por espaços

                while (col < gp.maxScreenCol) {
                    int num = Integer.parseInt(numbers[col]); // Converte o texto em número
                    mapTileNum[col][row] = num; // Atribui esse número à posição [col][row] no mapa
                    col++;
                }

                // Quando atingir o fim da linha, reinicia coluna e avança para próxima linha
                if (col == gp.maxScreenCol) {
                    col = 0;
                    row++;
                }
            }
            br.close(); // Fecha o arquivo após leitura

        } catch (Exception e) {
            e.printStackTrace(); // Mostra erro se falhar ao ler ou interpretar o mapa
        }
    }

    // Desenha o mapa de tiles na tela com base na matriz mapTileNum
    public void draw(Graphics2D g2) {

        int col = 0;
        int row = 0;
        int x = 0;
        int y = 0;

        // Percorre cada linha e coluna da matriz de tiles
        while (col < gp.maxScreenCol && row < gp.maxScreenRow) {

            int tileNum = mapTileNum[col][row]; // Número do tile a ser desenhado na posição atual

            // Desenha a imagem do tile na posição x, y
            g2.drawImage(tile[tileNum].image, x, y, gp.tileSize, gp.tileSize, null);

            col++;
            x += gp.tileSize;

            // Ao chegar ao fim da linha, reseta coluna e avança para próxima linha
            if (col == gp.maxScreenCol) {
                col = 0;
                x = 0;
                row++;
                y += gp.tileSize;
            }
        }
    }
}

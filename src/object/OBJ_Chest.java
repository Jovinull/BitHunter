package object;

import java.io.IOException;
import javax.imageio.ImageIO;

import main.GamePanel;

// Representa o baú do jogo, pode conter itens ou recompensas
public class OBJ_Chest extends SuperObject {

    GamePanel gp; // Referência ao GamePanel, necessário para acessar recursos do jogo

    // Construtor: define nome do objeto e carrega a imagem do baú
    public OBJ_Chest(GamePanel gp) {
        this.gp = gp; // Inicializa a referência ao GamePanel
        
        name = "Chest"; // Identificador usado em lógica de jogo
        try {
            // Carrega o sprite do baú a partir do classpath
            image = ImageIO.read(
                    getClass().getResourceAsStream("/resource/objects/chest.png"));
            uTool.scaleImage(image, gp.tileSize, gp.tileSize); // Redimensiona a imagem para o tamanho do tile
        } catch (IOException e) {
            // Em caso de erro no carregamento, exibe stacktrace para depuração
            e.printStackTrace();
        }
    }
}

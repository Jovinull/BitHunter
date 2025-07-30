package object;

import java.io.IOException;
import javax.imageio.ImageIO;

// Representa o baú do jogo, pode conter itens ou recompensas
public class OBJ_Chest extends SuperObject {

    // Construtor: define nome do objeto e carrega a imagem do baú
    public OBJ_Chest() {
        name = "Chest"; // Identificador usado em lógica de jogo
        try {
            // Carrega o sprite do baú a partir do classpath
            image = ImageIO.read(
                    getClass().getResourceAsStream("/resource/objects/chest.png"));
        } catch (IOException e) {
            // Em caso de erro no carregamento, exibe stacktrace para depuração
            e.printStackTrace();
        }
    }
}

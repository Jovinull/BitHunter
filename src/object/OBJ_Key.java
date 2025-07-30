package object;

import java.io.IOException;
import javax.imageio.ImageIO;

// Representa a chave do jogo, extensão de SuperObject
public class OBJ_Key extends SuperObject {

    // Construtor: define nome e carrega a imagem da chave
    public OBJ_Key() {
        name = "Key"; // Identificador do objeto
        try {
            // Carrega imagem do recurso no classpath
            image = ImageIO.read(
                    getClass().getResourceAsStream("/resource/objects/key.png"));
        } catch (IOException e) {
            // Em caso de falha no carregamento, imprime stacktrace para depuração
            e.printStackTrace();
        }
    }
}

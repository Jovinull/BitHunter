package object;

import java.io.IOException;
import javax.imageio.ImageIO;

import main.GamePanel;

// Representa a chave do jogo, extensão de SuperObject
public class OBJ_Key extends SuperObject {

    GamePanel gp; // Referência ao GamePanel, necessário para acessar recursos do jogo

    // Construtor: define nome e carrega a imagem da chave
    public OBJ_Key(GamePanel gp) {
        this.gp = gp; // Inicializa a referência ao GamePanel

        name = "Key"; // Identificador do objeto
        try {
            // Carrega imagem do recurso no classpath
            image = ImageIO.read(
                    getClass().getResourceAsStream("/resource/objects/key.png"));
            uTool.scaleImage(image, gp.tileSize, gp.tileSize); // Redimensiona a imagem para o tamanho do tile
        } catch (IOException e) {
            // Em caso de falha no carregamento, imprime stacktrace para depuração
            e.printStackTrace();
        }
    }
}

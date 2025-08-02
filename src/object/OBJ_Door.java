package object;

import java.io.IOException;
import javax.imageio.ImageIO;

import main.GamePanel;

// Representa a porta do jogo, permite bloqueio de passagem se collision=true
public class OBJ_Door extends SuperObject {

    GamePanel gp; // Referência ao GamePanel, necessário para acessar recursos do jogo

    // Construtor: define nome, habilita colisão e carrega a imagem da porta
    public OBJ_Door(GamePanel gp) {
        this.gp = gp; // Inicializa a referência ao GamePanel

        name = "Door"; // Identificador do objeto
        try {
            // Carrega imagem da porta a partir do recurso no classpath
            image = ImageIO.read(
                    getClass().getResourceAsStream("/resource/objects/door.png"));
            uTool.scaleImage(image, gp.tileSize, gp.tileSize); // Redimensiona a imagem para o tamanho do tile
        } catch (IOException e) {
            // Em caso de falha no carregamento, imprime informações de erro
            e.printStackTrace();
        }
    }
}

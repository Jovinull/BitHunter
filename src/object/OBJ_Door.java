package object;

import java.io.IOException;
import javax.imageio.ImageIO;

// Representa a porta do jogo, permite bloqueio de passagem se collision=true
public class OBJ_Door extends SuperObject {

    // Construtor: define nome, habilita colisão e carrega a imagem da porta
    public OBJ_Door() {
        name = "Door"; // Identificador do objeto
        collision = true; // Porta bloqueia movimento ao colidir
        try {
            // Carrega imagem da porta a partir do recurso no classpath
            image = ImageIO.read(
                    getClass().getResourceAsStream("/resource/objects/door.png"));
        } catch (IOException e) {
            // Em caso de falha no carregamento, imprime informações de erro
            e.printStackTrace();
        }

        collision = true; // Define que a porta deve bloquear colisões
    }
}

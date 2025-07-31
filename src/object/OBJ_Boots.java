package object;

import java.io.IOException;
import javax.imageio.ImageIO;

// Representa as botas coletáveis no jogo (herda de SuperObject)
public class OBJ_Boots extends SuperObject {

    // Construtor: define o nome e carrega a imagem associada ao objeto
    public OBJ_Boots() {
        name = "Boots"; // Nome interno do objeto (usado para identificação lógica)

        try {
            // Carrega a imagem do sprite das botas a partir dos recursos
            image = ImageIO.read(
                    getClass().getResourceAsStream("/resource/objects/boots.png"));
        } catch (IOException e) {
            // Exibe o erro no console caso a imagem não seja carregada corretamente
            e.printStackTrace();
        }
    }
}

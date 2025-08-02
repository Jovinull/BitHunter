package object;

import java.io.IOException;
import javax.imageio.ImageIO;

import main.GamePanel;

// Representa as botas coletáveis no jogo (herda de SuperObject)
public class OBJ_Boots extends SuperObject {

    GamePanel gp; // Referência ao GamePanel, necessário para acessar recursos do jogo

    // Construtor: define o nome e carrega a imagem associada ao objeto
    public OBJ_Boots(GamePanel gp) {
        this.gp = gp; // Inicializa a referência ao GamePanel

        name = "Boots"; // Nome interno do objeto (usado para identificação lógica)
        try {
            // Carrega a imagem do sprite das botas a partir dos recursos
            image = ImageIO.read(
                    getClass().getResourceAsStream("/resource/objects/boots.png"));
            uTool.scaleImage(image, gp.tileSize, gp.tileSize); // Redimensiona a imagem para o tamanho do tile
        } catch (IOException e) {
            // Exibe o erro no console caso a imagem não seja carregada corretamente
            e.printStackTrace();
        }
    }
}

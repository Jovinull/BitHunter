package object;

import entity.Entity;
import main.GamePanel;

// Representa as botas coletáveis no jogo (herda de SuperObject)
public class OBJ_Boots extends Entity {

    // Construtor: define o nome e carrega a imagem associada ao objeto
    public OBJ_Boots(GamePanel gp) {
        super(gp);

        name = "Boots"; // Nome interno do objeto (usado para identificação lógica)
        down1 = setup("/resource/objects/boots", gp.tileSize, gp.tileSize);
    }
}

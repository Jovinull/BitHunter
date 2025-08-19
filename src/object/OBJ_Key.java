package object;

import entity.Entity;
import main.GamePanel;

// Representa a chave do jogo, extensão de SuperObject
public class OBJ_Key extends Entity {

    // Construtor: define nome e carrega a imagem da chave
    public OBJ_Key(GamePanel gp) {
        super(gp);

        name = "Key"; // Identificador do objeto
        down1 = setup("/resource/objects/key");

    }
}

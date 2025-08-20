package object;

import entity.Entity;
import main.GamePanel;

// Representa o baú do jogo, pode conter itens ou recompensas
public class OBJ_Chest extends Entity {

    // Construtor: define nome do objeto e carrega a imagem do baú
    public OBJ_Chest(GamePanel gp) {
        super(gp);

        name = "Chest"; // Identificador usado em lógica de jogo
        down1 = setup("/resource/objects/chest", gp.tileSize, gp.tileSize);
    }
}

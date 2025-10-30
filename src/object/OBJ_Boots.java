package object;

import entity.Entity;
import main.GamePanel;

// Representa as botas coletáveis no jogo (herda de SuperObject)
public class OBJ_Boots extends Entity {

  public static final String objName = "Botas";

  // Construtor: define o nome e carrega a imagem associada ao objeto
  public OBJ_Boots(GamePanel gp) {
    super(gp);

    name = objName;
    down1 = setup("/objects/boots", gp.tileSize, gp.tileSize);
  }
}

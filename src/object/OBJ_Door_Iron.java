package object;

import entity.Entity;
import main.GamePanel;

public class OBJ_Door_Iron extends Entity {
  GamePanel gp;
  public static final String objName = "Porta de Ferro";

  // Construtor: define nome, habilita colisão e carrega a imagem da porta
  public OBJ_Door_Iron(GamePanel gp) {
    super(gp);
    this.gp = gp;

    type = TYPE_OBSTACLE;
    name = objName;
    down1 = setup("/objects/door_iron", gp.tileSize, gp.tileSize);
    collision = true;

    solidArea.x = 0;
    solidArea.y = 16;
    solidArea.width = 48;
    solidArea.height = 32;
    solidAreaDefaultX = solidArea.x;
    solidAreaDefaultY = solidArea.y;

    setDialogue();
  }

  public void setDialogue() {

    dialogues[0][0] = "Isso não vai se mexer.";
  }

  public void interact() {

    startDialogue(this, 0);
  }
}

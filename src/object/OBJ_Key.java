package object;

import entity.Entity;
import main.GamePanel;

// Representa a chave do jogo, extensão de SuperObject
public class OBJ_Key extends Entity {

  GamePanel gp;

  // Construtor: define nome e carrega a imagem da chave
  public OBJ_Key(GamePanel gp) {
    super(gp);
    this.gp = gp;

    type = type_consumable;
    name = "Chave";
    down1 = setup("/objects/key", gp.tileSize, gp.tileSize);
    description = "[" + name + "]\nAbre portas trancadas.";
    price = 100;
    stackable = true;

    setDialogue();
  }

  public void setDialogue() {

    dialogues[0][0] = "Você usa o " + name + " e abra a porta";

    dialogues[1][0] = "O que você está fazendo?";
  }

  public boolean use(Entity entity) {

    int objIndex = getDetected(entity, gp.obj, "Porta");

    if (objIndex != 999) {
      startDialogue(this, 0);
      gp.playSE(3);
      gp.obj[gp.currentMap][objIndex] = null;
      return true;
    } else {
      startDialogue(this, 1);
      return false;
    }
  }
}

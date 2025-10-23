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
    name = "Chave"; // Identificador do objeto
    down1 = setup("/objects/key", gp.tileSize, gp.tileSize);
    description = "[" + name + "]\nAbre portas trancadas.";
    price = 100;
    stackable = true;
  }

  public boolean use(Entity entity) {

    gp.gameState = gp.dialogueState;

    int objIndex = getDetected(entity, gp.obj, "Door");

    if (objIndex != 999) {
      gp.ui.currentDialogue = "Você usa o " + name + " e abra a porta";
      gp.playSE(3);
      gp.obj[gp.currentMap][objIndex] = null;
      return true;
    } else {
      gp.ui.currentDialogue = "O que você está fazendo?";
      return false;
    }
  }
}

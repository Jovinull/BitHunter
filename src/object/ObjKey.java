package object;

import entity.Entity;
import main.GamePanel;

public class ObjKey extends Entity {

  public static final String OBJ_NAME = "Chave";

  public ObjKey(GamePanel gp) {
    super(gp);

    setType(TYPE_CONSUMABLE);
    setName(OBJ_NAME);
    down1 = setup("/objects/key", gp.tileSize, gp.tileSize);
    setDescription("[" + getName() + "]\nAbre portas trancadas.");
    setPrice(100);
    setStackable(true);

    setDialogue();
  }

  public void setDialogue() {
    String[][] d = getDialogues();
    d[0][0] = "Você usa o " + getName() + " e abre a porta";
    d[1][0] = "O que você está fazendo?";
  }

  @Override
  public boolean use(Entity entity) {
    // procura por uma porta à frente do usuário
    int objIndex = getDetected(entity, gp.obj, ObjDoor.OBJ_NAME);

    if (objIndex != 999) {
      startDialogue(this, 0);
      gp.playSE(3);
      gp.obj[gp.currentMap][objIndex] = null; // remove a porta
      return true;
    } else {
      startDialogue(this, 1);
      return false;
    }
  }
}

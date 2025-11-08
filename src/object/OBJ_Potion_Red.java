package object;

import entity.Entity;
import main.GamePanel;

public class OBJ_Potion_Red extends Entity {

  GamePanel gp;
  public static final String objName = "Poção Vermelha";

  public OBJ_Potion_Red(GamePanel gp) {
    super(gp);

    this.gp = gp;

    type = TYPE_CONSUMABLE;
    name = objName;
    value = 5;
    down1 = setup("/objects/potion_red", gp.tileSize, gp.tileSize);
    description = "[" + name + "]\nRestaura sua vida em " + value + ".";
    price = 25;
    stackable = true;

    setDialogue();
  }

  public void setDialogue() {

    dialogues[0][0] = "Você bebe a " + name + "!\n"
        + "Sua vida foi restaurada em " + value + ".";
  }

  public boolean use(Entity entity) {

    startDialogue(this, 0);
    gp.playSE(2);
    return true;
  }
}

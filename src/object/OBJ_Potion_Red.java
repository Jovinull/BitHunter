package object;

import entity.Entity;
import main.GamePanel;

public class OBJ_Potion_Red extends Entity {

  public static final String objName = "Poção Vermelha";

  public OBJ_Potion_Red(GamePanel gp) {
    super(gp);

    setType(TYPE_CONSUMABLE);
    setName(objName);
    setValue(5);

    down1 = setup("/objects/potion_red", gp.tileSize, gp.tileSize);
    setDescription("[" + getName() + "]\nRestaura sua vida em " + getValue() + ".");
    setPrice(25);
    setStackable(true);

    setDialogue();
  }

  public void setDialogue() {
    String[][] d = getDialogues();
    d[0][0] = "Você bebe a " + getName() + "!\nSua vida foi restaurada em " + getValue() + ".";
  }

  @Override
  public boolean use(Entity entity) {
    startDialogue(this, 0);
    gp.playSE(2);
    return true;
  }
}

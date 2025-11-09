package object;

import entity.Entity;
import main.GamePanel;

public class ObjPotionRed extends Entity {

  public static final String OBJ_NAME = "Poção Vermelha";

  public ObjPotionRed(GamePanel gp) {
    super(gp);

    setType(TYPE_CONSUMABLE);
    setName(OBJ_NAME);
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

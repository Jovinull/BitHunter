package object;

import entity.Entity;
import main.GamePanel;

public class OBJ_Heart extends Entity {

  public static final String objName = "Coração";

  public OBJ_Heart(GamePanel gp) {
    super(gp);

    setType(TYPE_PICKUP_ONLY);
    setName(objName);
    setValue(2);

    down1 = setup("/objects/heart_full", gp.tileSize, gp.tileSize);
    image = setup("/objects/heart_full", gp.tileSize, gp.tileSize);
    image2 = setup("/objects/heart_half", gp.tileSize, gp.tileSize);
    image3 = setup("/objects/heart_blank", gp.tileSize, gp.tileSize);
  }

  @Override
  public boolean use(Entity entity) {
    gp.playSE(2);
    gp.ui.addMessage("Vida +" + getValue());
    gp.player.setLife(gp.player.getLife() + getValue()); // mantém comportamento original
    return true;
  }
}

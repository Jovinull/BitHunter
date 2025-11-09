package object;

import entity.Entity;
import main.GamePanel;

public class OBJ_Tent extends Entity {

  public static final String objName = "Tenda";

  public OBJ_Tent(GamePanel gp) {
    super(gp);

    setType(TYPE_CONSUMABLE);
    setName(objName);

    down1 = setup("/objects/tent", gp.tileSize, gp.tileSize);

    setDescription("[" + getName() + "]\nVocê pode dormir até \na manhã seguinte.");
    setPrice(300);
    setStackable(true);
  }

  @Override
  public boolean use(Entity entity) {
    gp.gameState = gp.sleepState;
    gp.playSE(14);

    gp.player.setLife(gp.player.getMaxLife());
    gp.player.setMana(gp.player.getMaxMana());

    // mantém o comportamento existente
    gp.player.getSleepingImage(down1);
    return true;
  }
}

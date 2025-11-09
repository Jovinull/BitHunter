package object;

import entity.Entity;
import main.GamePanel;

public class ObjManaCrystal extends Entity {

  public static final String OBJ_NAME = "Cristal de Mana";

  public ObjManaCrystal(GamePanel gp) {
    super(gp);

    setType(TYPE_PICKUP_ONLY);
    setName(OBJ_NAME);
    setValue(1);

    down1 = setup("/objects/manacrystal_full", gp.tileSize, gp.tileSize);
    image = setup("/objects/manacrystal_full", gp.tileSize, gp.tileSize);
    image2 = setup("/objects/manacrystal_blank", gp.tileSize, gp.tileSize);
  }

  @Override
  public boolean use(Entity entity) {
    gp.playSE(2);
    gp.ui.addMessage("Mana +" + getValue());
    gp.player.setMana(gp.player.getMana() + getValue());
    return true;
  }
}

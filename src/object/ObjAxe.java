package object;

import java.awt.Rectangle;

import entity.Entity;
import main.GamePanel;

public class ObjAxe extends Entity {

  public static final String OBJ_ID = "item.axe";

  public ObjAxe(GamePanel gp) {
    super(gp);

    setType(TYPE_AXE);
    setInternalId(OBJ_ID);
    setTranslationKey(OBJ_ID);

    // sprite
    down1 = setup("/objects/axe", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);

    // atributos de combate
    Rectangle ar = getAttackArea();
    ar.width = 30;
    ar.height = 30;

    setPrice(75);
    setKnockBackPower(10);

    setMotion1Duration(20);
    setMotion2Duration(40);
  }
}

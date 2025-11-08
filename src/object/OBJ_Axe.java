package object;

import java.awt.Rectangle;

import entity.Entity;
import main.GamePanel;

public class OBJ_Axe extends Entity {

  public static final String objName = "Machado de Lenhador";

  public OBJ_Axe(GamePanel gp) {
    super(gp);

    setType(TYPE_AXE);
    setName(objName);

    // sprite
    down1 = setup("/objects/axe", gp.tileSize, gp.tileSize);

    // atributos de combate
    setAttackValue(2);
    Rectangle ar = getAttackArea();
    ar.width = 30;
    ar.height = 30;

    setDescription("[" + getName() + "]\nUm pouco enferrujado, \nmas ainda corta algumas \nárvores.");
    setPrice(75);
    setKnockBackPower(10);

    setMotion1Duration(20);
    setMotion2Duration(40);
  }
}

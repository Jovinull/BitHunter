package object;

import java.awt.Rectangle;

import entity.Entity;
import main.GamePanel;

public class ObjAxe extends Entity {

  public static final String OBJ_NAME = "Machado de Lenhador";

  public ObjAxe(GamePanel gp) {
    super(gp);

    setType(TYPE_AXE);
    setName(OBJ_NAME);

    // sprite
    down1 = setup("/objects/axe", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);

    // atributos de combate
    Rectangle ar = getAttackArea();
    ar.width = 30;
    ar.height = 30;

    // descrição com i18n
    String displayName = t("item.axe.displayName");
    String description = "[" + displayName + "]\n" + t("item.axe.desc");
    setDescription(description);

    setPrice(75);
    setKnockBackPower(10);

    setMotion1Duration(20);
    setMotion2Duration(40);
  }

  private String t(String key) {
    return gp.getLanguageManager().text(key);
  }
}

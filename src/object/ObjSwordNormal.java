package object;

import entity.Entity;
import main.GamePanel;

public class ObjSwordNormal extends Entity {

  public static final String OBJ_NAME = "Espada Comum";

  public ObjSwordNormal(GamePanel gp) {
    super(gp);

    setType(TYPE_SWORD);
    setName(OBJ_NAME);

    down1 = setup("/objects/sword_normal", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);

    setAttackValue(1);
    getAttackArea().width = 36;
    getAttackArea().height = 36;

    String displayName = t("item.swordNormal.displayName");
    String description = "[" + displayName + "]\n" + t("item.swordNormal.desc");
    setDescription(description);

    setPrice(20);
    setKnockBackPower(2);

    setMotion1Duration(5);
    setMotion2Duration(25);
  }

  private String t(String key) {
    return gp.getLanguageManager().text(key);
  }
}

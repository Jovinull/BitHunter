package object;

import entity.Entity;
import main.GamePanel;

public class ObjPickaxe extends Entity {

  public static final String OBJ_NAME = "Picareta";

  public ObjPickaxe(GamePanel gp) {
    super(gp);

    setType(TYPE_PICKAXE);
    setName(OBJ_NAME);

    down1 = setup("/objects/pickaxe", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);

    setAttackValue(2);
    getAttackArea().width = 30;
    getAttackArea().height = 30;

    String displayName = t("item.pickaxe.displayName");
    String description = "[" + displayName + "]\n" + t("item.pickaxe.desc");
    setDescription(description);

    setPrice(75);
    setKnockBackPower(10);

    setMotion1Duration(10);
    setMotion2Duration(20);
  }

  private String t(String key) {
    return gp.getLanguageManager().text(key);
  }
}

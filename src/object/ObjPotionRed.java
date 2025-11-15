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

    down1 = setup("/objects/potion_red", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);

    String displayName = t("item.potionRed.displayName");
    String description = "[" + displayName + "]\n" + String.format(t("item.potionRed.desc"), getValue());
    setDescription(description);

    setPrice(25);
    setStackable(true);

    setDialogue();
  }

  public void setDialogue() {
    String[][] d = getDialogues();
    String displayName = t("item.potionRed.displayName");
    d[0][0] = String.format(t("item.potionRed.dialog.use"), displayName, getValue());
  }

  @Override
  public boolean use(Entity entity) {
    startDialogue(this, 0);
    gp.playSE(2);
    return true;
  }

  private String t(String key) {
    return gp.getLanguageManager().text(key);
  }
}

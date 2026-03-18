package object;

import entity.Entity;
import main.GamePanel;

public final class ObjPotionRed extends Entity {

  public static final String OBJ_ID = "item.potionRed";

  public ObjPotionRed(GamePanel gp) {
    super(gp);

    setType(TYPE_CONSUMABLE);
    setInternalId(OBJ_ID);
    setTranslationKey(OBJ_ID);
    setValue(5);

    down1 = setup("/objects/potion_red", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);

    setPrice(25);
    setStackable(true);

    setDialogue();
  }

  public void setDialogue() {
    String[][] d = getDialogues();
    String displayName = getDisplayName();
    d[0][0] = String.format(t("item.potionRed.dialog.use"), displayName, getValue());
  }

  @Override
  public boolean use(Entity entity) {
    startDialogue(this, 0);
    gp.playSE(2);
    return true;
  }

  @Override
  public String getDisplayDescription() {
    String key = getTranslationKey();
    if (key == null || key.isEmpty()) {
      return super.getDisplayDescription();
    }

    String pattern = t(key + ".desc");
    try {
      return String.format(pattern, getValue());
    } catch (IllegalArgumentException ex) {
      return pattern;
    }
  }
}

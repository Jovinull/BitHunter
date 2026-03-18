package object;

import entity.Entity;
import main.GamePanel;

public final class ObjClock extends Entity {

  public static final String OBJ_ID = "item.clock";

  public ObjClock(GamePanel gp) {
    super(gp);

    setType(TYPE_CONSUMABLE);
    setInternalId(OBJ_ID);
    setTranslationKey(OBJ_ID);
    setValue(0); // Não curador

    down1 = setup("/objects/clock", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);

    setPrice(80);
    setStackable(false); // Relógio não precisa ser stackable

    setDialogue();
  }

  public void setDialogue() {
    String[][] d = getDialogues();
    d[0][0] = t("item.clock.dialog.use");
  }

  @Override
  public boolean use(Entity entity) {
    startDialogue(this, 0);
    gp.playSE(9); // Som de interface/menu em vez de poção

    // Toggle the window
    gp.getGameUi().setClockWindowOpen(!gp.getGameUi().isClockWindowOpen());

    // Retornamos false no consumo para que o item NÃO seja removido do inventário
    return false;
  }
}

package object;

import entity.Entity;
import main.GamePanel;

public class ObjKey extends Entity {

  public static final String OBJ_ID = "item.key";

  public ObjKey(GamePanel gp) {
    super(gp);

    setType(TYPE_CONSUMABLE);
    setInternalId(OBJ_ID);
    setTranslationKey(OBJ_ID);

    down1 = setup("/objects/key", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);

    setPrice(100);
    setStackable(true);

    setDialogue();
  }

  public void setDialogue() {
    String[][] d = getDialogues();
    String displayName = getDisplayName();

    d[0][0] = String.format(t("item.key.dialog.use"), displayName);
    d[1][0] = t("item.key.dialog.nothing");
  }

  @Override
  public boolean use(Entity entity) {
    Entity[][] objects = gp.getObj();
    int currentMapIndex = gp.getCurrentMap();

    // Agora usando OBJ_ID da porta (e getDetected por internalId)
    int objIndex = getDetected(entity, objects, ObjDoor.OBJ_ID);

    if (objIndex != 999) {
      startDialogue(this, 0);
      gp.playSE(3);
      objects[currentMapIndex][objIndex] = null; // remove a porta
      return true;
    }

    startDialogue(this, 1);
    return false;
  }

}

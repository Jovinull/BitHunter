package object;

import entity.Entity;
import main.GamePanel;

public class ObjKey extends Entity {

  public static final String OBJ_NAME = "Chave";

  public ObjKey(GamePanel gp) {
    super(gp);

    setType(TYPE_CONSUMABLE);
    setName(OBJ_NAME);
    down1 = setup("/objects/key", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);

    String displayName = t("item.key.displayName");
    String description = "[" + displayName + "]\n" + t("item.key.desc");
    setDescription(description);

    setPrice(100);
    setStackable(true);

    setDialogue();
  }

  public void setDialogue() {
    String[][] d = getDialogues();
    d[0][0] = String.format(t("item.key.dialog.use"), getName());
    d[1][0] = t("item.key.dialog.nothing");
  }

  @Override
  public boolean use(Entity entity) {
    Entity[][] objects = gp.getObj();
    int currentMapIndex = gp.getCurrentMap();

    // procura por uma porta à frente do usuário
    int objIndex = getDetected(entity, objects, ObjDoor.OBJ_NAME);

    if (objIndex != 999) {
      startDialogue(this, 0);
      gp.playSE(3);
      objects[currentMapIndex][objIndex] = null; // remove a porta
      return true;
    }

    startDialogue(this, 1);
    return false;
  }

  private String t(String key) {
    return gp.getLanguageManager().text(key);
  }
}

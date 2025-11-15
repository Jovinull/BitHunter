package object;

import java.awt.Rectangle;

import entity.Entity;
import main.GamePanel;

public class ObjChest extends Entity {

  public static final String OBJ_NAME = "Baú";

  public ObjChest(GamePanel gp) {
    super(gp);

    setType(TYPE_OBSTACLE);
    setName(OBJ_NAME);

    // sprites (protegidos em Entity)
    image = setup("/objects/chest", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);
    image2 = setup("/objects/chest_opened", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);
    down1 = image;

    setCollision(true);

    // solidArea via getter + defaults via setters
    Rectangle sa = getSolidArea();
    sa.x = 4;
    sa.y = 16;
    sa.width = 40;
    sa.height = 32;
    setSolidAreaDefaultX(sa.x);
    setSolidAreaDefaultY(sa.y);
  }

  @Override
  public void setLoot(Entity loot) {
    super.setLoot(loot);
    setDialogue();
  }

  public void setDialogue() {
    String lootName = (getLoot() != null) ? getLoot().getName() : "???";
    String[][] d = getDialogues();

    d[0][0] = String.format(t("item.chest.dialog.fullInventory.line1"), lootName)
        + "\n"
        + t("item.chest.dialog.fullInventory.line2");

    d[1][0] = String.format(t("item.chest.dialog.obtained.line1"), lootName)
        + "\n"
        + String.format(t("item.chest.dialog.obtained.line2"), lootName);

    d[2][0] = t("item.chest.dialog.empty");
  }

  @Override
  public void interact() {
    if (!isOpened()) {
      gp.playSE(3);

      var player = gp.getPlayer();

      if (!player.canObtainItem(getLoot())) {
        startDialogue(this, 0);
      } else {
        startDialogue(this, 1);
        // aplica aparência aberta
        down1 = image2; // campos protegidos em Entity
        setOpened(true);
      }
    } else {
      startDialogue(this, 2);
    }
  }

  private String t(String key) {
    return gp.getLanguageManager().text(key);
  }
}

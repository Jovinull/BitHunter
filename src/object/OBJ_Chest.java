package object;

import java.awt.Rectangle;

import entity.Entity;
import main.GamePanel;

public class OBJ_Chest extends Entity {

  public static final String objName = "Baú";

  public OBJ_Chest(GamePanel gp) {
    super(gp);

    setType(TYPE_OBSTACLE);
    setName(objName);

    // sprites (protegidos em Entity)
    image = setup("/objects/chest", gp.tileSize, gp.tileSize);
    image2 = setup("/objects/chest_opened", gp.tileSize, gp.tileSize);
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

    d[0][0] = "Você abre o baú e encontra um " + lootName + "!" +
        "\n...Mas você não pode carregar mais nada!";
    d[1][0] = "Você abre o baú e encontra um " + lootName + "!" +
        "\nVocê obteve " + lootName + "!";
    d[2][0] = "Está vazio.";
  }

  @Override
  public void interact() {
    if (!isOpened()) {
      gp.playSE(3);

      if (!gp.player.canObtainItem(getLoot())) {
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
}

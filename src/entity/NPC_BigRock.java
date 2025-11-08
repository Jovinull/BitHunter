package entity;

import java.awt.Rectangle;
import java.util.ArrayList;

import main.GamePanel;
import object.OBJ_Door_Iron;
import tile_interactive.IT_MetalPlate;
import tile_interactive.InteractiveTile;

public class NPC_BigRock extends Entity {

  public static final String npcName = "Pedra Grande";

  public NPC_BigRock(GamePanel gp) {
    super(gp);

    setName(npcName);
    setDirection(DIR_DOWN);
    setSpeed(4);

    // solidArea é final na Entity -> apenas configuramos
    Rectangle sa = getSolidArea();
    sa.x = 2;
    sa.y = 6;
    setSolidAreaDefaultX(sa.x);
    setSolidAreaDefaultY(sa.y);
    sa.width = 44;
    sa.height = 40;

    setDialogueSet(-1);

    getImage();
    setDialogue();
  }

  public void getImage() {
    up1 = setup("/npc/bigrock", gp.tileSize, gp.tileSize);
    up2 = setup("/npc/bigrock", gp.tileSize, gp.tileSize);
    down1 = setup("/npc/bigrock", gp.tileSize, gp.tileSize);
    down2 = setup("/npc/bigrock", gp.tileSize, gp.tileSize);
    left1 = setup("/npc/bigrock", gp.tileSize, gp.tileSize);
    left2 = setup("/npc/bigrock", gp.tileSize, gp.tileSize);
    right1 = setup("/npc/bigrock", gp.tileSize, gp.tileSize);
    right2 = setup("/npc/bigrock", gp.tileSize, gp.tileSize);
  }

  public void setDialogue() {
    String[][] d = getDialogues();
    d[0][0] = "Isso é uma pedra gigante.";
    d[0][1] = "Acho que sou capaz de empurrar isso...";
  }

  @Override
  public void setAction() {
    // Rock não tem IA ativa por frame aqui.
  }

  @Override
  public void update() {
    // Rock não tem atualização automática aqui.
  }

  @Override
  public void speak() {
    facePlayer();
    startDialogue(this, getDialogueSet());
    setDialogueSet(getDialogueSet() + 1);

    String[][] d = getDialogues();
    if (d[getDialogueSet()][0] == null) {
      setDialogueSet(getDialogueSet() - 1);
    }
  }

  @Override
  public void move(String d) {
    setDirection(d); // já vem como DIR_*
    checkCollision();

    if (!isCollisionOn()) {
      switch (getDirection()) {
        case DIR_UP:
          setWorldY(getWorldY() - getSpeed());
          break;
        case DIR_DOWN:
          setWorldY(getWorldY() + getSpeed());
          break;
        case DIR_LEFT:
          setWorldX(getWorldX() - getSpeed());
          break;
        case DIR_RIGHT:
          setWorldX(getWorldX() + getSpeed());
          break;
        default:
          break;
      }
    }
    detectPlate();
  }

  public void detectPlate() {
    ArrayList<InteractiveTile> plateList = new ArrayList<>();
    ArrayList<Entity> rockList = new ArrayList<>();

    // Create a plate list
    int iTileSlots = gp.iTile[gp.currentMap].length;
    for (int i = 0; i < iTileSlots; i++) {
      if (gp.iTile[gp.currentMap][i] != null &&
          gp.iTile[gp.currentMap][i].getName() != null &&
          IT_MetalPlate.itName.equals(gp.iTile[gp.currentMap][i].getName())) {
        plateList.add(gp.iTile[gp.currentMap][i]);
      }
    }

    // Create a rock list
    int npcSlots = gp.npc[gp.currentMap].length;
    for (int i = 0; i < npcSlots; i++) {
      if (gp.npc[gp.currentMap][i] != null &&
          npcName.equals(gp.npc[gp.currentMap][i].getName())) {
        rockList.add(gp.npc[gp.currentMap][i]);
      }
    }

    int count = 0;

    // Scan the plate list
    for (int i = 0; i < plateList.size(); i++) {
      int xDistance = Math.abs(getWorldX() - plateList.get(i).getWorldX());
      int yDistance = Math.abs(getWorldY() - plateList.get(i).getWorldY());
      int distance = Math.max(xDistance, yDistance);

      if (distance < 8) {
        if (getLinkedEntity() == null) {
          setLinkedEntity(plateList.get(i));
          gp.playSE(3);
        }
      } else if (getLinkedEntity() == plateList.get(i)) {
        setLinkedEntity(null);
      }
    }

    // Count rocks on plates
    for (int i = 0; i < rockList.size(); i++) {
      if (rockList.get(i).getLinkedEntity() != null)
        count++;
    }

    // If all rocks are on plates, open iron door
    int objSlots = gp.obj[gp.currentMap].length;
    if (count == rockList.size()) {
      for (int i = 0; i < objSlots; i++) {
        if (gp.obj[gp.currentMap][i] != null &&
            OBJ_Door_Iron.objName.equals(gp.obj[gp.currentMap][i].getName())) {
          gp.obj[gp.currentMap][i] = null;
          gp.playSE(21);
        }
      }
    }
  }
}

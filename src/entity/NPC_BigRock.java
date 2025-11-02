package entity;

import java.awt.Rectangle;
import java.util.Random;

import main.GamePanel;

public class NPC_BigRock extends Entity {

  public static final String npcName = "Pedra Grande";

  public NPC_BigRock(GamePanel gp) {
    super(gp);

    name = npcName;
    direction = "down";
    speed = 4;

    solidArea = new Rectangle();
    solidArea.x = 2;
    solidArea.y = 6;
    solidAreaDefaultX = solidArea.x;
    solidAreaDefaultY = solidArea.y;
    solidArea.width = 44;
    solidArea.height = 40;

    dialogueSet = -1;

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
    dialogues[0][0] = "Isso é uma pedra gigante.";
    dialogues[0][1] = "Acho que sou capaz de empurrar isso...";
  }

  @Override
  public void setAction() {
  }

  public void update() {
  }

  public void speak() {

    // Do this character specific stuff
    facePlayer();
    startDialogue(this, dialogueSet);

    dialogueSet++;

    if (dialogues[dialogueSet][0] == null) {
      dialogueSet--;
    }
  }

  public void move(String d) {

    this.direction = d;

    checkCollision();

    if (collisionOn == false) {

      switch (direction) {
        case "up":
          worldY -= speed;
          break;
        case "down":
          worldY += speed;
          break;
        case "left":
          worldX -= speed;
          break;
        case "right":
          worldX += speed;
          break;
      }
    }
  }
}

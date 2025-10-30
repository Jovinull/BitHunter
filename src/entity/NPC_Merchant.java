package entity;

import java.awt.Rectangle;

import main.GamePanel;
import object.OBJ_Axe;
import object.OBJ_Key;
import object.OBJ_Lantern;
import object.OBJ_Potion_Red;
import object.OBJ_Shield_Blue;
import object.OBJ_Shield_Wood;
import object.OBJ_Sword_Normal;

public class NPC_Merchant extends Entity {

  public NPC_Merchant(GamePanel gp) {
    super(gp);

    direction = "down";
    speed = 1;

    solidArea = new Rectangle();
    solidArea.x = 8;
    solidArea.y = 16;
    solidAreaDefaultX = solidArea.x;
    solidAreaDefaultY = solidArea.y;
    solidArea.width = 32;
    solidArea.height = 32;

    getImage();
    setDialogue();
    setItems();
  }

  public void getImage() {
    up1 = setup("/npc/merchant/merchant_down_1", gp.tileSize, gp.tileSize);
    up2 = setup("/npc/merchant/merchant_down_2", gp.tileSize, gp.tileSize);
    down1 = setup("/npc/merchant/merchant_down_1", gp.tileSize, gp.tileSize);
    down2 = setup("/npc/merchant/merchant_down_2", gp.tileSize, gp.tileSize);
    left1 = setup("/npc/merchant/merchant_down_1", gp.tileSize, gp.tileSize);
    left2 = setup("/npc/merchant/merchant_down_2", gp.tileSize, gp.tileSize);
    right1 = setup("/npc/merchant/merchant_down_1", gp.tileSize, gp.tileSize);
    right2 = setup("/npc/merchant/merchant_down_2", gp.tileSize, gp.tileSize);
  }

  public void setDialogue() {
    dialogues[0][0] = "Hehe, então me encontrou.\nTenho mercadorias de primeira.\nQuer fazer negócio?";
    dialogues[1][0] = "Volte sempre, hehe!";
    dialogues[2][0] = "Você precisa de mais moedas para comprar isso!";
    dialogues[3][0] = "Você não pode carregar mais itens!";
    dialogues[4][0] = "Você não pode vender um item equipado!";
  }

  public void setItems() {

    inventory.add(new OBJ_Potion_Red(gp));
    inventory.add(new OBJ_Key(gp));
    inventory.add(new OBJ_Sword_Normal(gp));
    inventory.add(new OBJ_Axe(gp));
    inventory.add(new OBJ_Shield_Wood(gp));
    inventory.add(new OBJ_Shield_Blue(gp));
    inventory.add(new OBJ_Lantern(gp));
  }

  public void speak() {

    facePlayer();
    gp.gameState = gp.tradeState;
    gp.ui.npc = this;
  }
}

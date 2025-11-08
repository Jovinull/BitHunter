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

    setDirection(DIR_DOWN);
    setSpeed(1);

    // solidArea é final -> apenas configurar via getter
    Rectangle sa = getSolidArea();
    sa.x = 8;
    sa.y = 16;
    setSolidAreaDefaultX(sa.x);
    setSolidAreaDefaultY(sa.y);
    sa.width = 32;
    sa.height = 32;

    getImage();
    setDialogue();
    setItems();
  }

  public void getImage() {
    // Sprites continuam protegidos na Entity, então pode usar direto
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
    String[][] d = getDialogues();
    d[0][0] = "Hehe, então me encontrou.\nTenho mercadorias de primeira.\nQuer fazer negócio?";
    d[1][0] = "Volte sempre, hehe!";
    d[2][0] = "Você precisa de mais moedas para comprar isso!";
    d[3][0] = "Você não pode carregar mais itens!";
    d[4][0] = "Você não pode vender um item equipado!";
  }

  public void setItems() {
    var inv = getInventory();
    inv.add(new OBJ_Potion_Red(gp));
    inv.add(new OBJ_Key(gp));
    inv.add(new OBJ_Sword_Normal(gp));
    inv.add(new OBJ_Axe(gp));
    inv.add(new OBJ_Shield_Wood(gp));
    inv.add(new OBJ_Shield_Blue(gp));
    inv.add(new OBJ_Lantern(gp));
  }

  @Override
  public void speak() {
    facePlayer();
    gp.gameState = gp.tradeState;
    gp.ui.npc = this;
  }
}

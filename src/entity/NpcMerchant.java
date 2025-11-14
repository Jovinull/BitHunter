package entity;

import java.awt.Rectangle;

import main.GamePanel;
import object.ObjAxe;
import object.ObjKey;
import object.ObjLantern;
import object.ObjPotionRed;
import object.ObjShieldBlue;
import object.ObjShieldWood;
import object.ObjSwordNormal;

public class NpcMerchant extends Entity {

  private static final String SPRITE_DOWN_1 = "/npc/merchant/merchant_down_1";
  private static final String SPRITE_DOWN_2 = "/npc/merchant/merchant_down_2";

  public NpcMerchant(GamePanel gp) {
    super(gp);

    setDirection(DIR_DOWN);
    setSpeed(1);

    // solidArea é final -> configurar via getter
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

  @Override
  public void getImage() {
    // O sprite do comerciante usa as variações "down" para todas as direções
    int tileSize = GamePanel.TILE_SIZE;
    up1 = setup(SPRITE_DOWN_1, tileSize, tileSize);
    up2 = setup(SPRITE_DOWN_2, tileSize, tileSize);
    down1 = setup(SPRITE_DOWN_1, tileSize, tileSize);
    down2 = setup(SPRITE_DOWN_2, tileSize, tileSize);
    left1 = setup(SPRITE_DOWN_1, tileSize, tileSize);
    left2 = setup(SPRITE_DOWN_2, tileSize, tileSize);
    right1 = setup(SPRITE_DOWN_1, tileSize, tileSize);
    right2 = setup(SPRITE_DOWN_2, tileSize, tileSize);
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
    inv.add(new ObjPotionRed(gp));
    inv.add(new ObjKey(gp));
    inv.add(new ObjSwordNormal(gp));
    inv.add(new ObjAxe(gp));
    inv.add(new ObjShieldWood(gp));
    inv.add(new ObjShieldBlue(gp));
    inv.add(new ObjLantern(gp));
  }

  @Override
  public void speak() {
    facePlayer();
    gp.setGameState(GamePanel.TRADE_STATE);
    gp.getGameUi().setNpc(this);
  }
}

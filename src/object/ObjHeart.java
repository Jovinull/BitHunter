package object;

import entity.Entity;
import main.GamePanel;

public class ObjHeart extends Entity {

  public static final String OBJ_NAME = "Coração";

  public ObjHeart(GamePanel gp) {
    super(gp);

    setType(TYPE_PICKUP_ONLY);
    setName(OBJ_NAME);
    setValue(2);

    down1 = setup("/objects/heart_full", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);
    image = setup("/objects/heart_full", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);
    image2 = setup("/objects/heart_half", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);
    image3 = setup("/objects/heart_blank", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);
  }

  @Override
  public boolean use(Entity entity) {
    gp.playSE(2);

    var ui = gp.getGameUi();
    var player = gp.getPlayer();

    ui.addMessage("Vida +" + getValue());
    player.setLife(player.getLife() + getValue());
    return true;
  }
}

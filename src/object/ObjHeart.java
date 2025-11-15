package object;

import entity.Entity;
import main.GamePanel;

public class ObjHeart extends Entity {

  public static final String OBJ_ID = "item.heart";

  public ObjHeart(GamePanel gp) {
    super(gp);

    setType(TYPE_PICKUP_ONLY);
    setInternalId(OBJ_ID);
    setTranslationKey(OBJ_ID);
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

    String msg = String.format(t("item.heart.pickup"), getValue());
    ui.addMessage(msg);

    player.setLife(player.getLife() + getValue());
    return true;
  }

  @Override
  public String getDisplayDescription() {
    String key = getTranslationKey();
    if (key == null || key.isEmpty()) {
      return super.getDisplayDescription();
    }

    String pattern = t(key + ".desc");
    try {
      return String.format(pattern, getValue());
    } catch (IllegalArgumentException ex) {
      // se a string no lang estiver sem %d, evita crash e mostra o padrão cru
      return pattern;
    }
  }
}

package object;

import entity.Entity;
import main.GamePanel;

public class ObjManaCrystal extends Entity {

  public static final String OBJ_NAME = "Cristal de Mana";

  public ObjManaCrystal(GamePanel gp) {
    super(gp);

    setType(TYPE_PICKUP_ONLY);
    setName(OBJ_NAME);
    setValue(1);

    down1 = setup("/objects/manacrystal_full", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);
    image = setup("/objects/manacrystal_full", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);
    image2 = setup("/objects/manacrystal_blank", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);

    String displayName = t("item.manaCrystal.displayName");
    String description = "[" + displayName + "]\n" + String.format(t("item.manaCrystal.desc"), getValue());
    setDescription(description);
  }

  @Override
  public boolean use(Entity entity) {
    gp.playSE(2);

    var ui = gp.getGameUi();
    var player = gp.getPlayer();

    String msg = String.format(t("item.manaCrystal.pickup"), getValue());
    ui.addMessage(msg);

    player.setMana(player.getMana() + getValue());
    return true;
  }

  private String t(String key) {
    return gp.getLanguageManager().text(key);
  }
}

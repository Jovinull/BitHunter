package object;

import entity.Entity;
import main.GamePanel;

public final class ObjManaCrystal extends Entity {

  public static final String OBJ_ID = "item.manaCrystal";

  public ObjManaCrystal(GamePanel gp) {
    super(gp);

    setType(TYPE_PICKUP_ONLY);
    setInternalId(OBJ_ID);
    setTranslationKey(OBJ_ID);
    setValue(1);

    // Sprite usado para o DROP/LOOT no chão (novo asset dedicado)
    down1 = setup("/objects/manacrystal", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);
    image = setup("/objects/manacrystal_full", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);
    image2 = setup("/objects/manacrystal_blank", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);
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
      return pattern;
    }
  }
}

package object;

import java.awt.Rectangle;

import entity.Entity;
import main.GamePanel;

/**
 * Baú secreto que requer moedas para ser aberto.
 * Usado para a aquisição in-game do Escudo do Compilador (Java).
 *
 * Ao interagir:
 * - Se o player não tem moedas suficientes → mensagem de bloqueio.
 * - Se tem moedas mas inventário cheio → mensagem de inventário cheio.
 * - Se tem moedas e espaço → cobra as moedas, entrega o loot e abre o baú.
 * - Se já aberto → mensagem de "vazio".
 */
public final class ObjChestSecret extends Entity {

  public static final String OBJ_ID = "item.chestSecret";

  /** Custo fixo em moedas para abrir este baú. */
  private static final int COIN_COST = ObjShieldJava.SECRET_CHEST_COIN_COST;

  public ObjChestSecret(GamePanel gp) {
    super(gp);

    setType(TYPE_OBSTACLE);
    setInternalId(OBJ_ID);
    setTranslationKey(OBJ_ID);

    // Reutiliza sprites do baú normal
    image = setup("/objects/chest", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);
    image2 = setup("/objects/chest_opened", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);
    down1 = image;

    setCollision(true);

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
    String lootName = (getLoot() != null) ? getLoot().getDisplayName() : "???";
    String[][] d = getDialogues();

    // [0] Sem moedas suficientes
    d[0][0] = String.format(t("item.chestSecret.dialog.locked"), COIN_COST);

    // [1] Inventário cheio
    d[1][0] = String.format(t("item.chest.dialog.fullInventory.line1"), lootName)
        + "\n"
        + t("item.chest.dialog.fullInventory.line2");

    // [2] Item obtido com sucesso
    d[2][0] = String.format(t("item.chest.dialog.obtained.line1"), lootName)
        + "\n"
        + String.format(t("item.chest.dialog.obtained.line2"), lootName);

    // [3] Baú já aberto
    d[3][0] = t("item.chest.dialog.empty");
  }

  @Override
  public void interact() {
    if (isOpened()) {
      startDialogue(this, 3);
      return;
    }

    var player = gp.getPlayer();

    // Verifica se o player tem moedas suficientes
    if (player.getCoin() < COIN_COST) {
      gp.playSE(3);
      startDialogue(this, 0);
      return;
    }

    gp.playSE(3);

    if (!player.canObtainItem(getLoot())) {
      startDialogue(this, 1);
    } else {
      // Cobra moedas, entrega loot e abre o baú
      player.setCoin(player.getCoin() - COIN_COST);
      startDialogue(this, 2);
      applyOpenedAppearance();
      setOpened(true);
    }
  }
}

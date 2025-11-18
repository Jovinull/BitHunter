package ui;

import java.awt.Graphics2D;

import entity.Entity;
import main.GamePanel;

class TradeUi {

  private final UI ui;
  private final InventoryUi inventoryUi;

  TradeUi(UI ui, InventoryUi inventoryUi) {
    this.ui = ui;
    this.inventoryUi = inventoryUi;
  }

  void drawTradeScreen() {
    switch (ui.getSubState()) {
      case 0 -> tradeSelect();
      case 1 -> tradeBuy();
      case 2 -> tradeSell();
      default -> handleUnknownSubState();
    }
    ui.getKeyHandler().setEnterPressed(false);
  }

  /**
   * Trata estados de subState não mapeados na tela de trade.
   * Usado como fallback para evitar estados inválidos silenciosos.
   */
  private void handleUnknownSubState() {
    // Fallback seguro: volta para o menu de seleção de trade
    ui.setSubState(0);
  }

  private void tradeSelect() {
    Graphics2D g2 = ui.g2;
    GamePanel gp = ui.getGamePanel();
    Entity npc = ui.getNpc();
    if (npc == null) {
      return;
    }

    npc.setDialogueSet(0);
    ui.getDialogueUi().drawDialogueScreen();

    int x = GamePanel.TILE_SIZE * 15;
    int y = GamePanel.TILE_SIZE * 4;
    int width = GamePanel.TILE_SIZE * 3;
    int height = (int) (GamePanel.TILE_SIZE * 3.5);
    ui.drawSubWindow(x, y, width, height);

    x += GamePanel.TILE_SIZE;
    y += GamePanel.TILE_SIZE;

    var keyH = gp.getKeyH();

    g2.drawString(ui.t("ui.trade.buy"), x, y);
    if (ui.commandNum == 0) {
      g2.drawString(">", x - 24, y);
      if (keyH.isEnterPressed()) {
        ui.setSubState(1);
      }
    }
    y += GamePanel.TILE_SIZE;

    g2.drawString(ui.t("ui.trade.sell"), x, y);
    if (ui.commandNum == 1) {
      g2.drawString(">", x - 24, y);
      if (keyH.isEnterPressed()) {
        ui.setSubState(2);
      }
    }
    y += GamePanel.TILE_SIZE;

    g2.drawString(ui.t("ui.trade.leave"), x, y);
    if (ui.commandNum == 2) {
      g2.drawString(">", x - 24, y);
      if (keyH.isEnterPressed()) {
        ui.commandNum = 0;
        npc.startDialogue(npc, 1);
      }
    }
  }

  private void tradeBuy() {
    Graphics2D g2 = ui.g2;
    GamePanel gp = ui.getGamePanel();
    Entity npc = ui.getNpc();
    if (npc == null) {
      return;
    }

    var player = gp.getPlayer();

    inventoryUi.drawInventory(player, false);
    inventoryUi.drawInventory(npc, true);

    int x = GamePanel.TILE_SIZE * 2;
    int y = GamePanel.TILE_SIZE * 9;
    int width = GamePanel.TILE_SIZE * 6;
    int height = GamePanel.TILE_SIZE * 2;
    ui.drawSubWindow(x, y, width, height);
    g2.drawString(ui.t(UI.KEY_TRADE_HINT_BACK), x + 24, y + 60);

    x = GamePanel.TILE_SIZE * 12;
    y = GamePanel.TILE_SIZE * 9;
    width = GamePanel.TILE_SIZE * 6;
    height = GamePanel.TILE_SIZE * 2;
    ui.drawSubWindow(x, y, width, height);
    g2.drawString(ui.t("ui.trade.yourCoins") + " " + player.getCoin(), x + 24, y + 60);

    int itemIndex = ui.getItemIndexOnSlot(ui.getNpcSlotCol(), ui.getNpcSlotRow());
    if (itemIndex < npc.getInventory().size()) {
      x = (int) (GamePanel.TILE_SIZE * 5.5);
      y = (int) (GamePanel.TILE_SIZE * 5.5);
      width = (int) (GamePanel.TILE_SIZE * 2.5);
      height = GamePanel.TILE_SIZE;
      ui.drawSubWindow(x, y, width, height);
      g2.drawImage(ui.getCoinImage(), x + 10, y + 8, 32, 32, null);

      int price = npc.getInventory().get(itemIndex).getPrice();
      String text = String.valueOf(price);
      x = ui.getXforAlignToRightText(text, GamePanel.TILE_SIZE * 8 - 20);
      g2.drawString(text, x, y + 34);

      if (gp.getKeyH().isEnterPressed()) {
        if (price > player.getCoin()) {
          ui.setSubState(0);
          npc.startDialogue(npc, 2);
        } else if (player.canObtainItem(npc.getInventory().get(itemIndex))) {
          player.setCoin(player.getCoin() - price);
        } else {
          ui.setSubState(0);
          npc.startDialogue(npc, 3);
        }
      }
    }
  }

  private void tradeSell() {
    Graphics2D g2 = ui.g2;
    GamePanel gp = ui.getGamePanel();
    var player = gp.getPlayer();

    inventoryUi.drawInventory(player, true);

    int x = GamePanel.TILE_SIZE * 2;
    int y = GamePanel.TILE_SIZE * 9;
    int width = GamePanel.TILE_SIZE * 6;
    int height = GamePanel.TILE_SIZE * 2;
    ui.drawSubWindow(x, y, width, height);
    g2.drawString(ui.t(UI.KEY_TRADE_HINT_BACK), x + 24, y + 60);

    x = GamePanel.TILE_SIZE * 12;
    y = GamePanel.TILE_SIZE * 9;
    width = GamePanel.TILE_SIZE * 6;
    height = GamePanel.TILE_SIZE * 2;
    ui.drawSubWindow(x, y, width, height);
    g2.drawString(ui.t("ui.trade.yourCoins") + " " + player.getCoin(), x + 24, y + 60);

    int itemIndex = ui.getItemIndexOnSlot(ui.getPlayerSlotCol(), ui.getPlayerSlotRow());
    if (itemIndex >= player.getInventory().size()) {
      return;
    }

    drawSellPriceWindow(player, itemIndex);

    if (!gp.getKeyH().isEnterPressed()) {
      return;
    }

    handleSell(player, itemIndex);
  }

  private void drawSellPriceWindow(Entity player, int itemIndex) {
    Graphics2D g2 = ui.g2;

    int x = (int) (GamePanel.TILE_SIZE * 15.5);
    int y = (int) (GamePanel.TILE_SIZE * 5.5);
    int width = (int) (GamePanel.TILE_SIZE * 2.5);
    int height = GamePanel.TILE_SIZE;
    ui.drawSubWindow(x, y, width, height);
    g2.drawImage(ui.getCoinImage(), x + 10, y + 8, 32, 32, null);

    int price = player.getInventory().get(itemIndex).getPrice() / 2;
    String text = String.valueOf(price);
    int textX = ui.getXforAlignToRightText(text, GamePanel.TILE_SIZE * 18 - 20);
    g2.drawString(text, textX, y + 34);
  }

  private void handleSell(Entity player, int itemIndex) {
    var item = player.getInventory().get(itemIndex);
    int price = item.getPrice() / 2;

    if (isEquippedItem(player, item)) {
      ui.commandNum = 0;
      ui.setSubState(0);
      Entity npc = ui.getNpc();
      if (npc != null) {
        npc.startDialogue(npc, 4);
      }
      return;
    }

    if (item.getAmount() > 1) {
      item.setAmount(item.getAmount() - 1);
    } else {
      player.getInventory().remove(itemIndex);
    }
    player.setCoin(player.getCoin() + price);
  }

  private boolean isEquippedItem(Entity player, Entity item) {
    return item == player.getCurrentWeapon()
        || item == player.getCurrentShield()
        || item == player.getCurrentBoots()
        || item == player.getCurrentHelmet()
        || item == player.getCurrentChest()
        || item == player.getCurrentLegs();
  }
}

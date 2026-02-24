package ui;

import java.awt.Graphics2D;

import entity.Entity;
import main.GamePanel;

class TradeUi {

  private static final int TRADE_SUBSTATE_SELECT = 0;
  private static final int TRADE_SUBSTATE_BUY = 1;
  private static final int TRADE_SUBSTATE_SELL = 2;

  private static final int TRADE_MENU_X_TILES = 15;
  private static final int TRADE_MENU_Y_TILES = 4;
  private static final int TRADE_MENU_WIDTH_TILES = 3;
  private static final double TRADE_MENU_HEIGHT_TILES = 3.5;
  private static final int CURSOR_X_OFFSET = 24;

  private static final int HINT_BOX_X_TILES = 2;
  private static final int HINT_BOX_Y_TILES = 9;
  private static final int HINT_BOX_WIDTH_TILES = 6;
  private static final int HINT_BOX_HEIGHT_TILES = 2;
  private static final int HINT_TEXT_X_OFFSET = 24;
  private static final int HINT_TEXT_Y_OFFSET = 60;

  private static final int COINS_BOX_X_TILES = 12;
  private static final int COINS_BOX_Y_TILES = 9;
  private static final int COINS_BOX_WIDTH_TILES = 6;
  private static final int COINS_BOX_HEIGHT_TILES = 2;

  private static final double PRICE_BOX_X_TILES = 5.5;
  private static final double PRICE_BOX_Y_TILES = 5.5;
  private static final double PRICE_BOX_WIDTH_TILES = 2.5;
  private static final int PRICE_BOX_HEIGHT_TILES = 1;
  private static final int PRICE_COIN_X_OFFSET = 10;
  private static final int PRICE_COIN_Y_OFFSET = 8;
  private static final int PRICE_COIN_SIZE = 32;
  private static final int PRICE_RIGHT_ALIGN_TILES = 8;
  private static final int PRICE_RIGHT_ALIGN_OFFSET = 20;
  private static final int PRICE_TEXT_Y_OFFSET = 34;

  private static final double SELL_PRICE_BOX_X_TILES = 15.5;
  private static final double SELL_PRICE_BOX_Y_TILES = 5.5;
  private static final double SELL_PRICE_BOX_WIDTH_TILES = 2.5;
  private static final int SELL_PRICE_BOX_HEIGHT_TILES = 1;
  private static final int SELL_PRICE_RIGHT_ALIGN_TILES = 18;
  private static final int SELL_PRICE_RIGHT_ALIGN_OFFSET = 20;

  private final UI ui;
  private final InventoryUi inventoryUi;

  TradeUi(UI ui, InventoryUi inventoryUi) {
    this.ui = ui;
    this.inventoryUi = inventoryUi;
  }

  void drawTradeScreen() {
    switch (ui.getSubState()) {
      case TRADE_SUBSTATE_SELECT -> tradeSelect();
      case TRADE_SUBSTATE_BUY -> tradeBuy();
      case TRADE_SUBSTATE_SELL -> tradeSell();
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
    ui.setSubState(TRADE_SUBSTATE_SELECT);
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

    int x = GamePanel.TILE_SIZE * TRADE_MENU_X_TILES;
    int y = GamePanel.TILE_SIZE * TRADE_MENU_Y_TILES;
    int width = GamePanel.TILE_SIZE * TRADE_MENU_WIDTH_TILES;
    int height = (int) (GamePanel.TILE_SIZE * TRADE_MENU_HEIGHT_TILES);
    ui.drawSubWindow(x, y, width, height);

    x += GamePanel.TILE_SIZE;
    y += GamePanel.TILE_SIZE;

    var keyH = gp.getKeyH();

    g2.drawString(ui.t("ui.trade.buy"), x, y);
    if (ui.commandNum == 0) {
      g2.drawString(">", x - CURSOR_X_OFFSET, y);
      if (keyH.isEnterPressed()) {
        ui.setSubState(TRADE_SUBSTATE_BUY);
      }
    }
    y += GamePanel.TILE_SIZE;

    g2.drawString(ui.t("ui.trade.sell"), x, y);
    if (ui.commandNum == 1) {
      g2.drawString(">", x - CURSOR_X_OFFSET, y);
      if (keyH.isEnterPressed()) {
        ui.setSubState(TRADE_SUBSTATE_SELL);
      }
    }
    y += GamePanel.TILE_SIZE;

    g2.drawString(ui.t("ui.trade.leave"), x, y);
    if (ui.commandNum == 2) {
      g2.drawString(">", x - CURSOR_X_OFFSET, y);
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

    int x = GamePanel.TILE_SIZE * HINT_BOX_X_TILES;
    int y = GamePanel.TILE_SIZE * HINT_BOX_Y_TILES;
    int width = GamePanel.TILE_SIZE * HINT_BOX_WIDTH_TILES;
    int height = GamePanel.TILE_SIZE * HINT_BOX_HEIGHT_TILES;
    ui.drawSubWindow(x, y, width, height);
    g2.drawString(ui.t(UI.KEY_TRADE_HINT_BACK), x + HINT_TEXT_X_OFFSET, y + HINT_TEXT_Y_OFFSET);

    x = GamePanel.TILE_SIZE * COINS_BOX_X_TILES;
    y = GamePanel.TILE_SIZE * COINS_BOX_Y_TILES;
    width = GamePanel.TILE_SIZE * COINS_BOX_WIDTH_TILES;
    height = GamePanel.TILE_SIZE * COINS_BOX_HEIGHT_TILES;
    ui.drawSubWindow(x, y, width, height);
    g2.drawString(ui.t("ui.trade.yourCoins") + " " + player.getCoin(), x + HINT_TEXT_X_OFFSET, y + HINT_TEXT_Y_OFFSET);

    int itemIndex = ui.getItemIndexOnSlot(ui.getNpcSlotCol(), ui.getNpcSlotRow());
    if (itemIndex < npc.getInventory().size()) {
      x = (int) (GamePanel.TILE_SIZE * PRICE_BOX_X_TILES);
      y = (int) (GamePanel.TILE_SIZE * PRICE_BOX_Y_TILES);
      width = (int) (GamePanel.TILE_SIZE * PRICE_BOX_WIDTH_TILES);
      height = GamePanel.TILE_SIZE * PRICE_BOX_HEIGHT_TILES;
      ui.drawSubWindow(x, y, width, height);
      g2.drawImage(
          ui.getCoinImage(), x + PRICE_COIN_X_OFFSET, y + PRICE_COIN_Y_OFFSET, PRICE_COIN_SIZE, PRICE_COIN_SIZE, null);

      int price = npc.getInventory().get(itemIndex).getPrice();
      String text = String.valueOf(price);
      x = ui.getXforAlignToRightText(text, GamePanel.TILE_SIZE * PRICE_RIGHT_ALIGN_TILES - PRICE_RIGHT_ALIGN_OFFSET);
      g2.drawString(text, x, y + PRICE_TEXT_Y_OFFSET);

      if (gp.getKeyH().isEnterPressed()) {
        if (price > player.getCoin()) {
          ui.setSubState(TRADE_SUBSTATE_SELECT);
          npc.startDialogue(npc, 2);
        } else if (player.canObtainItem(npc.getInventory().get(itemIndex))) {
          player.setCoin(player.getCoin() - price);
        } else {
          ui.setSubState(TRADE_SUBSTATE_SELECT);
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

    int x = GamePanel.TILE_SIZE * HINT_BOX_X_TILES;
    int y = GamePanel.TILE_SIZE * HINT_BOX_Y_TILES;
    int width = GamePanel.TILE_SIZE * HINT_BOX_WIDTH_TILES;
    int height = GamePanel.TILE_SIZE * HINT_BOX_HEIGHT_TILES;
    ui.drawSubWindow(x, y, width, height);
    g2.drawString(ui.t(UI.KEY_TRADE_HINT_BACK), x + HINT_TEXT_X_OFFSET, y + HINT_TEXT_Y_OFFSET);

    x = GamePanel.TILE_SIZE * COINS_BOX_X_TILES;
    y = GamePanel.TILE_SIZE * COINS_BOX_Y_TILES;
    width = GamePanel.TILE_SIZE * COINS_BOX_WIDTH_TILES;
    height = GamePanel.TILE_SIZE * COINS_BOX_HEIGHT_TILES;
    ui.drawSubWindow(x, y, width, height);
    g2.drawString(ui.t("ui.trade.yourCoins") + " " + player.getCoin(), x + HINT_TEXT_X_OFFSET, y + HINT_TEXT_Y_OFFSET);

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

    int x = (int) (GamePanel.TILE_SIZE * SELL_PRICE_BOX_X_TILES);
    int y = (int) (GamePanel.TILE_SIZE * SELL_PRICE_BOX_Y_TILES);
    int width = (int) (GamePanel.TILE_SIZE * SELL_PRICE_BOX_WIDTH_TILES);
    int height = GamePanel.TILE_SIZE * SELL_PRICE_BOX_HEIGHT_TILES;
    ui.drawSubWindow(x, y, width, height);
    g2.drawImage(
        ui.getCoinImage(), x + PRICE_COIN_X_OFFSET, y + PRICE_COIN_Y_OFFSET, PRICE_COIN_SIZE, PRICE_COIN_SIZE, null);

    int price = player.getInventory().get(itemIndex).getPrice() / 2;
    String text = String.valueOf(price);
    int textX = ui.getXforAlignToRightText(text,
        GamePanel.TILE_SIZE * SELL_PRICE_RIGHT_ALIGN_TILES - SELL_PRICE_RIGHT_ALIGN_OFFSET);
    g2.drawString(text, textX, y + PRICE_TEXT_Y_OFFSET);
  }

  private void handleSell(Entity player, int itemIndex) {
    var item = player.getInventory().get(itemIndex);
    int price = item.getPrice() / 2;

    if (isEquippedItem(player, item)) {
      ui.commandNum = 0;
      ui.setSubState(TRADE_SUBSTATE_SELECT);
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

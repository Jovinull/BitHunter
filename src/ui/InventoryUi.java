package ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

import entity.Entity;
import main.GamePanel;

class InventoryUi {

  private static final int INVENTORY_COLS = 5;
  private static final Color COL_AMOUNT_SHADOW = new Color(60, 60, 60);
  private static final int PLAYER_FRAME_X_TILES = 12;
  private static final int NPC_FRAME_X_TILES = 2;
  private static final int INVENTORY_FRAME_Y_TILES = 1;
  private static final int INVENTORY_FRAME_WIDTH_TILES = 6;
  private static final int INVENTORY_FRAME_HEIGHT_TILES = 5;
  private static final int INVENTORY_FRAME_HEIGHT_EXTRA = 20;
  private static final int TITLE_X_OFFSET = 20;
  private static final int TITLE_Y_OFFSET = 32;
  private static final int SLOT_START_Y_OFFSET = 16;
  private static final int SLOT_WIDTH_EXTRA = 3;
  private static final int AMOUNT_RIGHT_ALIGN_OFFSET = 44;
  private static final int DESCRIPTION_LINE_HEIGHT = 22;
  private static final int DESCRIPTION_FIRST_LINE_HEIGHT = 24;

  private final UI ui;

  InventoryUi(UI ui) {
    this.ui = ui;
  }

  void drawInventory(Entity entity, boolean cursor) {
    Graphics2D g2 = ui.g2;

    int frameX;
    int frameY;
    int frameWidth;
    int frameHeight;
    int slotCol;
    int slotRow;

    if (entity == ui.getGamePanel().getPlayer()) {
      frameX = GamePanel.TILE_SIZE * PLAYER_FRAME_X_TILES;
      frameY = GamePanel.TILE_SIZE * INVENTORY_FRAME_Y_TILES;
      frameWidth = GamePanel.TILE_SIZE * INVENTORY_FRAME_WIDTH_TILES;
      frameHeight = GamePanel.TILE_SIZE * INVENTORY_FRAME_HEIGHT_TILES + INVENTORY_FRAME_HEIGHT_EXTRA;
      slotCol = ui.getPlayerSlotCol();
      slotRow = ui.getPlayerSlotRow();
    } else {
      frameX = GamePanel.TILE_SIZE * NPC_FRAME_X_TILES;
      frameY = GamePanel.TILE_SIZE * INVENTORY_FRAME_Y_TILES;
      frameWidth = GamePanel.TILE_SIZE * INVENTORY_FRAME_WIDTH_TILES;
      frameHeight = GamePanel.TILE_SIZE * INVENTORY_FRAME_HEIGHT_TILES + INVENTORY_FRAME_HEIGHT_EXTRA;
      slotCol = ui.getNpcSlotCol();
      slotRow = ui.getNpcSlotRow();
    }

    ui.drawSubWindow(frameX, frameY, frameWidth, frameHeight);

    String title = (entity == ui.getGamePanel().getPlayer())
        ? ui.t("ui.inventory.playerTitle")
        : ui.t("ui.inventory.npcTitle");
    int titleY = frameY + TITLE_Y_OFFSET;

    g2.setFont(ui.fonts.pixelBold(22F));
    ui.drawTextShadow(title, frameX + TITLE_X_OFFSET, titleY, UI.COL_TEXT, Color.black, 2);

    final int slotXstart = frameX + TITLE_X_OFFSET;
    final int slotYstart = titleY + SLOT_START_Y_OFFSET;
    final int slotWidth = GamePanel.TILE_SIZE + SLOT_WIDTH_EXTRA;
    final int slotHeight = GamePanel.TILE_SIZE;

    drawInventoryItems(entity, slotXstart, slotYstart, slotWidth, slotHeight);

    if (cursor) {
      drawInventoryCursor(slotXstart, slotYstart, slotWidth, slotHeight, slotCol, slotRow);
      drawInventoryDescription(entity, frameX, frameY, frameWidth, frameHeight, slotCol, slotRow);
    }
  }

  private void drawInventoryItems(
      Entity entity,
      int slotXstart,
      int slotYstart,
      int slotWidth,
      int slotHeight) {

    Graphics2D g2 = ui.g2;
    int slotX = slotXstart;
    int slotY = slotYstart;

    for (int i = 0; i < entity.getInventory().size(); i++) {
      Entity item = entity.getInventory().get(i);

      if (item != null && isEquippedItem(entity, item)) {
        g2.setColor(UI.COL_ACCENT);
        g2.fillRoundRect(slotX, slotY, GamePanel.TILE_SIZE, GamePanel.TILE_SIZE, 8, 8);
      }

      if (item != null && item.getDown1() != null) {
        g2.drawImage(item.getDown1(), slotX, slotY, null);
      }

      if (item != null
          && entity == ui.getGamePanel().getPlayer()
          && item.getAmount() > 1) {

        g2.setFont(ui.fonts.pixelBold(32F));
        String s = String.valueOf(item.getAmount());
        int amountX = ui.getXforAlignToRightText(s, slotX + AMOUNT_RIGHT_ALIGN_OFFSET);
        int amountY = slotY + GamePanel.TILE_SIZE;

        g2.setColor(COL_AMOUNT_SHADOW);
        g2.drawString(s, amountX, amountY);
        g2.setColor(Color.white);
        g2.drawString(s, amountX - 3, amountY - 3);
      }

      slotX += slotWidth;

      if ((i + 1) % INVENTORY_COLS == 0) {
        slotX = slotXstart;
        slotY += slotHeight;
      }
    }
  }

  private boolean isEquippedItem(Entity entity, Entity item) {
    if (item == null) {
      return false;
    }

    return item == entity.getCurrentWeapon()
        || item == entity.getCurrentShield()
        || item == entity.getCurrentLight()
        || item == entity.getCurrentBoots()
        || item == entity.getCurrentHelmet()
        || item == entity.getCurrentChest()
        || item == entity.getCurrentLegs();
  }

  private void drawInventoryCursor(
      int slotXstart,
      int slotYstart,
      int slotWidth,
      int slotHeight,
      int slotCol,
      int slotRow) {

    Graphics2D g2 = ui.g2;

    int cursorX = slotXstart + (slotWidth * slotCol);
    int cursorY = slotYstart + (slotHeight * slotRow);
    int cursorWidth = GamePanel.TILE_SIZE;
    int cursorHeight = GamePanel.TILE_SIZE;

    g2.setColor(UI.COL_TEXT);
    g2.setStroke(new BasicStroke(3));
    g2.drawRoundRect(cursorX, cursorY, cursorWidth, cursorHeight, 8, 8);
  }

  private void drawInventoryDescription(
      Entity entity,
      int frameX,
      int frameY,
      int frameWidth,
      int frameHeight,
      int slotCol,
      int slotRow) {

    Graphics2D g2 = ui.g2;

    int dFrameX = frameX;
    int dFrameY = frameY + frameHeight;
    int dFrameWidth = frameWidth;
    // Na tela de personagem, alinha o fundo da descrição com o fundo do painel de status
    // (CharacterUi: y = TILE_SIZE, height = TILE_SIZE * 10 → bottom = TILE_SIZE * 11)
    int dFrameHeight;
    if (ui.getGamePanel().getGameState() == GamePanel.CHARACTER_STATE) {
      int statusBottom = GamePanel.TILE_SIZE + GamePanel.TILE_SIZE * 10;
      dFrameHeight = statusBottom - dFrameY;
    } else {
      dFrameHeight = GamePanel.TILE_SIZE * 3;
    }

    int itemIndex = ui.getItemIndexOnSlot(slotCol, slotRow);
    if (itemIndex < entity.getInventory().size()) {

      Entity item = entity.getInventory().get(itemIndex);
      if (item == null) {
        return;
      }

      ui.drawSubWindow(dFrameX, dFrameY, dFrameWidth, dFrameHeight);

      int textX = dFrameX + 20;
      int textY = dFrameY + GamePanel.TILE_SIZE;
      g2.setColor(UI.COL_TEXT);

      String displayName = item.getDisplayName();
      g2.setFont(ui.fonts.pixelBold(20F));
      g2.drawString("[" + displayName + "]", textX, textY);
      textY += DESCRIPTION_FIRST_LINE_HEIGHT;

      String desc = item.getDisplayDescription();
      g2.setFont(ui.fonts.pixelPlain(20F));

      if (desc != null && !desc.isEmpty()) {
        String[] lines = desc.split("\n");
        for (String line : lines) {
          g2.drawString(line, textX, textY);
          textY += DESCRIPTION_LINE_HEIGHT;
        }
      }
    }
  }
}

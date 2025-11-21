package ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;

import entity.Entity;
import entity.Player;
import main.GamePanel;
import pets.PetEgg;
import pets.PetFactory;
import pets.PetIncubator;

/**
 * Tela de gerenciamento de pets (Pet Yard).
 *
 * subState 0 -> menu principal (Gerenciar / Chocar / Sair)
 * subState 1 -> lista de pets possuídos para equipar
 * subState 2 -> seleção de ovo no inventário para chocar
 */
class PetYardUi {

  private final UI ui;
  private final InventoryUi inventoryUi;

  PetYardUi(UI ui, InventoryUi inventoryUi) {
    this.ui = ui;
    this.inventoryUi = inventoryUi;
  }

  void drawPetYardScreen() {
    switch (ui.getSubState()) {
      case 0 -> drawMainMenu();
      case 1 -> drawPetList();
      case 2 -> drawEggSelection();
      default -> ui.setSubState(0);
    }

    ui.getKeyHandler().setEnterPressed(false);
  }

  /**
   * Menu principal do Pet Yard, com as opções:
   * - Gerenciar pets
   * - Chocar ovo
   * - Sair
   *
   * Aqui deixamos a fonte exatamente como o Trade:
   * o UI já configurou o Graphics2D com MaruMonica 16F,
   * então NÃO sobrescrevemos o tamanho da fonte.
   */
  private void drawMainMenu() {
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
    int width = GamePanel.TILE_SIZE * 4;
    int height = GamePanel.TILE_SIZE * 4;
    ui.drawSubWindow(x, y, width, height);

    x += GamePanel.TILE_SIZE;
    y += GamePanel.TILE_SIZE;

    var keyH = gp.getKeyH();

    // 0 - Gerenciar pets
    g2.drawString(ui.t("ui.petYard.manage"), x, y);
    if (ui.getCommandNum() == 0) {
      g2.drawString(">", x - 24, y);
      if (keyH.isEnterPressed()) {
        ui.setSubState(1);
        ui.setCommandNum(0);
      }
    }
    y += GamePanel.TILE_SIZE;

    // 1 - Chocar ovo
    g2.drawString(ui.t("ui.petYard.hatch"), x, y);
    if (ui.getCommandNum() == 1) {
      g2.drawString(">", x - 24, y);
      if (keyH.isEnterPressed()) {
        ui.setSubState(2);
      }
    }
    y += GamePanel.TILE_SIZE;

    // 2 - Sair
    g2.drawString(ui.t("ui.petYard.leave"), x, y);
    if (ui.getCommandNum() == 2) {
      g2.drawString(">", x - 24, y);
      if (keyH.isEnterPressed()) {
        ui.setCommandNum(0);
        gp.setGameState(GamePanel.PLAY_STATE);
      }
    }
  }

  private void drawPetList() {
    Graphics2D g2 = ui.g2;
    GamePanel gp = ui.getGamePanel();
    Player player = gp.getPlayer();
    List<String> pets = player.getOwnedPets();

    int frameX = GamePanel.TILE_SIZE * 2;
    int frameY = GamePanel.TILE_SIZE;
    int frameWidth = GamePanel.TILE_SIZE * 16;
    int frameHeight = GamePanel.TILE_SIZE * 8;

    ui.drawSubWindow(frameX, frameY, frameWidth, frameHeight);

    // Fonte igual à descrição/título de item do trade
    Font baseFont = ui.getMaruMonica() != null ? ui.getMaruMonica() : g2.getFont();
    Font titleFont = baseFont.deriveFont(Font.PLAIN, 24F);
    Font descFont = baseFont.deriveFont(Font.PLAIN, 20F);
    Font tagFont = baseFont.deriveFont(Font.ITALIC, 16F);
    Font hintFont = baseFont.deriveFont(Font.PLAIN, 16F);

    int titleY = frameY + 32;
    g2.setFont(titleFont);
    ui.drawTextShadow(ui.t("ui.petYard.manageTitle"), frameX + 24, titleY, UI.COL_TEXT, Color.black, 2);

    int listStartY = titleY + 24;
    int lineHeight = GamePanel.TILE_SIZE + 4;

    if (pets.isEmpty()) {
      g2.setFont(descFont);
      g2.setColor(UI.COL_TEXT_DIM);
      g2.drawString(ui.t("ui.petYard.noPets"), frameX + 24, listStartY + 8);
      return;
    }

    String activePetId = player.getActivePetId();

    for (int i = 0; i < pets.size(); i++) {
      String petId = pets.get(i);
      int itemY = listStartY + i * lineHeight;

      boolean selected = (i == ui.getCommandNum());

      int rowX = frameX + 16;
      int rowW = frameWidth - 32;
      int rowH = GamePanel.TILE_SIZE;

      if (selected) {
        g2.setColor(UI.COL_HIGHLIGHT_BG);
        g2.fillRoundRect(rowX, itemY - GamePanel.TILE_SIZE + 4, rowW, rowH, 10, 10);
        g2.setColor(UI.COL_PANEL_BORDER);
        g2.drawRoundRect(rowX, itemY - GamePanel.TILE_SIZE + 4, rowW, rowH, 10, 10);
      }

      BufferedImage icon = PetFactory.getPetIcon(petId, gp, player);
      if (icon != null) {
        g2.drawImage(icon, rowX + 4, itemY - GamePanel.TILE_SIZE + 4, null);
      }

      String name = ui.t(petId + ".name");
      g2.setFont(titleFont);
      g2.setColor(selected ? UI.COL_ACCENT : UI.COL_TEXT);
      int textX = rowX + GamePanel.TILE_SIZE + 12;
      g2.drawString(name, textX, itemY);

      if (petId.equals(activePetId)) {
        g2.setColor(UI.COL_TEXT_DIM);
        g2.setFont(tagFont);
        g2.drawString(ui.t("ui.petYard.currentPetTag"), textX + 200, itemY);
      }
    }

    // descrição do pet selecionado (subida 1 tile pra não pegar o hint)
    int descX = frameX + 24;
    int descY = frameY + frameHeight - GamePanel.TILE_SIZE * 2;
    int descWidth = frameWidth - 48;
    int descHeight = (int) (GamePanel.TILE_SIZE * 1.5);
    ui.drawSubWindow(descX - 12, descY - GamePanel.TILE_SIZE / 2, descWidth + 24, descHeight);

    if (ui.getCommandNum() >= 0 && ui.getCommandNum() < pets.size()) {
      String petId = pets.get(ui.getCommandNum());
      String desc = ui.t(petId + ".desc");

      g2.setFont(descFont);
      g2.setColor(UI.COL_TEXT);

      int textY = descY;
      for (String line : desc.split("\n")) {
        g2.drawString(line, descX, textY);
        textY += 22;
      }
    }

    // dica
    int hintY = frameY + frameHeight - 16;
    g2.setFont(hintFont);
    g2.setColor(UI.COL_TEXT_DIM);
    g2.drawString(ui.t("ui.petYard.hintEquip"), frameX + 24, hintY);

    // ENTER => equipa pet
    var keyH = gp.getKeyH();
    if (keyH.isEnterPressed() && !pets.isEmpty()) {
      int index = ui.getCommandNum();
      if (index < 0 || index >= pets.size()) {
        return;
      }

      String chosenId = pets.get(index);
      boolean equipped = player.equipPetById(chosenId);
      if (equipped) {
        String petName = ui.t(chosenId + ".name");
        String template = ui.t("ui.petYard.petEquipped");
        String msg = String.format(template, petName);
        gp.getGameUi().addMessage(msg);
      }
    }
  }

  private void drawEggSelection() {
    Graphics2D g2 = ui.g2;
    GamePanel gp = ui.getGamePanel();
    Player player = gp.getPlayer();

    // Fonte igual à descrição/título de item do trade
    Font baseFont = ui.getMaruMonica() != null ? ui.getMaruMonica() : g2.getFont();
    Font titleFont = baseFont.deriveFont(Font.BOLD, 20F);
    Font descFont = baseFont.deriveFont(Font.PLAIN, 20F);

    // inventário do player com cursor
    inventoryUi.drawInventory(player, true);

    int infoX = GamePanel.TILE_SIZE * 2;
    int infoY = GamePanel.TILE_SIZE * 9;
    int infoWidth = GamePanel.TILE_SIZE * 16;
    int infoHeight = GamePanel.TILE_SIZE * 2 + 8;
    ui.drawSubWindow(infoX, infoY, infoWidth, infoHeight);

    // hint
    g2.setFont(descFont);
    g2.setColor(UI.COL_TEXT);
    g2.drawString(ui.t("ui.petYard.eggHint"), infoX + 24, infoY + 32);

    int itemIndex = ui.getItemIndexOnSlot(ui.getPlayerSlotCol(), ui.getPlayerSlotRow());
    if (itemIndex >= player.getInventory().size()) {
      return;
    }

    Entity item = player.getInventory().get(itemIndex);
    if (!(item instanceof PetEgg egg)) {
      g2.setColor(UI.COL_TEXT_DIM);
      g2.setFont(descFont);
      g2.drawString(ui.t("ui.petYard.eggInvalid"), infoX + 24, infoY + 58);
      return;
    }

    // detalhes do ovo válido selecionado
    String eggName = egg.getDisplayName();
    int cost = egg.getHatchCost();
    int seconds = egg.getHatchTimeTicks() / 60;

    String lineCost = String.format(ui.t("ui.petYard.eggCost"), cost);
    String lineTime = String.format(ui.t("ui.petYard.eggTime"), seconds);

    g2.setColor(UI.COL_TEXT);

    // nome do ovo como "título" (igual nome do item no trade)
    g2.setFont(titleFont);
    g2.drawString(eggName, infoX + 24, infoY + 58);

    // custo + tempo como "descrição"
    g2.setFont(descFont);
    g2.drawString(lineCost, infoX + 24, infoY + 82);
    g2.drawString(lineTime, infoX + 220, infoY + 82);

    var keyH = gp.getKeyH();
    if (keyH.isEnterPressed()) {
      tryStartIncubation(player, egg, itemIndex);
    }
  }

  private void tryStartIncubation(Player player, PetEgg egg, int itemIndex) {
    GamePanel gp = ui.getGamePanel();
    PetIncubator incubator = gp.getPetIncubator();

    if (incubator.hasActiveEgg()) {
      gp.getGameUi().addMessage(ui.t("ui.petYard.alreadyIncubating"));
      return;
    }

    int cost = egg.getHatchCost();
    if (player.getCoin() < cost) {
      gp.getGameUi().addMessage(ui.t("ui.petYard.notEnoughCoins"));
      return;
    }

    // paga o ovo
    player.setCoin(player.getCoin() - cost);

    // remove do inventário
    var inv = player.getInventory();
    if (egg.getAmount() > 1) {
      egg.setAmount(egg.getAmount() - 1);
    } else {
      inv.remove(itemIndex);
    }

    incubator.startIncubation(egg);

    String msgTemplate = ui.t("ui.petYard.hatchStarted");
    String msg = String.format(msgTemplate, egg.getDisplayName());
    gp.getGameUi().addMessage(msg);
  }
}

package ui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;

import entity.Entity;
import entity.Player;
import main.GamePanel;
import pets.PetEgg;
import pets.PetEntity;
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

  private static final String PET_NAME_TRANSLATION_SUFFIX = ".name";
  private static final Color COL_BAR_BG = new Color(0, 0, 0, 160);
  private static final int CURSOR_X_OFFSET = 24;
  private static final int MAIN_MENU_X_TILES = 15;
  private static final int MAIN_MENU_Y_TILES = 4;
  private static final int MAIN_MENU_WIDTH_TILES = 4;
  private static final int MAIN_MENU_HEIGHT_TILES = 4;
  private static final int INCUBATOR_PANEL_X_TILES = 3;
  private static final int INCUBATOR_PANEL_Y_TILES = 7;
  private static final int INCUBATOR_PANEL_WIDTH_TILES = 6;
  private static final int INCUBATOR_PANEL_HEIGHT_TILES = 3;
  private static final int PET_LIST_FRAME_X_TILES = 2;
  private static final int PET_LIST_FRAME_Y_TILES = 1;
  private static final int PET_LIST_FRAME_WIDTH_TILES = 16;
  private static final int PET_LIST_FRAME_HEIGHT_TILES = 8;
  private static final int TITLE_Y_OFFSET = 32;
  private static final int LIST_AREA_X_OFFSET = 16;
  private static final int LIST_AREA_WIDTH_OFFSET = 24;
  private static final int ROW_LINE_EXTRA_SPACING = 8;
  private static final int ROW_TOP_OFFSET = 4;
  private static final int ICON_X_OFFSET = 4;
  private static final int TEXT_X_GAP_AFTER_ICON = 8;
  private static final int TAG_Y_OFFSET = 16;
  private static final int NAME_Y_OFFSET = 32;
  private static final int LEVEL_RIGHT_OFFSET = 10;
  private static final int HINT_Y_OFFSET = 16;
  private static final int DETAILS_PADDING = 24;
  private static final int DETAILS_TITLE_Y_OFFSET = 8;
  private static final int DETAILS_TITLE_BOTTOM_SPACING = 28;
  private static final int ACTIVE_PET_LINE_HEIGHT = 22;
  private static final int XP_BAR_HEIGHT = 12;
  private static final int XP_BAR_Y_OFFSET = 2;
  private static final int XP_BAR_ROUND = 6;
  private static final int XP_BAR_INNER_INSET = 2;
  private static final int XP_BAR_INNER_REDUCTION = 4;
  private static final int XP_TEXT_Y_OFFSET = 14;
  private static final int ACTIVE_STATS_START_Y_OFFSET = 30;
  private static final int INACTIVE_HINT_LINE_HEIGHT = 18;
  private static final int INACTIVE_HINT_BOTTOM_SPACING = 8;
  private static final int DESCRIPTION_LINE_HEIGHT = 22;
  private static final int EGG_INFO_X_TILES = 2;
  private static final int EGG_INFO_Y_TILES = 6;
  private static final int EGG_INFO_Y_EXTRA = 20;
  private static final int EGG_INFO_WIDTH_TILES = 10;
  private static final int EGG_INFO_HEIGHT_TILES = 3;
  private static final int EGG_TEXT_X_OFFSET = 24;
  private static final int EGG_HINT_Y_OFFSET = 32;
  private static final int EGG_INVALID_Y_OFFSET = 64;
  private static final int EGG_NAME_Y_OFFSET = 64;
  private static final int EGG_DETAILS_Y_OFFSET = 88;
  private static final int EGG_TIME_X_OFFSET = 260;
  private static final int EGG_ALREADY_OWNED_Y_OFFSET = 20;
  private static final int INCUBATOR_PANEL_Y_GAP = 16;
  private static final int INCUBATOR_STATUS_HEIGHT_TILES = 2;
  private static final int FRAMES_PER_SECOND = 60;
  private static final int SECONDS_PER_MINUTE = 60;
  private static final int INCUBATOR_PADDING = 24;
  private static final int INCUBATOR_TITLE_BOTTOM_SPACING = 24;
  private static final int INCUBATOR_EMPTY_TEXT_Y_OFFSET = 8;
  private static final int INCUBATOR_TIME_Y_OFFSET = 26;
  private static final int INCUBATOR_BAR_PADDING_X = 24;
  private static final int INCUBATOR_BAR_PADDING_Y = 16;
  private static final int INCUBATOR_BAR_HEIGHT = 18;
  private static final int INCUBATOR_BAR_ROUND = 10;
  private static final int INCUBATOR_BAR_INNER_INSET = 2;
  private static final int INCUBATOR_BAR_INNER_REDUCTION = 4;
  private static final int INCUBATOR_BAR_INNER_ROUND = 8;
  private static final int INCUBATOR_PERCENT_Y_OFFSET = 4;
  private static final int INCUBATOR_ICON_MARGIN = 16;

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

    int x = GamePanel.TILE_SIZE * MAIN_MENU_X_TILES;
    int y = GamePanel.TILE_SIZE * MAIN_MENU_Y_TILES;
    int width = GamePanel.TILE_SIZE * MAIN_MENU_WIDTH_TILES;
    int height = GamePanel.TILE_SIZE * MAIN_MENU_HEIGHT_TILES;
    ui.drawSubWindow(x, y, width, height);

    x += GamePanel.TILE_SIZE;
    y += GamePanel.TILE_SIZE;

    var keyH = gp.getKeyH();

    // 0 - Gerenciar pets
    g2.drawString(ui.t("ui.petYard.manage"), x, y);
    if (ui.getCommandNum() == 0) {
      g2.drawString(">", x - CURSOR_X_OFFSET, y);
      if (keyH.isEnterPressed()) {
        ui.setSubState(1);
        ui.setCommandNum(0);
      }
    }
    y += GamePanel.TILE_SIZE;

    // 1 - Chocar ovo
    g2.drawString(ui.t("ui.petYard.hatch"), x, y);
    if (ui.getCommandNum() == 1) {
      g2.drawString(">", x - CURSOR_X_OFFSET, y);
      if (keyH.isEnterPressed()) {
        ui.setSubState(2);
      }
    }
    y += GamePanel.TILE_SIZE;

    // 2 - Sair
    g2.drawString(ui.t("ui.petYard.leave"), x, y);
    if (ui.getCommandNum() == 2) {
      g2.drawString(">", x - CURSOR_X_OFFSET, y);
      if (keyH.isEnterPressed()) {
        ui.setCommandNum(0);
        gp.setGameState(GamePanel.PLAY_STATE);
      }
    }

    // --- Painel da incubadora no canto inferior esquerdo do Pet Yard ---
    int panelWidth = GamePanel.TILE_SIZE * INCUBATOR_PANEL_WIDTH_TILES;
    int panelHeight = GamePanel.TILE_SIZE * INCUBATOR_PANEL_HEIGHT_TILES;
    int panelX = GamePanel.TILE_SIZE * INCUBATOR_PANEL_X_TILES;
    int panelY = GamePanel.TILE_SIZE * INCUBATOR_PANEL_Y_TILES;

    drawIncubatorStatusPanel(panelX, panelY, panelWidth, panelHeight);
  }

  /**
   * Lista de pets + painel de detalhes ao lado.
   */
  private void drawPetList() {
    Graphics2D g2 = ui.g2;
    GamePanel gp = ui.getGamePanel();
    Player player = gp.getPlayer();
    List<String> pets = player.getOwnedPets();
    PetEntity activePet = player.getPet();

    int frameX = GamePanel.TILE_SIZE * PET_LIST_FRAME_X_TILES;
    int frameY = GamePanel.TILE_SIZE * PET_LIST_FRAME_Y_TILES;
    int frameWidth = GamePanel.TILE_SIZE * PET_LIST_FRAME_WIDTH_TILES;
    int frameHeight = GamePanel.TILE_SIZE * PET_LIST_FRAME_HEIGHT_TILES;

    ui.drawSubWindow(frameX, frameY, frameWidth, frameHeight);

    // Título
    int titleY = frameY + TITLE_Y_OFFSET;
    g2.setFont(ui.fonts.pixelPlain(24F));
    ui.drawTextShadow(
        ui.t("ui.petYard.manageTitle"),
        frameX + 24,
        titleY,
        UI.COL_TEXT,
        Color.black,
        2);

    // Área da lista (lado esquerdo)
    int listAreaX = frameX + LIST_AREA_X_OFFSET;
    int listAreaY = titleY + GamePanel.TILE_SIZE;
    int listAreaWidth = frameWidth / 2 - LIST_AREA_WIDTH_OFFSET;
    int lineHeight = GamePanel.TILE_SIZE + ROW_LINE_EXTRA_SPACING;

    g2.setFont(ui.fonts.pixelPlain(20F));

    if (pets.isEmpty()) {
      drawEmptyPetListMessage(g2, listAreaX, listAreaY);
      return;
    }

    PetRowLayout rowLayout = new PetRowLayout(listAreaX, listAreaY, listAreaWidth, lineHeight);

    drawPetListRows(rowLayout);

    // Painel de detalhes do pet selecionado (lado direito)
    int detailsX = frameX + frameWidth / 2;
    int detailsY = frameY + GamePanel.TILE_SIZE;
    int detailsWidth = frameWidth / 2 - 24;
    int detailsHeight = frameHeight - GamePanel.TILE_SIZE * 2;

    drawPetDetailsPanel(
        pets,
        activePet,
        detailsX,
        detailsY,
        detailsWidth,
        detailsHeight);

    // dica (ENTER / ESC) no rodapé, alinhada à esquerda
    drawPetListHint(g2, frameX, frameY, frameHeight);

    // ENTER => equipa / desequipa pet
    handlePetSelectionInput(gp, player, pets);
  }

  private void drawEmptyPetListMessage(Graphics2D g2, int listAreaX, int listAreaY) {
    g2.setColor(UI.COL_TEXT_DIM);
    g2.drawString(ui.t("ui.petYard.noPets"), listAreaX + 8, listAreaY + 8);
  }

  private void drawPetListRows(PetRowLayout layout) {
    GamePanel gp = ui.getGamePanel();
    if (gp == null) {
      return;
    }
    Player player = gp.getPlayer();
    if (player == null) {
      return;
    }

    List<String> pets = player.getOwnedPets();
    if (pets == null || pets.isEmpty()) {
      return;
    }

    for (int i = 0; i < pets.size(); i++) {
      String petId = pets.get(i);
      drawPetRow(petId, i, layout);
    }
  }

  private void drawPetRow(String petId, int rowIndex, PetRowLayout layout) {
    Graphics2D g2 = ui.g2;
    GamePanel gp = ui.getGamePanel();
    if (gp == null) {
      return;
    }
    Player player = gp.getPlayer();
    if (player == null) {
      return;
    }

    PetEntity activePet = player.getPet();

    int rowCenterY = layout.listAreaY + rowIndex * layout.lineHeight;
    int rowTop = rowCenterY - GamePanel.TILE_SIZE + ROW_TOP_OFFSET;

    boolean selected = (rowIndex == ui.getCommandNum());

    int rowX = layout.listAreaX;
    int rowW = layout.listAreaWidth;
    int rowH = GamePanel.TILE_SIZE;

    // Highlight da linha
    if (selected) {
      g2.setColor(UI.COL_HIGHLIGHT_BG);
      g2.fillRoundRect(rowX, rowTop, rowW, rowH, 10, 10);
      g2.setColor(UI.COL_PANEL_BORDER);
      g2.drawRoundRect(rowX, rowTop, rowW, rowH, 10, 10);
    }

    // Ícone
    BufferedImage icon = PetFactory.getPetIcon(petId, gp, player);
    int iconSize = GamePanel.TILE_SIZE;
    int iconX = rowX + ICON_X_OFFSET;
    int iconY = rowTop;
    if (icon != null) {
      g2.drawImage(icon, iconX, iconY, iconSize, iconSize, null);
    }

    String name = ui.t(petId + PET_NAME_TRANSLATION_SUFFIX);

    int textX = iconX + iconSize + TEXT_X_GAP_AFTER_ICON;
    int tagY = rowTop + TAG_Y_OFFSET; // linha de cima
    int nameY = rowTop + NAME_Y_OFFSET; // linha de baixo

    boolean isActive = activePet != null && petId.equals(activePet.getInternalId());

    // Linha de cima: [Ativo] e Lv.X
    if (isActive) {
      int level = activePet.getPetLevel();

      // Tag [Ativo] à esquerda (acima do nome)
      g2.setFont(ui.fonts.pixelItalic(16F));
      g2.setColor(UI.COL_TEXT_DIM);
      g2.drawString(ui.t("ui.petYard.currentPetTag"), textX, tagY);

      // Level alinhado à direita na mesma linha do [Ativo]
      String levelLabel = "Lv. " + level;
      g2.setFont(ui.fonts.pixelPlain(20F));
      g2.setColor(selected ? UI.COL_ACCENT : UI.COL_TEXT);
      var fmLevel = g2.getFontMetrics();
      int levelWidth = fmLevel.stringWidth(levelLabel);
      int levelX = rowX + rowW - levelWidth - LEVEL_RIGHT_OFFSET;
      g2.drawString(levelLabel, levelX, tagY);
    }

    // Linha de baixo: nome do pet
    g2.setFont(ui.fonts.pixelPlain(20F));
    g2.setColor(selected ? UI.COL_ACCENT : UI.COL_TEXT);
    g2.drawString(name, textX, nameY);
  }

  private void drawPetListHint(
      Graphics2D g2, int frameX, int frameY, int frameHeight) {
    int hintY = frameY + frameHeight - HINT_Y_OFFSET;
    g2.setFont(ui.fonts.pixelPlain(16F));
    g2.setColor(UI.COL_TEXT_DIM);
    g2.drawString(ui.t("ui.petYard.hintEquip"), frameX + 24, hintY);
  }

  private void handlePetSelectionInput(GamePanel gp, Player player, List<String> pets) {
    if (pets.isEmpty()) {
      return;
    }

    var keyH = gp.getKeyH();
    if (!keyH.isEnterPressed()) {
      return;
    }

    int index = ui.getCommandNum();
    if (index < 0 || index >= pets.size()) {
      return;
    }

    String chosenId = pets.get(index);
    String activePetId = player.getActivePetId();

    if (chosenId.equals(activePetId)) {
      handleUnequipPet(gp, player, chosenId);
    } else {
      handleEquipPet(gp, player, chosenId);
    }
  }

  private void handleUnequipPet(GamePanel gp, Player player, String petId) {
    player.unequipPet();

    String petName = ui.t(petId + PET_NAME_TRANSLATION_SUFFIX);
    String template = ui.t("ui.petYard.petUnequipped");
    String msg = String.format(template, petName);
    gp.getGameUi().addMessage(msg);
  }

  private void handleEquipPet(GamePanel gp, Player player, String petId) {
    boolean equipped = player.equipPetById(petId);
    if (!equipped) {
      return;
    }

    String petName = ui.t(petId + PET_NAME_TRANSLATION_SUFFIX);
    String template = ui.t("ui.petYard.petEquipped");
    String msg = String.format(template, petName);
    gp.getGameUi().addMessage(msg);
  }

  /**
   * Painel de detalhes do pet selecionado (lado direito).
   *
   * Mostra:
   * - Nome
   * - Nível + barra de XP (se for o pet ativo)
   * - Stats numéricos (cura/mana) do pet ativo
   * - Descrição
   */
  private void drawPetDetailsPanel(
      List<String> pets,
      PetEntity activePet,
      int frameX,
      int frameY,
      int frameWidth,
      int frameHeight) {

    Graphics2D g2 = ui.g2;
    ui.drawSubWindow(frameX, frameY, frameWidth, frameHeight);

    if (pets.isEmpty()) {
      return;
    }

    int selectedIndex = ui.getCommandNum();
    if (selectedIndex < 0 || selectedIndex >= pets.size()) {
      return;
    }

    String petId = pets.get(selectedIndex);
    String name = ui.t(petId + PET_NAME_TRANSLATION_SUFFIX);
    String desc = ui.t(petId + ".desc");

    int padding = DETAILS_PADDING;
    int textX = frameX + padding;
    int textY = frameY + padding + DETAILS_TITLE_Y_OFFSET;

    // Nome do pet
    g2.setFont(ui.fonts.pixelBold(22F));
    g2.setColor(UI.COL_TEXT);
    g2.drawString(name, textX, textY);

    textY += DETAILS_TITLE_BOTTOM_SPACING;

    boolean isActive = activePet != null && petId.equals(activePet.getInternalId());

    if (isActive) {
      textY = drawActivePetDetails(activePet, textX, textY, frameWidth, padding);
    } else {
      textY = drawInactivePetHint(textX, textY);
    }

    drawPetDescription(desc, textX, textY);
  }

  private int drawActivePetDetails(
      PetEntity activePet, int textX, int textY, int frameWidth, int padding) {

    Graphics2D g2 = ui.g2;

    int level = activePet.getPetLevel();
    int exp = activePet.getPetExp();
    int next = activePet.getPetNextLevelExp();

    g2.setFont(ui.fonts.pixelPlain(18F));
    g2.setColor(UI.COL_ACCENT);
    g2.drawString("Lv. " + level, textX, textY);
    textY += ACTIVE_PET_LINE_HEIGHT;

    // Barra de XP
    int barWidth = frameWidth - padding * 2;
    int barHeight = XP_BAR_HEIGHT;
    int barX = textX;
    int barY = textY + XP_BAR_Y_OFFSET;

    double ratio = (next > 0) ? Math.min(1.0, exp / (double) next) : 0.0;
    int filled = (int) (barWidth * ratio);

    g2.setColor(COL_BAR_BG);
    g2.fillRoundRect(barX, barY, barWidth, barHeight, XP_BAR_ROUND, XP_BAR_ROUND);
    g2.setColor(UI.COL_PANEL_BORDER);
    g2.drawRoundRect(barX, barY, barWidth, barHeight, XP_BAR_ROUND, XP_BAR_ROUND);

    if (filled > 0) {
      g2.setColor(UI.COL_ACCENT);
      g2.fillRoundRect(
          barX + XP_BAR_INNER_INSET,
          barY + XP_BAR_INNER_INSET,
          filled - XP_BAR_INNER_REDUCTION,
          barHeight - XP_BAR_INNER_REDUCTION,
          4,
          4);
    }

    String xpText = "XP " + exp + " / " + next;
    g2.setFont(ui.fonts.pixelPlain(14F));
    g2.setColor(UI.COL_TEXT_DIM);
    g2.drawString(xpText, barX, barY + barHeight + XP_TEXT_Y_OFFSET);

    int currentTextY = barY + barHeight + ACTIVE_STATS_START_Y_OFFSET;

    // ---- Stats numéricos específicos do pet ativo (delegados ao próprio Pet) ----
    String statsLine = activePet.buildPetStatsLineForUi();
    if (statsLine != null && !statsLine.isBlank()) {
      g2.setFont(ui.fonts.pixelPlain(18F));
      g2.setColor(UI.COL_TEXT);
      g2.drawString(statsLine, textX, currentTextY);
      currentTextY += ACTIVE_PET_LINE_HEIGHT;
    }

    return currentTextY;
  }

  private int drawInactivePetHint(int textX, int textY) {

    Graphics2D g2 = ui.g2;

    // Pet não ativo: dica para ativar (suporta quebra de linha com \n)
    g2.setFont(ui.fonts.pixelPlain(14F));
    g2.setColor(UI.COL_TEXT_DIM);

    String hint = ui.t("ui.petYard.inactivePetHint");
    for (String line : hint.split("\n")) {
      g2.drawString(line, textX, textY);
      textY += INACTIVE_HINT_LINE_HEIGHT; // espaçamento entre linhas
    }

    return textY + INACTIVE_HINT_BOTTOM_SPACING; // respiro antes da descrição
  }

  private void drawPetDescription(String desc, int textX, int textY) {

    Graphics2D g2 = ui.g2;

    // Descrição do pet
    g2.setFont(ui.fonts.pixelPlain(18F));
    g2.setColor(UI.COL_TEXT);

    for (String line : desc.split("\n")) {
      g2.drawString(line, textX, textY);
      textY += DESCRIPTION_LINE_HEIGHT;
    }
  }

  private void drawEggSelection() {
    Graphics2D g2 = ui.g2;
    GamePanel gp = ui.getGamePanel();
    Player player = gp.getPlayer();

    // inventário do player com cursor (desenha mochila + descrição)
    inventoryUi.drawInventory(player, true);

    int infoX = GamePanel.TILE_SIZE * EGG_INFO_X_TILES;
    int infoY = GamePanel.TILE_SIZE * EGG_INFO_Y_TILES + EGG_INFO_Y_EXTRA;
    int infoWidth = GamePanel.TILE_SIZE * EGG_INFO_WIDTH_TILES;
    int infoHeight = GamePanel.TILE_SIZE * EGG_INFO_HEIGHT_TILES;

    ui.drawSubWindow(infoX, infoY, infoWidth, infoHeight);

    // hint
    g2.setFont(ui.fonts.pixelPlain(20F));
    g2.setColor(UI.COL_TEXT);
    g2.drawString(ui.t("ui.petYard.eggHint"), infoX + EGG_TEXT_X_OFFSET, infoY + EGG_HINT_Y_OFFSET);

    int itemIndex = ui.getItemIndexOnSlot(ui.getPlayerSlotCol(), ui.getPlayerSlotRow());
    if (itemIndex >= player.getInventory().size()) {
      return;
    }

    Entity item = player.getInventory().get(itemIndex);

    if (item == null || item.getType() != Entity.TYPE_EGG) {
      g2.setColor(UI.COL_TEXT_DIM);
      g2.setFont(ui.fonts.pixelPlain(20F));
      g2.drawString(ui.t("ui.petYard.eggInvalid"), infoX + EGG_TEXT_X_OFFSET, infoY + EGG_INVALID_Y_OFFSET);
      return;
    }

    PetEgg egg = (PetEgg) item;

    // detalhes do ovo válido selecionado
    String eggName = egg.getDisplayName();
    int cost = egg.getHatchCost();
    int seconds = egg.getHatchTimeTicks() / FRAMES_PER_SECOND;

    String lineCost = String.format(ui.t("ui.petYard.eggCost"), cost);
    String lineTime = String.format(ui.t("ui.petYard.eggTime"), seconds);

    g2.setColor(UI.COL_TEXT);

    // nome do ovo como "título"
    g2.setFont(ui.fonts.pixelBold(20F));
    g2.drawString(eggName, infoX + EGG_TEXT_X_OFFSET, infoY + EGG_NAME_Y_OFFSET);

    // custo + tempo como "descrição"
    g2.setFont(ui.fonts.pixelPlain(20F));
    g2.drawString(lineCost, infoX + EGG_TEXT_X_OFFSET, infoY + EGG_DETAILS_Y_OFFSET);
    g2.drawString(lineTime, infoX + EGG_TIME_X_OFFSET, infoY + EGG_DETAILS_Y_OFFSET);

    // já possuo esse pet?
    boolean alreadyOwned = player.getOwnedPets().contains(egg.getPetId());
    if (alreadyOwned) {
      g2.setFont(ui.fonts.pixelPlain(20F));
      g2.setColor(UI.COL_ACCENT);
      int warnY = infoY + infoHeight - EGG_ALREADY_OWNED_Y_OFFSET;
      g2.drawString(ui.t("ui.petYard.petAlreadyOwned"), infoX + EGG_TEXT_X_OFFSET, warnY);
      return;
    }

    var keyH = gp.getKeyH();
    if (keyH.isEnterPressed()) {
      tryStartIncubation(player, egg, itemIndex);
    }

    // --- Painel da incubadora logo abaixo das informações do ovo ---
    int panelX = infoX;
    int panelY = infoY + infoHeight + INCUBATOR_PANEL_Y_GAP;
    int panelWidth = infoWidth;
    int panelHeight = GamePanel.TILE_SIZE * INCUBATOR_STATUS_HEIGHT_TILES;

    drawIncubatorStatusPanel(panelX, panelY, panelWidth, panelHeight);
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

  /**
   * Painel de status da incubadora:
   * - mostra ovo/pet atual
   * - tempo restante
   * - barra de progresso (%)
   *
   * Pode ser chamado em qualquer subState do Pet Yard.
   */
  private void drawIncubatorStatusPanel(int frameX, int frameY, int frameWidth, int frameHeight) {
    Graphics2D g2 = ui.g2;
    GamePanel gp = ui.getGamePanel();

    if (gp == null) {
      return;
    }

    PetIncubator incubator = gp.getPetIncubator();
    if (incubator == null) {
      return;
    }

    // Janela base
    ui.drawSubWindow(frameX, frameY, frameWidth, frameHeight);

    int padding = INCUBATOR_PADDING;
    int textX = frameX + padding;
    int textY = frameY + padding;

    // Título da caixa (ex.: "Incubadora")
    g2.setFont(ui.fonts.pixelBold(20F));
    ui.drawTextShadow(
        ui.t("ui.petYard.incubatorTitle"),
        textX,
        textY,
        UI.COL_TEXT,
        Color.black,
        2);

    textY += INCUBATOR_TITLE_BOTTOM_SPACING;

    if (!incubator.hasActiveEgg()) {
      drawIncubatorEmptyMessage(textX, textY);
      return;
    }

    drawIncubatorEggNameAndTime(incubator, textX, textY);

    int progress = incubator.getProgressPercent();
    boolean hasTicks = incubator.getTotalTicks() > 0;

    drawIncubatorProgressBar(frameX, frameY, frameWidth, frameHeight, progress, hasTicks);

    drawIncubatorPetIcon(incubator, gp, frameX, frameY, frameWidth);
  }

  private void drawIncubatorEmptyMessage(int textX, int textY) {

    Graphics2D g2 = ui.g2;

    g2.setFont(ui.fonts.pixelPlain(16F));
    g2.setColor(UI.COL_TEXT_DIM);
    g2.drawString(ui.t("ui.petYard.noEggIncubating"), textX, textY + INCUBATOR_EMPTY_TEXT_Y_OFFSET);
  }

  private void drawIncubatorEggNameAndTime(
      PetIncubator incubator,
      int textX,
      int textY) {

    Graphics2D g2 = ui.g2;

    String eggName = incubator.getEggName();
    if (eggName == null || eggName.isBlank()) {
      // fallback: nome do pet que vai nascer
      String petId = incubator.getPetId();
      if (petId != null) {
        eggName = ui.t(petId + PET_NAME_TRANSLATION_SUFFIX);
      } else {
        eggName = ui.t("ui.petYard.unknownEgg");
      }
    }

    int remainingTicks = incubator.getRemainingTicks();
    int remainingSeconds = Math.max(0, remainingTicks / FRAMES_PER_SECOND);
    int remMinutes = remainingSeconds / SECONDS_PER_MINUTE;
    int remSeconds = remainingSeconds % SECONDS_PER_MINUTE;
    String timeLabel = ui.t("ui.petYard.timeRemainingLabel"); // ex.: "Tempo restante"
    String timeText = String.format("%s %02d:%02d", timeLabel, remMinutes, remSeconds);

    // Nome do ovo/pet
    g2.setFont(ui.fonts.pixelPlain(16F));
    g2.setColor(UI.COL_TEXT);
    g2.drawString(eggName, textX, textY + 8);

    // Tempo restante logo abaixo
    g2.setColor(UI.COL_TEXT_DIM);
    g2.drawString(timeText, textX, textY + INCUBATOR_TIME_Y_OFFSET);
  }

  private void drawIncubatorProgressBar(
      int frameX,
      int frameY,
      int frameWidth,
      int frameHeight,
      int progress,
      boolean hasTicks) {

    Graphics2D g2 = ui.g2;

    int barPaddingX = INCUBATOR_BAR_PADDING_X;
    int barPaddingY = INCUBATOR_BAR_PADDING_Y;
    int barWidth = frameWidth - barPaddingX * 2;
    int barHeight = INCUBATOR_BAR_HEIGHT;
    int barX = frameX + barPaddingX;
    int barY = frameY + frameHeight - barHeight - barPaddingY;

    // Fundo da barra
    g2.setColor(COL_BAR_BG);
    g2.fillRoundRect(barX, barY, barWidth, barHeight, INCUBATOR_BAR_ROUND, INCUBATOR_BAR_ROUND);

    // Borda da barra
    g2.setColor(UI.COL_PANEL_BORDER);
    g2.drawRoundRect(barX, barY, barWidth, barHeight, INCUBATOR_BAR_ROUND, INCUBATOR_BAR_ROUND);

    // Parte preenchida
    if (hasTicks && progress > 0) {
      int filledWidth = (barWidth - INCUBATOR_BAR_INNER_REDUCTION) * progress / 100;
      g2.setColor(UI.COL_ACCENT);
      g2.fillRoundRect(
          barX + INCUBATOR_BAR_INNER_INSET,
          barY + INCUBATOR_BAR_INNER_INSET,
          filledWidth,
          barHeight - INCUBATOR_BAR_INNER_REDUCTION,
          INCUBATOR_BAR_INNER_ROUND,
          INCUBATOR_BAR_INNER_ROUND);
    }

    // Texto da % no centro da barra
    String percentText = progress + "%";
    g2.setFont(ui.fonts.pixelBold(14F));
    g2.setColor(Color.white);
    int textWidth = g2.getFontMetrics().stringWidth(percentText);
    int percentX = barX + (barWidth - textWidth) / 2;
    int percentY = barY + barHeight - INCUBATOR_PERCENT_Y_OFFSET;
    g2.drawString(percentText, percentX, percentY);
  }

  private void drawIncubatorPetIcon(
      PetIncubator incubator,
      GamePanel gp,
      int frameX,
      int frameY,
      int frameWidth) {

    String petId = incubator.getPetId();
    if (petId == null) {
      return;
    }

    try {
      BufferedImage icon = PetFactory.getPetIcon(petId, gp, gp.getPlayer());
      if (icon != null) {
        int iconSize = GamePanel.TILE_SIZE;
        int iconX = frameX + frameWidth - iconSize - INCUBATOR_ICON_MARGIN;
        int iconY = frameY + INCUBATOR_ICON_MARGIN;
        ui.g2.drawImage(icon, iconX, iconY, null);
      }
    } catch (Exception ignored) {
      // Se der erro ao carregar sprite, ignora só o ícone.
    }
  }

  /**
   * Layout de uma linha da lista de pets (apenas coordenadas).
   */
  private record PetRowLayout(int listAreaX, int listAreaY, int listAreaWidth, int lineHeight) {
  }
}

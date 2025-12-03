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

    // --- Painel da incubadora no canto inferior esquerdo do Pet Yard ---
    int panelWidth = GamePanel.TILE_SIZE * 6;
    int panelHeight = GamePanel.TILE_SIZE * 3;
    int panelX = GamePanel.TILE_SIZE * 3;
    int panelY = GamePanel.TILE_SIZE * 7;

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

    int frameX = GamePanel.TILE_SIZE * 2;
    int frameY = GamePanel.TILE_SIZE;
    int frameWidth = GamePanel.TILE_SIZE * 16;
    int frameHeight = GamePanel.TILE_SIZE * 8;

    ui.drawSubWindow(frameX, frameY, frameWidth, frameHeight);

    // Fonte igual à descrição/título de item do trade
    Font baseFont = ui.getMaruMonica() != null ? ui.getMaruMonica() : g2.getFont();
    Font titleFont = baseFont.deriveFont(Font.PLAIN, 24F);
    Font listFont = baseFont.deriveFont(Font.PLAIN, 20F);
    Font tagFont = baseFont.deriveFont(Font.ITALIC, 16F);
    Font hintFont = baseFont.deriveFont(Font.PLAIN, 16F);

    // Título
    int titleY = frameY + 32;
    g2.setFont(titleFont);
    ui.drawTextShadow(
        ui.t("ui.petYard.manageTitle"),
        frameX + 24,
        titleY,
        UI.COL_TEXT,
        Color.black,
        2);

    // Área da lista (lado esquerdo)
    int listAreaX = frameX + 16;
    int listAreaY = titleY + GamePanel.TILE_SIZE;
    int listAreaWidth = frameWidth / 2 - 24;
    int lineHeight = GamePanel.TILE_SIZE + 8;

    g2.setFont(listFont);

    if (pets.isEmpty()) {
      g2.setColor(UI.COL_TEXT_DIM);
      g2.drawString(ui.t("ui.petYard.noPets"), listAreaX + 8, listAreaY + 8);
      return;
    }

    for (int i = 0; i < pets.size(); i++) {
      String petId = pets.get(i);
      int rowCenterY = listAreaY + i * lineHeight;
      int rowTop = rowCenterY - GamePanel.TILE_SIZE + 4;

      boolean selected = (i == ui.getCommandNum());

      int rowX = listAreaX;
      int rowW = listAreaWidth;
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
      int iconX = rowX + 4;
      int iconY = rowTop;
      if (icon != null) {
        g2.drawImage(icon, iconX, iconY, iconSize, iconSize, null);
      }

      String name = ui.t(petId + ".name");

      int textX = iconX + iconSize + 8;
      int tagY = rowTop + 16; // linha de cima
      int nameY = rowTop + 32; // linha de baixo

      boolean isActive = activePet != null && petId.equals(activePet.getInternalId());

      // Linha de cima: [Ativo] e Lv.X
      if (isActive) {
        int level = activePet.getPetLevel();

        // Tag [Ativo] à esquerda (acima do nome)
        g2.setFont(tagFont);
        g2.setColor(UI.COL_TEXT_DIM);
        g2.drawString(ui.t("ui.petYard.currentPetTag"), textX, tagY);

        // Level alinhado à direita na mesma linha do [Ativo]
        String levelLabel = "Lv. " + level;
        g2.setFont(listFont);
        g2.setColor(selected ? UI.COL_ACCENT : UI.COL_TEXT);
        var fmLevel = g2.getFontMetrics();
        int levelWidth = fmLevel.stringWidth(levelLabel);
        int levelX = rowX + rowW - levelWidth - 10;
        g2.drawString(levelLabel, levelX, tagY);
      }

      // Linha de baixo: nome do pet
      g2.setFont(listFont);
      g2.setColor(selected ? UI.COL_ACCENT : UI.COL_TEXT);
      g2.drawString(name, textX, nameY);
    }

    // Painel de detalhes do pet selecionado (lado direito)
    int detailsX = frameX + frameWidth / 2;
    int detailsY = frameY + GamePanel.TILE_SIZE;
    int detailsWidth = frameWidth / 2 - 24;
    int detailsHeight = frameHeight - GamePanel.TILE_SIZE * 2;

    drawPetDetailsPanel(
        g2,
        gp,
        player,
        pets,
        activePet,
        detailsX,
        detailsY,
        detailsWidth,
        detailsHeight,
        baseFont);

    // dica (ENTER / ESC) no rodapé, alinhada à esquerda
    int hintY = frameY + frameHeight - 16;
    g2.setFont(hintFont);
    g2.setColor(UI.COL_TEXT_DIM);
    g2.drawString(ui.t("ui.petYard.hintEquip"), frameX + 24, hintY);

    // ENTER => equipa / desequipa pet
    var keyH = gp.getKeyH();
    if (keyH.isEnterPressed() && !pets.isEmpty()) {
      int index = ui.getCommandNum();
      if (index < 0 || index >= pets.size()) {
        return;
      }

      String chosenId = pets.get(index);

      // Se já é o pet ativo, ENTER vira "desequipar"
      if (chosenId.equals(player.getActivePetId())) {
        player.unequipPet();

        String petName = ui.t(chosenId + ".name");
        String template = ui.t("ui.petYard.petUnequipped");
        String msg = String.format(template, petName);
        gp.getGameUi().addMessage(msg);
      } else {
        // Senão, equipa normalmente
        boolean equipped = player.equipPetById(chosenId);
        if (equipped) {
          String petName = ui.t(chosenId + ".name");
          String template = ui.t("ui.petYard.petEquipped");
          String msg = String.format(template, petName);
          gp.getGameUi().addMessage(msg);
        }
      }
    }
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
      Graphics2D g2,
      GamePanel gp,
      Player player,
      List<String> pets,
      PetEntity activePet,
      int frameX,
      int frameY,
      int frameWidth,
      int frameHeight,
      Font baseFont) {

    ui.drawSubWindow(frameX, frameY, frameWidth, frameHeight);

    if (pets.isEmpty()) {
      return;
    }

    int selectedIndex = ui.getCommandNum();
    if (selectedIndex < 0 || selectedIndex >= pets.size()) {
      return;
    }

    String petId = pets.get(selectedIndex);
    String name = ui.t(petId + ".name");
    String desc = ui.t(petId + ".desc");

    Font titleFont = baseFont.deriveFont(Font.BOLD, 22F);
    Font descFont = baseFont.deriveFont(Font.PLAIN, 18F);
    Font smallFont = baseFont.deriveFont(Font.PLAIN, 14F);

    int padding = 24;
    int textX = frameX + padding;
    int textY = frameY + padding + 8;

    // Nome do pet
    g2.setFont(titleFont);
    g2.setColor(UI.COL_TEXT);
    g2.drawString(name, textX, textY);

    textY += 28;

    boolean isActive = activePet != null && petId.equals(activePet.getInternalId());

    // Se for o pet ativo, exibe o Level, barra de XP e números da habilidade
    if (isActive) {
      int level = activePet.getPetLevel();
      int exp = activePet.getPetExp();
      int next = activePet.getPetNextLevelExp();

      g2.setFont(descFont);
      g2.setColor(UI.COL_ACCENT);
      g2.drawString("Lv. " + level, textX, textY);
      textY += 22;

      // Barra de XP
      int barWidth = frameWidth - padding * 2;
      int barHeight = 12;
      int barX = textX;
      int barY = textY + 2;

      double ratio = (next > 0) ? Math.min(1.0, exp / (double) next) : 0.0;
      int filled = (int) (barWidth * ratio);

      g2.setColor(new Color(0, 0, 0, 160));
      g2.fillRoundRect(barX, barY, barWidth, barHeight, 6, 6);
      g2.setColor(UI.COL_PANEL_BORDER);
      g2.drawRoundRect(barX, barY, barWidth, barHeight, 6, 6);

      if (filled > 0) {
        g2.setColor(UI.COL_ACCENT);
        g2.fillRoundRect(barX + 2, barY + 2, filled - 4, barHeight - 4, 4, 4);
      }

      String xpText = "XP " + exp + " / " + next;
      g2.setFont(smallFont);
      g2.setColor(UI.COL_TEXT_DIM);
      g2.drawString(xpText, barX, barY + barHeight + 14);

      textY = barY + barHeight + 30;

      // ---- Stats numéricos específicos do pet ativo (delegados ao próprio Pet) ----
      String statsLine = activePet.buildPetStatsLineForUi();
      if (statsLine != null && !statsLine.isBlank()) {
        g2.setFont(descFont);
        g2.setColor(UI.COL_TEXT);
        g2.drawString(statsLine, textX, textY);
        textY += 22;
      }

    } else {
      // Pet não ativo: dica para ativar (suporta quebra de linha com \n)
      g2.setFont(smallFont);
      g2.setColor(UI.COL_TEXT_DIM);

      String hint = ui.t("ui.petYard.inactivePetHint");
      for (String line : hint.split("\n")) {
        g2.drawString(line, textX, textY);
        textY += 18; // espaçamento entre linhas
      }

      textY += 8; // respiro antes da descrição
    }

    // Descrição do pet
    g2.setFont(descFont);
    g2.setColor(UI.COL_TEXT);

    for (String line : desc.split("\n")) {
      g2.drawString(line, textX, textY);
      textY += 22;
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

    // inventário do player com cursor (desenha mochila + descrição)
    inventoryUi.drawInventory(player, true);

    int infoX = GamePanel.TILE_SIZE * 2;
    int infoY = GamePanel.TILE_SIZE * 6 + 20;
    int infoWidth = GamePanel.TILE_SIZE * 10;
    int infoHeight = GamePanel.TILE_SIZE * 3;

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

    if (item == null || item.getType() != Entity.TYPE_EGG) {
      g2.setColor(UI.COL_TEXT_DIM);
      g2.setFont(descFont);
      g2.drawString(ui.t("ui.petYard.eggInvalid"), infoX + 24, infoY + 64);
      return;
    }

    PetEgg egg = (PetEgg) item;

    // detalhes do ovo válido selecionado
    String eggName = egg.getDisplayName();
    int cost = egg.getHatchCost();
    int seconds = egg.getHatchTimeTicks() / 60;

    String lineCost = String.format(ui.t("ui.petYard.eggCost"), cost);
    String lineTime = String.format(ui.t("ui.petYard.eggTime"), seconds);

    g2.setColor(UI.COL_TEXT);

    // nome do ovo como "título"
    g2.setFont(titleFont);
    g2.drawString(eggName, infoX + 24, infoY + 64);

    // custo + tempo como "descrição"
    g2.setFont(descFont);
    g2.drawString(lineCost, infoX + 24, infoY + 88);
    g2.drawString(lineTime, infoX + 260, infoY + 88);

    // já possuo esse pet?
    boolean alreadyOwned = player.getOwnedPets().contains(egg.getPetId());
    if (alreadyOwned) {
      g2.setFont(descFont);
      g2.setColor(UI.COL_ACCENT);
      int warnY = infoY + infoHeight - 20;
      g2.drawString(ui.t("ui.petYard.petAlreadyOwned"), infoX + 24, warnY);
      return;
    }

    var keyH = gp.getKeyH();
    if (keyH.isEnterPressed()) {
      tryStartIncubation(player, egg, itemIndex);
    }

    // --- Painel da incubadora logo abaixo das informações do ovo ---
    int panelX = infoX;
    int panelY = infoY + infoHeight + 16;
    int panelWidth = infoWidth;
    int panelHeight = GamePanel.TILE_SIZE * 2;

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

    // Fonte padrão da UI
    Font baseFont = ui.getMaruMonica() != null ? ui.getMaruMonica() : g2.getFont();
    Font titleFont = baseFont.deriveFont(Font.BOLD, 20F);
    Font infoFont = baseFont.deriveFont(Font.PLAIN, 16F);
    Font percentFont = baseFont.deriveFont(Font.BOLD, 14F);

    int padding = 24;
    int textX = frameX + padding;
    int textY = frameY + padding;

    // Título da caixa (ex.: "Incubadora")
    g2.setFont(titleFont);
    ui.drawTextShadow(
        ui.t("ui.petYard.incubatorTitle"),
        textX,
        textY,
        UI.COL_TEXT,
        Color.black,
        2);

    textY += 24;

    if (!incubator.hasActiveEgg()) {
      // Sem ovo sendo chocado: mensagem simples
      g2.setFont(infoFont);
      g2.setColor(UI.COL_TEXT_DIM);
      g2.drawString(ui.t("ui.petYard.noEggIncubating"), textX, textY + 8);
      return;
    }

    // Há um ovo em progresso
    String eggName = incubator.getEggName();
    if (eggName == null || eggName.isBlank()) {
      // fallback: nome do pet que vai nascer
      String petId = incubator.getPetId();
      if (petId != null) {
        eggName = ui.t(petId + ".name");
      } else {
        eggName = ui.t("ui.petYard.unknownEgg");
      }
    }

    int remainingTicks = incubator.getRemainingTicks();
    int totalTicks = incubator.getTotalTicks();
    int progress = incubator.getProgressPercent();

    int remainingSeconds = Math.max(0, remainingTicks / 60);
    int remMinutes = remainingSeconds / 60;
    int remSeconds = remainingSeconds % 60;
    String timeLabel = ui.t("ui.petYard.timeRemainingLabel"); // ex.: "Tempo restante"
    String timeText = String.format("%s %02d:%02d", timeLabel, remMinutes, remSeconds);

    // Nome do ovo/pet
    g2.setFont(infoFont);
    g2.setColor(UI.COL_TEXT);
    g2.drawString(eggName, textX, textY + 8);

    // Tempo restante logo abaixo
    g2.setColor(UI.COL_TEXT_DIM);
    g2.drawString(timeText, textX, textY + 26);

    // Barra de progresso na parte de baixo da janela
    int barPaddingX = 24;
    int barPaddingY = 16;
    int barWidth = frameWidth - barPaddingX * 2;
    int barHeight = 18;
    int barX = frameX + barPaddingX;
    int barY = frameY + frameHeight - barHeight - barPaddingY;

    // Fundo da barra
    g2.setColor(new Color(0, 0, 0, 160));
    g2.fillRoundRect(barX, barY, barWidth, barHeight, 10, 10);

    // Borda da barra
    g2.setColor(UI.COL_PANEL_BORDER);
    g2.drawRoundRect(barX, barY, barWidth, barHeight, 10, 10);

    // Parte preenchida
    if (totalTicks > 0 && progress > 0) {
      int filledWidth = (barWidth - 4) * progress / 100;
      g2.setColor(UI.COL_ACCENT);
      g2.fillRoundRect(barX + 2, barY + 2, filledWidth, barHeight - 4, 8, 8);
    }

    // Texto da % no centro da barra
    String percentText = progress + "%";
    g2.setFont(percentFont);
    g2.setColor(Color.white);
    int textWidth = g2.getFontMetrics().stringWidth(percentText);
    int percentX = barX + (barWidth - textWidth) / 2;
    int percentY = barY + barHeight - 4;
    g2.drawString(percentText, percentX, percentY);

    // Ícone do pet (estilo RPG AAA, canto direito da janela)
    try {
      if (incubator.getPetId() != null) {
        BufferedImage icon = PetFactory.getPetIcon(incubator.getPetId(), gp, gp.getPlayer());
        if (icon != null) {
          int iconSize = GamePanel.TILE_SIZE;
          int iconX = frameX + frameWidth - iconSize - 16;
          int iconY = frameY + 16;
          g2.drawImage(icon, iconX, iconY, null);
        }
      }
    } catch (Exception e) {
      // Se der erro ao carregar sprite, ignora só o ícone.
    }
  }
}

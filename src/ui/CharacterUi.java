package ui;

import java.awt.Color;
import java.awt.Graphics2D;

import entity.Entity;
import main.GamePanel;

class CharacterUi {

  private static final Color COL_PORTRAIT_BG = new Color(10, 10, 15, 190);
  private static final Color COL_EQUIP_SLOT_BG = new Color(20, 24, 32, 220);
  private static final int FRAME_X_TILES = 2;
  private static final int FRAME_Y_TILES = 1;
  private static final int FRAME_WIDTH_TILES = 9;
  private static final int FRAME_HEIGHT_TILES = 10;
  private static final int BASE_PADDING = 24;
  private static final int TITLE_Y_OFFSET = 8;
  private static final int TITLE_BOTTOM_SPACING = 44;
  private static final int PORTRAIT_SIZE_TILES = 3;
  private static final int PORTRAIT_ROUND_RADIUS = 12;
  private static final int INFO_X_OFFSET = 24;
  private static final int INFO_Y_OFFSET = 8;
  private static final int INFO_LINE_HEIGHT = 30;
  private static final int STAT_BAR_WIDTH_TILES = 4;
  private static final int STAT_BAR_HEIGHT = 16;
  private static final int STATS_BLOCK_Y_OFFSET = 40;
  private static final int STATS_LINE_HEIGHT = 24;
  private static final int STATS_TAIL_X_OFFSET = 30;
  private static final int EQUIP_AREA_PADDING = 16;
  private static final int EQUIP_TITLE_Y_OFFSET = 32;
  private static final int EQUIP_TITLE_COLLISION_PADDING = 8;
  private static final int EQUIP_GRID_COLS = 3;
  private static final int EQUIP_GRID_ROWS = 3;
  private static final int EQUIP_MIN_H_GAP = 12;
  private static final int EQUIP_V_GAP = 32;
  private static final int EQUIP_GRID_START_Y_OFFSET = 28;
  private static final int EQUIP_LABEL_Y_OFFSET = 6;
  private static final int EQUIP_SLOT_ROUND_RADIUS = 10;
  private static final int EQUIP_SLOT_INNER_INSET = 1;
  private static final int EQUIP_SLOT_INNER_REDUCTION = 2;
  private static final int EQUIP_ICON_MARGIN = 4;
  private static final int STAT_BAR_ROUND_RADIUS = 8;
  private static final int STAT_TEXT_X_OFFSET = 8;
  private static final int STAT_TEXT_Y_BOTTOM_OFFSET = 4;
  private static final int SHADOW_OFFSET = 1;

  private final UI ui;

  CharacterUi(UI ui) {
    this.ui = ui;
  }

  void drawCharacterScreen() {
    Graphics2D g2 = ui.g2;
    var gp = ui.getGamePanel();
    var player = gp.getPlayer();

    final int frameX = GamePanel.TILE_SIZE * FRAME_X_TILES;
    final int frameY = GamePanel.TILE_SIZE * FRAME_Y_TILES;
    final int frameWidth = GamePanel.TILE_SIZE * FRAME_WIDTH_TILES;
    final int frameHeight = GamePanel.TILE_SIZE * FRAME_HEIGHT_TILES;
    ui.drawSubWindow(frameX, frameY, frameWidth, frameHeight);

    g2.setColor(UI.COL_TEXT);
    g2.setFont(ui.fonts.pixelBold(30F));

    final int padding = BASE_PADDING;
    int textX = frameX + padding;
    int textY = frameY + padding;

    int titleOffsetY = TITLE_Y_OFFSET;
    ui.drawTextShadow(ui.t("ui.character.title"),
        textX,
        textY + titleOffsetY,
        UI.COL_TEXT,
        Color.black,
        2);
    textY += TITLE_BOTTOM_SPACING;

    int portraitSize = GamePanel.TILE_SIZE * PORTRAIT_SIZE_TILES;
    int portraitX = textX;
    int portraitY = textY;

    g2.setColor(COL_PORTRAIT_BG);
    g2.fillRoundRect(portraitX, portraitY, portraitSize, portraitSize, PORTRAIT_ROUND_RADIUS, PORTRAIT_ROUND_RADIUS);

    g2.drawImage(
        player.getDown1(),
        portraitX + portraitSize / 2 - GamePanel.TILE_SIZE,
        portraitY + portraitSize / 2 - GamePanel.TILE_SIZE,
        GamePanel.TILE_SIZE * 2,
        GamePanel.TILE_SIZE * 2,
        null);

    int infoX = portraitX + portraitSize + INFO_X_OFFSET;
    int infoY = portraitY + INFO_Y_OFFSET;

    g2.setFont(ui.fonts.pixelPlain(22F));
    String nivelLabel = ui.t("ui.character.levelPrefix") + " " + player.getLevel();
    ui.drawTextShadow(nivelLabel, infoX, infoY, UI.COL_TEXT, Color.black, 2);
    infoY += INFO_LINE_HEIGHT;

    int barWidth = GamePanel.TILE_SIZE * STAT_BAR_WIDTH_TILES;
    int barHeight = STAT_BAR_HEIGHT;

    drawStatBar(
        infoX,
        infoY,
        barWidth,
        barHeight,
        player.getLife(),
        player.getMaxLife(),
        ui.t("ui.stats.hp"));
    infoY += INFO_LINE_HEIGHT;

    drawStatBar(
        infoX,
        infoY,
        barWidth,
        barHeight,
        player.getMana(),
        player.getMaxMana(),
        ui.t("ui.stats.mp"));

    infoY += INFO_LINE_HEIGHT;
    drawStatBar(
        infoX,
        infoY,
        barWidth,
        barHeight,
        player.getExp(),
        player.getNextLevelExp(),
        ui.t("ui.stats.exp"));

    int statsBlockX = frameX + padding;
    int statsBlockY = portraitY + portraitSize + STATS_BLOCK_Y_OFFSET;

    g2.setFont(ui.fonts.pixelPlain(20F));
    int lineHeight = STATS_LINE_HEIGHT;

    String[] labels = {
        ui.t("ui.character.stat.strength"),
        ui.t("ui.character.stat.dexterity"),
        ui.t("ui.character.stat.attack"),
        ui.t("ui.character.stat.defense"),
        ui.t("ui.character.stat.coins")
    };

    int tailX = frameX + frameWidth / 2 - STATS_TAIL_X_OFFSET;

    for (int i = 0; i < labels.length; i++) {
      g2.setColor(UI.COL_TEXT_DIM);
      g2.drawString(labels[i], statsBlockX, statsBlockY);

      int base = 0;
      int bonus = 0;
      String valueText = "";
      boolean usesBonusFormat = false;

      switch (i) {
        case 0 -> {
          // Força: só base (por enquanto)
          base = player.getStrength();
          bonus = 0;
          usesBonusFormat = true;
        }
        case 1 -> {
          // Destreza: só base (por enquanto)
          base = player.getDexterity();
          bonus = 0;
          usesBonusFormat = true;
        }
        case 2 -> {
          // Ataque: base do personagem + bônus de equipamento
          base = player.getAttackBaseStat();
          bonus = player.getAttackEquipBonusStat();
          usesBonusFormat = true;
        }
        case 3 -> {
          // Defesa: base do personagem + bônus de equipamento
          base = player.getDefenseBaseStat();
          bonus = player.getDefenseEquipBonusStat();
          usesBonusFormat = true;
        }
        // Moedas: só o valor direto
        case 4 -> valueText = String.valueOf(player.getCoin());
        default -> { /* índices fora do intervalo — nenhuma ação necessária */ }
      }

      if (usesBonusFormat) {
        // Desenha "base" + "(+X)" com cores diferentes, alinhado à direita
        drawBaseAndBonusRightAligned(base, bonus, tailX, statsBlockY);
      } else {
        g2.setColor(UI.COL_TEXT);
        g2.drawString(
            valueText,
            ui.getXforAlignToRightText(valueText, tailX),
            statsBlockY);
      }

      statsBlockY += lineHeight;
    }

    int equipAreaPadding = EQUIP_AREA_PADDING;
    int equipAreaX = frameX + frameWidth / 2 + equipAreaPadding;
    int equipAreaRight = frameX + frameWidth - equipAreaPadding;
    int equipAreaWidth = equipAreaRight - equipAreaX;

    g2.setFont(ui.fonts.pixelBold(22F));
    int equipTitleY = portraitY + portraitSize + EQUIP_TITLE_Y_OFFSET;
    if (equipTitleY > statsBlockY - EQUIP_TITLE_COLLISION_PADDING) {
      equipTitleY = statsBlockY - EQUIP_TITLE_COLLISION_PADDING;
    }
    ui.drawTextShadow(ui.t("ui.character.equipmentsTitle"),
        equipAreaX,
        equipTitleY,
        UI.COL_TEXT,
        Color.black,
        2);

    int cols = EQUIP_GRID_COLS;
    int rows = EQUIP_GRID_ROWS;

    int slotSize = GamePanel.TILE_SIZE;
    int hGap = Math.max(EQUIP_MIN_H_GAP, (equipAreaWidth - cols * slotSize) / (cols - 1));
    int vGap = EQUIP_V_GAP;

    int gridStartY = equipTitleY + EQUIP_GRID_START_Y_OFFSET;

    String[] equipLabels = {
        ui.t("ui.character.equipment.helmet"),
        ui.t("ui.character.equipment.chest"),
        ui.t("ui.character.equipment.legs"),
        ui.t("ui.character.equipment.boots"),
        ui.t("ui.character.equipment.weapon"),
        ui.t("ui.character.equipment.shield"),
        ui.t("ui.character.equipment.gloves"),
        ui.t("ui.character.equipment.ring"),
        ui.t("ui.character.equipment.amulet")
    };

    Entity[] equipItems = {
        player.getCurrentHelmet(),
        player.getCurrentChest(),
        player.getCurrentLegs(),
        player.getCurrentBoots(),
        player.getCurrentWeapon(),
        player.getCurrentShield(),
        player.getCurrentGloves(),
        player.getCurrentRing(),
        player.getCurrentAmulet()
    };

    EquipmentGridLayout equipLayout = new EquipmentGridLayout(
        equipAreaX,
        gridStartY,
        slotSize,
        cols,
        rows,
        hGap,
        vGap);
    drawEquipmentGrid(equipLayout, equipLabels, equipItems);
  }

  private record EquipmentGridLayout(
      int startX,
      int startY,
      int slotSize,
      int cols,
      int rows,
      int hGap,
      int vGap) {
  }

  private void drawEquipmentGrid(
      EquipmentGridLayout layout,
      String[] labels,
      Entity[] items) {

    Graphics2D g2 = ui.g2;
    int index = 0;

    g2.setFont(ui.fonts.pixelPlain(16F));

    for (int row = 0; row < layout.rows(); row++) {
      for (int col = 0; col < layout.cols(); col++) {
        if (index >= labels.length || index >= items.length) {
          return;
        }

        int slotX = layout.startX() + col * (layout.slotSize() + layout.hGap());
        int slotY = layout.startY() + row * (layout.slotSize() + layout.vGap());

        g2.setColor(UI.COL_TEXT_DIM);
        g2.drawString(labels[index], slotX, slotY - EQUIP_LABEL_Y_OFFSET);

        int boxY = slotY;
        g2.setColor(UI.COL_PANEL_BORDER);
        g2.drawRoundRect(slotX, boxY, layout.slotSize(), layout.slotSize(), EQUIP_SLOT_ROUND_RADIUS, EQUIP_SLOT_ROUND_RADIUS);
        g2.setColor(COL_EQUIP_SLOT_BG);
        g2.fillRoundRect(
            slotX + EQUIP_SLOT_INNER_INSET,
            boxY + EQUIP_SLOT_INNER_INSET,
            layout.slotSize() - EQUIP_SLOT_INNER_REDUCTION,
            layout.slotSize() - EQUIP_SLOT_INNER_REDUCTION,
            EQUIP_SLOT_ROUND_RADIUS,
            EQUIP_SLOT_ROUND_RADIUS);

        Entity item = items[index];
        if (item != null && item.getDown1() != null) {
          int iconMargin = EQUIP_ICON_MARGIN;
          int iconSize = layout.slotSize() - iconMargin * 2;
          int iconX = slotX + iconMargin;
          int iconY = boxY + iconMargin;
          g2.drawImage(item.getDown1(), iconX, iconY, iconSize, iconSize, null);
        }

        index++;
      }
    }
  }

  private void drawStatBar(int x, int y, int width, int height, int current, int max, String label) {
    Graphics2D g2 = ui.g2;

    g2.setColor(UI.COL_PANEL_BORDER);
    g2.fillRoundRect(x, y, width, height, STAT_BAR_ROUND_RADIUS, STAT_BAR_ROUND_RADIUS);

    if (max > 0 && current > 0) {
      int filledWidth = (int) (width * (Math.min(current, max) / (double) max));
      g2.setColor(UI.COL_ACCENT);
      g2.fillRoundRect(x, y, filledWidth, height, STAT_BAR_ROUND_RADIUS, STAT_BAR_ROUND_RADIUS);
    }

    g2.setFont(ui.fonts.pixelPlain(18F));
    String text = label + " " + current + "/" + max;
    int textX = x + STAT_TEXT_X_OFFSET;
    int textY = y + height - STAT_TEXT_Y_BOTTOM_OFFSET;

    g2.setColor(Color.black);
    g2.drawString(text, textX + SHADOW_OFFSET, textY + SHADOW_OFFSET);
    g2.setColor(UI.COL_TEXT);
    g2.drawString(text, textX, textY);
  }

  /**
   * Desenha stats no padrão:
   * base -> "5"
   * base+bonus -> "5 (+2)"
   * com o bônus em outra cor, alinhado à direita em tailX.
   */
  private void drawBaseAndBonusRightAligned(int base, int bonus, int tailX, int y) {
    Graphics2D g2 = ui.g2;

    // Sem bônus: desenha só o base, normal
    if (bonus == 0) {
      String baseText = String.valueOf(base);
      int x = ui.getXforAlignToRightText(baseText, tailX);
      g2.setColor(UI.COL_TEXT);
      g2.drawString(baseText, x, y);
      return;
    }

    String sign = bonus > 0 ? "+" : "-";
    int absBonus = Math.abs(bonus);

    String baseText = String.valueOf(base) + " ";
    String bonusText = "(" + sign + absBonus + ")";

    // Usa o texto completo só para calcular o alinhamento à direita
    String fullText = baseText + bonusText;
    int fullX = ui.getXforAlignToRightText(fullText, tailX);

    var fm = g2.getFontMetrics();
    int baseWidth = fm.stringWidth(baseText);

    int baseX = fullX;
    int bonusX = fullX + baseWidth;

    // Base na cor normal
    g2.setColor(UI.COL_TEXT);
    g2.drawString(baseText, baseX, y);

    // Bônus em outra cor (aqui usei o accent)
    g2.setColor(UI.COL_ACCENT);
    g2.drawString(bonusText, bonusX, y);

    // volta pra cor padrão pra não vazar pro restante
    g2.setColor(UI.COL_TEXT);
  }

}

package ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

import entity.Entity;
import main.GamePanel;

class CharacterUi {

  private final UI ui;

  CharacterUi(UI ui) {
    this.ui = ui;
  }

  void drawCharacterScreen() {
    Graphics2D g2 = ui.g2;
    var gp = ui.getGamePanel();
    var player = gp.getPlayer();

    final int frameX = GamePanel.TILE_SIZE * 2;
    final int frameY = GamePanel.TILE_SIZE;
    final int frameWidth = GamePanel.TILE_SIZE * 9;
    final int frameHeight = GamePanel.TILE_SIZE * 10;
    ui.drawSubWindow(frameX, frameY, frameWidth, frameHeight);

    g2.setColor(UI.COL_TEXT);
    g2.setFont(g2.getFont().deriveFont(Font.BOLD, 30F));

    final int padding = 24;
    int textX = frameX + padding;
    int textY = frameY + padding;

    int titleOffsetY = 8;
    ui.drawTextShadow(ui.t("ui.character.title"),
        textX,
        textY + titleOffsetY,
        UI.COL_TEXT,
        Color.black,
        2);
    textY += 44;

    int portraitSize = GamePanel.TILE_SIZE * 3;
    int portraitX = textX;
    int portraitY = textY;

    g2.setColor(new Color(10, 10, 15, 190));
    g2.fillRoundRect(portraitX, portraitY, portraitSize, portraitSize, 12, 12);

    g2.drawImage(
        player.getDown1(),
        portraitX + portraitSize / 2 - GamePanel.TILE_SIZE,
        portraitY + portraitSize / 2 - GamePanel.TILE_SIZE,
        GamePanel.TILE_SIZE * 2,
        GamePanel.TILE_SIZE * 2,
        null);

    int infoX = portraitX + portraitSize + 24;
    int infoY = portraitY + 8;

    g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 22F));
    String nivelLabel = ui.t("ui.character.levelPrefix") + " " + player.getLevel();
    ui.drawTextShadow(nivelLabel, infoX, infoY, UI.COL_TEXT, Color.black, 2);
    infoY += 30;

    int barWidth = GamePanel.TILE_SIZE * 4;
    int barHeight = 16;

    drawStatBar(
        infoX,
        infoY,
        barWidth,
        barHeight,
        player.getLife(),
        player.getMaxLife(),
        ui.t("ui.stats.hp"));
    infoY += 30;

    drawStatBar(
        infoX,
        infoY,
        barWidth,
        barHeight,
        player.getMana(),
        player.getMaxMana(),
        ui.t("ui.stats.mp"));

    infoY += 30;
    drawStatBar(
        infoX,
        infoY,
        barWidth,
        barHeight,
        player.getExp(),
        player.getNextLevelExp(),
        ui.t("ui.stats.exp"));

    int statsBlockX = frameX + padding;
    int statsBlockY = portraitY + portraitSize + 40;

    g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 20F));
    int lineHeight = 24;

    String[] labels = {
        ui.t("ui.character.stat.strength"),
        ui.t("ui.character.stat.dexterity"),
        ui.t("ui.character.stat.attack"),
        ui.t("ui.character.stat.defense"),
        ui.t("ui.character.stat.coins")
    };

    int tailX = frameX + frameWidth / 2 - 30;

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
        case 4 -> {
          // Moedas: só o valor direto
          valueText = String.valueOf(player.getCoin());
        }
        default -> {
        }
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

    int equipAreaPadding = 16;
    int equipAreaX = frameX + frameWidth / 2 + equipAreaPadding;
    int equipAreaRight = frameX + frameWidth - equipAreaPadding;
    int equipAreaWidth = equipAreaRight - equipAreaX;

    g2.setFont(g2.getFont().deriveFont(Font.BOLD, 22F));
    int equipTitleY = portraitY + portraitSize + 32;
    if (equipTitleY > statsBlockY - 8) {
      equipTitleY = statsBlockY - 8;
    }
    ui.drawTextShadow(ui.t("ui.character.equipmentsTitle"),
        equipAreaX,
        equipTitleY,
        UI.COL_TEXT,
        Color.black,
        2);

    int cols = 3;
    int rows = 2;

    int slotSize = GamePanel.TILE_SIZE;
    int hGap = Math.max(12, (equipAreaWidth - cols * slotSize) / (cols - 1));
    int vGap = 32;

    int gridStartY = equipTitleY + 28;

    String[] equipLabels = {
        ui.t("ui.character.equipment.helmet"),
        ui.t("ui.character.equipment.chest"),
        ui.t("ui.character.equipment.legs"),
        ui.t("ui.character.equipment.boots"),
        ui.t("ui.character.equipment.weapon"),
        ui.t("ui.character.equipment.shield")
    };

    Entity[] equipItems = {
        player.getCurrentHelmet(),
        player.getCurrentChest(),
        player.getCurrentLegs(),
        player.getCurrentBoots(),
        player.getCurrentWeapon(),
        player.getCurrentShield()
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

    g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 16F));

    for (int row = 0; row < layout.rows(); row++) {
      for (int col = 0; col < layout.cols(); col++) {
        if (index >= labels.length || index >= items.length) {
          return;
        }

        int slotX = layout.startX() + col * (layout.slotSize() + layout.hGap());
        int slotY = layout.startY() + row * (layout.slotSize() + layout.vGap());

        g2.setColor(UI.COL_TEXT_DIM);
        g2.drawString(labels[index], slotX, slotY - 6);

        int boxY = slotY;
        g2.setColor(UI.COL_PANEL_BORDER);
        g2.drawRoundRect(slotX, boxY, layout.slotSize(), layout.slotSize(), 10, 10);
        g2.setColor(new Color(20, 24, 32, 220));
        g2.fillRoundRect(
            slotX + 1,
            boxY + 1,
            layout.slotSize() - 2,
            layout.slotSize() - 2,
            10,
            10);

        Entity item = items[index];
        if (item != null && item.getDown1() != null) {
          int iconMargin = 4;
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
    g2.fillRoundRect(x, y, width, height, 8, 8);

    if (max > 0 && current > 0) {
      int filledWidth = (int) (width * (Math.min(current, max) / (double) max));
      g2.setColor(UI.COL_ACCENT);
      g2.fillRoundRect(x, y, filledWidth, height, 8, 8);
    }

    g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 18F));
    String text = label + " " + current + "/" + max;
    int textX = x + 8;
    int textY = y + height - 4;

    g2.setColor(Color.black);
    g2.drawString(text, textX + 1, textY + 1);
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

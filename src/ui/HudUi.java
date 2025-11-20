package ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

import entity.Entity;
import main.GamePanel;

class HudUi {

  private final UI ui;

  HudUi(UI ui) {
    this.ui = ui;
  }

  void drawPlayerLife() {
    var gp = ui.getGamePanel();
    var player = gp.getPlayer();

    int barWidth = GamePanel.TILE_SIZE * 3;
    int barHeight = 24;

    int x = GamePanel.TILE_SIZE;
    int y = GamePanel.TILE_SIZE / 2;

    // HP
    HudBarLayout lifeLayout = new HudBarLayout(x, y, barWidth, barHeight);
    drawHudBar(
        lifeLayout,
        player.getLife(),
        player.getMaxLife(),
        ui.t("ui.stats.hp"),
        new Color(210, 70, 70));

    // MP
    y += barHeight + 8;
    HudBarLayout manaLayout = new HudBarLayout(x, y, barWidth, barHeight);
    drawHudBar(
        manaLayout,
        player.getMana(),
        player.getMaxMana(),
        ui.t("ui.stats.mp"),
        new Color(70, 120, 220));

    // Guard Stamina
    int guardMax = player.getMaxGuardStamina();
    if (guardMax > 0) {
      y += barHeight + 8;
      HudBarLayout guardLayout = new HudBarLayout(x, y, barWidth, barHeight);
      drawHudBar(
          guardLayout,
          player.getGuardStamina(),
          guardMax,
          ui.t("ui.stats.guard"),
          new Color(120, 200, 120));
    }
  }

  void drawXpTopBar() {
    Graphics2D g2 = ui.g2;
    var gp = ui.getGamePanel();
    var player = gp.getPlayer();

    int currentExp = player.getExp();
    int nextLevelExp = player.getNextLevelExp();

    if (nextLevelExp <= 0) {
      return;
    }

    double ratio = Math.min(1.0, currentExp / (double) nextLevelExp);

    int x = 0;
    int y = 0;
    int width = GamePanel.SCREEN_WIDTH;
    int height = 6;

    // Fundo da barra
    g2.setColor(new Color(5, 12, 30, 220));
    g2.fillRect(x, y, width, height);

    // Preenchimento de XP
    int filledWidth = (int) (width * ratio);
    if (filledWidth > 0) {
      g2.setColor(new Color(70, 140, 255));
      g2.fillRect(x, y, filledWidth, height);
    }

    g2.setColor(new Color(0, 0, 0, 160));
    g2.drawLine(x, y + height, x + width, y + height);
  }

  private record HudBarLayout(int x, int y, int width, int height) {
  }

  private void drawHudBar(
      HudBarLayout layout,
      int current,
      int max,
      String label,
      Color fillColor) {

    Graphics2D g2 = ui.g2;
    int x = layout.x();
    int y = layout.y();
    int width = layout.width();
    int height = layout.height();

    g2.setColor(new Color(0, 0, 0, 180));
    g2.fillRoundRect(x - 2, y - 2, width + 4, height + 4, 8, 8);

    var oldStroke = g2.getStroke();
    g2.setStroke(new BasicStroke(2f));
    g2.setColor(UI.COL_PANEL_BORDER);
    g2.drawRoundRect(x - 2, y - 2, width + 4, height + 4, 8, 8);
    g2.setStroke(oldStroke);

    if (max > 0 && current > 0) {
      int clamped = Math.min(current, max);
      int filledWidth = (int) (width * (clamped / (double) max));
      g2.setColor(fillColor);
      g2.fillRoundRect(x, y, filledWidth, height, 8, 8);
    }

    String text = label + " " + current + "/" + max;
    Font baseFont = ui.getMaruMonica() != null ? ui.getMaruMonica() : g2.getFont();
    Font hudFont = baseFont.deriveFont(Font.BOLD, 18F);
    g2.setFont(hudFont);

    var fm = g2.getFontMetrics();
    int textX = x + 8;
    int textY = y + (height - fm.getHeight()) / 2 + fm.getAscent();

    g2.setColor(Color.black);
    g2.drawString(text, textX + 1, textY + 1);
    g2.setColor(UI.COL_TEXT);
    g2.drawString(text, textX, textY);
  }

  void drawMonsterLife() {
    var gp = ui.getGamePanel();
    Graphics2D g2 = ui.g2;

    Entity[][] monsters = gp.getMonster();
    int currentMapIndex = gp.getCurrentMap();
    Entity[] currentMonsters = monsters[currentMapIndex];

    for (Entity monster : currentMonsters) {
      if (monster == null || !monster.inCamera()) {
        continue;
      }

      if (monster.isHpBarOn() && !monster.isBoss()) {
        double oneScale = (double) GamePanel.TILE_SIZE / monster.getMaxLife();
        double hpBarValue = oneScale * monster.getLife();

        g2.setColor(new Color(35, 35, 35));
        g2.fillRect(
            monster.getScreenX() - 1,
            monster.getScreenY() - 16,
            GamePanel.TILE_SIZE + 2,
            12);

        g2.setColor(new Color(255, 0, 30));
        g2.fillRect(
            monster.getScreenX(),
            monster.getScreenY() - 15,
            (int) hpBarValue,
            10);

        monster.setHpBarCounter(monster.getHpBarCounter() + 1);
        if (monster.getHpBarCounter() > 600) {
          monster.setHpBarCounter(0);
          monster.setHpBarOn(false);
        }
      } else if (monster.isBoss()) {
        double oneScale = (double) GamePanel.TILE_SIZE * 8 / monster.getMaxLife();
        double hpBarValue = oneScale * monster.getLife();

        int x = GamePanel.SCREEN_WIDTH / 2 - GamePanel.TILE_SIZE * 4;
        int y = GamePanel.TILE_SIZE * 10;

        g2.setColor(new Color(35, 35, 35));
        g2.fillRect(x - 1, y - 1, GamePanel.TILE_SIZE * 8 + 2, 22);

        g2.setColor(new Color(255, 0, 30));
        g2.fillRect(x, y, (int) hpBarValue, 20);

        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 24f));
        g2.setColor(Color.white);

        String bossName = monster.getDisplayName();
        g2.drawString(bossName, x + 4, y - 10);
      }
    }
  }

  void drawMessage() {
    Graphics2D g2 = ui.g2;
    int messageX = GamePanel.TILE_SIZE;
    int messageY = GamePanel.TILE_SIZE * 4;

    g2.setFont(g2.getFont().deriveFont(Font.BOLD, 32F));

    int i = 0;
    while (i < ui.message.size()) {
      String m = ui.message.get(i);
      if (m != null) {
        g2.setColor(Color.black);
        g2.drawString(m, messageX + 2, messageY + 2);
        g2.setColor(UI.COL_TEXT);
        g2.drawString(m, messageX, messageY);

        int msgCounter = ui.messageCounter.get(i) + 1;
        ui.messageCounter.set(i, msgCounter);
        messageY += 40;

        if (ui.messageCounter.get(i) > 180) {
          ui.message.remove(i);
          ui.messageCounter.remove(i);
          continue;
        }
      }
      i++;
    }
  }
}

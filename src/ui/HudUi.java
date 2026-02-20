package ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import entity.Entity;
import environment.WeatherType;
import main.GamePanel;
import pets.PetFactory;

class HudUi {

  private static final Color COL_HP = new Color(210, 70, 70);
  private static final Color COL_MP = new Color(70, 120, 220);
  private static final Color COL_GUARD = new Color(120, 200, 120);
  private static final Color COL_PANEL_BG = new Color(0, 0, 0, 180);
  private static final Color COL_PANEL_BG_DIM = new Color(0, 0, 0, 160);
  private static final Color COL_XP_BG = new Color(5, 12, 30, 220);
  private static final Color COL_XP_FILL = new Color(70, 140, 255);
  private static final Color COL_MONSTER_BAR_BG = new Color(35, 35, 35);
  private static final Color COL_MONSTER_BAR_FILL = new Color(255, 0, 30);

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
        COL_HP);

    // MP
    y += barHeight + 8;
    HudBarLayout manaLayout = new HudBarLayout(x, y, barWidth, barHeight);
    drawHudBar(
        manaLayout,
        player.getMana(),
        player.getMaxMana(),
        ui.t("ui.stats.mp"),
        COL_MP);

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
          COL_GUARD);
    }
  }

  /**
   * Mini HUD do pet:
   * - Ícone do pet ativo
   * - "Pet Lv. X"
   * - Barra de XP pequena
   */
  void drawPetHud() {
    var gp = ui.getGamePanel();
    var player = gp.getPlayer();
    var pet = player.getPet();

    if (pet == null) {
      return;
    }

    Graphics2D g2 = ui.g2;

    int barWidth = GamePanel.TILE_SIZE * 3;
    int barHeight = 24;
    int spacing = 8;

    int baseX = GamePanel.TILE_SIZE;
    int baseY = GamePanel.TILE_SIZE / 2;

    // Posiciona logo abaixo de HP/MP/(Guard)
    int y = baseY + (barHeight + spacing) * 2;
    if (player.getMaxGuardStamina() > 0) {
      y += barHeight + spacing;
    }

    int panelX = baseX;
    int panelY = y;
    int panelWidth = barWidth;
    int panelHeight = 22;

    // Fundo do painel
    g2.setColor(COL_PANEL_BG);
    g2.fillRoundRect(panelX - 2, panelY - 2, panelWidth + 4, panelHeight + 4, 8, 8);

    var oldStroke = g2.getStroke();
    g2.setStroke(new BasicStroke(2f));
    g2.setColor(UI.COL_PANEL_BORDER);
    g2.drawRoundRect(panelX - 2, panelY - 2, panelWidth + 4, panelHeight + 4, 8, 8);
    g2.setStroke(oldStroke);

    // Ícone do pet
    int iconSize = GamePanel.TILE_SIZE / 2;
    int iconX = panelX;
    int iconY = panelY + (panelHeight - iconSize) / 2;

    BufferedImage icon = null;
    try {
      String activePetId = player.getActivePetId();
      if (activePetId != null) {
        icon = PetFactory.getPetIcon(activePetId, gp, player);
      }
    } catch (Exception ignored) {
      // ignore: se falhar ao carregar o ícone, só não desenha nada
    }
    if (icon != null) {
      g2.drawImage(icon, iconX, iconY, iconSize, iconSize, null);
    }

    int textX = iconX + iconSize + 6;
    int textY = panelY + 14;

    int level = pet.getPetLevel();
    int exp = pet.getPetExp();
    int next = pet.getPetNextLevelExp();

    g2.setFont(ui.fonts.pixelBold(14F));
    g2.setColor(UI.COL_TEXT);

    String label = "Pet Lv. " + level;
    g2.drawString(label, textX, textY);

    // Barra de XP mini
    int xpBarWidth = panelWidth - (textX - panelX) - 8;
    int xpBarHeight = 6;
    int xpBarX = textX;
    int xpBarY = panelY + panelHeight - xpBarHeight - 4;

    double ratio = (next > 0) ? Math.min(1.0, exp / (double) next) : 0.0;
    int filledWidth = (int) (xpBarWidth * ratio);

    g2.setColor(COL_PANEL_BG_DIM);
    g2.fillRoundRect(xpBarX, xpBarY, xpBarWidth, xpBarHeight, 4, 4);

    if (filledWidth > 0) {
      g2.setColor(UI.COL_ACCENT);
      g2.fillRoundRect(xpBarX, xpBarY, filledWidth, xpBarHeight, 4, 4);
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
    g2.setColor(COL_XP_BG);
    g2.fillRect(x, y, width, height);

    // Preenchimento de XP
    int filledWidth = (int) (width * ratio);
    if (filledWidth > 0) {
      g2.setColor(COL_XP_FILL);
      g2.fillRect(x, y, filledWidth, height);
    }

    g2.setColor(COL_PANEL_BG_DIM);
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

    g2.setColor(COL_PANEL_BG);
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
    g2.setFont(ui.fonts.pixelBold(18F));

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
      if (monster != null && monster.inCamera()) {
        if (shouldHideMonsterBar(monster)) {
          monster.setHpBarOn(false);
          monster.setHpBarCounter(0);
        } else if (monster.isBoss()) {
          drawBossHpBar(g2, monster);
        } else if (monster.isHpBarOn()) {
          drawNormalHpBar(g2, monster);
        }
      }
    }
  }

  private static boolean shouldHideMonsterBar(Entity monster) {
    return !monster.isAlive()
        || monster.isDying()
        || (monster.getType() == Entity.TYPE_MONSTER && monster.getLife() <= 0);
  }

  private static void drawNormalHpBar(Graphics2D g2, Entity monster) {
    double oneScale = (double) GamePanel.TILE_SIZE / monster.getMaxLife();
    double hpBarValue = oneScale * monster.getLife();

    g2.setColor(COL_MONSTER_BAR_BG);
    g2.fillRect(
        monster.getScreenX() - 1,
        monster.getScreenY() - 16,
        GamePanel.TILE_SIZE + 2,
        12);

    g2.setColor(COL_MONSTER_BAR_FILL);
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
  }

  private void drawBossHpBar(Graphics2D g2, Entity monster) {
    double oneScale = (double) GamePanel.TILE_SIZE * 8 / monster.getMaxLife();
    double hpBarValue = oneScale * monster.getLife();

    int x = GamePanel.SCREEN_WIDTH / 2 - GamePanel.TILE_SIZE * 4;
    int y = GamePanel.TILE_SIZE * 10;

    g2.setColor(COL_MONSTER_BAR_BG);
    g2.fillRect(x - 1, y - 1, GamePanel.TILE_SIZE * 8 + 2, 22);

    g2.setColor(COL_MONSTER_BAR_FILL);
    g2.fillRect(x, y, (int) hpBarValue, 20);

    g2.setFont(ui.fonts.pixelBold(24F));
    g2.setColor(Color.white);

    String bossName = monster.getDisplayName();
    g2.drawString(bossName, x + 4, y - 10);
  }

  /** Indicador discreto do clima atual no canto superior direito. */
  void drawWeatherIndicator() {
    var gp = ui.getGamePanel();
    var wm = gp.getEnvironmentManager().getWeatherManager();
    if (wm == null) return;

    WeatherType type = wm.getCurrentType();
    if (type == WeatherType.CLEAR) return;

    Graphics2D g2 = ui.g2;
    String label = type.name();

    g2.setFont(ui.fonts.pixelBold(12F));
    g2.setColor(new Color(255, 255, 255, 140));
    var fm = g2.getFontMetrics();
    int x = GamePanel.SCREEN_WIDTH - fm.stringWidth(label) - 12;
    int y = 22;
    g2.drawString(label, x, y);
  }

  void drawMessage() {
    Graphics2D g2 = ui.g2;
    int messageX = GamePanel.TILE_SIZE;
    int messageY = GamePanel.TILE_SIZE * 4;

    g2.setFont(ui.fonts.pixelBold(32F));

    int i = 0;
    while (i < ui.message.size()) {
      String m = ui.message.get(i);
      boolean removed = false;
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
          removed = true;
        }
      }
      if (!removed) {
        i++;
      }
    }
  }
}

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
  private static final int HUD_BAR_WIDTH_TILES = 3;
  private static final int HUD_BAR_HEIGHT = 24;
  private static final int HUD_BAR_SPACING = 8;
  private static final int HUD_BASE_X_TILES = 1;
  private static final int HUD_BASE_Y_DIVISOR = 2;
  private static final int PET_HUD_VERTICAL_OFFSET_BARS = 2;
  private static final int PET_PANEL_HEIGHT = 22;
  private static final int PANEL_INSET = 2;
  private static final int PANEL_ROUND = 8;
  private static final int PET_ICON_TO_TEXT_GAP = 6;
  private static final int PET_TEXT_Y_OFFSET = 14;
  private static final int PET_XP_BAR_HEIGHT = 6;
  private static final int PET_XP_BAR_BOTTOM_PADDING = 4;
  private static final int PET_XP_BAR_ROUND = 4;
  private static final int TOP_XP_BAR_HEIGHT = 6;
  private static final int TEXT_PADDING_X = 8;
  private static final int SHADOW_OFFSET = 1;
  private static final int MONSTER_HP_BAR_Y_OFFSET_BG = 16;
  private static final int MONSTER_HP_BAR_BG_HEIGHT = 12;
  private static final int MONSTER_HP_BAR_FILL_Y_OFFSET = 15;
  private static final int MONSTER_HP_BAR_FILL_HEIGHT = 10;
  private static final int MONSTER_HP_BAR_TIMEOUT = 600;
  private static final int BOSS_HP_BAR_WIDTH_TILES = 8;
  private static final int BOSS_HP_BAR_HALF_WIDTH_TILES = 4;
  private static final int BOSS_HP_BAR_Y_TILES = 10;
  private static final int BOSS_HP_BAR_HEIGHT = 20;
  private static final int BOSS_HP_BAR_BG_HEIGHT = 22;
  private static final int BOSS_HP_NAME_X_OFFSET = 4;
  private static final int BOSS_HP_NAME_Y_OFFSET = 10;
  private static final int WEATHER_LABEL_X_PADDING = 12;
  private static final int WEATHER_LABEL_Y = 22;
  private static final int MESSAGE_START_X_TILES = 1;
  private static final int MESSAGE_START_Y_TILES = 4;
  private static final int MESSAGE_LINE_HEIGHT = 40;
  private static final int MESSAGE_DURATION_FRAMES = 180;
  private static final int CLOCK_FRAME_X_TILES = 1;
  private static final int CLOCK_FRAME_Y_TILES = 5;
  private static final int CLOCK_FRAME_WIDTH_TILES = 5;
  private static final int CLOCK_FRAME_HEIGHT_TILES = 2;
  private static final int CLOCK_PANEL_ROUND = 14;
  private static final int CLOCK_ICON_X_OFFSET = 10;
  private static final int CLOCK_ICON_Y_OFFSET = 24;
  private static final int CLOCK_TEXT_X_OFFSET = 10;
  private static final int CLOCK_TEXT_Y_OFFSET = 36;
  private static final int CLOCK_WEATHER_Y_OFFSET = 24;

  private final UI ui;
  private BufferedImage clockImage;

  HudUi(UI ui) {
    this.ui = ui;
  }

  void drawPlayerLife() {
    var gp = ui.getGamePanel();
    var player = gp.getPlayer();

    int barWidth = GamePanel.TILE_SIZE * HUD_BAR_WIDTH_TILES;
    int barHeight = HUD_BAR_HEIGHT;

    int x = GamePanel.TILE_SIZE * HUD_BASE_X_TILES;
    int y = GamePanel.TILE_SIZE / HUD_BASE_Y_DIVISOR;

    // HP
    HudBarLayout lifeLayout = new HudBarLayout(x, y, barWidth, barHeight);
    drawHudBar(
        lifeLayout,
        player.getLife(),
        player.getMaxLife(),
        ui.t("ui.stats.hp"),
        COL_HP);

    // MP
    y += barHeight + HUD_BAR_SPACING;
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
      y += barHeight + HUD_BAR_SPACING;
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

    int barWidth = GamePanel.TILE_SIZE * HUD_BAR_WIDTH_TILES;
    int barHeight = HUD_BAR_HEIGHT;
    int spacing = HUD_BAR_SPACING;

    int baseX = GamePanel.TILE_SIZE * HUD_BASE_X_TILES;
    int baseY = GamePanel.TILE_SIZE / HUD_BASE_Y_DIVISOR;

    // Posiciona logo abaixo de HP/MP/(Guard)
    int y = baseY + (barHeight + spacing) * PET_HUD_VERTICAL_OFFSET_BARS;
    if (player.getMaxGuardStamina() > 0) {
      y += barHeight + spacing;
    }

    int panelX = baseX;
    int panelY = y;
    int panelWidth = barWidth;
    int panelHeight = PET_PANEL_HEIGHT;

    // Fundo do painel
    g2.setColor(COL_PANEL_BG);
    g2.fillRoundRect(panelX - PANEL_INSET, panelY - PANEL_INSET, panelWidth + PANEL_INSET * 2,
        panelHeight + PANEL_INSET * 2, PANEL_ROUND, PANEL_ROUND);

    var oldStroke = g2.getStroke();
    g2.setStroke(new BasicStroke(2f));
    g2.setColor(UI.COL_PANEL_BORDER);
    g2.drawRoundRect(panelX - PANEL_INSET, panelY - PANEL_INSET, panelWidth + PANEL_INSET * 2,
        panelHeight + PANEL_INSET * 2, PANEL_ROUND, PANEL_ROUND);
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

    int textX = iconX + iconSize + PET_ICON_TO_TEXT_GAP;
    int textY = panelY + PET_TEXT_Y_OFFSET;

    int level = pet.getPetLevel();
    int exp = pet.getPetExp();
    int next = pet.getPetNextLevelExp();

    g2.setFont(ui.fonts.pixelBold(14F));
    g2.setColor(UI.COL_TEXT);

    String label = "Pet Lv. " + level;
    g2.drawString(label, textX, textY);

    // Barra de XP mini
    int xpBarWidth = panelWidth - (textX - panelX) - 8;
    int xpBarHeight = PET_XP_BAR_HEIGHT;
    int xpBarX = textX;
    int xpBarY = panelY + panelHeight - xpBarHeight - PET_XP_BAR_BOTTOM_PADDING;

    double ratio = (next > 0) ? Math.min(1.0, exp / (double) next) : 0.0;
    int filledWidth = (int) (xpBarWidth * ratio);

    g2.setColor(COL_PANEL_BG_DIM);
    g2.fillRoundRect(xpBarX, xpBarY, xpBarWidth, xpBarHeight, PET_XP_BAR_ROUND, PET_XP_BAR_ROUND);

    if (filledWidth > 0) {
      g2.setColor(UI.COL_ACCENT);
      g2.fillRoundRect(xpBarX, xpBarY, filledWidth, xpBarHeight, PET_XP_BAR_ROUND, PET_XP_BAR_ROUND);
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
    int height = TOP_XP_BAR_HEIGHT;

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
    g2.fillRoundRect(x - PANEL_INSET, y - PANEL_INSET, width + PANEL_INSET * 2, height + PANEL_INSET * 2,
        PANEL_ROUND, PANEL_ROUND);

    var oldStroke = g2.getStroke();
    g2.setStroke(new BasicStroke(2f));
    g2.setColor(UI.COL_PANEL_BORDER);
    g2.drawRoundRect(x - PANEL_INSET, y - PANEL_INSET, width + PANEL_INSET * 2, height + PANEL_INSET * 2,
        PANEL_ROUND, PANEL_ROUND);
    g2.setStroke(oldStroke);

    if (max > 0 && current > 0) {
      int clamped = Math.min(current, max);
      int filledWidth = (int) (width * (clamped / (double) max));
      g2.setColor(fillColor);
      g2.fillRoundRect(x, y, filledWidth, height, PANEL_ROUND, PANEL_ROUND);
    }

    String text = label + " " + current + "/" + max;
    g2.setFont(ui.fonts.pixelBold(18F));

    var fm = g2.getFontMetrics();
    int textX = x + TEXT_PADDING_X;
    int textY = y + (height - fm.getHeight()) / 2 + fm.getAscent();

    g2.setColor(Color.black);
    g2.drawString(text, textX + SHADOW_OFFSET, textY + SHADOW_OFFSET);
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
        monster.getScreenY() - MONSTER_HP_BAR_Y_OFFSET_BG,
        GamePanel.TILE_SIZE + 2,
        MONSTER_HP_BAR_BG_HEIGHT);

    g2.setColor(COL_MONSTER_BAR_FILL);
    g2.fillRect(
        monster.getScreenX(),
        monster.getScreenY() - MONSTER_HP_BAR_FILL_Y_OFFSET,
        (int) hpBarValue,
        MONSTER_HP_BAR_FILL_HEIGHT);

    monster.setHpBarCounter(monster.getHpBarCounter() + 1);
    if (monster.getHpBarCounter() > MONSTER_HP_BAR_TIMEOUT) {
      monster.setHpBarCounter(0);
      monster.setHpBarOn(false);
    }
  }

  private void drawBossHpBar(Graphics2D g2, Entity monster) {
    double oneScale = (double) GamePanel.TILE_SIZE * BOSS_HP_BAR_WIDTH_TILES / monster.getMaxLife();
    double hpBarValue = oneScale * monster.getLife();

    int x = GamePanel.SCREEN_WIDTH / 2 - GamePanel.TILE_SIZE * BOSS_HP_BAR_HALF_WIDTH_TILES;
    int y = GamePanel.TILE_SIZE * BOSS_HP_BAR_Y_TILES;

    g2.setColor(COL_MONSTER_BAR_BG);
    g2.fillRect(x - 1, y - 1, GamePanel.TILE_SIZE * BOSS_HP_BAR_WIDTH_TILES + 2, BOSS_HP_BAR_BG_HEIGHT);

    g2.setColor(COL_MONSTER_BAR_FILL);
    g2.fillRect(x, y, (int) hpBarValue, BOSS_HP_BAR_HEIGHT);

    g2.setFont(ui.fonts.pixelBold(24F));
    g2.setColor(Color.white);

    String bossName = monster.getDisplayName();
    g2.drawString(bossName, x + BOSS_HP_NAME_X_OFFSET, y - BOSS_HP_NAME_Y_OFFSET);
  }

  /** Indicador discreto do clima atual no canto superior direito. */
  void drawWeatherIndicator() {
    var gp = ui.getGamePanel();
    var wm = gp.getEnvironmentManager().getWeatherManager();
    if (wm == null)
      return;

    WeatherType type = wm.getCurrentType();
    if (type == WeatherType.CLEAR)
      return;

    Graphics2D g2 = ui.g2;
    String label = type.name();

    g2.setFont(ui.fonts.pixelBold(12F));
    g2.setColor(new Color(255, 255, 255, 140));
    var fm = g2.getFontMetrics();
    int x = GamePanel.SCREEN_WIDTH - fm.stringWidth(label) - WEATHER_LABEL_X_PADDING;
    int y = WEATHER_LABEL_Y;
    g2.drawString(label, x, y);
  }

  void drawMessage() {
    Graphics2D g2 = ui.g2;
    int messageX = GamePanel.TILE_SIZE * MESSAGE_START_X_TILES;
    int messageY = GamePanel.TILE_SIZE * MESSAGE_START_Y_TILES;

    g2.setFont(ui.fonts.pixelBold(32F));

    int size = ui.message.size();
    int write = 0;

    for (int read = 0; read < size; read++) {
      String message = ui.message.get(read);
      int counter = ui.messageCounter.get(read);
      boolean keep = true;

      if (message != null) {
        g2.setColor(Color.black);
        g2.drawString(message, messageX + 2, messageY + 2);
        g2.setColor(UI.COL_TEXT);
        g2.drawString(message, messageX, messageY);

        counter++;
        messageY += MESSAGE_LINE_HEIGHT;
        keep = counter <= MESSAGE_DURATION_FRAMES;
      }

      if (keep) {
        if (write != read) {
          ui.message.set(write, message);
          ui.messageCounter.set(write, counter);
        } else if (message != null) {
          ui.messageCounter.set(write, counter);
        }
        write++;
      }
    }

    if (write < size) {
      ui.message.subList(write, size).clear();
      ui.messageCounter.subList(write, size).clear();
    }
  }

  void drawClockWindow() {
    Graphics2D g2 = ui.g2;
    var gp = ui.getGamePanel();

    // Lazy load the clock image
    if (clockImage == null) {
      try {
        clockImage = new object.ObjClock(gp).getDown1();
      } catch (Exception ignored) {
        // Fallback or ignore
      }
    }

    int frameX = GamePanel.TILE_SIZE * CLOCK_FRAME_X_TILES;
    int frameY = GamePanel.TILE_SIZE * CLOCK_FRAME_Y_TILES;
    int frameWidth = GamePanel.TILE_SIZE * CLOCK_FRAME_WIDTH_TILES;
    int frameHeight = GamePanel.TILE_SIZE * CLOCK_FRAME_HEIGHT_TILES;

    int currentArea = gp.getCurrentArea();
    if (currentArea == GamePanel.OUTSIDE || currentArea == GamePanel.DUNGEON) {
      // Just visually separate
    }

    // Fundo do painel zeldiano
    g2.setColor(UI.COL_PANEL);
    g2.fillRoundRect(frameX, frameY, frameWidth, frameHeight, CLOCK_PANEL_ROUND, CLOCK_PANEL_ROUND);

    var oldStroke = g2.getStroke();
    g2.setStroke(new BasicStroke(3f));
    g2.setColor(UI.COL_PANEL_BORDER);
    g2.drawRoundRect(frameX, frameY, frameWidth, frameHeight, CLOCK_PANEL_ROUND, CLOCK_PANEL_ROUND);
    g2.setStroke(oldStroke);

    // Ícone do relógio
    if (clockImage != null) {
      g2.drawImage(clockImage, frameX + CLOCK_ICON_X_OFFSET, frameY + CLOCK_ICON_Y_OFFSET, null);
    }

    g2.setFont(ui.fonts.pixelBold(24F));
    g2.setColor(UI.COL_TEXT);

    // Hora do jogo
    var lighting = gp.getEnvironmentManager().getLighting();
    String timeStr = "--:--";
    if (lighting != null) {
      timeStr = lighting.getFormattedTime();
    }

    // Clima
    var weatherManager = gp.getEnvironmentManager().getWeatherManager();
    String weatherStr = "";
    if (weatherManager != null) {
      String weatherKey = "weather.type." + weatherManager.getCurrentType().name().toLowerCase();
      weatherStr = ui.t(weatherKey);
      // Fallback
      if (weatherStr.startsWith("weather.type.")) {
        weatherStr = weatherManager.getCurrentType().name();
      }
    }

    int textX = frameX + GamePanel.TILE_SIZE + CLOCK_TEXT_X_OFFSET;
    int textY = frameY + CLOCK_TEXT_Y_OFFSET;
    g2.drawString(timeStr, textX, textY);

    g2.setFont(ui.fonts.pixelPlain(18F));
    g2.drawString(weatherStr, textX, textY + CLOCK_WEATHER_Y_OFFSET);
  }
}

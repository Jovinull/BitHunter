package ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;

import achievement.AchievementCategory;
import achievement.AchievementDef;
import achievement.AchievementManager;
import achievement.AchievementRegistry;
import main.GamePanel;

/**
 * Tela de conquistas com tabs de categoria e lista de achievements.
 */
public class AchievementUi {

  private static final int FRAME_X = GamePanel.TILE_SIZE;
  private static final int FRAME_Y = GamePanel.TILE_SIZE;
  private static final int FRAME_W = GamePanel.SCREEN_WIDTH - GamePanel.TILE_SIZE * 2;
  private static final int FRAME_H = GamePanel.SCREEN_HEIGHT - GamePanel.TILE_SIZE * 2;

  private static final int TAB_H = 32;
  private static final int TAB_PAD = 6;
  private static final int ITEM_H = 60;
  private static final int ITEM_PAD = 4;
  private static final int ICON_SIZE = 48;
  private static final int ICON_PAD = 6;
  private static final int TOOLTIP_H = 60;
  private static final int SCROLL_AREA_TOP = FRAME_Y + TAB_H * 2 + TAB_PAD;
  private static final int SCROLL_AREA_BOTTOM = FRAME_Y + FRAME_H - TOOLTIP_H - ITEM_PAD;
  private static final int MAX_VISIBLE = (SCROLL_AREA_BOTTOM - SCROLL_AREA_TOP) / ITEM_H;

  private static final Color LOCKED_BG = new Color(40, 40, 50, 200);
  private static final Color LOCKED_ICON_TINT = new Color(0, 0, 0, 150);
  private static final Color SELECTED_BG = UiTheme.HIGHLIGHT_BG;
  private static final Color LINE_COLOR = new Color(0x8C, 0xA6, 0xDA, 120);

  private final UI ui;
  private int selectedCategoryIndex;
  private int selectedAchievementIndex;
  private int scrollOffset;

  AchievementUi(UI ui) {
    this.ui = ui;
  }

  public void resetState() {
    selectedCategoryIndex = 0;
    selectedAchievementIndex = 0;
    scrollOffset = 0;
  }

  // ===== Getters para KeyHandler =====

  public int getCategoryCount() {
    return AchievementCategory.values().length;
  }

  public int getAchievementCount() {
    return getCurrentAchievements().size();
  }

  public int getSelectedCategoryIndex() {
    return selectedCategoryIndex;
  }

  public void setSelectedCategoryIndex(int idx) {
    this.selectedCategoryIndex = idx;
  }

  public int getSelectedAchievementIndex() {
    return selectedAchievementIndex;
  }

  public void setSelectedAchievementIndex(int idx) {
    this.selectedAchievementIndex = idx;
    ensureVisible();
  }

  public void navigateCategory(int delta) {
    int count = getCategoryCount();
    selectedCategoryIndex = (selectedCategoryIndex + delta + count) % count;
    selectedAchievementIndex = 0;
    scrollOffset = 0;
  }

  public void navigateAchievement(int delta) {
    int count = getAchievementCount();
    if (count <= 0) return;
    selectedAchievementIndex = (selectedAchievementIndex + delta + count) % count;
    ensureVisible();
  }

  private void ensureVisible() {
    if (selectedAchievementIndex < scrollOffset) {
      scrollOffset = selectedAchievementIndex;
    } else if (selectedAchievementIndex >= scrollOffset + MAX_VISIBLE) {
      scrollOffset = selectedAchievementIndex - MAX_VISIBLE + 1;
    }
  }

  // ===== Desenho =====

  void drawAchievementScreen() {
    Graphics2D g2 = ui.g2;
    AchievementManager manager = ui.getGamePanel().getAchievementManager();

    // Fundo
    g2.setColor(UiTheme.BG_DARK);
    g2.fillRect(0, 0, GamePanel.SCREEN_WIDTH, GamePanel.SCREEN_HEIGHT);

    // Frame principal
    ui.drawSubWindow(FRAME_X, FRAME_Y, FRAME_W, FRAME_H);

    // Titulo
    Font oldFont = g2.getFont();
    g2.setFont(oldFont.deriveFont(Font.BOLD, 16F));
    g2.setColor(UiTheme.ACCENT);
    String title = ui.t("achievement.title");
    String countText = " (" + manager.getUnlockedCount() + "/" + manager.getTotalCount() + ")";
    g2.drawString(title + countText, FRAME_X + 16, FRAME_Y + 22);

    // Tabs de categoria
    drawCategoryTabs(g2, manager);

    // Lista de conquistas
    drawAchievementList(g2, manager);

    // Tooltip
    drawTooltip(g2, manager);

    // Hint de navegacao
    g2.setFont(oldFont.deriveFont(Font.PLAIN, 11F));
    g2.setColor(UiTheme.TEXT_DIM);
    String hint = ui.t("achievement.hint.nav");
    g2.drawString(hint, FRAME_X + 16, FRAME_Y + FRAME_H - 6);

    g2.setFont(oldFont);
  }

  private void drawCategoryTabs(Graphics2D g2, AchievementManager manager) {
    AchievementCategory[] categories = AchievementCategory.values();
    int tabY = FRAME_Y + TAB_H;
    int tabStartX = FRAME_X + 12;

    Font oldFont = g2.getFont();
    g2.setFont(oldFont.deriveFont(Font.PLAIN, 12F));

    for (int i = 0; i < categories.length; i++) {
      AchievementCategory cat = categories[i];
      String catName = ui.t(cat.getTranslationKey());
      int unlocked = manager.getUnlockedCountForCategory(cat);
      int total = AchievementRegistry.getByCategory(cat).size();
      String label = catName + " " + unlocked + "/" + total;

      int textW = g2.getFontMetrics().stringWidth(label) + 12;
      int tabX = tabStartX;

      boolean selected = (i == selectedCategoryIndex);

      if (selected) {
        g2.setColor(UiTheme.PANEL_MID);
        g2.fillRoundRect(tabX, tabY - 2, textW, TAB_H - 4, 6, 6);
        g2.setColor(UiTheme.BORDER_LIGHT);
        g2.drawRoundRect(tabX, tabY - 2, textW, TAB_H - 4, 6, 6);
        g2.setColor(UiTheme.ACCENT);
      } else {
        g2.setColor(UiTheme.TEXT_DIM);
      }

      g2.drawString(label, tabX + 6, tabY + 16);
      tabStartX += textW + 6;
    }

    g2.setFont(oldFont);
  }

  private void drawAchievementList(Graphics2D g2, AchievementManager manager) {
    List<AchievementDef> achievements = getCurrentAchievements();
    if (achievements.isEmpty()) return;

    int startY = SCROLL_AREA_TOP;

    for (int i = scrollOffset; i < Math.min(achievements.size(), scrollOffset + MAX_VISIBLE); i++) {
      AchievementDef def = achievements.get(i);
      boolean isUnlocked = manager.isUnlocked(def.id());
      boolean isSelected = (i == selectedAchievementIndex);

      int itemY = startY + (i - scrollOffset) * ITEM_H;

      // Fundo do item
      if (isSelected) {
        g2.setColor(SELECTED_BG);
        g2.fillRoundRect(FRAME_X + 12, itemY, FRAME_W - 24, ITEM_H - ITEM_PAD, 6, 6);
        g2.setColor(UiTheme.BORDER_LIGHT);
        g2.drawRoundRect(FRAME_X + 12, itemY, FRAME_W - 24, ITEM_H - ITEM_PAD, 6, 6);
      }

      // Icone
      int iconX = FRAME_X + 16;
      int iconY = itemY + (ITEM_H - ITEM_PAD - ICON_SIZE) / 2;

      BufferedImage icon = manager.getIcon(def);
      if (icon != null) {
        g2.drawImage(icon, iconX, iconY, ICON_SIZE, ICON_SIZE, null);
        if (!isUnlocked) {
          g2.setColor(LOCKED_ICON_TINT);
          g2.fillRect(iconX, iconY, ICON_SIZE, ICON_SIZE);
        }
      } else {
        g2.setColor(isUnlocked ? UiTheme.PANEL_MID : LOCKED_BG);
        g2.fillRect(iconX, iconY, ICON_SIZE, ICON_SIZE);
      }

      // Borda do icone
      g2.setColor(isUnlocked ? UiTheme.BORDER_LIGHT : UiTheme.BORDER_SHADOW);
      g2.drawRect(iconX, iconY, ICON_SIZE, ICON_SIZE);

      // Texto
      int textX = iconX + ICON_SIZE + ICON_PAD;
      Font oldFont = g2.getFont();

      g2.setFont(oldFont.deriveFont(Font.BOLD, 13F));
      g2.setColor(isUnlocked ? UiTheme.TEXT : UiTheme.TEXT_DIM);
      String name = ui.t(def.nameKey());
      g2.drawString(name, textX, itemY + 22);

      g2.setFont(oldFont.deriveFont(Font.PLAIN, 11F));
      g2.setColor(isUnlocked ? UiTheme.TEXT_DIM : new Color(80, 80, 100));
      String desc = ui.t(def.descKey());
      g2.drawString(desc, textX, itemY + 40);

      // Linha de conexao com pre-requisito
      if (def.prerequisiteId() != null) {
        g2.setColor(LINE_COLOR);
        g2.drawLine(iconX - 4, iconY + ICON_SIZE / 2, iconX, iconY + ICON_SIZE / 2);
      }

      g2.setFont(oldFont);
    }
  }

  private void drawTooltip(Graphics2D g2, AchievementManager manager) {
    List<AchievementDef> achievements = getCurrentAchievements();
    if (achievements.isEmpty() || selectedAchievementIndex >= achievements.size()) return;

    AchievementDef def = achievements.get(selectedAchievementIndex);
    boolean isUnlocked = manager.isUnlocked(def.id());

    int tooltipY = FRAME_Y + FRAME_H - TOOLTIP_H - 16;
    int tooltipX = FRAME_X + 12;
    int tooltipW = FRAME_W - 24;

    g2.setColor(UiTheme.PANEL_MID);
    g2.fillRoundRect(tooltipX, tooltipY, tooltipW, TOOLTIP_H, 6, 6);
    g2.setColor(UiTheme.BORDER_SHADOW);
    g2.drawRoundRect(tooltipX, tooltipY, tooltipW, TOOLTIP_H, 6, 6);

    Font oldFont = g2.getFont();
    g2.setFont(oldFont.deriveFont(Font.BOLD, 13F));
    g2.setColor(UiTheme.ACCENT);
    g2.drawString(ui.t(def.nameKey()), tooltipX + 8, tooltipY + 20);

    g2.setFont(oldFont.deriveFont(Font.PLAIN, 11F));
    g2.setColor(UiTheme.TEXT_DIM);
    g2.drawString(ui.t(def.descKey()), tooltipX + 8, tooltipY + 38);

    String status = isUnlocked
        ? ui.t("achievement.status.unlocked")
        : ui.t("achievement.status.locked");
    g2.setColor(isUnlocked ? UiTheme.ACCENT : UiTheme.TEXT_DIM);
    int statusW = g2.getFontMetrics().stringWidth(status);
    g2.drawString(status, tooltipX + tooltipW - statusW - 8, tooltipY + 20);

    g2.setFont(oldFont);
  }

  private List<AchievementDef> getCurrentAchievements() {
    AchievementCategory[] categories = AchievementCategory.values();
    if (selectedCategoryIndex < 0 || selectedCategoryIndex >= categories.length) {
      return List.of();
    }
    return AchievementRegistry.getByCategory(categories[selectedCategoryIndex]);
  }
}

package ui;

import java.awt.Color;
import java.awt.Graphics2D;

import data.GameStatistics;
import data.PlayTimeTracker;
import data.SaveManager.SlotInfo;
import main.GamePanel;

/**
 * Tela de estatísticas de um slot de save, acessível a partir da
 * seleção de slots. Exibe tempo de jogo e métricas acumuladas.
 */
class StatsUi {

  private static final int PADDING = 20;
  private static final int LINE_HEIGHT = 32;
  private static final int HEADER_FONT_SIZE = 36;
  private static final int BODY_FONT_SIZE = 22;
  private static final int HINT_FONT_SIZE = 16;
  private static final Color COL_OVERLAY = new Color(0, 0, 0, 200);
  private static final Color COL_VALUE = new Color(0x8B, 0xCF, 0xFF);

  private final UI ui;

  StatsUi(UI ui) {
    this.ui = ui;
  }

  /**
   * Desenha a tela de estatísticas sobre a seleção de slots.
   *
   * @param slotIndex índice do slot (0-based) cujas stats serão exibidas
   */
  void drawStatsOverlay(int slotIndex) {
    Graphics2D g2 = ui.g2;

    SlotInfo[] infos = ui.getSaveSlotUi().getSlotInfos();
    if (slotIndex < 0 || slotIndex >= infos.length) {
      return;
    }

    SlotInfo info = infos[slotIndex];
    if (!info.occupied() || info.corrupt()) {
      return;
    }

    // Fundo escuro
    g2.setColor(COL_OVERLAY);
    g2.fillRect(0, 0, GamePanel.SCREEN_WIDTH, GamePanel.SCREEN_HEIGHT);

    // Painel central
    int panelW = (int) (GamePanel.SCREEN_WIDTH * 0.75);
    int panelH = (int) (GamePanel.SCREEN_HEIGHT * 0.80);
    int panelX = (GamePanel.SCREEN_WIDTH - panelW) / 2;
    int panelY = (GamePanel.SCREEN_HEIGHT - panelH) / 2;
    ui.drawSubWindow(panelX, panelY, panelW, panelH);

    int contentX = panelX + PADDING;
    int contentRight = panelX + panelW - PADDING;
    int y = panelY + PADDING + LINE_HEIGHT;

    // Título
    g2.setFont(ui.fonts.displayBold((float) HEADER_FONT_SIZE));
    g2.setColor(UI.COL_ACCENT);
    String title = ui.t("ui.stats.title");
    int titleW = (int) g2.getFontMetrics().getStringBounds(title, g2).getWidth();
    g2.drawString(title, (GamePanel.SCREEN_WIDTH - titleW) / 2, y);
    y += LINE_HEIGHT / 2;

    // Nome do jogador e classe
    g2.setFont(ui.fonts.displayPlain((float) BODY_FONT_SIZE));
    y += LINE_HEIGHT;
    String playerLabel = info.playerName();
    if (playerLabel == null || playerLabel.isBlank()) {
      playerLabel = "???";
    }
    String className = getClassDisplayName(info.characterClass());
    g2.setColor(UI.COL_TEXT);
    g2.drawString(playerLabel + "  —  " + className, contentX, y);

    // Separador
    y += LINE_HEIGHT / 2;
    g2.setColor(UI.COL_PANEL_BORDER);
    g2.drawLine(contentX, y, contentRight, y);
    y += LINE_HEIGHT;

    // Tempo de jogo (destaque especial)
    drawStatLine(g2, contentX, contentRight, y,
        ui.t("ui.stats.playTime"),
        PlayTimeTracker.formatPlayTime(info.totalPlayTimeSeconds()));
    y += LINE_HEIGHT;

    // Separador fino
    y += 4;
    g2.setColor(UI.COL_PANEL_BORDER);
    g2.drawLine(contentX, y, contentRight, y);
    y += LINE_HEIGHT - 4;

    // Estatísticas do mapa
    var stats = info.gameStats();

    for (String key : GameStatistics.DISPLAY_ORDER) {
      long value = stats.getOrDefault(key, 0L);
      String label = ui.t("ui.stats." + key);
      drawStatLine(g2, contentX, contentRight, y, label, formatStatValue(value));
      y += LINE_HEIGHT;
    }

    // Hint de navegação
    y = panelY + panelH - PADDING;
    ui.useCrispPixelText(g2);
    g2.setFont(ui.fonts.pixelPlain((float) HINT_FONT_SIZE));
    String hint = ui.t("ui.stats.hint");
    int hintW = (int) g2.getFontMetrics().getStringBounds(hint, g2).getWidth();
    g2.setColor(UI.COL_TEXT_DIM);
    g2.drawString(hint, (GamePanel.SCREEN_WIDTH - hintW) / 2, y);
  }

  private void drawStatLine(Graphics2D g2, int leftX, int rightX, int y,
      String label, String value) {
    g2.setFont(ui.fonts.displayPlain((float) BODY_FONT_SIZE));
    g2.setColor(UI.COL_TEXT);
    g2.drawString(label, leftX, y);

    g2.setColor(COL_VALUE);
    int vw = (int) g2.getFontMetrics().getStringBounds(value, g2).getWidth();
    g2.drawString(value, rightX - vw, y);
  }

  private String getClassDisplayName(int classIndex) {
    return switch (classIndex) {
      case 0 -> ui.t("ui.title.class.warrior");
      case 1 -> ui.t("ui.title.class.thief");
      case 2 -> ui.t("ui.title.class.wizard");
      default -> "???";
    };
  }

  private String formatStatValue(long value) {
    if (value >= 1_000_000) {
      return String.format("%.1fM", value / 1_000_000.0);
    } else if (value >= 10_000) {
      return String.format("%.1fK", value / 1_000.0);
    }
    return String.valueOf(value);
  }
}

package ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import main.GamePanel;

class PauseUi {

  /** Frames parado antes de iniciar a animação DVD (~30 s a 60 fps). */
  private static final int IDLE_FRAMES_BEFORE_ANIM = 1800;

  /** Velocidade de deslocamento (pixels por frame). */
  private static final int SPEED = 2;

  /** Margem mínima das bordas da tela. */
  private static final int MARGIN = 10;

  private final UI ui;

  // Estado da animação DVD
  private int idleFrames;
  private boolean animActive;
  private int pauseX;
  private int pauseY;
  private int vx = SPEED;
  private int vy = SPEED;

  PauseUi(UI ui) {
    this.ui = ui;
  }

  /** Reseta a animação para o estado inicial (chamado ao entrar em pause). */
  void reset() {
    idleFrames = 0;
    animActive = false;
    pauseX = -1;
    pauseY = -1;
    vx = SPEED;
    vy = SPEED;
  }

  void drawPauseScreen() {
    Graphics2D g2 = ui.g2;

    // Salva fonte anterior para restaurar ao final
    Font prevFont = g2.getFont();

    // Configura renderização suave apenas para o texto PAUSE
    ui.useSmoothUiText(g2);
    g2.setFont(ui.fonts.displayPlain(80F));

    String text = ui.t("ui.pause.title");

    if (!animActive) {
      drawIdlePhase(g2, text);
    } else {
      drawDvdPhase(g2, text);
    }

    // Restaura renderização crisp pixel e fonte anterior
    ui.useCrispPixelText(g2);
    g2.setFont(prevFont);
  }

  /** Fase estática: sub-window centralizada + texto PAUSE no centro. */
  private void drawIdlePhase(Graphics2D g2, String text) {
    idleFrames++;

    int w = (int) (GamePanel.SCREEN_WIDTH * 0.6);
    int h = GamePanel.TILE_SIZE * 4;
    int x = (GamePanel.SCREEN_WIDTH - w) / 2;
    int y = (GamePanel.SCREEN_HEIGHT - h) / 2;

    ui.drawSubWindow(x, y, w, h);
    ui.drawTextShadowCentered(text, y + (int) (h * 0.62), UI.COL_TEXT, Color.black, 3);

    if (idleFrames >= IDLE_FRAMES_BEFORE_ANIM) {
      animActive = true;
      // Captura posição atual do texto como ponto de partida da animação
      FontMetrics fm = g2.getFontMetrics();
      int textW = fm.stringWidth(text);
      pauseX = (GamePanel.SCREEN_WIDTH - textW) / 2;
      pauseY = y + (int) (h * 0.62);
    }
  }

  /** Fase DVD: texto quicando nas bordas da tela. */
  private void drawDvdPhase(Graphics2D g2, String text) {
    FontMetrics fm = g2.getFontMetrics();
    int textW = fm.stringWidth(text);
    int ascent = fm.getAscent();
    int descent = fm.getDescent();

    pauseX += vx;
    pauseY += vy;

    int screenW = GamePanel.SCREEN_WIDTH;
    int screenH = GamePanel.SCREEN_HEIGHT;

    // Colisão horizontal
    if (pauseX <= MARGIN) {
      pauseX = MARGIN;
      vx = Math.abs(vx);
    } else if (pauseX + textW >= screenW - MARGIN) {
      pauseX = screenW - MARGIN - textW;
      vx = -Math.abs(vx);
    }

    // Colisão vertical (y é baseline; topo = y - ascent, base = y + descent)
    if (pauseY - ascent <= MARGIN) {
      pauseY = MARGIN + ascent;
      vy = Math.abs(vy);
    } else if (pauseY + descent >= screenH - MARGIN) {
      pauseY = screenH - MARGIN - descent;
      vy = -Math.abs(vy);
    }

    ui.drawTextShadow(text, pauseX, pauseY, UI.COL_TEXT, Color.black, 3);
  }
}

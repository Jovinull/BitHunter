package achievement;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import main.GamePanel;
import main.LanguageManager;

/**
 * Renderizador do toast popup de conquista desbloqueada.
 * <p>
 * State machine: IDLE -> SLIDING_IN (15 frames) -> DISPLAYING (240 frames)
 * -> SLIDING_OUT (15 frames) -> IDLE
 * </p>
 */
public final class AchievementToast {

  private static final int SLIDE_IN_FRAMES = 15;
  private static final int DISPLAY_FRAMES = 240;
  private static final int SLIDE_OUT_FRAMES = 15;

  private static final int TOAST_W = 300;
  private static final int TOAST_H = 64;
  private static final int ICON_SIZE = 48;
  private static final int ICON_PAD = 8;
  private static final int TEXT_X_OFFSET = ICON_PAD + ICON_SIZE + 8;
  private static final int MARGIN_TOP = 8;
  private static final int MARGIN_RIGHT = 8;

  private static final Color BG_COLOR = new Color(0x0D, 0x1A, 0x46, 235);
  private static final Color BORDER_COLOR = new Color(0xF3, 0xF7, 0xFF);
  private static final Color HEADER_COLOR = new Color(0x4F, 0xD1, 0xC5);
  private static final Color TITLE_COLOR = new Color(0xF4, 0xF5, 0xFB);

  private enum State { IDLE, SLIDING_IN, DISPLAYING, SLIDING_OUT }

  private State state = State.IDLE;
  private int frameCounter;
  private AchievementDef currentDef;
  private BufferedImage currentIcon;

  /**
   * Atualiza o estado do toast. Deve ser chamado a cada frame em PLAY_STATE.
   */
  public void update(AchievementManager manager) {
    switch (state) {
      case IDLE -> {
        AchievementDef next = manager.pollToast();
        if (next != null) {
          currentDef = next;
          currentIcon = manager.getIcon(next);
          state = State.SLIDING_IN;
          frameCounter = 0;
        }
      }
      case SLIDING_IN -> {
        frameCounter++;
        if (frameCounter >= SLIDE_IN_FRAMES) {
          state = State.DISPLAYING;
          frameCounter = 0;
        }
      }
      case DISPLAYING -> {
        frameCounter++;
        if (frameCounter >= DISPLAY_FRAMES) {
          state = State.SLIDING_OUT;
          frameCounter = 0;
        }
      }
      case SLIDING_OUT -> {
        frameCounter++;
        if (frameCounter >= SLIDE_OUT_FRAMES) {
          state = State.IDLE;
          currentDef = null;
          currentIcon = null;
        }
      }
    }
  }

  /**
   * Desenha o toast na tela (chamado durante o draw do HUD).
   */
  public void draw(Graphics2D g2, LanguageManager lm) {
    if (state == State.IDLE || currentDef == null) return;

    int baseX = GamePanel.SCREEN_WIDTH - TOAST_W - MARGIN_RIGHT;
    int baseY = MARGIN_TOP;

    // Calcula offset de slide horizontal
    int offsetX = switch (state) {
      case SLIDING_IN -> {
        float t = (float) frameCounter / SLIDE_IN_FRAMES;
        yield (int) ((1.0f - t) * (TOAST_W + MARGIN_RIGHT));
      }
      case SLIDING_OUT -> {
        float t = (float) frameCounter / SLIDE_OUT_FRAMES;
        yield (int) (t * (TOAST_W + MARGIN_RIGHT));
      }
      default -> 0;
    };

    int x = baseX + offsetX;
    int y = baseY;

    // Fundo
    g2.setColor(BG_COLOR);
    g2.fillRoundRect(x, y, TOAST_W, TOAST_H, 8, 8);

    // Borda
    g2.setColor(BORDER_COLOR);
    g2.drawRoundRect(x, y, TOAST_W, TOAST_H, 8, 8);

    // Icone
    if (currentIcon != null) {
      g2.drawImage(currentIcon, x + ICON_PAD, y + (TOAST_H - ICON_SIZE) / 2,
          ICON_SIZE, ICON_SIZE, null);
    }

    // Header
    Font oldFont = g2.getFont();
    g2.setFont(oldFont.deriveFont(Font.PLAIN, 11F));
    g2.setColor(HEADER_COLOR);
    String header = lm.text("achievement.toast.header");
    g2.drawString(header, x + TEXT_X_OFFSET, y + 22);

    // Titulo da conquista
    g2.setFont(oldFont.deriveFont(Font.BOLD, 13F));
    g2.setColor(TITLE_COLOR);
    String title = lm.text(currentDef.nameKey());
    g2.drawString(title, x + TEXT_X_OFFSET, y + 44);

    g2.setFont(oldFont);
  }
}

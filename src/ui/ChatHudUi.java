package ui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;

import main.GamePanel;
import network.ChatManager;
import network.ChatManager.ChatEntry;

/**
 * Overlay de chat na HUD do jogo.
 * <p>
 * Mostra mensagens recentes no canto inferior esquerdo e,
 * quando o input está ativo, exibe o campo de digitação.
 */
final class ChatHudUi {

  private static final int CHAT_X = 10;
  private static final int CHAT_BOTTOM_MARGIN = 60;
  private static final int LINE_HEIGHT = 18;
  private static final int INPUT_HEIGHT = 24;
  private static final int INPUT_PADDING = 6;
  private static final Color BG_COLOR = new Color(0, 0, 0, 140);
  private static final Color INPUT_BG = new Color(0, 0, 0, 180);
  private static final Color NAME_COLOR = new Color(120, 200, 255);
  private static final Color MSG_COLOR = new Color(230, 230, 230);
  private static final Color INPUT_COLOR = Color.WHITE;
  private static final Color CURSOR_COLOR = new Color(255, 255, 255, 180);

  private final UI ui;

  ChatHudUi(UI ui) {
    this.ui = ui;
  }

  /**
   * Desenha o overlay de chat. Deve ser chamado durante o draw do PLAY_STATE.
   */
  void draw() {
    GamePanel gp = ui.getGamePanel();
    ChatManager cm = gp.getChatManager();
    if (cm == null) {
      return;
    }

    Graphics2D g2 = ui.g2;
    g2.setFont(ui.fonts.pixelPlain(13F));

    int baseY = GamePanel.SCREEN_HEIGHT - CHAT_BOTTOM_MARGIN;

    // Campo de input (quando ativo)
    if (cm.isInputActive()) {
      drawInputField(g2, cm, baseY);
      baseY -= INPUT_HEIGHT + 4;
    }

    // Mensagens recentes
    List<ChatEntry> recent = cm.getRecentMessages();
    if (recent.isEmpty() && !cm.isInputActive()) {
      return;
    }

    // Fundo semi-transparente para as mensagens
    if (!recent.isEmpty()) {
      int bgHeight = recent.size() * LINE_HEIGHT + 8;
      int bgY = baseY - bgHeight;
      g2.setColor(BG_COLOR);
      g2.fillRoundRect(CHAT_X - 4, bgY, 350, bgHeight + 4, 6, 6);

      // Desenha cada mensagem
      int y = baseY - (recent.size() - 1) * LINE_HEIGHT;
      for (ChatEntry entry : recent) {
        String namePrefix = entry.senderName() + ": ";
        // Nome em cor diferente
        g2.setColor(NAME_COLOR);
        g2.drawString(namePrefix, CHAT_X, y);
        int nameWidth = g2.getFontMetrics().stringWidth(namePrefix);
        // Mensagem
        g2.setColor(MSG_COLOR);
        g2.drawString(entry.message(), CHAT_X + nameWidth, y);
        y += LINE_HEIGHT;
      }
    }
  }

  private void drawInputField(Graphics2D g2, ChatManager cm, int baseY) {
    int inputY = baseY - INPUT_HEIGHT;
    g2.setColor(INPUT_BG);
    g2.fillRoundRect(CHAT_X - 4, inputY, 350, INPUT_HEIGHT, 6, 6);

    g2.setColor(INPUT_COLOR);
    String text = "> " + cm.getInputText();
    g2.drawString(text, CHAT_X, inputY + INPUT_HEIGHT - INPUT_PADDING);

    // Cursor piscante
    if ((System.currentTimeMillis() / 500) % 2 == 0) {
      int cursorX = CHAT_X + g2.getFontMetrics().stringWidth(text);
      g2.setColor(CURSOR_COLOR);
      g2.fillRect(cursorX + 1, inputY + 4, 2, INPUT_HEIGHT - 8);
    }
  }
}

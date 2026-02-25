package ui;

import java.awt.Color;

/**
 * Paleta e métricas base da UI inspiradas no estilo Zelda de SNES.
 */
final class UiTheme {

  private UiTheme() {
    // utilitário estático
  }

  static final Color BG_DARK = new Color(0x04, 0x08, 0x1A);
  static final Color PANEL_DARK = new Color(0x0D, 0x1A, 0x46, 235);
  static final Color PANEL_MID = new Color(0x13, 0x2A, 0x72, 235);
  static final Color BORDER_LIGHT = new Color(0xF3, 0xF7, 0xFF);
  static final Color BORDER_SHADOW = new Color(0x8C, 0xA6, 0xDA);

  static final Color TEXT = new Color(0xF4, 0xF5, 0xFB);
  static final Color TEXT_DIM = new Color(0xA9, 0xB4, 0xD8);
  static final Color HIGHLIGHT_BG = new Color(0x1E, 0x2E, 0x4F);
  static final Color ACCENT = new Color(0x4F, 0xD1, 0xC5);

  static final int PANEL_PADDING = 12;
  static final int DIALOG_LINE_HEIGHT = 34;
}

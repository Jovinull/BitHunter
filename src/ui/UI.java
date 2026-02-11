package ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import entity.Entity;
import main.GamePanel;
import main.KeyHandler;
import main.LanguageManager;
import object.ObjCoinBronze;
import object.ObjHeart;
import object.ObjManaCrystal;

public class UI {

  // --- Referência do jogo ---
  private final GamePanel gp;
  Graphics2D g2;

  // --- Fontes (gerenciadas pelo FontManager) ---
  final FontManager fonts;

  // --- HUD ---
  BufferedImage heartFull;
  BufferedImage heartHalf;
  BufferedImage heartBlank;
  BufferedImage crystalFull;
  BufferedImage crystalBlank;
  BufferedImage coin;

  // --- Estado de UI (compartilhado entre telas) ---
  boolean messageOn = false;
  final ArrayList<String> message = new ArrayList<>();
  final ArrayList<Integer> messageCounter = new ArrayList<>();
  boolean gameFinished = false;
  String currentDialogue = "";
  int commandNum = 0;
  int titleScreenState = 0;
  int playerSlotCol = 0;
  int playerSlotRow = 0;
  int npcSlotCol = 0;
  int npcSlotRow = 0;
  int subState = 0;
  int counter = 0;
  Entity npc;
  int charIndex = 0;
  String combinedText = "";

  static final Color COL_BG = new Color(0x05, 0x07, 0x14); // fundo quase preto azulado
  static final Color COL_PANEL = new Color(0x10, 0x15, 0x28); // painel um pouco mais claro
  static final Color COL_PANEL_BORDER = new Color(0x28, 0x36, 0x56); // borda azul-acinzentada

  static final Color COL_TEXT = new Color(0xF4, 0xF5, 0xFB); // quase branco, bem legível
  static final Color COL_TEXT_DIM = new Color(0xA9, 0xB4, 0xD8); // texto secundário mais frio

  static final Color COL_HIGHLIGHT_BG = new Color(0x1E, 0x2E, 0x4F); // fundo de seleção / hover

  static final Color COL_ACCENT = new Color(0x4F, 0xD1, 0xC5); // turquesa forte, para botões, barras etc.

  static final int MENU_LINE = 48;
  static final int MAX_VISIBLE_LANG_OPTIONS = 5;
  int languageScrollOffset = 0;

  // Paginação da tela de controles
  int controlsPage = 0;
  int controlsPageCount = 1;

  static final String KEY_COMMON_BACK = "ui.common.back";
  static final String KEY_TRADE_HINT_BACK = "ui.trade.hintBack";

  // --- Sub-UIs ---
  private final HudUi hudUi;
  private final TitleUi titleUi;
  private final PauseUi pauseUi;
  private final DialogueUi dialogueUi;
  private final InventoryUi inventoryUi;
  private final OptionsUi optionsUi;
  private final TradeUi tradeUi;
  private final TransitionUi transitionUi;
  private final CharacterUi characterUi;
  private final GameOverUi gameOverUi;
  private final PetYardUi petYardUi;

  public UI(GamePanel gp) {
    this.gp = gp;
    this.fonts = new FontManager();

    Entity heart = new ObjHeart(gp);
    heartFull = heart.getBaseImage();
    heartHalf = heart.getAltImage1();
    heartBlank = heart.getAltImage2();

    Entity crystal = new ObjManaCrystal(gp);
    crystalFull = crystal.getBaseImage();
    crystalBlank = crystal.getAltImage1();

    Entity bronzeCoin = new ObjCoinBronze(gp);
    coin = bronzeCoin.getDown1();

    // instancia sub-UIs
    hudUi = new HudUi(this);
    titleUi = new TitleUi(this);
    pauseUi = new PauseUi(this);
    dialogueUi = new DialogueUi(this);
    inventoryUi = new InventoryUi(this);
    optionsUi = new OptionsUi(this);
    tradeUi = new TradeUi(this, inventoryUi);
    transitionUi = new TransitionUi(this);
    characterUi = new CharacterUi(this);
    gameOverUi = new GameOverUi(this);
    petYardUi = new PetYardUi(this, inventoryUi);
  }

  // ---- Helper de tradução ----
  String t(String key) {
    return gp.getLanguageManager().text(key);
  }

  LanguageManager getLanguageManager() {
    return gp.getLanguageManager();
  }

  KeyHandler getKeyHandler() {
    return gp.getKeyH();
  }

  GamePanel getGamePanel() {
    return gp;
  }

  BufferedImage getCoinImage() {
    return coin;
  }

  DialogueUi getDialogueUi() {
    return dialogueUi;
  }

  public void drawDialogueScreen() {
    dialogueUi.drawDialogueScreen();
  }

  // --- Mensagens temporárias ---
  public void addMessage(String text) {
    message.add(text);
    messageCounter.add(0);
  }

  // --- Entrypoint de desenho ---
  public void draw(Graphics2D g2) {
    this.g2 = g2;

    configureTextRendering();
    g2.setColor(COL_TEXT);

    int state = gp.getGameState();

    if (state == GamePanel.TITLE_STATE) {
      titleUi.drawTitleScreen();
      return;
    }

    if (state == GamePanel.PLAY_STATE) {
      hudUi.drawXpTopBar();
      hudUi.drawPlayerLife();
      hudUi.drawPetHud();
      hudUi.drawMonsterLife();
      hudUi.drawMessage();
    } else if (state == GamePanel.PAUSE_STATE) {
      hudUi.drawPlayerLife();
      pauseUi.drawPauseScreen();
    } else if (state == GamePanel.DIALOGUE_STATE) {
      dialogueUi.drawDialogueScreen();
    } else if (state == GamePanel.CHARACTER_STATE) {
      characterUi.drawCharacterScreen();
      inventoryUi.drawInventory(gp.getPlayer(), true);
    } else if (state == GamePanel.OPTIONS_STATE) {
      optionsUi.drawOptionsScreen();
    } else if (state == GamePanel.GAME_OVER_STATE) {
      gameOverUi.drawGameOverScreen();
    } else if (state == GamePanel.TRANSITION_STATE) {
      transitionUi.drawTransition();
    } else if (state == GamePanel.TRADE_STATE) {
      tradeUi.drawTradeScreen();
    } else if (state == GamePanel.PET_YARD_STATE) {
      petYardUi.drawPetYardScreen();
    } else if (state == GamePanel.SLEEP_STATE) {
      transitionUi.drawSleepScreen();
    }
  }

  private void configureTextRendering() {
    int state = gp.getGameState();
    boolean smoothUi = state == GamePanel.TITLE_STATE
        || state == GamePanel.PAUSE_STATE
        || state == GamePanel.GAME_OVER_STATE;

    if (smoothUi) {
      useSmoothUiText(g2);
      g2.setFont(fonts.displayPlain(48F));
    } else {
      useCrispPixelText(g2);
      g2.setFont(fonts.pixelPlain(16F));
    }
  }

  // --- Helpers visuais / utilitários compartilhados ---

  void drawSubWindow(int x, int y, int width, int height) {
    Graphics2D g = (Graphics2D) g2.create();
    g.setColor(COL_PANEL);
    g.fillRoundRect(x, y, width, height, 14, 14);
    g.setStroke(new BasicStroke(2f));
    g.setColor(COL_PANEL_BORDER);
    g.drawRoundRect(x, y, width, height, 14, 14);
    g.dispose();
  }

  void drawSubWindowCenter(int w, int h) {
    int x = (GamePanel.SCREEN_WIDTH - w) / 2;
    int y = (GamePanel.SCREEN_HEIGHT - h) / 2;
    drawSubWindow(x, y, w, h);
  }

  void drawMenuItem(String text, int centerY, boolean selected) {
    int w = Math.min((int) (GamePanel.SCREEN_WIDTH * 0.6), 800);
    int x = (GamePanel.SCREEN_WIDTH - w) / 2;
    int h = (int) (GamePanel.TILE_SIZE * 1.0);
    int yTop = centerY - (h * 3) / 4;

    if (selected) {
      g2.setColor(COL_HIGHLIGHT_BG);
      g2.fillRoundRect(x, yTop, w, h, 10, 10);
      g2.setColor(COL_PANEL_BORDER);
      g2.drawRoundRect(x, yTop, w, h, 10, 10);
    }

    int tx = getXforCenteredText(text);
    g2.setColor(Color.black);
    g2.drawString(text, tx + 2, centerY + 2);
    g2.setColor(selected ? COL_ACCENT : COL_TEXT);
    g2.drawString(text, tx, centerY);

    if (selected) {
      int triX = x - (int) (GamePanel.TILE_SIZE * 0.3);
      int triY = centerY - (int) (GamePanel.TILE_SIZE * 0.60);
      int triH = (int) (GamePanel.TILE_SIZE * 0.7);
      int[] xs = { triX, triX + (int) (GamePanel.TILE_SIZE * 0.45), triX };
      int[] ys = { triY, triY + triH / 2, triY + triH };
      g2.setColor(COL_ACCENT);
      g2.fillPolygon(xs, ys, 3);
    }
  }

  void useCrispPixelText(Graphics2D g) {
    g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
    g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
    g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
  }

  void useSmoothUiText(Graphics2D g) {
    g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
    g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
    g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
  }

  void drawTextShadowCentered(String text, int y, Color fill, Color shadow, int offset) {
    int x = getXforCenteredText(text);
    drawTextShadow(text, x, y, fill, shadow, offset);
  }

  void drawTextShadow(String text, int x, int y, Color fill, Color shadow, int offset) {
    g2.setColor(shadow);
    g2.drawString(text, x + offset, y + offset);
    g2.setColor(fill);
    g2.drawString(text, x, y);
  }

  int getXforCenteredText(String text) {
    int length = (int) g2.getFontMetrics().getStringBounds(text, g2).getWidth();
    return GamePanel.SCREEN_WIDTH / 2 - length / 2;
  }

  int getXforAlignToRightText(String text, int tailX) {
    int length = (int) g2.getFontMetrics().getStringBounds(text, g2).getWidth();
    return tailX - length;
  }

  public int getItemIndexOnSlot(int slotCol, int slotRow) {
    return slotCol + (slotRow * 5);
  }

  public void resetOptionsMenu() {
    subState = 0;
    commandNum = 0;
    controlsPage = 0;
  }

  // --- Getters/Setters já existentes (mantidos p/ compatibilidade externa) ---

  public Font getMaruMonicaPublic() {
    return fonts.getPixelBase();
  }

  public boolean isMessageOn() {
    return messageOn;
  }

  public void setMessageOn(boolean messageOn) {
    this.messageOn = messageOn;
  }

  public boolean isGameFinished() {
    return gameFinished;
  }

  public void setGameFinished(boolean gameFinished) {
    this.gameFinished = gameFinished;
  }

  public int getCommandNum() {
    return commandNum;
  }

  public void setCommandNum(int commandNum) {
    this.commandNum = commandNum;
  }

  public int getTitleScreenState() {
    return titleScreenState;
  }

  public void setTitleScreenState(int titleScreenState) {
    this.titleScreenState = titleScreenState;
  }

  public int getPlayerSlotCol() {
    return playerSlotCol;
  }

  public void setPlayerSlotCol(int playerSlotCol) {
    this.playerSlotCol = playerSlotCol;
  }

  public int getPlayerSlotRow() {
    return playerSlotRow;
  }

  public void setPlayerSlotRow(int playerSlotRow) {
    this.playerSlotRow = playerSlotRow;
  }

  public int getNpcSlotCol() {
    return npcSlotCol;
  }

  public void setNpcSlotCol(int npcSlotCol) {
    this.npcSlotCol = npcSlotCol;
  }

  public int getNpcSlotRow() {
    return npcSlotRow;
  }

  public void setNpcSlotRow(int npcSlotRow) {
    this.npcSlotRow = npcSlotRow;
  }

  public Entity getNpc() {
    return npc;
  }

  public void setNpc(Entity npc) {
    this.npc = npc;
  }

  public int getSubState() {
    return subState;
  }

  public void setSubState(int subState) {
    this.subState = subState;
  }

  // --- Paginação da tela de controles ---

  public int getControlsPage() {
    return controlsPage;
  }

  public void setControlsPage(int controlsPage) {
    this.controlsPage = controlsPage;
  }

  public int getControlsPageCount() {
    return controlsPageCount;
  }

  public void setControlsPageCount(int controlsPageCount) {
    this.controlsPageCount = controlsPageCount;
  }
}

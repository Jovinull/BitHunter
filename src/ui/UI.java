package ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import data.SaveManager.SlotInfo;
import entity.Entity;
import main.GamePanel;
import main.KeyHandler;
import main.LanguageManager;
import object.ObjCoinBronze;

public class UI {

  // --- Referência do jogo ---
  private final GamePanel gp;
  Graphics2D g2;

  // --- Fontes (gerenciadas pelo FontManager) ---
  final FontManager fonts;

  // --- HUD ---
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
  final StringBuilder combinedText = new StringBuilder();
  private boolean clockWindowOpen = false;

  // Estado do remapeamento de teclas (tela de controles)
  boolean waitingForRebind = false;
  int rebindControlIndex = -1;

  // Estado da tela de slots de save
  int saveSlotMode = SaveSlotUi.MODE_NEW_GAME;
  int selectedSaveSlot = -1;
  private final StringBuilder playerNameInput = new StringBuilder();

  static final Color COL_BG = UiTheme.BG_DARK;
  static final Color COL_PANEL = UiTheme.PANEL_DARK;
  static final Color COL_PANEL_BORDER = UiTheme.BORDER_LIGHT;

  static final Color COL_TEXT = UiTheme.TEXT;
  static final Color COL_TEXT_DIM = UiTheme.TEXT_DIM;

  static final Color COL_HIGHLIGHT_BG = UiTheme.HIGHLIGHT_BG;

  static final Color COL_ACCENT = UiTheme.ACCENT;

  static final int MENU_LINE = 48;
  static final int MAX_VISIBLE_LANG_OPTIONS = 5;
  int languageScrollOffset = 0;

  // Paginação da tela de controles
  int controlsPage = 0;
  int controlsPageCount = 1;

  static final String KEY_COMMON_BACK = "ui.common.back";
  static final String KEY_TRADE_HINT_BACK = "ui.trade.hintBack";

  // --- Sub-UIs ---
  private final UiWindowRenderer windowRenderer;
  private final UiScreenRouter screenRouter;
  private final UiDebugOverlay debugOverlay;
  private final HudUi hudUi;
  private final TitleUi titleUi;
  private final SaveSlotUi saveSlotUi;
  private final PauseUi pauseUi;
  private final DialogueUi dialogueUi;
  private final InventoryUi inventoryUi;
  private final OptionsUi optionsUi;
  private final TradeUi tradeUi;
  private final TransitionUi transitionUi;
  private final CharacterUi characterUi;
  private final GameOverUi gameOverUi;
  private final PetYardUi petYardUi;
  private final BestiaryUi bestiaryUi;
  private final AchievementUi achievementUi;
  private final StatsUi statsUi;

  public UI(GamePanel gp) {
    this.gp = gp;
    this.fonts = new FontManager();

    Entity bronzeCoin = new ObjCoinBronze(gp);
    coin = bronzeCoin.getDown1();

    windowRenderer = new UiWindowRenderer();
    screenRouter = new UiScreenRouter(this);
    debugOverlay = new UiDebugOverlay(this);

    // instancia sub-UIs
    hudUi = new HudUi(this);
    saveSlotUi = new SaveSlotUi(this);
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
    bestiaryUi = new BestiaryUi(this);
    achievementUi = new AchievementUi(this);
    statsUi = new StatsUi(this);
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
    screenRouter.drawCurrentScreen();
  }

  private void configureTextRendering() {
    int state = gp.getGameState();
    boolean smoothUi = state == GamePanel.TITLE_STATE
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
    windowRenderer.drawWindow(g2, x, y, width, height);
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

  int getPanelPadding() {
    return UiTheme.PANEL_PADDING;
  }

  int getDialogueLineHeight() {
    return UiTheme.DIALOG_LINE_HEIGHT;
  }

  public void resetOptionsMenu() {
    subState = 0;
    commandNum = 0;
    controlsPage = 0;
    waitingForRebind = false;
    rebindControlIndex = -1;
  }

  /** Reseta a animação do pause (chamado ao entrar em PAUSE_STATE). */
  public void resetPauseAnimation() {
    pauseUi.reset();
  }

  public BestiaryUi getBestiaryUi() {
    return bestiaryUi;
  }

  public AchievementUi getAchievementUi() {
    return achievementUi;
  }

  StatsUi getStatsUi() {
    return statsUi;
  }

  HudUi getHudUi() {
    return hudUi;
  }

  TitleUi getTitleUi() {
    return titleUi;
  }

  PauseUi getPauseUi() {
    return pauseUi;
  }

  InventoryUi getInventoryUi() {
    return inventoryUi;
  }

  OptionsUi getOptionsUi() {
    return optionsUi;
  }

  TradeUi getTradeUi() {
    return tradeUi;
  }

  TransitionUi getTransitionUi() {
    return transitionUi;
  }

  CharacterUi getCharacterUi() {
    return characterUi;
  }

  GameOverUi getGameOverUi() {
    return gameOverUi;
  }

  PetYardUi getPetYardUi() {
    return petYardUi;
  }

  // --- Getters/Setters já existentes (mantidos p/ compatibilidade externa) ---

  public Font getMaruMonicaPublic() {
    return fonts.getPixelBase();
  }

  /**
   * Fonte Arial 20pt carregada do classpath (usada pelo debug overlay em
   * GamePanel).
   */
  public Font getDebugFont() {
    return fonts.arialPlain(20F);
  }

  public void drawDebugOverlay(long drawStartNanos) {
    debugOverlay.draw(drawStartNanos);
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

  // --- Rebind de controles ---

  public boolean isWaitingForRebind() {
    return waitingForRebind;
  }

  public void setWaitingForRebind(boolean waitingForRebind) {
    this.waitingForRebind = waitingForRebind;
  }

  public int getRebindControlIndex() {
    return rebindControlIndex;
  }

  public void setRebindControlIndex(int rebindControlIndex) {
    this.rebindControlIndex = rebindControlIndex;
  }

  // --- Slots de save ---

  SaveSlotUi getSaveSlotUi() {
    return saveSlotUi;
  }

  /** Atualiza o cache de metadados dos slots de save. */
  public void refreshSaveSlotInfos() {
    saveSlotUi.refreshSlotInfos();
  }

  /** Retorna os metadados em cache dos 3 slots (índice 0 = Slot 1). */
  public SlotInfo[] getSaveSlotInfos() {
    return saveSlotUi.getSlotInfos();
  }

  /** Comprimento máximo do nome do jogador. */
  public int getMaxPlayerNameLength() {
    return saveSlotUi.getMaxNameLength();
  }

  public int getSaveSlotMode() {
    return saveSlotMode;
  }

  public void setSaveSlotMode(int saveSlotMode) {
    this.saveSlotMode = saveSlotMode;
  }

  public int getSelectedSaveSlot() {
    return selectedSaveSlot;
  }

  public void setSelectedSaveSlot(int selectedSaveSlot) {
    this.selectedSaveSlot = selectedSaveSlot;
  }

  // --- Input do nome do jogador ---

  public String getPlayerNameText() {
    return playerNameInput.toString();
  }

  public void clearPlayerName() {
    playerNameInput.setLength(0);
  }

  public void appendPlayerNameChar(char c) {
    playerNameInput.append(c);
  }

  public void deleteLastPlayerNameChar() {
    if (!playerNameInput.isEmpty()) {
      playerNameInput.deleteCharAt(playerNameInput.length() - 1);
    }
  }

  public int getPlayerNameLength() {
    return playerNameInput.length();
  }

  public boolean isClockWindowOpen() {
    return clockWindowOpen;
  }

  public void setClockWindowOpen(boolean clockWindowOpen) {
    this.clockWindowOpen = clockWindowOpen;
  }
}

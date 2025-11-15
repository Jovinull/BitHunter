package main;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import entity.Entity;
import object.ObjCoinBronze;
import object.ObjHeart;
import object.ObjManaCrystal;

public class UI {

  // --- Logger ---
  private static final Logger LOGGER = Logger.getLogger(UI.class.getName());

  // --- Referências do jogo ---
  private final GamePanel gp;
  Graphics2D g2;

  // --- Fontes & HUD ---
  private Font maruMonica;
  Font purisaB;
  BufferedImage heartFull;
  BufferedImage heartHalf;
  BufferedImage heartBlank;
  BufferedImage crystalFull;
  BufferedImage crystalBlank;
  BufferedImage coin;

  // --- Estado de UI ---
  private boolean messageOn = false;
  private final ArrayList<String> message = new ArrayList<>();
  private final ArrayList<Integer> messageCounter = new ArrayList<>();
  private boolean gameFinished = false;
  private String currentDialogue = "";
  private int commandNum = 0;
  private int titleScreenState = 0;
  private int playerSlotCol = 0;
  private int playerSlotRow = 0;
  private int npcSlotCol = 0;
  private int npcSlotRow = 0;
  int subState = 0;
  int counter = 0;
  private Entity npc;
  int charIndex = 0;
  String combinedText = "";

  // --- Paleta de Cores ---
  private static final Color COL_BG = new Color(0x0F, 0x11, 0x15);
  private static final Color COL_PANEL = new Color(0x1A, 0x1F, 0x29);
  private static final Color COL_PANEL_BORDER = new Color(0x3A, 0x42, 0x54);
  private static final Color COL_TEXT = new Color(0xE6, 0xE9, 0xEF);
  private static final Color COL_TEXT_DIM = new Color(0xB8, 0xC0, 0xCC);
  private static final Color COL_HIGHLIGHT_BG = new Color(0x2A, 0x34, 0x43);
  private static final Color COL_ACCENT = new Color(0xE0, 0xB5, 0x66);

  private static final int MENU_LINE = 48;
  private static final String LABEL_BACK = "Voltar";

  public UI(GamePanel gp) {
    this.gp = gp;

    try (InputStream is = getClass().getResourceAsStream("/font/x12y16pxMaruMonica.ttf")) {
      if (is != null) {
        maruMonica = Font.createFont(Font.TRUETYPE_FONT, is);
      }
    } catch (FontFormatException | IOException e) {
      LOGGER.log(Level.WARNING, "Falha ao carregar fonte MaruMonica", e);
    }

    try (InputStream is = getClass().getResourceAsStream("/font/Purisa Bold.ttf")) {
      if (is != null) {
        purisaB = Font.createFont(Font.TRUETYPE_FONT, is);
      }
    } catch (FontFormatException | IOException e) {
      LOGGER.log(Level.WARNING, "Falha ao carregar fonte Purisa Bold", e);
    }

    // HUD (via getters)
    Entity heart = new ObjHeart(gp);
    heartFull = heart.getBaseImage();
    heartHalf = heart.getAltImage1();
    heartBlank = heart.getAltImage2();

    Entity crystal = new ObjManaCrystal(gp);
    crystalFull = crystal.getBaseImage();
    crystalBlank = crystal.getAltImage1();

    Entity bronzeCoin = new ObjCoinBronze(gp);
    coin = bronzeCoin.getDown1();
  }

  // ---- Helper de tradução ----
  private String t(String key) {
    return gp.getLanguageManager().text(key);
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
    drawByGameState();
  }

  private void configureTextRendering() {
    int state = gp.getGameState();
    boolean smoothUi = state == GamePanel.TITLE_STATE
        || state == GamePanel.PAUSE_STATE
        || state == GamePanel.GAME_OVER_STATE;

    if (smoothUi) {
      useSmoothUiText(g2);
      if (purisaB != null) {
        g2.setFont(purisaB.deriveFont(Font.PLAIN, 48F));
      }
    } else {
      useCrispPixelText(g2);
      if (maruMonica != null) {
        g2.setFont(maruMonica.deriveFont(Font.PLAIN, 16F));
      }
    }
  }

  private void drawByGameState() {
    int state = gp.getGameState();

    if (state == GamePanel.TITLE_STATE) {
      drawTitleScreen();
    } else if (state == GamePanel.PLAY_STATE) {
      drawPlayerLife();
      drawMonsterLife();
      drawMessage();
    } else if (state == GamePanel.PAUSE_STATE) {
      drawPlayerLife();
      drawPauseScreen();
    } else if (state == GamePanel.DIALOGUE_STATE) {
      drawDialogueScreen();
    } else if (state == GamePanel.CHARACTER_STATE) {
      drawCharacterScreen();
      drawInventory(gp.getPlayer(), true);
    } else if (state == GamePanel.OPTIONS_STATE) {
      drawOptionsScreen();
    } else if (state == GamePanel.GAME_OVER_STATE) {
      drawGameOverScreen();
    } else if (state == GamePanel.TRANSITION_STATE) {
      drawTransition();
    } else if (state == GamePanel.TRADE_STATE) {
      drawTradeScreen();
    } else if (state == GamePanel.SLEEP_STATE) {
      drawSleepScreen();
    }
  }

  // --- HUD: Vida & Mana (barras) ---
  public void drawPlayerLife() {
    var player = gp.getPlayer();

    int barWidth = GamePanel.TILE_SIZE * 3;
    int barHeight = 20;

    int x = GamePanel.TILE_SIZE;
    int y = GamePanel.TILE_SIZE / 2;

    // Vida - barra vermelha
    HudBarLayout lifeLayout = new HudBarLayout(x, y, barWidth, barHeight);
    drawHudBar(
        lifeLayout,
        player.getLife(),
        player.getMaxLife(),
        t("ui.stats.hp"),
        new Color(210, 70, 70));

    // Mana - barra azul, logo abaixo
    y += barHeight + 8;
    HudBarLayout manaLayout = new HudBarLayout(x, y, barWidth, barHeight);
    drawHudBar(
        manaLayout,
        player.getMana(),
        player.getMaxMana(),
        t("ui.stats.mp"),
        new Color(70, 120, 220));
  }

  private record HudBarLayout(int x, int y, int width, int height) {
  }

  private void drawHudBar(
      HudBarLayout layout,
      int current,
      int max,
      String label,
      Color fillColor) {

    int x = layout.x();
    int y = layout.y();
    int width = layout.width();
    int height = layout.height();

    // fundo da barra
    g2.setColor(new Color(0, 0, 0, 180));
    g2.fillRoundRect(x - 2, y - 2, width + 4, height + 4, 8, 8);

    // borda
    g2.setColor(COL_PANEL_BORDER);
    g2.drawRoundRect(x - 2, y - 2, width + 4, height + 4, 8, 8);

    // valor atual
    if (max > 0 && current > 0) {
      int clamped = Math.min(current, max);
      int filledWidth = (int) (width * (clamped / (double) max));
      g2.setColor(fillColor);
      g2.fillRoundRect(x, y, filledWidth, height, 8, 8);
    }

    // texto: "Vida 12/12", "Mana 4/4"
    String text = label + " " + current + "/" + max;
    g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 14F));

    int textX = x + 6;
    int textY = y + height - 3;

    g2.setColor(Color.black);
    g2.drawString(text, textX + 1, textY + 1);
    g2.setColor(COL_TEXT);
    g2.drawString(text, textX, textY);
  }

  public void drawMonsterLife() {
    Entity[][] monsters = gp.getMonster();
    int currentMapIndex = gp.getCurrentMap();
    Entity[] currentMonsters = monsters[currentMapIndex];

    for (int i = 0; i < currentMonsters.length; i++) {
      Entity monster = currentMonsters[i];
      if (monster != null && monster.inCamera()) {
        if (monster.isHpBarOn() && !monster.isBoss()) {

          double oneScale = (double) GamePanel.TILE_SIZE / monster.getMaxLife();
          double hpBarValue = oneScale * monster.getLife();

          g2.setColor(new Color(35, 35, 35));
          g2.fillRect(
              monster.getScreenX() - 1,
              monster.getScreenY() - 16,
              GamePanel.TILE_SIZE + 2,
              12);

          g2.setColor(new Color(255, 0, 30));
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
        } else if (monster.isBoss()) {

          double oneScale = (double) GamePanel.TILE_SIZE * 8 / monster.getMaxLife();
          double hpBarValue = oneScale * monster.getLife();

          int x = GamePanel.SCREEN_WIDTH / 2 - GamePanel.TILE_SIZE * 4;
          int y = GamePanel.TILE_SIZE * 10;

          g2.setColor(new Color(35, 35, 35));
          g2.fillRect(x - 1, y - 1, GamePanel.TILE_SIZE * 8 + 2, 22);

          g2.setColor(new Color(255, 0, 30));
          g2.fillRect(x, y, (int) hpBarValue, 20);

          g2.setFont(g2.getFont().deriveFont(Font.BOLD, 24f));
          g2.setColor(Color.white);
          g2.drawString(monster.getName(), x + 4, y - 10);
        }
      }
    }
  }

  // --- Mensagens flutuantes ---
  public void drawMessage() {
    int messageX = GamePanel.TILE_SIZE;
    int messageY = GamePanel.TILE_SIZE * 4;

    g2.setFont(g2.getFont().deriveFont(Font.BOLD, 32F));

    int i = 0;
    while (i < message.size()) {
      String m = message.get(i);
      if (m != null) {
        // sombra
        g2.setColor(Color.black);
        g2.drawString(m, messageX + 2, messageY + 2);
        // texto
        g2.setColor(COL_TEXT);
        g2.drawString(m, messageX, messageY);

        int msgCounter = messageCounter.get(i) + 1;
        messageCounter.set(i, msgCounter);
        messageY += 40;

        if (messageCounter.get(i) > 180) {
          message.remove(i);
          messageCounter.remove(i);
          continue;
        }
      }
      i++;
    }
  }

  // --- Título ---
  public void drawTitleScreen() {
    g2.setColor(COL_BG);
    g2.fillRect(0, 0, GamePanel.SCREEN_WIDTH, GamePanel.SCREEN_HEIGHT);

    if (titleScreenState == 0) {
      // --- TELA 0: menu principal ---
      if (purisaB != null) {
        g2.setFont(purisaB.deriveFont(Font.BOLD, 112F));
      }
      drawTextShadowCentered(t("ui.title.gameName"), GamePanel.TILE_SIZE * 3, COL_TEXT, Color.black, 3);

      int xImg = GamePanel.SCREEN_WIDTH / 2 - (GamePanel.TILE_SIZE * 2) / 2;
      int yImg = GamePanel.TILE_SIZE * 3 + GamePanel.TILE_SIZE * 2;
      g2.drawImage(
          gp.getPlayer().getDown1(),
          xImg,
          yImg,
          GamePanel.TILE_SIZE * 2,
          GamePanel.TILE_SIZE * 2,
          null);

      if (purisaB != null) {
        g2.setFont(purisaB.deriveFont(Font.BOLD, 48F));
      }
      int yMenuStart = yImg + (int) (GamePanel.TILE_SIZE * 3.2);
      drawMenuItem(t("ui.title.newGame"), yMenuStart, commandNum == 0);
      drawMenuItem(t("ui.title.loadGame"), yMenuStart + MENU_LINE, commandNum == 1);
      drawMenuItem(t("ui.title.exit"), yMenuStart + MENU_LINE * 2, commandNum == 2);

      // Dica
      useCrispPixelText(g2);
      if (maruMonica != null) {
        g2.setFont(maruMonica.deriveFont(Font.PLAIN, 16F));
      }
      String hint = t("ui.title.hintNavigation");
      drawTextShadowCentered(
          hint,
          GamePanel.SCREEN_HEIGHT - GamePanel.TILE_SIZE,
          COL_TEXT_DIM,
          Color.black,
          2);
      return;
    }

    if (titleScreenState == 1) {
      if (purisaB != null) {
        g2.setFont(purisaB.deriveFont(Font.BOLD, 36F));
      }
      drawSubWindowCenter(GamePanel.TILE_SIZE * 10, GamePanel.TILE_SIZE * 8);
      int cy = GamePanel.SCREEN_HEIGHT / 2 - GamePanel.TILE_SIZE;
      drawTextShadowCentered(t("ui.title.classSelection"), cy - MENU_LINE, COL_TEXT, Color.black, 2);
      drawMenuItem(t("ui.title.class.warrior"), cy, commandNum == 0);
      drawMenuItem(t("ui.title.class.thief"), cy + MENU_LINE, commandNum == 1);
      drawMenuItem(t("ui.title.class.wizard"), cy + MENU_LINE * 2, commandNum == 2);
      drawMenuItem(t("ui.common.back"), cy + MENU_LINE * 4, commandNum == 3);
    }
  }

  // --- Pause ---
  public void drawPauseScreen() {
    int w = (int) (GamePanel.SCREEN_WIDTH * 0.6);
    int h = GamePanel.TILE_SIZE * 4;
    int x = (GamePanel.SCREEN_WIDTH - w) / 2;
    int y = (GamePanel.SCREEN_HEIGHT - h) / 2;
    drawSubWindow(x, y, w, h);

    if (purisaB != null) {
      g2.setFont(purisaB.deriveFont(Font.PLAIN, 80F));
    }
    String text = t("ui.pause.title");
    drawTextShadowCentered(text, y + (int) (h * 0.62), COL_TEXT, Color.black, 3);
  }

  // --- Diálogo ---
  public void drawDialogueScreen() {
    // WINDOW
    int x = GamePanel.TILE_SIZE * 3;
    int y = GamePanel.TILE_SIZE / 2;
    int width = GamePanel.SCREEN_WIDTH - (GamePanel.TILE_SIZE * 6);
    int height = GamePanel.TILE_SIZE * 4;
    drawSubWindow(x, y, width, height);

    if (maruMonica != null) {
      g2.setFont(maruMonica.deriveFont(Font.PLAIN, 32F));
    }
    x += GamePanel.TILE_SIZE;
    y += GamePanel.TILE_SIZE;

    String[][] ds = npc.getDialogues();
    int set = npc.getDialogueSet();
    int idx = npc.getDialogueIndex();

    if (ds[set][idx] != null) {
      updateDialogueCharacters(ds[set][idx], idx);
    } else {
      handleDialogueEnd();
    }

    drawCurrentDialogueLines(x, y);
  }

  private void updateDialogueCharacters(String dialogueLine, int idx) {
    char[] characters = dialogueLine.toCharArray();

    if (charIndex < characters.length) {
      gp.playSE(17);
      String s = String.valueOf(characters[charIndex]);
      combinedText = combinedText + s;
      currentDialogue = combinedText;
      charIndex++;
    }

    var keyHandler = gp.getKeyH();
    int state = gp.getGameState();

    if (keyHandler.isEnterPressed()) {
      charIndex = 0;
      combinedText = "";

      if (state == GamePanel.DIALOGUE_STATE || state == GamePanel.CUTSCENE_STATE) {
        npc.setDialogueIndex(idx + 1);
        keyHandler.setEnterPressed(false);
      }
    }
  }

  private void handleDialogueEnd() {
    npc.setDialogueIndex(0);
    int state = gp.getGameState();

    if (state == GamePanel.DIALOGUE_STATE) {
      gp.setGameState(GamePanel.PLAY_STATE);
    } else if (state == GamePanel.CUTSCENE_STATE) {
      gp.getCsManager().nextPhase();
    }
  }

  private void drawCurrentDialogueLines(int x, int y) {
    for (String line : currentDialogue.split("\n")) {
      g2.drawString(line, x, y);
      y += 40;
    }
  }

  // --- Inventário ---
  public void drawInventory(Entity entity, boolean cursor) {
    int frameX;
    int frameY;
    int frameWidth;
    int frameHeight;
    int slotCol;
    int slotRow;

    if (entity == gp.getPlayer()) {
      frameX = GamePanel.TILE_SIZE * 12;
      frameY = GamePanel.TILE_SIZE;
      frameWidth = GamePanel.TILE_SIZE * 6;
      frameHeight = GamePanel.TILE_SIZE * 5;
      slotCol = playerSlotCol;
      slotRow = playerSlotRow;
    } else {
      frameX = GamePanel.TILE_SIZE * 2;
      frameY = GamePanel.TILE_SIZE;
      frameWidth = GamePanel.TILE_SIZE * 6;
      frameHeight = GamePanel.TILE_SIZE * 5;
      slotCol = npcSlotCol;
      slotRow = npcSlotRow;
    }

    drawSubWindow(frameX, frameY, frameWidth, frameHeight);

    // Título interno (fonte ligeiramente menor)
    String title = (entity == gp.getPlayer())
        ? t("ui.inventory.playerTitle")
        : t("ui.inventory.npcTitle");
    int titleY = frameY + 32;

    g2.setFont(g2.getFont().deriveFont(Font.BOLD, 22F));
    drawTextShadow(title, frameX + 20, titleY, COL_TEXT, Color.black, 2);

    final int slotXstart = frameX + 20;
    final int slotYstart = titleY + 16;
    int slotSize = GamePanel.TILE_SIZE + 3;

    drawInventoryItems(entity, slotXstart, slotYstart, slotSize);

    if (cursor) {
      drawInventoryCursor(slotXstart, slotYstart, slotSize, slotCol, slotRow);
      drawInventoryDescription(entity, frameX, frameY, frameWidth, frameHeight, slotCol, slotRow);
    }
  }

  private void drawInventoryItems(Entity entity, int slotXstart, int slotYstart, int slotSize) {
    int slotX = slotXstart;
    int slotY = slotYstart;

    for (int i = 0; i < entity.getInventory().size(); i++) {
      Entity item = entity.getInventory().get(i);

      if (item != null && isEquippedItem(entity, item)) {
        g2.setColor(COL_ACCENT);
        g2.fillRoundRect(slotX, slotY, GamePanel.TILE_SIZE, GamePanel.TILE_SIZE, 8, 8);
      }

      if (item != null && item.getDown1() != null) {
        g2.drawImage(item.getDown1(), slotX, slotY, null);
      }

      // DISPLAY AMOUNT
      if (item != null && entity == gp.getPlayer() && item.getAmount() > 1) {
        g2.setFont(g2.getFont().deriveFont(32f));
        String s = String.valueOf(item.getAmount());
        int amountX = getXforAlignToRightText(s, slotX + 44);
        int amountY = slotY + GamePanel.TILE_SIZE;

        // sombra
        g2.setColor(new Color(60, 60, 60));
        g2.drawString(s, amountX, amountY);

        // número
        g2.setColor(Color.white);
        g2.drawString(s, amountX - 3, amountY - 3);
      }

      slotX += slotSize;

      if (i == 4 || i == 9 || i == 14) {
        slotX = slotXstart;
        slotY += GamePanel.TILE_SIZE;
      }
    }
  }

  private boolean isEquippedItem(Entity entity, Entity item) {
    if (item == null) {
      return false;
    }

    return item == entity.getCurrentWeapon()
        || item == entity.getCurrentShield()
        || item == entity.getCurrentLight()
        || item == entity.getCurrentBoots()
        || item == entity.getCurrentHelmet()
        || item == entity.getCurrentChest()
        || item == entity.getCurrentLegs();
  }

  private void drawInventoryCursor(
      int slotXstart,
      int slotYstart,
      int slotSize,
      int slotCol,
      int slotRow) {

    int cursorX = slotXstart + (slotSize * slotCol);
    int cursorY = slotYstart + (slotSize * slotRow);
    int cursorWidth = GamePanel.TILE_SIZE;
    int cursorHeight = GamePanel.TILE_SIZE;

    g2.setColor(COL_TEXT);
    g2.setStroke(new BasicStroke(3));
    g2.drawRoundRect(cursorX, cursorY, cursorWidth, cursorHeight, 8, 8);
  }

  private void drawInventoryDescription(
      Entity entity,
      int frameX,
      int frameY,
      int frameWidth,
      int frameHeight,
      int slotCol,
      int slotRow) {

    int dFrameX = frameX;
    int dFrameY = frameY + frameHeight;
    int dFrameWidth = frameWidth;
    int dFrameHeight = GamePanel.TILE_SIZE * 3;

    int itemIndex = getItemIndexOnSlot(slotCol, slotRow);
    if (itemIndex < entity.getInventory().size()) {
      drawSubWindow(dFrameX, dFrameY, dFrameWidth, dFrameHeight);

      int textX = dFrameX + 20;
      int textY = dFrameY + GamePanel.TILE_SIZE;
      g2.setColor(COL_TEXT);

      Font baseFont = maruMonica != null ? maruMonica : g2.getFont();
      Font titleFont = baseFont.deriveFont(Font.BOLD, 20F);
      Font descFont = baseFont.deriveFont(Font.PLAIN, 20F);

      String[] lines = entity.getInventory().get(itemIndex).getDescription().split("\n");

      // primeira linha (ex: [Espada Comum]) em negrito
      if (lines.length > 0) {
        g2.setFont(titleFont);
        g2.drawString(lines[0], textX, textY);
        textY += 24;
      }

      // demais linhas em normal
      g2.setFont(descFont);
      for (int i = 1; i < lines.length; i++) {
        g2.drawString(lines[i], textX, textY);
        textY += 22;
      }
    }
  }

  // --- Game Over ---
  public void drawGameOverScreen() {
    g2.setColor(new Color(0, 0, 0, 140));
    g2.fillRect(0, 0, GamePanel.SCREEN_WIDTH, GamePanel.SCREEN_HEIGHT);

    if (purisaB != null) {
      g2.setFont(purisaB.deriveFont(Font.BOLD, 96f));
    }
    String text = t("ui.gameover.title");
    drawTextShadowCentered(text, GamePanel.TILE_SIZE * 4, COL_TEXT, Color.black, 4);

    g2.setFont(g2.getFont().deriveFont(44f));
    int y = GamePanel.TILE_SIZE * 8;

    drawMenuItem(t("ui.gameover.retry"), y, commandNum == 0);
    drawMenuItem(t("ui.gameover.exit"), y + MENU_LINE, commandNum == 1);
  }

  // --- Opções ---
  public void drawOptionsScreen() {
    g2.setColor(COL_TEXT);
    g2.setFont(g2.getFont().deriveFont(32F));

    int frameX = GamePanel.TILE_SIZE * 6;
    int frameY = GamePanel.TILE_SIZE;
    int frameWidth = GamePanel.TILE_SIZE * 8;
    int frameHeight = GamePanel.TILE_SIZE * 10;
    drawSubWindow(frameX, frameY, frameWidth, frameHeight);

    switch (subState) {
      case 0 -> optionsTop(frameX, frameY);
      case 1 -> optionsFullScreenNotification(frameX, frameY);
      case 2 -> optionsControl(frameX, frameY);
      case 3 -> optionsEndGameConfirmation(frameX, frameY);
      case 4 -> optionsLanguage(frameX, frameY); // <<< NOVO SUBMENU DE IDIOMA
      default -> {
        /* no-op */ }
    }

    gp.getKeyH().setEnterPressed(false);
  }

  public void optionsTop(int frameX, int frameY) {
    int textX;
    int textY;

    String text = t("ui.options.title");
    textX = getXforCenteredText(text);
    textY = frameY + GamePanel.TILE_SIZE;
    drawTextShadow(text, textX, textY, COL_TEXT, Color.black, 2);

    var keyH = gp.getKeyH();

    // TELA CHEIA
    textX = frameX + GamePanel.TILE_SIZE;
    textY = frameY + GamePanel.TILE_SIZE * 2;
    g2.setColor(COL_TEXT);
    g2.drawString(t("ui.options.fullscreen"), textX, textY);
    if (commandNum == 0) {
      g2.drawString(">", textX - 25, textY);
      if (keyH.isEnterPressed()) {
        gp.setFullScreenOn(!gp.isFullScreenOn());
        subState = 1;
      }
    }

    // MÚSICA
    textY += GamePanel.TILE_SIZE;
    g2.drawString(t("ui.options.music"), textX, textY);
    if (commandNum == 1) {
      g2.drawString(">", textX - 25, textY);
    }

    // EFEITOS SONOROS
    textY += GamePanel.TILE_SIZE;
    g2.drawString(t("ui.options.effects"), textX, textY);
    if (commandNum == 2) {
      g2.drawString(">", textX - 25, textY);
    }

    // CONTROLES
    textY += GamePanel.TILE_SIZE;
    g2.drawString(t("ui.options.controls"), textX, textY);
    if (commandNum == 3) {
      g2.drawString(">", textX - 25, textY);
      if (keyH.isEnterPressed()) {
        subState = 2;
        commandNum = 0;
      }
    }

    // IDIOMA -> abre submenu dedicado
    textY += GamePanel.TILE_SIZE;
    String currentLangName = gp.getLanguageManager().getCurrentLanguageDisplayName();
    String languageLine = t("ui.options.language") + ": " + currentLangName;
    g2.drawString(languageLine, textX, textY);
    if (commandNum == 4) {
      g2.drawString(">", textX - 25, textY);
      if (keyH.isEnterPressed()) {
        subState = 4; // vai para submenu de idiomas
        commandNum = 0; // começa na primeira opção de idioma
      }
    }

    // ENCERRAR JOGO
    textY += GamePanel.TILE_SIZE;
    g2.drawString(t("ui.options.endGame"), textX, textY);
    if (commandNum == 5) {
      g2.drawString(">", textX - 25, textY);
      if (keyH.isEnterPressed()) {
        subState = 3;
        commandNum = 0;
      }
    }

    // Dica de voltar (ESC)
    textY += GamePanel.TILE_SIZE * 2;
    g2.setColor(COL_TEXT_DIM);
    g2.drawString(t("ui.trade.hintBack"), textX, textY);

    // CHECKBOX TELA CHEIA
    textX = frameX + (int) (GamePanel.TILE_SIZE * 4.5);
    textY = frameY + GamePanel.TILE_SIZE * 2 - 22;
    g2.setStroke(new BasicStroke(3));
    g2.setColor(COL_PANEL_BORDER);
    g2.drawRect(textX, textY, 24, 24);
    if (gp.isFullScreenOn()) {
      g2.setColor(COL_ACCENT);
      g2.fillRect(textX + 2, textY + 2, 20, 20);
    }

    // VOLUME MÚSICA
    textY += GamePanel.TILE_SIZE;
    g2.setColor(COL_PANEL_BORDER);
    g2.drawRect(textX, textY, 120, 24);
    g2.setColor(COL_ACCENT);
    int volumeWidth = 24 * gp.music.volumeScale;
    g2.fillRect(textX + 1, textY + 1, Math.max(0, volumeWidth - 2), 22);

    // VOLUME EFEITOS
    textY += GamePanel.TILE_SIZE;
    g2.setColor(COL_PANEL_BORDER);
    g2.drawRect(textX, textY, 120, 24);
    g2.setColor(COL_ACCENT);
    volumeWidth = 24 * gp.se.volumeScale;
    g2.fillRect(textX + 1, textY + 1, Math.max(0, volumeWidth - 2), 22);

    gp.config.saveConfig();
  }

  /**
   * Submenu dedicado de seleção de idioma.
   * Lista os idiomas retornados por LanguageManager.getAvailableLanguages()
   * e adiciona, ao final, uma opção "Voltar".
   *
   * Navegação:
   * - W/S: já é tratada no KeyHandler (optionsState + getOptionsMaxCommandNum)
   * - ENTER:
   * - se commandNum < langs.size(): troca o idioma
   * - se commandNum == langs.size(): volta para subState 0 (Opções)
   */
  public void optionsLanguage(int frameX, int frameY) {
    var langManager = gp.getLanguageManager();
    java.util.List<LanguageManager.LanguageOption> langs = langManager.getAvailableLanguages();

    int textX;
    int textY;

    // Título
    String title = t("ui.options.languageTitle");
    textX = getXforCenteredText(title);
    textY = frameY + GamePanel.TILE_SIZE;
    drawTextShadow(title, textX, textY, COL_TEXT, Color.black, 2);

    // Idioma atual
    textX = frameX + GamePanel.TILE_SIZE;
    textY += GamePanel.TILE_SIZE * 2;
    String currentLabel = t("ui.options.languageCurrent") + ": " + langManager.getCurrentLanguageDisplayName();
    g2.setColor(COL_TEXT_DIM);
    g2.drawString(currentLabel, textX, textY);

    // Lista de idiomas
    textY += GamePanel.TILE_SIZE;
    g2.setColor(COL_TEXT);

    var keyH = gp.getKeyH();

    for (int i = 0; i < langs.size(); i++) {
      LanguageManager.LanguageOption opt = langs.get(i);
      String name = opt.displayName();

      g2.drawString(name, textX, textY);
      if (commandNum == i) {
        g2.drawString(">", textX - 25, textY);
        if (keyH.isEnterPressed()) {
          // Troca idioma
          langManager.setCurrentLanguage(opt.code());
          gp.config.saveConfig();
          gp.playSE(9);
        }
      }
      textY += GamePanel.TILE_SIZE;
    }

    // Opção "Voltar" (último índice)
    int backIndex = langs.size();
    g2.drawString(t("ui.common.back"), textX, textY);
    if (commandNum == backIndex) {
      g2.drawString(">", textX - 25, textY);
      if (keyH.isEnterPressed()) {
        subState = 0; // volta para opções principais
        commandNum = 4; // reposiciona o cursor em "Idioma"
        keyH.setEnterPressed(false);
      }
    }
  }

  private void cycleLanguage() {
    LanguageManager languageManager = gp.getLanguageManager();
    java.util.List<LanguageManager.LanguageOption> langs = languageManager.getAvailableLanguages();

    if (langs == null || langs.isEmpty()) {
      return;
    }

    String currentCode = languageManager.getCurrentLanguageCode();
    int index = 0;

    for (int i = 0; i < langs.size(); i++) {
      if (langs.get(i).code().equals(currentCode)) {
        index = i;
        break;
      }
    }

    int nextIndex = (index + 1) % langs.size();
    String nextCode = langs.get(nextIndex).code();

    languageManager.setCurrentLanguage(nextCode);
    gp.config.saveConfig();
    gp.playSE(9);
  }

  public void optionsFullScreenNotification(int frameX, int frameY) {
    int textX = frameX + GamePanel.TILE_SIZE;
    int textY = frameY + GamePanel.TILE_SIZE * 3;

    currentDialogue = t("ui.options.fullscreenNotice");

    g2.setColor(COL_TEXT);
    for (String line : currentDialogue.split("\n")) {
      g2.drawString(line, textX, textY);
      textY += 32;
    }

    textY = frameY + GamePanel.TILE_SIZE * 9;
    g2.drawString(t("ui.common.back"), textX, textY);
    if (commandNum == 0) {
      g2.drawString(">", textX - 25, textY);
      if (gp.getKeyH().isEnterPressed()) {
        subState = 0;
      }
    }
  }

  public void optionsControl(int frameX, int frameY) {
    int textX;
    int textY;

    String text = t("ui.controls.title");
    textX = getXforCenteredText(text);
    textY = frameY + GamePanel.TILE_SIZE;
    drawTextShadow(text, textX, textY, COL_TEXT, Color.black, 2);

    textX = frameX + GamePanel.TILE_SIZE;
    textY += GamePanel.TILE_SIZE;
    g2.setColor(COL_TEXT);
    g2.drawString(t("ui.controls.move"), textX, textY);
    textY += GamePanel.TILE_SIZE;
    g2.drawString(t("ui.controls.confirmAttack"), textX, textY);
    textY += GamePanel.TILE_SIZE;
    g2.drawString(t("ui.controls.shootCast"), textX, textY);
    textY += GamePanel.TILE_SIZE;
    g2.drawString(t("ui.controls.characterScreen"), textX, textY);
    textY += GamePanel.TILE_SIZE;
    g2.drawString(t("ui.controls.pause"), textX, textY);
    textY += GamePanel.TILE_SIZE;
    g2.drawString(t("ui.controls.options"), textX, textY);

    textX = frameX + GamePanel.TILE_SIZE * 6;
    textY = frameY + GamePanel.TILE_SIZE * 2;
    g2.drawString("WASD", textX, textY);
    textY += GamePanel.TILE_SIZE;
    g2.drawString("ENTER", textX, textY);
    textY += GamePanel.TILE_SIZE;
    g2.drawString("F", textX, textY);
    textY += GamePanel.TILE_SIZE;
    g2.drawString("C", textX, textY);
    textY += GamePanel.TILE_SIZE;
    g2.drawString("P", textX, textY);
    textY += GamePanel.TILE_SIZE;
    g2.drawString("ESC", textX, textY);

    // Voltar
    textX = frameX + GamePanel.TILE_SIZE;
    textY = frameY + GamePanel.TILE_SIZE * 9;
    g2.drawString(t("ui.common.back"), textX, textY);
    if (commandNum == 0) {
      g2.drawString(">", textX - 25, textY);
      if (gp.getKeyH().isEnterPressed()) {
        subState = 0;
        commandNum = 3;
      }
    }
  }

  public void optionsEndGameConfirmation(int frameX, int frameY) {
    int textX = frameX + GamePanel.TILE_SIZE;
    int textY = frameY + GamePanel.TILE_SIZE * 3;

    currentDialogue = t("ui.endGame.confirmation");

    g2.setColor(COL_TEXT);
    for (String line : currentDialogue.split("\n")) {
      g2.drawString(line, textX, textY);
      textY += 32;
    }

    // SIM
    String text = t("ui.common.yes");
    textX = getXforCenteredText(text);
    textY += GamePanel.TILE_SIZE * 2;
    g2.drawString(text, textX, textY);
    var keyH = gp.getKeyH();
    if (commandNum == 0) {
      g2.drawString(">", textX - 25, textY);
      if (keyH.isEnterPressed()) {
        subState = 0;
        commandNum = 0;
        titleScreenState = 0;
        gp.stopMusic();
        gp.setGameState(GamePanel.TITLE_STATE);
        keyH.setEnterPressed(false);
        gp.resetGame(true);
      }
    }

    // NÃO
    text = t("ui.common.no");
    textX = getXforCenteredText(text);
    textY += GamePanel.TILE_SIZE;
    g2.drawString(text, textX, textY);
    if (commandNum == 1) {
      g2.drawString(">", textX - 25, textY);
      if (keyH.isEnterPressed()) {
        subState = 0;
        commandNum = 4;
        keyH.setEnterPressed(false);
      }
    }
  }

  public void drawTradeScreen() {
    switch (subState) {
      case 0 -> tradeSelect();
      case 1 -> tradeBuy();
      case 2 -> tradeSell();
      default -> {
        /* no-op */ }
    }
    gp.getKeyH().setEnterPressed(false);
  }

  public void tradeSelect() {
    npc.setDialogueSet(0);
    drawDialogueScreen();

    // JANELA
    int x = GamePanel.TILE_SIZE * 15;
    int y = GamePanel.TILE_SIZE * 4;
    int width = GamePanel.TILE_SIZE * 3;
    int height = (int) (GamePanel.TILE_SIZE * 3.5);
    drawSubWindow(x, y, width, height);

    // TEXTOS
    x += GamePanel.TILE_SIZE;
    y += GamePanel.TILE_SIZE;

    var keyH = gp.getKeyH();

    g2.drawString(t("ui.trade.buy"), x, y);
    if (commandNum == 0) {
      g2.drawString(">", x - 24, y);
      if (keyH.isEnterPressed()) {
        subState = 1;
      }
    }
    y += GamePanel.TILE_SIZE;

    g2.drawString(t("ui.trade.sell"), x, y);
    if (commandNum == 1) {
      g2.drawString(">", x - 24, y);
      if (keyH.isEnterPressed()) {
        subState = 2;
      }
    }
    y += GamePanel.TILE_SIZE;

    g2.drawString(t("ui.trade.leave"), x, y);
    if (commandNum == 2) {
      g2.drawString(">", x - 24, y);
      if (keyH.isEnterPressed()) {
        commandNum = 0;
        npc.startDialogue(npc, 1);
      }
    }
  }

  public void tradeBuy() {
    var player = gp.getPlayer();

    // INVENTÁRIO DO JOGADOR
    drawInventory(player, false);

    // INVENTÁRIO DO NPC
    drawInventory(npc, true);

    // JANELA DE DICA
    int x = GamePanel.TILE_SIZE * 2;
    int y = GamePanel.TILE_SIZE * 9;
    int width = GamePanel.TILE_SIZE * 6;
    int height = GamePanel.TILE_SIZE * 2;
    drawSubWindow(x, y, width, height);
    g2.drawString(t("ui.trade.hintBack"), x + 24, y + 60);

    // JANELA DE MOEDAS DO JOGADOR
    x = GamePanel.TILE_SIZE * 12;
    y = GamePanel.TILE_SIZE * 9;
    width = GamePanel.TILE_SIZE * 6;
    height = GamePanel.TILE_SIZE * 2;
    drawSubWindow(x, y, width, height);
    g2.drawString(t("ui.trade.yourCoins") + " " + player.getCoin(), x + 24, y + 60);

    // JANELA DE PREÇO
    int itemIndex = getItemIndexOnSlot(npcSlotCol, npcSlotRow);
    if (itemIndex < npc.getInventory().size()) {
      x = (int) (GamePanel.TILE_SIZE * 5.5);
      y = (int) (GamePanel.TILE_SIZE * 5.5);
      width = (int) (GamePanel.TILE_SIZE * 2.5);
      height = GamePanel.TILE_SIZE;
      drawSubWindow(x, y, width, height);
      g2.drawImage(coin, x + 10, y + 8, 32, 32, null);

      int price = npc.getInventory().get(itemIndex).getPrice();
      String text = String.valueOf(price);
      x = getXforAlignToRightText(text, GamePanel.TILE_SIZE * 8 - 20);
      g2.drawString(text, x, y + 34);

      // COMPRAR UM ITEM
      if (gp.getKeyH().isEnterPressed()) {
        if (price > player.getCoin()) {
          subState = 0;
          npc.startDialogue(npc, 2);
        } else {
          if (player.canObtainItem(npc.getInventory().get(itemIndex))) {
            player.setCoin(player.getCoin() - price);
          } else {
            subState = 0;
            npc.startDialogue(npc, 3);
          }
        }
      }
    }
  }

  public void tradeSell() {
    var player = gp.getPlayer();

    // INVENTÁRIO DO JOGADOR
    drawInventory(player, true);

    int x;
    int y;
    int width;
    int height;

    // JANELA DE DICA
    x = GamePanel.TILE_SIZE * 2;
    y = GamePanel.TILE_SIZE * 9;
    width = GamePanel.TILE_SIZE * 6;
    height = GamePanel.TILE_SIZE * 2;
    drawSubWindow(x, y, width, height);
    g2.drawString(t("ui.trade.hintBack"), x + 24, y + 60);

    // JANELA DE MOEDAS DO JOGADOR
    x = GamePanel.TILE_SIZE * 12;
    y = GamePanel.TILE_SIZE * 9;
    width = GamePanel.TILE_SIZE * 6;
    height = GamePanel.TILE_SIZE * 2;
    drawSubWindow(x, y, width, height);
    g2.drawString(t("ui.trade.yourCoins") + " " + player.getCoin(), x + 24, y + 60);

    // JANELA DE PREÇO
    int itemIndex = getItemIndexOnSlot(playerSlotCol, playerSlotRow);
    if (itemIndex < player.getInventory().size()) {
      x = (int) (GamePanel.TILE_SIZE * 15.5);
      y = (int) (GamePanel.TILE_SIZE * 5.5);
      width = (int) (GamePanel.TILE_SIZE * 2.5);
      height = GamePanel.TILE_SIZE;
      drawSubWindow(x, y, width, height);
      g2.drawImage(coin, x + 10, y + 8, 32, 32, null);

      int price = player.getInventory().get(itemIndex).getPrice() / 2;
      String text = String.valueOf(price);
      x = getXforAlignToRightText(text, GamePanel.TILE_SIZE * 18 - 20);
      g2.drawString(text, x, y + 34);

      // VENDER UM ITEM
      if (gp.getKeyH().isEnterPressed()) {

        if (player.getInventory().get(itemIndex) == player.getCurrentWeapon()
            || player.getInventory().get(itemIndex) == player.getCurrentShield()
            || player.getInventory().get(itemIndex) == player.getCurrentBoots()
            || player.getInventory().get(itemIndex) == player.getCurrentHelmet()
            || player.getInventory().get(itemIndex) == player.getCurrentChest()
            || player.getInventory().get(itemIndex) == player.getCurrentLegs()) {

          commandNum = 0;
          subState = 0;
          npc.startDialogue(npc, 4);
        } else {
          if (player.getInventory().get(itemIndex).getAmount() > 1) {
            player.getInventory().get(itemIndex)
                .setAmount(player.getInventory().get(itemIndex).getAmount() - 1);
          } else {
            player.getInventory().remove(itemIndex);
          }
          player.setCoin(player.getCoin() + price);
        }
      }
    }
  }

  public void drawTransition() {
    counter++;
    g2.setColor(new Color(0, 0, 0, counter * 5));
    g2.fillRect(0, 0, GamePanel.SCREEN_WIDTH, GamePanel.SCREEN_HEIGHT);

    if (counter == 50) {
      counter = 0;
      gp.setGameState(GamePanel.PLAY_STATE);

      var eventHandler = gp.geteHandler();
      var player = gp.getPlayer();

      gp.setCurrentMap(eventHandler.tempMap);

      player.setWorldX(GamePanel.TILE_SIZE * eventHandler.tempCol);
      player.setWorldY(GamePanel.TILE_SIZE * eventHandler.tempRow);

      eventHandler.previousEventX = player.getWorldX();
      eventHandler.previousEventY = player.getWorldY();

      gp.changeArea();
    }
  }

  public void drawSleepScreen() {
    counter++;

    final var lighting = gp.eManager.getLighting();
    if (lighting == null) {
      counter = 0;
      gp.setGameState(GamePanel.PLAY_STATE);
      return;
    }

    final int TRANSITION_TICKS = 120;
    final float STEP = 0.01f;

    if (counter < TRANSITION_TICKS) {
      lighting.setFilterAlpha(lighting.getFilterAlpha() + STEP);
    } else {
      lighting.setFilterAlpha(lighting.getFilterAlpha() - STEP);
      if (lighting.getFilterAlpha() <= 0f) {
        counter = 0;
        lighting.setDayStateToDay();
        lighting.setDayCounter(0);
        gp.setGameState(GamePanel.PLAY_STATE);
        gp.getPlayer().getImage();
      }
    }
  }

  // --- Índice do slot ---
  public int getItemIndexOnSlot(int slotCol, int slotRow) {
    return slotCol + (slotRow * 5);
  }

  // --- Subjanela leve ---
  public void drawSubWindow(int x, int y, int width, int height) {
    Graphics2D g = (Graphics2D) g2.create();

    g.setColor(COL_PANEL);
    g.fillRoundRect(x, y, width, height, 14, 14);

    g.setStroke(new BasicStroke(2f));
    g.setColor(COL_PANEL_BORDER);
    g.drawRoundRect(x, y, width, height, 14, 14);

    g.dispose();
  }

  // Centralizada por tamanho
  private void drawSubWindowCenter(int w, int h) {
    int x = (GamePanel.SCREEN_WIDTH - w) / 2;
    int y = (GamePanel.SCREEN_HEIGHT - h) / 2;
    drawSubWindow(x, y, w, h);
  }

  // --- Helpers visuais ---
  private void drawMenuItem(String text, int centerY, boolean selected) {
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

  private void useCrispPixelText(Graphics2D g) {
    g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
    g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
    g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
  }

  private void useSmoothUiText(Graphics2D g) {
    g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
    g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
    g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
  }

  private void drawTextShadowCentered(String text, int y, Color fill, Color shadow, int offset) {
    int x = getXforCenteredText(text);
    drawTextShadow(text, x, y, fill, shadow, offset);
  }

  private void drawTextShadow(String text, int x, int y, Color fill, Color shadow, int offset) {
    g2.setColor(shadow);
    g2.drawString(text, x + offset, y + offset);
    g2.setColor(fill);
    g2.drawString(text, x, y);
  }

  // --- Utils ---
  public int getXforCenteredText(String text) {
    int length = (int) g2.getFontMetrics().getStringBounds(text, g2).getWidth();
    return GamePanel.SCREEN_WIDTH / 2 - length / 2;
  }

  public void drawCharacterScreen() {
    final int frameX = GamePanel.TILE_SIZE * 2;
    final int frameY = GamePanel.TILE_SIZE;
    final int frameWidth = GamePanel.TILE_SIZE * 9; // um pouco mais largo
    final int frameHeight = GamePanel.TILE_SIZE * 10;
    drawSubWindow(frameX, frameY, frameWidth, frameHeight);

    var player = gp.getPlayer();

    g2.setColor(COL_TEXT);
    g2.setFont(g2.getFont().deriveFont(Font.BOLD, 30F));

    final int padding = 24;
    int textX = frameX + padding;
    int textY = frameY + padding;

    // ===== TÍTULO =====
    int titleOffsetY = 8;
    drawTextShadow(t("ui.character.title"), textX, textY + titleOffsetY, COL_TEXT, Color.black, 2);
    textY += 44;

    // ===== BLOCO SUPERIOR: PORTRAIT + HP/MP =====
    int portraitSize = GamePanel.TILE_SIZE * 3;
    int portraitX = textX;
    int portraitY = textY;

    // Fundo do portrait
    g2.setColor(new Color(10, 10, 15, 190));
    g2.fillRoundRect(portraitX, portraitY, portraitSize, portraitSize, 12, 12);

    // Sprite do player
    g2.drawImage(
        player.getDown1(),
        portraitX + portraitSize / 2 - GamePanel.TILE_SIZE,
        portraitY + portraitSize / 2 - GamePanel.TILE_SIZE,
        GamePanel.TILE_SIZE * 2,
        GamePanel.TILE_SIZE * 2,
        null);

    // Infos rápidas ao lado
    int infoX = portraitX + portraitSize + 24;
    int infoY = portraitY + 8;

    g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 22F));
    String nivelLabel = t("ui.character.levelPrefix") + " " + player.getLevel();
    drawTextShadow(nivelLabel, infoX, infoY, COL_TEXT, Color.black, 2);
    infoY += 30;

    // Barras HP / MP / XP
    int barWidth = GamePanel.TILE_SIZE * 4;
    int barHeight = 16;

    drawStatBar(
        infoX,
        infoY,
        barWidth,
        barHeight,
        player.getLife(),
        player.getMaxLife(),
        t("ui.stats.hp"));
    infoY += 30;

    drawStatBar(
        infoX,
        infoY,
        barWidth,
        barHeight,
        player.getMana(),
        player.getMaxMana(),
        t("ui.stats.mp"));

    infoY += 30;
    drawStatBar(
        infoX,
        infoY,
        barWidth,
        barHeight,
        player.getExp(),
        player.getNextLevelExp(),
        t("ui.stats.exp"));

    // ===== BLOCO INFERIOR ESQUERDA: ATRIBUTOS =====
    int statsBlockX = frameX + padding;
    int statsBlockY = portraitY + portraitSize + 40;

    g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 20F));
    int lineHeight = 24;

    String[] labels = {
        t("ui.character.stat.strength"),
        t("ui.character.stat.dexterity"),
        t("ui.character.stat.attack"),
        t("ui.character.stat.defense"),
        t("ui.character.stat.coins")
    };

    String[] values = {
        String.valueOf(player.getStrength()),
        String.valueOf(player.getDexterity()),
        String.valueOf(player.getAttack()),
        String.valueOf(player.getDefense()),
        String.valueOf(player.getCoin())
    };

    int tailX = frameX + frameWidth / 2 - 30;

    for (int i = 0; i < labels.length; i++) {
      g2.setColor(COL_TEXT_DIM);
      g2.drawString(labels[i], statsBlockX, statsBlockY);
      g2.setColor(COL_TEXT);
      String value = values[i];
      g2.drawString(value, getXforAlignToRightText(value, tailX), statsBlockY);
      statsBlockY += lineHeight;
    }

    // ===== BLOCO DIREITA: GRID DE EQUIPAMENTOS 3x2 =====
    int equipAreaPadding = 16;
    int equipAreaX = frameX + frameWidth / 2 + equipAreaPadding;
    int equipAreaRight = frameX + frameWidth - equipAreaPadding;
    int equipAreaWidth = equipAreaRight - equipAreaX;

    g2.setFont(g2.getFont().deriveFont(Font.BOLD, 22F));
    int equipTitleY = portraitY + portraitSize + 32;
    if (equipTitleY > statsBlockY - 8) {
      equipTitleY = statsBlockY - 8;
    }
    drawTextShadow(t("ui.character.equipmentsTitle"), equipAreaX, equipTitleY, COL_TEXT, Color.black, 2);

    int cols = 3;
    int rows = 2;

    int slotSize = GamePanel.TILE_SIZE;
    int hGap = Math.max(12, (equipAreaWidth - cols * slotSize) / (cols - 1));
    int vGap = 32;

    int gridStartY = equipTitleY + 28;

    // Ordem visual no grid (3 colunas x 2 linhas):
    // [ Capacete | Peitoral | Calça ]
    // [ Botas | Arma | Escudo ]
    String[] equipLabels = {
        t("ui.character.equipment.helmet"),
        t("ui.character.equipment.chest"),
        t("ui.character.equipment.legs"),
        t("ui.character.equipment.boots"),
        t("ui.character.equipment.weapon"),
        t("ui.character.equipment.shield")
    };

    Entity[] equipItems = {
        player.getCurrentHelmet(),
        player.getCurrentChest(),
        player.getCurrentLegs(),
        player.getCurrentBoots(),
        player.getCurrentWeapon(),
        player.getCurrentShield()
    };

    EquipmentGridLayout equipLayout = new EquipmentGridLayout(
        equipAreaX,
        gridStartY,
        slotSize,
        cols,
        rows,
        hGap,
        vGap);
    drawEquipmentGrid(equipLayout, equipLabels, equipItems);
  }

  // Auxiliar para evitar muitos parâmetros em drawEquipmentGrid
  private record EquipmentGridLayout(
      int startX,
      int startY,
      int slotSize,
      int cols,
      int rows,
      int hGap,
      int vGap) {
  }

  private void drawEquipmentGrid(
      EquipmentGridLayout layout,
      String[] labels,
      Entity[] items) {

    int index = 0;

    g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 16F));

    for (int row = 0; row < layout.rows(); row++) {
      for (int col = 0; col < layout.cols(); col++) {
        if (index >= labels.length || index >= items.length) {
          return;
        }

        int slotX = layout.startX() + col * (layout.slotSize() + layout.hGap());
        int slotY = layout.startY() + row * (layout.slotSize() + layout.vGap());

        // Label acima do slot
        g2.setColor(COL_TEXT_DIM);
        g2.drawString(labels[index], slotX, slotY - 6);

        // Caixa do slot
        int boxY = slotY;
        g2.setColor(COL_PANEL_BORDER);
        g2.drawRoundRect(slotX, boxY, layout.slotSize(), layout.slotSize(), 10, 10);
        g2.setColor(new Color(20, 24, 32, 220));
        g2.fillRoundRect(
            slotX + 1,
            boxY + 1,
            layout.slotSize() - 2,
            layout.slotSize() - 2,
            10,
            10);

        // Ícone do equipamento
        Entity item = items[index];
        if (item != null && item.getDown1() != null) {
          int iconMargin = 4;
          int iconSize = layout.slotSize() - iconMargin * 2;
          int iconX = slotX + iconMargin;
          int iconY = boxY + iconMargin;
          g2.drawImage(item.getDown1(), iconX, iconY, iconSize, iconSize, null);
        }

        index++;
      }
    }
  }

  private void drawStatBar(int x, int y, int width, int height, int current, int max, String label) {
    // Fundo
    g2.setColor(COL_PANEL_BORDER);
    g2.fillRoundRect(x, y, width, height, 8, 8);

    // Valor atual
    if (max > 0 && current > 0) {
      int filledWidth = (int) (width * (Math.min(current, max) / (double) max));
      g2.setColor(COL_ACCENT);
      g2.fillRoundRect(x, y, filledWidth, height, 8, 8);
    }

    // Label e texto (ex: "Vida 30/50")
    g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 18F));
    String text = label + " " + current + "/" + max;
    int textX = x + 8;
    int textY = y + height - 4;

    g2.setColor(Color.black);
    g2.drawString(text, textX + 1, textY + 1);
    g2.setColor(COL_TEXT);
    g2.drawString(text, textX, textY);
  }

  public int getXforAlignToRightText(String text, int tailX) {
    int length = (int) g2.getFontMetrics().getStringBounds(text, g2).getWidth();
    return tailX - length;
  }

  // --- Getters específicos ---
  public Font getMaruMonica() {
    return maruMonica;
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
}

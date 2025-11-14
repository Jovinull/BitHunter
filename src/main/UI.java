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

  // --- HUD: Vida & Mana ---
  public void drawPlayerLife() {
    final int iconSize = 32;
    final int colsPerRow = 8;

    int startX = GamePanel.TILE_SIZE / 2;
    int startY = GamePanel.TILE_SIZE / 2;

    int x = startX;
    int y = startY;
    int i = 0;

    int manaStartX = (GamePanel.TILE_SIZE / 2) - 5;
    int manaStartY;

    var player = gp.getPlayer();

    // --- MAX LIFE (corações vazios) ---
    int maxHearts = player.getMaxLife() / 2;
    while (i < maxHearts) {
      g2.drawImage(heartBlank, x, y, iconSize, iconSize, null);
      i++;
      x += iconSize;

      if (i % colsPerRow == 0) {
        x = startX;
        y += iconSize;
      }
    }

    // Base da mana logo abaixo dos corações
    manaStartY = y + iconSize;

    // --- VIDA ATUAL ---
    x = startX;
    y = startY;

    int fullHearts = player.getLife() / 2;
    int halfHeart = player.getLife() % 2;

    for (int idx = 0; idx < fullHearts; idx++) {
      g2.drawImage(heartFull, x, y, iconSize, iconSize, null);
      x += iconSize;
      if ((idx + 1) % colsPerRow == 0) {
        x = startX;
        y += iconSize;
      }
    }

    if (halfHeart == 1) {
      g2.drawImage(heartHalf, x, y, iconSize, iconSize, null);
    }

    // --- MANA (vazio) ---
    int crystalStep = iconSize + 3;
    x = manaStartX;
    y = manaStartY;
    i = 0;

    while (i < player.getMaxMana()) {
      g2.drawImage(crystalBlank, x, y, iconSize, iconSize, null);
      i++;
      x += crystalStep;

      if (i % colsPerRow == 0) {
        x = manaStartX;
        y += iconSize;
      }
    }

    // --- MANA ATUAL ---
    x = manaStartX;
    y = manaStartY;
    i = 0;

    while (i < player.getMana()) {
      g2.drawImage(crystalFull, x, y, iconSize, iconSize, null);
      i++;
      x += crystalStep;

      if (i % colsPerRow == 0) {
        x = manaStartX;
        y += iconSize;
      }
    }
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
      drawTextShadowCentered("Bit Hunter", GamePanel.TILE_SIZE * 3, COL_TEXT, Color.black, 3);

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
      drawMenuItem("NOVO JOGO", yMenuStart, commandNum == 0);
      drawMenuItem("CARREGAR JOGO", yMenuStart + MENU_LINE, commandNum == 1);
      drawMenuItem("SAIR", yMenuStart + MENU_LINE * 2, commandNum == 2);

      // Dica
      useCrispPixelText(g2);
      if (maruMonica != null) {
        g2.setFont(maruMonica.deriveFont(Font.PLAIN, 16F));
      }
      String hint = "W/S ou ↑/↓ navegar • ENTER confirma";
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
      drawTextShadowCentered("Selecione sua classe", cy - MENU_LINE, COL_TEXT, Color.black, 2);
      drawMenuItem("Guerreiro", cy, commandNum == 0);
      drawMenuItem("Ladrão", cy + MENU_LINE, commandNum == 1);
      drawMenuItem("Feiticeiro", cy + MENU_LINE * 2, commandNum == 2);
      drawMenuItem(LABEL_BACK, cy + MENU_LINE * 4, commandNum == 3);
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
    String text = "PAUSADO";
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

    final int slotXstart = frameX + 20;
    final int slotYstart = frameY + 20;
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

      if (entity.getInventory().get(i) == entity.getCurrentWeapon() ||
          entity.getInventory().get(i) == entity.getCurrentShield() ||
          entity.getInventory().get(i) == entity.getCurrentLight()) {
        g2.setColor(COL_ACCENT);
        g2.fillRoundRect(slotX, slotY, GamePanel.TILE_SIZE, GamePanel.TILE_SIZE, 8, 8);
      }

      g2.drawImage(entity.getInventory().get(i).getDown1(), slotX, slotY, null);

      // DISPLAY AMOUNT
      if (entity == gp.getPlayer() && entity.getInventory().get(i).getAmount() > 1) {
        g2.setFont(g2.getFont().deriveFont(32f));
        String s = String.valueOf(entity.getInventory().get(i).getAmount());
        int amountX = getXforAlignToRightText(s, slotX + 44);
        int amountY = slotY + GamePanel.TILE_SIZE;

        // SHADOW
        g2.setColor(new Color(60, 60, 60));
        g2.drawString(s, amountX, amountY);

        // NUMBER
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
      g2.setFont(g2.getFont().deriveFont(28F));

      for (String line : entity.getInventory().get(itemIndex).getDescription().split("\n")) {
        g2.drawString(line, textX, textY);
        textY += 28;
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
    String text = "GAME OVER";
    drawTextShadowCentered(text, GamePanel.TILE_SIZE * 4, COL_TEXT, Color.black, 4);

    g2.setFont(g2.getFont().deriveFont(44f));
    int y = GamePanel.TILE_SIZE * 8;

    drawMenuItem("Reviver", y, commandNum == 0);
    drawMenuItem("Sair", y + MENU_LINE, commandNum == 1);
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
      default -> {
        /* no-op */ }
    }

    gp.getKeyH().setEnterPressed(false);
  }

  public void optionsTop(int frameX, int frameY) {
    int textX;
    int textY;

    String text = "Opções";
    textX = getXforCenteredText(text);
    textY = frameY + GamePanel.TILE_SIZE;
    drawTextShadow(text, textX, textY, COL_TEXT, Color.black, 2);

    var keyH = gp.getKeyH();

    // TELA CHEIA
    textX = frameX + GamePanel.TILE_SIZE;
    textY = frameY + GamePanel.TILE_SIZE * 2;
    g2.setColor(COL_TEXT);
    g2.drawString("Tela cheia", textX, textY);
    if (commandNum == 0) {
      g2.drawString(">", textX - 25, textY);
      if (keyH.isEnterPressed()) {
        gp.setFullScreenOn(!gp.isFullScreenOn());
        subState = 1;
      }
    }

    // MÚSICA
    textY += GamePanel.TILE_SIZE;
    g2.drawString("Música", textX, textY);
    if (commandNum == 1) {
      g2.drawString(">", textX - 25, textY);
    }

    // EFEITOS SONOROS
    textY += GamePanel.TILE_SIZE;
    g2.drawString("Efeitos", textX, textY);
    if (commandNum == 2) {
      g2.drawString(">", textX - 25, textY);
    }

    // CONTROLES
    textY += GamePanel.TILE_SIZE;
    g2.drawString("Controles", textX, textY);
    if (commandNum == 3) {
      g2.drawString(">", textX - 25, textY);
      if (keyH.isEnterPressed()) {
        subState = 2;
        commandNum = 0;
      }
    }

    // ENCERRAR JOGO
    textY += GamePanel.TILE_SIZE;
    g2.drawString("Encerrar jogo", textX, textY);
    if (commandNum == 4) {
      g2.drawString(">", textX - 25, textY);
      if (keyH.isEnterPressed()) {
        subState = 3;
        commandNum = 0;
      }
    }

    // VOLTAR
    textY += GamePanel.TILE_SIZE * 3;
    g2.drawString(LABEL_BACK, textX, textY);
    if (commandNum == 5) {
      g2.drawString(">", textX - 25, textY);
      if (keyH.isEnterPressed()) {
        gp.setGameState(GamePanel.PLAY_STATE);
        commandNum = 0;
      }
    }

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

  public void optionsFullScreenNotification(int frameX, int frameY) {
    int textX = frameX + GamePanel.TILE_SIZE;
    int textY = frameY + GamePanel.TILE_SIZE * 3;

    currentDialogue = "A alteração terá efeito\napós reiniciar o jogo.";

    g2.setColor(COL_TEXT);
    for (String line : currentDialogue.split("\n")) {
      g2.drawString(line, textX, textY);
      textY += 32;
    }

    textY = frameY + GamePanel.TILE_SIZE * 9;
    g2.drawString(LABEL_BACK, textX, textY);
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

    String text = "Controles";
    textX = getXforCenteredText(text);
    textY = frameY + GamePanel.TILE_SIZE;
    drawTextShadow(text, textX, textY, COL_TEXT, Color.black, 2);

    textX = frameX + GamePanel.TILE_SIZE;
    textY += GamePanel.TILE_SIZE;
    g2.setColor(COL_TEXT);
    g2.drawString("Mover", textX, textY);
    textY += GamePanel.TILE_SIZE;
    g2.drawString("Confirmar/Atacar", textX, textY);
    textY += GamePanel.TILE_SIZE;
    g2.drawString("Atirar/Conjurar", textX, textY);
    textY += GamePanel.TILE_SIZE;
    g2.drawString("Tela do Personagem", textX, textY);
    textY += GamePanel.TILE_SIZE;
    g2.drawString("Pausar", textX, textY);
    textY += GamePanel.TILE_SIZE;
    g2.drawString("Opções", textX, textY);

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
    g2.drawString(LABEL_BACK, textX, textY);
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

    currentDialogue = "Sair do jogo e\nvoltar à tela de título?";

    g2.setColor(COL_TEXT);
    for (String line : currentDialogue.split("\n")) {
      g2.drawString(line, textX, textY);
      textY += 32;
    }

    // SIM
    String text = "Sim";
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
    text = "Não";
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

    g2.drawString("Comprar", x, y);
    if (commandNum == 0) {
      g2.drawString(">", x - 24, y);
      if (keyH.isEnterPressed()) {
        subState = 1;
      }
    }
    y += GamePanel.TILE_SIZE;

    g2.drawString("Vender", x, y);
    if (commandNum == 1) {
      g2.drawString(">", x - 24, y);
      if (keyH.isEnterPressed()) {
        subState = 2;
      }
    }
    y += GamePanel.TILE_SIZE;

    g2.drawString("Sair", x, y);
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
    g2.drawString("[ESC] Voltar", x + 24, y + 60);

    // JANELA DE MOEDAS DO JOGADOR
    x = GamePanel.TILE_SIZE * 12;
    y = GamePanel.TILE_SIZE * 9;
    width = GamePanel.TILE_SIZE * 6;
    height = GamePanel.TILE_SIZE * 2;
    drawSubWindow(x, y, width, height);
    g2.drawString("Suas moedas: " + player.getCoin(), x + 24, y + 60);

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
    g2.drawString("[ESC] Voltar", x + 24, y + 60);

    // JANELA DE MOEDAS DO JOGADOR
    x = GamePanel.TILE_SIZE * 12;
    y = GamePanel.TILE_SIZE * 9;
    width = GamePanel.TILE_SIZE * 6;
    height = GamePanel.TILE_SIZE * 2;
    drawSubWindow(x, y, width, height);
    g2.drawString("Suas moedas: " + player.getCoin(), x + 24, y + 60);

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
            || player.getInventory().get(itemIndex) == player.getCurrentShield()) {

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
    final int frameWidth = GamePanel.TILE_SIZE * 5;
    final int frameHeight = GamePanel.TILE_SIZE * 10;
    drawSubWindow(frameX, frameY, frameWidth, frameHeight);

    g2.setColor(COL_TEXT);
    g2.setFont(g2.getFont().deriveFont(32F));

    int textX = frameX + 20;
    int textY = frameY + GamePanel.TILE_SIZE;
    final int lineHeight = 30;

    // Labels
    g2.drawString("Nível", textX, textY);
    textY += lineHeight;
    g2.drawString("Vida", textX, textY);
    textY += lineHeight;
    g2.drawString("Mana", textX, textY);
    textY += lineHeight;
    g2.drawString("Força", textX, textY);
    textY += lineHeight;
    g2.drawString("Destreza", textX, textY);
    textY += lineHeight;
    g2.drawString("Ataque", textX, textY);
    textY += lineHeight;
    g2.drawString("Defesa", textX, textY);
    textY += lineHeight;
    g2.drawString("EXP", textX, textY);
    textY += lineHeight;
    g2.drawString("Próximo Nível", textX, textY);
    textY += lineHeight;
    g2.drawString("Moedas", textX, textY);
    textY += lineHeight + 8;
    g2.drawString("Arma", textX, textY);
    textY += lineHeight + 12;
    g2.drawString("Escudo", textX, textY);

    // Valores
    int tailX = (frameX + frameWidth) - 30;
    textY = frameY + GamePanel.TILE_SIZE;

    var player = gp.getPlayer();
    String value;

    value = String.valueOf(player.getLevel());
    g2.drawString(value, getXforAlignToRightText(value, tailX), textY);
    textY += lineHeight;

    value = player.getLife() + "/" + player.getMaxLife();
    g2.drawString(value, getXforAlignToRightText(value, tailX), textY);
    textY += lineHeight;

    value = player.getMana() + "/" + player.getMaxMana();
    g2.drawString(value, getXforAlignToRightText(value, tailX), textY);
    textY += lineHeight;

    value = String.valueOf(player.getStrength());
    g2.drawString(value, getXforAlignToRightText(value, tailX), textY);
    textY += lineHeight;

    value = String.valueOf(player.getDexterity());
    g2.drawString(value, getXforAlignToRightText(value, tailX), textY);
    textY += lineHeight;

    value = String.valueOf(player.getAttack());
    g2.drawString(value, getXforAlignToRightText(value, tailX), textY);
    textY += lineHeight;

    value = String.valueOf(player.getDefense());
    g2.drawString(value, getXforAlignToRightText(value, tailX), textY);
    textY += lineHeight;

    value = String.valueOf(player.getExp());
    g2.drawString(value, getXforAlignToRightText(value, tailX), textY);
    textY += lineHeight;

    value = String.valueOf(player.getNextLevelExp());
    g2.drawString(value, getXforAlignToRightText(value, tailX), textY);
    textY += lineHeight;

    value = String.valueOf(player.getCoin());
    g2.drawString(value, getXforAlignToRightText(value, tailX), textY);
    textY += lineHeight;

    if (player.getCurrentWeapon() != null) {
      g2.drawImage(player.getCurrentWeapon().getDown1(), tailX - GamePanel.TILE_SIZE, textY - 24, null);
    }
    textY += GamePanel.TILE_SIZE;
    if (player.getCurrentShield() != null) {
      g2.drawImage(player.getCurrentShield().getDown1(), tailX - GamePanel.TILE_SIZE, textY - 24, null);
    }
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

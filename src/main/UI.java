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

import entity.Entity;
import object.OBJ_Coin_Bronze;
import object.OBJ_Heart;
import object.OBJ_ManaCrystal;

public class UI {

  // --- Referências do jogo ---
  private final GamePanel gp;
  Graphics2D g2;

  // --- Fontes & HUD ---
  public Font maruMonica;
  Font purisaB;
  BufferedImage heart_full, heart_half, heart_blank, crystal_full, crystal_blank, coin;

  // --- Estado de UI ---
  public boolean messageOn = false;
  private final ArrayList<String> message = new ArrayList<>();
  private final ArrayList<Integer> messageCounter = new ArrayList<>();
  public boolean gameFinished = false;
  public String currentDialogue = "";
  public int commandNum = 0;
  public int titleScreenState = 0;
  public int playerSlotCol = 0;
  public int playerSlotRow = 0;
  public int npcSlotCol = 0;
  public int npcSlotRow = 0;
  int subState = 0;
  int counter = 0;
  public Entity npc;
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

  // Altura de linha padrão de menus
  private static final int MENU_LINE = 48;

  public UI(GamePanel gp) {
    this.gp = gp;

    // Carrega fontes com try-with-resources
    try (InputStream is = getClass().getResourceAsStream("/font/x12y16pxMaruMonica.ttf")) {
      if (is != null) {
        maruMonica = Font.createFont(Font.TRUETYPE_FONT, is);
      }
    } catch (FontFormatException | IOException e) {
      System.err.println("Falha ao carregar fonte MaruMonica: " + e.getMessage());
    }

    try (InputStream is = getClass().getResourceAsStream("/font/Purisa Bold.ttf")) {
      if (is != null) {
        purisaB = Font.createFont(Font.TRUETYPE_FONT, is);
      }
    } catch (FontFormatException | IOException e) {
      System.err.println("Falha ao carregar fonte Purisa Bold: " + e.getMessage());
    }

    // HUD (via getters, sem acessar campos diretamente)
    Entity heart = new OBJ_Heart(gp);
    heart_full = heart.getBaseImage();
    heart_half = heart.getAltImage1();
    heart_blank = heart.getAltImage2();

    Entity crystal = new OBJ_ManaCrystal(gp);
    crystal_full = crystal.getBaseImage();
    crystal_blank = crystal.getAltImage1();

    Entity bronzeCoin = new OBJ_Coin_Bronze(gp);
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

    boolean smoothUi = gp.gameState == gp.titleState ||
        gp.gameState == gp.pauseState ||
        gp.gameState == gp.gameOverState;

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

    g2.setColor(COL_TEXT);

    if (gp.gameState == gp.titleState) {
      drawTitleScreen();
      return;
    }

    if (gp.gameState == gp.playState) {
      drawPlayerLife();
      drawMonsterLife();
      drawMessage();
      return;
    }

    if (gp.gameState == gp.pauseState) {
      drawPlayerLife();
      drawPauseScreen();
      return;
    }

    if (gp.gameState == gp.dialogueState) {
      drawDialogueScreen();
      return;
    }

    if (gp.gameState == gp.characterState) {
      drawCharacterScreen();
      drawInventory(gp.player, true);
      return;
    }

    if (gp.gameState == gp.optionsState) {
      drawOptionsScreen();
      return;
    }

    if (gp.gameState == gp.gameOverState) {
      drawGameOverScreen();
    }

    if (gp.gameState == gp.transitionState) {
      drawTransition();
    }

    if (gp.gameState == gp.tradeState) {
      drawTradeScreen();
    }

    if (gp.gameState == gp.sleepState) {
      drawSleepScreen();
    }
  }

  // --- HUD: Vida & Mana ---
  public void drawPlayerLife() {
    final int iconSize = 32;
    final int colsPerRow = 8;

    int startX = gp.tileSize / 2;
    int startY = gp.tileSize / 2;

    int x = startX;
    int y = startY;
    int i = 0;

    int manaStartX = (gp.tileSize / 2) - 5;
    int manaStartY;

    // --- MAX LIFE (corações vazios) ---
    int maxHearts = gp.player.getMaxLife() / 2;
    while (i < maxHearts) {
      g2.drawImage(heart_blank, x, y, iconSize, iconSize, null);
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

    int fullHearts = gp.player.getLife() / 2;
    int halfHeart = gp.player.getLife() % 2;

    for (int idx = 0; idx < fullHearts; idx++) {
      g2.drawImage(heart_full, x, y, iconSize, iconSize, null);
      x += iconSize;
      if ((idx + 1) % colsPerRow == 0) {
        x = startX;
        y += iconSize;
      }
    }

    if (halfHeart == 1) {
      g2.drawImage(heart_half, x, y, iconSize, iconSize, null);
      x += iconSize;
      if (((fullHearts + 1) % colsPerRow) == 0) {
        x = startX;
        y += iconSize;
      }
    }

    // --- MANA (vazio) ---
    int crystalStep = iconSize + 3;
    x = manaStartX;
    y = manaStartY;
    i = 0;

    while (i < gp.player.getMaxMana()) {
      g2.drawImage(crystal_blank, x, y, iconSize, iconSize, null);
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

    while (i < gp.player.getMana()) {
      g2.drawImage(crystal_full, x, y, iconSize, iconSize, null);
      i++;
      x += crystalStep;

      if (i % colsPerRow == 0) {
        x = manaStartX;
        y += iconSize;
      }
    }
  }

  public void drawMonsterLife() {
    for (int i = 0; i < gp.monster[1].length; i++) {
      Entity monster = gp.monster[gp.currentMap][i];
      if (monster != null && monster.inCamera()) {
        if (monster.isHpBarOn() && !monster.isBoss()) {

          double oneScale = (double) gp.tileSize / monster.getMaxLife();
          double hpBarValue = oneScale * monster.getLife();

          g2.setColor(new Color(35, 35, 35));
          g2.fillRect(monster.getScreenX() - 1, monster.getScreenY() - 16, gp.tileSize + 2, 12);

          g2.setColor(new Color(255, 0, 30));
          g2.fillRect(monster.getScreenX(), monster.getScreenY() - 15, (int) hpBarValue, 10);

          monster.setHpBarCounter(monster.getHpBarCounter() + 1);
          if (monster.getHpBarCounter() > 600) {
            monster.setHpBarCounter(0);
            monster.setHpBarOn(false);
          }
        } else if (monster.isBoss()) {

          double oneScale = (double) gp.tileSize * 8 / monster.getMaxLife();
          double hpBarValue = oneScale * monster.getLife();

          int x = gp.screenWidth / 2 - gp.tileSize * 4;
          int y = gp.tileSize * 10;

          g2.setColor(new Color(35, 35, 35));
          g2.fillRect(x - 1, y - 1, gp.tileSize * 8 + 2, 22);

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
    int messageX = gp.tileSize;
    int messageY = gp.tileSize * 4;

    g2.setFont(g2.getFont().deriveFont(Font.BOLD, 32F));

    for (int i = 0; i < message.size(); i++) {
      String m = message.get(i);
      if (m != null) {
        // sombra simples
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
          i--;
        }
      }
    }
  }

  // --- Título ---
  public void drawTitleScreen() {
    g2.setColor(COL_BG);
    g2.fillRect(0, 0, gp.screenWidth, gp.screenHeight);

    if (titleScreenState == 0) {
      // --- TELA 0: menu principal ---
      if (purisaB != null) {
        g2.setFont(purisaB.deriveFont(Font.BOLD, 112F));
      }
      drawTextShadowCentered("Bit Hunter", gp.tileSize * 3, COL_TEXT, Color.black, 3);

      int xImg = gp.screenWidth / 2 - (gp.tileSize * 2) / 2;
      int yImg = gp.tileSize * 3 + gp.tileSize * 2;
      g2.drawImage(gp.player.getDown1(), xImg, yImg, gp.tileSize * 2, gp.tileSize * 2, null);

      if (purisaB != null) {
        g2.setFont(purisaB.deriveFont(Font.BOLD, 48F));
      }
      int yMenuStart = yImg + (int) (gp.tileSize * 3.2);
      drawMenuItem("NOVO JOGO", yMenuStart, commandNum == 0);
      drawMenuItem("CARREGAR JOGO", yMenuStart + MENU_LINE, commandNum == 1);
      drawMenuItem("SAIR", yMenuStart + MENU_LINE * 2, commandNum == 2);

      // Dica em MaruMonica
      useCrispPixelText(g2);
      if (maruMonica != null) {
        g2.setFont(maruMonica.deriveFont(Font.PLAIN, 16F));
      }
      String hint = "W/S ou ↑/↓ navegar • ENTER confirma";
      drawTextShadowCentered(hint, gp.screenHeight - gp.tileSize, COL_TEXT_DIM, Color.black, 2);
      return;
    }

    if (titleScreenState == 1) {
      if (purisaB != null) {
        g2.setFont(purisaB.deriveFont(Font.BOLD, 36F));
      }
      drawSubWindowCenter(gp.tileSize * 10, gp.tileSize * 8);
      int cy = gp.screenHeight / 2 - gp.tileSize;
      drawTextShadowCentered("Selecione sua classe", cy - MENU_LINE, COL_TEXT, Color.black, 2);
      drawMenuItem("Guerreiro", cy, commandNum == 0);
      drawMenuItem("Ladrão", cy + MENU_LINE, commandNum == 1);
      drawMenuItem("Feiticeiro", cy + MENU_LINE * 2, commandNum == 2);
      drawMenuItem("Voltar", cy + MENU_LINE * 4, commandNum == 3);
    }
  }

  // --- Pause ---
  public void drawPauseScreen() {
    int w = (int) (gp.screenWidth * 0.6);
    int h = gp.tileSize * 4;
    int x = (gp.screenWidth - w) / 2;
    int y = (gp.screenHeight - h) / 2;
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
    int x = gp.tileSize * 3;
    int y = gp.tileSize / 2;
    int width = gp.screenWidth - (gp.tileSize * 6);
    int height = gp.tileSize * 4;
    drawSubWindow(x, y, width, height);

    if (maruMonica != null) {
      g2.setFont(maruMonica.deriveFont(Font.PLAIN, 32F));
    }
    x += gp.tileSize;
    y += gp.tileSize;

    String[][] ds = npc.getDialogues();
    int set = npc.getDialogueSet();
    int idx = npc.getDialogueIndex();

    if (ds[set][idx] != null) {
      char[] characters = ds[set][idx].toCharArray();

      if (charIndex < characters.length) {
        gp.playSE(17);
        String s = String.valueOf(characters[charIndex]);
        combinedText = combinedText + s;
        currentDialogue = combinedText;
        charIndex++;
      }

      if (gp.keyH.enterPressed) {
        charIndex = 0;
        combinedText = "";

        if (gp.gameState == gp.dialogueState || gp.gameState == gp.cutsceneState) {
          npc.setDialogueIndex(idx + 1);
          gp.keyH.enterPressed = false;
        }
      }
    } else {
      npc.setDialogueIndex(0);

      if (gp.gameState == gp.dialogueState) {
        gp.gameState = gp.playState;
      }

      if (gp.gameState == gp.cutsceneState) {
        gp.csManager.scenePhase++;
      }
    }

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

    if (entity == gp.player) {
      frameX = gp.tileSize * 12;
      frameY = gp.tileSize;
      frameWidth = gp.tileSize * 6;
      frameHeight = gp.tileSize * 5;
      slotCol = playerSlotCol;
      slotRow = playerSlotRow;
    } else {
      frameX = gp.tileSize * 2;
      frameY = gp.tileSize;
      frameWidth = gp.tileSize * 6;
      frameHeight = gp.tileSize * 5;
      slotCol = npcSlotCol;
      slotRow = npcSlotRow;
    }

    drawSubWindow(frameX, frameY, frameWidth, frameHeight);

    final int slotXstart = frameX + 20;
    final int slotYstart = frameY + 20;
    int slotX = slotXstart;
    int slotY = slotYstart;
    int slotSize = gp.tileSize + 3;

    for (int i = 0; i < entity.getInventory().size(); i++) {

      if (entity.getInventory().get(i) == entity.getCurrentWeapon() ||
          entity.getInventory().get(i) == entity.getCurrentShield() ||
          entity.getInventory().get(i) == entity.getCurrentLight()) {
        g2.setColor(COL_ACCENT);
        g2.fillRoundRect(slotX, slotY, gp.tileSize, gp.tileSize, 8, 8);
      }

      g2.drawImage(entity.getInventory().get(i).getDown1(), slotX, slotY, null);

      // DISPLAY AMOUNT
      if (entity == gp.player && entity.getInventory().get(i).getAmount() > 1) {
        g2.setFont(g2.getFont().deriveFont(32f));
        String s = String.valueOf(entity.getInventory().get(i).getAmount());
        int amountX = getXforAlignToRightText(s, slotX + 44);
        int amountY = slotY + gp.tileSize;

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
        slotY += gp.tileSize;
      }
    }

    // Cursor
    if (cursor) {
      int cursorX = slotXstart + (slotSize * slotCol);
      int cursorY = slotYstart + (slotSize * slotRow);
      int cursorWidth = gp.tileSize;
      int cursorHeight = gp.tileSize;

      g2.setColor(COL_TEXT);
      g2.setStroke(new BasicStroke(3));
      g2.drawRoundRect(cursorX, cursorY, cursorWidth, cursorHeight, 8, 8);

      // Descrição
      int dFrameX = frameX;
      int dFrameY = frameY + frameHeight;
      int dFrameWidth = frameWidth;
      int dFrameHeight = gp.tileSize * 3;

      int itemIndex = getItemIndexOnSlot(slotCol, slotRow);
      if (itemIndex < entity.getInventory().size()) {
        drawSubWindow(dFrameX, dFrameY, dFrameWidth, dFrameHeight);

        int textX = dFrameX + 20;
        int textY = dFrameY + gp.tileSize;
        g2.setColor(COL_TEXT);
        g2.setFont(g2.getFont().deriveFont(28F));

        for (String line : entity.getInventory().get(itemIndex).getDescription().split("\n")) {
          g2.drawString(line, textX, textY);
          textY += 28;
        }
      }
    }
  }

  // --- Game Over ---
  public void drawGameOverScreen() {
    g2.setColor(new Color(0, 0, 0, 140));
    g2.fillRect(0, 0, gp.screenWidth, gp.screenHeight);

    if (purisaB != null) {
      g2.setFont(purisaB.deriveFont(Font.BOLD, 96f));
    }
    String text = "GAME OVER";
    drawTextShadowCentered(text, gp.tileSize * 4, COL_TEXT, Color.black, 4);

    g2.setFont(g2.getFont().deriveFont(44f));
    int y = gp.tileSize * 8;

    drawMenuItem("Reviver", y, commandNum == 0);
    drawMenuItem("Sair", y + MENU_LINE, commandNum == 1);
  }

  // --- Opções ---
  public void drawOptionsScreen() {
    g2.setColor(COL_TEXT);
    g2.setFont(g2.getFont().deriveFont(32F));

    int frameX = gp.tileSize * 6;
    int frameY = gp.tileSize;
    int frameWidth = gp.tileSize * 8;
    int frameHeight = gp.tileSize * 10;
    drawSubWindow(frameX, frameY, frameWidth, frameHeight);

    switch (subState) {
      case 0 -> optionsTop(frameX, frameY);
      case 1 -> optionsFullScreenNotification(frameX, frameY);
      case 2 -> optionsControl(frameX, frameY);
      case 3 -> optionsEndGameConfirmation(frameX, frameY);
      default -> {
        /* no-op */ }
    }

    gp.keyH.enterPressed = false;
  }

  public void optionsTop(int frameX, int frameY) {
    int textX;
    int textY;

    String text = "Opções";
    textX = getXforCenteredText(text);
    textY = frameY + gp.tileSize;
    drawTextShadow(text, textX, textY, COL_TEXT, Color.black, 2);

    // TELA CHEIA
    textX = frameX + gp.tileSize;
    textY = frameY + gp.tileSize * 2;
    g2.setColor(COL_TEXT);
    g2.drawString("Tela cheia", textX, textY);
    if (commandNum == 0) {
      g2.drawString(">", textX - 25, textY);
      if (gp.keyH.enterPressed) {
        gp.fullScreenOn = !gp.fullScreenOn;
        subState = 1;
      }
    }

    // MÚSICA
    textY += gp.tileSize;
    g2.drawString("Música", textX, textY);
    if (commandNum == 1) {
      g2.drawString(">", textX - 25, textY);
    }

    // EFEITOS SONOROS
    textY += gp.tileSize;
    g2.drawString("Efeitos", textX, textY);
    if (commandNum == 2) {
      g2.drawString(">", textX - 25, textY);
    }

    // CONTROLES
    textY += gp.tileSize;
    g2.drawString("Controles", textX, textY);
    if (commandNum == 3) {
      g2.drawString(">", textX - 25, textY);
      if (gp.keyH.enterPressed) {
        subState = 2;
        commandNum = 0;
      }
    }

    // ENCERRAR JOGO
    textY += gp.tileSize;
    g2.drawString("Encerrar jogo", textX, textY);
    if (commandNum == 4) {
      g2.drawString(">", textX - 25, textY);
      if (gp.keyH.enterPressed) {
        subState = 3;
        commandNum = 0;
      }
    }

    // VOLTAR
    textY += gp.tileSize * 3;
    g2.drawString("Voltar", textX, textY);
    if (commandNum == 5) {
      g2.drawString(">", textX - 25, textY);
      if (gp.keyH.enterPressed) {
        gp.gameState = gp.playState;
        commandNum = 0;
      }
    }

    // CHECKBOX TELA CHEIA
    textX = frameX + (int) (gp.tileSize * 4.5);
    textY = frameY + gp.tileSize * 2 - 22;
    g2.setStroke(new BasicStroke(3));
    g2.setColor(COL_PANEL_BORDER);
    g2.drawRect(textX, textY, 24, 24);
    if (gp.fullScreenOn) {
      g2.setColor(COL_ACCENT);
      g2.fillRect(textX + 2, textY + 2, 20, 20);
    }

    // VOLUME MÚSICA
    textY += gp.tileSize;
    g2.setColor(COL_PANEL_BORDER);
    g2.drawRect(textX, textY, 120, 24);
    g2.setColor(COL_ACCENT);
    int volumeWidth = 24 * gp.music.volumeScale;
    g2.fillRect(textX + 1, textY + 1, Math.max(0, volumeWidth - 2), 22);

    // VOLUME EFEITOS
    textY += gp.tileSize;
    g2.setColor(COL_PANEL_BORDER);
    g2.drawRect(textX, textY, 120, 24);
    g2.setColor(COL_ACCENT);
    volumeWidth = 24 * gp.se.volumeScale;
    g2.fillRect(textX + 1, textY + 1, Math.max(0, volumeWidth - 2), 22);

    gp.config.saveConfig();
  }

  public void optionsFullScreenNotification(int frameX, int frameY) {
    int textX = frameX + gp.tileSize;
    int textY = frameY + gp.tileSize * 3;

    currentDialogue = "A alteração terá efeito\napós reiniciar o jogo.";

    g2.setColor(COL_TEXT);
    for (String line : currentDialogue.split("\n")) {
      g2.drawString(line, textX, textY);
      textY += 32;
    }

    textY = frameY + gp.tileSize * 9;
    g2.drawString("Voltar", textX, textY);
    if (commandNum == 0) {
      g2.drawString(">", textX - 25, textY);
      if (gp.keyH.enterPressed) {
        subState = 0;
      }
    }
  }

  public void optionsControl(int frameX, int frameY) {
    int textX;
    int textY;

    String text = "Controles";
    textX = getXforCenteredText(text);
    textY = frameY + gp.tileSize;
    drawTextShadow(text, textX, textY, COL_TEXT, Color.black, 2);

    textX = frameX + gp.tileSize;
    textY += gp.tileSize;
    g2.setColor(COL_TEXT);
    g2.drawString("Mover", textX, textY);
    textY += gp.tileSize;
    g2.drawString("Confirmar/Atacar", textX, textY);
    textY += gp.tileSize;
    g2.drawString("Atirar/Conjurar", textX, textY);
    textY += gp.tileSize;
    g2.drawString("Tela do Personagem", textX, textY);
    textY += gp.tileSize;
    g2.drawString("Pausar", textX, textY);
    textY += gp.tileSize;
    g2.drawString("Opções", textX, textY);

    textX = frameX + gp.tileSize * 6;
    textY = frameY + gp.tileSize * 2;
    g2.drawString("WASD", textX, textY);
    textY += gp.tileSize;
    g2.drawString("ENTER", textX, textY);
    textY += gp.tileSize;
    g2.drawString("F", textX, textY);
    textY += gp.tileSize;
    g2.drawString("C", textX, textY);
    textY += gp.tileSize;
    g2.drawString("P", textX, textY);
    textY += gp.tileSize;
    g2.drawString("ESC", textX, textY);

    // Voltar
    textX = frameX + gp.tileSize;
    textY = frameY + gp.tileSize * 9;
    g2.drawString("Voltar", textX, textY);
    if (commandNum == 0) {
      g2.drawString(">", textX - 25, textY);
      if (gp.keyH.enterPressed) {
        subState = 0;
        commandNum = 3;
      }
    }
  }

  public void optionsEndGameConfirmation(int frameX, int frameY) {
    int textX = frameX + gp.tileSize;
    int textY = frameY + gp.tileSize * 3;

    currentDialogue = "Sair do jogo e\nvoltar à tela de título?";

    g2.setColor(COL_TEXT);
    for (String line : currentDialogue.split("\n")) {
      g2.drawString(line, textX, textY);
      textY += 32;
    }

    // SIM
    String text = "Sim";
    textX = getXforCenteredText(text);
    textY += gp.tileSize * 2;
    g2.drawString(text, textX, textY);
    if (commandNum == 0) {
      g2.drawString(">", textX - 25, textY);
      if (gp.keyH.enterPressed) {
        subState = 0;
        commandNum = 0;
        titleScreenState = 0;
        gp.music.stop();
        gp.gameState = gp.titleState;
        gp.keyH.enterPressed = false;
        gp.resetGame(true);
      }
    }

    // NÃO
    text = "Não";
    textX = getXforCenteredText(text);
    textY += gp.tileSize;
    g2.drawString(text, textX, textY);
    if (commandNum == 1) {
      g2.drawString(">", textX - 25, textY);
      if (gp.keyH.enterPressed) {
        subState = 0;
        commandNum = 4;
        gp.keyH.enterPressed = false;
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
    gp.keyH.enterPressed = false;
  }

  public void tradeSelect() {
    npc.setDialogueSet(0);
    drawDialogueScreen();

    // JANELA
    int x = gp.tileSize * 15;
    int y = gp.tileSize * 4;
    int width = gp.tileSize * 3;
    int height = (int) (gp.tileSize * 3.5);
    drawSubWindow(x, y, width, height);

    // TEXTOS
    x += gp.tileSize;
    y += gp.tileSize;

    g2.drawString("Comprar", x, y);
    if (commandNum == 0) {
      g2.drawString(">", x - 24, y);
      if (gp.keyH.enterPressed) {
        subState = 1;
      }
    }
    y += gp.tileSize;

    g2.drawString("Vender", x, y);
    if (commandNum == 1) {
      g2.drawString(">", x - 24, y);
      if (gp.keyH.enterPressed) {
        subState = 2;
      }
    }
    y += gp.tileSize;

    g2.drawString("Sair", x, y);
    if (commandNum == 2) {
      g2.drawString(">", x - 24, y);
      if (gp.keyH.enterPressed) {
        commandNum = 0;
        npc.startDialogue(npc, 1);
      }
    }
  }

  public void tradeBuy() {
    // INVENTÁRIO DO JOGADOR
    drawInventory(gp.player, false);

    // INVENTÁRIO DO NPC
    drawInventory(npc, true);

    // JANELA DE DICA
    int x = gp.tileSize * 2;
    int y = gp.tileSize * 9;
    int width = gp.tileSize * 6;
    int height = gp.tileSize * 2;
    drawSubWindow(x, y, width, height);
    g2.drawString("[ESC] Voltar", x + 24, y + 60);

    // JANELA DE MOEDAS DO JOGADOR
    x = gp.tileSize * 12;
    y = gp.tileSize * 9;
    width = gp.tileSize * 6;
    height = gp.tileSize * 2;
    drawSubWindow(x, y, width, height);
    g2.drawString("Suas moedas: " + gp.player.getCoin(), x + 24, y + 60);

    // JANELA DE PREÇO
    int itemIndex = getItemIndexOnSlot(npcSlotCol, npcSlotRow);
    if (itemIndex < npc.getInventory().size()) {
      x = (int) (gp.tileSize * 5.5);
      y = (int) (gp.tileSize * 5.5);
      width = (int) (gp.tileSize * 2.5);
      height = gp.tileSize;
      drawSubWindow(x, y, width, height);
      g2.drawImage(coin, x + 10, y + 8, 32, 32, null);

      int price = npc.getInventory().get(itemIndex).getPrice();
      String text = String.valueOf(price);
      x = getXforAlignToRightText(text, gp.tileSize * 8 - 20);
      g2.drawString(text, x, y + 34);

      // COMPRAR UM ITEM
      if (gp.keyH.enterPressed) {
        if (price > gp.player.getCoin()) {
          subState = 0;
          npc.startDialogue(npc, 2);
        } else {
          if (gp.player.canObtainItem(npc.getInventory().get(itemIndex))) {
            gp.player.setCoin(gp.player.getCoin() - price);
          } else {
            subState = 0;
            npc.startDialogue(npc, 3);
          }
        }
      }
    }
  }

  public void tradeSell() {
    // INVENTÁRIO DO JOGADOR
    drawInventory(gp.player, true);

    int x;
    int y;
    int width;
    int height;

    // JANELA DE DICA
    x = gp.tileSize * 2;
    y = gp.tileSize * 9;
    width = gp.tileSize * 6;
    height = gp.tileSize * 2;
    drawSubWindow(x, y, width, height);
    g2.drawString("[ESC] Voltar", x + 24, y + 60);

    // JANELA DE MOEDAS DO JOGADOR
    x = gp.tileSize * 12;
    y = gp.tileSize * 9;
    width = gp.tileSize * 6;
    height = gp.tileSize * 2;
    drawSubWindow(x, y, width, height);
    g2.drawString("Suas moedas: " + gp.player.getCoin(), x + 24, y + 60);

    // JANELA DE PREÇO
    int itemIndex = getItemIndexOnSlot(playerSlotCol, playerSlotRow);
    if (itemIndex < gp.player.getInventory().size()) {
      x = (int) (gp.tileSize * 15.5);
      y = (int) (gp.tileSize * 5.5);
      width = (int) (gp.tileSize * 2.5);
      height = gp.tileSize;
      drawSubWindow(x, y, width, height);
      g2.drawImage(coin, x + 10, y + 8, 32, 32, null);

      int price = gp.player.getInventory().get(itemIndex).getPrice() / 2;
      String text = String.valueOf(price);
      x = getXforAlignToRightText(text, gp.tileSize * 18 - 20);
      g2.drawString(text, x, y + 34);

      // VENDER UM ITEM
      if (gp.keyH.enterPressed) {

        if (gp.player.getInventory().get(itemIndex) == gp.player.getCurrentWeapon()
            || gp.player.getInventory().get(itemIndex) == gp.player.getCurrentShield()) {

          commandNum = 0;
          subState = 0;
          npc.startDialogue(npc, 4);
        } else {
          if (gp.player.getInventory().get(itemIndex).getAmount() > 1) {
            gp.player.getInventory().get(itemIndex)
                .setAmount(gp.player.getInventory().get(itemIndex).getAmount() - 1);
          } else {
            gp.player.getInventory().remove(itemIndex);
          }
          gp.player.setCoin(gp.player.getCoin() + price);
        }
      }
    }
  }

  public void drawTransition() {
    counter++;
    g2.setColor(new Color(0, 0, 0, counter * 5));
    g2.fillRect(0, 0, gp.screenWidth, gp.screenHeight);

    if (counter == 50) {
      counter = 0;
      gp.gameState = gp.playState;
      gp.currentMap = gp.eHandler.tempMap;

      gp.player.setWorldX(gp.tileSize * gp.eHandler.tempCol);
      gp.player.setWorldY(gp.tileSize * gp.eHandler.tempRow);

      gp.eHandler.previousEventX = gp.player.getWorldX();
      gp.eHandler.previousEventY = gp.player.getWorldY();

      gp.changeArea();
    }
  }

  public void drawSleepScreen() {
    counter++;

    if (counter < 120) {
      float a = gp.eManager.lighting.getFilterAlpha() + 0.01f;
      gp.eManager.lighting.setFilterAlpha(a);
      if (gp.eManager.lighting.getFilterAlpha() > 1f) {
        gp.eManager.lighting.setFilterAlpha(1f);
      }
    }

    if (counter >= 120) {
      float a = gp.eManager.lighting.getFilterAlpha() - 0.01f;
      gp.eManager.lighting.setFilterAlpha(a);
      if (gp.eManager.lighting.getFilterAlpha() <= 0f) {
        gp.eManager.lighting.setFilterAlpha(0f);
        counter = 0;
        gp.eManager.lighting.setDayStateToDay();
        gp.eManager.lighting.setDayCounter(0);
        gp.gameState = gp.playState;
        gp.player.getImage(); // método void existente nas entidades
      }
    }
  }

  // --- Índice do slot ---
  public int getItemIndexOnSlot(int slotCol, int slotRow) {
    return slotCol + (slotRow * 5);
  }

  // --- Subjanela leve (sem gradiente) ---
  public void drawSubWindow(int x, int y, int width, int height) {
    Graphics2D g = (Graphics2D) g2.create(); // snapshot do estado

    g.setColor(COL_PANEL);
    g.fillRoundRect(x, y, width, height, 14, 14);

    g.setStroke(new BasicStroke(2f));
    g.setColor(COL_PANEL_BORDER);
    g.drawRoundRect(x, y, width, height, 14, 14);

    g.dispose(); // restaura o estado do g2 original
  }

  // Centralizada por tamanho
  private void drawSubWindowCenter(int w, int h) {
    int x = (gp.screenWidth - w) / 2;
    int y = (gp.screenHeight - h) / 2;
    drawSubWindow(x, y, w, h);
  }

  // --- Helpers visuais leves ---
  private void drawMenuItem(String text, int centerY, boolean selected) {
    int w = Math.min((int) (gp.screenWidth * 0.6), 800);
    int x = (gp.screenWidth - w) / 2;
    int h = (int) (gp.tileSize * 1.0);
    int yTop = centerY - (h * 3) / 4;

    if (selected) {
      g2.setColor(COL_HIGHLIGHT_BG);
      g2.fillRoundRect(x, yTop, w, h, 10, 10);
      g2.setColor(COL_PANEL_BORDER);
      g2.drawRoundRect(x, yTop, w, h, 10, 10);
    }

    int tx = getXforCenteredText(text);
    g2.setColor(selected ? COL_ACCENT : COL_TEXT);
    // sombra leve
    g2.setColor(Color.black);
    g2.drawString(text, tx + 2, centerY + 2);
    g2.setColor(selected ? COL_ACCENT : COL_TEXT);
    g2.drawString(text, tx, centerY);

    // indicador esquerdo
    if (selected) {
      int triX = x - (int) (gp.tileSize * 0.3);
      int triY = centerY - (int) (gp.tileSize * 0.60);
      int triH = (int) (gp.tileSize * 0.7);
      int[] xs = { triX, triX + (int) (gp.tileSize * 0.45), triX };
      int[] ys = { triY, triY + triH / 2, triY + triH };
      g2.setColor(COL_ACCENT);
      g2.fillPolygon(xs, ys, 3);
    }
  }

  // --- Helpers de render ---
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
    return gp.screenWidth / 2 - length / 2;
  }

  public void drawCharacterScreen() {
    final int frameX = gp.tileSize * 2;
    final int frameY = gp.tileSize;
    final int frameWidth = gp.tileSize * 5;
    final int frameHeight = gp.tileSize * 10;
    drawSubWindow(frameX, frameY, frameWidth, frameHeight);

    g2.setColor(COL_TEXT);
    g2.setFont(g2.getFont().deriveFont(32F));

    int textX = frameX + 20;
    int textY = frameY + gp.tileSize;
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
    textY += lineHeight;

    // Valores
    int tailX = (frameX + frameWidth) - 30;
    textY = frameY + gp.tileSize;

    String value;

    value = String.valueOf(gp.player.getLevel());
    g2.drawString(value, getXforAlignToRightText(value, tailX), textY);
    textY += lineHeight;

    value = gp.player.getLife() + "/" + gp.player.getMaxLife();
    g2.drawString(value, getXforAlignToRightText(value, tailX), textY);
    textY += lineHeight;

    value = gp.player.getMana() + "/" + gp.player.getMaxMana();
    g2.drawString(value, getXforAlignToRightText(value, tailX), textY);
    textY += lineHeight;

    value = String.valueOf(gp.player.getStrength());
    g2.drawString(value, getXforAlignToRightText(value, tailX), textY);
    textY += lineHeight;

    value = String.valueOf(gp.player.getDexterity());
    g2.drawString(value, getXforAlignToRightText(value, tailX), textY);
    textY += lineHeight;

    value = String.valueOf(gp.player.getAttack());
    g2.drawString(value, getXforAlignToRightText(value, tailX), textY);
    textY += lineHeight;

    value = String.valueOf(gp.player.getDefense());
    g2.drawString(value, getXforAlignToRightText(value, tailX), textY);
    textY += lineHeight;

    value = String.valueOf(gp.player.getExp());
    g2.drawString(value, getXforAlignToRightText(value, tailX), textY);
    textY += lineHeight;

    value = String.valueOf(gp.player.getNextLevelExp());
    g2.drawString(value, getXforAlignToRightText(value, tailX), textY);
    textY += lineHeight;

    value = String.valueOf(gp.player.getCoin());
    g2.drawString(value, getXforAlignToRightText(value, tailX), textY);
    textY += lineHeight;

    if (gp.player.getCurrentWeapon() != null) {
      g2.drawImage(gp.player.getCurrentWeapon().getDown1(), tailX - gp.tileSize, textY - 24, null);
    }
    textY += gp.tileSize;
    if (gp.player.getCurrentShield() != null) {
      g2.drawImage(gp.player.getCurrentShield().getDown1(), tailX - gp.tileSize, textY - 24, null);
    }
  }

  public int getXforAlignToRightText(String text, int tailX) {
    int length = (int) g2.getFontMetrics().getStringBounds(text, g2).getWidth();
    return tailX - length;
  }
}

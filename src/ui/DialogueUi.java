package ui;

import java.awt.Graphics2D;

import entity.Entity;
import main.GamePanel;

class DialogueUi {

  private static final int DIALOG_SFX_TYPING = 17;
  private static final int DIALOG_WINDOW_X_TILES = 2;
  private static final int DIALOG_WINDOW_Y_DIVISOR = 3;
  private static final int DIALOG_WINDOW_HORIZONTAL_MARGIN_TILES = 4;
  private static final int DIALOG_WINDOW_HEIGHT_TILES = 4;

  private final UI ui;

  DialogueUi(UI ui) {
    this.ui = ui;
  }

  void drawDialogueScreen() {
    Graphics2D g2 = ui.g2;
    Entity npc = ui.getNpc();
    if (npc == null) {
      return;
    }

    int x = GamePanel.TILE_SIZE * DIALOG_WINDOW_X_TILES;
    int y = GamePanel.TILE_SIZE / DIALOG_WINDOW_Y_DIVISOR;
    int width = GamePanel.SCREEN_WIDTH - (GamePanel.TILE_SIZE * DIALOG_WINDOW_HORIZONTAL_MARGIN_TILES);
    int height = GamePanel.TILE_SIZE * DIALOG_WINDOW_HEIGHT_TILES;
    ui.drawSubWindow(x, y, width, height);

    int panelPadding = ui.getPanelPadding();
    g2.setFont(ui.fonts.pixelPlain(28F));
    x += panelPadding + 8;
    y += panelPadding + 24;

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
    GamePanel gp = ui.getGamePanel();
    Entity npc = ui.getNpc();

    char[] characters = dialogueLine.toCharArray();

    if (ui.charIndex < characters.length) {
      gp.playSE(DIALOG_SFX_TYPING);
      ui.combinedText.append(characters[ui.charIndex]);
      ui.currentDialogue = ui.combinedText.toString();
      ui.charIndex++;
    }

    var keyHandler = ui.getKeyHandler();
    int state = gp.getGameState();

    if (keyHandler.isEnterPressed()) {
      ui.charIndex = 0;
      ui.combinedText.setLength(0);
      ui.currentDialogue = "";

      if (state == GamePanel.DIALOGUE_STATE || state == GamePanel.CUTSCENE_STATE) {
        npc.setDialogueIndex(idx + 1);
        keyHandler.setEnterPressed(false);
      }
    }
  }

  private void handleDialogueEnd() {
    Entity npc = ui.getNpc();
    if (npc == null) {
      return;
    }

    GamePanel gp = ui.getGamePanel();
    npc.setDialogueIndex(0);

    int state = gp.getGameState();
    if (state == GamePanel.DIALOGUE_STATE) {
      gp.setGameState(GamePanel.PLAY_STATE);
    } else if (state == GamePanel.CUTSCENE_STATE) {
      gp.getCsManager().nextPhase();
    }
  }

  private void drawCurrentDialogueLines(int x, int y) {
    Graphics2D g2 = ui.g2;
    String current = ui.currentDialogue;

    for (String line : current.split("\n")) {
      g2.drawString(line, x, y);
      y += ui.getDialogueLineHeight();
    }
  }
}

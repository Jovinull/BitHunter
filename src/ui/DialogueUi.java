package ui;

import java.awt.Graphics2D;

import entity.Entity;
import main.GamePanel;

class DialogueUi {

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

    int x = GamePanel.TILE_SIZE * 3;
    int y = GamePanel.TILE_SIZE / 2;
    int width = GamePanel.SCREEN_WIDTH - (GamePanel.TILE_SIZE * 6);
    int height = GamePanel.TILE_SIZE * 4;
    ui.drawSubWindow(x, y, width, height);

    g2.setFont(ui.fonts.pixelPlain(32F));
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
    GamePanel gp = ui.getGamePanel();
    Entity npc = ui.getNpc();

    char[] characters = dialogueLine.toCharArray();

    if (ui.charIndex < characters.length) {
      gp.playSE(17);
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
      y += 40;
    }
  }
}

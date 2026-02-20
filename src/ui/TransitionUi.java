package ui;

import java.awt.Color;
import java.awt.Graphics2D;

import main.GamePanel;

class TransitionUi {

  private final UI ui;

  TransitionUi(UI ui) {
    this.ui = ui;
  }

  void drawTransition() {
    Graphics2D g2 = ui.g2;
    GamePanel gp = ui.getGamePanel();

    ui.counter++;
    g2.setColor(new Color(0, 0, 0, ui.counter * 5));
    g2.fillRect(0, 0, GamePanel.SCREEN_WIDTH, GamePanel.SCREEN_HEIGHT);

    if (ui.counter == 50) {
      ui.counter = 0;
      gp.setGameState(GamePanel.PLAY_STATE);

      var eventHandler = gp.geteHandler();
      var player = gp.getPlayer();

      gp.setCurrentMap(eventHandler.getTempMap());

      player.setWorldX(GamePanel.TILE_SIZE * eventHandler.getTempCol());
      player.setWorldY(GamePanel.TILE_SIZE * eventHandler.getTempRow());
      player.snapPetToSelf();

      eventHandler.setPreviousEventX(player.getWorldX());
      eventHandler.setPreviousEventY(player.getWorldY());

      gp.changeArea();
    }
  }

  void drawSleepScreen() {
    GamePanel gp = ui.getGamePanel();

    ui.counter++;

    final var lighting = gp.getEnvironmentManager().getLighting();
    if (lighting == null) {
      ui.counter = 0;
      gp.setGameState(GamePanel.PLAY_STATE);
      return;
    }

    final int TRANSITION_TICKS = 120;
    final float STEP = 0.01f;

    if (ui.counter < TRANSITION_TICKS) {
      lighting.setFilterAlpha(lighting.getFilterAlpha() + STEP);
    } else {
      lighting.setFilterAlpha(lighting.getFilterAlpha() - STEP);
      if (lighting.getFilterAlpha() <= 0f) {
        ui.counter = 0;
        lighting.setDayStateToDay();
        lighting.setDayCounter(0);
        gp.setGameState(GamePanel.PLAY_STATE);
        gp.getPlayer().getImage();
      }
    }
  }
}

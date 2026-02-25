package ui;

import main.GamePanel;

/**
 * Orquestra o desenho das sub-telas de UI conforme o estado atual do jogo.
 */
final class UiScreenRouter {

  private final UI ui;

  UiScreenRouter(UI ui) {
    this.ui = ui;
  }

  void drawCurrentScreen() {
    var gp = ui.getGamePanel();
    int state = gp.getGameState();

    if (state == GamePanel.TITLE_STATE) {
      ui.getTitleUi().drawTitleScreen();
      return;
    }

    switch (state) {
      case GamePanel.PLAY_STATE -> drawPlayHud();
      case GamePanel.PAUSE_STATE -> ui.getPauseUi().drawPauseScreen();
      case GamePanel.DIALOGUE_STATE -> ui.getDialogueUi().drawDialogueScreen();
      case GamePanel.CHARACTER_STATE -> {
        ui.getCharacterUi().drawCharacterScreen();
        ui.getInventoryUi().drawInventory(gp.getPlayer(), true);
      }
      case GamePanel.OPTIONS_STATE -> ui.getOptionsUi().drawOptionsScreen();
      case GamePanel.GAME_OVER_STATE -> ui.getGameOverUi().drawGameOverScreen();
      case GamePanel.TRANSITION_STATE -> ui.getTransitionUi().drawTransition();
      case GamePanel.TRADE_STATE -> ui.getTradeUi().drawTradeScreen();
      case GamePanel.PET_YARD_STATE -> ui.getPetYardUi().drawPetYardScreen();
      case GamePanel.SLEEP_STATE -> ui.getTransitionUi().drawSleepScreen();
      case GamePanel.BESTIARY_STATE -> ui.getBestiaryUi().drawBestiaryScreen();
      default -> {
        // Estados sem camada extra de UI dedicada.
      }
    }
  }

  private void drawPlayHud() {
    ui.getHudUi().drawXpTopBar();
    ui.getHudUi().drawPlayerLife();
    ui.getHudUi().drawPetHud();
    ui.getHudUi().drawMonsterLife();
    ui.getHudUi().drawWeatherIndicator();
    ui.getHudUi().drawMessage();
    if (ui.isClockWindowOpen()) {
      ui.getHudUi().drawClockWindow();
    }
  }
}

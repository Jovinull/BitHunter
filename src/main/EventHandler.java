package main;

import data.Progress;
import entity.Entity;

public class EventHandler {

  GamePanel gp;
  EventRect[][][] eventRect;
  Entity eventMaster;

  int previousEventX;
  int previousEventY; // mantido em linha separada (S1659)
  boolean canTouchEvent = true;
  int tempMap;
  int tempCol; // mantido em linha separada (S1659)
  int tempRow;

  public EventHandler(GamePanel gp) {
    this.gp = gp;

    eventMaster = new Entity(gp);

    int maxMap = GamePanel.MAX_MAP;
    int maxCol = gp.getMaxWorldCol();
    int maxRow = gp.getMaxWorldRow();

    eventRect = new EventRect[maxMap][maxCol][maxRow];

    int map = 0;
    int col = 0;
    int row = 0;

    while (map < maxMap && col < maxCol && row < maxRow) {
      eventRect[map][col][row] = new EventRect();
      eventRect[map][col][row].x = 23;
      eventRect[map][col][row].y = 23;
      eventRect[map][col][row].width = 2;
      eventRect[map][col][row].height = 2;
      eventRect[map][col][row].eventRectDefaultX = eventRect[map][col][row].x;
      eventRect[map][col][row].eventRectDefaultY = eventRect[map][col][row].y;

      col++;
      if (col == maxCol) {
        col = 0;
        row++;
        if (row == maxRow) {
          row = 0;
          map++;
        }
      }
    }

    setDialogue();
  }

  public void setDialogue() {
    var lm = gp.getLanguageManager();
    String[][] d = eventMaster.getDialogues();

    // Buraco (dano)
    d[0][0] = lm.text("event.pit.dialog.0.line1");

    // Fonte de cura
    d[1][0] = lm.text("event.healingPool.dialog.1.line1");
    d[1][1] = lm.text("event.healingPool.dialog.1.line2");
  }

  public boolean hit(int map, int col, int row, String reqDirection) {
    boolean hit = false;

    int currentMap = gp.getCurrentMap();

    if (map == currentMap) {
      var player = gp.getPlayer();
      var playerArea = player.getSolidArea();
      int playerDefaultX = player.getSolidAreaDefaultX();
      int playerDefaultY = player.getSolidAreaDefaultY();

      playerArea.x = player.getWorldX() + playerArea.x;
      playerArea.y = player.getWorldY() + playerArea.y;

      EventRect rect = eventRect[map][col][row];
      rect.x = col * GamePanel.TILE_SIZE + rect.x;
      rect.y = row * GamePanel.TILE_SIZE + rect.y;

      String playerDir = player.getDirection();

      if (playerArea.intersects(rect)
          && !rect.eventDone
          && (playerDir.equals(reqDirection) || "any".equals(reqDirection))) {
        hit = true;
        previousEventX = player.getWorldX();
        previousEventY = player.getWorldY();
      }

      playerArea.x = playerDefaultX;
      playerArea.y = playerDefaultY;
      rect.x = rect.eventRectDefaultX;
      rect.y = rect.eventRectDefaultY;
    }

    return hit;
  }

  public void checkEvent() {
    var player = gp.getPlayer();

    int xDistance = Math.abs(player.getWorldX() - previousEventX);
    int yDistance = Math.abs(player.getWorldY() - previousEventY);
    int distance = Math.max(xDistance, yDistance);
    if (distance > GamePanel.TILE_SIZE) {
      canTouchEvent = true;
    }

    if (!canTouchEvent) {
      return;
    }

    // dano no buraco
    if (hit(0, 27, 16, Entity.DIR_RIGHT)) {
      damagePit(GamePanel.DIALOGUE_STATE);
      return;
    }
    // fonte de cura
    if (hit(0, 23, 12, Entity.DIR_UP)) {
      healingPool(GamePanel.DIALOGUE_STATE);
      return;
    }
    // casa
    if (hit(0, 10, 39, "any")) {
      teleport(1, 12, 13, GamePanel.INDOOR);
      return;
    }
    if (hit(1, 12, 13, "any")) {
      teleport(0, 10, 39, GamePanel.OUTSIDE);
      return;
    }
    // falar com NPC
    if (hit(1, 12, 9, Entity.DIR_UP)) {
      speak(gp.getNpc()[1][0]);
      return;
    }
    // dungeon 1
    if (hit(0, 12, 9, "any")) {
      teleport(2, 9, 41, GamePanel.DUNGEON);
      return;
    }
    if (hit(2, 9, 41, "any")) {
      teleport(0, 12, 9, GamePanel.OUTSIDE);
      return;
    }
    // dungeon 2 / boss room
    if (hit(2, 8, 7, "any")) {
      teleport(3, 26, 41, GamePanel.DUNGEON);
      return;
    }
    if (hit(3, 26, 41, "any")) {
      teleport(2, 8, 7, GamePanel.DUNGEON);
      return;
    }
    // trigger do Skeleton Lord
    if (hit(3, 25, 27, "any")) {
      skeletonLord();
    }
  }

  public void teleport(int map, int col, int row, int area) {
    gp.setGameState(GamePanel.TRANSITION_STATE);
    gp.setNextArea(area);
    tempMap = map;
    tempCol = col;
    tempRow = row;
    canTouchEvent = false;
    gp.playSE(13);
  }

  public void damagePit(int gameState) {
    gp.setGameState(gameState);
    gp.playSE(6);
    eventMaster.startDialogue(eventMaster, 0);

    var player = gp.getPlayer();
    player.setLife(player.getLife() - 1);

    canTouchEvent = false;
  }

  public void healingPool(int gameState) {
    var keyH = gp.getKeyH();
    var player = gp.getPlayer();

    if (keyH.isEnterPressed()) {
      keyH.setEnterPressed(false);

      gp.setGameState(gameState);
      player.setAttackCanceled(true);

      gp.playSE(2);

      eventMaster.startDialogue(eventMaster, 1);

      player.setLife(player.getMaxLife());
      player.setMana(player.getMaxMana());

      canTouchEvent = false;

      gp.getaSetter().setMonster();

      gp.saveLoad.save();
    }
  }

  public void speak(Entity entity) {
    var keyH = gp.getKeyH();
    var player = gp.getPlayer();

    if (keyH.isEnterPressed()) {
      gp.setGameState(GamePanel.DIALOGUE_STATE);
      player.setAttackCanceled(true);
      entity.speak();
    }
  }

  public void skeletonLord() {
    if (!gp.isBossBattleOn() && !Progress.isSkeletonLordDefeated()) {
      gp.setGameState(GamePanel.CUTSCENE_STATE);
      gp.getCsManager().startSkeletonLordScene();
    }
  }
}

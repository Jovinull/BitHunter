package main;

import data.Progress;
import entity.Entity;

public class EventHandler {

  private final GamePanel gp;
  private final EventRect[][][] eventRect;
  private final Entity eventMaster;

  private int previousEventX;
  private int previousEventY;

  private boolean canTouchEvent = true;

  private int tempMap;
  private int tempCol;
  private int tempRow;

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

  private void setDialogue() {
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

    // dano no buraco (leste do centro)
    if (hit(0, 55, 55, Entity.DIR_RIGHT)) {
      damagePit(GamePanel.DIALOGUE_STATE);
      return;
    }
    // fonte de cura (norte da vila)
    if (hit(0, 50, 42, Entity.DIR_UP)) {
      healingPool(GamePanel.DIALOGUE_STATE);
      return;
    }
    // casa - entrada (overworld → indoor)
    if (hit(0, 25, 70, "any")) {
      teleport(1, 12, 13, GamePanel.INDOOR);
      return;
    }
    // casa - saída (indoor → overworld)
    if (hit(1, 12, 13, "any")) {
      teleport(0, 25, 72, GamePanel.OUTSIDE);
      return;
    }
    // falar com NPC merchant
    if (hit(1, 12, 9, Entity.DIR_UP)) {
      speak(gp.getNpc()[1][0]);
      return;
    }
    // dungeon 1 - entrada (overworld → dungeon)
    if (hit(0, 80, 14, "any")) {
      teleport(2, 25, 90, GamePanel.DUNGEON);
      return;
    }
    // dungeon 1 - saída (dungeon → overworld)
    if (hit(2, 25, 91, "any")) {
      teleport(0, 75, 45, GamePanel.OUTSIDE);
      return;
    }
    // dungeon 2 / boss room - entrada
    if (hit(2, 25, 9, "any")) {
      teleport(3, 50, 90, GamePanel.DUNGEON);
      return;
    }
    // boss room - saída (volta para dungeon)
    if (hit(3, 50, 91, "any")) {
      teleport(2, 25, 10, GamePanel.DUNGEON);
      return;
    }
    // trigger do Skeleton Lord (arena)
    if (hit(3, 50, 55, "any")) {
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

  // ===== Getters/Setters usados pela UI/TransitionUi =====

  public int getPreviousEventX() {
    return previousEventX;
  }

  public void setPreviousEventX(int previousEventX) {
    this.previousEventX = previousEventX;
  }

  public int getPreviousEventY() {
    return previousEventY;
  }

  public void setPreviousEventY(int previousEventY) {
    this.previousEventY = previousEventY;
  }

  public boolean isCanTouchEvent() {
    return canTouchEvent;
  }

  public void setCanTouchEvent(boolean canTouchEvent) {
    this.canTouchEvent = canTouchEvent;
  }

  public int getTempMap() {
    return tempMap;
  }

  public void setTempMap(int tempMap) {
    this.tempMap = tempMap;
  }

  public int getTempCol() {
    return tempCol;
  }

  public void setTempCol(int tempCol) {
    this.tempCol = tempCol;
  }

  public int getTempRow() {
    return tempRow;
  }

  public void setTempRow(int tempRow) {
    this.tempRow = tempRow;
  }
}

package main;

import data.Progress;
import entity.Entity;

public class EventHandler {

  GamePanel gp;
  EventRect[][][] eventRect;
  Entity eventMaster;

  int previousEventX, previousEventY;
  boolean canTouchEvent = true;
  int tempMap, tempCol, tempRow;

  public EventHandler(GamePanel gp) {
    this.gp = gp;

    eventMaster = new Entity(gp);

    eventRect = new EventRect[gp.maxMap][gp.maxWorldCol][gp.maxWorldRow];

    int map = 0;
    int col = 0;
    int row = 0;
    while (map < gp.maxMap && col < gp.maxWorldCol && row < gp.maxWorldRow) {
      eventRect[map][col][row] = new EventRect();
      eventRect[map][col][row].x = 23;
      eventRect[map][col][row].y = 23;
      eventRect[map][col][row].width = 2;
      eventRect[map][col][row].height = 2;
      eventRect[map][col][row].eventRectDefaultX = eventRect[map][col][row].x;
      eventRect[map][col][row].eventRectDefaultY = eventRect[map][col][row].y;

      col++;
      if (col == gp.maxWorldCol) {
        col = 0;
        row++;
        if (row == gp.maxWorldRow) {
          row = 0;
          map++;
        }
      }
    }

    setDialogue();
  }

  public void setDialogue() {
    String[][] d = eventMaster.getDialogues();
    d[0][0] = "Você caiu em um buraco!!";
    d[1][0] = "Você bebe a água.\nSua vida e mana foi restaurada.\n(O progresso foi salvo)";
    d[1][1] = "Caramba, essa água é boa demais.";
  }

  public boolean hit(int map, int col, int row, String reqDirection) {
    boolean hit = false;

    if (map == gp.currentMap) {
      var playerArea = gp.player.getSolidArea();
      int playerDefaultX = gp.player.getSolidAreaDefaultX();
      int playerDefaultY = gp.player.getSolidAreaDefaultY();

      playerArea.x = gp.player.getWorldX() + playerArea.x;
      playerArea.y = gp.player.getWorldY() + playerArea.y;

      eventRect[map][col][row].x = col * gp.tileSize + eventRect[map][col][row].x;
      eventRect[map][col][row].y = row * gp.tileSize + eventRect[map][col][row].y;

      String playerDir = gp.player.getDirection();

      if (playerArea.intersects(eventRect[map][col][row]) && !eventRect[map][col][row].eventDone) {
        if (playerDir.equals(reqDirection) || "any".equals(reqDirection)) {
          hit = true;
          previousEventX = gp.player.getWorldX();
          previousEventY = gp.player.getWorldY();
        }
      }

      playerArea.x = playerDefaultX;
      playerArea.y = playerDefaultY;
      eventRect[map][col][row].x = eventRect[map][col][row].eventRectDefaultX;
      eventRect[map][col][row].y = eventRect[map][col][row].eventRectDefaultY;
    }

    return hit;
  }

  public void checkEvent() {
    int xDistance = Math.abs(gp.player.getWorldX() - previousEventX);
    int yDistance = Math.abs(gp.player.getWorldY() - previousEventY);
    int distance = Math.max(xDistance, yDistance);
    if (distance > gp.tileSize) {
      canTouchEvent = true;
    }

    if (canTouchEvent) {
      if (hit(0, 27, 16, Entity.DIR_RIGHT)) {
        damagePit(gp.dialogueState);
      } else if (hit(0, 23, 12, Entity.DIR_UP)) {
        healingPool(gp.dialogueState);
      } else if (hit(0, 10, 39, "any")) {
        teleport(1, 12, 13, gp.indoor);
      } else if (hit(1, 12, 13, "any")) {
        teleport(0, 10, 39, gp.outside);
      } else if (hit(1, 12, 9, Entity.DIR_UP)) {
        speak(gp.npc[1][0]);
      } else if (hit(0, 12, 9, "any")) {
        teleport(2, 9, 41, gp.dungeon);
      } else if (hit(2, 9, 41, "any")) {
        teleport(0, 12, 9, gp.outside);
      } else if (hit(2, 8, 7, "any")) {
        teleport(3, 26, 41, gp.dungeon);
      } else if (hit(3, 26, 41, "any")) {
        teleport(2, 8, 7, gp.dungeon);
      } else if (hit(3, 25, 27, "any")) {
        skeletonLord();
      }
    }
  }

  public void teleport(int map, int col, int row, int area) {
    gp.gameState = gp.transitionState;
    gp.nextArea = area;
    tempMap = map;
    tempCol = col;
    tempRow = row;
    canTouchEvent = false;
    gp.playSE(13);
  }

  public void damagePit(int gameState) {
    gp.gameState = gameState;
    gp.playSE(6);
    eventMaster.startDialogue(eventMaster, 0);
    gp.player.setLife(gp.player.getLife() - 1);
    canTouchEvent = false;
  }

  public void healingPool(int gameState) {
    if (gp.keyH.enterPressed) {
      gp.keyH.enterPressed = false;

      gp.gameState = gameState;
      gp.player.setAttackCanceled(true); // usar accessor

      gp.playSE(2);

      eventMaster.startDialogue(eventMaster, 1);

      gp.player.setLife(gp.player.getMaxLife());
      gp.player.setMana(gp.player.getMaxMana());

      canTouchEvent = false;

      gp.aSetter.setMonster();

      gp.saveLoad.save();
    }
  }

  public void speak(Entity entity) {
    if (gp.keyH.enterPressed) {
      gp.gameState = gp.dialogueState;
      gp.player.setAttackCanceled(true);
      entity.speak();
    }
  }

  public void skeletonLord() {
    if (!gp.bossBattleOn && !Progress.isSkeletonLordDefeated()) {
      gp.gameState = gp.cutsceneState;
      gp.csManager.startSkeletonLordScene();
    }
  }
}

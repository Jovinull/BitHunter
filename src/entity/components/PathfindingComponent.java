package entity.components;

import entity.Entity;
import main.GamePanel;

public class PathfindingComponent {

  private final Entity entity;

  public PathfindingComponent(Entity entity) {
    this.entity = entity;
  }

  public void searchPath(int goalCol, int goalRow) {
    // Stuck detection: cancela o path se o NPC estiver travado
    entity.getStuckDetector().tick();
    if (!entity.isOnPath()) {
      return;
    }

    int startCol = entity.getCol();
    int startRow = entity.getRow();

    var pathFinder = entity.getGp().getpFinder();
    pathFinder.setNodes(startCol, startRow, goalCol, goalRow);

    if (!pathFinder.search()) {
      entity.setOnPath(false);
      return;
    }

    var path = pathFinder.getPathList();
    if (path.isEmpty()) {
      entity.setOnPath(false);
      return;
    }

    int nextCol = path.get(0).getCol();
    int nextRow = path.get(0).getRow();

    int nextX = nextCol * GamePanel.TILE_SIZE;
    int nextY = nextRow * GamePanel.TILE_SIZE;

    int enLeftX = entity.getLeftX();
    int enRightX = entity.getRightX();
    int enTopY = entity.getTopY();
    int enBottomY = entity.getBottomY();

    boolean alignedVertical = isAlignedVertical(enLeftX, enRightX, nextX);
    boolean alignedHorizontal = isAlignedHorizontal(enTopY, enBottomY, nextY);

    if (alignedVertical && moveVerticallyToward(enTopY, nextY)) {
      checkGoalReached(goalCol, goalRow, nextCol, nextRow);
      return;
    }
    if (alignedHorizontal && moveHorizontallyToward(enLeftX, nextX)) {
      checkGoalReached(goalCol, goalRow, nextCol, nextRow);
      return;
    }

    moveDiagonallyTowards(nextX, nextY, enLeftX, enRightX, enTopY, enBottomY);

    checkGoalReached(goalCol, goalRow, nextCol, nextRow);
  }

  private boolean isAlignedVertical(int leftX, int rightX, int nextX) {
    return leftX >= nextX && rightX < nextX + GamePanel.TILE_SIZE;
  }

  private boolean isAlignedHorizontal(int topY, int bottomY, int nextY) {
    return topY >= nextY && bottomY < nextY + GamePanel.TILE_SIZE;
  }

  private boolean moveVerticallyToward(int enTopY, int nextY) {
    if (enTopY > nextY) {
      entity.setDirection(Entity.DIR_UP);
      return true;
    } else if (enTopY < nextY) {
      entity.setDirection(Entity.DIR_DOWN);
      return true;
    }
    return false;
  }

  private boolean moveHorizontallyToward(int enLeftX, int nextX) {
    if (enLeftX > nextX) {
      entity.setDirection(Entity.DIR_LEFT);
      return true;
    } else if (enLeftX < nextX) {
      entity.setDirection(Entity.DIR_RIGHT);
      return true;
    }
    return false;
  }

  private void moveDiagonallyTowards(int nextX, int nextY, int enLeftX, int enRightX, int enTopY, int enBottomY) {
    final int centerX = (enLeftX + enRightX) / 2;
    final int centerY = (enTopY + enBottomY) / 2;
    final int targetCenterX = nextX + GamePanel.TILE_SIZE / 2;
    final int targetCenterY = nextY + GamePanel.TILE_SIZE / 2;

    final int dx = centerX - targetCenterX;
    final int dy = centerY - targetCenterY;

    final boolean horizontalPrimary = Math.abs(dx) > Math.abs(dy);

    String primaryDir;
    String fallbackDir;

    if (horizontalPrimary) {
      primaryDir = horizontalDir(dx);
      fallbackDir = verticalDir(dy);
    } else {
      primaryDir = verticalDir(dy);
      fallbackDir = horizontalDir(dx);
    }

    setDirectionAndCheck(primaryDir);
    if (entity.isCollisionOn()) {
      entity.setDirection(fallbackDir);
    }
  }

  private String horizontalDir(int dx) {
    return (dx > 0) ? Entity.DIR_LEFT : Entity.DIR_RIGHT;
  }

  private String verticalDir(int dy) {
    return (dy > 0) ? Entity.DIR_UP : Entity.DIR_DOWN;
  }

  private void setDirectionAndCheck(String dir) {
    entity.setDirection(dir);
    entity.checkCollision();
  }

  private void checkGoalReached(int goalCol, int goalRow, int nextCol, int nextRow) {
    if (nextCol == goalCol && nextRow == goalRow) {
      entity.setOnPath(false);
    }
  }
}

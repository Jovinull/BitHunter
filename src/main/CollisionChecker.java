package main;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.List;

import entity.Entity;

// Classe responsável por verificar colisões entre entidades e tiles
public class CollisionChecker {

  private static final int NO_COLLISION_INDEX = -1;
  private static final int SPATIAL_CELL_SIZE = GamePanel.TILE_SIZE * 2;
  private static final int QUERY_PADDING_PIXELS = GamePanel.TILE_SIZE * 2;

  private final GamePanel gp; // Referência ao painel principal do jogo (acessa tiles e configurações)
  private final Rectangle tempRect1 = new Rectangle();
  private final Rectangle tempRect2 = new Rectangle();
  private final IdentityHashMap<Entity[][], SpatialIndex> spatialIndexes = new IdentityHashMap<>();

  // Construtor: recebe o GamePanel para acessar o mundo e as tiles
  public CollisionChecker(GamePanel gp) {
    this.gp = gp;
  }

  // Verifica se a entidade colidirá com algum tile sólido ao se mover
  public void checkTile(Entity entity) {

    // Coordenadas absolutas (no mundo) da área sólida da entidade
    Rectangle sa = entity.getSolidArea();
    int entityLeftWorldX = entity.getWorldX() + sa.x;
    int entityRightWorldX = entity.getWorldX() + sa.x + sa.width;
    int entityTopWorldY = entity.getWorldY() + sa.y;
    int entityBottomWorldY = entity.getWorldY() + sa.y + sa.height;

    int tileSize = GamePanel.TILE_SIZE;

    // Conversão dessas coordenadas para colunas e linhas da matriz de tiles
    int entityLeftCol = entityLeftWorldX / tileSize;
    int entityRightCol = entityRightWorldX / tileSize;
    int entityTopRow = entityTopWorldY / tileSize;
    int entityBottomRow = entityBottomWorldY / tileSize;

    int tileNum1;
    int tileNum2;

    // Direção temporal quando em knockback
    String direction = entity.isKnockBack() ? entity.getKnockBackDirection() : entity.getDirection();
    int speed = entity.getSpeed();

    // Atalhos para o TileManager
    var tileManager = gp.getTileM();
    int[][][] map = tileManager.getMapTileNum();
    var tiles = tileManager.getTiles();
    int currentMapIndex = gp.getCurrentMap();

    // Verifica colisão com base na direção do movimento
    switch (direction) {
      case Entity.DIR_UP:
        entityTopRow = (entityTopWorldY - speed) / tileSize;
        tileNum1 = map[currentMapIndex][entityLeftCol][entityTopRow];
        tileNum2 = map[currentMapIndex][entityRightCol][entityTopRow];
        if (tiles[tileNum1].isCollision() || tiles[tileNum2].isCollision()) {
          entity.setCollisionOn(true);
        }
        break;

      case Entity.DIR_DOWN:
        entityBottomRow = (entityBottomWorldY + speed) / tileSize;
        tileNum1 = map[currentMapIndex][entityLeftCol][entityBottomRow];
        tileNum2 = map[currentMapIndex][entityRightCol][entityBottomRow];
        if (tiles[tileNum1].isCollision() || tiles[tileNum2].isCollision()) {
          entity.setCollisionOn(true);
        }
        break;

      case Entity.DIR_LEFT:
        entityLeftCol = (entityLeftWorldX - speed) / tileSize;
        tileNum1 = map[currentMapIndex][entityLeftCol][entityTopRow];
        tileNum2 = map[currentMapIndex][entityLeftCol][entityBottomRow];
        if (tiles[tileNum1].isCollision() || tiles[tileNum2].isCollision()) {
          entity.setCollisionOn(true);
        }
        break;

      case Entity.DIR_RIGHT:
        entityRightCol = (entityRightWorldX + speed) / tileSize;
        tileNum1 = map[currentMapIndex][entityRightCol][entityTopRow];
        tileNum2 = map[currentMapIndex][entityRightCol][entityBottomRow];
        if (tiles[tileNum1].isCollision() || tiles[tileNum2].isCollision()) {
          entity.setCollisionOn(true);
        }
        break;

      default:
        // sem direção definida -> não processa colisão de tile
        return;
    }
  }

  public int checkObject(Entity entity) {
    String direction = entity.isKnockBack() ? entity.getKnockBackDirection() : entity.getDirection();
    int speed = entity.getSpeed();

    var objects = gp.getObj();
    int currentMapIndex = gp.getCurrentMap();
    Entity[] currentObjects = objects[currentMapIndex];

    Rectangle eRect = tempRect1;
    setWorldRect(entity, eRect);
    applyMovement(direction, speed, eRect);

    SpatialIndex index = getOrRebuildSpatialIndex(objects, currentMapIndex);
    return checkObjectAgainstSpatialCandidates(entity, eRect, currentObjects, index);
  }

  // Verifica colisão entre uma entidade e um array de entidades-alvo
  // Retorna o índice da entidade-alvo com a qual houve colisão (ou -1 se
  // nenhuma)
  public int checkEntity(Entity entity, Entity[][] target) {

    String direction = entity.isKnockBack() ? entity.getKnockBackDirection() : entity.getDirection();
    int speed = entity.getSpeed();
    int currentMapIndex = gp.getCurrentMap();
    Entity[] currentTargets = target[currentMapIndex];

    Rectangle eRect = tempRect1;
    setWorldRect(entity, eRect);
    applyMovement(direction, speed, eRect);

    SpatialIndex index = getOrRebuildSpatialIndex(target, currentMapIndex);
    return checkEntityAgainstSpatialCandidates(entity, eRect, currentTargets, index);
  }

  // Verifica se a entidade colidiu com o jogador
  public boolean checkPlayer(Entity entity) {

    boolean contactPlayer = false;

    // Monstro morto/morrendo não bate mais no player
    if (entity.getType() == Entity.TYPE_MONSTER
        && (!entity.isAlive() || entity.isDying() || entity.getLife() <= 0)) {
      return false;
    }

    var player = gp.getPlayer();

    String direction = entity.isKnockBack() ? entity.getKnockBackDirection() : entity.getDirection();
    int speed = entity.getSpeed();

    Rectangle eRect = tempRect1;
    Rectangle pRect = tempRect2;
    setWorldRect(entity, eRect);
    setWorldRect(player, pRect);
    applyMovement(direction, speed, eRect);

    if (eRect.intersects(pRect)) {
      entity.setCollisionOn(true);
      contactPlayer = true;
    }

    return contactPlayer;
  }

  // ----- Helpers -----

  private static void setWorldRect(Entity e, Rectangle out) {
    Rectangle sa = e.getSolidArea();
    out.setBounds(e.getWorldX() + sa.x, e.getWorldY() + sa.y, sa.width, sa.height);
  }

  private static void applyMovement(String direction, int speed, Rectangle rect) {
    switch (direction) {
      case Entity.DIR_UP:
        rect.y -= speed;
        break;
      case Entity.DIR_DOWN:
        rect.y += speed;
        break;
      case Entity.DIR_LEFT:
        rect.x -= speed;
        break;
      case Entity.DIR_RIGHT:
        rect.x += speed;
        break;
      default:
        break;
    }
  }

  private int checkObjectAgainstSpatialCandidates(
      Entity entity, Rectangle eRect, Entity[] objects, SpatialIndex spatialIndex) {
    int index = NO_COLLISION_INDEX;
    Rectangle oRect = tempRect2;

    spatialIndex.beginQuery();
    QueryWindow queryWindow = buildQueryWindow(eRect, spatialIndex);

    for (int row = queryWindow.minRow; row <= queryWindow.maxRow; row++) {
      for (int col = queryWindow.minCol; col <= queryWindow.maxCol; col++) {
        List<Integer> bucket = spatialIndex.getBucket(col, row);
        if (bucket != null) {
          index = processObjectBucket(entity, eRect, objects, spatialIndex, oRect, bucket, index);
        }
      }
    }

    return index;
  }

  private int checkEntityAgainstSpatialCandidates(
      Entity entity, Rectangle eRect, Entity[] targets, SpatialIndex spatialIndex) {
    int index = NO_COLLISION_INDEX;
    Rectangle oRect = tempRect2;

    spatialIndex.beginQuery();
    QueryWindow queryWindow = buildQueryWindow(eRect, spatialIndex);

    for (int row = queryWindow.minRow; row <= queryWindow.maxRow; row++) {
      for (int col = queryWindow.minCol; col <= queryWindow.maxCol; col++) {
        List<Integer> bucket = spatialIndex.getBucket(col, row);
        if (bucket != null) {
          index = processEntityBucket(entity, eRect, targets, spatialIndex, oRect, bucket, index);
        }
      }
    }

    return index;
  }

  private SpatialIndex getOrRebuildSpatialIndex(Entity[][] target, int currentMapIndex) {
    SpatialIndex index = spatialIndexes.get(target);
    long frameCounter = gp.getCollisionFrameCounter();
    Entity[] currentLayer = target[currentMapIndex];

    if (index == null || index.requiresResize(currentLayer.length)) {
      index = createSpatialIndex(currentLayer.length);
      spatialIndexes.put(target, index);
    }

    if (index.shouldRebuild(currentMapIndex, frameCounter)) {
      rebuildSpatialIndex(index, currentLayer, currentMapIndex, frameCounter);
    }
    return index;
  }

  private SpatialIndex createSpatialIndex(int entityCount) {
    int worldPixelWidth = gp.getMaxWorldCol() * GamePanel.TILE_SIZE;
    int worldPixelHeight = gp.getMaxWorldRow() * GamePanel.TILE_SIZE;
    int gridCols = Math.max(1, (worldPixelWidth + SPATIAL_CELL_SIZE - 1) / SPATIAL_CELL_SIZE);
    int gridRows = Math.max(1, (worldPixelHeight + SPATIAL_CELL_SIZE - 1) / SPATIAL_CELL_SIZE);
    return new SpatialIndex(gridCols, gridRows, entityCount);
  }

  private void rebuildSpatialIndex(
      SpatialIndex index, Entity[] currentLayer, int mapIndex, long frameCounter) {
    index.clearBuckets();
    index.setMapAndFrame(mapIndex, frameCounter);

    for (int i = 0; i < currentLayer.length; i++) {
      addEntityToSpatialIndex(index, currentLayer[i], i);
    }
  }

  private static QueryWindow buildQueryWindow(Rectangle eRect, SpatialIndex spatialIndex) {
    int minCol = getMinCell(eRect.x, spatialIndex.getGridCols());
    int maxCol = getMaxCell(eRect.x + eRect.width - 1, spatialIndex.getGridCols());
    int minRow = getMinCell(eRect.y, spatialIndex.getGridRows());
    int maxRow = getMaxCell(eRect.y + eRect.height - 1, spatialIndex.getGridRows());
    return new QueryWindow(minCol, maxCol, minRow, maxRow);
  }

  private int processObjectBucket(
      Entity entity,
      Rectangle eRect,
      Entity[] objects,
      SpatialIndex spatialIndex,
      Rectangle oRect,
      List<Integer> bucket,
      int currentIndex) {
    int index = currentIndex;
    for (int candidateIndex : bucket) {
      if (spatialIndex.markIfUnseen(candidateIndex)) {
        Entity obj = objects[candidateIndex];
        if (obj != null) {
          // Rects temporários em coords do mundo (não muta as solidArea reais)
          setWorldRect(obj, oRect);
          if (eRect.intersects(oRect)) {
            if (obj.isCollision()) {
              entity.setCollisionOn(true);
            }
            index = candidateIndex;
          }
        }
      }
    }
    return index;
  }

  private int processEntityBucket(
      Entity entity,
      Rectangle eRect,
      Entity[] targets,
      SpatialIndex spatialIndex,
      Rectangle oRect,
      List<Integer> bucket,
      int currentIndex) {
    int index = currentIndex;
    for (int candidateIndex : bucket) {
      if (spatialIndex.markIfUnseen(candidateIndex)) {
        Entity other = targets[candidateIndex];
        if (isValidCollisionTarget(other)) {
          setWorldRect(other, oRect);
          if (eRect.intersects(oRect) && other != entity) {
            entity.setCollisionOn(true);
            index = candidateIndex;
          }
        }
      }
    }
    return index;
  }

  private static boolean isValidCollisionTarget(Entity other) {
    return other != null
        && other.isAlive()
        && !other.isDying()
        && (other.getType() != Entity.TYPE_MONSTER || other.getLife() > 0);
  }

  private static void addEntityToSpatialIndex(SpatialIndex index, Entity candidate, int candidateIndex) {
    if (candidate == null) {
      return;
    }

    Rectangle sa = candidate.getSolidArea();
    if (sa == null || sa.width <= 0 || sa.height <= 0) {
      return;
    }

    int left = candidate.getWorldX() + sa.x;
    int right = left + sa.width - 1;
    int top = candidate.getWorldY() + sa.y;
    int bottom = top + sa.height - 1;

    int minCol = clampCell(left / SPATIAL_CELL_SIZE, index.getGridCols());
    int maxCol = clampCell(right / SPATIAL_CELL_SIZE, index.getGridCols());
    int minRow = clampCell(top / SPATIAL_CELL_SIZE, index.getGridRows());
    int maxRow = clampCell(bottom / SPATIAL_CELL_SIZE, index.getGridRows());

    for (int row = minRow; row <= maxRow; row++) {
      for (int col = minCol; col <= maxCol; col++) {
        index.addToBucket(col, row, candidateIndex);
      }
    }
  }

  private static int getMinCell(int worldValue, int gridLimit) {
    int padded = worldValue - QUERY_PADDING_PIXELS;
    int cell = Math.floorDiv(padded, SPATIAL_CELL_SIZE);
    return clampCell(cell, gridLimit);
  }

  private static int getMaxCell(int worldValue, int gridLimit) {
    int padded = worldValue + QUERY_PADDING_PIXELS;
    int cell = Math.floorDiv(padded, SPATIAL_CELL_SIZE);
    return clampCell(cell, gridLimit);
  }

  private static int clampCell(int cell, int gridLimit) {
    if (cell < 0) {
      return 0;
    }
    int max = gridLimit - 1;
    if (cell > max) {
      return max;
    }
    return cell;
  }

  private static final class SpatialIndex {
    private final int gridCols;
    private final int gridRows;
    private final List<List<Integer>> buckets;
    private final int[] seenMarks;
    private int queryMark = 1;
    private int mapIndex = -1;
    private long frameCounter = -1L;

    private SpatialIndex(int gridCols, int gridRows, int entityCount) {
      this.gridCols = gridCols;
      this.gridRows = gridRows;
      this.buckets = new ArrayList<>(gridCols * gridRows);
      for (int i = 0; i < gridCols * gridRows; i++) {
        buckets.add(null);
      }
      this.seenMarks = new int[entityCount];
    }

    private boolean requiresResize(int newEntityCount) {
      return seenMarks.length != newEntityCount;
    }

    private boolean shouldRebuild(int mapIndex, long frameCounter) {
      return this.mapIndex != mapIndex || this.frameCounter != frameCounter;
    }

    private void setMapAndFrame(int mapIndex, long frameCounter) {
      this.mapIndex = mapIndex;
      this.frameCounter = frameCounter;
    }

    private void clearBuckets() {
      for (int i = 0; i < buckets.size(); i++) {
        buckets.set(i, null);
      }
    }

    private void addToBucket(int col, int row, int entityIndex) {
      int idx = row * gridCols + col;
      List<Integer> bucket = buckets.get(idx);
      if (bucket == null) {
        bucket = new ArrayList<>(4);
        buckets.set(idx, bucket);
      }
      bucket.add(entityIndex);
    }

    private List<Integer> getBucket(int col, int row) {
      return buckets.get(row * gridCols + col);
    }

    private void beginQuery() {
      queryMark++;
      if (queryMark == Integer.MAX_VALUE) {
        Arrays.fill(seenMarks, 0);
        queryMark = 1;
      }
    }

    private boolean markIfUnseen(int entityIndex) {
      if (seenMarks[entityIndex] == queryMark) {
        return false;
      }
      seenMarks[entityIndex] = queryMark;
      return true;
    }

    private int getGridCols() {
      return gridCols;
    }

    private int getGridRows() {
      return gridRows;
    }
  }

  private static final class QueryWindow {
    private final int minCol;
    private final int maxCol;
    private final int minRow;
    private final int maxRow;

    private QueryWindow(int minCol, int maxCol, int minRow, int maxRow) {
      this.minCol = minCol;
      this.maxCol = maxCol;
      this.minRow = minRow;
      this.maxRow = maxRow;
    }
  }
}

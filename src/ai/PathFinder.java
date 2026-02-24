package ai;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

import main.GamePanel;
import tile.Tile;
import tile_interactive.InteractiveTile;

public class PathFinder {

  private static final int MAX_STEPS = 500;

  private final GamePanel gp;
  private Node[][] node;

  private final PriorityQueue<Node> openList = new PriorityQueue<>(
      Comparator.comparingInt(Node::getFCost).thenComparingInt(Node::getGCost));
  private final List<Node> pathList = new ArrayList<>();

  private Node startNode;
  private Node goalNode;
  private Node currentNode;

  private boolean goalReached = false;
  private int step = 0;

  public PathFinder(GamePanel gp) {
    this.gp = gp;
    instantiateNodes();
  }

  public void instantiateNodes() {
    int maxCol = gp.getMaxWorldCol();
    int maxRow = gp.getMaxWorldRow();

    node = new Node[maxCol][maxRow];

    for (int col = 0; col < maxCol; col++) {
      for (int row = 0; row < maxRow; row++) {
        node[col][row] = new Node(col, row);
      }
    }
  }

  public void resetNodes() {
    int maxCol = gp.getMaxWorldCol();
    int maxRow = gp.getMaxWorldRow();

    for (int col = 0; col < maxCol; col++) {
      for (int row = 0; row < maxRow; row++) {
        Node current = node[col][row];
        current.setOpen(false);
        current.setChecked(false);
        current.setSolid(false);
      }
    }

    openList.clear();
    pathList.clear();
    goalReached = false;
    step = 0;
  }

  public void setNodes(int startCol, int startRow, int goalCol, int goalRow) {
    resetNodes();

    startNode = node[startCol][startRow];
    currentNode = startNode;
    goalNode = node[goalCol][goalRow];
    openList.add(currentNode);

    final int[][][] map = gp.getTileM().getMapTileNum();
    final Tile[] tiles = gp.getTileM().getTiles();

    // Pré-computa as posições sólidas vindas de iTiles (reduz loops e complexidade)
    final boolean[][] iTileMask = buildInteractiveSolidMask();

    int maxCol = gp.getMaxWorldCol();
    int maxRow = gp.getMaxWorldRow();

    for (int col = 0; col < maxCol; col++) {
      for (int row = 0; row < maxRow; row++) {

        if (isSolidCell(col, row, map, tiles, iTileMask)) {
          node[col][row].setSolid(true);
        }

        setCosts(node[col][row]);
      }
    }
  }

  private boolean[][] buildInteractiveSolidMask() {
    int maxCol = gp.getMaxWorldCol();
    int maxRow = gp.getMaxWorldRow();

    boolean[][] mask = new boolean[maxCol][maxRow];

    InteractiveTile[][] interactiveTiles = gp.getiTile();
    int currentMapIndex = gp.getCurrentMap();

    InteractiveTile[] layer = (interactiveTiles != null && interactiveTiles[currentMapIndex] != null)
        ? interactiveTiles[currentMapIndex]
        : null;

    if (layer == null) {
      return mask;
    }

    for (InteractiveTile it : layer) {
      if (it == null || !it.isDestructible()) {
        continue;
      }
      int itCol = it.getWorldX() / GamePanel.TILE_SIZE;
      int itRow = it.getWorldY() / GamePanel.TILE_SIZE;
      if (itCol >= 0 && itRow >= 0 && itCol < maxCol && itRow < maxRow) {
        mask[itCol][itRow] = true;
      }
    }

    return mask;
  }

  private boolean isSolidCell(int col, int row, int[][][] map, Tile[] tiles, boolean[][] iTileMask) {
    int currentMapIndex = gp.getCurrentMap();
    int id = map[currentMapIndex][col][row];
    boolean solidFromTile = id >= 0 && id < tiles.length && tiles[id] != null && tiles[id].isCollision();
    boolean solidFromInteractive = iTileMask[col][row];
    return solidFromTile || solidFromInteractive;
  }

  private void setCosts(Node n) {
    int xDistance = Math.abs(n.getCol() - startNode.getCol());
    int yDistance = Math.abs(n.getRow() - startNode.getRow());
    n.setGCost(xDistance + yDistance);

    xDistance = Math.abs(n.getCol() - goalNode.getCol());
    yDistance = Math.abs(n.getRow() - goalNode.getRow());
    n.setHCost(xDistance + yDistance);
  }

  public boolean search() {
    while (!goalReached && step < MAX_STEPS && !openList.isEmpty()) {
      currentNode = selectBestOpenNode();
      currentNode.setOpen(false);
      currentNode.setChecked(true);

      if (currentNode == goalNode) {
        goalReached = true;
        trackThePath();
      } else {
        expandNeighbors(currentNode.getCol(), currentNode.getRow());
        step++;
      }
    }
    return goalReached;
  }

  // ---- helpers ----

  private void expandNeighbors(int col, int row) {
    int maxRow = gp.getMaxWorldRow();
    int maxCol = gp.getMaxWorldCol();

    if (row > 0) {
      openNode(node[col][row - 1]); // up
    }
    if (col > 0) {
      openNode(node[col - 1][row]); // left
    }
    if (row + 1 < maxRow) {
      openNode(node[col][row + 1]); // down
    }
    if (col + 1 < maxCol) {
      openNode(node[col + 1][row]); // right
    }
  }

  private Node selectBestOpenNode() {
    return openList.poll();
  }

  private void openNode(Node n) {
    if (!n.isOpen() && !n.isChecked() && !n.isSolid()) {
      n.setOpen(true);
      n.setParent(currentNode);
      openList.add(n);
    }
  }

  private void trackThePath() {
    Node cur = goalNode;
    while (cur != startNode) {
      pathList.add(0, cur);
      cur = cur.getParent();
    }
  }

  public List<Node> getPathList() {
    return pathList;
  }
}

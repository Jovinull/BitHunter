package ai;

import java.util.ArrayList;
import java.util.List;

import main.GamePanel;
import tile.Tile;
import tile_interactive.InteractiveTile;

public class PathFinder {

  private static final int MAX_STEPS = 500;

  private final GamePanel gp;
  private Node[][] node;

  private final List<Node> openList = new ArrayList<>();
  public final List<Node> pathList = new ArrayList<>();

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
    node = new Node[gp.maxWorldCol][gp.maxWorldRow];

    for (int col = 0; col < gp.maxWorldCol; col++) {
      for (int row = 0; row < gp.maxWorldRow; row++) {
        node[col][row] = new Node(col, row);
      }
    }
  }

  public void resetNodes() {
    for (int col = 0; col < gp.maxWorldCol; col++) {
      for (int row = 0; row < gp.maxWorldRow; row++) {
        node[col][row].setOpen(false);
        node[col][row].setChecked(false);
        node[col][row].setSolid(false);
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

    final int[][][] map = gp.tileM.getMapTileNum();
    final Tile[] tiles = gp.tileM.getTiles();

    // Pré-computa as posições sólidas vindas de iTiles (reduz loops e complexidade)
    final boolean[][] iTileMask = buildInteractiveSolidMask();

    for (int col = 0; col < gp.maxWorldCol; col++) {
      for (int row = 0; row < gp.maxWorldRow; row++) {

        if (isSolidCell(col, row, map, tiles, iTileMask)) {
          node[col][row].setSolid(true);
        }

        setCosts(node[col][row]);
      }
    }
  }

  private boolean[][] buildInteractiveSolidMask() {
    boolean[][] mask = new boolean[gp.maxWorldCol][gp.maxWorldRow];

    InteractiveTile[] layer = (gp.iTile != null && gp.iTile[gp.currentMap] != null)
        ? gp.iTile[gp.currentMap]
        : null;

    if (layer == null) {
      return mask;
    }

    for (InteractiveTile it : layer) {
      if (it == null || !it.isDestructible()) {
        continue;
      }
      int itCol = it.getWorldX() / gp.tileSize;
      int itRow = it.getWorldY() / gp.tileSize;
      if (itCol >= 0 && itRow >= 0 && itCol < gp.maxWorldCol && itRow < gp.maxWorldRow) {
        mask[itCol][itRow] = true;
      }
    }

    return mask;
  }

  private boolean isSolidCell(int col, int row, int[][][] map, Tile[] tiles, boolean[][] iTileMask) {
    int id = map[gp.currentMap][col][row];
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
    while (!goalReached && step < MAX_STEPS) {

      currentNode.setChecked(true);
      openList.remove(currentNode);

      expandNeighbors(currentNode.getCol(), currentNode.getRow());

      if (openList.isEmpty()) {
        break;
      }

      currentNode = selectBestOpenNode();

      if (currentNode == goalNode) {
        goalReached = true;
        trackThePath();
      }

      step++;
    }
    return goalReached;
  }

  // ---- helpers ----

  private void expandNeighbors(int col, int row) {
    if (row > 0) {
      openNode(node[col][row - 1]); // up
    }
    if (col > 0) {
      openNode(node[col - 1][row]); // left
    }
    if (row + 1 < gp.maxWorldRow) {
      openNode(node[col][row + 1]); // down
    }
    if (col + 1 < gp.maxWorldCol) {
      openNode(node[col + 1][row]); // right
    }
  }

  private Node selectBestOpenNode() {
    int bestIdx = 0;
    int bestF = Integer.MAX_VALUE;

    for (int i = 0; i < openList.size(); i++) {
      Node n = openList.get(i);
      int f = n.getFCost();
      int g = n.getGCost();
      int bestG = openList.get(bestIdx).getGCost();

      if (f < bestF || (f == bestF && g < bestG)) {
        bestIdx = i;
        bestF = f;
      }
    }
    return openList.get(bestIdx);
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
}

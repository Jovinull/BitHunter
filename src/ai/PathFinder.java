package ai;

import java.util.ArrayList;
import java.util.List;

import main.GamePanel;

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

    int col = 0;
    int row = 0;

    while (col < gp.maxWorldCol && row < gp.maxWorldRow) {
      node[col][row] = new Node(col, row);

      col++;
      if (col == gp.maxWorldCol) {
        col = 0;
        row++;
      }
    }
  }

  public void resetNodes() {
    int col = 0;
    int row = 0;

    while (col < gp.maxWorldCol && row < gp.maxWorldRow) {
      node[col][row].setOpen(false);
      node[col][row].setChecked(false);
      node[col][row].setSolid(false);

      col++;
      if (col == gp.maxWorldCol) {
        col = 0;
        row++;
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

    int col = 0;
    int row = 0;

    while (col < gp.maxWorldCol && row < gp.maxWorldRow) {

      // tiles sólidos
      int tileNum = gp.tileM.mapTileNum[gp.currentMap][col][row];
      if (gp.tileM.tile[tileNum].collision) {
        node[col][row].setSolid(true);
      }

      // iTiles sólidos
      for (int i = 0; i < gp.iTile[gp.currentMap].length; i++) {
        if (gp.iTile[gp.currentMap][i] != null && gp.iTile[gp.currentMap][i].destructible) {
          int itCol = gp.iTile[gp.currentMap][i].worldX / gp.tileSize;
          int itRow = gp.iTile[gp.currentMap][i].worldY / gp.tileSize;
          node[itCol][itRow].setSolid(true);
        }
      }

      // custos
      setCosts(node[col][row]);

      col++;
      if (col == gp.maxWorldCol) {
        col = 0;
        row++;
      }
    }
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

      if (openList.isEmpty())
        break;

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
    if (row > 0)
      openNode(node[col][row - 1]); // up
    if (col > 0)
      openNode(node[col - 1][row]); // left
    if (row + 1 < gp.maxWorldRow)
      openNode(node[col][row + 1]); // down
    if (col + 1 < gp.maxWorldCol)
      openNode(node[col + 1][row]); // right
  }

  private Node selectBestOpenNode() {
    int bestIdx = 0;
    int bestF = Integer.MAX_VALUE;

    for (int i = 0; i < openList.size(); i++) {
      Node n = openList.get(i);
      int f = n.getFCost();
      int g = n.getGCost();
      int bestG = openList.get(bestIdx).getGCost();

      // if aninhado fundido
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
    Node current = goalNode;
    while (current != startNode) {
      pathList.add(0, current);
      current = current.getParent();
    }
  }
}

package ai;

public class Node {
  private Node parent;

  private final int col;
  private final int row;

  private int gCost;
  private int hCost;
  private boolean solid;
  private boolean open;
  private boolean checked;

  public Node(int col, int row) {
    this.col = col;
    this.row = row;
  }

  public int getCol() {
    return col;
  }

  public int getRow() {
    return row;
  }

  public Node getParent() {
    return parent;
  }

  public void setParent(Node parent) {
    this.parent = parent;
  }

  public int getGCost() {
    return gCost;
  }

  public void setGCost(int gCost) {
    this.gCost = gCost;
  }

  public int getHCost() {
    return hCost;
  }

  public void setHCost(int hCost) {
    this.hCost = hCost;
  }

  public int getFCost() {
    return gCost + hCost;
  }

  public boolean isSolid() {
    return solid;
  }

  public void setSolid(boolean solid) {
    this.solid = solid;
  }

  public boolean isOpen() {
    return open;
  }

  public void setOpen(boolean open) {
    this.open = open;
  }

  public boolean isChecked() {
    return checked;
  }

  public void setChecked(boolean checked) {
    this.checked = checked;
  }
}

package tile_interactive;

import java.awt.Rectangle;

import main.GamePanel;

public class ItTrunk extends InteractiveTile {

  public ItTrunk(GamePanel gp, int col, int row) {
    super(gp, col, row);

    // Posição via setters
    setWorldX(gp.tileSize * col);
    setWorldY(gp.tileSize * row);

    // Sprite
    down1 = setup("/tiles_interactive/trunk", gp.tileSize, gp.tileSize);

    Rectangle sa = getSolidArea();
    sa.x = 0;
    sa.y = 0;
    sa.width = 0;
    sa.height = 0;

    // Defaults da área sólida
    setSolidAreaDefaultX(sa.x);
    setSolidAreaDefaultY(sa.y);
  }

  @Override
  public void playSE() {
    gp.playSE(11);
  }

  @Override
  public boolean isCorrectItem(entity.Entity entity) {
    // Tronco (forma destruída) normalmente não requer item
    return false;
  }
}

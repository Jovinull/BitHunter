package tile_interactive;

import java.awt.Rectangle;

import main.GamePanel;

public class ItTrunk extends InteractiveTile {

  public static final String TILE_ID = "tile.trunk";

  public ItTrunk(GamePanel gp, int col, int row) {
    super(gp, col, row);

    setInternalId(TILE_ID);
    setTranslationKey(TILE_ID);

    down1 = setup("/tiles_interactive/trunk", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);

    Rectangle sa = getSolidArea();
    sa.x = 0;
    sa.y = 0;
    sa.width = 0;
    sa.height = 0;

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

package tile_interactive;

import java.awt.Rectangle;

import main.GamePanel;

public final class ItMetalPlate extends InteractiveTile {

  public static final String TILE_ID = "tile.metalPlate";

  public ItMetalPlate(GamePanel gp, int col, int row) {
    super(gp, col, row);

    setInternalId(TILE_ID);
    setTranslationKey(TILE_ID);

    down1 = setup("/tiles_interactive/metalplate", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);

    Rectangle sa = getSolidArea();
    sa.x = 0;
    sa.y = 0;
    sa.width = 0;
    sa.height = 0;

    setSolidAreaDefaultX(sa.x);
    setSolidAreaDefaultY(sa.y);
  }

  @Override
  public boolean isCorrectItem(entity.Entity entity) {
    // Por padrão, metal plate não requer item (pode ajustar conforme regra)
    return false;
  }
}

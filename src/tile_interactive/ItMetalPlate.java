package tile_interactive;

import java.awt.Rectangle;

import main.GamePanel;

public class ItMetalPlate extends InteractiveTile {

  public static final String IT_NAME = "Placa de Metal";

  public ItMetalPlate(GamePanel gp, int col, int row) {
    super(gp, col, row);

    // Posição via setters
    setWorldX(gp.tileSize * col);
    setWorldY(gp.tileSize * row);

    // Nome via setter
    setName(IT_NAME);

    // Sprite
    down1 = setup("/tiles_interactive/metalplate", gp.tileSize, gp.tileSize);

    // Área sólida zerada usando getters/setters
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
  public boolean isCorrectItem(entity.Entity entity) {
    // Por padrão, metal plate não requer item (pode ajustar conforme regra)
    return false;
  }
}

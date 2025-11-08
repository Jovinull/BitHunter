package tile_interactive;

import java.awt.Rectangle;

import main.GamePanel;

public class IT_MetalPlate extends InteractiveTile {

  public static final String itName = "Placa de Metal";

  public IT_MetalPlate(GamePanel gp, int col, int row) {
    super(gp, col, row);

    // Posição via setters
    setWorldX(gp.tileSize * col);
    setWorldY(gp.tileSize * row);

    // Nome via setter
    setName(itName);

    // Sprite
    down1 = setup("/tiles_interactive/metalplate", gp.tileSize, gp.tileSize);

    // Área sólida zerada usando getters/setters (sem acessar campos privados)
    Rectangle sa = getSolidArea();
    sa.x = 0;
    sa.y = 0;
    sa.width = 0;
    sa.height = 0;

    // Defaults da área sólida
    setSolidAreaDefaultX(sa.x);
    setSolidAreaDefaultY(sa.y);
  }
}

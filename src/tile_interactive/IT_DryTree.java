package tile_interactive;

import java.awt.Color;

import entity.Entity;
import main.GamePanel;

public class IT_DryTree extends InteractiveTile {

  public IT_DryTree(GamePanel gp, int col, int row) {
    super(gp, col, row);

    // Posiciona via setters (sem acessar worldX/worldY diretamente)
    setWorldX(gp.tileSize * col);
    setWorldY(gp.tileSize * row);

    // Sprite
    down1 = setup("/tiles_interactive/drytree", gp.tileSize, gp.tileSize);

    // Comportamento
    destructible = true;
    setMaxLife(3);
    setLife(3);
  }

  @Override
  public boolean isCorrectItem(Entity entity) {
    // Precisa de machado equipado
    return entity.getCurrentWeapon() != null
        && entity.getCurrentWeapon().getType() == TYPE_AXE;
  }

  @Override
  public void playSE() {
    gp.playSE(11);
  }

  @Override
  public InteractiveTile getDestroyedForm() {
    int col = getWorldX() / gp.tileSize;
    int row = getWorldY() / gp.tileSize;
    return new IT_Trunk(gp, col, row);
  }

  // Aparência das partículas ao ser atingido
  @Override
  public Color getParticleColor() {
    return new Color(65, 50, 30);
  }

  @Override
  public int getParticleSize() {
    return 6;
  }

  @Override
  public int getParticleSpeed() {
    return 1;
  }

  @Override
  public int getParticleMaxLife() {
    return 20;
  }
}

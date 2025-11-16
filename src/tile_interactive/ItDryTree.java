package tile_interactive;

import java.awt.Color;

import entity.Entity;
import main.GamePanel;

public class ItDryTree extends InteractiveTile {

  public static final String TILE_ID = "tile.dryTree";

  public ItDryTree(GamePanel gp, int col, int row) {
    super(gp, col, row);

    setInternalId(TILE_ID);
    setTranslationKey(TILE_ID);

    down1 = setup("/tiles_interactive/drytree", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);

    setDestructible(true);
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
    int col = getWorldX() / GamePanel.TILE_SIZE;
    int row = getWorldY() / GamePanel.TILE_SIZE;
    return new ItTrunk(gp, col, row);
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

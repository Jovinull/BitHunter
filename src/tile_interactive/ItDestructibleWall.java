package tile_interactive;

import java.awt.Color;
import java.util.Random;

import entity.Entity;
import main.GamePanel;
import object.ObjCoinBronze;

public class ItDestructibleWall extends InteractiveTile {

  private static final Random RNG = new Random();

  public ItDestructibleWall(GamePanel gp, int col, int row) {
    super(gp, col, row);

    // Sprite do bloco
    down1 = setup("/tiles_interactive/destructiblewall", gp.tileSize, gp.tileSize);

    // Comportamento de "quebrável" + vida
    setDestructible(true);
    setMaxLife(3);
    setLife(3);
  }

  @Override
  public boolean isCorrectItem(Entity entity) {
    // Requer picareta equipada
    return entity.getCurrentWeapon() != null
        && entity.getCurrentWeapon().getType() == TYPE_PICKAXE;
  }

  @Override
  public void playSE() {
    // usa gp herdado de Entity
    gp.playSE(20);
  }

  @Override
  public InteractiveTile getDestroyedForm() {
    // Sem forma destruída por padrão; retorne outra InteractiveTile se quiser
    return null;
  }

  // Partículas (aparência ao ser atingido)
  @Override
  public Color getParticleColor() {
    return new Color(65, 65, 65);
  }

  @Override
  public int getParticleSize() {
    return 6; // pixels
  }

  @Override
  public int getParticleSpeed() {
    return 1;
  }

  @Override
  public int getParticleMaxLife() {
    return 20;
  }

  @Override
  public void checkDrop() {
    int i = RNG.nextInt(100) + 1;
    if (i < 20) {
      dropItem(new ObjCoinBronze(gp));
    }
  }
}

package tile_interactive;

import java.awt.Graphics2D;

import entity.Entity;
import main.GamePanel;

// this-escape: chama setWorldX/setWorldY de Entity no construtor.
// Seguro pois nenhuma subclasse sobrescreve esses setters.
@SuppressWarnings("this-escape")
public abstract class InteractiveTile extends Entity {

  private boolean destructible = false;

  protected InteractiveTile(GamePanel gp, int col, int row) {
    super(gp);
    // posiciona o tile no mundo com base no grid
    int tileSize = GamePanel.TILE_SIZE;
    setWorldX(col * tileSize);
    setWorldY(row * tileSize);
  }

  public abstract boolean isCorrectItem(Entity entity);

  public void playSE() {
    // hook: som ao interagir/quebrar; sobrescreva nas subclasses
  }

  public InteractiveTile getDestroyedForm() {
    // hook: retorna a forma destruída; por padrão não há
    return null;
  }

  public boolean isDestructible() {
    return destructible;
  }

  protected void setDestructible(boolean value) {
    this.destructible = value;
  }

  @Override
  public void update() {
    if (isInvincible()) {
      setInvincibleCounter(getInvincibleCounter() + 1);
      if (getInvincibleCounter() > 20) {
        setInvincible(false);
        setInvincibleCounter(0);
      }
    }
  }

  @Override
  public void draw(Graphics2D g2) {
    if (!inCamera()) {
      return;
    }

    int screenX = getScreenX();
    int screenY = getScreenY();
    g2.drawImage(getDown1(), screenX, screenY, null);
  }
}

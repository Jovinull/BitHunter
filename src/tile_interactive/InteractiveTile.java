package tile_interactive;

import java.awt.Graphics2D;

import entity.Entity;
import main.GamePanel;

public class InteractiveTile extends Entity {

  public boolean destructible = false;

  public InteractiveTile(GamePanel gp, int col, int row) {
    super(gp);
    // posiciona o tile no mundo com base no grid
    setWorldX(col * gp.tileSize);
    setWorldY(row * gp.tileSize);
  }

  public boolean isCorrectItem(Entity entity) {
    // Por padrão, nenhum item é o correto; subclasses podem sobrescrever
    return false;
  }

  public void playSE() {
    // hook: som ao interagir/quebrar; sobrescreva nas subclasses
  }

  public InteractiveTile getDestroyedForm() {
    // hook: retorna a forma destruída; por padrão não há
    return null;
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
    // Desenha apenas se dentro da câmera (usa cálculo do Entity)
    if (!inCamera()) {
      return;
    }
    int screenX = getScreenX();
    int screenY = getScreenY();

    // Desenha a sprite base do tile interativo
    g2.drawImage(getDown1(), screenX, screenY, null);
  }
}

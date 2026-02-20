package environment;

import java.awt.Graphics2D;

/**
 * Efeito de céu limpo — nenhum overlay visual.
 * Serve como implementação neutra para {@link WeatherType#CLEAR}.
 */
public class ClearEffect implements WeatherEffect {

  @Override
  public void update() {
    // Sem partículas ou estado para atualizar
  }

  @Override
  public void render(Graphics2D g2, int screenWidth, int screenHeight, float intensity) {
    // Sem efeito visual
  }
}

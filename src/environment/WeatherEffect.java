package environment;

import java.awt.Graphics2D;

/**
 * Contrato para efeitos visuais de clima.
 * Cada {@link WeatherType} possui uma implementação que
 * renderiza partículas e overlays via {@link Graphics2D}.
 */
public interface WeatherEffect {

  /** Atualiza o estado interno (partículas, timers) — chamado a cada frame. */
  void update();

  /**
   * Renderiza o efeito na tela.
   *
   * @param g2           contexto gráfico
   * @param screenWidth  largura da tela em pixels
   * @param screenHeight altura da tela em pixels
   * @param intensity    multiplicador de intensidade (0.0–1.0), usado em transições
   */
  void render(Graphics2D g2, int screenWidth, int screenHeight, float intensity);
}

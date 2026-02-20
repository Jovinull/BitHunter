package main;

import java.awt.event.KeyEvent;
import java.util.List;

/**
 * Catálogo de todas as ações do jogo, independente da tecla física.
 * <p>
 * O {@link KeyHandler} traduz keyCodes em ações via {@link InputBindings},
 * permitindo remapeamento por configuração ({@code controls.properties}).
 * </p>
 *
 * <p>Para adicionar uma nova ação:
 * <ol>
 *   <li>Declare a constante aqui com a tecla-padrão e a categoria.</li>
 *   <li>Adicione o tratamento correspondente no {@link KeyHandler}.</li>
 *   <li>O arquivo {@code controls.properties} já aceita a nova chave
 *       automaticamente (fallback para o default definido aqui).</li>
 * </ol>
 * </p>
 */
public enum GameAction {

  // ── Movimento ──
  MOVE_UP(KeyEvent.VK_W, Category.MOVEMENT),
  MOVE_DOWN(KeyEvent.VK_S, Category.MOVEMENT),
  MOVE_LEFT(KeyEvent.VK_A, Category.MOVEMENT),
  MOVE_RIGHT(KeyEvent.VK_D, Category.MOVEMENT),

  // ── Combate ──
  CONFIRM(KeyEvent.VK_ENTER, Category.COMBAT),
  SHOOT(KeyEvent.VK_F, Category.COMBAT),
  GUARD(KeyEvent.VK_SPACE, Category.COMBAT),

  // ── Interface ──
  PAUSE(KeyEvent.VK_P, Category.INTERFACE),
  CHARACTER(KeyEvent.VK_C, Category.INTERFACE),
  OPTIONS(KeyEvent.VK_ESCAPE, Category.INTERFACE),
  MAP(KeyEvent.VK_M, Category.INTERFACE),
  MINIMAP(KeyEvent.VK_X, Category.INTERFACE),
  BESTIARY(KeyEvent.VK_B, Category.INTERFACE),

  // ── Debug ──
  DEBUG_TOGGLE(KeyEvent.VK_T, Category.DEBUG),
  DEBUG_RELOAD_MAP(KeyEvent.VK_R, Category.DEBUG),
  DEBUG_WEATHER_CYCLE(KeyEvent.VK_F6, Category.DEBUG),

  // ── Camadas do mapa (tela de mapa) ──
  MAP_LAYER_NPC(KeyEvent.VK_1, Category.MAP_LAYERS),
  MAP_LAYER_MONSTER(KeyEvent.VK_2, Category.MAP_LAYERS),
  MAP_LAYER_INTERACTIVE(KeyEvent.VK_3, Category.MAP_LAYERS),
  MAP_LAYER_DROP(KeyEvent.VK_4, Category.MAP_LAYERS);

  private final int defaultKey;
  private final Category category;

  GameAction(int defaultKey, Category category) {
    this.defaultKey = defaultKey;
    this.category = category;
  }

  /** Tecla-padrão ({@code KeyEvent.VK_*}) quando nenhum override é configurado. */
  public int getDefaultKey() {
    return defaultKey;
  }

  /** Categoria de agrupamento para a UI de controles. */
  public Category getCategory() {
    return category;
  }

  /**
   * Categorias para organizar as ações na tela de controles
   * e permitir ativar/desativar grupos (ex.: debug).
   */
  public enum Category {
    MOVEMENT,
    COMBAT,
    INTERFACE,
    DEBUG,
    MAP_LAYERS
  }

  // ── Ações remapeáveis (exibidas na UI de controles) ──

  private static final List<GameAction> REMAPPABLE_LIST = List.of(
      MOVE_UP, MOVE_DOWN, MOVE_LEFT, MOVE_RIGHT,
      CONFIRM, SHOOT, GUARD,
      PAUSE, CHARACTER, OPTIONS, MINIMAP, MAP, BESTIARY
  );

  /**
   * Ações que podem ser remapeadas na tela de controles do jogo.
   * Retorna lista imutável; debug e camadas do mapa ficam de fora.
   */
  public static List<GameAction> getRemappable() {
    return REMAPPABLE_LIST;
  }
}

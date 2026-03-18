package data;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Estatísticas de jogo rastreadas por slot de save.
 * <p>Usa um {@link Map} interno para flexibilidade: novas métricas podem
 * ser adicionadas sem alterar {@code serialVersionUID} ou quebrar saves antigos.</p>
 */
public class GameStatistics implements Serializable {
  @Serial
  private static final long serialVersionUID = 1L;

  // ===== Chaves de estatísticas =====

  /** Total de monstros eliminados. */
  public static final String KILLS = "kills";
  /** Total de mortes do jogador. */
  public static final String DEATHS = "deaths";
  /** Total de dano causado a monstros. */
  public static final String DAMAGE_DEALT = "damageDealt";
  /** Total de dano recebido pelo jogador. */
  public static final String DAMAGE_RECEIVED = "damageReceived";
  /** Total de passos dados (tiles percorridos). */
  public static final String STEPS = "steps";
  /** Total de moedas coletadas (acumulado, não saldo atual). */
  public static final String COINS_COLLECTED = "coinsCollected";
  /** Total de level-ups alcançados. */
  public static final String LEVEL_UPS = "levelUps";
  /** Total de itens coletados (pickups). */
  public static final String ITEMS_COLLECTED = "itemsCollected";
  /** Total de vezes que o jogo foi salvo. */
  public static final String SAVES_COUNT = "savesCount";

  /**
   * Chaves exibidas na UI de estatísticas, na ordem desejada.
   * O tempo de jogo é exibido separadamente (vem do PlayTimeTracker).
   */
  public static final String[] DISPLAY_ORDER = {
      KILLS, DEATHS, DAMAGE_DEALT, DAMAGE_RECEIVED,
      STEPS, COINS_COLLECTED, ITEMS_COLLECTED, LEVEL_UPS
  };

  private final HashMap<String, Long> stats;

  public GameStatistics() {
    stats = new HashMap<>();
  }

  /** Incrementa a estatística em 1. */
  public void increment(String key) {
    stats.merge(key, 1L, Long::sum);
  }

  /** Incrementa a estatística pelo valor informado. */
  public void add(String key, long value) {
    if (value != 0) {
      stats.merge(key, value, Long::sum);
    }
  }

  /** Retorna o valor atual da estatística (0 se nunca registrada). */
  public long get(String key) {
    return stats.getOrDefault(key, 0L);
  }

  /** Retorna o mapa interno (para serialização). */
  public Map<String, Long> getAll() {
    return stats;
  }

  /** Restaura estatísticas a partir de um mapa salvo (null-safe). */
  public void restoreFrom(Map<String, Long> saved) {
    stats.clear();
    if (saved != null) {
      stats.putAll(saved);
    }
  }

  /** Reseta todas as estatísticas para zero. */
  public void reset() {
    stats.clear();
  }
}

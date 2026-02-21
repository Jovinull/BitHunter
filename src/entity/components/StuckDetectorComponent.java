package entity.components;

import entity.Entity;

/**
 * Detecta quando um NPC fica "travado" (andando parado) contra obstáculos
 * dinâmicos
 * e cancela o pathfinding após um período configurável, evitando que o NPC
 * fique empurrando um obstáculo que não pode ultrapassar.
 *
 * <p>
 * Funcionamento baseado em contagem de frames (sem timers reais), aproveitando
 * o game loop fixo de 60 FPS.
 * </p>
 *
 * <h3>Fluxo</h3>
 * <ol>
 * <li>A cada {@code CHECK_INTERVAL} frames, compara a posição atual com a
 * última posição amostrada.</li>
 * <li>Se a distância percorrida for menor que {@code MIN_DISTANCE_SQ},
 * incrementa
 * o contador {@code stuckChecks}.</li>
 * <li>Se {@code stuckChecks} atingir {@code STUCK_THRESHOLD}, cancela o
 * pathfinding ({@code entity.setOnPath(false)}) e entra em idle por
 * {@code IDLE_FRAMES} frames, dando um aspecto natural de "dúvida".</li>
 * </ol>
 */
public class StuckDetectorComponent {

  // ── Configuração (em frames @60 FPS) ──────────────────────────────────

  /** Frames entre cada amostragem de posição (0.5 s). */
  private static final int CHECK_INTERVAL = 30;

  /** Distância mínima² (em pixels²) para considerar que o NPC se moveu. */
  private static final int MIN_DISTANCE_SQ = 4;

  /** Nº de checks consecutivos parado antes de cancelar o path (~2 s). */
  private static final int STUCK_THRESHOLD = 4;

  /** Frames de "pensar" (idle) depois de detectar stuck (0.5 s). */
  private static final int IDLE_FRAMES = 30;

  // ── Estado interno ────────────────────────────────────────────────────

  private final Entity entity;

  private int frameCounter;
  private int stuckChecks;
  private int idleFramesLeft;
  private int lastX;
  private int lastY;

  public StuckDetectorComponent(Entity entity) {
    this.entity = entity;
    reset();
  }

  /**
   * Deve ser chamado uma vez por frame de update enquanto o NPC estiver
   * em pathfinding. Se o NPC não estiver no path, o detector se auto-reseta.
   */
  public void tick() {
    if (!entity.isOnPath()) {
      reset();
      return;
    }

    // Cooldown pós-stuck: aguarda idle acabar antes de voltar a verificar
    if (idleFramesLeft > 0) {
      idleFramesLeft--;
      return;
    }

    frameCounter++;
    if (frameCounter < CHECK_INTERVAL) {
      return;
    }
    frameCounter = 0;

    int dx = entity.getWorldX() - lastX;
    int dy = entity.getWorldY() - lastY;
    int distSq = dx * dx + dy * dy;

    if (distSq < MIN_DISTANCE_SQ) {
      stuckChecks++;
    } else {
      stuckChecks = 0;
    }

    lastX = entity.getWorldX();
    lastY = entity.getWorldY();

    if (stuckChecks >= STUCK_THRESHOLD) {
      entity.setOnPath(false);
      idleFramesLeft = IDLE_FRAMES;
      stuckChecks = 0;
    }
  }

  /** Reinicia todos o estado do detector. */
  public void reset() {
    frameCounter = 0;
    stuckChecks = 0;
    idleFramesLeft = 0;
    lastX = entity.getWorldX();
    lastY = entity.getWorldY();
  }

  /** @return {@code true} se o detector está no período de idle pós-stuck. */
  public boolean isInIdleCooldown() {
    return idleFramesLeft > 0;
  }
}

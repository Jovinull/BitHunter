package data;

/**
 * Rastreia o tempo de jogo acumulado por sessão para o slot ativo.
 * <p>Usa {@code System.nanoTime()} para medir intervalos reais,
 * independente de variações de FPS ou lag.</p>
 */
public class PlayTimeTracker {

  private long accumulatedSeconds;
  private long sessionStartNanos = -1;
  private boolean running;

  /**
   * Inicia (ou retoma) a contagem de tempo com o total previamente acumulado.
   *
   * @param previousTotalSeconds total de segundos já salvos no slot
   */
  public void start(long previousTotalSeconds) {
    this.accumulatedSeconds = previousTotalSeconds;
    this.sessionStartNanos = System.nanoTime();
    this.running = true;
  }

  /** Para a contagem e consolida o tempo da sessão atual. */
  public void stop() {
    if (running) {
      accumulatedSeconds += elapsedSessionSeconds();
      sessionStartNanos = -1;
      running = false;
    }
  }

  /** Pausa temporariamente (ex.: pause menu). */
  public void pause() {
    if (running) {
      accumulatedSeconds += elapsedSessionSeconds();
      sessionStartNanos = -1;
      running = false;
    }
  }

  /** Retoma após pausa. */
  public void resume() {
    if (!running) {
      sessionStartNanos = System.nanoTime();
      running = true;
    }
  }

  /**
   * Retorna o total de segundos jogados (acumulado + sessão atual).
   */
  public long getTotalSeconds() {
    if (running) {
      return accumulatedSeconds + elapsedSessionSeconds();
    }
    return accumulatedSeconds;
  }

  public boolean isRunning() {
    return running;
  }

  /** Reseta para zero (usado ao sobrescrever um slot). */
  public void reset() {
    accumulatedSeconds = 0;
    sessionStartNanos = -1;
    running = false;
  }

  private long elapsedSessionSeconds() {
    if (sessionStartNanos < 0) {
      return 0;
    }
    return (System.nanoTime() - sessionStartNanos) / 1_000_000_000L;
  }

  /**
   * Formata segundos totais em "HH:MM:SS".
   */
  public static String formatPlayTime(long totalSeconds) {
    if (totalSeconds < 0) {
      return "--:--:--";
    }
    long hours = totalSeconds / 3600;
    long minutes = (totalSeconds % 3600) / 60;
    long seconds = totalSeconds % 60;
    return String.format("%02d:%02d:%02d", hours, minutes, seconds);
  }
}

package environment;

import java.awt.Graphics2D;
import java.util.Random;
import java.util.logging.Logger;

import main.GamePanel;

/**
 * Gerenciador central do sistema de clima dinâmico.
 *
 * <p>Responsabilidades:</p>
 * <ul>
 *   <li>Manter o clima atual e decidir quando trocar</li>
 *   <li>Aplicar transições suaves (fade in/out) entre climas</li>
 *   <li>Delegar update/render para o {@link WeatherEffect} ativo</li>
 *   <li>Fornecer informações para HUD e debug</li>
 * </ul>
 *
 * <p>A seleção de clima respeita pesos/probabilidades configuráveis
 * em {@code weather.properties}, com bônus contextuais baseados
 * na fase do dia ({@link Lighting}).</p>
 */
public class WeatherManager {

  private static final Logger LOGGER = Logger.getLogger(WeatherManager.class.getName());

  private final GamePanel gp;
  private final Lighting lighting;
  private final WeatherConfig config;
  private final Random random;

  private WeatherType currentType = WeatherType.CLEAR;
  private WeatherEffect currentEffect;

  // Máquina de estados para transições suaves
  private enum TransitionState {
    ACTIVE,
    FADING_OUT,
    FADING_IN
  }

  private TransitionState transitionState = TransitionState.ACTIVE;
  private float transitionAlpha = 1.0f;
  private WeatherType pendingType;

  // Controle de duração (em frames)
  private int durationRemaining;

  public WeatherManager(GamePanel gp, Lighting lighting) {
    this.gp = gp;
    this.lighting = lighting;
    this.config = new WeatherConfig();
    this.random = new Random();
    this.currentEffect = createEffect(WeatherType.CLEAR);
    this.durationRemaining = rollDuration();
  }

  // ── Update ──

  /**
   * Atualiza timers, transições e efeito ativo.
   * Chamado a cada frame pelo {@link EnvironmentManager}.
   */
  public void update() {
    if (!config.isEnabled()) return;

    if (currentEffect != null) {
      currentEffect.update();
    }

    switch (transitionState) {
      case ACTIVE -> {
        durationRemaining--;
        if (durationRemaining <= 0) {
          pendingType = pickNextWeather();
          transitionState = TransitionState.FADING_OUT;
          LOGGER.fine(() -> "Clima expirando: " + currentType + " -> fade out para " + pendingType);
        }
      }
      case FADING_OUT -> {
        float fadeStep = 1.0f / Math.max(1, config.getTransitionFadeFrames());
        transitionAlpha -= fadeStep;
        if (transitionAlpha <= 0) {
          transitionAlpha = 0;
          switchToWeather(pendingType);
          transitionState = TransitionState.FADING_IN;
          LOGGER.fine(() -> "Clima trocado para: " + currentType);
        }
      }
      case FADING_IN -> {
        float fadeStep = 1.0f / Math.max(1, config.getTransitionFadeFrames());
        transitionAlpha += fadeStep;
        if (transitionAlpha >= 1.0f) {
          transitionAlpha = 1.0f;
          durationRemaining = rollDuration();
          transitionState = TransitionState.ACTIVE;
        }
      }
    }
  }

  // ── Render ──

  /**
   * Renderiza o efeito de clima ativo.
   * Só desenha em áreas externas ({@link GamePanel#OUTSIDE}).
   */
  public void draw(Graphics2D g2) {
    if (!config.isEnabled()) return;
    if (gp.getCurrentArea() != GamePanel.OUTSIDE) return;
    if (currentEffect == null || currentType == WeatherType.CLEAR) return;

    currentEffect.render(g2, GamePanel.SCREEN_WIDTH, GamePanel.SCREEN_HEIGHT, transitionAlpha);
  }

  // ── Seleção de clima ──

  /**
   * Seleciona o próximo clima por sorteio ponderado.
   * Aplica bônus contextuais por fase do dia e penaliza
   * o clima atual para evitar repetição imediata.
   */
  private WeatherType pickNextWeather() {
    String dayPhase = getDayPhase();
    WeatherType[] types = WeatherType.values();
    int[] adjusted = new int[types.length];
    int total = 0;

    for (WeatherType type : types) {
      int weight = config.getWeight(type);

      // Bônus: neblina mais provável na madrugada/manhã
      if (type == WeatherType.FOG
          && ("DAWN".equals(dayPhase) || "DAY".equals(dayPhase))) {
        weight += config.getFogDawnBonus();
      }

      // Bônus: trovoada mais provável à noite/entardecer
      if (type == WeatherType.STORM
          && ("NIGHT".equals(dayPhase) || "DUSK".equals(dayPhase))) {
        weight += config.getStormNightBonus();
      }

      // Penaliza repetição do clima atual
      if (type == currentType) {
        weight = Math.max(1, weight / 4);
      }

      adjusted[type.ordinal()] = weight;
      total += weight;
    }

    int roll = random.nextInt(Math.max(1, total));
    int cumulative = 0;
    for (WeatherType type : types) {
      cumulative += adjusted[type.ordinal()];
      if (roll < cumulative) {
        return type;
      }
    }

    return WeatherType.CLEAR;
  }

  private String getDayPhase() {
    if (lighting == null) return "DAY";
    return lighting.getDayPhaseName();
  }

  // ── Fábricas e helpers ──

  private WeatherEffect createEffect(WeatherType type) {
    int sw = GamePanel.SCREEN_WIDTH;
    int sh = GamePanel.SCREEN_HEIGHT;

    return switch (type) {
      case CLEAR -> new ClearEffect();
      case OVERCAST -> new OvercastEffect(config.getOvercastDarkness());
      case DRIZZLE -> new RainEffect(
          config.getDrizzleParticleCount(), config.getDrizzleDarkness(),
          3f, 5f, 6, 0.5f, false, sw, sh);
      case RAIN -> new RainEffect(
          config.getRainParticleCount(), config.getRainDarkness(),
          6f, 10f, 10, 1.0f, true, sw, sh);
      case FOG -> new FogEffect(config.getFogMaxAlpha(), sw, sh);
      case STORM -> {
        Runnable thunderCb = null;
        int sfxIdx = config.getThunderSfxIndex();
        if (sfxIdx >= 0) {
          thunderCb = () -> gp.playSE(sfxIdx);
        }
        yield new StormEffect(
            config.getStormParticleCount(), config.getStormDarkness(),
            thunderCb, sw, sh);
      }
    };
  }

  private void switchToWeather(WeatherType type) {
    currentType = type;
    currentEffect = createEffect(type);
  }

  private int rollDuration() {
    int min = config.getMinDurationFrames();
    int max = config.getMaxDurationFrames();
    if (max <= min) return min;
    return min + random.nextInt(max - min + 1);
  }

  // ── API pública ──

  /** Força um tipo de clima com transição suave (uso em debug/cheat). */
  public void forceWeather(WeatherType type) {
    pendingType = type;
    transitionState = TransitionState.FADING_OUT;
  }

  /** Alterna para o próximo tipo de clima na sequência do enum (debug). */
  public void cycleWeather() {
    WeatherType[] types = WeatherType.values();
    int next = (currentType.ordinal() + 1) % types.length;
    forceWeather(types[next]);
  }

  /** Reseta o clima para CLEAR com duração nova (usado em restart). */
  public void reset() {
    switchToWeather(WeatherType.CLEAR);
    transitionAlpha = 1.0f;
    transitionState = TransitionState.ACTIVE;
    durationRemaining = rollDuration();
  }

  /** Tipo de clima ativo no momento. */
  public WeatherType getCurrentType() {
    return currentType;
  }

  /** Alpha atual da transição (0.0–1.0). */
  public float getTransitionAlpha() {
    return transitionAlpha;
  }

  /** Frames restantes de duração do clima atual. */
  public int getDurationRemaining() {
    return durationRemaining;
  }

  /** Configuração carregada do weather.properties. */
  public WeatherConfig getConfig() {
    return config;
  }
}

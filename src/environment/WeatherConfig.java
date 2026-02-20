package environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.EnumMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Carrega e fornece parâmetros de configuração do sistema de clima.
 * Lê do arquivo {@code weather.properties} na raiz do projeto,
 * com fallback para valores padrão sensatos.
 *
 * <p>Todas as durações na config são informadas em segundos reais
 * e convertidas internamente para frames (60 FPS).</p>
 */
public class WeatherConfig {

  private static final Logger LOGGER = Logger.getLogger(WeatherConfig.class.getName());
  private static final String CONFIG_FILE = "weather.properties";
  private static final int FPS = 60;

  // ── Valores padrão ──

  private boolean enabled = true;

  private int checkIntervalFrames = 60 * FPS;
  private int minDurationFrames = 90 * FPS;
  private int maxDurationFrames = 360 * FPS;

  private final Map<WeatherType, Integer> weights = new EnumMap<>(WeatherType.class);

  private int fogDawnBonus = 15;
  private int stormNightBonus = 5;

  private int drizzleParticleCount = 80;
  private int rainParticleCount = 150;
  private int stormParticleCount = 300;

  private float overcastDarkness = 0.15f;
  private float drizzleDarkness = 0.08f;
  private float rainDarkness = 0.20f;
  private float stormDarkness = 0.35f;

  private float fogMaxAlpha = 0.45f;

  private int transitionFadeFrames = 3 * FPS;

  private int thunderSfxIndex = -1;

  public WeatherConfig() {
    weights.put(WeatherType.CLEAR, 40);
    weights.put(WeatherType.OVERCAST, 20);
    weights.put(WeatherType.DRIZZLE, 15);
    weights.put(WeatherType.RAIN, 15);
    weights.put(WeatherType.FOG, 7);
    weights.put(WeatherType.STORM, 3);

    load();
  }

  private void load() {
    File file = new File(CONFIG_FILE);
    if (!file.exists()) {
      LOGGER.info("Arquivo " + CONFIG_FILE + " nao encontrado; usando valores padrao.");
      return;
    }

    Properties props = new Properties();
    try (InputStream is = new FileInputStream(file)) {
      props.load(is);
    } catch (IOException e) {
      LOGGER.log(Level.WARNING, "Falha ao carregar " + CONFIG_FILE, e);
      return;
    }

    enabled = getBool(props, "weather.enabled", enabled);

    checkIntervalFrames = getSeconds(props, "weather.check_interval_seconds", 60);
    minDurationFrames = getSeconds(props, "weather.min_duration_seconds", 90);
    maxDurationFrames = getSeconds(props, "weather.max_duration_seconds", 360);

    weights.put(WeatherType.CLEAR, getInt(props, "weather.weight.clear", 40));
    weights.put(WeatherType.OVERCAST, getInt(props, "weather.weight.overcast", 20));
    weights.put(WeatherType.DRIZZLE, getInt(props, "weather.weight.drizzle", 15));
    weights.put(WeatherType.RAIN, getInt(props, "weather.weight.rain", 15));
    weights.put(WeatherType.FOG, getInt(props, "weather.weight.fog", 7));
    weights.put(WeatherType.STORM, getInt(props, "weather.weight.storm", 3));

    fogDawnBonus = getInt(props, "weather.fog.dawn_bonus", fogDawnBonus);
    stormNightBonus = getInt(props, "weather.storm.night_bonus", stormNightBonus);

    drizzleParticleCount = getInt(props, "weather.particles.drizzle.count", drizzleParticleCount);
    rainParticleCount = getInt(props, "weather.particles.rain.count", rainParticleCount);
    stormParticleCount = getInt(props, "weather.particles.storm.count", stormParticleCount);

    overcastDarkness = getFloat(props, "weather.overlay.darkness.overcast", overcastDarkness);
    drizzleDarkness = getFloat(props, "weather.overlay.darkness.drizzle", drizzleDarkness);
    rainDarkness = getFloat(props, "weather.overlay.darkness.rain", rainDarkness);
    stormDarkness = getFloat(props, "weather.overlay.darkness.storm", stormDarkness);

    fogMaxAlpha = getFloat(props, "weather.fog.max_alpha", fogMaxAlpha);

    transitionFadeFrames = (int) (getFloat(props, "weather.transition.fade_seconds", 3.0f) * FPS);

    thunderSfxIndex = getInt(props, "weather.storm.thunder_sfx_index", thunderSfxIndex);
  }

  /** Converte segundos da config para frames (60 FPS). */
  private static int getSeconds(Properties props, String key, int defaultSeconds) {
    return getInt(props, key, defaultSeconds) * FPS;
  }

  private static int getInt(Properties props, String key, int defaultValue) {
    String val = props.getProperty(key);
    if (val == null || val.isBlank()) return defaultValue;
    try {
      return Integer.parseInt(val.trim());
    } catch (NumberFormatException e) {
      LOGGER.warning(() -> "Valor invalido para " + key + ": " + val);
      return defaultValue;
    }
  }

  private static float getFloat(Properties props, String key, float defaultValue) {
    String val = props.getProperty(key);
    if (val == null || val.isBlank()) return defaultValue;
    try {
      return Float.parseFloat(val.trim());
    } catch (NumberFormatException e) {
      LOGGER.warning(() -> "Valor invalido para " + key + ": " + val);
      return defaultValue;
    }
  }

  private static boolean getBool(Properties props, String key, boolean defaultValue) {
    String val = props.getProperty(key);
    if (val == null || val.isBlank()) return defaultValue;
    return Boolean.parseBoolean(val.trim());
  }

  // ── Getters ──

  public boolean isEnabled() { return enabled; }

  public int getCheckIntervalFrames() { return checkIntervalFrames; }

  public int getMinDurationFrames() { return minDurationFrames; }

  public int getMaxDurationFrames() { return maxDurationFrames; }

  public int getWeight(WeatherType type) { return weights.getOrDefault(type, 0); }

  public int getFogDawnBonus() { return fogDawnBonus; }

  public int getStormNightBonus() { return stormNightBonus; }

  public int getDrizzleParticleCount() { return drizzleParticleCount; }

  public int getRainParticleCount() { return rainParticleCount; }

  public int getStormParticleCount() { return stormParticleCount; }

  public float getOvercastDarkness() { return overcastDarkness; }

  public float getDrizzleDarkness() { return drizzleDarkness; }

  public float getRainDarkness() { return rainDarkness; }

  public float getStormDarkness() { return stormDarkness; }

  public float getFogMaxAlpha() { return fogMaxAlpha; }

  public int getTransitionFadeFrames() { return transitionFadeFrames; }

  public int getThunderSfxIndex() { return thunderSfxIndex; }
}

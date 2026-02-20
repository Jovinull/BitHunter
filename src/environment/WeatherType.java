package environment;

/**
 * Tipos de clima disponíveis no jogo.
 * Cada tipo define um efeito visual renderizado por overlay,
 * sem alterar sprites ou tiles do ambiente.
 */
public enum WeatherType {

  /** Céu limpo — sem efeito visual. */
  CLEAR,

  /** Nublado — leve escurecimento do cenário. */
  OVERCAST,

  /** Garoa — chuva leve com poucas partículas. */
  DRIZZLE,

  /** Chuva — partículas moderadas com escurecimento. */
  RAIN,

  /** Neblina — overlay translúcido que reduz visibilidade. */
  FOG,

  /** Trovoada — chuva intensa com flashes de relâmpago. */
  STORM
}

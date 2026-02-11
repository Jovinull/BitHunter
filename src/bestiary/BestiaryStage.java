package bestiary;

/**
 * Estágio de progresso de uma criatura no Bestiário do jogador.
 */
public enum BestiaryStage {
  /** Criatura nunca encontrada. */
  LOCKED,
  /** Primeira kill realizada — nome e sprite revelados. */
  DISCOVERED,
  /** Estágio 1 completo — stats básicos e drops comuns. */
  STAGE_1,
  /** Estágio 2 completo — drops incomuns, resistências e localizações. */
  STAGE_2,
  /** Estágio final completo — todos os detalhes revelados. */
  COMPLETED
}

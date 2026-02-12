package bestiary;

/**
 * Calcula os limiares de abates para cada estágio do Bestiário,
 * baseado na dificuldade e raridade da criatura (tabela estilo Tibia).
 *
 * <p>Para MUITO_RARO + DIFICIL/DESAFIADOR, faz fallback para NORMAL
 * (combinação não usual no Tibia — tratamento seguro).</p>
 */
public final class KillThresholds {

  private KillThresholds() {
  }

  /**
   * Retorna {@code [stage1, stage2, stageFinal]} para a combinação dada.
   */
  public static int[] getThresholds(BestiaryDifficulty difficulty, BestiaryRarity rarity) {
    if (rarity == BestiaryRarity.MUITO_RARO) {
      return getVeryRareThresholds(difficulty);
    }
    return getNormalThresholds(difficulty);
  }

  /** Retorna o total de kills para completar o bestiário inteiro (estágio final). */
  public static int getFinalThreshold(BestiaryDifficulty difficulty, BestiaryRarity rarity) {
    int[] t = getThresholds(difficulty, rarity);
    return t[2];
  }

  /**
   * Calcula o {@link BestiaryStage} atual a partir do número de kills.
   */
  public static BestiaryStage stageForKills(int kills, BestiaryDifficulty difficulty, BestiaryRarity rarity) {
    if (kills <= 0) {
      return BestiaryStage.LOCKED;
    }

    int[] t = getThresholds(difficulty, rarity);

    if (kills >= t[2]) {
      return BestiaryStage.COMPLETED;
    }
    if (kills >= t[1]) {
      return BestiaryStage.STAGE_2;
    }
    if (kills >= t[0]) {
      return BestiaryStage.STAGE_1;
    }
    return BestiaryStage.DISCOVERED;
  }

  // --- Tabelas internas ---

  private static int[] getNormalThresholds(BestiaryDifficulty difficulty) {
    return switch (difficulty) {
      case INOFENSIVO -> new int[]{5, 10, 25};
      case TRIVIAL -> new int[]{10, 100, 250};
      case FACIL -> new int[]{25, 250, 500};
      case MEDIO -> new int[]{50, 500, 1000};
      case DIFICIL -> new int[]{100, 1000, 2500};
      case DESAFIADOR -> new int[]{200, 2000, 5000};
    };
  }

  private static int[] getVeryRareThresholds(BestiaryDifficulty difficulty) {
    // MUITO_RARO: limiares reduzidos (somente para dificuldades até MEDIO)
    // Para DIFICIL/DESAFIADOR, fallback seguro para tabela NORMAL
    return switch (difficulty) {
      case INOFENSIVO, TRIVIAL, FACIL, MEDIO -> new int[]{2, 3, 5};
      case DIFICIL, DESAFIADOR -> getNormalThresholds(difficulty);
    };
  }
}

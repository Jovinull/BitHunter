package bestiary;

/**
 * Classificação de raridade de um drop no Bestiário.
 * Determina em qual estágio o item é revelado.
 *
 * <ul>
 *   <li>COMMON — revelado no estágio 1</li>
 *   <li>UNCOMMON — revelado no estágio 2</li>
 *   <li>SEMI_RARE — revelado no estágio final</li>
 *   <li>RARE — revelado no estágio final</li>
 *   <li>VERY_RARE — só aparece quando o item realmente dropou para o jogador</li>
 * </ul>
 */
public enum LootTier {
  COMMON,
  UNCOMMON,
  SEMI_RARE,
  RARE,
  VERY_RARE
}

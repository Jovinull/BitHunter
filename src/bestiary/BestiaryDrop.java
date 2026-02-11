package bestiary;

/**
 * Representa um item de espólio no cadastro do Bestiário.
 *
 * @param itemId   identificador interno do item (ex.: "item.coin.bronze")
 * @param itemName nome de exibição (traduzido ou fallback)
 * @param tier     classificação de raridade do drop
 */
public record BestiaryDrop(String itemId, String itemName, LootTier tier) {
}

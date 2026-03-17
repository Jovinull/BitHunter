package achievement;

/**
 * Definicao imutavel de uma conquista.
 *
 * @param id            identificador unico (ex.: "ach.first_blood")
 * @param nameKey       chave i18n para o nome
 * @param descKey       chave i18n para a descricao
 * @param iconSprite    caminho do sprite (sem extensao)
 * @param category      categoria da conquista
 * @param prerequisiteId id da conquista pre-requisito (null se nenhum)
 * @param gridX         posicao X na arvore visual
 * @param gridY         posicao Y na arvore visual
 */
public record AchievementDef(
    String id,
    String nameKey,
    String descKey,
    String iconSprite,
    AchievementCategory category,
    String prerequisiteId,
    int gridX,
    int gridY) {
}

package achievement;

/**
 * Categorias de conquistas do jogo.
 */
public enum AchievementCategory {

  COMBAT("achievement.category.combat"),
  COLLECTION("achievement.category.collection"),
  EXPLORATION("achievement.category.exploration"),
  PROGRESSION("achievement.category.progression"),
  PETS("achievement.category.pets"),
  BESTIARY("achievement.category.bestiary"),
  SPECIAL("achievement.category.special");

  private final String translationKey;

  AchievementCategory(String translationKey) {
    this.translationKey = translationKey;
  }

  public String getTranslationKey() {
    return translationKey;
  }
}

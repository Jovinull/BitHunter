package bestiary;

/**
 * Classe/categoria de criatura no Bestiário (agrupamento visual).
 * Cada criatura pertence a exatamente uma classe.
 */
public enum CreatureClass {
  SLIMES("bestiary.class.slimes"),
  ANIMAIS("bestiary.class.animais"),
  HUMANOIDES("bestiary.class.humanoides"),
  MORTOS_VIVOS("bestiary.class.mortos_vivos");

  private final String translationKey;

  CreatureClass(String translationKey) {
    this.translationKey = translationKey;
  }

  public String getTranslationKey() {
    return translationKey;
  }
}

package bestiary;

import java.util.List;

/**
 * Definição imutável de uma criatura no Bestiário (dados carregados do arquivo de recursos).
 * Cada instância representa a "ficha" de uma espécie de monstro.
 */
public final class BestiaryCreature {

  private final String id;           // ID estável no bestiário (ex.: "greenSlime")
  private final String monsterRef;   // internalId do monstro no jogo (ex.: "monster.greenSlime")
  private final String nameKey;      // chave de tradução para o nome
  private final CreatureClass creatureClass;
  private final BestiaryDifficulty difficulty;
  private final BestiaryRarity rarity;
  private final String spriteRef;    // caminho do sprite (ex.: "/monster/greenslime_down_1")

  // Stats base (somente o que existe no projeto)
  private final int hp;
  private final int speed;
  private final int armor;   // defense no engine

  // Comportamento de combate
  private final String behavior;     // MELEE, RANGED, MIXED
  private final boolean usesSpells;

  // Drops
  private final List<BestiaryDrop> drops;

  // Localizações
  private final List<BestiaryLocation> locations;

  // Pontos de recompensa ao completar o bestiário desta criatura
  private final int completionPoints;

  public BestiaryCreature(
      String id, String monsterRef, String nameKey,
      CreatureClass creatureClass, BestiaryDifficulty difficulty, BestiaryRarity rarity,
      String spriteRef, int hp, int speed, int armor,
      String behavior, boolean usesSpells,
      List<BestiaryDrop> drops, List<BestiaryLocation> locations,
      int completionPoints) {
    this.id = id;
    this.monsterRef = monsterRef;
    this.nameKey = nameKey;
    this.creatureClass = creatureClass;
    this.difficulty = difficulty;
    this.rarity = rarity;
    this.spriteRef = spriteRef;
    this.hp = hp;
    this.speed = speed;
    this.armor = armor;
    this.behavior = behavior;
    this.usesSpells = usesSpells;
    this.drops = List.copyOf(drops);
    this.locations = List.copyOf(locations);
    this.completionPoints = completionPoints;
  }

  // --- Getters ---

  public String getId() { return id; }
  public String getMonsterRef() { return monsterRef; }
  public String getNameKey() { return nameKey; }
  public CreatureClass getCreatureClass() { return creatureClass; }
  public BestiaryDifficulty getDifficulty() { return difficulty; }
  public BestiaryRarity getRarity() { return rarity; }
  public String getSpriteRef() { return spriteRef; }
  public int getHp() { return hp; }
  public int getSpeed() { return speed; }
  public int getArmor() { return armor; }
  public String getBehavior() { return behavior; }
  public boolean isUsesSpells() { return usesSpells; }
  public List<BestiaryDrop> getDrops() { return drops; }
  public List<BestiaryLocation> getLocations() { return locations; }
  public int getCompletionPoints() { return completionPoints; }
}

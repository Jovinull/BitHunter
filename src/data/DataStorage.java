package data;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DataStorage implements Serializable {
  @Serial
  private static final long serialVersionUID = 1L;

  // PLAYER STATS
  private int level;
  private int maxLife;
  private int life;
  private int maxMana;
  private int mana;
  private int strength;
  private int dexterity;
  private int exp;
  private int nextLevelExp;
  private int coin;

  // PLAYER INVENTORY
  private final ArrayList<String> itemNames = new ArrayList<>();
  private final ArrayList<Integer> itemAmounts = new ArrayList<>();

  private int currentWeaponSlot;
  private int currentShieldSlot;
  private int currentBootsSlot;

  // ===== PETS =====
  private final ArrayList<String> ownedPetIds = new ArrayList<>();
  private String activePetId;

  // nível e xp na mesma ordem de ownedPetIds
  private final ArrayList<Integer> petLevels = new ArrayList<>();
  private final ArrayList<Integer> petExps = new ArrayList<>();

  // ===== PET INCUBATOR =====
  private boolean incubatorActive;
  private String incubatorPetId;
  private int incubatorRemainingTicks;
  private String incubatorEggName;
  private int incubatorTotalTicks;

  // ===== BESTIARY =====
  private HashMap<String, Integer> bestiaryKills = new HashMap<>();
  private HashMap<String, Set<String>> bestiaryRevealedVeryRare = new HashMap<>();
  private int bestiaryPoints;

  // ===== IDENTIDADE DO JOGADOR =====
  private String playerName;
  private int characterClass;

  // ===== TEMPO DE JOGO =====
  // Total de segundos jogados neste save (acumulado entre sessões).
  // Saves antigos não possuem este campo; o getter retorna 0 nesse caso.
  private long totalPlayTimeSeconds;

  // ===== ESTATÍSTICAS DE JOGO =====
  // Mapa flexível de métricas acumuladas (kills, mortes, dano, passos, etc.).
  // Pode ser null em saves antigos; o getter trata isso.
  private HashMap<String, Long> gameStats;

  // ===== CONQUISTAS =====
  private HashSet<String> unlockedAchievements;
  private HashMap<String, Integer> achievementCounters;

  // ===== CHEAT CODES (ONE_TIME) =====
  // Armazena os códigos ONE_TIME já utilizados para impedir reuso entre sessões.
  // Pode ser null em saves antigos (antes desta feature); o getter trata isso.
  private HashSet<String> usedCheatCodes;

  // OBJECT ON MAP
  private String[][] mapObjectNames;
  private int[][] mapObjectWorldX;
  private int[][] mapObjectWorldY;
  private String[][] mapObjectLootNames;
  private boolean[][] mapObjectOpened;

  public int getLevel() {
    return level;
  }

  public void setLevel(int level) {
    this.level = level;
  }

  public int getMaxLife() {
    return maxLife;
  }

  public void setMaxLife(int maxLife) {
    this.maxLife = maxLife;
  }

  public int getLife() {
    return life;
  }

  public void setLife(int life) {
    this.life = life;
  }

  public int getMaxMana() {
    return maxMana;
  }

  public void setMaxMana(int maxMana) {
    this.maxMana = maxMana;
  }

  public int getMana() {
    return mana;
  }

  public void setMana(int mana) {
    this.mana = mana;
  }

  public int getStrength() {
    return strength;
  }

  public void setStrength(int strength) {
    this.strength = strength;
  }

  public int getDexterity() {
    return dexterity;
  }

  public void setDexterity(int dexterity) {
    this.dexterity = dexterity;
  }

  public int getExp() {
    return exp;
  }

  public void setExp(int exp) {
    this.exp = exp;
  }

  public int getNextLevelExp() {
    return nextLevelExp;
  }

  public void setNextLevelExp(int nextLevelExp) {
    this.nextLevelExp = nextLevelExp;
  }

  public int getCoin() {
    return coin;
  }

  public void setCoin(int coin) {
    this.coin = coin;
  }

  public List<String> getItemNames() {
    return itemNames;
  }

  public List<Integer> getItemAmounts() {
    return itemAmounts;
  }

  public int getCurrentWeaponSlot() {
    return currentWeaponSlot;
  }

  public void setCurrentWeaponSlot(int currentWeaponSlot) {
    this.currentWeaponSlot = currentWeaponSlot;
  }

  public int getCurrentShieldSlot() {
    return currentShieldSlot;
  }

  public void setCurrentShieldSlot(int currentShieldSlot) {
    this.currentShieldSlot = currentShieldSlot;
  }

  public String[][] getMapObjectNames() {
    return mapObjectNames;
  }

  public void setMapObjectNames(String[][] mapObjectNames) {
    this.mapObjectNames = mapObjectNames;
  }

  public int[][] getMapObjectWorldX() {
    return mapObjectWorldX;
  }

  public void setMapObjectWorldX(int[][] mapObjectWorldX) {
    this.mapObjectWorldX = mapObjectWorldX;
  }

  public int[][] getMapObjectWorldY() {
    return mapObjectWorldY;
  }

  public void setMapObjectWorldY(int[][] mapObjectWorldY) {
    this.mapObjectWorldY = mapObjectWorldY;
  }

  public String[][] getMapObjectLootNames() {
    return mapObjectLootNames;
  }

  public void setMapObjectLootNames(String[][] mapObjectLootNames) {
    this.mapObjectLootNames = mapObjectLootNames;
  }

  public boolean[][] getMapObjectOpened() {
    return mapObjectOpened;
  }

  public void setMapObjectOpened(boolean[][] mapObjectOpened) {
    this.mapObjectOpened = mapObjectOpened;
  }

  public int getCurrentBootsSlot() {
    return currentBootsSlot;
  }

  public void setCurrentBootsSlot(int currentBootsSlot) {
    this.currentBootsSlot = currentBootsSlot;
  }

  public List<String> getOwnedPetIds() {
    return ownedPetIds;
  }

  public String getActivePetId() {
    return activePetId;
  }

  public void setActivePetId(String activePetId) {
    this.activePetId = activePetId;
  }

  public List<Integer> getPetLevels() {
    return petLevels;
  }

  public List<Integer> getPetExps() {
    return petExps;
  }

  public boolean isIncubatorActive() {
    return incubatorActive;
  }

  public void setIncubatorActive(boolean incubatorActive) {
    this.incubatorActive = incubatorActive;
  }

  public String getIncubatorPetId() {
    return incubatorPetId;
  }

  public void setIncubatorPetId(String incubatorPetId) {
    this.incubatorPetId = incubatorPetId;
  }

  public int getIncubatorRemainingTicks() {
    return incubatorRemainingTicks;
  }

  public void setIncubatorRemainingTicks(int incubatorRemainingTicks) {
    this.incubatorRemainingTicks = incubatorRemainingTicks;
  }

  public String getIncubatorEggName() {
    return incubatorEggName;
  }

  public void setIncubatorEggName(String incubatorEggName) {
    this.incubatorEggName = incubatorEggName;
  }

  public int getIncubatorTotalTicks() {
    return incubatorTotalTicks;
  }

  public void setIncubatorTotalTicks(int incubatorTotalTicks) {
    this.incubatorTotalTicks = incubatorTotalTicks;
  }

  // ===== BESTIARY getters/setters =====

  public Map<String, Integer> getBestiaryKills() {
    return bestiaryKills;
  }

  public void setBestiaryKills(Map<String, Integer> bestiaryKills) {
    this.bestiaryKills = bestiaryKills != null ? new HashMap<>(bestiaryKills) : new HashMap<>();
  }

  public Map<String, Set<String>> getBestiaryRevealedVeryRare() {
    return bestiaryRevealedVeryRare;
  }

  public void setBestiaryRevealedVeryRare(Map<String, Set<String>> bestiaryRevealedVeryRare) {
    this.bestiaryRevealedVeryRare = bestiaryRevealedVeryRare != null
        ? new HashMap<>(bestiaryRevealedVeryRare) : new HashMap<>();
  }

  public int getBestiaryPoints() {
    return bestiaryPoints;
  }

  public void setBestiaryPoints(int bestiaryPoints) {
    this.bestiaryPoints = bestiaryPoints;
  }

  // ===== CONQUISTAS getters/setters =====

  /**
   * Retorna os IDs das conquistas desbloqueadas.
   * Null-safe: retorna {@link HashSet} vazio para saves antigos.
   */
  public Set<String> getUnlockedAchievements() {
    if (unlockedAchievements == null) {
      unlockedAchievements = new HashSet<>();
    }
    return unlockedAchievements;
  }

  public void setUnlockedAchievements(Set<String> unlockedAchievements) {
    this.unlockedAchievements = unlockedAchievements != null ? new HashSet<>(unlockedAchievements) : new HashSet<>();
  }

  /**
   * Retorna os contadores cumulativos de conquistas.
   * Null-safe: retorna {@link HashMap} vazio para saves antigos.
   */
  public Map<String, Integer> getAchievementCounters() {
    if (achievementCounters == null) {
      achievementCounters = new HashMap<>();
    }
    return achievementCounters;
  }

  public void setAchievementCounters(Map<String, Integer> achievementCounters) {
    this.achievementCounters = achievementCounters != null ? new HashMap<>(achievementCounters) : new HashMap<>();
  }

  // ===== TEMPO DE JOGO getters/setters =====

  /**
   * Total de segundos jogados neste save.
   * Retorna 0 para saves antigos (campo primitivo long — default seguro).
   */
  public long getTotalPlayTimeSeconds() {
    return totalPlayTimeSeconds;
  }

  public void setTotalPlayTimeSeconds(long totalPlayTimeSeconds) {
    this.totalPlayTimeSeconds = totalPlayTimeSeconds;
  }

  // ===== CHEAT CODES getters/setters =====

  /**
   * Retorna os códigos ONE_TIME já usados.
   * <p>Null-safe: retorna {@link HashSet} vazio para saves antigos
   * que não possuíam este campo (desserialização atribui null).</p>
   */
  public Set<String> getUsedCheatCodes() {
    if (usedCheatCodes == null) {
      usedCheatCodes = new HashSet<>();
    }
    return usedCheatCodes;
  }

  public void setUsedCheatCodes(Set<String> usedCheatCodes) {
    this.usedCheatCodes = usedCheatCodes != null ? new HashSet<>(usedCheatCodes) : new HashSet<>();
  }

  // ===== IDENTIDADE DO JOGADOR getters/setters =====

  /**
   * Nome do jogador definido na criação do save.
   * Pode ser null em saves antigos (antes do sistema de slots).
   */
  public String getPlayerName() {
    return playerName;
  }

  public void setPlayerName(String playerName) {
    this.playerName = playerName;
  }

  /** Índice da classe (0=Guerreiro, 1=Ladrão, 2=Feiticeiro). */
  public int getCharacterClass() {
    return characterClass;
  }

  public void setCharacterClass(int characterClass) {
    this.characterClass = characterClass;
  }

  // ===== ESTATÍSTICAS DE JOGO getters/setters =====

  /**
   * Retorna o mapa de estatísticas de jogo.
   * Null-safe: retorna {@link HashMap} vazio para saves antigos.
   */
  public Map<String, Long> getGameStats() {
    if (gameStats == null) {
      gameStats = new HashMap<>();
    }
    return gameStats;
  }

  public void setGameStats(Map<String, Long> gameStats) {
    this.gameStats = gameStats != null ? new HashMap<>(gameStats) : new HashMap<>();
  }
}

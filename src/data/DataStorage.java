package data;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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
  private final List<String> itemNames = new ArrayList<>();
  private final List<Integer> itemAmounts = new ArrayList<>();

  private int currentWeaponSlot;
  private int currentShieldSlot;
  private int currentBootsSlot;

  // ===== PETS =====
  private final List<String> ownedPetIds = new ArrayList<>();
  private String activePetId;

  // ===== PET INCUBATOR =====
  private boolean incubatorActive;
  private String incubatorPetId;
  private int incubatorRemainingTicks;
  private String incubatorEggName;

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
}

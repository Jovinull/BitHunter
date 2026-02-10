package data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import entity.Entity;
import main.GamePanel;
import pets.PetIncubator;

public class SaveLoad {

  private static final Logger LOG = Logger.getLogger(SaveLoad.class.getName());
  private static final String SAVE_FILE = "save.dat";

  private final GamePanel gp;

  public SaveLoad(GamePanel gp) {
    this.gp = gp;
  }

  public void save() {
    File file = new File(SAVE_FILE);
    try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
      DataStorage ds = buildDataStorage();
      oos.writeObject(ds);
      LOG.fine(() -> "Game saved: " + file.getAbsolutePath());
    } catch (IOException ex) {
      LOG.log(Level.SEVERE, "Failed to save game", ex);
    }
  }

  public void load() {
    File file = new File(SAVE_FILE);
    if (!file.exists()) {
      LOG.info(() -> "No save file found at " + file.getAbsolutePath());
      return;
    }

    try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
      DataStorage ds = readDataStorage(ois);
      if (ds == null) {
        return;
      }

      applyFromDataStorage(ds);
      LOG.fine(() -> "Game loaded: " + file.getAbsolutePath());
    } catch (IOException | ClassNotFoundException ex) {
      LOG.log(Level.SEVERE, "Failed to load game", ex);
    }
  }

  /* ===== Helpers ===== */

  private DataStorage readDataStorage(ObjectInputStream ois)
      throws IOException, ClassNotFoundException {

    Object raw = ois.readObject();

    if (raw == null) {
      LOG.warning("Invalid save file format (not a DataStorage).");
      return null;
    }

    try {
      return (DataStorage) raw;
    } catch (ClassCastException ex) { // variável descartada (unnamed pattern)
      LOG.warning("Invalid save file format (not a DataStorage).");
      return null;
    }
  }

  private DataStorage buildDataStorage() {
    DataStorage ds = new DataStorage();
    copyPlayerStatsToStorage(ds);
    copyInventoryToStorage(ds);
    copyEquipmentToStorage(ds);
    copyPetsToStorage(ds);
    copyIncubatorToStorage(ds);
    copyMapObjectsToStorage(ds);
    return ds;
  }

  private void applyFromDataStorage(DataStorage ds) {
    applyPlayerStats(ds);
    applyInventory(ds);
    applyMapObjects(ds);
    applyEquipment(ds);
    applyPets(ds);
    applyIncubator(ds);

    // Recalcular atributos derivados
    var player = gp.getPlayer();
    player.getAttack();
    player.getDefense();
    player.getAttackImage();
    player.recalculateSpeedFromEquipment();
  }

  /* ----- Copy to storage (SAVE) ----- */

  private void copyPlayerStatsToStorage(DataStorage ds) {
    var player = gp.getPlayer();
    ds.setLevel(player.getLevel());
    ds.setMaxLife(player.getMaxLife());
    ds.setLife(player.getLife());
    ds.setMaxMana(player.getMaxMana());
    ds.setMana(player.getMana());
    ds.setStrength(player.getStrength());
    ds.setDexterity(player.getDexterity());
    ds.setExp(player.getExp());
    ds.setNextLevelExp(player.getNextLevelExp());
    ds.setCoin(player.getCoin());
  }

  private void copyInventoryToStorage(DataStorage ds) {
    var player = gp.getPlayer();
    var inv = player.getInventory();
    for (int i = 0; i < inv.size(); i++) {
      Entity item = inv.get(i);
      ds.getItemNames().add(item.getInternalId());
      ds.getItemAmounts().add(item.getAmount());
    }
  }

  private void copyEquipmentToStorage(DataStorage ds) {
    var player = gp.getPlayer();
    ds.setCurrentWeaponSlot(player.getCurrentWeaponSlot());
    ds.setCurrentShieldSlot(player.getCurrentShieldSlot());
    ds.setCurrentBootsSlot(player.getCurrentBootsSlot());
  }

  private void copyMapObjectsToStorage(DataStorage ds) {
    Entity[][] objects = gp.getObj();
    int maps = objects.length;
    int slots = objects[0].length;

    ds.setMapObjectNames(new String[maps][slots]);
    ds.setMapObjectWorldX(new int[maps][slots]);
    ds.setMapObjectWorldY(new int[maps][slots]);
    ds.setMapObjectLootNames(new String[maps][slots]);
    ds.setMapObjectOpened(new boolean[maps][slots]);

    for (int mapNum = 0; mapNum < maps; mapNum++) {
      for (int i = 0; i < slots; i++) {
        Entity objectEntity = objects[mapNum][i];
        if (objectEntity == null) {
          ds.getMapObjectNames()[mapNum][i] = "NA";
        } else {
          ds.getMapObjectNames()[mapNum][i] = objectEntity.getInternalId();
          ds.getMapObjectWorldX()[mapNum][i] = objectEntity.getWorldX();
          ds.getMapObjectWorldY()[mapNum][i] = objectEntity.getWorldY();

          if (objectEntity.getLoot() != null) {
            ds.getMapObjectLootNames()[mapNum][i] = objectEntity.getLoot().getInternalId();
          }
          ds.getMapObjectOpened()[mapNum][i] = objectEntity.isOpened();
        }
      }
    }
  }

  /* ----- Apply from storage (LOAD) ----- */

  private void applyPlayerStats(DataStorage ds) {
    var player = gp.getPlayer();
    player.setLevel(ds.getLevel());
    player.setMaxLife(ds.getMaxLife());
    player.setLife(ds.getLife());
    player.setMaxMana(ds.getMaxMana());
    player.setMana(ds.getMana());
    player.setStrength(ds.getStrength());
    player.setDexterity(ds.getDexterity());
    player.setExp(ds.getExp());
    player.setNextLevelExp(ds.getNextLevelExp());
    player.setCoin(ds.getCoin());
  }

  private void applyInventory(DataStorage ds) {
    var player = gp.getPlayer();
    var inv = player.getInventory();
    inv.clear();

    var generator = gp.geteGenerator();

    for (int i = 0; i < ds.getItemNames().size(); i++) {
      String itemName = ds.getItemNames().get(i);
      int amount = ds.getItemAmounts().get(i);
      Entity item = generator.getObject(itemName);
      item.setAmount(amount);
      inv.add(item);
    }
  }

  private void applyMapObjects(DataStorage ds) {
    Entity[][] objects = gp.getObj();
    int maps = objects.length;

    var generator = gp.geteGenerator();

    for (int mapNum = 0; mapNum < maps; mapNum++) {
      int slots = objects[mapNum].length;
      for (int i = 0; i < slots; i++) {
        String name = ds.getMapObjectNames()[mapNum][i];
        if ("NA".equals(name)) {
          objects[mapNum][i] = null;
        } else {
          Entity objectEntity = generator.getObject(name);
          objects[mapNum][i] = objectEntity;

          objectEntity.setWorldX(ds.getMapObjectWorldX()[mapNum][i]);
          objectEntity.setWorldY(ds.getMapObjectWorldY()[mapNum][i]);

          String lootName = ds.getMapObjectLootNames()[mapNum][i];
          if (lootName != null) {
            objectEntity.setLoot(generator.getObject(lootName));
          }

          objectEntity.setOpened(ds.getMapObjectOpened()[mapNum][i]);

          if (objectEntity.isOpened()) {
            objectEntity.applyOpenedAppearance();
          }
        }
      }
    }
  }

  private void applyEquipment(DataStorage ds) {
    var player = gp.getPlayer();

    int weaponSlot = ds.getCurrentWeaponSlot();
    int shieldSlot = ds.getCurrentShieldSlot();
    int bootsSlot = ds.getCurrentBootsSlot();

    var inv = player.getInventory();
    if (weaponSlot >= 0 && weaponSlot < inv.size()) {
      player.setCurrentWeapon(inv.get(weaponSlot));
    }
    if (shieldSlot >= 0 && shieldSlot < inv.size()) {
      player.setCurrentShield(inv.get(shieldSlot));
    }
    if (bootsSlot >= 0 && bootsSlot < inv.size()) {
      player.setCurrentBoots(inv.get(bootsSlot));
    }
  }

  private void copyPetsToStorage(DataStorage ds) {
    var player = gp.getPlayer();

    ds.getOwnedPetIds().clear();
    ds.getPetLevels().clear();
    ds.getPetExps().clear();

    for (String petId : player.getOwnedPets()) {
      ds.getOwnedPetIds().add(petId);
      ds.getPetLevels().add(player.getPetLevelFor(petId));
      ds.getPetExps().add(player.getPetExpFor(petId));
    }

    // pet ativo (pode ser null)
    ds.setActivePetId(player.getActivePetId());
  }

  private void applyPets(DataStorage ds) {
    var player = gp.getPlayer();

    player.getOwnedPets().clear();
    player.clearPetProgress();

    List<String> ids = ds.getOwnedPetIds();
    List<Integer> levels = ds.getPetLevels();
    List<Integer> exps = ds.getPetExps();

    int size = ids.size();
    for (int i = 0; i < size; i++) {
      String petId = ids.get(i);
      if (petId == null || petId.isBlank()) {
        continue;
      }

      player.addOwnedPet(petId);

      int lvl = (i < levels.size()) ? levels.get(i) : 1;
      int xp = (i < exps.size()) ? exps.get(i) : 0;

      player.restorePetProgress(petId, lvl, xp);
    }

    String activeId = ds.getActivePetId();
    if (activeId != null && !activeId.isBlank() && player.getOwnedPets().contains(activeId)) {
      player.equipPetById(activeId);
    } else {
      player.unequipPet();
    }
  }

  private void copyIncubatorToStorage(DataStorage ds) {
    PetIncubator incubator = gp.getPetIncubator();
    if (incubator == null) {
      ds.setIncubatorActive(false);
      ds.setIncubatorPetId(null);
      ds.setIncubatorRemainingTicks(0);
      ds.setIncubatorEggName(null);
      ds.setIncubatorTotalTicks(0);
      return;
    }

    boolean active = incubator.hasActiveEgg();
    ds.setIncubatorActive(active);

    if (active) {
      ds.setIncubatorPetId(incubator.getPetId());
      ds.setIncubatorRemainingTicks(incubator.getRemainingTicks());
      ds.setIncubatorEggName(incubator.getEggName());
      ds.setIncubatorTotalTicks(incubator.getTotalTicks());
    } else {
      ds.setIncubatorPetId(null);
      ds.setIncubatorRemainingTicks(0);
      ds.setIncubatorEggName(null);
      ds.setIncubatorTotalTicks(0);
    }
  }

  private void applyIncubator(DataStorage ds) {
    PetIncubator incubator = gp.getPetIncubator();
    if (incubator == null) {
      return;
    }

    incubator.restoreFromSave(
        ds.isIncubatorActive(),
        ds.getIncubatorPetId(),
        ds.getIncubatorRemainingTicks(),
        ds.getIncubatorEggName(),
        ds.getIncubatorTotalTicks());
  }
}

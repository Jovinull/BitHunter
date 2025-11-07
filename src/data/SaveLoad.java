package data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import main.GamePanel;

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
    } catch (IOException e) {
      LOG.log(Level.SEVERE, "Failed to save game", e);
    }
  }

  public void load() {
    File file = new File(SAVE_FILE);
    if (!file.exists()) {
      LOG.info(() -> "No save file found at " + file.getAbsolutePath());
      return;
    }

    try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
      Object obj = ois.readObject();
      if (!(obj instanceof DataStorage ds)) {
        LOG.warning("Invalid save file format (not a DataStorage).");
        return;
      }
      applyFromDataStorage(ds);
      LOG.fine(() -> "Game loaded: " + file.getAbsolutePath());
    } catch (IOException | ClassNotFoundException e) {
      LOG.log(Level.SEVERE, "Failed to load game", e);
    }
  }

  /* ===== Helpers ===== */

  private DataStorage buildDataStorage() {
    DataStorage ds = new DataStorage();
    copyPlayerStatsToStorage(ds);
    copyInventoryToStorage(ds);
    copyEquipmentToStorage(ds);
    copyMapObjectsToStorage(ds);
    return ds;
  }

  private void applyFromDataStorage(DataStorage ds) {
    applyPlayerStats(ds);
    applyInventory(ds);
    applyMapObjects(ds);
    applyEquipment(ds);
    gp.player.getAttack();
    gp.player.getDefense();
    gp.player.getAttackImage();
  }

  /* ----- Copy to storage (SAVE) ----- */

  private void copyPlayerStatsToStorage(DataStorage ds) {
    ds.setLevel(gp.player.level);
    ds.setMaxLife(gp.player.maxLife);
    ds.setLife(gp.player.life);
    ds.setMaxMana(gp.player.maxMana);
    ds.setMana(gp.player.mana);
    ds.setStrength(gp.player.strength);
    ds.setDexterity(gp.player.dexterity);
    ds.setExp(gp.player.exp);
    ds.setNextLevelExp(gp.player.nextLevelExp);
    ds.setCoin(gp.player.coin);
  }

  private void copyInventoryToStorage(DataStorage ds) {
    for (int i = 0; i < gp.player.inventory.size(); i++) {
      ds.getItemNames().add(gp.player.inventory.get(i).name);
      ds.getItemAmounts().add(gp.player.inventory.get(i).amount);
    }
  }

  private void copyEquipmentToStorage(DataStorage ds) {
    ds.setCurrentWeaponSlot(gp.player.getCurrentWeaponSlot());
    ds.setCurrentShieldSlot(gp.player.getCurrentShieldSlot());
  }

  private void copyMapObjectsToStorage(DataStorage ds) {
    int maps = gp.maxMap;
    int slots = gp.obj[1].length;

    ds.setMapObjectNames(new String[maps][slots]);
    ds.setMapObjectWorldX(new int[maps][slots]);
    ds.setMapObjectWorldY(new int[maps][slots]);
    ds.setMapObjectLootNames(new String[maps][slots]);
    ds.setMapObjectOpened(new boolean[maps][slots]);

    for (int mapNum = 0; mapNum < maps; mapNum++) {
      for (int i = 0; i < slots; i++) {
        if (gp.obj[mapNum][i] == null) {
          ds.getMapObjectNames()[mapNum][i] = "NA";
        } else {
          ds.getMapObjectNames()[mapNum][i] = gp.obj[mapNum][i].name;
          ds.getMapObjectWorldX()[mapNum][i] = gp.obj[mapNum][i].worldX;
          ds.getMapObjectWorldY()[mapNum][i] = gp.obj[mapNum][i].worldY;
          if (gp.obj[mapNum][i].loot != null) {
            ds.getMapObjectLootNames()[mapNum][i] = gp.obj[mapNum][i].loot.name;
          }
          ds.getMapObjectOpened()[mapNum][i] = gp.obj[mapNum][i].opened;
        }
      }
    }
  }

  /* ----- Apply from storage (LOAD) ----- */

  private void applyPlayerStats(DataStorage ds) {
    gp.player.level = ds.getLevel();
    gp.player.maxLife = ds.getMaxLife();
    gp.player.life = ds.getLife();
    gp.player.maxMana = ds.getMaxMana();
    gp.player.mana = ds.getMana();
    gp.player.strength = ds.getStrength();
    gp.player.dexterity = ds.getDexterity();
    gp.player.exp = ds.getExp();
    gp.player.nextLevelExp = ds.getNextLevelExp();
    gp.player.coin = ds.getCoin();
  }

  private void applyInventory(DataStorage ds) {
    gp.player.inventory.clear();
    for (int i = 0; i < ds.getItemNames().size(); i++) {
      gp.player.inventory.add(gp.eGenerator.getObject(ds.getItemNames().get(i)));
      gp.player.inventory.get(i).amount = ds.getItemAmounts().get(i);
    }
  }

  private void applyMapObjects(DataStorage ds) {
    for (int mapNum = 0; mapNum < gp.maxMap; mapNum++) {
      for (int i = 0; i < gp.obj[1].length; i++) {
        if ("NA".equals(ds.getMapObjectNames()[mapNum][i])) {
          gp.obj[mapNum][i] = null;
        } else {
          gp.obj[mapNum][i] = gp.eGenerator.getObject(ds.getMapObjectNames()[mapNum][i]);
          gp.obj[mapNum][i].worldX = ds.getMapObjectWorldX()[mapNum][i];
          gp.obj[mapNum][i].worldY = ds.getMapObjectWorldY()[mapNum][i];
          if (ds.getMapObjectLootNames()[mapNum][i] != null) {
            gp.obj[mapNum][i].setLoot(gp.eGenerator.getObject(ds.getMapObjectLootNames()[mapNum][i]));
          }
          gp.obj[mapNum][i].opened = ds.getMapObjectOpened()[mapNum][i];
          if (gp.obj[mapNum][i].opened) {
            gp.obj[mapNum][i].down1 = gp.obj[mapNum][i].image2;
          }
        }
      }
    }
  }

  private void applyEquipment(DataStorage ds) {
    int weaponSlot = ds.getCurrentWeaponSlot();
    int shieldSlot = ds.getCurrentShieldSlot();
    if (weaponSlot >= 0 && weaponSlot < gp.player.inventory.size()) {
      gp.player.currentWeapon = gp.player.inventory.get(weaponSlot);
    }
    if (shieldSlot >= 0 && shieldSlot < gp.player.inventory.size()) {
      gp.player.currentShield = gp.player.inventory.get(shieldSlot);
    }
  }
}

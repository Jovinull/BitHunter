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
import entity.Entity;

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

    // Recalcular atributos derivados (mantém seu fluxo original)
    gp.player.getAttack();
    gp.player.getDefense();
    gp.player.getAttackImage();
  }

  /* ----- Copy to storage (SAVE) ----- */

  private void copyPlayerStatsToStorage(DataStorage ds) {
    ds.setLevel(gp.player.getLevel());
    ds.setMaxLife(gp.player.getMaxLife());
    ds.setLife(gp.player.getLife());
    ds.setMaxMana(gp.player.getMaxMana());
    ds.setMana(gp.player.getMana());
    ds.setStrength(gp.player.getStrength());
    ds.setDexterity(gp.player.getDexterity());
    ds.setExp(gp.player.getExp());
    ds.setNextLevelExp(gp.player.getNextLevelExp());
    ds.setCoin(gp.player.getCoin());
  }

  private void copyInventoryToStorage(DataStorage ds) {
    var inv = gp.player.getInventory();
    for (int i = 0; i < inv.size(); i++) {
      Entity item = inv.get(i);
      ds.getItemNames().add(item.getName());
      ds.getItemAmounts().add(item.getAmount());
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
        Entity obj = gp.obj[mapNum][i];
        if (obj == null) {
          ds.getMapObjectNames()[mapNum][i] = "NA";
        } else {
          ds.getMapObjectNames()[mapNum][i] = obj.getName();
          ds.getMapObjectWorldX()[mapNum][i] = obj.getWorldX();
          ds.getMapObjectWorldY()[mapNum][i] = obj.getWorldY();

          if (obj.getLoot() != null) {
            ds.getMapObjectLootNames()[mapNum][i] = obj.getLoot().getName();
          }
          ds.getMapObjectOpened()[mapNum][i] = obj.isOpened();
        }
      }
    }
  }

  /* ----- Apply from storage (LOAD) ----- */

  private void applyPlayerStats(DataStorage ds) {
    gp.player.setLevel(ds.getLevel());
    gp.player.setMaxLife(ds.getMaxLife());
    gp.player.setLife(ds.getLife());
    gp.player.setMaxMana(ds.getMaxMana());
    gp.player.setMana(ds.getMana());
    gp.player.setStrength(ds.getStrength());
    gp.player.setDexterity(ds.getDexterity());
    gp.player.setExp(ds.getExp());
    gp.player.setNextLevelExp(ds.getNextLevelExp());
    gp.player.setCoin(ds.getCoin());
  }

  private void applyInventory(DataStorage ds) {
    var inv = gp.player.getInventory();
    inv.clear();
    for (int i = 0; i < ds.getItemNames().size(); i++) {
      String itemName = ds.getItemNames().get(i);
      int amount = ds.getItemAmounts().get(i);
      Entity item = gp.eGenerator.getObject(itemName);
      item.setAmount(amount);
      inv.add(item);
    }
  }

  private void applyMapObjects(DataStorage ds) {
    for (int mapNum = 0; mapNum < gp.maxMap; mapNum++) {
      for (int i = 0; i < gp.obj[1].length; i++) {
        String name = ds.getMapObjectNames()[mapNum][i];
        if ("NA".equals(name)) {
          gp.obj[mapNum][i] = null;
        } else {
          Entity obj = gp.eGenerator.getObject(name);
          gp.obj[mapNum][i] = obj;

          obj.setWorldX(ds.getMapObjectWorldX()[mapNum][i]);
          obj.setWorldY(ds.getMapObjectWorldY()[mapNum][i]);

          String lootName = ds.getMapObjectLootNames()[mapNum][i];
          if (lootName != null) {
            obj.setLoot(gp.eGenerator.getObject(lootName));
          }

          obj.setOpened(ds.getMapObjectOpened()[mapNum][i]);

          // Caso precise restaurar o sprite "aberto":
          // Requer que Entity tenha getImage2() e setDown1(BufferedImage).
          if (obj.isOpened()) {
            obj.applyOpenedAppearance();
          }
        }
      }
    }
  }

  private void applyEquipment(DataStorage ds) {
    int weaponSlot = ds.getCurrentWeaponSlot();
    int shieldSlot = ds.getCurrentShieldSlot();

    var inv = gp.player.getInventory();
    if (weaponSlot >= 0 && weaponSlot < inv.size()) {
      gp.player.setCurrentWeapon(inv.get(weaponSlot));
    }
    if (shieldSlot >= 0 && shieldSlot < inv.size()) {
      gp.player.setCurrentShield(inv.get(shieldSlot));
    }
  }
}

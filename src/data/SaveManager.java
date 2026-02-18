package data;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.util.logging.Level;
import java.util.logging.Logger;

import main.GamePanel;

/**
 * Gerencia até {@value #MAX_SLOTS} slots de save independentes.
 * <p>Cria a pasta {@code saves/} automaticamente e migra o antigo
 * {@code save.dat} para o Slot 1 na primeira execução.</p>
 */
public class SaveManager {

  private static final Logger LOG = Logger.getLogger(SaveManager.class.getName());

  public static final int MAX_SLOTS = 3;
  private static final String SAVES_DIR = "saves";
  private static final String SLOT_PREFIX = "slot";
  private static final String SLOT_EXT = ".dat";
  private static final String LEGACY_SAVE = "save.dat";

  private final GamePanel gp;
  private int currentSlot = -1;

  /**
   * Metadados leves de um slot (para exibição na UI sem carregar o jogo).
   */
  public record SlotInfo(
      boolean occupied,
      boolean corrupt,
      String playerName,
      int characterClass
  ) {
    public static SlotInfo empty() {
      return new SlotInfo(false, false, null, -1);
    }

    public static SlotInfo ofCorrupt() {
      return new SlotInfo(true, true, null, -1);
    }
  }

  public SaveManager(GamePanel gp) {
    this.gp = gp;
    ensureSavesDirectory();
    migrateLegacySave();
  }

  private File getSlotFile(int slot) {
    return new File(SAVES_DIR, SLOT_PREFIX + slot + SLOT_EXT);
  }

  private void ensureSavesDirectory() {
    File dir = new File(SAVES_DIR);
    if (!dir.exists() && !dir.mkdirs()) {
      LOG.warning("Não foi possível criar o diretório de saves: " + SAVES_DIR);
    }
  }

  /**
   * Migra o save legado ({@code save.dat}) para o Slot 1 automaticamente.
   * Só executa se o arquivo legado existe e o Slot 1 ainda está vazio.
   */
  private void migrateLegacySave() {
    File legacy = new File(LEGACY_SAVE);
    File slot1 = getSlotFile(1);
    if (legacy.exists() && !slot1.exists()) {
      try {
        Files.move(legacy.toPath(), slot1.toPath());
        LOG.info("Save legado migrado automaticamente para Slot 1");
      } catch (IOException e) {
        LOG.log(Level.WARNING, "Falha ao migrar save legado", e);
      }
    }
  }

  /**
   * Lê os metadados de um slot sem aplicar o estado ao jogo.
   */
  public SlotInfo getSlotInfo(int slot) {
    File file = getSlotFile(slot);
    if (!file.exists()) {
      return SlotInfo.empty();
    }

    try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
      Object raw = ois.readObject();
      if (raw instanceof DataStorage ds) {
        String name = ds.getPlayerName();
        return new SlotInfo(true, false,
            name != null ? name : "",
            ds.getCharacterClass());
      }
      return SlotInfo.ofCorrupt();
    } catch (Exception e) {
      LOG.log(Level.WARNING, "Save corrompido no slot " + slot, e);
      return SlotInfo.ofCorrupt();
    }
  }

  /**
   * Retorna os metadados de todos os slots (índice 0 = Slot 1).
   */
  public SlotInfo[] getAllSlotInfos() {
    SlotInfo[] infos = new SlotInfo[MAX_SLOTS];
    for (int i = 0; i < MAX_SLOTS; i++) {
      infos[i] = getSlotInfo(i + 1);
    }
    return infos;
  }

  /**
   * Salva o estado atual do jogo no slot especificado.
   */
  public void save(int slot) {
    ensureSavesDirectory();
    currentSlot = slot;
    gp.getSaveLoad().saveToFile(getSlotFile(slot));
  }

  /**
   * Salva no slot atual. Não faz nada se nenhum slot estiver ativo.
   */
  public void save() {
    if (currentSlot > 0) {
      save(currentSlot);
    }
  }

  /**
   * Carrega o save do slot especificado e o define como slot atual.
   *
   * @return {@code true} se o carregamento foi bem-sucedido
   */
  public boolean load(int slot) {
    File file = getSlotFile(slot);
    if (!file.exists()) {
      return false;
    }
    boolean success = gp.getSaveLoad().loadFromFile(file);
    if (success) {
      currentSlot = slot;
    }
    return success;
  }

  /**
   * Apaga o save do slot especificado.
   */
  public void delete(int slot) {
    File file = getSlotFile(slot);
    if (file.exists() && !file.delete()) {
      LOG.warning(() -> "Não foi possível apagar o save do slot " + slot);
    }
  }

  public int getCurrentSlot() {
    return currentSlot;
  }

  public void setCurrentSlot(int slot) {
    this.currentSlot = slot;
  }
}

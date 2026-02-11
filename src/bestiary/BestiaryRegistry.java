package bestiary;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Carrega e mantém o catálogo de criaturas do Bestiário a partir de
 * {@code /bestiary/creatures.properties}.
 *
 * <p>Data-driven: toda a definição de criaturas vem do arquivo de recursos,
 * evitando hardcode no código Java.</p>
 */
public final class BestiaryRegistry {

  private static final Logger LOG = Logger.getLogger(BestiaryRegistry.class.getName());
  private static final String DATA_PATH = "/bestiary/creatures.properties";

  /** Criaturas indexadas pelo ID do bestiário (ex.: "greenSlime"). */
  private final Map<String, BestiaryCreature> creaturesById = new LinkedHashMap<>();

  /** Criaturas indexadas pelo monsterRef (ex.: "monster.greenSlime") → bestiaryId. */
  private final Map<String, String> monsterRefToId = new LinkedHashMap<>();

  /** Lista ordenada de IDs (ordem do arquivo). */
  private final List<String> orderedIds = new ArrayList<>();

  /** Classes disponíveis na ordem do arquivo. */
  private final List<CreatureClass> orderedClasses = new ArrayList<>();

  public BestiaryRegistry() {
    load();
  }

  // --- API pública ---

  /** Retorna criatura pelo ID do bestiário, ou null. */
  public BestiaryCreature getById(String id) {
    return creaturesById.get(id);
  }

  /** Retorna criatura pelo internalId do monstro (ex.: "monster.greenSlime"), ou null. */
  public BestiaryCreature getByMonsterRef(String monsterRef) {
    String id = monsterRefToId.get(monsterRef);
    return id != null ? creaturesById.get(id) : null;
  }

  /** Retorna o ID do bestiário a partir do monsterRef, ou null. */
  public String getBestiaryIdForMonster(String monsterRef) {
    return monsterRefToId.get(monsterRef);
  }

  /** Todas as criaturas na ordem do arquivo. */
  public List<BestiaryCreature> getAllCreatures() {
    return orderedIds.stream()
        .map(creaturesById::get)
        .toList();
  }

  /** Criaturas de uma classe específica, na ordem do arquivo. */
  public List<BestiaryCreature> getCreaturesByClass(CreatureClass cc) {
    return orderedIds.stream()
        .map(creaturesById::get)
        .filter(c -> c.getCreatureClass() == cc)
        .toList();
  }

  /** Classes disponíveis na ordem do arquivo. */
  public List<CreatureClass> getOrderedClasses() {
    return Collections.unmodifiableList(orderedClasses);
  }

  public int getTotalCreatures() {
    return creaturesById.size();
  }

  // --- Carregamento ---

  private void load() {
    Properties props = new Properties();

    try (InputStream is = getClass().getResourceAsStream(DATA_PATH)) {
      if (is == null) {
        LOG.warning("Bestiary data not found: " + DATA_PATH);
        return;
      }
      props.load(is);
    } catch (IOException e) {
      LOG.log(Level.SEVERE, "Failed to load bestiary data", e);
      return;
    }

    // Carregar lista ordenada de classes
    String classesStr = props.getProperty("classes", "");
    for (String cls : classesStr.split(",")) {
      String trimmed = cls.trim();
      if (!trimmed.isEmpty()) {
        try {
          orderedClasses.add(CreatureClass.valueOf(trimmed));
        } catch (IllegalArgumentException e) {
          LOG.warning("Unknown creature class in data: " + trimmed);
        }
      }
    }

    // Carregar lista ordenada de criaturas
    String creaturesStr = props.getProperty("creatures", "");
    for (String idRaw : creaturesStr.split(",")) {
      String id = idRaw.trim();
      if (id.isEmpty()) continue;

      BestiaryCreature creature = parseCreature(id, props);
      if (creature != null) {
        creaturesById.put(id, creature);
        monsterRefToId.put(creature.getMonsterRef(), id);
        orderedIds.add(id);
      }
    }

    LOG.info(() -> "Bestiary loaded: " + creaturesById.size() + " creatures in "
        + orderedClasses.size() + " classes");
  }

  private BestiaryCreature parseCreature(String id, Properties props) {
    try {
      String monsterRef = req(props, id + ".monsterRef");
      String nameKey = props.getProperty(id + ".nameKey", monsterRef + ".name");

      CreatureClass cc = CreatureClass.valueOf(req(props, id + ".class"));
      BestiaryDifficulty diff = BestiaryDifficulty.valueOf(req(props, id + ".difficulty"));
      BestiaryRarity rar = BestiaryRarity.valueOf(
          props.getProperty(id + ".rarity", "NORMAL"));

      String spriteRef = props.getProperty(id + ".spriteRef", "");
      int hp = intProp(props, id + ".hp", 0);
      int speed = intProp(props, id + ".speed", 0);
      int armor = intProp(props, id + ".armor", 0);
      String behavior = props.getProperty(id + ".behavior", "MELEE");
      boolean usesSpells = Boolean.parseBoolean(
          props.getProperty(id + ".usesSpells", "false"));
      int completionPoints = intProp(props, id + ".completionPoints", 5);

      // Drops
      List<BestiaryDrop> drops = parseDrops(props, id);

      // Locations
      List<BestiaryLocation> locations = parseLocations(props, id);

      return new BestiaryCreature(
          id, monsterRef, nameKey, cc, diff, rar,
          spriteRef, hp, speed, armor,
          behavior, usesSpells, drops, locations,
          completionPoints);

    } catch (Exception e) {
      LOG.log(Level.WARNING, "Failed to parse bestiary creature: " + id, e);
      return null;
    }
  }

  private List<BestiaryDrop> parseDrops(Properties props, String id) {
    List<BestiaryDrop> drops = new ArrayList<>();
    String dropsStr = props.getProperty(id + ".drops", "");
    if (dropsStr.isEmpty()) return drops;

    for (String entry : dropsStr.split(",")) {
      String trimmed = entry.trim();
      String[] parts = trimmed.split(":");
      if (parts.length >= 2) {
        String itemId = parts[0].trim();
        String itemName = props.getProperty(id + ".dropName." + itemId, itemId);
        LootTier tier;
        try {
          tier = LootTier.valueOf(parts[1].trim());
        } catch (IllegalArgumentException e) {
          tier = LootTier.COMMON;
        }
        drops.add(new BestiaryDrop(itemId, itemName, tier));
      }
    }
    return drops;
  }

  private List<BestiaryLocation> parseLocations(Properties props, String id) {
    List<BestiaryLocation> locs = new ArrayList<>();
    String locsStr = props.getProperty(id + ".locations", "");
    if (locsStr.isEmpty()) return locs;

    for (String entry : locsStr.split(",")) {
      String trimmed = entry.trim();
      String[] parts = trimmed.split(":");
      if (parts.length >= 2) {
        try {
          int mapId = Integer.parseInt(parts[0].trim());
          String region = parts[1].trim();
          locs.add(new BestiaryLocation(mapId, region));
        } catch (NumberFormatException e) {
          LOG.warning("Invalid location in bestiary: " + trimmed);
        }
      }
    }
    return locs;
  }

  private static String req(Properties props, String key) {
    String value = props.getProperty(key);
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("Missing required bestiary property: " + key);
    }
    return value.trim();
  }

  private static int intProp(Properties props, String key, int defaultVal) {
    String val = props.getProperty(key);
    if (val == null || val.isBlank()) return defaultVal;
    try {
      return Integer.parseInt(val.trim());
    } catch (NumberFormatException e) {
      return defaultVal;
    }
  }
}

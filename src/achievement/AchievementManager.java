package achievement;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import main.GamePanel;
import main.UtilityTool;

/**
 * Servico central de conquistas.
 * <p>
 * Gerencia desbloqueio, contadores cumulativos, fila de toasts e cache de icones.
 * </p>
 */
public final class AchievementManager {

  private static final Logger LOG = Logger.getLogger(AchievementManager.class.getName());

  private static final int MAP_BOSS_ROOM = 3;
  private static final int TOTAL_GAME_MAPS = 5;
  private static final int KILL_THRESHOLD_PER_TYPE = 10;
  private static final int RICH_COIN_THRESHOLD = 100;
  private static final int TREES_THRESHOLD = 5;
  private static final int TWO_PETS_THRESHOLD = 2;

  private final GamePanel gp;

  /** IDs das conquistas desbloqueadas. */
  private final Set<String> unlocked = new HashSet<>();

  /** Contadores cumulativos (kills por tipo, total, etc.). */
  private final Map<String, Integer> counters = new HashMap<>();

  /** Fila de toasts pendentes para exibir. */
  private final Queue<AchievementDef> toastQueue = new LinkedList<>();

  /** Cache de sprites de icones (path → BufferedImage). */
  private final Map<String, BufferedImage> iconCache = new HashMap<>();

  public AchievementManager(GamePanel gp) {
    this.gp = gp;
  }

  // ===== Triggers =====

  /** Chamado quando um monstro e morto. */
  public void onMonsterKill(String monsterId) {
    if (monsterId == null || monsterId.isBlank()) return;

    increment("kill." + monsterId);
    increment("total_kills");
    setFlag("killed_type." + monsterId);

    tryUnlock("ach.first_blood", () -> getCounter("total_kills") >= 1);
    tryUnlock("ach.slime_hunter", () -> getCounter("kill.monster.greenSlime") >= KILL_THRESHOLD_PER_TYPE);
    tryUnlock("ach.red_menace", () -> getCounter("kill.monster.redSlime") >= KILL_THRESHOLD_PER_TYPE);
    tryUnlock("ach.bat_slayer", () -> getCounter("kill.monster.bat") >= KILL_THRESHOLD_PER_TYPE);
    tryUnlock("ach.orc_crusher", () -> getCounter("kill.monster.orc") >= KILL_THRESHOLD_PER_TYPE);
    tryUnlock("ach.monster_expert", () ->
        hasFlag("killed_type.monster.greenSlime")
            && hasFlag("killed_type.monster.redSlime")
            && hasFlag("killed_type.monster.bat")
            && hasFlag("killed_type.monster.orc"));
  }

  /** Chamado quando um item e coletado para o inventario. */
  public void onItemPickup(String itemId) {
    if (itemId == null || itemId.isBlank()) return;

    increment("total_pickups");
    setFlag("has." + itemId);

    tryUnlock("ach.first_loot", () -> getCounter("total_pickups") >= 1);
    tryUnlock("ach.armed", () -> hasAnyFlag("has.item.swordNormal", "has.item.axe", "has.item.pickaxe"));
    tryUnlock("ach.shielded", () -> hasAnyFlag("has.item.shieldWood", "has.item.shieldBlue", "has.item.shieldJava"));
    tryUnlock("ach.key_master", () -> hasFlag("has.item.key"));
    tryUnlock("ach.light_bringer", () -> hasFlag("has.item.lantern"));
    tryUnlock("ach.first_egg", () ->
        hasAnyFlag("has.egg.yellowSnail", "has.egg.greenSnail"));
  }

  /** Chamado quando o jogador sobe de nivel. */
  public void onLevelUp(int level) {
    setCounter("level", level);
    tryUnlock("ach.level_5", () -> level >= 5);
    tryUnlock("ach.level_10", () -> level >= 10);
    tryUnlock("ach.level_15", () -> level >= 15);
  }

  /** Chamado quando um tile interativo e destruido. */
  public void onInteractiveTileDestroyed(String tileId) {
    if (tileId == null) return;

    if (tileId.contains("dryTree") || tileId.contains("DryTree")) {
      increment("trees_destroyed");
      tryUnlock("ach.lumberjack", () -> getCounter("trees_destroyed") >= TREES_THRESHOLD);
    }
  }

  /** Chamado quando o boss e derrotado. */
  public void onBossDefeated() {
    setFlag("boss_defeated");
    tryUnlock("ach.lord_slayer", () -> true);
  }

  /** Chamado ao entrar em um novo mapa via teleporte. */
  public void onMapEntered(int mapIndex, int areaType) {
    setFlag("visited.map." + mapIndex);

    if (areaType == GamePanel.INDOOR) {
      setFlag("visited.area.indoor");
      tryUnlock("ach.indoor_visit", () -> true);
    } else if (areaType == GamePanel.DUNGEON) {
      setFlag("visited.area.dungeon");
      tryUnlock("ach.dungeon_delver", () -> true);
    }

    if (mapIndex == MAP_BOSS_ROOM) {
      setFlag("visited.boss_room");
      tryUnlock("ach.boss_arena", () -> true);
    }

    // Verifica se visitou todos os mapas
    tryUnlock("ach.world_explorer", () -> {
      for (int i = 0; i < TOTAL_GAME_MAPS; i++) {
        if (!hasFlag("visited.map." + i)) return false;
      }
      return true;
    });
  }

  /** Chamado quando o jogador usa um consumivel. */
  public void onConsumableUsed(String itemId) {
    if (itemId == null) return;

    if (itemId.contains("fireball") || itemId.contains("Fireball")) {
      increment("fireball_used");
      tryUnlock("ach.pyromancer", () -> true);
    }
  }

  /** Chamado quando um projetil e disparado. */
  public void onProjectileShot(String projectileId) {
    if (projectileId == null) return;

    if (projectileId.contains("fireball") || projectileId.contains("Fireball")) {
      increment("fireball_used");
      tryUnlock("ach.pyromancer", () -> true);
    }
  }

  /** Chamado quando um pet e chocado. */
  public void onPetHatched() {
    increment("pets_hatched");
    tryUnlock("ach.pet_hatched", () -> true);
    tryUnlock("ach.two_pets", () -> getCounter("pets_hatched") >= TWO_PETS_THRESHOLD);
  }

  /** Chamado em eventos do bestiario. */
  public void onBestiaryEvent(String type) {
    if ("discovered".equals(type)) {
      setFlag("bestiary_discovered");
      tryUnlock("ach.first_discovery", () -> true);
    } else if ("completed".equals(type)) {
      increment("bestiary_completed_count");
      tryUnlock("ach.first_complete", () -> true);
    } else if ("all_completed".equals(type)) {
      setFlag("bestiary_all_completed");
      tryUnlock("ach.bestiary_master", () -> true);
    }
  }

  /** Chamado quando as moedas do jogador mudam. */
  public void onCoinChange(int coins) {
    setCounter("coins_current", coins);
    tryUnlock("ach.rich", () -> coins >= RICH_COIN_THRESHOLD);
  }

  // ===== Toast Queue =====

  /** Retorna e remove o proximo toast da fila, ou null se vazia. */
  public AchievementDef pollToast() {
    return toastQueue.poll();
  }

  public boolean hasToasts() {
    return !toastQueue.isEmpty();
  }

  // ===== Queries =====

  public boolean isUnlocked(String id) {
    return unlocked.contains(id);
  }

  public int getUnlockedCount() {
    return unlocked.size();
  }

  public int getTotalCount() {
    return AchievementRegistry.getAll().size();
  }

  public int getUnlockedCountForCategory(AchievementCategory category) {
    int count = 0;
    for (AchievementDef def : AchievementRegistry.getByCategory(category)) {
      if (unlocked.contains(def.id())) {
        count++;
      }
    }
    return count;
  }

  // ===== Icones =====

  /** Carrega e cacheia o icone de uma conquista (48x48). */
  public BufferedImage getIcon(AchievementDef def) {
    return iconCache.computeIfAbsent(def.iconSprite(), this::loadIcon);
  }

  private BufferedImage loadIcon(String spriteRef) {
    if (spriteRef == null || spriteRef.isBlank()) return null;

    try {
      var stream = getClass().getResourceAsStream(spriteRef + ".png");
      if (stream == null) return null;
      BufferedImage raw = ImageIO.read(stream);
      UtilityTool tool = new UtilityTool();
      return tool.scaleImage(raw, GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);
    } catch (IOException e) {
      LOG.log(Level.WARNING, "Falha ao carregar icone de conquista: " + spriteRef, e);
      return null;
    }
  }

  // ===== Persistencia =====

  public Set<String> getUnlockedIds() {
    return new HashSet<>(unlocked);
  }

  public Map<String, Integer> getCounters() {
    return new HashMap<>(counters);
  }

  public void restoreFromSave(Set<String> savedUnlocked, Map<String, Integer> savedCounters) {
    unlocked.clear();
    counters.clear();

    if (savedUnlocked != null) {
      // Filtra IDs desconhecidos para compatibilidade
      for (String id : savedUnlocked) {
        if (AchievementRegistry.getById(id) != null) {
          unlocked.add(id);
        }
      }
    }

    if (savedCounters != null) {
      counters.putAll(savedCounters);
    }
  }

  public void reset() {
    unlocked.clear();
    counters.clear();
    toastQueue.clear();
  }

  // ===== Helpers internos =====

  private void tryUnlock(String achievementId, java.util.function.BooleanSupplier condition) {
    if (unlocked.contains(achievementId)) return;

    AchievementDef def = AchievementRegistry.getById(achievementId);
    if (def == null) return;

    // Verifica pre-requisito
    String prereq = def.prerequisiteId();
    if (prereq != null && !unlocked.contains(prereq)) return;

    // Verifica condicao
    if (!condition.getAsBoolean()) return;

    // Desbloqueia
    unlocked.add(achievementId);
    toastQueue.add(def);
    gp.playSE(3);

    LOG.info(() -> "Conquista desbloqueada: " + achievementId);
  }

  private void increment(String key) {
    counters.merge(key, 1, Integer::sum);
  }

  private int getCounter(String key) {
    return counters.getOrDefault(key, 0);
  }

  private void setCounter(String key, int value) {
    counters.put(key, value);
  }

  private void setFlag(String key) {
    counters.put(key, 1);
  }

  private boolean hasFlag(String key) {
    return counters.getOrDefault(key, 0) >= 1;
  }

  private boolean hasAnyFlag(String... keys) {
    for (String key : keys) {
      if (hasFlag(key)) return true;
    }
    return false;
  }
}

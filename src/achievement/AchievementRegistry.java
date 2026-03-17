package achievement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static achievement.AchievementCategory.*;

/**
 * Registro estatico das 28 conquistas do jogo.
 */
public final class AchievementRegistry {

  private static final Map<String, AchievementDef> DEFS = new LinkedHashMap<>();

  static {
    // ── COMBAT (7) ──
    reg("ach.first_blood", "achievement.first_blood.name", "achievement.first_blood.desc",
        "/objects/sword_normal", COMBAT, null, 0, 0);
    reg("ach.slime_hunter", "achievement.slime_hunter.name", "achievement.slime_hunter.desc",
        "/monster/greenslime_down_1", COMBAT, "ach.first_blood", 1, 0);
    reg("ach.red_menace", "achievement.red_menace.name", "achievement.red_menace.desc",
        "/monster/redslime_down_1", COMBAT, "ach.first_blood", 1, 1);
    reg("ach.bat_slayer", "achievement.bat_slayer.name", "achievement.bat_slayer.desc",
        "/monster/bat_down_1", COMBAT, "ach.first_blood", 1, 2);
    reg("ach.orc_crusher", "achievement.orc_crusher.name", "achievement.orc_crusher.desc",
        "/monster/orc_down_1", COMBAT, "ach.first_blood", 1, 3);
    reg("ach.monster_expert", "achievement.monster_expert.name", "achievement.monster_expert.desc",
        "/objects/sword_normal", COMBAT, "ach.slime_hunter", 2, 1);
    reg("ach.lord_slayer", "achievement.lord_slayer.name", "achievement.lord_slayer.desc",
        "/monster/skeletonlord_down_1", COMBAT, "ach.monster_expert", 3, 1);

    // ── COLLECTION (5) ──
    reg("ach.first_loot", "achievement.first_loot.name", "achievement.first_loot.desc",
        "/objects/chest", COLLECTION, null, 0, 0);
    reg("ach.armed", "achievement.armed.name", "achievement.armed.desc",
        "/objects/sword_normal", COLLECTION, "ach.first_loot", 1, 0);
    reg("ach.shielded", "achievement.shielded.name", "achievement.shielded.desc",
        "/objects/shield_wood", COLLECTION, "ach.first_loot", 1, 1);
    reg("ach.key_master", "achievement.key_master.name", "achievement.key_master.desc",
        "/objects/key", COLLECTION, "ach.first_loot", 1, 2);
    reg("ach.light_bringer", "achievement.light_bringer.name", "achievement.light_bringer.desc",
        "/objects/lantern", COLLECTION, "ach.first_loot", 1, 3);

    // ── EXPLORATION (4) ──
    reg("ach.indoor_visit", "achievement.indoor_visit.name", "achievement.indoor_visit.desc",
        "/objects/door", EXPLORATION, null, 0, 0);
    reg("ach.dungeon_delver", "achievement.dungeon_delver.name", "achievement.dungeon_delver.desc",
        "/objects/key", EXPLORATION, "ach.indoor_visit", 1, 0);
    reg("ach.boss_arena", "achievement.boss_arena.name", "achievement.boss_arena.desc",
        "/objects/door", EXPLORATION, "ach.dungeon_delver", 2, 0);
    reg("ach.world_explorer", "achievement.world_explorer.name", "achievement.world_explorer.desc",
        "/objects/lantern", EXPLORATION, "ach.boss_arena", 3, 0);

    // ── PROGRESSION (4) ──
    reg("ach.level_5", "achievement.level_5.name", "achievement.level_5.desc",
        "/player/heart_full", PROGRESSION, null, 0, 0);
    reg("ach.level_10", "achievement.level_10.name", "achievement.level_10.desc",
        "/player/heart_full", PROGRESSION, "ach.level_5", 1, 0);
    reg("ach.level_15", "achievement.level_15.name", "achievement.level_15.desc",
        "/player/heart_full", PROGRESSION, "ach.level_10", 2, 0);
    reg("ach.rich", "achievement.rich.name", "achievement.rich.desc",
        "/objects/coin_bronze", PROGRESSION, null, 0, 1);

    // ── PETS (3) ──
    reg("ach.first_egg", "achievement.first_egg.name", "achievement.first_egg.desc",
        "/objects/yellow_snail_egg", PETS, null, 0, 0);
    reg("ach.pet_hatched", "achievement.pet_hatched.name", "achievement.pet_hatched.desc",
        "/pets/yellowsnail_down_1", PETS, "ach.first_egg", 1, 0);
    reg("ach.two_pets", "achievement.two_pets.name", "achievement.two_pets.desc",
        "/pets/greensnail_down_1", PETS, "ach.pet_hatched", 2, 0);

    // ── BESTIARY (3) ──
    reg("ach.first_discovery", "achievement.first_discovery.name", "achievement.first_discovery.desc",
        "/monster/greenslime_down_1", BESTIARY, null, 0, 0);
    reg("ach.first_complete", "achievement.first_complete.name", "achievement.first_complete.desc",
        "/monster/bat_down_1", BESTIARY, "ach.first_discovery", 1, 0);
    reg("ach.bestiary_master", "achievement.bestiary_master.name", "achievement.bestiary_master.desc",
        "/monster/skeletonlord_down_1", BESTIARY, "ach.first_complete", 2, 0);

    // ── SPECIAL (2) ──
    reg("ach.pyromancer", "achievement.pyromancer.name", "achievement.pyromancer.desc",
        "/projectile/fireball_down_1", SPECIAL, null, 0, 0);
    reg("ach.lumberjack", "achievement.lumberjack.name", "achievement.lumberjack.desc",
        "/objects/axe", SPECIAL, null, 0, 1);
  }

  private AchievementRegistry() {
  }

  private static void reg(String id, String nameKey, String descKey,
      String iconSprite, AchievementCategory cat, String prereq, int gx, int gy) {
    DEFS.put(id, new AchievementDef(id, nameKey, descKey, iconSprite, cat, prereq, gx, gy));
  }

  public static AchievementDef getById(String id) {
    return DEFS.get(id);
  }

  public static List<AchievementDef> getByCategory(AchievementCategory category) {
    List<AchievementDef> result = new ArrayList<>();
    for (AchievementDef def : DEFS.values()) {
      if (def.category() == category) {
        result.add(def);
      }
    }
    return result;
  }

  public static List<AchievementDef> getAll() {
    return List.copyOf(DEFS.values());
  }

  public static AchievementCategory[] getCategories() {
    return AchievementCategory.values();
  }

  public static Map<String, AchievementDef> getAllMap() {
    return Collections.unmodifiableMap(DEFS);
  }
}

package skill;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import entity.Player;

/**
 * Gerencia pontos de habilidade e as 25 habilidades em 5 ramos.
 *
 * <h3>Design de efeitos:</h3>
 * <ul>
 *   <li>Efeitos em stats <em>armazenados</em> (maxLife, maxMana, strength, dexterity)
 *       são aplicados diretamente ao Player no desbloqueio e persistem no save normal.</li>
 *   <li>Efeitos em stats <em>recalculados</em> (attack, defense, speed, bowCooldown)
 *       são derivados em tempo real pelos métodos {@code getSkill*Bonus()}, sem
 *       duplicação no save — basta salvar o conjunto de IDs desbloqueados.</li>
 * </ul>
 */
public class SkillTree {

  // ── IDs — RAMO 1: FORÇA ─────────────────────────────────────────────────
  public static final String STRENGTH_1   = "skill.strength1";
  public static final String STRENGTH_2   = "skill.strength2";
  public static final String STRENGTH_3   = "skill.strength3";
  public static final String BATTLE_FURY  = "skill.battleFury";
  public static final String BRUTAL_BLOW  = "skill.brutalBlow";

  // ── IDs — RAMO 2: RESISTÊNCIA ───────────────────────────────────────────
  public static final String VITALITY_1     = "skill.vitality1";
  public static final String VITALITY_2     = "skill.vitality2";
  public static final String VITALITY_3     = "skill.vitality3";
  public static final String NATURAL_ARMOR  = "skill.naturalArmor";
  public static final String STONE_HIDE     = "skill.stoneHide";

  // ── IDs — RAMO 3: ARCANA ────────────────────────────────────────────────
  public static final String MANA_1          = "skill.mana1";
  public static final String MANA_2          = "skill.mana2";
  public static final String MANA_3          = "skill.mana3";
  public static final String ARCANE_POWER    = "skill.arcanePower";
  public static final String MAGIC_MASTERY   = "skill.magicMastery";

  // ── IDs — RAMO 4: AGILIDADE ─────────────────────────────────────────────
  public static final String AGILITY_1    = "skill.agility1";
  public static final String AGILITY_2    = "skill.agility2";
  public static final String DEXTERITY_1  = "skill.dexterity1";
  public static final String DEXTERITY_2  = "skill.dexterity2";
  public static final String RESILIENCE   = "skill.resilience";

  // ── IDs — RAMO 5: CAÇADOR ───────────────────────────────────────────────
  public static final String HAWK_EYE       = "skill.hawkEye";
  public static final String SWIFT_SHOT     = "skill.swiftShot";
  public static final String ELITE_SHOOTER  = "skill.eliteShooter";
  public static final String PIERCING_ARROW = "skill.piercingArrow";
  public static final String EAGLE_SHOT     = "skill.eagleShot";

  // ── Estrutura dos ramos (ordem de exibição: topo → base) ────────────────
  public static final List<List<String>> BRANCHES = List.of(
      List.of(STRENGTH_1, STRENGTH_2, STRENGTH_3, BATTLE_FURY, BRUTAL_BLOW),
      List.of(VITALITY_1, VITALITY_2, VITALITY_3, NATURAL_ARMOR, STONE_HIDE),
      List.of(MANA_1, MANA_2, MANA_3, ARCANE_POWER, MAGIC_MASTERY),
      List.of(AGILITY_1, AGILITY_2, DEXTERITY_1, DEXTERITY_2, RESILIENCE),
      List.of(HAWK_EYE, SWIFT_SHOT, ELITE_SHOOTER, PIERCING_ARROW, EAGLE_SHOT)
  );

  public static final List<String> BRANCH_KEYS = List.of(
      "ui.skillTree.branch.might",
      "ui.skillTree.branch.endurance",
      "ui.skillTree.branch.arcana",
      "ui.skillTree.branch.agility",
      "ui.skillTree.branch.hunter"
  );

  // ── Estado ───────────────────────────────────────────────────────────────
  private int skillPoints = 0;
  private final Set<String> unlockedSkills = new HashSet<>();
  private final Map<String, Skill> catalogue = new LinkedHashMap<>();

  public SkillTree() {
    buildCatalogue();
  }

  // ── Construção do catálogo ────────────────────────────────────────────────

  private void buildCatalogue() {
    buildMightBranch();
    buildEnduranceBranch();
    buildArcanaBranch();
    buildAgilityBranch();
    buildHunterBranch();
  }

  /** RAMO 1 — Força: eleva STR diretamente e concede bônus de ataque plano. */
  private void buildMightBranch() {
    register(new Skill(STRENGTH_1, "skill.strength1", 1, List.of(),
        p -> p.setStrength(p.getStrength() + 1)));

    register(new Skill(STRENGTH_2, "skill.strength2", 2, List.of(STRENGTH_1),
        p -> p.setStrength(p.getStrength() + 2)));

    register(new Skill(STRENGTH_3, "skill.strength3", 3, List.of(STRENGTH_2),
        p -> p.setStrength(p.getStrength() + 3)));

    register(new Skill(BATTLE_FURY, "skill.battleFury", 2, List.of(STRENGTH_1),
        p -> { /* bônus via getSkillAttackBonus() */ }));

    register(new Skill(BRUTAL_BLOW, "skill.brutalBlow", 3, List.of(BATTLE_FURY),
        p -> { /* bônus via getSkillAttackBonus() */ }));
  }

  /** RAMO 2 — Resistência: eleva maxLife e adiciona defesa plana. */
  private void buildEnduranceBranch() {
    register(new Skill(VITALITY_1, "skill.vitality1", 1, List.of(),
        p -> { p.setMaxLife(p.getMaxLife() + 8); p.setLife(p.getMaxLife()); }));

    register(new Skill(VITALITY_2, "skill.vitality2", 2, List.of(VITALITY_1),
        p -> { p.setMaxLife(p.getMaxLife() + 15); p.setLife(p.getMaxLife()); }));

    register(new Skill(VITALITY_3, "skill.vitality3", 3, List.of(VITALITY_2),
        p -> { p.setMaxLife(p.getMaxLife() + 25); p.setLife(p.getMaxLife()); }));

    register(new Skill(NATURAL_ARMOR, "skill.naturalArmor", 2, List.of(VITALITY_1),
        p -> { /* bônus via getSkillDefenseBonus() */ }));

    register(new Skill(STONE_HIDE, "skill.stoneHide", 3, List.of(NATURAL_ARMOR),
        p -> { /* bônus via getSkillDefenseBonus() */ }));
  }

  /** RAMO 3 — Arcana: eleva maxMana e concede ataque mágico plano. */
  private void buildArcanaBranch() {
    register(new Skill(MANA_1, "skill.mana1", 1, List.of(),
        p -> { p.setMaxMana(p.getMaxMana() + 4); p.setMana(p.getMaxMana()); }));

    register(new Skill(MANA_2, "skill.mana2", 2, List.of(MANA_1),
        p -> { p.setMaxMana(p.getMaxMana() + 8); p.setMana(p.getMaxMana()); }));

    register(new Skill(MANA_3, "skill.mana3", 3, List.of(MANA_2),
        p -> { p.setMaxMana(p.getMaxMana() + 14); p.setMana(p.getMaxMana()); }));

    register(new Skill(ARCANE_POWER, "skill.arcanePower", 2, List.of(MANA_1),
        p -> { /* bônus via getSkillAttackBonus() */ }));

    register(new Skill(MAGIC_MASTERY, "skill.magicMastery", 3, List.of(ARCANE_POWER),
        p -> { /* bônus via getSkillAttackBonus() */ }));
  }

  /** RAMO 4 — Agilidade: eleva velocidade e destreza. */
  private void buildAgilityBranch() {
    register(new Skill(AGILITY_1, "skill.agility1", 1, List.of(),
        p -> { /* bônus via getSkillSpeedBonus() */ }));

    register(new Skill(AGILITY_2, "skill.agility2", 2, List.of(AGILITY_1),
        p -> { /* bônus via getSkillSpeedBonus() */ }));

    register(new Skill(DEXTERITY_1, "skill.dexterity1", 1, List.of(),
        p -> p.setDexterity(p.getDexterity() + 1)));

    register(new Skill(DEXTERITY_2, "skill.dexterity2", 2, List.of(DEXTERITY_1),
        p -> p.setDexterity(p.getDexterity() + 2)));

    register(new Skill(RESILIENCE, "skill.resilience", 3, List.of(DEXTERITY_2),
        p -> p.setDexterity(p.getDexterity() + 3)));
  }

  /** RAMO 5 — Caçador: potencializa ataque e velocidade de arcos. */
  private void buildHunterBranch() {
    register(new Skill(HAWK_EYE, "skill.hawkEye", 1, List.of(),
        p -> { /* bônus via getSkillAttackBonus() */ }));

    register(new Skill(SWIFT_SHOT, "skill.swiftShot", 2, List.of(HAWK_EYE),
        p -> { /* redução via getSkillBowCooldownReduction() */ }));

    register(new Skill(ELITE_SHOOTER, "skill.eliteShooter", 3, List.of(SWIFT_SHOT),
        p -> { /* bônus via getSkillAttackBonus() */ }));

    register(new Skill(PIERCING_ARROW, "skill.piercingArrow", 2, List.of(HAWK_EYE),
        p -> { /* bônus via getSkillAttackBonus() */ }));

    register(new Skill(EAGLE_SHOT, "skill.eagleShot", 3, List.of(PIERCING_ARROW),
        p -> { /* redução via getSkillBowCooldownReduction() */ }));
  }

  private void register(Skill skill) {
    catalogue.put(skill.getId(), skill);
  }

  // ── Consulta ──────────────────────────────────────────────────────────────

  public Skill getSkill(String id) {
    return catalogue.get(id);
  }

  public boolean isUnlocked(String id) {
    return unlockedSkills.contains(id);
  }

  public boolean prerequisitesMet(String id) {
    Skill skill = catalogue.get(id);
    if (skill == null) return false;
    for (String prereqId : skill.getPrerequisiteIds()) {
      if (!unlockedSkills.contains(prereqId)) return false;
    }
    return true;
  }

  /** Tenta desbloquear a habilidade. Retorna true se bem-sucedido. */
  public boolean tryUnlock(String id, Player player) {
    if (isUnlocked(id)) return false;
    Skill skill = catalogue.get(id);
    if (skill == null) return false;
    if (!prerequisitesMet(id)) return false;
    if (skillPoints < skill.getCost()) return false;

    skillPoints -= skill.getCost();
    unlockedSkills.add(id);
    skill.applyEffect(player);

    player.recalculateAttackStats();
    player.setDefense(player.getDefense());
    player.recalculateSpeedFromEquipment();
    return true;
  }

  // ── Bônus recalculados ────────────────────────────────────────────────────

  /** Bônus plano de ataque somado de todos os ramos que contribuem. */
  public int getSkillAttackBonus() {
    int bonus = 0;
    // Ramo Força
    if (isUnlocked(BATTLE_FURY))  bonus += 5;
    if (isUnlocked(BRUTAL_BLOW))  bonus += 10;
    // Ramo Arcana
    if (isUnlocked(ARCANE_POWER))  bonus += 4;
    if (isUnlocked(MAGIC_MASTERY)) bonus += 8;
    // Ramo Caçador
    if (isUnlocked(HAWK_EYE))       bonus += 3;
    if (isUnlocked(ELITE_SHOOTER))  bonus += 6;
    if (isUnlocked(PIERCING_ARROW)) bonus += 3;
    return bonus;
  }

  /** Bônus plano de defesa (adicionado diretamente ao valor final de defesa). */
  public int getSkillDefenseBonus() {
    int bonus = 0;
    if (isUnlocked(NATURAL_ARMOR)) bonus += 2;
    if (isUnlocked(STONE_HIDE))    bonus += 4;
    return bonus;
  }

  /** Bônus de velocidade de movimento. */
  public int getSkillSpeedBonus() {
    int bonus = 0;
    if (isUnlocked(AGILITY_1)) bonus += 1;
    if (isUnlocked(AGILITY_2)) bonus += 1;
    return bonus;
  }

  /**
   * Redução de frames no cooldown entre disparos de arco.
   * Tiro Veloz: -10f; Tiro de Águia: -10f adicionais.
   */
  public int getSkillBowCooldownReduction() {
    int reduction = 0;
    if (isUnlocked(SWIFT_SHOT)) reduction += 10;
    if (isUnlocked(EAGLE_SHOT)) reduction += 10;
    return reduction;
  }

  // ── Pontos ────────────────────────────────────────────────────────────────

  public int getSkillPoints() { return skillPoints; }

  public void setSkillPoints(int skillPoints) {
    this.skillPoints = Math.max(0, skillPoints);
  }

  public void addSkillPoints(int amount) { skillPoints += amount; }

  // ── Save / Load ───────────────────────────────────────────────────────────

  public Set<String> getUnlockedSkillIds() {
    return new HashSet<>(unlockedSkills);
  }

  /** Restaura estado a partir do save sem reaplicar efeitos de maxLife/maxMana/str/dex. */
  public void restoreFromSave(int points, Set<String> ids) {
    skillPoints = points;
    unlockedSkills.clear();
    if (ids != null) unlockedSkills.addAll(ids);
  }
}

package skill;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import entity.Player;

/**
 * Gerencia pontos de habilidade e habilidades desbloqueadas.
 * <p>
 * Bônus sobre stats recalculados (ataque, defesa, velocidade) são derivados
 * em tempo real do conjunto de habilidades desbloqueadas, nunca duplicados no save.
 * Bônus sobre stats armazenados (maxLife, maxMana) são aplicados diretamente
 * no Player no momento do desbloqueio e persistem via save normal.
 * </p>
 */
public class SkillTree {

  // ── Ramos ────────────────────────────────────────────────────────────────
  public static final String BRANCH_COMBAT  = "combat";
  public static final String BRANCH_DEFENSE = "defense";
  public static final String BRANCH_UTILITY = "utility";

  // ── IDs das habilidades ──────────────────────────────────────────────────
  public static final String STRENGTH_1  = "skill.strength1";
  public static final String STRENGTH_2  = "skill.strength2";
  public static final String ATTACK_1    = "skill.attack1";
  public static final String VITALITY_1  = "skill.vitality1";
  public static final String VITALITY_2  = "skill.vitality2";
  public static final String RESILIENCE  = "skill.resilience";
  public static final String AGILITY_1   = "skill.agility1";
  public static final String MANA_1      = "skill.mana1";
  public static final String DEXTERITY_1 = "skill.dexterity1";

  // ── Estado ───────────────────────────────────────────────────────────────
  private int skillPoints = 0;
  private final Set<String> unlockedSkills = new HashSet<>();

  // ── Catálogo (ordem de exibição por ramo) ────────────────────────────────
  private final Map<String, Skill> catalogue = new LinkedHashMap<>();

  /** Ramos na ordem de exibição na UI, cada um com os IDs em ordem de topo → fundo. */
  public static final List<List<String>> BRANCHES = List.of(
      List.of(STRENGTH_1, STRENGTH_2, ATTACK_1),   // BRANCH_COMBAT
      List.of(VITALITY_1, VITALITY_2, RESILIENCE),  // BRANCH_DEFENSE
      List.of(AGILITY_1, DEXTERITY_1, MANA_1)       // BRANCH_UTILITY
  );

  public static final List<String> BRANCH_KEYS = List.of(
      "ui.skillTree.branch.combat",
      "ui.skillTree.branch.defense",
      "ui.skillTree.branch.utility"
  );

  // ── Construtor ────────────────────────────────────────────────────────────
  public SkillTree() {
    buildCatalogue();
  }

  private void buildCatalogue() {
    // ── Combate ──────────────────────────────────────────────────────────
    register(new Skill(
        STRENGTH_1, "skill.strength1",
        1, List.of(),
        p -> p.setStrength(p.getStrength() + 1)));

    register(new Skill(
        STRENGTH_2, "skill.strength2",
        2, List.of(STRENGTH_1),
        p -> p.setStrength(p.getStrength() + 2)));

    register(new Skill(
        ATTACK_1, "skill.attack1",
        2, List.of(STRENGTH_1),
        p -> { /* efeito via getSkillAttackBonus() */ }));

    // ── Defesa ───────────────────────────────────────────────────────────
    register(new Skill(
        VITALITY_1, "skill.vitality1",
        1, List.of(),
        p -> {
          p.setMaxLife(p.getMaxLife() + 5);
          p.setLife(p.getMaxLife());
        }));

    register(new Skill(
        VITALITY_2, "skill.vitality2",
        2, List.of(VITALITY_1),
        p -> {
          p.setMaxLife(p.getMaxLife() + 10);
          p.setLife(p.getMaxLife());
        }));

    register(new Skill(
        RESILIENCE, "skill.resilience",
        2, List.of(VITALITY_1),
        p -> { /* efeito via getSkillDexterityBonus() */ }));

    // ── Utilidade ────────────────────────────────────────────────────────
    register(new Skill(
        AGILITY_1, "skill.agility1",
        1, List.of(),
        p -> { /* efeito via getSkillSpeedBonus() */ }));

    register(new Skill(
        DEXTERITY_1, "skill.dexterity1",
        1, List.of(AGILITY_1),
        p -> { /* efeito via getSkillDexterityBonus() */ }));

    register(new Skill(
        MANA_1, "skill.mana1",
        1, List.of(),
        p -> {
          p.setMaxMana(p.getMaxMana() + 3);
          p.setMana(p.getMaxMana());
        }));
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

  /**
   * Tenta desbloquear a habilidade. Retorna true se bem-sucedido.
   */
  public boolean tryUnlock(String id, Player player) {
    if (isUnlocked(id)) return false;
    Skill skill = catalogue.get(id);
    if (skill == null) return false;
    if (!prerequisitesMet(id)) return false;
    if (skillPoints < skill.getCost()) return false;

    skillPoints -= skill.getCost();
    unlockedSkills.add(id);
    skill.applyEffect(player);

    // Recalcular stats derivados
    player.recalculateAttackStats();
    player.setDefense(player.getDefense());
    player.recalculateSpeedFromEquipment();

    return true;
  }

  // ── Bônus derivados (recalculados a partir do conjunto desbloqueado) ──────

  /** Bônus de força acumulado por habilidades (usado em recalculateAttackStats). */
  public int getSkillStrengthBonus() {
    // A habilidade Força I e II aplicam diretamente ao setStrength(),
    // então não contabilizamos aqui. Apenas flat attack bonus.
    return 0;
  }

  /** Bônus fixo de ataque (não multiplicado pela arma). */
  public int getSkillAttackBonus() {
    return isUnlocked(ATTACK_1) ? 3 : 0;
  }

  /** Bônus de destreza acumulado por habilidades (usado em getDefense). */
  public int getSkillDexterityBonus() {
    int bonus = 0;
    if (isUnlocked(RESILIENCE))  bonus += 1;
    if (isUnlocked(DEXTERITY_1)) bonus += 1;
    return bonus;
  }

  /** Bônus de velocidade de movimento. */
  public int getSkillSpeedBonus() {
    return isUnlocked(AGILITY_1) ? 1 : 0;
  }

  // ── Pontos ────────────────────────────────────────────────────────────────

  public int getSkillPoints() {
    return skillPoints;
  }

  public void setSkillPoints(int skillPoints) {
    this.skillPoints = Math.max(0, skillPoints);
  }

  public void addSkillPoints(int amount) {
    skillPoints += amount;
  }

  // ── Save / Load ───────────────────────────────────────────────────────────

  public Set<String> getUnlockedSkillIds() {
    return new HashSet<>(unlockedSkills);
  }

  /**
   * Restaura o estado a partir do save (sem reaplicar efeitos em maxLife/maxMana,
   * pois esses já estão baked no save normal do player).
   */
  public void restoreFromSave(int points, Set<String> ids) {
    skillPoints = points;
    unlockedSkills.clear();
    if (ids != null) {
      unlockedSkills.addAll(ids);
    }
  }
}

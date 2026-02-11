package bestiary;

import java.util.HashSet;
import java.util.Set;

/**
 * Progresso mutável do jogador para UMA criatura do Bestiário.
 * Instância por criatura; gerenciado pelo {@link BestiaryService}.
 */
public final class BestiaryProgress {

  private int kills;
  private final Set<String> revealedVeryRareItems;

  public BestiaryProgress() {
    this.kills = 0;
    this.revealedVeryRareItems = new HashSet<>();
  }

  public BestiaryProgress(int kills, Set<String> revealedVeryRareItems) {
    this.kills = Math.max(0, kills);
    this.revealedVeryRareItems = new HashSet<>(revealedVeryRareItems);
  }

  public int getKills() {
    return kills;
  }

  public void incrementKills() {
    kills++;
  }

  /**
   * Calcula o estágio atual baseado nos kills e na definição da criatura.
   */
  public BestiaryStage getStage(BestiaryDifficulty difficulty, BestiaryRarity rarity) {
    return KillThresholds.stageForKills(kills, difficulty, rarity);
  }

  /**
   * Retorna as kills necessárias para atingir o próximo estágio.
   * Retorna 0 se já completou.
   */
  public int getKillsForNextStage(BestiaryDifficulty difficulty, BestiaryRarity rarity) {
    int[] thresholds = KillThresholds.getThresholds(difficulty, rarity);
    BestiaryStage stage = getStage(difficulty, rarity);

    return switch (stage) {
      case LOCKED, DISCOVERED -> thresholds[0];
      case STAGE_1 -> thresholds[1];
      case STAGE_2 -> thresholds[2];
      case COMPLETED -> 0;
    };
  }

  // --- Very Rare revelation ---

  public boolean isVeryRareRevealed(String itemId) {
    return revealedVeryRareItems.contains(itemId);
  }

  public void revealVeryRareItem(String itemId) {
    revealedVeryRareItems.add(itemId);
  }

  public Set<String> getRevealedVeryRareItems() {
    return Set.copyOf(revealedVeryRareItems);
  }
}

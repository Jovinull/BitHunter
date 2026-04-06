package skill;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import entity.Player;

/**
 * Define imutavelmente uma habilidade da Árvore de Habilidades.
 * O efeito é aplicado uma única vez ao desbloquear.
 */
public final class Skill {

  private final String id;
  private final String translationKey;
  private final int cost;
  private final List<String> prerequisiteIds;
  private final Consumer<Player> onUnlock;

  public Skill(
      String id,
      String translationKey,
      int cost,
      List<String> prerequisiteIds,
      Consumer<Player> onUnlock) {
    this.id = Objects.requireNonNull(id, "id");
    this.translationKey = Objects.requireNonNull(translationKey, "translationKey");
    this.cost = cost;
    this.prerequisiteIds = List.copyOf(prerequisiteIds);
    this.onUnlock = Objects.requireNonNull(onUnlock, "onUnlock");
  }

  public String getId() {
    return id;
  }

  public String getTranslationKey() {
    return translationKey;
  }

  public int getCost() {
    return cost;
  }

  public List<String> getPrerequisiteIds() {
    return prerequisiteIds;
  }

  /** Aplica o efeito passivo ao Player (chamado apenas uma vez, no desbloqueio). */
  public void applyEffect(Player player) {
    onUnlock.accept(player);
  }
}

package pets;

import entity.Player;
import main.GamePanel;

/**
 * Controle global de incubação de ovos.
 *
 * Por simplicidade, só permite 1 ovo sendo chocado por vez.
 * O tempo só corre quando o jogo está em PLAY_STATE.
 */
public class PetIncubator {

  private final GamePanel gp;

  private boolean active;
  private String petId;
  private int remainingTicks;
  private String eggName;

  public PetIncubator(GamePanel gp) {
    this.gp = gp;
  }

  public boolean hasActiveEgg() {
    return active;
  }

  public int getRemainingTicks() {
    return remainingTicks;
  }

  public String getPetId() {
    return petId;
  }

  public String getEggName() {
    return eggName;
  }

  public void startIncubation(PetEgg egg) {
    if (egg == null || active) {
      return;
    }

    active = true;
    petId = egg.getPetId();
    remainingTicks = egg.getHatchTimeTicks();
    eggName = egg.getDisplayName();
  }

  public void update() {
    if (!active) {
      return;
    }

    // pausa em menus / cutscenes
    if (gp.getGameState() != GamePanel.PLAY_STATE) {
      return;
    }

    if (remainingTicks > 0) {
      remainingTicks--;
      if (remainingTicks == 0) {
        finishIncubation();
      }
    }
  }

  /**
   * Finaliza a incubação:
   * - adiciona o pet à coleção do jogador (se ainda não tiver)
   * - NÃO equipa automaticamente o pet; o jogador deve ir ao Pet Yard para
   * equipar
   */
  private void finishIncubation() {
    active = false;

    Player player = gp.getPlayer();
    player.addOwnedPet(petId);

    String petName = gp.getLanguageManager().text(petId + ".name");
    String msgTemplate = gp.getLanguageManager().text("ui.petYard.hatchCompleted");
    String msg = String.format(msgTemplate, petName);
    gp.getGameUi().addMessage(msg);
  }

  public void reset() {
    active = false;
    petId = null;
    remainingTicks = 0;
    eggName = null;
  }

  /**
   * Restaura o estado da incubação a partir de dados de save.
   * Se os dados forem inválidos, o incubador é resetado.
   */
  public void restoreFromSave(
      boolean active,
      String petId,
      int remainingTicks,
      String eggName) {

    if (!active || petId == null || petId.isBlank() || remainingTicks <= 0) {
      reset();
      return;
    }

    this.active = true;
    this.petId = petId;
    this.remainingTicks = remainingTicks;
    this.eggName = eggName;
  }
}

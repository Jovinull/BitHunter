package pets;

import entity.Entity;
import main.GamePanel;

/**
 * Item de inventário que representa um ovo de pet.
 *
 * Cada ovo sabe:
 * - qual pet nascerá (petId)
 * - custo em moedas para chocar
 * - tempo em "ticks" de jogo para chocar (usando 60 FPS)
 */
public abstract class PetEgg extends Entity {

  private final String petId;
  private final int hatchTimeTicks;
  private final int hatchCost;

  protected PetEgg(GamePanel gp, String petId, int hatchTimeTicks, int hatchCost) {
    super(gp);
    this.petId = petId;
    this.hatchTimeTicks = hatchTimeTicks;
    this.hatchCost = hatchCost;

    setStackable(false);
  }

  public String getPetId() {
    return petId;
  }

  public int getHatchTimeTicks() {
    return hatchTimeTicks;
  }

  public int getHatchCost() {
    return hatchCost;
  }
}

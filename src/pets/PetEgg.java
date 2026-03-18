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
// this-escape: chama setStackable()/setType() de Entity no construtor.
// Seguro pois nenhuma subclasse sobrescreve esses setters.
@SuppressWarnings("this-escape")
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
    setType(TYPE_EGG);
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

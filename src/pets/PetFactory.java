package pets;

import java.awt.image.BufferedImage;

import entity.Player;
import main.GamePanel;

/**
 * Fábrica/registry de pets do jogo.
 *
 * Centraliza:
 * - IDs estáveis de pets
 * - criação das instâncias (uso no Player)
 * - acesso a ícones para UI
 */
public final class PetFactory {

  private PetFactory() {
    // utilitário estático
  }

  // IDs estáveis – use sempre essas constantes
  public static final String PET_YELLOW_SNAIL = "pet.yellowSnail";

  public static PetEntity createPet(String petId, GamePanel gp, Player owner) {
    if (petId == null || gp == null || owner == null) {
      return null;
    }

    return switch (petId) {
      case PET_YELLOW_SNAIL -> new YellowSnail(gp, owner);
      default -> null;
    };
  }

  /**
   * Ícone 48x48 (tile) para listagens de pets (Pet Yard, etc).
   * Usa o frame "down1" do pet.
   */
  public static BufferedImage getPetIcon(String petId, GamePanel gp, Player owner) {
    PetEntity pet = createPet(petId, gp, owner);
    if (pet == null) {
      return null;
    }
    pet.getImage(); // garante sprites carregados
    return pet.getDown1();
  }
}

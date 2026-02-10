package pets;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

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

  private static final Map<String, BufferedImage> ICON_CACHE = new HashMap<>();

  private PetFactory() {
    // utilitário estático
  }

  /**
   * IDs estáveis – use sempre essas constantes.
   *
   * Elas apontam para as constantes ID de cada classe de Pet,
   * evitando duplicar as strings em vários lugares.
   */
  public static final String PET_YELLOW_SNAIL = YellowSnail.ID;
  public static final String PET_GREEN_SNAIL = GreenSnail.ID;

  public static PetEntity createPet(String petId, GamePanel gp, Player owner) {
    if (petId == null || gp == null || owner == null) {
      return null;
    }

    return switch (petId) {
      case PET_YELLOW_SNAIL -> new YellowSnail(gp, owner);
      case PET_GREEN_SNAIL -> new GreenSnail(gp, owner);
      default -> null;
    };
  }

  /**
   * Ícone 48x48 (tile) para listagens de pets (Pet Yard, etc).
   * Usa o frame "down1" do pet.
   */
  public static BufferedImage getPetIcon(String petId, GamePanel gp, Player owner) {
    if (petId == null || gp == null || owner == null) {
      return null;
    }
    BufferedImage cached = ICON_CACHE.get(petId);
    if (cached != null) {
      return cached;
    }
    PetEntity pet = createPet(petId, gp, owner);
    if (pet == null) {
      return null;
    }
    pet.getImage(); // garante sprites carregados
    BufferedImage icon = pet.getDown1();
    if (icon != null) {
      ICON_CACHE.put(petId, icon);
    }
    return icon;
  }
}

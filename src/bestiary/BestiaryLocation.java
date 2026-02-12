package bestiary;

/**
 * Localização onde uma criatura pode aparecer.
 *
 * @param mapId      índice do mapa (0=World, 1=House, 2=Dungeon, 3=BossRoom)
 * @param regionName nome legível da região
 */
public record BestiaryLocation(int mapId, String regionName) {
}

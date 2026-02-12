package tile;

/**
 * Representa um marcador posicional no mapa/mini mapa.
 *
 * <p>Objeto imutável e leve — criado e descartado a cada
 * ciclo de atualização do {@link MapMarkerProvider}.</p>
 *
 * @param type   categoria visual do marcador
 * @param worldX posição X em pixels do mundo
 * @param worldY posição Y em pixels do mundo
 */
public record MapMarker(MapMarkerType type, int worldX, int worldY) {
}

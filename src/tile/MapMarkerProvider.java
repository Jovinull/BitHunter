package tile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import entity.Entity;
import main.GamePanel;
import tile_interactive.InteractiveTile;

/**
 * Fonte única de marcadores para o mapa e mini mapa.
 *
 * <p>Coleta dados das arrays de entidades do {@link GamePanel}
 * (NPCs, monstros, objetos, tiles interativos) e os converte
 * em {@link MapMarker}s imutáveis.</p>
 *
 * <h3>Cache / Performance</h3>
 * <p>Os marcadores são cacheados e só re-coletados quando:</p>
 * <ul>
 *   <li>O mapa muda ({@code mapId} diferente do cache).</li>
 *   <li>O TTL expira ({@value #CACHE_TTL_MS}ms).</li>
 *   <li>{@link #invalidateCache()} é chamado explicitamente.</li>
 * </ul>
 */
public class MapMarkerProvider {

  /**
   * Tempo máximo entre coletas (ms).
   * ~180ms ≈ 11 frames a 60fps — bom equilíbrio entre
   * frescor dos dados e custo de coleta.
   */
  private static final long CACHE_TTL_MS = 180L;

  private final GamePanel gp;

  // --- Cache ---
  private List<MapMarker> cachedMarkers = Collections.emptyList();
  private int cachedMapId = -1;
  private long lastRefreshMs;

  public MapMarkerProvider(GamePanel gp) {
    this.gp = gp;
  }

  /**
   * Retorna os marcadores do mapa especificado, usando cache
   * quando possível.
   */
  public List<MapMarker> getMarkers(int mapId) {
    long now = System.currentTimeMillis();
    if (mapId == cachedMapId && (now - lastRefreshMs) < CACHE_TTL_MS) {
      return cachedMarkers;
    }
    cachedMarkers = collectMarkers(mapId);
    cachedMapId = mapId;
    lastRefreshMs = now;
    return cachedMarkers;
  }

  /** Força re-coleta na próxima chamada a {@link #getMarkers}. */
  public void invalidateCache() {
    lastRefreshMs = 0;
  }

  // ====================================================================
  // Coleta de marcadores
  // ====================================================================

  private List<MapMarker> collectMarkers(int mapId) {
    List<MapMarker> markers = new ArrayList<>();
    collectNpcs(mapId, markers);
    collectMonsters(mapId, markers);
    collectObjects(mapId, markers);
    collectInteractiveTiles(mapId, markers);
    return markers;
  }

  /** NPCs → marcador azul (CIRCLE). */
  private void collectNpcs(int mapId, List<MapMarker> out) {
    Entity[][] npcs = gp.getNpc();
    for (Entity npc : npcs[mapId]) {
      if (npc != null) {
        out.add(new MapMarker(MapMarkerType.NPC, npc.getWorldX(), npc.getWorldY()));
      }
    }
  }

  /**
   * Monstros vivos → MONSTER (vermelho) ou BOSS (vermelho+diamante).
   * Monstros mortos/morrendo são ignorados.
   */
  private void collectMonsters(int mapId, List<MapMarker> out) {
    Entity[][] monsters = gp.getMonster();
    for (Entity mon : monsters[mapId]) {
      if (mon != null && mon.isAlive() && !mon.isDying()) {
        MapMarkerType type = mon.isBoss() ? MapMarkerType.BOSS : MapMarkerType.MONSTER;
        out.add(new MapMarker(type, mon.getWorldX(), mon.getWorldY()));
      }
    }
  }

  /**
   * Objetos no mapa:
   * <ul>
   *   <li>{@code TYPE_OBSTACLE} (baús, portas, etc.) → INTERACTIVE</li>
   *   <li>Demais (consumíveis, equipamentos, pickups) → DROP</li>
   * </ul>
   */
  private void collectObjects(int mapId, List<MapMarker> out) {
    Entity[][] objects = gp.getObj();
    for (Entity obj : objects[mapId]) {
      if (obj == null) {
        continue;
      }
      if (obj.getType() == Entity.TYPE_OBSTACLE) {
        out.add(new MapMarker(MapMarkerType.INTERACTIVE, obj.getWorldX(), obj.getWorldY()));
      } else {
        out.add(new MapMarker(MapMarkerType.DROP, obj.getWorldX(), obj.getWorldY()));
      }
    }
  }

  /** Tiles interativos (paredes destrutíveis, placas, etc.) → INTERACTIVE. */
  private void collectInteractiveTiles(int mapId, List<MapMarker> out) {
    InteractiveTile[][] tiles = gp.getiTile();
    for (InteractiveTile tile : tiles[mapId]) {
      if (tile != null) {
        out.add(new MapMarker(MapMarkerType.INTERACTIVE, tile.getWorldX(), tile.getWorldY()));
      }
    }
  }
}

package tile;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import main.GamePanel;

/**
 * Gerencia a renderização do mini mapa e do mapa grande (tela cheia).
 *
 * <p>Agora inclui marcadores ({@link MapMarker}) para NPCs, monstros,
 * bosses, tiles interativos e drops, coletados pelo
 * {@link MapMarkerProvider} com cache de ~180ms.</p>
 *
 * <h3>Toggles de camadas (mapa grande)</h3>
 * <ul>
 *   <li>1 — NPCs</li>
 *   <li>2 — Monstros + Bosses</li>
 *   <li>3 — Interativos (baús, placas, paredes destrutíveis)</li>
 *   <li>4 — Drops (itens no chão)</li>
 * </ul>
 */
public class Map extends TileManager {

  // ====================================================================
  // Constantes de layout
  // ====================================================================

  /** Tamanho do mapa grande (pixels). */
  private static final int FULL_MAP_SIZE = 600;

  /** Tamanho do mini mapa (pixels). */
  private static final int MINI_MAP_SIZE = 300;

  /** Margem do mini mapa em relação à borda da tela. */
  private static final int MINI_MAP_MARGIN = 50;

  /** Tamanho do marcador no mini mapa (raio em pixels). */
  private static final int MINI_MARKER_RADIUS = 3;

  /** Tamanho do marcador no mapa grande (raio em pixels). */
  private static final int FULL_MARKER_RADIUS = 5;

  /** Tamanho do marcador de boss (raio em pixels, ambos os mapas). */
  private static final int BOSS_MARKER_RADIUS_MINI = 5;
  private static final int BOSS_MARKER_RADIUS_FULL = 8;

  /** Transparência do mini mapa. */
  private static final float MINI_MAP_ALPHA = 0.8f;

  /** Cor de fundo da legenda no mapa grande. */
  private static final Color LEGEND_BG = new Color(0, 0, 0, 180);

  /** Cor do contorno dos marcadores (para destaque). */
  private static final Color MARKER_OUTLINE = new Color(0, 0, 0, 160);

  // ====================================================================
  // Estado
  // ====================================================================

  private BufferedImage[] worldMap;
  private boolean miniMapOn = false;

  /** Provider centralizado de marcadores (com cache). */
  private final MapMarkerProvider markerProvider;

  /**
   * Camadas de marcadores visíveis.
   * Todas ligadas por padrão; toggleáveis com teclas 1-4 no MAP_STATE.
   */
  private final Set<MapMarkerType> visibleLayers = EnumSet.allOf(MapMarkerType.class);

  // ====================================================================
  // Construtor
  // ====================================================================

  public Map(GamePanel gp) {
    super(gp);
    this.markerProvider = new MapMarkerProvider(gp);
    createWorldMap();
  }

  // ====================================================================
  // Getters / Setters
  // ====================================================================

  public boolean isMiniMapOn() {
    return miniMapOn;
  }

  public void setMiniMapOn(boolean miniMapOn) {
    this.miniMapOn = miniMapOn;
  }

  public BufferedImage[] getWorldMap() {
    return worldMap;
  }

  // ====================================================================
  // Toggle de camadas
  // ====================================================================

  /** Alterna visibilidade de uma camada de marcador. */
  public void toggleLayer(MapMarkerType type) {
    if (visibleLayers.contains(type)) {
      visibleLayers.remove(type);
    } else {
      visibleLayers.add(type);
    }
  }

  /** Verifica se uma camada está visível. */
  public boolean isLayerVisible(MapMarkerType type) {
    return visibleLayers.contains(type);
  }

  // ====================================================================
  // Criação do world map (pre-render dos tiles)
  // ====================================================================

  public void createWorldMap() {
    worldMap = new BufferedImage[GamePanel.MAX_MAP];

    int maxWorldCol = gp.getMaxWorldCol();
    int maxWorldRow = gp.getMaxWorldRow();
    int worldMapWidth = GamePanel.TILE_SIZE * maxWorldCol;
    int worldMapHeight = GamePanel.TILE_SIZE * maxWorldRow;

    for (int i = 0; i < GamePanel.MAX_MAP; i++) {
      worldMap[i] = new BufferedImage(worldMapWidth, worldMapHeight, BufferedImage.TYPE_INT_ARGB);
      Graphics2D g2 = worldMap[i].createGraphics();

      int col = 0;
      int row = 0;

      while (col < maxWorldCol && row < maxWorldRow) {
        int tileNum = getMapTileNum()[i][col][row];
        int x = GamePanel.TILE_SIZE * col;
        int y = GamePanel.TILE_SIZE * row;
        g2.drawImage(getTiles()[tileNum].getImage(), x, y, null);

        col++;
        if (col == maxWorldCol) {
          col = 0;
          row++;
        }
      }
      g2.dispose();
    }
  }

  // ====================================================================
  // Mapa grande (tela cheia, MAP_STATE)
  // ====================================================================

  public void drawFullMapScreen(Graphics2D g2) {

    // Background
    g2.setColor(Color.black);
    g2.fillRect(0, 0, GamePanel.SCREEN_WIDTH, GamePanel.SCREEN_HEIGHT);

    // Draw Map
    int width = FULL_MAP_SIZE;
    int height = FULL_MAP_SIZE;
    int x = GamePanel.SCREEN_WIDTH / 2 - width / 2;
    int y = GamePanel.SCREEN_HEIGHT / 2 - height / 2;

    int currentMapIndex = gp.getCurrentMap();
    g2.drawImage(worldMap[currentMapIndex], x, y, width, height, null);

    // Escala: world pixels → display pixels
    int maxWorldCol = gp.getMaxWorldCol();
    double scale = (double) (GamePanel.TILE_SIZE * maxWorldCol) / width;

    // Draw Markers
    List<MapMarker> markers = markerProvider.getMarkers(currentMapIndex);
    MapView view = new MapView(x, y, width, height, scale, false);
    drawMarkers(g2, markers, view);

    // Draw Player (por cima dos markers)
    var player = gp.getPlayer();
    int playerX = (int) (x + player.getWorldX() / scale);
    int playerY = (int) (y + player.getWorldY() / scale);
    int playerSize = (int) (GamePanel.TILE_SIZE / 2.0);
    g2.drawImage(player.getDown1(), playerX, playerY, playerSize, playerSize, null);

    // Legenda
    drawLegend(g2, x, y + height + 10);
  }

  // ====================================================================
  // Mini mapa (canto superior direito, PLAY_STATE)
  // ====================================================================

  public void drawMiniMap(Graphics2D g2) {
    if (!miniMapOn) {
      return;
    }

    int width = MINI_MAP_SIZE;
    int height = MINI_MAP_SIZE;
    int x = GamePanel.SCREEN_WIDTH - width - MINI_MAP_MARGIN;
    int y = MINI_MAP_MARGIN;

    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, MINI_MAP_ALPHA));

    int currentMapIndex = gp.getCurrentMap();
    g2.drawImage(worldMap[currentMapIndex], x, y, width, height, null);

    // Escala
    int maxWorldCol = gp.getMaxWorldCol();
    double scale = (double) (GamePanel.TILE_SIZE * maxWorldCol) / width;

    // Draw Markers
    List<MapMarker> markers = markerProvider.getMarkers(currentMapIndex);
    MapView miniView = new MapView(x, y, width, height, scale, true);
    drawMarkers(g2, markers, miniView);

    // Draw Player
    var player = gp.getPlayer();
    int playerX = (int) (x + player.getWorldX() / scale);
    int playerY = (int) (y + player.getWorldY() / scale);
    int playerSize = (int) (GamePanel.TILE_SIZE / 3.0);
    g2.drawImage(player.getDown1(), playerX - 6, playerY - 6, playerSize, playerSize, null);

    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
  }

  // ====================================================================
  // Renderização de marcadores (compartilhada)
  // ====================================================================

  /**
   * Contexto de renderização do mapa — agrupa posição, tamanho e escala
   * para evitar métodos com muitos parâmetros.
   */
  private record MapView(int x, int y, int width, int height, double scale, boolean mini) {
  }

  /**
   * Desenha todos os marcadores visíveis sobre o mapa.
   *
   * @param g2      contexto gráfico
   * @param markers lista de marcadores do mapa atual
   * @param view    contexto de posição/escala do mapa na tela
   */
  private void drawMarkers(Graphics2D g2, List<MapMarker> markers, MapView view) {
    for (MapMarker marker : markers) {
      if (visibleLayers.contains(marker.type())) {
        int sx = (int) (view.x() + marker.worldX() / view.scale());
        int sy = (int) (view.y() + marker.worldY() / view.scale());

        // Clipping + renderização
        boolean inBounds = sx >= view.x() && sx <= view.x() + view.width()
            && sy >= view.y() && sy <= view.y() + view.height();
        if (inBounds) {
          int radius = getMarkerRadius(marker.type(), view.mini());
          drawShape(g2, marker.type(), sx, sy, radius);
        }
      }
    }
  }

  /** Retorna o raio do marcador conforme tipo e contexto (mini vs full). */
  private static int getMarkerRadius(MapMarkerType type, boolean isMini) {
    if (type == MapMarkerType.BOSS) {
      return isMini ? BOSS_MARKER_RADIUS_MINI : BOSS_MARKER_RADIUS_FULL;
    }
    return isMini ? MINI_MARKER_RADIUS : FULL_MARKER_RADIUS;
  }

  /**
   * Desenha a forma geométrica do marcador, com contorno para destaque.
   * Sem sprites extras — usa apenas {@link Graphics2D}.
   */
  private static void drawShape(Graphics2D g2, MapMarkerType type, int cx, int cy, int r) {
    // Contorno (1px maior em cada direção)
    g2.setColor(MARKER_OUTLINE);
    fillShapeAt(g2, type.getShape(), cx, cy, r + 1);

    // Preenchimento
    g2.setColor(type.getColor());
    fillShapeAt(g2, type.getShape(), cx, cy, r);
  }

  /** Preenche a forma geométrica centrada em (cx, cy) com o raio dado. */
  private static void fillShapeAt(Graphics2D g2, MapMarkerType.Shape shape, int cx, int cy, int r) {
    switch (shape) {
      case CIRCLE -> g2.fillOval(cx - r, cy - r, r * 2, r * 2);

      case DIAMOND -> {
        int[] xs = { cx, cx + r, cx, cx - r };
        int[] ys = { cy - r, cy, cy + r, cy };
        g2.fillPolygon(xs, ys, 4);
      }

      case SQUARE -> g2.fillRect(cx - r, cy - r, r * 2, r * 2);

      case TRIANGLE -> {
        int[] xs = { cx, cx + r, cx - r };
        int[] ys = { cy - r, cy + r, cy + r };
        g2.fillPolygon(xs, ys, 3);
      }
    }
  }

  // ====================================================================
  // Legenda (só no mapa grande)
  // ====================================================================

  /**
   * Desenha uma legenda discreta abaixo do mapa grande,
   * mostrando quais camadas estão visíveis e seus ícones.
   */
  private void drawLegend(Graphics2D g2, int startX, int startY) {
    MapMarkerType[] types = MapMarkerType.values();
    String[] labels = { "NPC", "Monster", "Boss", "Interactive", "Drop" };
    int[] toggleKeys = { 1, 2, 2, 3, 4 };

    int itemWidth = 100;
    int totalWidth = types.length * itemWidth;
    int legendX = startX + (FULL_MAP_SIZE - totalWidth) / 2;
    int legendY = startY;

    // Fundo da legenda
    g2.setColor(LEGEND_BG);
    g2.fillRoundRect(legendX - 10, legendY - 4, totalWidth + 20, 24, 8, 8);

    int curX = legendX;

    for (int i = 0; i < types.length; i++) {
      MapMarkerType type = types[i];
      boolean visible = visibleLayers.contains(type);

      // Ícone
      int iconCx = curX + 6;
      int iconCy = legendY + 8;
      if (visible) {
        drawShape(g2, type, iconCx, iconCy, 5);
      } else {
        g2.setColor(new Color(80, 80, 80));
        g2.fillOval(iconCx - 4, iconCy - 4, 8, 8);
      }

      // Texto
      g2.setColor(visible ? Color.WHITE : new Color(100, 100, 100));
      g2.setFont(g2.getFont().deriveFont(11f));
      g2.drawString(labels[i] + "(" + toggleKeys[i] + ")", curX + 16, legendY + 12);

      curX += itemWidth;
    }
  }
}

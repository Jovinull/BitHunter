package tile;

import java.awt.Color;

/**
 * Categorias de marcadores exibidos no mapa e mini mapa.
 *
 * <p>Cada tipo define uma cor e forma geométrica (desenhada via
 * {@link java.awt.Graphics2D}, sem sprites adicionais) para
 * identificação visual rápida.</p>
 */
public enum MapMarkerType {

  NPC(new Color(0x4F, 0xA8, 0xF5), Shape.CIRCLE),         // azul claro
  MONSTER(new Color(0xE0, 0x40, 0x40), Shape.CIRCLE),      // vermelho
  BOSS(new Color(0xFF, 0x20, 0x20), Shape.DIAMOND),         // vermelho forte, destaque
  INTERACTIVE(new Color(0xF5, 0xC5, 0x42), Shape.SQUARE),  // amarelo
  DROP(new Color(0x4F, 0xD1, 0xC5), Shape.TRIANGLE);       // turquesa

  /** Forma geométrica do marcador (renderizada pelo Map). */
  public enum Shape { CIRCLE, DIAMOND, SQUARE, TRIANGLE }

  private final Color color;
  private final Shape shape;

  MapMarkerType(Color color, Shape shape) {
    this.color = color;
    this.shape = shape;
  }

  public Color getColor() {
    return color;
  }

  public Shape getShape() {
    return shape;
  }
}

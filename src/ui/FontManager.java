package ui;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Gerenciador centralizado de fontes do jogo.
 *
 * <ul>
 *   <li>Carrega fontes uma única vez (classpath {@code /font/...}).</li>
 *   <li>Fallback automático para {@code SansSerif} se o recurso falhar.</li>
 *   <li>Cacheia todas as derivações (style + size) — zero alocações no render loop.</li>
 * </ul>
 *
 * Funciona tanto rodando via IDE quanto via JAR (usa
 * {@code Class.getResourceAsStream} que resolve no classpath).
 */
final class FontManager {

  private static final Logger LOGGER = Logger.getLogger(FontManager.class.getName());

  private static final String FALLBACK_FAMILY = "SansSerif";
  private static final Font FALLBACK_PIXEL = new Font(FALLBACK_FAMILY, Font.PLAIN, 16);
  private static final Font FALLBACK_DISPLAY = new Font(FALLBACK_FAMILY, Font.BOLD, 48);
  private static final Font FALLBACK_ARIAL = new Font(FALLBACK_FAMILY, Font.PLAIN, 20);

  /** Fonte pixel/retro (MaruMonica). Nunca null. */
  private final Font pixelBase;
  /** Fonte display/títulos (Purisa Bold). Nunca null. */
  private final Font displayBase;
  /** Fonte Arial (debug overlay). Nunca null. */
  private final Font arialBase;

  private final Map<Long, Font> pixelCache = new HashMap<>();
  private final Map<Long, Font> displayCache = new HashMap<>();
  private final Map<Long, Font> arialCache = new HashMap<>();

  FontManager() {
    pixelBase = loadFont("/font/x12y16pxMaruMonica.ttf", FALLBACK_PIXEL);
    displayBase = loadFont("/font/Purisa Bold.ttf", FALLBACK_DISPLAY);
    arialBase = loadFont("/font/ARIAL.ttf", FALLBACK_ARIAL);
  }

  // ---- API principal ----

  /** Fonte pixel derivada (estilo + tamanho). Resultado cacheado. */
  Font pixel(int style, float size) {
    return cached(pixelCache, pixelBase, style, size);
  }

  /** Fonte display derivada (estilo + tamanho). Resultado cacheado. */
  Font display(int style, float size) {
    return cached(displayCache, displayBase, style, size);
  }

  /** Fonte Arial derivada (estilo + tamanho). Resultado cacheado. */
  Font arial(int style, float size) {
    return cached(arialCache, arialBase, style, size);
  }

  // ---- Atalhos frequentes ----

  Font pixelPlain(float size)    { return pixel(Font.PLAIN, size); }
  Font pixelBold(float size)     { return pixel(Font.BOLD, size); }
  Font pixelItalic(float size)   { return pixel(Font.ITALIC, size); }
  Font displayPlain(float size)  { return display(Font.PLAIN, size); }
  Font displayBold(float size)   { return display(Font.BOLD, size); }
  Font arialPlain(float size)    { return arial(Font.PLAIN, size); }

  /** Fonte-base pixel (para compatibilidade externa). Nunca null. */
  Font getPixelBase() {
    return pixelBase;
  }

  // ---- Internos ----

  private static Font loadFont(String path, Font fallback) {
    try (InputStream is = FontManager.class.getResourceAsStream(path)) {
      if (is != null) {
        Font f = Font.createFont(Font.TRUETYPE_FONT, is);
        LOGGER.log(Level.INFO, "Fonte carregada: {0}", path);
        return f;
      }
      LOGGER.log(Level.WARNING, "Fonte n\u00e3o encontrada: {0} \u2014 usando fallback", path);
    } catch (FontFormatException | IOException e) {
      LOGGER.log(Level.WARNING, e, () -> "Erro ao carregar fonte " + path + " \u2014 usando fallback");
    }
    return fallback;
  }

  private static Font cached(Map<Long, Font> cache, Font base, int style, float size) {
    long key = ((long) style << 32) | Float.floatToIntBits(size);
    return cache.computeIfAbsent(key, k -> base.deriveFont(style, size));
  }
}

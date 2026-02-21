package main;

import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cache em memória para imagens carregadas e escalonadas do disco.
 * Evita carregamento redundante de assets e stutters causados pelo GC,
 * especialmente durante as transições de "Novo Jogo" e trocas de mapas,
 * onde múltiplas instâncias da mesma entidade são criadas.
 */
public class ImageCache {

  private static final Map<String, BufferedImage> cache = new ConcurrentHashMap<>();

  private ImageCache() {
    // Hidden constructor
  }

  /**
   * Obtém uma imagem do cache pela sua chave combinada (path + _ + w + x + h).
   *
   * @param key
   *          Chave única da imagem (ex: "/monster/greenslime_down_1_48x48")
   * @return A BufferedImage já escalada e cacheada, ou null se não existir.
   */
  public static BufferedImage get(String key) {
    return cache.get(key);
  }

  /**
   * Armazena uma imagem carregada e escalonada no cache.
   *
   * @param key
   *          Chave única da imagem
   * @param image
   *          Imagem escalonada
   */
  public static void put(String key, BufferedImage image) {
    if (key != null && image != null) {
      cache.put(key, image);
    }
  }

  /**
   * Limpa o cache. Útil se precisarmos liberar memória (ex: menus pesados ou
   * recarga agressiva).
   */
  public static void clear() {
    cache.clear();
  }
}

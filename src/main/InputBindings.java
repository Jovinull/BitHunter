package main;

import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Gerencia o mapeamento tecla → ação do jogo.
 * <p>
 * Carrega bindings de {@code controls.properties} com fallback para os
 * defaults definidos em {@link GameAction#getDefaultKey()}.
 * Oferece lookup reverso: dado um keyCode, retorna as ações associadas.
 * </p>
 *
 * <p><b>Formato do arquivo:</b></p>
 * <pre>
 *   MOVE_UP=W
 *   MOVE_UP=W,UP        (duas teclas para a mesma ação)
 *   debug.enabled=false  (desativa ações de debug)
 * </pre>
 */
public class InputBindings {

  private static final Logger LOGGER = Logger.getLogger(InputBindings.class.getName());
  private static final String FILE_NAME = "controls.properties";
  private static final String DEBUG_ENABLED_KEY = "debug.enabled";

  /** Mapeamento ação → lista de keyCodes. */
  private final Map<GameAction, List<Integer>> actionToKeys =
      new EnumMap<>(GameAction.class);

  /** Lookup reverso: keyCode → lista de ações. */
  private final Map<Integer, List<GameAction>> keyToActions = new HashMap<>();

  /** Se false, ações da categoria DEBUG são ignoradas. */
  private boolean debugEnabled = true;

  public InputBindings() {
    applyDefaults();
    loadFromFile();
    rebuildReverseLookup();
  }

  // ───────────────────────────────────────────
  // Defaults
  // ───────────────────────────────────────────

  /** Aplica os defaults a partir das teclas definidas em {@link GameAction}. */
  private void applyDefaults() {
    for (GameAction action : GameAction.values()) {
      actionToKeys.put(action, new ArrayList<>(List.of(action.getDefaultKey())));
    }
  }

  // ───────────────────────────────────────────
  // Carregamento do arquivo
  // ───────────────────────────────────────────

  private void loadFromFile() {
    File file = new File(FILE_NAME);
    if (!file.exists()) {
      LOGGER.info("Arquivo de controles nao encontrado ("
          + FILE_NAME + "); usando padroes.");
      return;
    }

    Properties props = new Properties();
    try (var reader = new BufferedReader(
        new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
      props.load(reader);
    } catch (IOException e) {
      LOGGER.log(Level.WARNING,
          "Falha ao carregar " + FILE_NAME + "; usando padroes.", e);
      return;
    }

    // Flag global de debug
    String debugFlag = props.getProperty(DEBUG_ENABLED_KEY);
    if (debugFlag != null) {
      debugEnabled = !"false".equalsIgnoreCase(debugFlag.trim());
    }

    // Override de ações individuais
    for (GameAction action : GameAction.values()) {
      String value = props.getProperty(action.name());
      if (value == null || value.isBlank()) {
        continue;
      }

      List<Integer> keys = parseKeyList(action.name(), value);
      if (!keys.isEmpty()) {
        actionToKeys.put(action, keys);
      } else {
        LOGGER.log(Level.WARNING, "Nenhuma tecla valida para {0} em {1}; mantendo default.",
            new Object[]{action.name(), FILE_NAME});
      }
    }
  }

  /**
   * Interpreta uma lista de nomes de tecla separados por vírgula.
   * Ex.: {@code "W"}, {@code "W,UP"}, {@code "ENTER"}
   */
  private List<Integer> parseKeyList(String actionName, String raw) {
    List<Integer> keys = new ArrayList<>();
    for (String part : raw.split(",")) {
      String trimmed = part.trim();
      if (trimmed.isEmpty()) {
        continue;
      }

      int keyCode = parseKeyName(trimmed);
      if (keyCode != -1) {
        keys.add(keyCode);
      } else {
        LOGGER.log(Level.WARNING, "Tecla invalida ''{0}'' para acao {1}; ignorada.",
            new Object[]{trimmed, actionName});
      }
    }
    return keys;
  }

  /**
   * Converte um nome amigável de tecla para o keyCode correspondente.
   * <p>
   * Aceita:
   * letras ({@code A-Z}), números ({@code 0-9}),
   * especiais ({@code ENTER, ESCAPE, ESC, SPACE, TAB}),
   * setas ({@code UP, DOWN, LEFT, RIGHT}),
   * funções ({@code F1..F12}), e outros ({@code SHIFT, CTRL, ALT}).
   * </p>
   *
   * @return keyCode ou -1 se inválido
   */
  static int parseKeyName(String name) {
    String upper = name.toUpperCase().trim();

    // Aliases que não correspondem diretamente a VK_*
    switch (upper) {
      case "ESC" -> {
        return KeyEvent.VK_ESCAPE;
      }
      case "BACKSPACE" -> {
        return KeyEvent.VK_BACK_SPACE;
      }
      case "CTRL" -> {
        return KeyEvent.VK_CONTROL;
      }
      default -> { /* fall through para reflection */ }
    }

    // Tenta localizar o campo VK_<NOME> em KeyEvent via reflection
    try {
      Field field = KeyEvent.class.getField("VK_" + upper);
      return field.getInt(null);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      return -1;
    }
  }

  // ───────────────────────────────────────────
  // Lookup reverso
  // ───────────────────────────────────────────

  private void rebuildReverseLookup() {
    keyToActions.clear();
    for (var entry : actionToKeys.entrySet()) {
      for (int keyCode : entry.getValue()) {
        keyToActions
            .computeIfAbsent(keyCode, k -> new ArrayList<>())
            .add(entry.getKey());
      }
    }
  }

  // ───────────────────────────────────────────
  // API pública
  // ───────────────────────────────────────────

  /**
   * Verifica se o keyCode corresponde à ação informada.
   * Ações de debug retornam {@code false} se {@code debug.enabled=false}.
   */
  public boolean isAction(int keyCode, GameAction action) {
    if (action.getCategory() == GameAction.Category.DEBUG && !debugEnabled) {
      return false;
    }
    List<GameAction> actions = keyToActions.get(keyCode);
    return actions != null && actions.contains(action);
  }

  /**
   * Retorna todas as ações mapeadas para o keyCode.
   * Ações de debug são omitidas se desativadas.
   */
  public Set<GameAction> getActions(int keyCode) {
    List<GameAction> actions = keyToActions.get(keyCode);
    if (actions == null || actions.isEmpty()) {
      return Collections.emptySet();
    }

    if (debugEnabled) {
      return EnumSet.copyOf(actions);
    }

    EnumSet<GameAction> filtered = EnumSet.noneOf(GameAction.class);
    for (GameAction a : actions) {
      if (a.getCategory() != GameAction.Category.DEBUG) {
        filtered.add(a);
      }
    }
    return filtered;
  }

  /** Retorna a(s) tecla(s) mapeada(s) para a ação (lista imutável). */
  public List<Integer> getKeys(GameAction action) {
    return Collections.unmodifiableList(
        actionToKeys.getOrDefault(action, Collections.emptyList()));
  }

  /** Primeira tecla mapeada para a ação, ou {@code -1} se nenhuma. */
  public int getFirstKey(GameAction action) {
    List<Integer> keys = actionToKeys.get(action);
    if (keys == null || keys.isEmpty()) {
      return -1;
    }
    return keys.get(0);
  }

  /** Se as ações de debug estão habilitadas. */
  public boolean isDebugEnabled() {
    return debugEnabled;
  }

  // ───────────────────────────────────────────
  // Helpers de exibição
  // ───────────────────────────────────────────

  /**
   * Nome amigável e conciso de uma tecla para exibição na UI.
   * Ex.: {@code "W"}, {@code "ENTER"}, {@code "ESC"}, {@code "↑"}
   */
  public static String keyName(int keyCode) {
    return switch (keyCode) {
      case KeyEvent.VK_ESCAPE -> "ESC";
      case KeyEvent.VK_ENTER -> "ENTER";
      case KeyEvent.VK_SPACE -> "SPACE";
      case KeyEvent.VK_UP -> "\u2191";
      case KeyEvent.VK_DOWN -> "\u2193";
      case KeyEvent.VK_LEFT -> "\u2190";
      case KeyEvent.VK_RIGHT -> "\u2192";
      case KeyEvent.VK_BACK_SPACE -> "BACKSPACE";
      case KeyEvent.VK_DELETE -> "DEL";
      case KeyEvent.VK_SHIFT -> "SHIFT";
      case KeyEvent.VK_CONTROL -> "CTRL";
      case KeyEvent.VK_ALT -> "ALT";
      case KeyEvent.VK_TAB -> "TAB";
      default -> KeyEvent.getKeyText(keyCode).toUpperCase();
    };
  }

  /**
   * Nome(s) da(s) tecla(s) mapeada(s) para uma ação.
   * Ex.: {@code "W"}, {@code "ENTER"}, {@code "W, ↑"}
   */
  public String getKeyDisplayName(GameAction action) {
    List<Integer> keys = actionToKeys.get(action);
    if (keys == null || keys.isEmpty()) {
      return "???";
    }
    if (keys.size() == 1) {
      return keyName(keys.get(0));
    }

    var sb = new StringBuilder();
    for (int i = 0; i < keys.size(); i++) {
      if (i > 0) {
        sb.append(", ");
      }
      sb.append(keyName(keys.get(i)));
    }
    return sb.toString();
  }

  /**
   * Representação compacta das 4 teclas de movimento para a UI.
   * <p>
   * Se todas forem caracteres únicos, concatena (ex.: {@code "WASD"});
   * caso contrário, separa com barra (ex.: {@code "↑/←/↓/→"}).
   * </p>
   */
  public String getMovementDisplay() {
    String u = keyName(getFirstKey(GameAction.MOVE_UP));
    String l = keyName(getFirstKey(GameAction.MOVE_LEFT));
    String d = keyName(getFirstKey(GameAction.MOVE_DOWN));
    String r = keyName(getFirstKey(GameAction.MOVE_RIGHT));

    if (u.length() == 1 && l.length() == 1
        && d.length() == 1 && r.length() == 1) {
      return u + l + d + r;
    }
    return u + "/" + l + "/" + d + "/" + r;
  }

  // ───────────────────────────────────────────
  // Remapeamento em tempo de execução
  // ───────────────────────────────────────────

  /**
   * Remapeia uma ação para um novo keyCode (substitui bindings anteriores).
   * Atualiza o lookup reverso automaticamente.
   */
  public void rebind(GameAction action, int keyCode) {
    actionToKeys.put(action, new ArrayList<>(List.of(keyCode)));
    rebuildReverseLookup();
  }

  /**
   * Persiste os bindings atuais em {@value #FILE_NAME}.
   * <p>O formato gerado é compatível com {@link #loadFromFile()}.</p>
   */
  public void saveToFile() {
    try (var writer = new BufferedWriter(
        new OutputStreamWriter(
            new FileOutputStream(FILE_NAME), StandardCharsets.UTF_8))) {

      writer.write("# BitHunter — Configuracao de Controles");
      writer.newLine();
      writer.newLine();

      // Flag de debug
      writer.write(DEBUG_ENABLED_KEY + "=" + debugEnabled);
      writer.newLine();
      writer.newLine();

      // Ações agrupadas por categoria
      GameAction.Category lastCategory = null;
      for (GameAction action : GameAction.values()) {
        if (action.getCategory() != lastCategory) {
          lastCategory = action.getCategory();
          writer.write("# " + lastCategory.name());
          writer.newLine();
        }

        List<Integer> keys = actionToKeys.get(action);
        if (keys == null || keys.isEmpty()) {
          continue;
        }

        var sb = new StringBuilder();
        for (int i = 0; i < keys.size(); i++) {
          if (i > 0) sb.append(",");
          sb.append(keyNameForSave(keys.get(i)));
        }
        writer.write(action.name() + "=" + sb);
        writer.newLine();
      }
    } catch (IOException e) {
      LOGGER.log(Level.WARNING, "Falha ao salvar " + FILE_NAME, e);
    }
  }

  /**
   * Nome da tecla para gravação no arquivo (compatível com {@link #parseKeyName}).
   */
  private static String keyNameForSave(int keyCode) {
    return switch (keyCode) {
      case KeyEvent.VK_ESCAPE -> "ESCAPE";
      case KeyEvent.VK_ENTER -> "ENTER";
      case KeyEvent.VK_SPACE -> "SPACE";
      case KeyEvent.VK_UP -> "UP";
      case KeyEvent.VK_DOWN -> "DOWN";
      case KeyEvent.VK_LEFT -> "LEFT";
      case KeyEvent.VK_RIGHT -> "RIGHT";
      case KeyEvent.VK_BACK_SPACE -> "BACKSPACE";
      case KeyEvent.VK_CONTROL -> "CTRL";
      case KeyEvent.VK_TAB -> "TAB";
      case KeyEvent.VK_SHIFT -> "SHIFT";
      case KeyEvent.VK_ALT -> "ALT";
      case KeyEvent.VK_DELETE -> "DELETE";
      default -> KeyEvent.getKeyText(keyCode).toUpperCase().replace(' ', '_');
    };
  }
}

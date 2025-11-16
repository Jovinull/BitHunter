package main;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Gerencia os textos do jogo (i18n) usando arquivos .properties.
 * Não usa nenhuma lib externa, apenas Java SE.
 */
public class LanguageManager {

  private static final Logger LOGGER = Logger.getLogger(LanguageManager.class.getName());

  private static final String BASE_PATH = "/lang/messages_";
  private static final String EXT = ".properties";

  public static final String DEFAULT_LANGUAGE = "pt_BR";

  private final Map<String, Properties> languages = new HashMap<>();
  private String currentLanguageCode = DEFAULT_LANGUAGE;

  public record LanguageOption(String code, String displayName) {
  }

  public LanguageManager() {
    // Carrega o idioma padrão logo de início
    loadLanguage(DEFAULT_LANGUAGE);
  }

  /**
   * Idiomas disponíveis no jogo.
   * Aqui você controla quais aparecem no menu.
   */
  public List<LanguageOption> getAvailableLanguages() {
    List<LanguageOption> list = new ArrayList<>();
    list.add(new LanguageOption("pt_BR", "Português (Brasil)"));
    list.add(new LanguageOption("en_US", "English"));
    list.add(new LanguageOption("es_ES", "Español"));
    list.add(new LanguageOption("fr_FR", "Français"));
    list.add(new LanguageOption("it_IT", "Italiano"));
    // adicionar aqui mais idiomas futuramente
    return list;
  }

  public String getCurrentLanguageCode() {
    return currentLanguageCode;
  }

  public String getCurrentLanguageDisplayName() {
    return getDisplayNameFor(currentLanguageCode);
  }

  public String getDisplayNameFor(String code) {
    Optional<LanguageOption> opt = getAvailableLanguages().stream()
        .filter(o -> o.code().equals(code))
        .findFirst();
    return opt.map(LanguageOption::displayName).orElse(code);
  }

  /**
   * Define o idioma atual. Tenta carregar o arquivo correspondente.
   * Em caso de falha, volta para o DEFAULT_LANGUAGE.
   */
  public void setCurrentLanguage(String languageCode) {
    if (languageCode == null || languageCode.isBlank()) {
      languageCode = DEFAULT_LANGUAGE;
    }

    if (loadLanguage(languageCode)) {
      currentLanguageCode = languageCode;
    } else {
      LOGGER.warning("Falha ao carregar idioma " + languageCode + ", usando idioma padrão.");
      loadLanguage(DEFAULT_LANGUAGE);
      currentLanguageCode = DEFAULT_LANGUAGE;
    }
  }

  /**
   * Retorna o texto traduzido para a chave.
   * Se não encontrar, devolve "??chave??" e tenta fallback para o idioma padrão.
   */
  public String text(String key) {
    if (key == null) {
      return "";
    }

    Properties currentProps = languages.get(currentLanguageCode);
    String value = currentProps != null ? currentProps.getProperty(key) : null;

    if (value != null) {
      return value;
    }

    if (!DEFAULT_LANGUAGE.equals(currentLanguageCode)) {
      Properties defaultProps = languages.get(DEFAULT_LANGUAGE);
      if (defaultProps != null) {
        value = defaultProps.getProperty(key);
        if (value != null) {
          return value;
        }
      }
    }

    return "??" + key + "??";
  }

  private boolean loadLanguage(String languageCode) {
    if (languages.containsKey(languageCode)) {
      return true;
    }

    String resourceName = BASE_PATH + languageCode + EXT;

    try (InputStream in = getClass().getResourceAsStream(resourceName)) {
      if (in == null) {
        LOGGER.warning("Arquivo de idioma não encontrado: " + resourceName);
        return false;
      }

      Properties props = new Properties();
      try (InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
        props.load(reader);
      }

      languages.put(languageCode, props);
      return true;
    } catch (IOException e) {
      LOGGER.log(Level.SEVERE, "Erro ao carregar idioma " + languageCode, e);
      return false;
    }
  }
}

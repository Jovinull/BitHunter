package entity.services;

import main.GamePanel;
import main.LanguageManager;

public class TranslationService {

  public String text(GamePanel gp, String key) {
    LanguageManager lm = gp != null ? gp.getLanguageManager() : null;
    if (lm == null) {
      return key;
    }
    return lm.text(key);
  }
}

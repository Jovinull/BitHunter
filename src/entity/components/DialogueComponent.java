package entity.components;

import entity.Entity;
import main.GamePanel;
import main.LanguageManager;

public class DialogueComponent {

  private final Entity entity;
  private final String[][] dialogues = new String[20][20];
  private int dialogueSet = 0;
  private int dialogueIndex = 0;

  public DialogueComponent(Entity entity) {
    this.entity = entity;
  }

  public String[][] getDialogues() {
    return dialogues;
  }

  public int getDialogueSet() {
    return dialogueSet;
  }

  public void setDialogueSet(int dialogueSet) {
    this.dialogueSet = dialogueSet;
  }

  public int getDialogueIndex() {
    return dialogueIndex;
  }

  public void setDialogueIndex(int dialogueIndex) {
    this.dialogueIndex = dialogueIndex;
  }

  public void startDialogue(Entity entity, int setNum) {
    entity.getGp().setGameState(GamePanel.DIALOGUE_STATE);
    entity.getGp().getGameUi().setNpc(entity);
    this.dialogueSet = setNum;
  }

  public String getDisplayName() {
    LanguageManager lm = entity.getGp() != null ? entity.getGp().getLanguageManager() : null;

    String baseKey = null;
    if (entity.getTranslationKey() != null && !entity.getTranslationKey().isEmpty()) {
      baseKey = entity.getTranslationKey();
    } else if (entity.getInternalId() != null && !entity.getInternalId().isEmpty()) {
      baseKey = entity.getInternalId();
    }

    if (lm != null && baseKey != null) {
      return lm.text(baseKey + ".name");
    }

    return entity.getName() != null ? entity.getName() : "";
  }

  public String getDisplayDescription() {
    LanguageManager lm = entity.getGp() != null ? entity.getGp().getLanguageManager() : null;

    String baseKey = null;
    if (entity.getTranslationKey() != null && !entity.getTranslationKey().isEmpty()) {
      baseKey = entity.getTranslationKey();
    } else if (entity.getInternalId() != null && !entity.getInternalId().isEmpty()) {
      baseKey = entity.getInternalId();
    }

    if (lm != null && baseKey != null) {
      return lm.text(baseKey + ".desc");
    }

    // Fallback: usa a descrição que eventualmente foi preenchida manualmente
    String desc = entity.getDescription();
    return desc != null ? desc : "";
  }

  public String t(String key) {
    LanguageManager lm = entity.getGp() != null ? entity.getGp().getLanguageManager() : null;
    if (lm == null) {
      return key;
    }
    return lm.text(key);
  }

  public void fillDialogueSet(int setIndex, String baseKey, int totalLines) {
    if (baseKey == null || baseKey.isBlank()) {
      return;
    }

    for (int i = 0; i < totalLines; i++) {
      dialogues[setIndex][i] = t(baseKey + ".line" + (i + 1));
    }
  }
}

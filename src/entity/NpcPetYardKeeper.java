package entity;

import main.GamePanel;

/**
 * NPC responsável pelo viveiro de pets (Pet Yard).
 * Sprite: /npc/yard.png
 */
public class NpcPetYardKeeper extends Entity {

  private static final String NPC_ID = "npc.petYardKeeper";

  public NpcPetYardKeeper(GamePanel gp) {
    super(gp);
    setType(TYPE_NPC);
    setInternalId(NPC_ID);
    setTranslationKey(NPC_ID);

    setDirection(DIR_DOWN);
    setSpeed(1);

    getImage();
    setDialogues();
  }

  @Override
  public void getImage() {
    int size = GamePanel.TILE_SIZE;

    var yard = setup("/npc/yard", size, size);

    // Usa a mesma sprite em todas direções
    up1 = yard;
    up2 = yard;
    down1 = yard;
    down2 = yard;
    left1 = yard;
    left2 = yard;
    right1 = yard;
    right2 = yard;
  }

  private void setDialogues() {
    // Exemplo: duas linhas de fala
    // npc.petYardKeeper.dialog.0.line1
    // npc.petYardKeeper.dialog.0.line2
    fillDialogueSet(0, "npc.petYardKeeper.dialog.0", 2);
  }

  @Override
  public void speak() {
    facePlayer();
    startDialogue(this, 0);

    gp.setGameState(GamePanel.PET_YARD_STATE);
    gp.getGameUi().setNpc(this);
    gp.getGameUi().setSubState(0);
    gp.getGameUi().setCommandNum(0);
  }
}

package entity;

import java.awt.Rectangle;

import main.GamePanel;

public class NpcOldMan extends Entity {

  public static final String NPC_ID = "npc.oldMan";

  // Objetivo do pathfinder
  private static final int GOAL_COL = 23;
  private static final int GOAL_ROW = 13;

  private static final String SPRITE_UP_1 = "/npc/oldman/oldman_up_1";
  private static final String SPRITE_UP_2 = "/npc/oldman/oldman_up_2";
  private static final String SPRITE_DOWN_1 = "/npc/oldman/oldman_down_1";
  private static final String SPRITE_DOWN_2 = "/npc/oldman/oldman_down_2";
  private static final String SPRITE_LEFT_1 = "/npc/oldman/oldman_left_1";
  private static final String SPRITE_LEFT_2 = "/npc/oldman/oldman_left_2";
  private static final String SPRITE_RIGHT_1 = "/npc/oldman/oldman_right_1";
  private static final String SPRITE_RIGHT_2 = "/npc/oldman/oldman_right_2";

  // Parâmetros de comportamento
  private static final int RANDOM_DIR_INTERVAL_TICKS = 120;
  private static final int LOW_LIFE_DIVISOR = 3;

  private boolean startedWalkToGoal = false;

  public NpcOldMan(GamePanel gp) {
    super(gp);

    setInternalId(NPC_ID);
    setTranslationKey(NPC_ID);

    setDirection(DIR_DOWN);
    setSpeed(1);
    setDefaultSpeed(getSpeed());

    // solidArea é final -> configurar via getter
    Rectangle sa = getSolidArea();
    sa.x = 8;
    sa.y = 16;
    setSolidAreaDefaultX(sa.x);
    setSolidAreaDefaultY(sa.y);
    sa.width = 30;
    sa.height = 30;

    setDialogueSet(-1);

    getImage();
    setDialogue();
  }

  @Override
  public void getImage() {
    int tileSize = GamePanel.TILE_SIZE;
    up1 = setup(SPRITE_UP_1, tileSize, tileSize);
    up2 = setup(SPRITE_UP_2, tileSize, tileSize);
    down1 = setup(SPRITE_DOWN_1, tileSize, tileSize);
    down2 = setup(SPRITE_DOWN_2, tileSize, tileSize);
    left1 = setup(SPRITE_LEFT_1, tileSize, tileSize);
    left2 = setup(SPRITE_LEFT_2, tileSize, tileSize);
    right1 = setup(SPRITE_RIGHT_1, tileSize, tileSize);
    right2 = setup(SPRITE_RIGHT_2, tileSize, tileSize);
  }

  public void setDialogue() {
    String base = getTranslationKey() + ".dialog.";

    // Conjunto 0: 4 linhas
    fillDialogueSet(0, base + "0", 4);

    // Conjunto 1: 3 linhas
    fillDialogueSet(1, base + "1", 3);

    // Conjunto 2: 1 linha
    fillDialogueSet(2, base + "2", 1);
  }

  private boolean atGoal() {
    return getCol() == GOAL_COL && getRow() == GOAL_ROW;
  }

  @Override
  public void setAction() {
    if (startedWalkToGoal) {
      if (atGoal()) {
        startedWalkToGoal = false;
        setOnPath(false);
      } else {
        setOnPath(true);
        searchPath(GOAL_COL, GOAL_ROW);
      }
      return;
    }
    getRandomDirection(RANDOM_DIR_INTERVAL_TICKS);
  }

  @Override
  public void speak() {
    facePlayer();
    startDialogue(this, getDialogueSet());
    setDialogueSet(getDialogueSet() + 1);

    var player = gp.getPlayer();

    // Reage ao HP do player
    if (player.getLife() < player.getMaxLife() / LOW_LIFE_DIVISOR) {
      setDialogueSet(1);
      startDialogue(this, getDialogueSet());
    }

    // Inicia caminhada ao objetivo quando entra no diálogo 1
    if (getDialogueSet() == 1 && !startedWalkToGoal && !atGoal()) {
      startedWalkToGoal = true;
      setOnPath(true);
    }

    String[][] d = getDialogues();
    if (d[getDialogueSet()][0] == null) {
      setDialogueSet(Math.max(0, getDialogueSet() - 1));
    }
  }
}

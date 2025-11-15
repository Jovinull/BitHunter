package main;

import java.awt.Graphics2D;

import entity.PlayerDummy;
import monster.MonSkeletonLord;
import object.ObjDoorIron;

public class CutsceneManager {

  private final GamePanel gp;

  // estado interno da cena (campos não públicos conforme S1104)
  private int sceneNum;
  private int scenePhase;

  // constantes (estáticas e em CAIXA_ALTA conforme S1170 e S115)
  private static final int NA = 0;
  private static final int SKELETON_LORD = 1;

  public CutsceneManager(GamePanel gp) {
    this.gp = gp;
  }

  public void draw(Graphics2D g2) {
    // usar o parâmetro evita S1172 (parâmetro não utilizado)
    if (g2 == null) {
      return;
    }

    if (sceneNum == SKELETON_LORD) {
      sceneSkeletonLord();
    }
  }

  // renomeado para lowerCamelCase sem underscore (S100)
  private void sceneSkeletonLord() {
    // reduzir complexidade cognitiva (S3776) delegando por fase
    if (scenePhase == 0) {
      phase0Setup();
      return;
    }
    if (scenePhase == 1) {
      phase1MovePlayerUp();
      return;
    }
    if (scenePhase == 2) {
      phase2WakeBossAndOpenDialogue();
      return;
    }
    if (scenePhase == 3) {
      phase3ShowBossDialogue();
      return;
    }
    if (scenePhase == 4) {
      phase4RestorePlayerAndReset();
    }
  }

  private void phase0Setup() {
    gp.setBossBattleOn(true);
    placeIronDoor();
    spawnPlayerDummy();
    gp.getPlayer().setDrawing(false);
    scenePhase++;
  }

  private void phase1MovePlayerUp() {
    var player = gp.getPlayer();
    player.setWorldY(player.getWorldY() - 2);
    if (player.getWorldY() < GamePanel.TILE_SIZE * 16) {
      scenePhase++;
    }
  }

  private void phase2WakeBossAndOpenDialogue() {
    var monsters = gp.getMonster();
    int currentMap = gp.getCurrentMap();

    // Procurar o boss
    for (int i = 0; i < monsters[currentMap].length; i++) {
      var monster = monsters[currentMap][i];
      if (monster != null && MonSkeletonLord.MON_ID.equals(monster.getInternalId())) {
        monster.setSleep(false);
        gp.getGameUi().setNpc(monster);
        scenePhase++;
        break;
      }
    }
  }

  private void phase3ShowBossDialogue() {
    // O boss fala
    gp.getGameUi().drawDialogueScreen();
  }

  private void phase4RestorePlayerAndReset() {
    restorePlayerFromDummy();
    gp.getPlayer().setDrawing(true);

    // Reset
    sceneNum = NA;
    scenePhase = 0;
    gp.setGameState(GamePanel.PLAY_STATE);

    // Trocar música
    gp.stopMusic();
    gp.playMusic(22);
  }

  private void placeIronDoor() {
    var objects = gp.getObj();
    int currentMap = gp.getCurrentMap();

    // Fechar a porta de ferro
    for (int i = 0; i < objects[currentMap].length; i++) {
      if (objects[currentMap][i] == null) {
        objects[currentMap][i] = new ObjDoorIron(gp);
        objects[currentMap][i].setWorldX(GamePanel.TILE_SIZE * 25);
        objects[currentMap][i].setWorldY(GamePanel.TILE_SIZE * 28);
        objects[currentMap][i].setTemp(true);
        gp.playSE(21);
        break;
      }
    }
  }

  private void spawnPlayerDummy() {
    var npcs = gp.getNpc();
    int currentMap = gp.getCurrentMap();
    var player = gp.getPlayer();

    // Slot vago para o dummy
    for (int i = 0; i < npcs[currentMap].length; i++) {
      if (npcs[currentMap][i] == null) {
        npcs[currentMap][i] = new PlayerDummy(gp);
        npcs[currentMap][i].setWorldX(player.getWorldX());
        npcs[currentMap][i].setWorldY(player.getWorldY());
        npcs[currentMap][i].setDirection(player.getDirection());
        break;
      }
    }
  }

  private void restorePlayerFromDummy() {
    var npcs = gp.getNpc();
    int currentMap = gp.getCurrentMap();
    var player = gp.getPlayer();

    // Procurar o dummy
    for (int i = 0; i < npcs[currentMap].length; i++) {
      if (npcs[currentMap][i] != null &&
          PlayerDummy.NPC_ID.equals(npcs[currentMap][i].getInternalId())) {
        // Restaurar posição do player
        player.setWorldX(npcs[currentMap][i].getWorldX());
        player.setWorldY(npcs[currentMap][i].getWorldY());
        // Remover o dummy
        npcs[currentMap][i] = null;
        break;
      }
    }
  }

  public int getSceneNum() {
    return sceneNum;
  }

  public void nextPhase() {
    this.scenePhase++;
  }

  public void startSkeletonLordScene() {
    this.sceneNum = SKELETON_LORD;
    this.scenePhase = 0;
  }
}

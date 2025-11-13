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
    gp.bossBattleOn = true;
    placeIronDoor();
    spawnPlayerDummy();
    gp.player.setDrawing(false);
    scenePhase++;
  }

  private void phase1MovePlayerUp() {
    gp.player.setWorldY(gp.player.getWorldY() - 2);
    if (gp.player.getWorldY() < gp.tileSize * 16) {
      scenePhase++;
    }
  }

  private void phase2WakeBossAndOpenDialogue() {
    // Procurar o boss
    for (int i = 0; i < gp.monster[gp.currentMap].length; i++) {
      if (gp.monster[gp.currentMap][i] != null
          && MonSkeletonLord.MON_NAME.equals(gp.monster[gp.currentMap][i].getName())) {
        gp.monster[gp.currentMap][i].setSleep(false);
        gp.ui.setNpc(gp.monster[gp.currentMap][i]);
        scenePhase++;
        break;
      }
    }
  }

  private void phase3ShowBossDialogue() {
    // O boss fala
    gp.ui.drawDialogueScreen();
  }

  private void phase4RestorePlayerAndReset() {
    restorePlayerFromDummy();
    gp.player.setDrawing(true);

    // Reset
    sceneNum = NA;
    scenePhase = 0;
    gp.gameState = gp.playState;

    // Trocar música
    gp.stopMusic();
    gp.playMusic(22);
  }

  private void placeIronDoor() {
    // Fechar a porta de ferro
    for (int i = 0; i < gp.obj[gp.currentMap].length; i++) {
      if (gp.obj[gp.currentMap][i] == null) {
        gp.obj[gp.currentMap][i] = new ObjDoorIron(gp);
        gp.obj[gp.currentMap][i].setWorldX(gp.tileSize * 25);
        gp.obj[gp.currentMap][i].setWorldY(gp.tileSize * 28);
        gp.obj[gp.currentMap][i].setTemp(true);
        gp.playSE(21);
        break;
      }
    }
  }

  private void spawnPlayerDummy() {
    // Slot vago para o dummy
    for (int i = 0; i < gp.npc[gp.currentMap].length; i++) {
      if (gp.npc[gp.currentMap][i] == null) {
        gp.npc[gp.currentMap][i] = new PlayerDummy(gp);
        gp.npc[gp.currentMap][i].setWorldX(gp.player.getWorldX());
        gp.npc[gp.currentMap][i].setWorldY(gp.player.getWorldY());
        gp.npc[gp.currentMap][i].setDirection(gp.player.getDirection());
        break;
      }
    }
  }

  private void restorePlayerFromDummy() {
    // Procurar o dummy
    for (int i = 0; i < gp.npc[gp.currentMap].length; i++) {
      if (gp.npc[gp.currentMap][i] != null && PlayerDummy.NPC_NAME.equals(gp.npc[gp.currentMap][i].getName())) {
        // Restaurar posição do player
        gp.player.setWorldX(gp.npc[gp.currentMap][i].getWorldX());
        gp.player.setWorldY(gp.npc[gp.currentMap][i].getWorldY());
        // Remover o dummy
        gp.npc[gp.currentMap][i] = null;
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

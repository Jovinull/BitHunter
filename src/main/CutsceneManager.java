package main;

import java.awt.Graphics2D;

import entity.PlayerDummy;
import monster.MON_SkeletonLord;
import object.OBJ_Door_Iron;

public class CutsceneManager {

  private final GamePanel gp;
  private Graphics2D g2;
  public int sceneNum;
  public int scenePhase;

  // Scene Number
  public final int NA = 0;
  public final int skeletonLord = 1;

  public CutsceneManager(GamePanel gp) {
    this.gp = gp;
  }

  public void draw(Graphics2D g2) {
    this.g2 = g2;

    switch (sceneNum) {
      case skeletonLord:
        scene_skeletonLord();
        break;
      default:
        // nenhuma cena ativa
        break;
    }
  }

  public void scene_skeletonLord() {

    if (scenePhase == 0) {

      gp.bossBattleOn = true;

      // Fechar a porta de ferro
      for (int i = 0; i < gp.obj[gp.currentMap].length; i++) {
        if (gp.obj[gp.currentMap][i] == null) {
          gp.obj[gp.currentMap][i] = new OBJ_Door_Iron(gp);
          gp.obj[gp.currentMap][i].setWorldX(gp.tileSize * 25);
          gp.obj[gp.currentMap][i].setWorldY(gp.tileSize * 28);
          gp.obj[gp.currentMap][i].setTemp(true);
          gp.playSE(21);
          break;
        }
      }

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

      gp.player.setDrawing(false);
      scenePhase++;
    }

    if (scenePhase == 1) {
      gp.player.setWorldY(gp.player.getWorldY() - 2);

      if (gp.player.getWorldY() < gp.tileSize * 16) {
        scenePhase++;
      }
    }

    if (scenePhase == 2) {
      // Procurar o boss
      for (int i = 0; i < gp.monster[gp.currentMap].length; i++) {
        if (gp.monster[gp.currentMap][i] != null
            && MON_SkeletonLord.monName.equals(gp.monster[gp.currentMap][i].getName())) {

          gp.monster[gp.currentMap][i].setSleep(false);
          gp.ui.npc = gp.monster[gp.currentMap][i];
          scenePhase++;
          break;
        }
      }
    }

    if (scenePhase == 3) {
      // O boss fala
      gp.ui.drawDialogueScreen();
    }

    if (scenePhase == 4) {
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

      // Voltar a desenhar o player
      gp.player.setDrawing(true);

      // Reset
      sceneNum = NA;
      scenePhase = 0;
      gp.gameState = gp.playState;

      // Trocar música
      gp.stopMusic();
      gp.playMusic(22);
    }
  }
}

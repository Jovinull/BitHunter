package main;

import java.awt.Graphics2D;

import entity.PlayerDummy;
import entity.RemotePlayer;
import monster.MonSkeletonLord;
import network.packets.EventPacket;
import object.ObjDoorIron;

public class CutsceneManager {

  private final GamePanel gp;

  private int sceneNum;
  private int scenePhase;

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

  private void sceneSkeletonLord() {
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
    if (player.getWorldY() < GamePanel.TILE_SIZE * 40) {
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

    // Notifica fim da cutscene aos jogadores próximos
    broadcastCutsceneToNearby(NA);

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
        objects[currentMap][i].setWorldX(GamePanel.TILE_SIZE * 50);
        objects[currentMap][i].setWorldY(GamePanel.TILE_SIZE * 53);
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
        player.snapPetToSelf();
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
    broadcastCutsceneToNearby(SKELETON_LORD);
  }

  /**
   * Broadcast EVT_CUTSCENE para jogadores remotos no mesmo mapa e próximos.
   * {@code sceneId} = número da cena (0 = fim da cutscene).
   */
  private void broadcastCutsceneToNearby(int sceneId) {
    if (!gp.getNetworkManager().isMultiplayer() || !gp.getNetworkManager().isHost()) {
      return;
    }
    int px = gp.getPlayer().getWorldX();
    int py = gp.getPlayer().getWorldY();
    int threshold = GamePanel.TILE_SIZE * 12; // ~12 tiles de raio
    int threshSq = threshold * threshold;
    for (RemotePlayer rp : gp.getRemotePlayers()) {
      int dx = rp.getWorldX() - px;
      int dy = rp.getWorldY() - py;
      if (dx * dx + dy * dy <= threshSq) {
        // Está perto — envia evento de cutscene
        gp.getNetworkManager().broadcast(new EventPacket(
            EventPacket.EVT_CUTSCENE, 0, rp.getNetworkId(),
            sceneId, (byte) gp.getCurrentMap()));
      }
    }
  }
}

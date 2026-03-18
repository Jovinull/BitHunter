package main;

import data.Progress;
import entity.Entity;
import entity.NpcBigRock;
import entity.NpcMerchant;
import entity.NpcOldMan;
import entity.NpcPetYardKeeper;
import monster.MonBat;
import monster.MonGreenSlime;
import monster.MonOrc;
import monster.MonRedSlime;
import monster.MonSkeletonLord;
import object.ObjAxe;
import object.ObjChest;
import object.ObjChestSecret;
import object.ObjCoinBronze;
import object.ObjDoor;
import object.ObjDoorIron;
import object.ObjHeart;
import object.ObjKey;
import object.ObjManaCrystal;
import object.ObjPickaxe;
import object.ObjPotionRed;
import object.ObjShieldBlue;
import object.ObjShieldJava;
import object.ObjTent;
import java.util.function.Supplier;
import tile_interactive.ItDestructibleWall;
import tile_interactive.ItDryTree;
import tile_interactive.InteractiveTile;
import tile_interactive.ItMetalPlate;

public class AssetSetter {

  private static final int MAP_WORLD = 0;
  private static final int MAP_HOUSE = 1;
  private static final int MAP_DUNGEON = 2;
  private static final int MAP_BOSS_ROOM = 3;
  private static final int MAP_INDOOR2 = 4;
  private static final int[][] WORLD_GREEN_SLIME_POSITIONS = {
      {45, 78}, {50, 82}, {55, 79}, {48, 85}, {52, 76},
      {42, 30}, {39, 27}, {42, 24}, {45, 26}, {44, 20}, {38, 17}, {31, 11},
      {35, 9}, {40, 10}, {45, 12}, {55, 16}, {56, 20}, {54, 22}, {60, 20}, {59, 25},
      {20, 40}, {18, 36}, {12, 34}, {9, 27}, {10, 20}, {11, 12}
  };
  private static final int[][] WORLD_ORC_POSITIONS = {
      {25, 60}, {17, 92}, {9, 88}, {10, 80}, {9, 73}, {7, 65}
  };
  private static final int[][] WORLD_RED_SLIME_POSITIONS = {
      {78, 22}, {80, 20}, {82, 22}, {82, 38}, {85, 33}, {90, 40}, {91, 30}, {90, 18},
      {82, 26}, {17, 18}, {20, 5}, {22, 11}, {16, 10}, {14, 14}, {25, 14}, {38, 71},
      {46, 77}, {41, 83}
  };
  private static final int[][] DUNGEON_BAT_POSITIONS = {
      {38, 46}, {58, 58}, {16, 52}, {38, 20}, {58, 28}
  };
  private static final int[][] WORLD_DRY_TREE_POSITIONS = {
      {73, 40}, {77, 40}, {28, 15}, {12, 30}, {70, 28}, {48, 70}, {52, 70},
      {50, 9}, {11, 65}, {10, 70}, {16, 64}, {14, 74}, {18, 81}, {19, 88}
  };
  private static final int[][] DUNGEON_DESTRUCTIBLE_WALL_POSITIONS = {
      {34, 48}, {34, 49}, {34, 50}, {18, 38}, {18, 39}, {60, 38}, {60, 39}, {22, 72},
      {22, 73}, {42, 22}, {42, 23}, {42, 24}, {30, 62}, {48, 46}, {48, 47}, {48, 48}
  };
  private static final int[][] DUNGEON_METAL_PLATE_POSITIONS = {
      {20, 24}, {14, 55}, {56, 54}
  };

  private final GamePanel gp;

  public AssetSetter(GamePanel gp) {
    this.gp = gp;
  }

  public void setObject() {
    var objects = gp.getObj();

    int mapNum = MAP_WORLD;
    int i = 0;

    // Moedas ao longo dos caminhos
    objects[mapNum][i] = new ObjCoinBronze(gp);
    place(objects[mapNum][i++], 50, 60);

    objects[mapNum][i] = new ObjCoinBronze(gp);
    place(objects[mapNum][i++], 45, 55);

    // Chave escondida na floresta NE
    objects[mapNum][i] = new ObjKey(gp);
    place(objects[mapNum][i++], 80, 22);

    // Machado na clareira da floresta
    objects[mapNum][i] = new ObjAxe(gp);
    place(objects[mapNum][i++], 82, 24);

    // Escudo perto do lago NW
    objects[mapNum][i] = new ObjShieldBlue(gp);
    place(objects[mapNum][i++], 30, 32);

    // Tenda na vila central
    objects[mapNum][i] = new ObjTent(gp);
    place(objects[mapNum][i++], 46, 46);

    // Poção no caminho sul
    objects[mapNum][i] = new ObjPotionRed(gp);
    place(objects[mapNum][i++], 50, 75);

    // Coração na área de combate sul
    objects[mapNum][i] = new ObjHeart(gp);
    place(objects[mapNum][i++], 50, 80);

    // Cristal de mana no caminho oeste
    objects[mapNum][i] = new ObjManaCrystal(gp);
    place(objects[mapNum][i++], 30, 42);

    // Porta da entrada da dungeon
    objects[mapNum][i] = new ObjDoor(gp);
    place(objects[mapNum][i++], 80, 17);

    // Baú perto do lago com chave
    objects[mapNum][i] = new ObjChest(gp);
    objects[mapNum][i].setLoot(new ObjKey(gp));
    place(objects[mapNum][i], 20, 28);

    // MAP DUNGEON
    mapNum = MAP_DUNGEON;
    i = 0;

    // Baú com picareta na sala leste
    objects[mapNum][i] = new ObjChest(gp);
    objects[mapNum][i].setLoot(new ObjPickaxe(gp));
    place(objects[mapNum][i++], 60, 56);

    // Baú com poção na sala oeste
    objects[mapNum][i] = new ObjChest(gp);
    objects[mapNum][i].setLoot(new ObjPotionRed(gp));
    place(objects[mapNum][i++], 12, 54);

    // Baú com poção no hub sul
    objects[mapNum][i] = new ObjChest(gp);
    objects[mapNum][i].setLoot(new ObjPotionRed(gp));
    place(objects[mapNum][i++], 25, 77);

    // Baú com poção na câmara norte
    objects[mapNum][i] = new ObjChest(gp);
    objects[mapNum][i].setLoot(new ObjPotionRed(gp));
    place(objects[mapNum][i++], 38, 18);

    // Porta de ferro bloqueando acesso ao boss
    objects[mapNum][i] = new ObjDoorIron(gp);
    place(objects[mapNum][i++], 25, 15);

    // Baú secreto com o Escudo do Compilador (Java) — requer moedas para abrir
    objects[mapNum][i] = new ObjChestSecret(gp);
    objects[mapNum][i].setLoot(new ObjShieldJava(gp));
    place(objects[mapNum][i], 16, 26);

    // MAP BOSS ROOM
    mapNum = MAP_BOSS_ROOM;
    i = 0;

    // Porta de ferro na entrada da arena
    objects[mapNum][i] = new ObjDoorIron(gp);
    place(objects[mapNum][i], 50, 52);
  }

  public void setNPC() {
    var npcs = gp.getNpc();

    int mapNum = MAP_WORLD;
    int i = 0;

    // MAP WORLD - NPCs na vila central
    npcs[mapNum][i] = new NpcOldMan(gp);
    place(npcs[mapNum][i], 48, 48);

    // MAP HOUSE
    mapNum = MAP_HOUSE;
    i = 0;

    npcs[mapNum][i] = new NpcMerchant(gp);
    place(npcs[mapNum][i], 12, 6);

    // MAP INDOOR2 - Pet Yard
    mapNum = MAP_INDOOR2;
    i = 0;

    npcs[mapNum][i] = new NpcPetYardKeeper(gp);
    place(npcs[mapNum][i], 53, 53);

    // MAP DUNGEON - Pedras-obstáculo nas salas
    mapNum = MAP_DUNGEON;
    i = 0;

    npcs[mapNum][i] = new NpcBigRock(gp);
    place(npcs[mapNum][i++], 25, 72);

    npcs[mapNum][i] = new NpcBigRock(gp);
    place(npcs[mapNum][i++], 38, 52);

    npcs[mapNum][i] = new NpcBigRock(gp);
    place(npcs[mapNum][i], 16, 28);
  }

  public void setMonster() {
    var monsters = gp.getMonster();

    // MAP WORLD
    int mapNum = MAP_WORLD;
    int i = 0;

    i = placeMonsterBatch(monsters[mapNum], i, WORLD_GREEN_SLIME_POSITIONS, () -> new MonGreenSlime(gp));
    i = placeMonsterBatch(monsters[mapNum], i, WORLD_ORC_POSITIONS, () -> new MonOrc(gp));
    placeMonsterBatch(monsters[mapNum], i, WORLD_RED_SLIME_POSITIONS, () -> new MonRedSlime(gp));

    // MAP HOUSE (sem monstros - dentro de casa)

    // MAP DUNGEON
    mapNum = MAP_DUNGEON;
    i = 0;

    placeMonsterBatch(monsters[mapNum], i, DUNGEON_BAT_POSITIONS, () -> new MonBat(gp));

    // MAP BOSS ROOM
    mapNum = MAP_BOSS_ROOM;
    i = 0;

    if (!Progress.isSkeletonLordDefeated()) {
      monsters[mapNum][i] = new MonSkeletonLord(gp);
      place(monsters[mapNum][i], 49, 38);
    }
  }

  /**
   * Reseta o estado dos monstros já instanciados (vida, posição, flags)
   * sem recriar objetos. Evita alocação e carregamento de sprites na transição.
   * O boss é tratado de forma especial: removido se já derrotado.
   */
  public void resetMonsters() {
    var monsters = gp.getMonster();

    for (int mapNum = 0; mapNum < GamePanel.MAX_MAP; mapNum++) {
      for (int i = 0; i < monsters[mapNum].length; i++) {
        Entity mon = monsters[mapNum][i];
        if (mon == null) {
          continue;
        }

        // Boss derrotado: manter slot como null
        if (mon.isBoss() && Progress.isSkeletonLordDefeated()) {
          monsters[mapNum][i] = null;
          continue;
        }

        // Reseta ao estado original de spawn
        mon.setLife(mon.getMaxLife());
        mon.setAlive(true);
        mon.setDying(false);
        mon.setInvincible(false);
        mon.setTransparent(false);
        mon.setKnockBack(false);
        mon.setKnockBackDirection(null);
        mon.setCollisionOn(false);
        mon.setOnPath(false);
        mon.setSpeed(mon.getDefaultSpeed());
        mon.setDropHandled(false);
        mon.setDrawing(true);
        mon.resetCounter();

        // Boss: manter dormindo até o jogador ativar
        if (mon.isBoss()) {
          mon.setSleep(true);
          mon.setInRage(false);
        }

        // Reposiciona no ponto de spawn original
        mon.setSpawnPoint(mon.getSpawnWorldX(), mon.getSpawnWorldY());
        mon.setWorldX(mon.getSpawnWorldX());
        mon.setWorldY(mon.getSpawnWorldY());
      }
    }
  }

  public void setInteractiveTile() {
    var interactiveTiles = gp.getiTile();

    int mapNum = MAP_WORLD;
    int i = 0;

    placeInteractiveTileBatch(interactiveTiles[mapNum], i, WORLD_DRY_TREE_POSITIONS,
        (col, row) -> new ItDryTree(gp, col, row));

    // MAP DUNGEON
    mapNum = MAP_DUNGEON;
    i = 0;

    i = placeInteractiveTileBatch(interactiveTiles[mapNum], i, DUNGEON_DESTRUCTIBLE_WALL_POSITIONS,
        (col, row) -> new ItDestructibleWall(gp, col, row));
    placeInteractiveTileBatch(interactiveTiles[mapNum], i, DUNGEON_METAL_PLATE_POSITIONS,
        (col, row) -> new ItMetalPlate(gp, col, row));
  }

  /** Posiciona uma entidade em (col,row) -> (worldX, worldY) e registra spawn. */
  private void place(Entity entity, int col, int row) {
    int worldX = GamePanel.TILE_SIZE * col;
    int worldY = GamePanel.TILE_SIZE * row;

    entity.setWorldX(worldX);
    entity.setWorldY(worldY);

    // Ponto padrão de respawn = ponto onde a entidade nasce
    entity.setSpawnPoint(worldX, worldY);
  }

  private int placeMonsterBatch(Entity[] mapMonsters, int startIndex, int[][] positions,
      Supplier<Entity> factory) {
    int i = startIndex;
    for (int[] pos : positions) {
      mapMonsters[i] = factory.get();
      place(mapMonsters[i], pos[0], pos[1]);
      i++;
    }
    return i;
  }

  private int placeInteractiveTileBatch(
      InteractiveTile[] mapTiles,
      int startIndex,
      int[][] positions,
      InteractiveTileFactory factory) {
    int i = startIndex;
    for (int[] pos : positions) {
      mapTiles[i++] = factory.create(pos[0], pos[1]);
    }
    return i;
  }

  @FunctionalInterface
  private interface InteractiveTileFactory {
    InteractiveTile create(int col, int row);
  }
}

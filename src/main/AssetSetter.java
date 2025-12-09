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
import object.ObjCoinBronze;
import object.ObjDoor;
import object.ObjDoorIron;
import object.ObjHeart;
import object.ObjKey;
import object.ObjManaCrystal;
import object.ObjPickaxe;
import object.ObjPotionRed;
import object.ObjShieldBlue;
import object.ObjTent;
import tile_interactive.ItDestructibleWall;
import tile_interactive.ItDryTree;
import tile_interactive.ItMetalPlate;

public class AssetSetter {

  private static final int MAP_WORLD = 0;
  private static final int MAP_HOUSE = 1;
  private static final int MAP_DUNGEON = 2;
  private static final int MAP_BOSS_ROOM = 3;

  private final GamePanel gp;

  public AssetSetter(GamePanel gp) {
    this.gp = gp;
  }

  public void setObject() {
    var objects = gp.getObj();

    int mapNum = MAP_WORLD;
    int i = 0;

    objects[mapNum][i] = new ObjCoinBronze(gp);
    place(objects[mapNum][i++], 25, 23);

    objects[mapNum][i] = new ObjCoinBronze(gp);
    place(objects[mapNum][i++], 21, 19);

    objects[mapNum][i] = new ObjKey(gp);
    place(objects[mapNum][i++], 26, 21);

    objects[mapNum][i] = new ObjAxe(gp);
    place(objects[mapNum][i++], 33, 21);

    objects[mapNum][i] = new ObjShieldBlue(gp);
    place(objects[mapNum][i++], 35, 21);

    objects[mapNum][i] = new ObjTent(gp);
    place(objects[mapNum][i++], 19, 20);

    objects[mapNum][i] = new ObjPotionRed(gp);
    place(objects[mapNum][i++], 22, 27);

    objects[mapNum][i] = new ObjHeart(gp);
    place(objects[mapNum][i++], 22, 29);

    objects[mapNum][i] = new ObjManaCrystal(gp);
    place(objects[mapNum][i++], 22, 31);

    objects[mapNum][i] = new ObjDoor(gp);
    place(objects[mapNum][i++], 14, 28);

    objects[mapNum][i] = new ObjDoor(gp);
    place(objects[mapNum][i++], 12, 12);

    objects[mapNum][i] = new ObjChest(gp);
    objects[mapNum][i].setLoot(new ObjKey(gp));
    place(objects[mapNum][i], 12, 8);

    // MAP DUNGEON
    mapNum = MAP_DUNGEON;
    i = 0;

    objects[mapNum][i] = new ObjChest(gp);
    objects[mapNum][i].setLoot(new ObjPickaxe(gp));
    place(objects[mapNum][i++], 40, 41);

    objects[mapNum][i] = new ObjChest(gp);
    objects[mapNum][i].setLoot(new ObjPotionRed(gp));
    place(objects[mapNum][i++], 13, 16);

    objects[mapNum][i] = new ObjChest(gp);
    objects[mapNum][i].setLoot(new ObjPotionRed(gp));
    place(objects[mapNum][i++], 26, 34);

    objects[mapNum][i] = new ObjChest(gp);
    objects[mapNum][i].setLoot(new ObjPotionRed(gp));
    place(objects[mapNum][i++], 27, 15);

    objects[mapNum][i] = new ObjDoorIron(gp);
    place(objects[mapNum][i], 18, 23);

    // MAP BOSS ROOM
    mapNum = MAP_BOSS_ROOM;
    i = 0;

    objects[mapNum][i] = new ObjDoorIron(gp);
    place(objects[mapNum][i], 25, 15);
  }

  public void setNPC() {
    var npcs = gp.getNpc();

    int mapNum = MAP_WORLD;
    int i = 0;

    // MAP WORLD
    npcs[mapNum][i] = new NpcOldMan(gp);
    place(npcs[mapNum][i++], 21, 21);

    npcs[mapNum][i] = new NpcPetYardKeeper(gp);
    place(npcs[mapNum][i], 22, 25);

    // MAP HOUSE
    mapNum = MAP_HOUSE;
    i = 0;

    npcs[mapNum][i] = new NpcMerchant(gp);
    place(npcs[mapNum][i], 12, 7);

    // MAP DUNGEON
    mapNum = MAP_DUNGEON;
    i = 0;

    npcs[mapNum][i] = new NpcBigRock(gp);
    place(npcs[mapNum][i++], 20, 25);

    npcs[mapNum][i] = new NpcBigRock(gp);
    place(npcs[mapNum][i++], 11, 18);

    npcs[mapNum][i] = new NpcBigRock(gp);
    place(npcs[mapNum][i], 23, 14);
  }

  public void setMonster() {
    var monsters = gp.getMonster();

    // MAP WORLD
    int mapNum = MAP_WORLD;
    int i = 0;

    monsters[mapNum][i] = new MonGreenSlime(gp);
    place(monsters[mapNum][i++], 21, 38);

    monsters[mapNum][i] = new MonGreenSlime(gp);
    place(monsters[mapNum][i++], 23, 42);

    monsters[mapNum][i] = new MonGreenSlime(gp);
    place(monsters[mapNum][i++], 24, 37);

    monsters[mapNum][i] = new MonGreenSlime(gp);
    place(monsters[mapNum][i++], 34, 42);

    monsters[mapNum][i] = new MonGreenSlime(gp);
    place(monsters[mapNum][i++], 38, 42);

    monsters[mapNum][i] = new MonOrc(gp);
    place(monsters[mapNum][i++], 12, 33);

    monsters[mapNum][i] = new MonRedSlime(gp);
    place(monsters[mapNum][i++], 35, 9);

    monsters[mapNum][i] = new MonRedSlime(gp);
    place(monsters[mapNum][i++], 37, 9);

    monsters[mapNum][i] = new MonRedSlime(gp);
    place(monsters[mapNum][i], 39, 9);

    // MAP HOUSE
    mapNum = MAP_HOUSE;
    i = 0;

    monsters[mapNum][i] = new MonGreenSlime(gp);
    place(monsters[mapNum][i], 38, 42);

    // MAP DUNGEON
    mapNum = MAP_DUNGEON;
    i = 0;

    monsters[mapNum][i] = new MonBat(gp);
    place(monsters[mapNum][i++], 34, 39);

    monsters[mapNum][i] = new MonBat(gp);
    place(monsters[mapNum][i++], 36, 25);

    monsters[mapNum][i] = new MonBat(gp);
    place(monsters[mapNum][i++], 39, 26);

    monsters[mapNum][i] = new MonBat(gp);
    place(monsters[mapNum][i++], 28, 11);

    monsters[mapNum][i] = new MonBat(gp);
    place(monsters[mapNum][i], 10, 19);

    // MAP BOSS ROOM
    mapNum = MAP_BOSS_ROOM;
    i = 0;

    if (!Progress.isSkeletonLordDefeated()) {
      monsters[mapNum][i] = new MonSkeletonLord(gp);
      place(monsters[mapNum][i], 23, 16);
    }
  }

  public void setInteractiveTile() {
    var interactiveTiles = gp.getiTile();

    int mapNum = MAP_WORLD;
    int i = 0;

    interactiveTiles[mapNum][i++] = new ItDryTree(gp, 27, 12);
    interactiveTiles[mapNum][i++] = new ItDryTree(gp, 28, 12);
    interactiveTiles[mapNum][i++] = new ItDryTree(gp, 29, 12);
    interactiveTiles[mapNum][i++] = new ItDryTree(gp, 30, 12);
    interactiveTiles[mapNum][i++] = new ItDryTree(gp, 31, 12);
    interactiveTiles[mapNum][i++] = new ItDryTree(gp, 32, 12);
    interactiveTiles[mapNum][i++] = new ItDryTree(gp, 33, 12);
    interactiveTiles[mapNum][i++] = new ItDryTree(gp, 31, 21);
    interactiveTiles[mapNum][i++] = new ItDryTree(gp, 17, 40);
    interactiveTiles[mapNum][i] = new ItDryTree(gp, 12, 41);

    // MAP DUNGEON
    mapNum = MAP_DUNGEON;
    i = 0;

    interactiveTiles[mapNum][i++] = new ItDestructibleWall(gp, 18, 30);
    interactiveTiles[mapNum][i++] = new ItDestructibleWall(gp, 17, 31);
    interactiveTiles[mapNum][i++] = new ItDestructibleWall(gp, 17, 32);
    interactiveTiles[mapNum][i++] = new ItDestructibleWall(gp, 17, 34);
    interactiveTiles[mapNum][i++] = new ItDestructibleWall(gp, 18, 34);
    interactiveTiles[mapNum][i++] = new ItDestructibleWall(gp, 18, 33);
    interactiveTiles[mapNum][i++] = new ItDestructibleWall(gp, 10, 22);
    interactiveTiles[mapNum][i++] = new ItDestructibleWall(gp, 10, 24);
    interactiveTiles[mapNum][i++] = new ItDestructibleWall(gp, 38, 18);
    interactiveTiles[mapNum][i++] = new ItDestructibleWall(gp, 38, 19);
    interactiveTiles[mapNum][i++] = new ItDestructibleWall(gp, 38, 20);
    interactiveTiles[mapNum][i++] = new ItDestructibleWall(gp, 38, 21);
    interactiveTiles[mapNum][i++] = new ItDestructibleWall(gp, 11, 34);
    interactiveTiles[mapNum][i++] = new ItDestructibleWall(gp, 22, 28);
    interactiveTiles[mapNum][i++] = new ItDestructibleWall(gp, 30, 28);
    interactiveTiles[mapNum][i++] = new ItDestructibleWall(gp, 32, 28);

    interactiveTiles[mapNum][i++] = new ItMetalPlate(gp, 20, 22);
    interactiveTiles[mapNum][i++] = new ItMetalPlate(gp, 8, 17);
    interactiveTiles[mapNum][i] = new ItMetalPlate(gp, 39, 31);
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
}

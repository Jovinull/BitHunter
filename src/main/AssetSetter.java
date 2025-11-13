package main;

import data.Progress;
import entity.Entity;
import entity.NpcBigRock;
import entity.NpcMerchant;
import entity.NpcOldMan;
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
    int mapNum = MAP_WORLD;
    int i = 0;

    gp.obj[mapNum][i] = new ObjCoinBronze(gp);
    place(gp.obj[mapNum][i++], 25, 23);

    gp.obj[mapNum][i] = new ObjCoinBronze(gp);
    place(gp.obj[mapNum][i++], 21, 19);

    gp.obj[mapNum][i] = new ObjKey(gp);
    place(gp.obj[mapNum][i++], 26, 21);

    gp.obj[mapNum][i] = new ObjAxe(gp);
    place(gp.obj[mapNum][i++], 33, 21);

    gp.obj[mapNum][i] = new ObjShieldBlue(gp);
    place(gp.obj[mapNum][i++], 35, 21);

    gp.obj[mapNum][i] = new ObjTent(gp);
    place(gp.obj[mapNum][i++], 19, 20);

    gp.obj[mapNum][i] = new ObjPotionRed(gp);
    place(gp.obj[mapNum][i++], 22, 27);

    gp.obj[mapNum][i] = new ObjHeart(gp);
    place(gp.obj[mapNum][i++], 22, 29);

    gp.obj[mapNum][i] = new ObjManaCrystal(gp);
    place(gp.obj[mapNum][i++], 22, 31);

    gp.obj[mapNum][i] = new ObjDoor(gp);
    place(gp.obj[mapNum][i++], 14, 28);

    gp.obj[mapNum][i] = new ObjDoor(gp);
    place(gp.obj[mapNum][i++], 12, 12);

    gp.obj[mapNum][i] = new ObjChest(gp);
    gp.obj[mapNum][i].setLoot(new ObjKey(gp));
    place(gp.obj[mapNum][i], 12, 8);

    // MAP DUNGEON
    mapNum = MAP_DUNGEON;
    i = 0;

    gp.obj[mapNum][i] = new ObjChest(gp);
    gp.obj[mapNum][i].setLoot(new ObjPickaxe(gp));
    place(gp.obj[mapNum][i++], 40, 41);

    gp.obj[mapNum][i] = new ObjChest(gp);
    gp.obj[mapNum][i].setLoot(new ObjPotionRed(gp));
    place(gp.obj[mapNum][i++], 13, 16);

    gp.obj[mapNum][i] = new ObjChest(gp);
    gp.obj[mapNum][i].setLoot(new ObjPotionRed(gp));
    place(gp.obj[mapNum][i++], 26, 34);

    gp.obj[mapNum][i] = new ObjChest(gp);
    gp.obj[mapNum][i].setLoot(new ObjPotionRed(gp));
    place(gp.obj[mapNum][i++], 27, 15);

    gp.obj[mapNum][i] = new ObjDoorIron(gp);
    place(gp.obj[mapNum][i], 18, 23);

    // MAP BOSS ROOM
    mapNum = MAP_BOSS_ROOM;
    i = 0;

    gp.obj[mapNum][i] = new ObjDoorIron(gp);
    place(gp.obj[mapNum][i], 25, 15);
  }

  public void setNPC() {
    int mapNum = MAP_WORLD;
    int i = 0;

    // MAP WORLD
    gp.npc[mapNum][i] = new NpcOldMan(gp);
    place(gp.npc[mapNum][i], 21, 21);

    // MAP HOUSE
    mapNum = MAP_HOUSE;
    i = 0;
    gp.npc[mapNum][i] = new NpcMerchant(gp);
    place(gp.npc[mapNum][i], 12, 7);

    // MAP DUNGEON
    mapNum = MAP_DUNGEON;
    i = 0;
    gp.npc[mapNum][i] = new NpcBigRock(gp);
    place(gp.npc[mapNum][i++], 20, 25);

    gp.npc[mapNum][i] = new NpcBigRock(gp);
    place(gp.npc[mapNum][i++], 11, 18);

    gp.npc[mapNum][i] = new NpcBigRock(gp);
    place(gp.npc[mapNum][i], 23, 14);
  }

  public void setMonster() {
    // MAP WORLD
    int mapNum = MAP_WORLD;
    int i = 0;

    gp.monster[mapNum][i] = new MonGreenSlime(gp);
    place(gp.monster[mapNum][i++], 21, 38);

    gp.monster[mapNum][i] = new MonGreenSlime(gp);
    place(gp.monster[mapNum][i++], 23, 42);

    gp.monster[mapNum][i] = new MonGreenSlime(gp);
    place(gp.monster[mapNum][i++], 24, 37);

    gp.monster[mapNum][i] = new MonGreenSlime(gp);
    place(gp.monster[mapNum][i++], 34, 42);

    gp.monster[mapNum][i] = new MonGreenSlime(gp);
    place(gp.monster[mapNum][i++], 38, 42);

    gp.monster[mapNum][i] = new MonOrc(gp);
    place(gp.monster[mapNum][i++], 12, 33);

    gp.monster[mapNum][i] = new MonRedSlime(gp);
    place(gp.monster[mapNum][i++], 35, 9);

    gp.monster[mapNum][i] = new MonRedSlime(gp);
    place(gp.monster[mapNum][i++], 37, 9);

    gp.monster[mapNum][i] = new MonRedSlime(gp);
    place(gp.monster[mapNum][i], 39, 9);

    // MAP HOUSE
    mapNum = MAP_HOUSE;
    i = 0;
    gp.monster[mapNum][i] = new MonGreenSlime(gp);
    place(gp.monster[mapNum][i], 38, 42);

    // MAP DUNGEON
    mapNum = MAP_DUNGEON;
    i = 0;
    gp.monster[mapNum][i] = new MonBat(gp);
    place(gp.monster[mapNum][i++], 34, 39);

    gp.monster[mapNum][i] = new MonBat(gp);
    place(gp.monster[mapNum][i++], 36, 25);

    gp.monster[mapNum][i] = new MonBat(gp);
    place(gp.monster[mapNum][i++], 39, 26);

    gp.monster[mapNum][i] = new MonBat(gp);
    place(gp.monster[mapNum][i++], 28, 11);

    gp.monster[mapNum][i] = new MonBat(gp);
    place(gp.monster[mapNum][i], 10, 19);

    // MAP BOSS ROOM
    mapNum = MAP_BOSS_ROOM;
    i = 0;
    if (!Progress.isSkeletonLordDefeated()) {
      gp.monster[mapNum][i] = new MonSkeletonLord(gp);
      place(gp.monster[mapNum][i], 23, 16);
    }
  }

  public void setInteractiveTile() {
    int mapNum = MAP_WORLD;
    int i = 0;

    gp.iTile[mapNum][i++] = new ItDryTree(gp, 27, 12);
    gp.iTile[mapNum][i++] = new ItDryTree(gp, 28, 12);
    gp.iTile[mapNum][i++] = new ItDryTree(gp, 29, 12);
    gp.iTile[mapNum][i++] = new ItDryTree(gp, 30, 12);
    gp.iTile[mapNum][i++] = new ItDryTree(gp, 31, 12);
    gp.iTile[mapNum][i++] = new ItDryTree(gp, 32, 12);
    gp.iTile[mapNum][i++] = new ItDryTree(gp, 33, 12);
    gp.iTile[mapNum][i++] = new ItDryTree(gp, 31, 21);
    gp.iTile[mapNum][i++] = new ItDryTree(gp, 17, 40);
    gp.iTile[mapNum][i] = new ItDryTree(gp, 12, 41);

    // MAP DUNGEON
    mapNum = MAP_DUNGEON;
    i = 0;

    gp.iTile[mapNum][i++] = new ItDestructibleWall(gp, 18, 30);
    gp.iTile[mapNum][i++] = new ItDestructibleWall(gp, 17, 31);
    gp.iTile[mapNum][i++] = new ItDestructibleWall(gp, 17, 32);
    gp.iTile[mapNum][i++] = new ItDestructibleWall(gp, 17, 34);
    gp.iTile[mapNum][i++] = new ItDestructibleWall(gp, 18, 34);
    gp.iTile[mapNum][i++] = new ItDestructibleWall(gp, 18, 33);
    gp.iTile[mapNum][i++] = new ItDestructibleWall(gp, 10, 22);
    gp.iTile[mapNum][i++] = new ItDestructibleWall(gp, 10, 24);
    gp.iTile[mapNum][i++] = new ItDestructibleWall(gp, 38, 18);
    gp.iTile[mapNum][i++] = new ItDestructibleWall(gp, 38, 19);
    gp.iTile[mapNum][i++] = new ItDestructibleWall(gp, 38, 20);
    gp.iTile[mapNum][i++] = new ItDestructibleWall(gp, 38, 21);
    gp.iTile[mapNum][i++] = new ItDestructibleWall(gp, 11, 34);
    gp.iTile[mapNum][i++] = new ItDestructibleWall(gp, 22, 28);
    gp.iTile[mapNum][i++] = new ItDestructibleWall(gp, 30, 28);
    gp.iTile[mapNum][i++] = new ItDestructibleWall(gp, 32, 28);

    gp.iTile[mapNum][i++] = new ItMetalPlate(gp, 20, 22);
    gp.iTile[mapNum][i++] = new ItMetalPlate(gp, 8, 17);
    gp.iTile[mapNum][i] = new ItMetalPlate(gp, 39, 31);
  }

  /** Posiciona uma entidade em (col,row) -> (worldX, worldY). */
  private void place(Entity entity, int col, int row) {
    entity.setWorldX(gp.tileSize * col);
    entity.setWorldY(gp.tileSize * row);
  }
}

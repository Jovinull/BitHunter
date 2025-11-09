package main;

import data.Progress;
import entity.Entity;
import entity.NpcBigRock;
import entity.NpcMerchant;
import entity.NpcOldMan;
import monster.MON_Bat;
import monster.MON_GreenSlime;
import monster.MON_Orc;
import monster.MON_RedSlime;
import monster.MON_SkeletonLord;
import object.OBJ_Axe;
import object.OBJ_Chest;
import object.OBJ_Coin_Bronze;
import object.OBJ_Door;
import object.OBJ_Door_Iron;
import object.OBJ_Heart;
import object.OBJ_Key;
import object.OBJ_ManaCrystal;
import object.OBJ_Pickaxe;
import object.OBJ_Potion_Red;
import object.OBJ_Shield_Blue;
import object.OBJ_Tent;
import tile_interactive.IT_DestructibleWall;
import tile_interactive.IT_DryTree;
import tile_interactive.IT_MetalPlate;

public class AssetSetter {

  private final GamePanel gp;

  public AssetSetter(GamePanel gp) {
    this.gp = gp;
  }

  public void setObject() {
    int mapNum = 0;
    int i = 0;

    gp.obj[mapNum][i] = new OBJ_Coin_Bronze(gp);
    place(gp.obj[mapNum][i++], 25, 23);

    gp.obj[mapNum][i] = new OBJ_Coin_Bronze(gp);
    place(gp.obj[mapNum][i++], 21, 19);

    gp.obj[mapNum][i] = new OBJ_Key(gp);
    place(gp.obj[mapNum][i++], 26, 21);

    gp.obj[mapNum][i] = new OBJ_Axe(gp);
    place(gp.obj[mapNum][i++], 33, 21);

    gp.obj[mapNum][i] = new OBJ_Shield_Blue(gp);
    place(gp.obj[mapNum][i++], 35, 21);

    gp.obj[mapNum][i] = new OBJ_Tent(gp);
    place(gp.obj[mapNum][i++], 19, 20);

    gp.obj[mapNum][i] = new OBJ_Potion_Red(gp);
    place(gp.obj[mapNum][i++], 22, 27);

    gp.obj[mapNum][i] = new OBJ_Heart(gp);
    place(gp.obj[mapNum][i++], 22, 29);

    gp.obj[mapNum][i] = new OBJ_ManaCrystal(gp);
    place(gp.obj[mapNum][i++], 22, 31);

    gp.obj[mapNum][i] = new OBJ_Door(gp);
    place(gp.obj[mapNum][i++], 14, 28);

    gp.obj[mapNum][i] = new OBJ_Door(gp);
    place(gp.obj[mapNum][i++], 12, 12);

    gp.obj[mapNum][i] = new OBJ_Chest(gp);
    gp.obj[mapNum][i].setLoot(new OBJ_Key(gp));
    place(gp.obj[mapNum][i], 12, 8);

    // MAP 2
    mapNum = 2;
    i = 0;

    gp.obj[mapNum][i] = new OBJ_Chest(gp);
    gp.obj[mapNum][i].setLoot(new OBJ_Pickaxe(gp));
    place(gp.obj[mapNum][i++], 40, 41);

    gp.obj[mapNum][i] = new OBJ_Chest(gp);
    gp.obj[mapNum][i].setLoot(new OBJ_Potion_Red(gp));
    place(gp.obj[mapNum][i++], 13, 16);

    gp.obj[mapNum][i] = new OBJ_Chest(gp);
    gp.obj[mapNum][i].setLoot(new OBJ_Potion_Red(gp));
    place(gp.obj[mapNum][i++], 26, 34);

    gp.obj[mapNum][i] = new OBJ_Chest(gp);
    gp.obj[mapNum][i].setLoot(new OBJ_Potion_Red(gp));
    place(gp.obj[mapNum][i++], 27, 15);

    gp.obj[mapNum][i] = new OBJ_Door_Iron(gp);
    place(gp.obj[mapNum][i], 18, 23);

    // MAP 3
    mapNum = 3;
    i = 0;

    gp.obj[mapNum][i] = new OBJ_Door_Iron(gp);
    place(gp.obj[mapNum][i], 25, 15);
  }

  public void setNPC() {
    int mapNum = 0;
    int i = 0;

    // MAP 0
    gp.npc[mapNum][i] = new NpcOldMan(gp);
    place(gp.npc[mapNum][i++], 21, 21);

    // MAP 1
    mapNum = 1;
    i = 0;
    gp.npc[mapNum][i] = new NpcMerchant(gp);
    place(gp.npc[mapNum][i], 12, 7);

    // MAP 2
    mapNum = 2;
    i = 0;
    gp.npc[mapNum][i] = new NpcBigRock(gp);
    place(gp.npc[mapNum][i++], 20, 25);

    gp.npc[mapNum][i] = new NpcBigRock(gp);
    place(gp.npc[mapNum][i++], 11, 18);

    gp.npc[mapNum][i] = new NpcBigRock(gp);
    place(gp.npc[mapNum][i], 23, 14);
  }

  public void setMonster() {
    // MAP 0
    int mapNum = 0;
    int i = 0;

    gp.monster[mapNum][i] = new MON_GreenSlime(gp);
    place(gp.monster[mapNum][i++], 21, 38);

    gp.monster[mapNum][i] = new MON_GreenSlime(gp);
    place(gp.monster[mapNum][i++], 23, 42);

    gp.monster[mapNum][i] = new MON_GreenSlime(gp);
    place(gp.monster[mapNum][i++], 24, 37);

    gp.monster[mapNum][i] = new MON_GreenSlime(gp);
    place(gp.monster[mapNum][i++], 34, 42);

    gp.monster[mapNum][i] = new MON_GreenSlime(gp);
    place(gp.monster[mapNum][i++], 38, 42);

    gp.monster[mapNum][i] = new MON_Orc(gp);
    place(gp.monster[mapNum][i++], 12, 33);

    gp.monster[mapNum][i] = new MON_RedSlime(gp);
    place(gp.monster[mapNum][i++], 35, 9);

    gp.monster[mapNum][i] = new MON_RedSlime(gp);
    place(gp.monster[mapNum][i++], 37, 9);

    gp.monster[mapNum][i] = new MON_RedSlime(gp);
    place(gp.monster[mapNum][i], 39, 9);

    // MAP 1
    mapNum = 1;
    i = 0;
    gp.monster[mapNum][i] = new MON_GreenSlime(gp);
    place(gp.monster[mapNum][i], 38, 42);

    // MAP 2
    mapNum = 2;
    i = 0;
    gp.monster[mapNum][i] = new MON_Bat(gp);
    place(gp.monster[mapNum][i++], 34, 39);

    gp.monster[mapNum][i] = new MON_Bat(gp);
    place(gp.monster[mapNum][i++], 36, 25);

    gp.monster[mapNum][i] = new MON_Bat(gp);
    place(gp.monster[mapNum][i++], 39, 26);

    gp.monster[mapNum][i] = new MON_Bat(gp);
    place(gp.monster[mapNum][i++], 28, 11);

    gp.monster[mapNum][i] = new MON_Bat(gp);
    place(gp.monster[mapNum][i], 10, 19);

    // MAP 3
    mapNum = 3;
    i = 0;
    if (!Progress.isSkeletonLordDefeated()) {
      gp.monster[mapNum][i] = new MON_SkeletonLord(gp);
      place(gp.monster[mapNum][i], 23, 16);
    }
  }

  public void setInteractiveTile() {
    int mapNum = 0;
    int i = 0;

    gp.iTile[mapNum][i++] = new IT_DryTree(gp, 27, 12);
    gp.iTile[mapNum][i++] = new IT_DryTree(gp, 28, 12);
    gp.iTile[mapNum][i++] = new IT_DryTree(gp, 29, 12);
    gp.iTile[mapNum][i++] = new IT_DryTree(gp, 30, 12);
    gp.iTile[mapNum][i++] = new IT_DryTree(gp, 31, 12);
    gp.iTile[mapNum][i++] = new IT_DryTree(gp, 32, 12);
    gp.iTile[mapNum][i++] = new IT_DryTree(gp, 33, 12);
    gp.iTile[mapNum][i++] = new IT_DryTree(gp, 31, 21);
    gp.iTile[mapNum][i++] = new IT_DryTree(gp, 17, 40);
    gp.iTile[mapNum][i] = new IT_DryTree(gp, 12, 41);

    // MAP 2
    mapNum = 2;
    i = 0;

    gp.iTile[mapNum][i++] = new IT_DestructibleWall(gp, 18, 30);
    gp.iTile[mapNum][i++] = new IT_DestructibleWall(gp, 17, 31);
    gp.iTile[mapNum][i++] = new IT_DestructibleWall(gp, 17, 32);
    gp.iTile[mapNum][i++] = new IT_DestructibleWall(gp, 17, 34);
    gp.iTile[mapNum][i++] = new IT_DestructibleWall(gp, 18, 34);
    gp.iTile[mapNum][i++] = new IT_DestructibleWall(gp, 18, 33);
    gp.iTile[mapNum][i++] = new IT_DestructibleWall(gp, 10, 22);
    gp.iTile[mapNum][i++] = new IT_DestructibleWall(gp, 10, 24);
    gp.iTile[mapNum][i++] = new IT_DestructibleWall(gp, 38, 18);
    gp.iTile[mapNum][i++] = new IT_DestructibleWall(gp, 38, 19);
    gp.iTile[mapNum][i++] = new IT_DestructibleWall(gp, 38, 20);
    gp.iTile[mapNum][i++] = new IT_DestructibleWall(gp, 38, 21);
    gp.iTile[mapNum][i++] = new IT_DestructibleWall(gp, 11, 34);
    gp.iTile[mapNum][i++] = new IT_DestructibleWall(gp, 22, 28);
    gp.iTile[mapNum][i++] = new IT_DestructibleWall(gp, 30, 28);
    gp.iTile[mapNum][i++] = new IT_DestructibleWall(gp, 32, 28);

    gp.iTile[mapNum][i++] = new IT_MetalPlate(gp, 20, 22);
    gp.iTile[mapNum][i++] = new IT_MetalPlate(gp, 8, 17);
    gp.iTile[mapNum][i] = new IT_MetalPlate(gp, 39, 31);
  }

  /** Posiciona uma entidade em (col,row) -> (worldX, worldY). */
  private void place(Entity e, int col, int row) {
    e.setWorldX(gp.tileSize * col);
    e.setWorldY(gp.tileSize * row);
  }
}

package main;

import data.Progress;
import entity.NPC_BigRock;
import entity.NPC_Merchant;
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
    placeEntity(mapNum, i++, 25, 23);

    gp.obj[mapNum][i] = new OBJ_Coin_Bronze(gp);
    placeEntity(mapNum, i++, 21, 19);

    gp.obj[mapNum][i] = new OBJ_Key(gp);
    placeEntity(mapNum, i++, 26, 21);

    gp.obj[mapNum][i] = new OBJ_Axe(gp);
    placeEntity(mapNum, i++, 33, 21);

    gp.obj[mapNum][i] = new OBJ_Shield_Blue(gp);
    placeEntity(mapNum, i++, 35, 21);

    gp.obj[mapNum][i] = new OBJ_Tent(gp);
    placeEntity(mapNum, i++, 19, 20);

    gp.obj[mapNum][i] = new OBJ_Potion_Red(gp);
    placeEntity(mapNum, i++, 22, 27);

    gp.obj[mapNum][i] = new OBJ_Heart(gp);
    placeEntity(mapNum, i++, 22, 29);

    gp.obj[mapNum][i] = new OBJ_ManaCrystal(gp);
    placeEntity(mapNum, i++, 22, 31);

    gp.obj[mapNum][i] = new OBJ_Door(gp);
    placeEntity(mapNum, i++, 14, 28);

    gp.obj[mapNum][i] = new OBJ_Door(gp);
    placeEntity(mapNum, i++, 12, 12);

    gp.obj[mapNum][i] = new OBJ_Chest(gp);
    gp.obj[mapNum][i].setLoot(new OBJ_Key(gp));
    placeEntity(mapNum, i, 12, 8);

    mapNum = 2;
    i = 0;

    gp.obj[mapNum][i] = new OBJ_Chest(gp);
    gp.obj[mapNum][i].setLoot(new OBJ_Pickaxe(gp));
    placeEntity(mapNum, i++, 40, 41);

    gp.obj[mapNum][i] = new OBJ_Chest(gp);
    gp.obj[mapNum][i].setLoot(new OBJ_Potion_Red(gp));
    placeEntity(mapNum, i++, 13, 16);

    gp.obj[mapNum][i] = new OBJ_Chest(gp);
    gp.obj[mapNum][i].setLoot(new OBJ_Potion_Red(gp));
    placeEntity(mapNum, i++, 26, 34);

    gp.obj[mapNum][i] = new OBJ_Chest(gp);
    gp.obj[mapNum][i].setLoot(new OBJ_Potion_Red(gp));
    placeEntity(mapNum, i++, 27, 15);

    gp.obj[mapNum][i] = new OBJ_Door_Iron(gp);
    placeEntity(mapNum, i, 18, 23);

    mapNum = 3;
    i = 0;

    gp.obj[mapNum][i] = new OBJ_Door_Iron(gp);
    placeEntity(mapNum, i, 25, 15);
  }

  public void setNPC() {
    int mapNum = 0;
    int i = 0;

    // MAP 0
    gp.npc[mapNum][i] = new NpcOldMan(gp);
    placeEntity(mapNum, i++, 21, 21);

    // MAP 1
    mapNum = 1;
    i = 0;
    gp.npc[mapNum][i] = new NPC_Merchant(gp);
    placeEntity(mapNum, i, 12, 7);

    // MAP 2
    mapNum = 2;
    i = 0;
    gp.npc[mapNum][i] = new NPC_BigRock(gp);
    placeEntity(mapNum, i++, 20, 25);

    gp.npc[mapNum][i] = new NPC_BigRock(gp);
    placeEntity(mapNum, i++, 11, 18);

    gp.npc[mapNum][i] = new NPC_BigRock(gp);
    placeEntity(mapNum, i, 23, 14);
  }

  public void setMonster() {
    int mapNum = 0;
    int i = 0;

    gp.monster[mapNum][i] = new MON_GreenSlime(gp);
    placeEntity(mapNum, i++, 21, 38);

    gp.monster[mapNum][i] = new MON_GreenSlime(gp);
    placeEntity(mapNum, i++, 23, 42);

    gp.monster[mapNum][i] = new MON_GreenSlime(gp);
    placeEntity(mapNum, i++, 24, 37);

    gp.monster[mapNum][i] = new MON_GreenSlime(gp);
    placeEntity(mapNum, i++, 34, 42);

    gp.monster[mapNum][i] = new MON_GreenSlime(gp);
    placeEntity(mapNum, i++, 38, 42);

    gp.monster[mapNum][i] = new MON_Orc(gp);
    placeEntity(mapNum, i++, 12, 33);

    gp.monster[mapNum][i] = new MON_RedSlime(gp);
    placeEntity(mapNum, i++, 35, 9);

    gp.monster[mapNum][i] = new MON_RedSlime(gp);
    placeEntity(mapNum, i++, 37, 9);

    gp.monster[mapNum][i] = new MON_RedSlime(gp);
    placeEntity(mapNum, i, 39, 9);

    mapNum = 1;
    gp.monster[mapNum][i] = new MON_GreenSlime(gp);
    placeEntity(mapNum, i, 38, 42);

    mapNum = 2;

    i++;
    gp.monster[mapNum][i] = new MON_Bat(gp);
    placeEntity(mapNum, i++, 34, 39);

    gp.monster[mapNum][i] = new MON_Bat(gp);
    placeEntity(mapNum, i++, 36, 25);

    gp.monster[mapNum][i] = new MON_Bat(gp);
    placeEntity(mapNum, i++, 39, 26);

    gp.monster[mapNum][i] = new MON_Bat(gp);
    placeEntity(mapNum, i++, 28, 11);

    gp.monster[mapNum][i] = new MON_Bat(gp);
    placeEntity(mapNum, i, 10, 19);

    mapNum = 3;

    if (!Progress.isSkeletonLordDefeated()) {
      gp.monster[mapNum][i] = new MON_SkeletonLord(gp);
      placeEntity(mapNum, i, 23, 16);
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

  /**
   * Posiciona uma entidade em col/row, convertendo para worldX/worldY via
   * tileSize.
   */
  private void placeEntity(int mapNum, int index, int col, int row) {
    var e = gp.obj.length > mapNum && gp.obj[mapNum] != null && index < gp.obj[mapNum].length ? gp.obj[mapNum][index]
        : gp.npc.length > mapNum && gp.npc[mapNum] != null && index < gp.npc[mapNum].length ? gp.npc[mapNum][index]
            : gp.monster.length > mapNum && gp.monster[mapNum] != null && index < gp.monster[mapNum].length
                ? gp.monster[mapNum][index]
                : null;

    if (e != null) {
      e.setWorldX(gp.tileSize * col);
      e.setWorldY(gp.tileSize * row);
    }
  }
}

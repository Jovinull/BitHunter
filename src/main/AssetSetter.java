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

    // Porta da casa
    objects[mapNum][i] = new ObjDoor(gp);
    place(objects[mapNum][i++], 25, 69);

    // Porta da entrada da dungeon
    objects[mapNum][i] = new ObjDoor(gp);
    place(objects[mapNum][i++], 75, 42);

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
    place(npcs[mapNum][i++], 48, 48);

    npcs[mapNum][i] = new NpcPetYardKeeper(gp);
    place(npcs[mapNum][i], 52, 52);

    // MAP HOUSE
    mapNum = MAP_HOUSE;
    i = 0;

    npcs[mapNum][i] = new NpcMerchant(gp);
    place(npcs[mapNum][i], 12, 9);

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

    // Green Slimes na área sul (campo de combate)
    monsters[mapNum][i] = new MonGreenSlime(gp);
    place(monsters[mapNum][i++], 45, 78);

    monsters[mapNum][i] = new MonGreenSlime(gp);
    place(monsters[mapNum][i++], 50, 82);

    monsters[mapNum][i] = new MonGreenSlime(gp);
    place(monsters[mapNum][i++], 55, 79);

    monsters[mapNum][i] = new MonGreenSlime(gp);
    place(monsters[mapNum][i++], 48, 85);

    monsters[mapNum][i] = new MonGreenSlime(gp);
    place(monsters[mapNum][i++], 52, 76);

    // Orc patrulhando caminho oeste
    monsters[mapNum][i] = new MonOrc(gp);
    place(monsters[mapNum][i++], 25, 60);

    // Red Slimes na clareira da floresta NE
    monsters[mapNum][i] = new MonRedSlime(gp);
    place(monsters[mapNum][i++], 78, 22);

    monsters[mapNum][i] = new MonRedSlime(gp);
    place(monsters[mapNum][i++], 80, 20);

    monsters[mapNum][i] = new MonRedSlime(gp);
    place(monsters[mapNum][i], 82, 22);

    // MAP HOUSE (sem monstros - dentro de casa)

    // MAP DUNGEON
    mapNum = MAP_DUNGEON;
    i = 0;

    // Morcegos distribuídos pelas salas
    monsters[mapNum][i] = new MonBat(gp);
    place(monsters[mapNum][i++], 38, 46);

    monsters[mapNum][i] = new MonBat(gp);
    place(monsters[mapNum][i++], 58, 58);

    monsters[mapNum][i] = new MonBat(gp);
    place(monsters[mapNum][i++], 16, 52);

    monsters[mapNum][i] = new MonBat(gp);
    place(monsters[mapNum][i++], 38, 20);

    monsters[mapNum][i] = new MonBat(gp);
    place(monsters[mapNum][i], 58, 28);

    // MAP BOSS ROOM
    mapNum = MAP_BOSS_ROOM;
    i = 0;

    if (!Progress.isSkeletonLordDefeated()) {
      monsters[mapNum][i] = new MonSkeletonLord(gp);
      place(monsters[mapNum][i], 49, 38);
    }
  }

  public void setInteractiveTile() {
    var interactiveTiles = gp.getiTile();

    int mapNum = MAP_WORLD;
    int i = 0;

    // Árvores secas bloqueando caminho alternativo para a dungeon
    interactiveTiles[mapNum][i++] = new ItDryTree(gp, 73, 40);
    interactiveTiles[mapNum][i++] = new ItDryTree(gp, 74, 40);
    interactiveTiles[mapNum][i++] = new ItDryTree(gp, 76, 40);
    interactiveTiles[mapNum][i++] = new ItDryTree(gp, 77, 40);
    // Árvores secas decorativas perto do lago
    interactiveTiles[mapNum][i++] = new ItDryTree(gp, 28, 15);
    interactiveTiles[mapNum][i++] = new ItDryTree(gp, 12, 30);
    // Árvores secas na floresta
    interactiveTiles[mapNum][i++] = new ItDryTree(gp, 70, 28);
    interactiveTiles[mapNum][i++] = new ItDryTree(gp, 72, 30);
    // Árvores secas no caminho sul
    interactiveTiles[mapNum][i++] = new ItDryTree(gp, 48, 70);
    interactiveTiles[mapNum][i] = new ItDryTree(gp, 52, 70);

    // MAP DUNGEON
    mapNum = MAP_DUNGEON;
    i = 0;

    // Paredes destrutíveis - atalho entre salas
    // Parede entre corridor e central hall
    interactiveTiles[mapNum][i++] = new ItDestructibleWall(gp, 34, 48);
    interactiveTiles[mapNum][i++] = new ItDestructibleWall(gp, 34, 49);
    interactiveTiles[mapNum][i++] = new ItDestructibleWall(gp, 34, 50);
    // Parede entre west chamber e NW room
    interactiveTiles[mapNum][i++] = new ItDestructibleWall(gp, 18, 38);
    interactiveTiles[mapNum][i++] = new ItDestructibleWall(gp, 18, 39);
    // Parede entre east chamber e NE room
    interactiveTiles[mapNum][i++] = new ItDestructibleWall(gp, 60, 38);
    interactiveTiles[mapNum][i++] = new ItDestructibleWall(gp, 60, 39);
    // Parede secreta no hub sul
    interactiveTiles[mapNum][i++] = new ItDestructibleWall(gp, 22, 72);
    interactiveTiles[mapNum][i++] = new ItDestructibleWall(gp, 22, 73);
    // Paredes na câmara norte
    interactiveTiles[mapNum][i++] = new ItDestructibleWall(gp, 42, 22);
    interactiveTiles[mapNum][i++] = new ItDestructibleWall(gp, 42, 23);
    interactiveTiles[mapNum][i++] = new ItDestructibleWall(gp, 42, 24);
    // Mais paredes destrutíveis para exploração
    interactiveTiles[mapNum][i++] = new ItDestructibleWall(gp, 30, 62);
    interactiveTiles[mapNum][i++] = new ItDestructibleWall(gp, 48, 46);
    interactiveTiles[mapNum][i++] = new ItDestructibleWall(gp, 48, 47);
    interactiveTiles[mapNum][i++] = new ItDestructibleWall(gp, 48, 48);

    // Placas de metal (puzzles)
    interactiveTiles[mapNum][i++] = new ItMetalPlate(gp, 20, 24);
    interactiveTiles[mapNum][i++] = new ItMetalPlate(gp, 14, 55);
    interactiveTiles[mapNum][i] = new ItMetalPlate(gp, 56, 54);
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

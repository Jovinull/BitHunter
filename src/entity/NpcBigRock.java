package entity;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import main.GamePanel;
import object.ObjDoorIron;
import tile_interactive.InteractiveTile;
import tile_interactive.ItMetalPlate;

public class NpcBigRock extends Entity {

  // Nome público e estável do NPC (usado para comparação)
  public static final String NPC_NAME = "Pedra Grande";

  // Evita duplicação do literal do caminho
  private static final String SPRITE_PATH = "/npc/bigrock";

  // Limiares e SFX usados aqui
  private static final int PROXIMITY_THRESHOLD = 8;
  private static final int SFX_PLATE = 3;
  private static final int SFX_DOOR_OPEN = 21;

  public NpcBigRock(GamePanel gp) {
    super(gp);

    setName(NPC_NAME);
    setDirection(DIR_DOWN);
    setSpeed(4);

    // solidArea é final na Entity -> apenas configuramos
    Rectangle sa = getSolidArea();
    sa.x = 2;
    sa.y = 6;
    setSolidAreaDefaultX(sa.x);
    setSolidAreaDefaultY(sa.y);
    sa.width = 44;
    sa.height = 40;

    setDialogueSet(-1);

    getImage();
    setDialogue();
  }

  @Override
  public void getImage() {
    up1 = setup(SPRITE_PATH, gp.tileSize, gp.tileSize);
    up2 = setup(SPRITE_PATH, gp.tileSize, gp.tileSize);
    down1 = setup(SPRITE_PATH, gp.tileSize, gp.tileSize);
    down2 = setup(SPRITE_PATH, gp.tileSize, gp.tileSize);
    left1 = setup(SPRITE_PATH, gp.tileSize, gp.tileSize);
    left2 = setup(SPRITE_PATH, gp.tileSize, gp.tileSize);
    right1 = setup(SPRITE_PATH, gp.tileSize, gp.tileSize);
    right2 = setup(SPRITE_PATH, gp.tileSize, gp.tileSize);
  }

  public void setDialogue() {
    String[][] d = getDialogues();
    d[0][0] = "Isso é uma pedra gigante.";
    d[0][1] = "Acho que sou capaz de empurrar isso...";
  }

  @Override
  public void setAction() {
    // Rock não tem IA ativa por frame aqui.
  }

  @Override
  public void update() {
    // Rock não tem atualização automática aqui.
  }

  @Override
  public void speak() {
    facePlayer();
    startDialogue(this, getDialogueSet());
    setDialogueSet(getDialogueSet() + 1);

    String[][] d = getDialogues();
    if (d[getDialogueSet()][0] == null) {
      setDialogueSet(getDialogueSet() - 1);
    }
  }

  @Override
  public void move(String d) {
    setDirection(d); // já vem como DIR_*
    checkCollision();

    if (!isCollisionOn()) {
      switch (getDirection()) {
        case DIR_UP:
          setWorldY(getWorldY() - getSpeed());
          break;
        case DIR_DOWN:
          setWorldY(getWorldY() + getSpeed());
          break;
        case DIR_LEFT:
          setWorldX(getWorldX() - getSpeed());
          break;
        case DIR_RIGHT:
          setWorldX(getWorldX() + getSpeed());
          break;
        default:
          break;
      }
    }
    detectPlate();
  }

  public void detectPlate() {
    // 1) capturar pratos metálicos e rochas do mapa atual
    final List<InteractiveTile> plates = findMetalPlatesOnCurrentMap();
    final List<Entity> rocks = findRocksOnCurrentMap();

    // 2) vincular/desvincular esta rocha ao prato mais próximo (regra de
    // proximidade)
    updateLinkWithNearestPlate(plates);

    // 3) contar rochas vinculadas
    final int linkedCount = countLinkedRocks(rocks);

    // 4) se todas as rochas estiverem em pratos, abrir as portas de ferro
    if (!rocks.isEmpty() && linkedCount == rocks.size()) {
      openAllIronDoors();
    }
  }

  // ===== Helpers para reduzir complexidade =====

  private List<InteractiveTile> findMetalPlatesOnCurrentMap() {
    List<InteractiveTile> plates = new ArrayList<>();
    InteractiveTile[] tiles = gp.iTile[gp.currentMap];
    for (InteractiveTile tile : tiles) {
      if (tile != null && ItMetalPlate.IT_NAME.equals(tile.getName())) {
        plates.add(tile);
      }
    }
    return plates;
  }

  private List<Entity> findRocksOnCurrentMap() {
    List<Entity> rocks = new ArrayList<>();
    Entity[] npcs = gp.npc[gp.currentMap];
    for (Entity e : npcs) {
      if (e != null && NPC_NAME.equals(e.getName())) {
        rocks.add(e);
      }
    }
    return rocks;
  }

  private void updateLinkWithNearestPlate(List<InteractiveTile> plates) {
    // vincula quando estiver suficientemente perto; desvincula quando se afasta do
    // prato vinculado
    for (InteractiveTile plate : plates) {
      int distance = chebyshevDistance(getWorldX(), getWorldY(), plate.getWorldX(), plate.getWorldY());
      boolean near = distance < PROXIMITY_THRESHOLD;

      if (near && getLinkedEntity() == null) {
        setLinkedEntity(plate);
        gp.playSE(SFX_PLATE);
        // como só precisamos vincular uma vez, podemos sair cedo
        return;
      }

      if (!near && getLinkedEntity() == plate) {
        setLinkedEntity(null);
      }
    }
  }

  private int countLinkedRocks(List<Entity> rocks) {
    int count = 0;
    for (Entity r : rocks) {
      if (r.getLinkedEntity() != null) {
        count++;
      }
    }
    return count;
  }

  private void openAllIronDoors() {
    Entity[] objects = gp.obj[gp.currentMap];
    boolean opened = false;

    for (int i = 0; i < objects.length; i++) {
      Entity obj = objects[i];
      if (obj != null && ObjDoorIron.OBJ_NAME.equals(obj.getName())) {
        objects[i] = null;
        opened = true;
      }
    }

    if (opened) {
      gp.playSE(SFX_DOOR_OPEN);
    }
  }

  private int chebyshevDistance(int ax, int ay, int bx, int by) {
    int dx = Math.abs(ax - bx);
    int dy = Math.abs(ay - by);
    return Math.max(dx, dy);
  }
}

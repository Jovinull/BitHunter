package main;

import java.awt.Rectangle;

import entity.Entity;

// Classe responsável por verificar colisões entre entidades e tiles
public class CollisionChecker {

  private final GamePanel gp; // Referência ao painel principal do jogo (acessa tiles e configurações)
  private final Rectangle tempRect1 = new Rectangle();
  private final Rectangle tempRect2 = new Rectangle();

  // Construtor: recebe o GamePanel para acessar o mundo e as tiles
  public CollisionChecker(GamePanel gp) {
    this.gp = gp;
  }

  // Verifica se a entidade colidirá com algum tile sólido ao se mover
  public void checkTile(Entity entity) {

    // Coordenadas absolutas (no mundo) da área sólida da entidade
    Rectangle sa = entity.getSolidArea();
    int entityLeftWorldX = entity.getWorldX() + sa.x;
    int entityRightWorldX = entity.getWorldX() + sa.x + sa.width;
    int entityTopWorldY = entity.getWorldY() + sa.y;
    int entityBottomWorldY = entity.getWorldY() + sa.y + sa.height;

    int tileSize = GamePanel.TILE_SIZE;

    // Conversão dessas coordenadas para colunas e linhas da matriz de tiles
    int entityLeftCol = entityLeftWorldX / tileSize;
    int entityRightCol = entityRightWorldX / tileSize;
    int entityTopRow = entityTopWorldY / tileSize;
    int entityBottomRow = entityBottomWorldY / tileSize;

    int tileNum1;
    int tileNum2;

    // Direção temporal quando em knockback
    String direction = entity.isKnockBack() ? entity.getKnockBackDirection() : entity.getDirection();
    int speed = entity.getSpeed();

    // Atalhos para o TileManager
    var tileManager = gp.getTileM();
    int[][][] map = tileManager.getMapTileNum();
    var tiles = tileManager.getTiles();
    int currentMapIndex = gp.getCurrentMap();

    // Verifica colisão com base na direção do movimento
    switch (direction) {
      case Entity.DIR_UP:
        entityTopRow = (entityTopWorldY - speed) / tileSize;
        tileNum1 = map[currentMapIndex][entityLeftCol][entityTopRow];
        tileNum2 = map[currentMapIndex][entityRightCol][entityTopRow];
        if (tiles[tileNum1].isCollision() || tiles[tileNum2].isCollision()) {
          entity.setCollisionOn(true);
        }
        break;

      case Entity.DIR_DOWN:
        entityBottomRow = (entityBottomWorldY + speed) / tileSize;
        tileNum1 = map[currentMapIndex][entityLeftCol][entityBottomRow];
        tileNum2 = map[currentMapIndex][entityRightCol][entityBottomRow];
        if (tiles[tileNum1].isCollision() || tiles[tileNum2].isCollision()) {
          entity.setCollisionOn(true);
        }
        break;

      case Entity.DIR_LEFT:
        entityLeftCol = (entityLeftWorldX - speed) / tileSize;
        tileNum1 = map[currentMapIndex][entityLeftCol][entityTopRow];
        tileNum2 = map[currentMapIndex][entityLeftCol][entityBottomRow];
        if (tiles[tileNum1].isCollision() || tiles[tileNum2].isCollision()) {
          entity.setCollisionOn(true);
        }
        break;

      case Entity.DIR_RIGHT:
        entityRightCol = (entityRightWorldX + speed) / tileSize;
        tileNum1 = map[currentMapIndex][entityRightCol][entityTopRow];
        tileNum2 = map[currentMapIndex][entityRightCol][entityBottomRow];
        if (tiles[tileNum1].isCollision() || tiles[tileNum2].isCollision()) {
          entity.setCollisionOn(true);
        }
        break;

      default:
        // sem direção definida -> nada a fazer
        break;
    }
  }

  public int checkObject(Entity entity) {
    int index = 999;

    String direction = entity.isKnockBack() ? entity.getKnockBackDirection() : entity.getDirection();
    int speed = entity.getSpeed();

    var objects = gp.getObj();
    int currentMapIndex = gp.getCurrentMap();
    int len = objects[currentMapIndex].length;

    Rectangle eRect = tempRect1;
    Rectangle oRect = tempRect2;
    setWorldRect(entity, eRect);
    applyMovement(direction, speed, eRect);

    for (int i = 0; i < len; i++) {
      Entity obj = objects[currentMapIndex][i];
      if (obj == null) {
        continue;
      }

      // Rects temporários em coords do mundo (não muta as solidArea reais)
      setWorldRect(obj, oRect);

      if (eRect.intersects(oRect)) {
        if (obj.isCollision()) {
          entity.setCollisionOn(true);
        }
        index = i;
      }
    }

    return index;
  }

  // Verifica colisão entre uma entidade e um array de entidades-alvo
  // Retorna o índice da entidade-alvo com a qual houve colisão (ou 999 se
  // nenhuma)
  public int checkEntity(Entity entity, Entity[][] target) {

    int index = 999;

    String direction = entity.isKnockBack() ? entity.getKnockBackDirection() : entity.getDirection();
    int speed = entity.getSpeed();
    int currentMapIndex = gp.getCurrentMap();
    int len = target[currentMapIndex].length;

    Rectangle eRect = tempRect1;
    Rectangle oRect = tempRect2;
    setWorldRect(entity, eRect);
    applyMovement(direction, speed, eRect);

    for (int i = 0; i < len; i++) {
      Entity other = target[currentMapIndex][i];
      if (other != null
          && other.isAlive()
          && !other.isDying()
          && (other.getType() != Entity.TYPE_MONSTER || other.getLife() > 0)) {
        setWorldRect(other, oRect);

        if (eRect.intersects(oRect) && other != entity) {
          entity.setCollisionOn(true);
          index = i;
        }
      }
    }

    return index;
  }

  // Verifica se a entidade colidiu com o jogador
  public boolean checKPlayer(Entity entity) {

    boolean contactPlayer = false;

    // Monstro morto/morrendo não bate mais no player
    if (entity.getType() == Entity.TYPE_MONSTER
        && (!entity.isAlive() || entity.isDying() || entity.getLife() <= 0)) {
      return false;
    }

    var player = gp.getPlayer();

    String direction = entity.isKnockBack() ? entity.getKnockBackDirection() : entity.getDirection();
    int speed = entity.getSpeed();

    Rectangle eRect = tempRect1;
    Rectangle pRect = tempRect2;
    setWorldRect(entity, eRect);
    setWorldRect(player, pRect);
    applyMovement(direction, speed, eRect);

    if (eRect.intersects(pRect)) {
      entity.setCollisionOn(true);
      contactPlayer = true;
    }

    return contactPlayer;
  }

  // ----- Helpers -----

  private static void setWorldRect(Entity e, Rectangle out) {
    Rectangle sa = e.getSolidArea();
    out.setBounds(e.getWorldX() + sa.x, e.getWorldY() + sa.y, sa.width, sa.height);
  }

  private static void applyMovement(String direction, int speed, Rectangle rect) {
    switch (direction) {
      case Entity.DIR_UP:
        rect.y -= speed;
        break;
      case Entity.DIR_DOWN:
        rect.y += speed;
        break;
      case Entity.DIR_LEFT:
        rect.x -= speed;
        break;
      case Entity.DIR_RIGHT:
        rect.x += speed;
        break;
      default:
        break;
    }
  }
}

package main;

import java.awt.Rectangle;

import entity.Entity;

// Classe responsável por verificar colisões entre entidades e tiles
public class CollisionChecker {

  private final GamePanel gp; // Referência ao painel principal do jogo (acessa tiles e configurações)

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

    // Conversão dessas coordenadas para colunas e linhas da matriz de tiles
    int entityLeftCol = entityLeftWorldX / gp.tileSize;
    int entityRightCol = entityRightWorldX / gp.tileSize;
    int entityTopRow = entityTopWorldY / gp.tileSize;
    int entityBottomRow = entityBottomWorldY / gp.tileSize;

    int tileNum1;
    int tileNum2;

    // Direção temporal quando em knockback
    String direction = entity.isKnockBack() ? entity.getKnockBackDirection() : entity.getDirection();
    int speed = entity.getSpeed();

    // Atalhos para os getters do TileManager
    int[][][] map = gp.tileM.getMapTileNum();

    // Verifica colisão com base na direção do movimento
    switch (direction) {
      case Entity.DIR_UP:
        entityTopRow = (entityTopWorldY - speed) / gp.tileSize;
        tileNum1 = map[gp.currentMap][entityLeftCol][entityTopRow];
        tileNum2 = map[gp.currentMap][entityRightCol][entityTopRow];
        if (gp.tileM.getTiles()[tileNum1].isCollision() || gp.tileM.getTiles()[tileNum2].isCollision()) {
          entity.setCollisionOn(true);
        }
        break;

      case Entity.DIR_DOWN:
        entityBottomRow = (entityBottomWorldY + speed) / gp.tileSize;
        tileNum1 = map[gp.currentMap][entityLeftCol][entityBottomRow];
        tileNum2 = map[gp.currentMap][entityRightCol][entityBottomRow];
        if (gp.tileM.getTiles()[tileNum1].isCollision() || gp.tileM.getTiles()[tileNum2].isCollision()) {
          entity.setCollisionOn(true);
        }
        break;

      case Entity.DIR_LEFT:
        entityLeftCol = (entityLeftWorldX - speed) / gp.tileSize;
        tileNum1 = map[gp.currentMap][entityLeftCol][entityTopRow];
        tileNum2 = map[gp.currentMap][entityLeftCol][entityBottomRow];
        if (gp.tileM.getTiles()[tileNum1].isCollision() || gp.tileM.getTiles()[tileNum2].isCollision()) {
          entity.setCollisionOn(true);
        }
        break;

      case Entity.DIR_RIGHT:
        entityRightCol = (entityRightWorldX + speed) / gp.tileSize;
        tileNum1 = map[gp.currentMap][entityRightCol][entityTopRow];
        tileNum2 = map[gp.currentMap][entityRightCol][entityBottomRow];
        if (gp.tileM.getTiles()[tileNum1].isCollision() || gp.tileM.getTiles()[tileNum2].isCollision()) {
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
    int len = gp.obj[gp.currentMap].length;

    for (int i = 0; i < len; i++) {
      Entity obj = gp.obj[gp.currentMap][i];
      if (obj == null)
        continue;

      // Rects temporários em coords do mundo (não muta as solidArea reais)
      Rectangle eRect = toWorldRect(entity);
      Rectangle oRect = toWorldRect(obj);

      // Simula o movimento da entidade
      switch (direction) {
        case Entity.DIR_UP:
          eRect.y -= speed;
          break;
        case Entity.DIR_DOWN:
          eRect.y += speed;
          break;
        case Entity.DIR_LEFT:
          eRect.x -= speed;
          break;
        case Entity.DIR_RIGHT:
          eRect.x += speed;
          break;
        default:
          break;
      }

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
    int len = target[gp.currentMap].length;

    for (int i = 0; i < len; i++) {
      Entity other = target[gp.currentMap][i];
      if (other == null)
        continue;

      Rectangle eRect = toWorldRect(entity);
      Rectangle oRect = toWorldRect(other);

      switch (direction) {
        case Entity.DIR_UP:
          eRect.y -= speed;
          break;
        case Entity.DIR_DOWN:
          eRect.y += speed;
          break;
        case Entity.DIR_LEFT:
          eRect.x -= speed;
          break;
        case Entity.DIR_RIGHT:
          eRect.x += speed;
          break;
        default:
          break;
      }

      if (eRect.intersects(oRect) && other != entity) {
        entity.setCollisionOn(true);
        index = i;
      }
    }

    return index;
  }

  // Verifica se a entidade colidiu com o jogador
  public boolean checKPlayer(Entity entity) {

    boolean contactPlayer = false;

    Rectangle eRect = toWorldRect(entity);
    Rectangle pRect = toWorldRect(gp.player);

    String direction = entity.isKnockBack() ? entity.getKnockBackDirection() : entity.getDirection();
    int speed = entity.getSpeed();

    switch (direction) {
      case Entity.DIR_UP:
        eRect.y -= speed;
        break;
      case Entity.DIR_DOWN:
        eRect.y += speed;
        break;
      case Entity.DIR_LEFT:
        eRect.x -= speed;
        break;
      case Entity.DIR_RIGHT:
        eRect.x += speed;
        break;
      default:
        break;
    }

    if (eRect.intersects(pRect)) {
      entity.setCollisionOn(true);
      contactPlayer = true;
    }

    return contactPlayer;
  }

  // ----- Helpers -----

  private Rectangle toWorldRect(Entity e) {
    Rectangle sa = e.getSolidArea();
    return new Rectangle(e.getWorldX() + sa.x, e.getWorldY() + sa.y, sa.width, sa.height);
  }
}

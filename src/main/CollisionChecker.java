package main;

import entity.Entity;

// Classe responsável por verificar colisões entre entidades e tiles
public class CollisionChecker {

    GamePanel gp; // Referência ao painel principal do jogo (acessa tiles e configurações)

    // Construtor: recebe o GamePanel para acessar o mundo e as tiles
    public CollisionChecker(GamePanel gp) {
        this.gp = gp;
    }

    // Verifica se a entidade colidirá com algum tile sólido ao se mover
    public void checkTile(Entity entity) {

        // Coordenadas absolutas (no mundo) da área sólida da entidade
        int entityLeftWorldX = entity.worldX + entity.solidArea.x;
        int entityRightWorldX = entity.worldX + entity.solidArea.x + entity.solidArea.width;
        int entityTopWorldY = entity.worldY + entity.solidArea.y;
        int entityBottomWorldY = entity.worldY + entity.solidArea.y + entity.solidArea.height;

        // Conversão dessas coordenadas para colunas e linhas da matriz de tiles
        int entityLeftCol = entityLeftWorldX / gp.tileSize;
        int entityRightCol = entityRightWorldX / gp.tileSize;
        int entityTopRow = entityTopWorldY / gp.tileSize;
        int entityBottomRow = entityBottomWorldY / gp.tileSize;

        int tileNum1, tileNum2;

        // Verifica colisão com base na direção do movimento
        switch (entity.direction) {
            case "up":
                // Verifica os tiles acima da entidade (após movimento)
                entityTopRow = (entityTopWorldY - entity.speed) / gp.tileSize;
                tileNum1 = gp.tileM.mapTileNum[entityLeftCol][entityTopRow];
                tileNum2 = gp.tileM.mapTileNum[entityRightCol][entityTopRow];
                if (gp.tileM.tile[tileNum1].collision || gp.tileM.tile[tileNum2].collision) {
                    entity.collisionOn = true; // Ativa flag de colisão
                }
                break;

            case "down":
                // Verifica os tiles abaixo da entidade
                entityBottomRow = (entityBottomWorldY + entity.speed) / gp.tileSize;
                tileNum1 = gp.tileM.mapTileNum[entityLeftCol][entityBottomRow];
                tileNum2 = gp.tileM.mapTileNum[entityRightCol][entityBottomRow];
                if (gp.tileM.tile[tileNum1].collision || gp.tileM.tile[tileNum2].collision) {
                    entity.collisionOn = true;
                }
                break;

            case "left":
                // Verifica os tiles à esquerda da entidade
                entityLeftCol = (entityLeftWorldX - entity.speed) / gp.tileSize;
                tileNum1 = gp.tileM.mapTileNum[entityLeftCol][entityTopRow];
                tileNum2 = gp.tileM.mapTileNum[entityLeftCol][entityBottomRow];
                if (gp.tileM.tile[tileNum1].collision || gp.tileM.tile[tileNum2].collision) {
                    entity.collisionOn = true;
                }
                break;

            case "right":
                // Verifica os tiles à direita da entidade
                entityRightCol = (entityRightWorldX + entity.speed) / gp.tileSize;
                tileNum1 = gp.tileM.mapTileNum[entityRightCol][entityTopRow];
                tileNum2 = gp.tileM.mapTileNum[entityRightCol][entityBottomRow];
                if (gp.tileM.tile[tileNum1].collision || gp.tileM.tile[tileNum2].collision) {
                    entity.collisionOn = true;
                }
                break;
        }
    }

    public int checkObject(Entity entity, boolean player) {
        int index = 999; // Índice padrão (999 = nenhum objeto relevante colidido)

        for (int i = 0; i < gp.obj.length; i++) {
            if (gp.obj[i] != null) {

                // Calcula a posição real da área de colisão da entidade no mundo
                entity.solidArea.x = entity.worldX + entity.solidArea.x;
                entity.solidArea.y = entity.worldY + entity.solidArea.y;

                // Calcula a posição real da área de colisão do objeto no mundo
                gp.obj[i].solidArea.x = gp.obj[i].worldX + gp.obj[i].solidArea.x;
                gp.obj[i].solidArea.y = gp.obj[i].worldY + gp.obj[i].solidArea.y;

                // Simula o movimento da entidade conforme a direção atual para verificar
                // colisão
                switch (entity.direction) {
                    case "up":
                        entity.solidArea.y -= entity.speed;
                        if (entity.solidArea.intersects(gp.obj[i].solidArea)) {
                            if (gp.obj[i].collision)
                                entity.collisionOn = true;
                            if (player)
                                index = i; // Retorna índice do objeto se for o jogador
                        }
                        break;

                    case "down":
                        entity.solidArea.y += entity.speed;
                        if (entity.solidArea.intersects(gp.obj[i].solidArea)) {
                            if (gp.obj[i].collision)
                                entity.collisionOn = true;
                            if (player)
                                index = i;
                        }
                        break;

                    case "left":
                        entity.solidArea.x -= entity.speed;
                        if (entity.solidArea.intersects(gp.obj[i].solidArea)) {
                            if (gp.obj[i].collision)
                                entity.collisionOn = true;
                            if (player)
                                index = i;
                        }
                        break;

                    case "right":
                        entity.solidArea.x += entity.speed;
                        if (entity.solidArea.intersects(gp.obj[i].solidArea)) {
                            if (gp.obj[i].collision)
                                entity.collisionOn = true;
                            if (player)
                                index = i;
                        }
                        break;
                }

                // Restaura as posições originais das áreas sólidas
                entity.solidArea.x = entity.solidAreaDefaultX;
                entity.solidArea.y = entity.solidAreaDefaultY;
                gp.obj[i].solidArea.x = gp.obj[i].solidAreaDefaultX;
                gp.obj[i].solidArea.y = gp.obj[i].solidAreaDefaultY;
            }
        }

        return index; // Retorna o índice do objeto colidido (ou 999 se nenhum)
    }

    // Verifica colisão entre uma entidade e um array de entidades-alvo
    // Retorna o índice da entidade-alvo com a qual houve colisão (ou 999 se
    // nenhuma)
    public int checkEntity(Entity entity, Entity[] target) {
        int index = 999; // Valor padrão indicando "sem colisão detectada"

        for (int i = 0; i < target.length; i++) {
            if (target[i] != null) {

                // Posiciona temporariamente as áreas de colisão no mundo real
                entity.solidArea.x = entity.worldX + entity.solidArea.x;
                entity.solidArea.y = entity.worldY + entity.solidArea.y;

                target[i].solidArea.x = target[i].worldX + target[i].solidArea.x;
                target[i].solidArea.y = target[i].worldY + target[i].solidArea.y;

                // Simula o deslocamento da entidade em direção ao alvo para testar colisão
                switch (entity.direction) {
                    case "up":
                        entity.solidArea.y -= entity.speed;
                        if (entity.solidArea.intersects(target[i].solidArea)) {
                            entity.collisionOn = true;
                            index = i;
                        }
                        break;

                    case "down":
                        entity.solidArea.y += entity.speed;
                        if (entity.solidArea.intersects(target[i].solidArea)) {
                            entity.collisionOn = true;
                            index = i;
                        }
                        break;

                    case "left":
                        entity.solidArea.x -= entity.speed;
                        if (entity.solidArea.intersects(target[i].solidArea)) {
                            entity.collisionOn = true;
                            index = i;
                        }
                        break;

                    case "right":
                        entity.solidArea.x += entity.speed;
                        if (entity.solidArea.intersects(target[i].solidArea)) {
                            entity.collisionOn = true;
                            index = i;
                        }
                        break;
                }

                // Após o teste, as áreas sólidas são restauradas às posições padrão
                entity.solidArea.x = entity.solidAreaDefaultX;
                entity.solidArea.y = entity.solidAreaDefaultY;
                target[i].solidArea.x = target[i].solidAreaDefaultX;
                target[i].solidArea.y = target[i].solidAreaDefaultY;
            }
        }

        return index;
    }

    // Verifica se a entidade colidiu com o jogador
    // Define a flag de colisão `collisionOn = true` caso haja interseção
    public void checKPlayer(Entity entity) {

        // Posiciona temporariamente as áreas de colisão da entidade e do jogador no
        // mundo real
        entity.solidArea.x = entity.worldX + entity.solidArea.x;
        entity.solidArea.y = entity.worldY + entity.solidArea.y;

        gp.player.solidArea.x = gp.player.worldX + gp.player.solidArea.x;
        gp.player.solidArea.y = gp.player.worldY + gp.player.solidArea.y;

        // Simula o deslocamento da entidade para detectar se vai colidir com o jogador
        switch (entity.direction) {
            case "up":
                entity.solidArea.y -= entity.speed;
                if (entity.solidArea.intersects(gp.player.solidArea)) {
                    entity.collisionOn = true;
                }
                break;

            case "down":
                entity.solidArea.y += entity.speed;
                if (entity.solidArea.intersects(gp.player.solidArea)) {
                    entity.collisionOn = true;
                }
                break;

            case "left":
                entity.solidArea.x -= entity.speed;
                if (entity.solidArea.intersects(gp.player.solidArea)) {
                    entity.collisionOn = true;
                }
                break;

            case "right":
                entity.solidArea.x += entity.speed;
                if (entity.solidArea.intersects(gp.player.solidArea)) {
                    entity.collisionOn = true;
                }
                break;
        }

        // Restaura as áreas sólidas para evitar efeitos colaterais
        entity.solidArea.x = entity.solidAreaDefaultX;
        entity.solidArea.y = entity.solidAreaDefaultY;
        gp.player.solidArea.x = gp.player.solidAreaDefaultX;
        gp.player.solidArea.y = gp.player.solidAreaDefaultY;
    }
}

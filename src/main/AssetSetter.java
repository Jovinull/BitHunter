package main;

import object.OBJ_Boots;
import object.OBJ_Chest;
import object.OBJ_Door;
import object.OBJ_Key;

// Classe responsável por posicionar os objetos do jogo no mundo
public class AssetSetter {

    GamePanel gp; // Referência ao painel principal, usado para acessar tileSize e o array de
                  // objetos

    // Construtor: captura a instância do GamePanel para configurar posição dos
    // objetos
    public AssetSetter(GamePanel gp) {
        this.gp = gp;
    }

    // Método que instancia e define a posição (em coordenadas de mundo) de cada
    // objeto
    public void setObject() {
        // Cria chaves em posições estratégicas
        gp.obj[0] = new OBJ_Key(gp);
        gp.obj[0].worldX = 23 * gp.tileSize; // 23 tiles à direita da origem
        gp.obj[0].worldY = 7 * gp.tileSize; // 7 tiles abaixo da origem

        gp.obj[1] = new OBJ_Key(gp);
        gp.obj[1].worldX = 23 * gp.tileSize;
        gp.obj[1].worldY = 40 * gp.tileSize;

        gp.obj[2] = new OBJ_Key(gp);
        gp.obj[2].worldX = 38 * gp.tileSize;
        gp.obj[2].worldY = 8 * gp.tileSize;

        // Cria portas que bloqueiam áreas específicas
        gp.obj[3] = new OBJ_Door(gp);
        gp.obj[3].worldX = 10 * gp.tileSize;
        gp.obj[3].worldY = 12 * gp.tileSize;

        gp.obj[4] = new OBJ_Door(gp);
        gp.obj[4].worldX = 8 * gp.tileSize;
        gp.obj[4].worldY = 28 * gp.tileSize;

        gp.obj[5] = new OBJ_Door(gp);
        gp.obj[5].worldX = 12 * gp.tileSize;
        gp.obj[5].worldY = 23 * gp.tileSize;

        // Cria baú que pode conter itens ou recompensas
        gp.obj[6] = new OBJ_Chest(gp);
        gp.obj[6].worldX = 10 * gp.tileSize;
        gp.obj[6].worldY = 8 * gp.tileSize;

        // Cria botas que podem ser coletadas pelo jogador para aumentar speed
        gp.obj[7] = new OBJ_Boots(gp);
        gp.obj[7].worldX = 37 * gp.tileSize;
        gp.obj[7].worldY = 42 * gp.tileSize;
    }
}

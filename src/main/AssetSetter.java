package main;

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

    }
}

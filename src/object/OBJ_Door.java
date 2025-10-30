package object;

import entity.Entity;
import main.GamePanel;

// Representa a porta do jogo, permite bloqueio de passagem se collision=true
public class OBJ_Door extends Entity {

  GamePanel gp;

  // Construtor: define nome, habilita colisão e carrega a imagem da porta
  public OBJ_Door(GamePanel gp) {
    super(gp);
    this.gp = gp;

    type = type_obstacle;
    name = "Porta";
    down1 = setup("/objects/door", gp.tileSize, gp.tileSize);
    collision = true;

    solidArea.x = 0;
    solidArea.y = 16;
    solidArea.width = 48;
    solidArea.height = 32;
    solidAreaDefaultX = solidArea.x;
    solidAreaDefaultY = solidArea.y;

    setDialogue();
  }

  public void setDialogue() {

    dialogues[0][0] = "Uma porta trancada. Você precisa de uma \nchave para abri-la.";
  }

  public void interact() {

    startDialogue(this, 0);
  }
}

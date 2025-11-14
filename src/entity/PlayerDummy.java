package entity;

import main.GamePanel;

public class PlayerDummy extends Entity {

  public static final String NPC_NAME = "Dummy";

  public PlayerDummy(GamePanel gp) {
    super(gp);
    setName(NPC_NAME);
    getImage();
  }

  @Override
  public void getImage() {
    up1 = setup("/player/Walking sprites/boy_up_1", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);
    up2 = setup("/player/Walking sprites/boy_up_2", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);

    down1 = setup("/player/Walking sprites/boy_down_1", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);
    down2 = setup("/player/Walking sprites/boy_down_2", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);

    left1 = setup("/player/Walking sprites/boy_left_1", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);
    left2 = setup("/player/Walking sprites/boy_left_2", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);

    right1 = setup("/player/Walking sprites/boy_right_1", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);
    right2 = setup("/player/Walking sprites/boy_right_2", GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);
  }
}

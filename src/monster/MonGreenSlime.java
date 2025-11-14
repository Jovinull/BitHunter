package monster;

import java.util.concurrent.ThreadLocalRandom;

import entity.Entity;
import main.GamePanel;
import object.ObjCoinBronze;
import object.ObjHeart;
import object.ObjManaCrystal;
import object.ObjRock;

public class MonGreenSlime extends Entity {

  // Evita duplicação de literais (S1192)
  private static final String SLIME_DOWN_1 = "/monster/greenslime_down_1";
  private static final String SLIME_DOWN_2 = "/monster/greenslime_down_2";

  public MonGreenSlime(GamePanel gp) {
    super(gp);

    setType(TYPE_MONSTER);
    setName("Slime Verde");
    setDefaultSpeed(1);
    setSpeed(getDefaultSpeed());
    setMaxLife(6);
    setLife(getMaxLife());
    setAttack(5);
    setDefense(0);
    setExp(2);
    setProjectile(new ObjRock(gp));

    // solidArea (usar o objeto via getter, Rectangle tem campos públicos)
    getSolidArea().x = 3;
    getSolidArea().y = 18;
    getSolidArea().width = 42;
    getSolidArea().height = 30;
    setSolidAreaDefaultX(getSolidArea().x);
    setSolidAreaDefaultY(getSolidArea().y);

    getImage();
  }

  @Override
  public void getImage() {
    // Sprites em todas as direções (slime usa os mesmos frames do "down")
    up1 = setup(SLIME_DOWN_1, GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);
    up2 = setup(SLIME_DOWN_2, GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);
    down1 = setup(SLIME_DOWN_1, GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);
    down2 = setup(SLIME_DOWN_2, GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);
    left1 = setup(SLIME_DOWN_1, GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);
    left2 = setup(SLIME_DOWN_2, GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);
    right1 = setup(SLIME_DOWN_1, GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);
    right2 = setup(SLIME_DOWN_2, GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);
  }

  @Override
  public void setAction() {
    var player = gp.getPlayer();

    if (isOnPath()) {
      checkStopChasingOrNot(player, 15, 100);
      searchPath(getGoalCol(player), getGoalRow(player));
      checkShootOrNot(200, 30);
    } else {
      checkStartChasingOrNot(player, 5, 100);
      getRandomDirection(120);
    }
  }

  @Override
  public void damageReaction() {
    setActionLockCounter(0); // Reseta o contador de ações
    setOnPath(true);
  }

  @Override
  public void checkDrop() {
    // (S2119) Reusar Random -> ThreadLocalRandom
    int i = ThreadLocalRandom.current().nextInt(1, 101);

    if (i < 50) {
      dropItem(new ObjCoinBronze(gp));
    } else if (i < 75) {
      dropItem(new ObjHeart(gp));
    } else {
      dropItem(new ObjManaCrystal(gp));
    }
  }
}

package main;

import entity.Entity;
import object.*;
import pets.GreenSnailEgg;
import pets.YellowSnailEgg;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class EntityGenerator {

  private final GamePanel gp;
  private final Map<String, Function<GamePanel, Entity>> factories = new HashMap<>();

  public EntityGenerator(GamePanel gp) {
    this.gp = gp;
    initFactories();
  }

  private void initFactories() {
    factories.put(ObjAxe.OBJ_ID, ObjAxe::new);
    factories.put(ObjBoots.OBJ_ID, ObjBoots::new);
    factories.put(ObjChest.OBJ_ID, ObjChest::new);
    factories.put(ObjChestSecret.OBJ_ID, ObjChestSecret::new);
    factories.put(ObjCoinBronze.OBJ_ID, ObjCoinBronze::new);
    factories.put(ObjDoor.OBJ_ID, ObjDoor::new);
    factories.put(ObjDoorIron.OBJ_ID, ObjDoorIron::new);
    factories.put(ObjFireball.OBJ_ID, ObjFireball::new);
    factories.put(ObjHeart.OBJ_ID, ObjHeart::new);
    factories.put(ObjKey.OBJ_ID, ObjKey::new);
    factories.put(ObjLantern.OBJ_ID, ObjLantern::new);
    factories.put(ObjClock.OBJ_ID, ObjClock::new);
    factories.put(ObjManaCrystal.OBJ_ID, ObjManaCrystal::new);
    factories.put(ObjPickaxe.OBJ_ID, ObjPickaxe::new);
    factories.put(ObjPotionRed.OBJ_ID, ObjPotionRed::new);
    factories.put(ObjRock.OBJ_ID, ObjRock::new);
    factories.put(ObjShieldBlue.OBJ_ID, ObjShieldBlue::new);
    factories.put(ObjShieldWood.OBJ_ID, ObjShieldWood::new);
    factories.put(ObjShieldJava.OBJ_ID, ObjShieldJava::new);
    factories.put(ObjSwordNormal.OBJ_ID, ObjSwordNormal::new);
    factories.put(ObjTent.OBJ_ID, ObjTent::new);
    factories.put(YellowSnailEgg.OBJ_ID, YellowSnailEgg::new);
    factories.put(GreenSnailEgg.OBJ_ID, GreenSnailEgg::new);
  }

  public Entity getObject(String idOrName) {
    if (idOrName == null || idOrName.isEmpty()) {
      return null;
    }
    Function<GamePanel, Entity> factory = factories.get(idOrName);
    return factory != null ? factory.apply(gp) : null;
  }
}

package main;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import entity.Entity;
import object.ObjAxe;
import object.ObjBoots;
import object.ObjChest;
import object.ObjChestGold;
import object.ObjChestIron;
import object.ObjChestLeather;
import object.ObjChestObsidian;
import object.ObjChestSecret;
import object.ObjClock;
import object.ObjCoinBronze;
import object.ObjDoor;
import object.ObjDoorIron;
import object.ObjFireball;
import object.ObjHeart;
import object.ObjHelmetGold;
import object.ObjHelmetIron;
import object.ObjHelmetLeather;
import object.ObjHelmetObsidian;
import object.ObjKey;
import object.ObjLantern;
import object.ObjLegsGold;
import object.ObjLegsIron;
import object.ObjLegsLeather;
import object.ObjLegsObsidian;
import object.ObjManaCrystal;
import object.ObjPickaxe;
import object.ObjPotionRed;
import object.ObjAmuletArcane;
import object.ObjAmuletLife;
import object.ObjAmuletObsidian;
import object.ObjAmuletProtection;
import object.ObjGlovesGold;
import object.ObjGlovesIron;
import object.ObjGlovesLeather;
import object.ObjGlovesObsidian;
import object.ObjRingEmerald;
import object.ObjRingObsidian;
import object.ObjRingRuby;
import object.ObjRingSapphire;
import object.ObjRock;
import object.ObjShieldBlue;
import object.ObjStaffArcane;
import object.ObjStaffCrystal;
import object.ObjStaffObsidian;
import object.ObjStaffWood;
import object.ObjShieldJava;
import object.ObjShieldWood;
import object.ObjSwordNormal;
import object.ObjTent;
import pets.GreenSnailEgg;
import pets.YellowSnailEgg;

public class EntityGenerator {

  private final GamePanel gp;
  private final Map<String, Function<GamePanel, Entity>> factories = new HashMap<>();

  public EntityGenerator(GamePanel gp) {
    this.gp = gp;
    initFactories();
  }

  private void initFactories() {
    factories.put(ObjAmuletArcane.OBJ_ID, ObjAmuletArcane::new);
    factories.put(ObjAmuletLife.OBJ_ID, ObjAmuletLife::new);
    factories.put(ObjAmuletObsidian.OBJ_ID, ObjAmuletObsidian::new);
    factories.put(ObjAmuletProtection.OBJ_ID, ObjAmuletProtection::new);
    factories.put(ObjAxe.OBJ_ID, ObjAxe::new);
    factories.put(ObjBoots.OBJ_ID, ObjBoots::new);
    factories.put(ObjGlovesGold.OBJ_ID, ObjGlovesGold::new);
    factories.put(ObjGlovesIron.OBJ_ID, ObjGlovesIron::new);
    factories.put(ObjGlovesLeather.OBJ_ID, ObjGlovesLeather::new);
    factories.put(ObjGlovesObsidian.OBJ_ID, ObjGlovesObsidian::new);
    factories.put(ObjRingEmerald.OBJ_ID, ObjRingEmerald::new);
    factories.put(ObjRingObsidian.OBJ_ID, ObjRingObsidian::new);
    factories.put(ObjRingRuby.OBJ_ID, ObjRingRuby::new);
    factories.put(ObjRingSapphire.OBJ_ID, ObjRingSapphire::new);
    factories.put(ObjChest.OBJ_ID, ObjChest::new);
    factories.put(ObjChestGold.OBJ_ID, ObjChestGold::new);
    factories.put(ObjChestIron.OBJ_ID, ObjChestIron::new);
    factories.put(ObjChestLeather.OBJ_ID, ObjChestLeather::new);
    factories.put(ObjChestObsidian.OBJ_ID, ObjChestObsidian::new);
    factories.put(ObjChestSecret.OBJ_ID, ObjChestSecret::new);
    factories.put(ObjHelmetGold.OBJ_ID, ObjHelmetGold::new);
    factories.put(ObjHelmetIron.OBJ_ID, ObjHelmetIron::new);
    factories.put(ObjHelmetLeather.OBJ_ID, ObjHelmetLeather::new);
    factories.put(ObjHelmetObsidian.OBJ_ID, ObjHelmetObsidian::new);
    factories.put(ObjLegsGold.OBJ_ID, ObjLegsGold::new);
    factories.put(ObjLegsIron.OBJ_ID, ObjLegsIron::new);
    factories.put(ObjLegsLeather.OBJ_ID, ObjLegsLeather::new);
    factories.put(ObjLegsObsidian.OBJ_ID, ObjLegsObsidian::new);
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
    factories.put(ObjStaffWood.OBJ_ID, ObjStaffWood::new);
    factories.put(ObjStaffCrystal.OBJ_ID, ObjStaffCrystal::new);
    factories.put(ObjStaffArcane.OBJ_ID, ObjStaffArcane::new);
    factories.put(ObjStaffObsidian.OBJ_ID, ObjStaffObsidian::new);
    factories.put(ObjSwordNormal.OBJ_ID, ObjSwordNormal::new);
    factories.put(ObjTent.OBJ_ID, ObjTent::new);
    factories.put(YellowSnailEgg.OBJ_ID, YellowSnailEgg::new);
    factories.put(GreenSnailEgg.OBJ_ID, GreenSnailEgg::new);

    // Monsters
    factories.put(monster.MonBat.MON_ID, monster.MonBat::new);
    factories.put(monster.MonGreenSlime.MON_ID, monster.MonGreenSlime::new);
    factories.put(monster.MonOrc.MON_ID, monster.MonOrc::new);
    factories.put(monster.MonRedSlime.MON_ID, monster.MonRedSlime::new);
    factories.put(monster.MonSkeletonLord.MON_ID, monster.MonSkeletonLord::new);
  }

  public Entity getObject(String idOrName) {
    if (idOrName == null || idOrName.isEmpty()) {
      return null;
    }
    Function<GamePanel, Entity> factory = factories.get(idOrName);
    return factory != null ? factory.apply(gp) : null;
  }

  public java.util.Set<String> getRegisteredIds() {
    return factories.keySet();
  }
}

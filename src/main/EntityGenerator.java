package main;

import entity.Entity;
import object.ObjAxe;
import object.ObjBoots;
import object.ObjChest;
import object.ObjCoinBronze;
import object.ObjDoor;
import object.ObjDoorIron;
import object.ObjFireball;
import object.ObjHeart;
import object.ObjKey;
import object.ObjLantern;
import object.ObjManaCrystal;
import object.ObjPickaxe;
import object.ObjPotionRed;
import object.ObjRock;
import object.ObjShieldBlue;
import object.ObjShieldWood;
import object.ObjSwordNormal;
import object.ObjTent;

public class EntityGenerator {

  GamePanel gp;

  public EntityGenerator(GamePanel gp) {
    this.gp = gp;
  }

  public Entity getObject(String itemName) {

    Entity obj = null;

    switch (itemName) {
      case ObjAxe.OBJ_NAME:
        obj = new ObjAxe(gp);
        break;
      case ObjBoots.OBJ_NAME:
        obj = new ObjBoots(gp);
        break;
      case ObjChest.OBJ_NAME:
        obj = new ObjChest(gp);
        break;
      case ObjCoinBronze.OBJ_NAME:
        obj = new ObjCoinBronze(gp);
        break;
      case ObjDoorIron.OBJ_NAME:
        obj = new ObjDoorIron(gp);
        break;
      case ObjDoor.OBJ_NAME:
        obj = new ObjDoor(gp);
        break;
      case ObjFireball.OBJ_NAME:
        obj = new ObjFireball(gp);
        break;
      case ObjHeart.OBJ_NAME:
        obj = new ObjHeart(gp);
        break;
      case ObjKey.OBJ_NAME:
        obj = new ObjKey(gp);
        break;
      case ObjLantern.OBJ_NAME:
        obj = new ObjLantern(gp);
        break;
      case ObjManaCrystal.OBJ_NAME:
        obj = new ObjManaCrystal(gp);
        break;
      case ObjPickaxe.OBJ_NAME:
        obj = new ObjPickaxe(gp);
        break;
      case ObjPotionRed.OBJ_NAME:
        obj = new ObjPotionRed(gp);
        break;
      case ObjRock.OBJ_NAME:
        obj = new ObjRock(gp);
        break;
      case ObjShieldBlue.OBJ_NAME:
        obj = new ObjShieldBlue(gp);
        break;
      case ObjShieldWood.OBJ_NAME:
        obj = new ObjShieldWood(gp);
        break;
      case ObjSwordNormal.OBJ_NAME:
        obj = new ObjSwordNormal(gp);
        break;
      case ObjTent.OBJ_NAME:
        obj = new ObjTent(gp);
        break;
      default:
        // itemName desconhecido; obj permanece null
        break;
    }

    return obj;
  }
}

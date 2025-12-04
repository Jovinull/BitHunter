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
import pets.GreenSnailEgg;
import pets.YellowSnailEgg;

public class EntityGenerator {

  private final GamePanel gp;

  public EntityGenerator(GamePanel gp) {
    this.gp = gp;
  }

  public Entity getObject(String idOrName) {

    if (idOrName == null || idOrName.isEmpty()) {
      return null;
    }

    Entity obj = null;

    switch (idOrName) {

      // Machado
      case ObjAxe.OBJ_ID:
        obj = new ObjAxe(gp);
        break;

      // Botas
      case ObjBoots.OBJ_ID:
        obj = new ObjBoots(gp);
        break;

      // Baú
      case ObjChest.OBJ_ID:
        obj = new ObjChest(gp);
        break;

      // Moeda de bronze
      case ObjCoinBronze.OBJ_ID:
        obj = new ObjCoinBronze(gp);
        break;

      // Porta de ferro
      case ObjDoorIron.OBJ_ID:
        obj = new ObjDoorIron(gp);
        break;

      // Porta comum
      case ObjDoor.OBJ_ID:
        obj = new ObjDoor(gp);
        break;

      // Bola de fogo
      case ObjFireball.OBJ_ID:
        obj = new ObjFireball(gp);
        break;

      // Coração
      case ObjHeart.OBJ_ID:
        obj = new ObjHeart(gp);
        break;

      // Chave
      case ObjKey.OBJ_ID:
        obj = new ObjKey(gp);
        break;

      // Lanterna
      case ObjLantern.OBJ_ID:
        obj = new ObjLantern(gp);
        break;

      // Cristal de Mana
      case ObjManaCrystal.OBJ_ID:
        obj = new ObjManaCrystal(gp);
        break;

      // Picareta
      case ObjPickaxe.OBJ_ID:
        obj = new ObjPickaxe(gp);
        break;

      // Poção Vermelha
      case ObjPotionRed.OBJ_ID:
        obj = new ObjPotionRed(gp);
        break;

      // Pedra
      case ObjRock.OBJ_ID:
        obj = new ObjRock(gp);
        break;

      // Escudo Azul
      case ObjShieldBlue.OBJ_ID:
        obj = new ObjShieldBlue(gp);
        break;

      // Escudo de Madeira
      case ObjShieldWood.OBJ_ID:
        obj = new ObjShieldWood(gp);
        break;

      // Espada Comum
      case ObjSwordNormal.OBJ_ID:
        obj = new ObjSwordNormal(gp);
        break;

      // Tenda
      case ObjTent.OBJ_ID:
        obj = new ObjTent(gp);
        break;

      // Ovo do Caracol Amarelo
      case YellowSnailEgg.OBJ_ID:
        obj = new YellowSnailEgg(gp);
        break;

      // Ovo do Caracol Verde
      case GreenSnailEgg.OBJ_ID:
        obj = new GreenSnailEgg(gp);
        break;

      default:
        // id desconhecido; obj permanece null
        break;
    }

    return obj;
  }
}

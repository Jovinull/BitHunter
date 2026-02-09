package entity.components;

import entity.Entity;

public class EquipmentComponent {

  private Entity currentWeapon;
  private Entity currentShield;
  private Entity currentLight;
  private Entity currentBoots;
  private Entity currentHelmet;
  private Entity currentChest;
  private Entity currentLegs;

  public Entity getCurrentWeapon() {
    return currentWeapon;
  }

  public void setCurrentWeapon(Entity currentWeapon) {
    this.currentWeapon = currentWeapon;
  }

  public Entity getCurrentShield() {
    return currentShield;
  }

  public void setCurrentShield(Entity currentShield) {
    this.currentShield = currentShield;
  }

  public Entity getCurrentLight() {
    return currentLight;
  }

  public void setCurrentLight(Entity currentLight) {
    this.currentLight = currentLight;
  }

  public Entity getCurrentBoots() {
    return currentBoots;
  }

  public void setCurrentBoots(Entity currentBoots) {
    this.currentBoots = currentBoots;
  }

  public Entity getCurrentHelmet() {
    return currentHelmet;
  }

  public void setCurrentHelmet(Entity currentHelmet) {
    this.currentHelmet = currentHelmet;
  }

  public Entity getCurrentChest() {
    return currentChest;
  }

  public void setCurrentChest(Entity currentChest) {
    this.currentChest = currentChest;
  }

  public Entity getCurrentLegs() {
    return currentLegs;
  }

  public void setCurrentLegs(Entity currentLegs) {
    this.currentLegs = currentLegs;
  }
}

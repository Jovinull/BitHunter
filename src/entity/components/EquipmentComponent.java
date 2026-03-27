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
  private Entity currentRing;
  private Entity currentAmulet;
  private Entity currentGloves;

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

  public Entity getCurrentRing() {
    return currentRing;
  }

  public void setCurrentRing(Entity currentRing) {
    this.currentRing = currentRing;
  }

  public Entity getCurrentAmulet() {
    return currentAmulet;
  }

  public void setCurrentAmulet(Entity currentAmulet) {
    this.currentAmulet = currentAmulet;
  }

  public Entity getCurrentGloves() {
    return currentGloves;
  }

  public void setCurrentGloves(Entity currentGloves) {
    this.currentGloves = currentGloves;
  }
}

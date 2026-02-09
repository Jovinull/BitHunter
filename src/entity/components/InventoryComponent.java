package entity.components;

import java.util.ArrayList;
import java.util.List;

import entity.Entity;

public class InventoryComponent {

  private final List<Entity> inventory = new ArrayList<>();

  public List<Entity> getInventory() {
    return inventory;
  }
}

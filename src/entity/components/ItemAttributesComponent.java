package entity.components;

public class ItemAttributesComponent {

  private int value;
  private int attackValue;
  private int defenseValue;
  private int speedValue;
  private String description = "";
  private int useCost;
  private int price;
  private int knockBackPower = 0;
  private boolean stackable = false;
  private int amount = 1;
  private int lightRadius;
  private int guardStaminaCapacity;

  public int getValue() {
    return value;
  }

  public void setValue(int value) {
    this.value = value;
  }

  public int getAttackValue() {
    return attackValue;
  }

  public void setAttackValue(int attackValue) {
    this.attackValue = attackValue;
  }

  public int getDefenseValue() {
    return defenseValue;
  }

  public void setDefenseValue(int defenseValue) {
    this.defenseValue = defenseValue;
  }

  public int getSpeedValue() {
    return speedValue;
  }

  public void setSpeedValue(int speedValue) {
    this.speedValue = speedValue;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public int getUseCost() {
    return useCost;
  }

  public void setUseCost(int useCost) {
    this.useCost = useCost;
  }

  public int getPrice() {
    return price;
  }

  public void setPrice(int price) {
    this.price = price;
  }

  public int getKnockBackPower() {
    return knockBackPower;
  }

  public void setKnockBackPower(int knockBackPower) {
    this.knockBackPower = knockBackPower;
  }

  public boolean isStackable() {
    return stackable;
  }

  public void setStackable(boolean stackable) {
    this.stackable = stackable;
  }

  public int getAmount() {
    return amount;
  }

  public void setAmount(int amount) {
    this.amount = amount;
  }

  public int getLightRadius() {
    return lightRadius;
  }

  public void setLightRadius(int lightRadius) {
    this.lightRadius = lightRadius;
  }

  public int getGuardStaminaCapacity() {
    return guardStaminaCapacity;
  }

  public void setGuardStaminaCapacity(int guardStaminaCapacity) {
    this.guardStaminaCapacity = guardStaminaCapacity;
  }
}

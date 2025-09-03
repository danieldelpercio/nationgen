package nationGen.items;

public enum ItemType {
  MELEE("melee"),
  RANGED("ranged"),
  LOW_SHOTS("lowshots"),
  MOUNT("mount");

  private String id;

  ItemType(String typeId) {
    this.id = typeId;
  }

  public String getId() {
    return this.id;
  }
}

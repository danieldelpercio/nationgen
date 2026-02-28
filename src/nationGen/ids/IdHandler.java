package nationGen.ids;

public class IdHandler {
  
  public final static int MIN_CUSTOM_ARMOR_ID = 300;
  public final static int MAX_CUSTOM_ARMOR_ID = 999;

  public final static int MIN_MONTAG_ID = 1000;
  public final static int MAX_MONTAG_ID = 100000;

  public final static int MIN_CUSTOM_MONSTER_ID = 5000;
  public final static int MAX_CUSTOM_MONSTER_ID = 8999;

  public final static int MIN_CUSTOM_NAMETYPE_ID = 170;
  public final static int MAX_CUSTOM_NAMETYPE_ID = 399;

  public final static int MIN_CUSTOM_NATION_ID = 150;
  public final static int MAX_CUSTOM_NATION_ID = 499;

  public final static int MIN_CUSTOM_SPELL_ID = 1300;
  public final static int MAX_CUSTOM_SPELL_ID = 3999;

  public final static int MIN_CUSTOM_SITE_ID = 1700;
  public final static int MAX_CUSTOM_SITE_ID = 3999;

  public final static int MIN_CUSTOM_WEAPON_ID = 1000;
  public final static int MAX_CUSTOM_WEAPON_ID = 3999;


  private int lastAssignedArmorId = MIN_CUSTOM_ARMOR_ID;
  private int lastAssignedMontagId = MIN_MONTAG_ID;
  private int lastAssignedNametypeId = MIN_CUSTOM_NAMETYPE_ID;
  private int lastAssignedNationId = MIN_CUSTOM_NATION_ID;
  private int lastAssignedSiteId = MIN_CUSTOM_SITE_ID;
  private int lastAssignedUnitId = MIN_CUSTOM_MONSTER_ID;
  private int lastAssignedWeaponId = MIN_CUSTOM_WEAPON_ID;

  public IdHandler() {}

  public int nextArmorId() {
    int nextId = this.lastAssignedArmorId+1;

    if (nextId > IdHandler.MAX_CUSTOM_ARMOR_ID) {
      throw new IllegalStateException(
        "Max armor id reached! Cannot assign id " +
        nextId +
        " as it is above the maximum of " +
        IdHandler.MAX_CUSTOM_ARMOR_ID
      );
    }

    this.lastAssignedArmorId = nextId;
    return nextId;
  }

  public int nextMontagId() {
    int nextId = this.lastAssignedMontagId+1;

    if (nextId > IdHandler.MAX_MONTAG_ID) {
      throw new IllegalStateException(
        "Max montag id reached! Cannot assign id " +
        nextId +
        " as it is above the maximum of " +
        IdHandler.MAX_MONTAG_ID
      );
    }

    this.lastAssignedMontagId = nextId;
    return nextId;
  }

  public int nextNametypeId() {
    int nextId = this.lastAssignedNametypeId+1;

    if (nextId > IdHandler.MAX_CUSTOM_NAMETYPE_ID) {
      throw new IllegalStateException(
        "Max nametype id reached! Cannot assign id " +
        nextId +
        " as it is above the maximum of " +
        IdHandler.MAX_CUSTOM_NAMETYPE_ID
      );
    }

    this.lastAssignedNametypeId = nextId;
    return nextId;
  }

  public int nextNationId() {
    int nextId = this.lastAssignedNationId+1;

    if (nextId > IdHandler.MAX_CUSTOM_NATION_ID) {
      throw new IllegalStateException(
        "Max nation id reached! Cannot assign id " +
        nextId +
        " as it is above the maximum of " +
        IdHandler.MAX_CUSTOM_NATION_ID
      );
    }

    this.lastAssignedNationId = nextId;
    return nextId;
  }

  public int nextSiteId() {
    int nextId = this.lastAssignedSiteId+1;

    if (nextId > IdHandler.MAX_CUSTOM_SITE_ID) {
      throw new IllegalStateException(
        "Max site id reached! Cannot assign id " +
        nextId +
        " as it is above the maximum of " +
        IdHandler.MAX_CUSTOM_SITE_ID
      );
    }

    this.lastAssignedSiteId = nextId;
    return nextId;
  }

  public int nextUnitId() {
    int nextId = this.lastAssignedUnitId+1;

    if (nextId > IdHandler.MAX_CUSTOM_MONSTER_ID) {
      throw new IllegalStateException(
        "Max unit id reached! Cannot assign id " +
        nextId +
        " as it is above the maximum of " +
        IdHandler.MAX_CUSTOM_MONSTER_ID
      );
    }

    this.lastAssignedUnitId = nextId;
    return nextId;
  }

  public int nextWeaponId() {
    int nextId = this.lastAssignedWeaponId+1;

    if (nextId > IdHandler.MAX_CUSTOM_WEAPON_ID) {
      throw new IllegalStateException(
        "Max weapon id reached! Cannot assign id " +
        nextId +
        " as it is above the maximum of " +
        IdHandler.MAX_CUSTOM_WEAPON_ID
      );
    }

    this.lastAssignedWeaponId = nextId;
    return nextId;
  }

  static private boolean isCustomGameId(int id, int minInclusive, int maxInclusive) {
    return id >= minInclusive && id <= maxInclusive;
  }

  static public boolean isCustomArmorGameId(int id) {
    return isCustomGameId(id, IdHandler.MIN_CUSTOM_ARMOR_ID, IdHandler.MAX_CUSTOM_ARMOR_ID);
  }

  static public boolean isCustomWeaponGameId(int id) {
    return isCustomGameId(id, IdHandler.MIN_CUSTOM_WEAPON_ID, IdHandler.MAX_CUSTOM_WEAPON_ID);
  }
}

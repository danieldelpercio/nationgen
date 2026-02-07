package nationGen.items;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.elmokki.Dom3DB;
import com.elmokki.Generic;

import nationGen.CustomItemsHandler;
import nationGen.NationGen;
import nationGen.chances.ThemeInc;
import nationGen.entities.Drawable;
import nationGen.entities.Filter;
import nationGen.misc.Command;

public class Item extends Drawable {

  private Integer dominionsId = -1;
  protected Boolean pendingDominionsId = false;

  private CustomItem equippedItem;

  public Filter filter = null;
  private List<ItemType> itemTypes = new ArrayList<>();
  private String bardingId;

  public List<ItemDependency> dependencies = new ArrayList<>();
  //public LinkedHashMap<String, String> dependencies = new LinkedHashMap<>();
  //public LinkedHashMap<String, String> typedependencies = new LinkedHashMap<>();

  public String slot = "";
  public String set = "";

  public Item(NationGen nationGen) {
    super(nationGen);
  }

  public Item(Item item) {
    super(item);
    this.dominionsId = item.dominionsId;
    this.pendingDominionsId = item.pendingDominionsId;
    this.filter = (item.filter != null) ? new Filter(item.filter) : null;
    this.itemTypes = new ArrayList<>(item.itemTypes);
    this.dependencies = new ArrayList<>(item.dependencies)
      .stream()
      .map(d -> new ItemDependency(d))
      .collect(Collectors.toList());

    this.slot = item.slot;
    this.set = item.set;
  }

  public Integer getDominionsId() {
    return this.dominionsId;
  }

  public void setDominionsId(Integer id) {
    if (id == null || id <= -1) {
      throw new IllegalArgumentException("Invalid Dominions id assignment for item " + this.name + "! (invalid id: " + id + ")");
    }

    this.dominionsId = id;
  }

  public String getValueFromDb(String dbColumn, String defaultValue) {
    String value = this.getValueFromDb(dbColumn);

    if (value.isBlank()) {
      return defaultValue;
    }

    return value;
  }

  public String getValueFromDb(String dbColumn) {
    Dom3DB db = this.isArmor() ? this.nationGen.armordb : this.nationGen.weapondb;
    String itemIdInDb = String.valueOf(this.dominionsId);
    String value = db.GetValue(itemIdInDb, dbColumn);

    if (value.isBlank()) {
      itemIdInDb = this.name;
      value = db.GetValue(itemIdInDb, dbColumn);
    }

    return value;
  }

  public Integer getIntegerFromDb(String dbColumn, Integer defaultValue) {
    String value = this.getValueFromDb(dbColumn, "");
    Integer parsedValue;

    if (value.isBlank()) {
      return defaultValue;
    }

    try {
      parsedValue = Integer.parseInt(value);
      return parsedValue;
    }

    catch(Exception e) {
      return defaultValue;
    }
  }

  public String getBardingId() {
    return this.bardingId;
  }

  public Boolean anyTypesMatchString(String typeStr) {
    return this.itemTypes.stream().anyMatch(t -> t.getId() == typeStr);
  }

  public void addType(ItemType type) {
    if (this.hasType(type) == true) {
      return;
    }

    this.itemTypes.add(type);
  }

  public void addType(List<ItemType> types) {
    types.forEach(t -> this.addType(t));
  }

  public Boolean hasType(ItemType type) {
    return this.itemTypes.contains(type);
  }

  public Boolean isOfType(ItemType type) {
    Boolean hasType = this.hasType(type);

    if (hasType == false && type.check(this) == true) {
      hasType = true;
      this.itemTypes.add(type);
    }

    return hasType;
  }

  // Armor type for now has to be determined at file-parsing time
  // through the #armor tag. This is because armor and weapon DBs
  // share ids, so any given item id will be present on both.
  public Boolean isArmor() {
    return this.hasType(ItemType.ARMOR);
  }
  
  public Boolean isBarding() {
    return this.isArmor() && this.isOfType(ItemType.BARDING);
  }
  
  public Boolean isBodyArmor() {
    return this.isArmor() && this.isOfType(ItemType.BODY_ARMOR);
  }
  
  public Boolean isHelmet() {
    return this.isArmor() && this.isOfType(ItemType.HELMET);
  }
  
  public Boolean isShield() {
    return this.isArmor() && this.isOfType(ItemType.SHIELD);
  }

  public Boolean isWeapon() {
    return this.isOfType(ItemType.WEAPON);
  }

  public Boolean isRangedWeapon() {
    return this.isOfType(ItemType.RANGED);
  }

  public Boolean isLowAmmoWeapon() {
    return this.isOfType(ItemType.LOW_SHOTS);
  }

  public Boolean isMeleeWeapon() {
    return this.isOfType(ItemType.MELEE);
  }

  public Boolean isOneHandedWeapon() {
    return this.getIntegerFromDb("2h", 0) != 1;
  }

  public Boolean isTwoHandedWeapon() {
    return this.getIntegerFromDb("2h", 0) == 1;
  }

  public Boolean isMountItem() {
    return this.isOfType(ItemType.MOUNT);
  }

  public Boolean isVanillaItem() {
    if (this.dominionsId <= 0) {
      return false;
    }

    else if (this.isArmor()) {
      if (this.dominionsId < CustomItemsHandler.CUSTOM_ARMOR_DOMINIONS_ID_START) {
        return true;
      }
    }

    else if (this.isWeapon()) {
      if (this.dominionsId < CustomItemsHandler.CUSTOM_WEAPON_DOMINIONS_ID_START) {
        return true;
      }
    }

    return false;
  }

  public List<ItemType> getItemTypes() {
    return this.itemTypes;
  }

  public Boolean isPendingDominionsIdAssignemnt() {
    return this.pendingDominionsId;
  }

  /**
   * A "Dominions id" is an id above 0 to be written into an #armor or #weapon
   * modding command. For example, A "basesprite" item does not have a
   * Dominions id, but an equipped Greatsword does. "Item" is a misleading
   * name in NationGen, since it can refer both to the equipped Dominions
   * items as well as to cosmetic sprite parts, such as "hands" or "shadow".
   * @return
   */
  public Boolean hasDominionsId() {
    return this.dominionsId > 0;
  }

  public Boolean hasDominionsId(Integer id) {
    return this.dominionsId == id;
  }

  public Boolean hasDominionsId(String id) {
    if (!Generic.isNumeric(id)) {
      throw new IllegalArgumentException("Expected id to be a numeric String! Instead got '" + id + "\"");
    }

    return this.dominionsId == Integer.parseInt(id);
  }

  /**
   * NationGen Item class instances which are meant to eventually result in an equipped
   * in-game Dominions item (say, a custom-made greatsword) must, at some point in the
   * program, have a custom Dominions id assigned to them, which will be used in the
   * relevant mod commands when writing the final .dm file.
   * @return
   */
  public Boolean isDominionsIdResolved() {
    return this.dominionsId > 0 && this.pendingDominionsId == false;
  }

  public Boolean isSameAs(Item item) {
    Integer otherDominionsId = item.getDominionsId();
    Boolean hasSameName = this.name.equals(item.name);
    Boolean hasSameDominionsId = this.hasDominionsId(otherDominionsId);
    Boolean bothDominionsIdsAssigned = this.hasDominionsId() && item.hasDominionsId();

    if (hasSameName) {
      if (bothDominionsIdsAssigned == false || hasSameDominionsId == true) {
        return true;
      }

      throw new IllegalArgumentException("Items " + this.name + " and " + item.name + " share the same name, but their dominionsId is different!");
    }

    return false;
  }

  /**
   * Returns a copy of this item with its dominions id resolved. For example, we have the custom item
   * with a nationGenId "atl_conchshield". When resolving it, it will get a dominionsId Integer assigned,
   * such as 5008.
   * 
   * Note that this returns a copy of the item because we don't want to modify the original, as that is
   * (probably) an asset instance that gets equipped with the same reference every time.
   * 
   * @param item - an Item instance
   * @return a copy of the item with a dominions id, or the same item if it already had one
   */
  static public Item assignDominionsId(Item item) {
    if (item.isDominionsIdResolved()) {
      return item;
    }

    Item copy = new Item(item);
    CustomItemsHandler customItemsHandler = item.nationGen.GetCustomItemsHandler();
    Integer dominionsId = customItemsHandler.resolveDominionsId(item.getName());
    copy.dominionsId = dominionsId;
    copy.pendingDominionsId = false;
    return copy;
  }

  @Override
  public void handleOwnCommand(Command command) {
    try {
      switch (command.command) {
        case "#dominionsId":
          this.dominionsId = command.args.get(0).getInt();
          break;
        case "#needsDominionsId":
          this.pendingDominionsId = true;
          break;
        case "#armor":
          this.itemTypes.add(ItemType.ARMOR);
          break;
        case "#barding":
          this.bardingId = command.args.getString(0);
          this.itemTypes.add(ItemType.BARDING);
          break;
        case "#mountmnr":
          this.itemTypes.add(ItemType.MOUNT);
          this.commands.add(command);
          break;
        case "#addthemeinc":
          if (this.filter == null) {
            this.filter = new Filter(nationGen);
            filter.tags.addName("do_not_show_in_descriptions");
            if (this.name != null) filter.name = "Item " +
            this.name +
            " generation effects";
          }
          this.filter.themeincs.add(ThemeInc.from(command.args));
          break;
        case "#name":
          if (filter != null) filter.name = "Item " +
          command.args.get(0).get() +
          " generation effects";
          super.handleOwnCommand(command);
          break;
        case "#needs":
          this.dependencies.add(
              new ItemDependency(
                command.args.get(0).get(),
                command.args.get(1).get(),
                false,
                false
              )
            );
          break;
        case "#needstype":
          this.dependencies.add(
              new ItemDependency(
                command.args.get(0).get(),
                command.args.get(1).get(),
                true,
                false
              )
            );
          break;
        case "#forceslot":
          this.dependencies.add(
              new ItemDependency(
                command.args.get(0).get(),
                command.args.get(1).get(),
                false,
                true
              )
            );
          break;
        case "#forceslottype":
          this.dependencies.add(
              new ItemDependency(
                command.args.get(0).get(),
                command.args.get(1).get(),
                true,
                true
              )
            );
          break;
        case "#command":
        case "#define":
          if (command.args.size() != 1) {
            throw new IllegalArgumentException(
              "#command or #define must have a single arg. Surround the command with quotes if needed."
            );
          }
          this.commands.add(command.args.get(0).getCommand());
          break;
        default:
          super.handleOwnCommand(command);
          break;
      }
    } catch (IndexOutOfBoundsException e) {
      throw new IllegalArgumentException(
        "Command [" +
        command +
        "] has insufficient arguments (" +
        this.name +
        ")",
        e
      );
    }
  }

  @Override
  protected void finish() {
    if (this.hasDominionsId() == false) {
      return;
    }

    if (this.isArmor()) {
      return;
    }

    this.addType(ItemType.WEAPON);

    if (ItemType.RANGED.check(this)) {
      this.addType(ItemType.RANGED);
    }

    else {
      this.addType(ItemType.MELEE);
    }
  }
}

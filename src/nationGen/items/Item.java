package nationGen.items;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.elmokki.Dom3DB;

import nationGen.CustomItemsHandler;
import nationGen.NationGen;
import nationGen.chances.ThemeInc;
import nationGen.entities.Drawable;
import nationGen.entities.Filter;
import nationGen.misc.Command;

public class Item extends Drawable {

  public String id = "-1";

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
    this.id = item.id;
    this.filter = (item.filter != null) ? new Filter(item.filter) : null;
    this.itemTypes = new ArrayList<>(item.itemTypes);
    this.dependencies = new ArrayList<>(item.dependencies)
      .stream()
      .map(d -> new ItemDependency(d))
      .collect(Collectors.toList());

    this.slot = item.slot;
    this.set = item.set;
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
    String itemIdInDb = this.id;
    String value = db.GetValue(itemIdInDb, dbColumn);

    if (value.isBlank()) {
      itemIdInDb = this.name;
      value = db.GetValue(itemIdInDb, dbColumn);
    }

    if (value.isBlank()) {
      itemIdInDb = this.tags.getString("OLDID").orElse("");
      value = db.GetValue(itemIdInDb, dbColumn);
    }

    return value;
  }

  public String getBardingId() {
    return this.bardingId;
  }

  public Boolean anyTypesMatchString(String typeStr) {
    return this.itemTypes.stream().anyMatch(t -> t.getId() == typeStr);
  }

  public void addType(ItemType type) {
    if (this.hasType(type) == false) {
      return;
    }

    this.itemTypes.add(type);
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
  
  public Boolean isBodyArmorBarding() {
    return this.isArmor() && this.isOfType(ItemType.BODY_ARMOR);
  }
  
  public Boolean hasHelmet() {
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

  public Boolean isMountItem() {
    return this.isOfType(ItemType.MOUNT);
  }

  public List<ItemType> getItemTypes() {
    return this.itemTypes;
  }

  /**
   * A "Dominions id" is an id above 0 which can or must be written into
   * an #armor or #weapon command. A "basesprite" item does not have a
   * Dominions id, but an equipped Greatsword does. "Item" is a misleading
   * name in NationGen, since it can refer both to the equipped Dominions
   * items as well as to cosmetic sprite parts, such as "hands" or "shadow".
   * @return
   */
  public Boolean hasDominionsId() {
    return this.id.equals("-1") == false;
  }

  /**
   * A custom id is a NationGen-specific id, such as "obsidian_barding",
   * which must be resolved into a numeric id when writing the final mod
   * .dm file where the custom item is defined.
   * @return
   */
  public boolean isCustomIdResolved() {
    return CustomItemsHandler.isIdResolved(this.id);
  }

  /**
   * Returns a copy of this item with its custom id resolved. For example, we have the custom item
   * with id atl_conchshield, once it's resolved to a Dominions parsable id it'll be a number, such
   * as 5008. We assign that number here and return the copy. Note that we need to return a copy of
   * the item with the original customId because we don't want to modify the original, as that is
   * (probably) an asset instance that gets equipped with the same reference every time.
   * 
   * @param item - an item with a custom id, such as "atl_conchshield"
   * @return a copy of the item with a resolved id, or the same item of the id was already resolved
   */
  static public Item resolveId(Item item) {
    if (item.isCustomIdResolved() == false) {
      Item copy = new Item(item);
      copy.tags.add("OLDID", item.id);
      copy.id = item.nationGen.GetCustomItemsHandler().getCustomItemId(item.id);
      return copy;
    }

    // If the id is already numeric, like 5008, it should have
    // already been resolved, so we just a copy of the item
    else {
      return item;
    }
  }

  @Override
  public void handleOwnCommand(Command command) {
    try {
      switch (command.command) {
        case "#gameid":
          this.id = command.args.get(0).get();
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

    String range = this.getValueFromDb("rng", "0");
    this.addType(ItemType.WEAPON);

    if (Integer.parseInt(range) > 0) {
      this.addType(ItemType.RANGED);
    }

    else {
      this.addType(ItemType.MELEE);
    }
  }
}

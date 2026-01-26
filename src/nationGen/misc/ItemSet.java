package nationGen.misc;

import com.elmokki.Dom3DB;
import java.util.*;
import java.util.stream.Collectors;
import nationGen.entities.Pose;
import nationGen.entities.Race;
import nationGen.items.Item;
import nationGen.units.Unit;

public class ItemSet extends ArrayList<Item> {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  /**
   * Adds items of set to the new set
   * @param set
   */
  public ItemSet(ItemSet set) {
    addAll(set);
  }

  public ItemSet() {}

  public boolean addAll(ItemSet set) {
    return super.addAll(set);
  }

  @Override
  public boolean addAll(Collection<? extends Item> c) {
    return super.addAll(
      c.stream().filter(Objects::nonNull).collect(Collectors.toList())
    );
  }

  // Overwriting add to avoid nulls
  public boolean add(Item value) {
    if (value != null) return super.add(value);

    //System.out.println("Tried to add null to an itemset.");
    return false;
  }

  public ItemSet filterProt(Dom3DB armordb, int min, int max) {
    return filterProt(armordb, min, max, false);
  }

  public ItemSet filterKeepSameSprite(ItemSet old) {
    ItemSet newlist = new ItemSet();

    for (Item i : this) for (Item i2 : old) if (
      i.sprite.equals(i2.sprite)
    ) newlist.add(i);

    return newlist;
  }

  public ItemSet filterRemoveSameSprite(ItemSet old) {
    ItemSet newlist = new ItemSet();
    newlist.addAll(this);

    for (Item i : this) for (Item i2 : old) if (
      i.sprite.equals(i2.sprite)
    ) newlist.remove(i);

    return newlist;
  }

  public ItemSet filterProt(
    Dom3DB armordb,
    int min,
    int max,
    boolean includeDef
  ) {
    ItemSet newlist = new ItemSet();
    for (Item i : this) {
      int prot = i.getIntegerFromDb("prot", 0);
      if (
        includeDef
          ? (prot + i.getIntegerFromDb("def", 0) <= max && prot >= min)
          : (prot <= max && prot >= min)
      ) {
        newlist.add(i);
      }
    }
    return newlist;
  }

  public ItemSet filterDef(Dom3DB armordb, int min, int max) {
    ItemSet newlist = new ItemSet();
    for (Item i : this) if (
      i.getIntegerFromDb("def", 0) <= max &&
      i.getIntegerFromDb("def", 0) >= min
    ) newlist.add(i);

    return newlist;
  }

  public ItemSet filterMinMaxProt(int itemprot) {
    ItemSet newlist = new ItemSet();
    for (Item i : this) {
      int minprot = i.tags.getInt("minprot").orElse(0);
      int maxprot = i.tags.getInt("maxprot").orElse(100);

      if (itemprot >= minprot && itemprot <= maxprot) newlist.add(i);
    }

    return newlist;
  }

  public Item getItemWithID(String id, String slot) {
    ItemSet possibles = this; //this.filterTheme("elite", false).filterTheme("sacred", false);

    for (Item item : possibles) {
      Boolean idMatches = item.hasDominionsId(id) || item.name.equals(id);

      if (idMatches && item.slot.equals(slot)) {
        return item;
      }
    }
    return null;
  }

  public ItemSet getItemsWithID(String id, String slot) {
    ItemSet matchingItems = new ItemSet();
    ItemSet possibles = this; //this.filterTheme("elite", false).filterTheme("sacred", false);

    for (Item item : possibles) {
      Item matchingItem = this.getItemWithID(id, slot);

      if (matchingItem != null) {
        matchingItems.add(item);
      }
    }

    return matchingItems;
  }

  public Item getItemWithName(String name, String slot) {
    for (Item item : this) {
      if (item.name.equals(name) && item.slot.equals(slot)) return item;
    }
    return null;
  }

  public boolean alreadyHas(Item item) {
    if (item == null) {
      return true;
    }

    for (Item i : this) {
      if (item.isSameAs(i)) {
        return true;
      }
    }

    return false;
  }

  /**
   * Filters a database by whether the items in it include or exclude
   * the provided property value.
   * 
   * @param filterProperty The property name that we're querying 
   * @param filterValue The desired value in a given item we're filtering for
   * @param exclude If value == wanted, exclude from list instead
   * @param db The Dom3DB to query
   * @return A list only including (or excluding) the relevant items
   */
  public ItemSet filterDom3DB(
    String filterProperty,
    String filterValue,
    boolean exclude,
    Dom3DB db
  ) {
    ItemSet newlist = new ItemSet();

    for (Item i : this) {
      String itemValue = i.getValueFromDb(filterProperty, "0");
      Boolean isEqualValue = itemValue.equals(filterValue);
      Boolean shouldKeepItem = isEqualValue == true && exclude == false;

      if (shouldKeepItem) {
        newlist.add(i);
      }
    }

    return newlist;
  }

  /**
   * Filters a database by whether the items in it have a property value
   * that is higher than the provided filterValue.
   * 
   * @param filterProperty The property name that we're querying 
   * @param filterValue The desired value in a given item we're filtering for
   * @param hasToBeLower Include items whose value is lower than filterValue instead
   * @param db The Dom3DB to query
   * @return A list only including (or excluding) the relevant items
   */
  public ItemSet filterDom3DBInteger(
    String filterProperty,
    int filterValue,
    boolean hasToBeLower,
    Dom3DB db
  ) {
    ItemSet newlist = new ItemSet();

    for (Item i : this) {
      Integer itemValue = i.getIntegerFromDb(filterProperty, 0);

      if (hasToBeLower && itemValue < filterValue) {
        newlist.add(i);
      }
        
      else if (!hasToBeLower && itemValue > filterValue) {
        newlist.add(i);
      }
    }

    return newlist;
  }

  public ItemSet filterForRole(String role, Race race) {
    ItemSet filtered = new ItemSet();

    for (Item item : this) {
      for (Pose pose : race.poses) {
        if (!pose.roles.contains(role)) {
          continue;
        }

        if (pose.getItems(item.slot) == null) {
          continue;
        }

        for (Item poseItem : pose.getItems(item.slot)) {
          if (poseItem.isSameAs(item) && item.hasDominionsId() == false) {
            filtered.add(poseItem);
          }

          else if (poseItem.isSameAs(item)) {
            filtered.add(poseItem);
          }

          else if (poseItem.isSameAs(item) && item.sprite.equals(poseItem.sprite)) {
            filtered.add(poseItem);
          }
        }
      }
    }

    return filtered;
  }

  public ItemSet filterForPosesWith(String role, Race race, Item olditem) {
    ItemSet newlist = new ItemSet();
    for (Item i : this) {
      for (Pose p : race.poses) {
        if (
          !p.roles.contains(role) ||
          p.getItems(olditem.slot) == null ||
          !p.getItems(olditem.slot).contains(olditem)
        ) continue;

        if (
          p.getItems(i.slot) != null &&
          p.getItems(i.slot).contains(i) &&
          !newlist.contains(i)
        ) newlist.add(i);
      }
    }

    return newlist;
  }

  public Item getRandom(ChanceIncHandler ch, Unit u, Random random) {
    return ch.handleChanceIncs(u, this).getRandom(random);
  }

  public ItemSet filterForPose(Pose pose) {
    ItemSet filtered = new ItemSet();

    for (Item item : this) {
      if (pose.getItems(item.slot) == null) {
        continue;
      }
        
      for (Item poseItem : pose.getItems(item.slot)) {
        Boolean sharesName = item.name.equals(poseItem.name) || item.sprite.equals(poseItem.name);
        Boolean isReplacement = poseItem.tags.contains("replacement", item.name);

        if (item.isSameAs(poseItem) && (sharesName || isReplacement)) {
          filtered.add(poseItem);
        }
      }
    }

    if (filtered.possibleItems() == 0) {
      for (Item item : this) {
        if (pose.getItems(item.slot) == null) {
          continue; 
        }

      for (Item poseItem : pose.getItems(item.slot)) {
        if (item.isSameAs(poseItem) && item.hasDominionsId()) {
          filtered.add(poseItem);
        }
        
        else if (
            item.getGameId().equals(poseItem.getGameId()) &&
            (item.name.equals(poseItem.name) || item.sprite.equals(poseItem.sprite))
          ) filtered.add(poseItem);
        }
      }
    }

    return filtered;
  }

  public ItemSet getCopy() {
    return new ItemSet(this);
  }

  public ItemSet filterSlot(String slot) {
    ItemSet newlist = new ItemSet();
    for (Item i : this) if (i.slot.equals(slot)) newlist.add(i);

    return newlist;
  }

  public ItemSet filterTag(Command tagLine) {
    ItemSet newlist = new ItemSet();
    for (Item i : this) if (i.tags.containsTag(tagLine)) newlist.add(i);

    return newlist;
  }

  public ItemSet filterOutTag(Command tagLine) {
    ItemSet newlist = new ItemSet();
    for (Item i : this) if (!i.tags.containsTag(tagLine)) newlist.add(i);

    return newlist;
  }

  public ItemSet filterTheme(String tag, boolean keepTag) {
    ItemSet newlist = new ItemSet();
    for (Item i : this) if (i.themes.contains(tag) == keepTag) newlist.add(i);

    return newlist;
  }

  public int possibleItems() {
    int items = 0;
    for (Item item : this) if (item.basechance > 0) items++;

    return items;
  }

  public ItemSet filterArmor(boolean keepArmor) {
    ItemSet newlist = new ItemSet();
    for (Item i : this) if (i.isArmor() == keepArmor) newlist.add(i);

    return newlist;
  }

  public ItemSet filterAbstracts() {
    ItemSet newlist = new ItemSet();
    for (Item i : this) if (i.hasDominionsId()) newlist.add(i);

    return newlist;
  }

  public ItemSet filterImpossibleAdditions(ItemSet old) {
    if (old == null) return this;

    List<Item> list = new ArrayList<>(old);
    return filterImpossibleAdditions(list);
  }

  public ItemSet filterImpossibleAdditions(List<Item> list) {
    ItemSet newlist = new ItemSet();
    newlist.addAll(this);

    for (Item i : list) {
      newlist.remove(i);

      // Remove stuff with the same ids or names or customid tag
      List<Item> derps = new ArrayList<>();

      for (Item i2 : newlist) {
        if (
          i2.isArmor() == i.isArmor() &&
          i2.getGameId().equals(i.getGameId()) &&
          i.slot.equals(i2.slot) &&
          i.hasDominionsId()
        ) derps.add(i2);
        else if (i2.name.equals(i.name) && i.slot.equals(i2.slot)) derps.add(
          i2
        );
      }

      for (Item i2 : derps) newlist.remove(i2);
    }
    return newlist;
  }
}

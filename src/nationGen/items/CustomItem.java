package nationGen.items;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import nationGen.NationGen;
import nationGen.entities.MagicItem;
import nationGen.misc.Arg;
import nationGen.misc.Args;
import nationGen.misc.Command;

public class CustomItem extends Item {

  public List<Command> values = new ArrayList<>();
  public Item olditem = null;

  public CustomItem getCopy() {
    CustomItem item = this.getCustomItemCopy();
    item.olditem = this.olditem;
    for (Command command : values) item.values.add(command.copy());

    return item;
  }

  public MagicItem magicItem = null;

  public CustomItem(NationGen nationGen) {
    super(nationGen);
    this.values.add(new Command("#rcost", new Arg(0)));
    this.values.add(new Command("#def", new Arg(0)));
  }

  public Optional<Args> getValue(String commandName) {
    return this.values.stream()
      .filter(c -> c.command.equals(commandName))
      .findFirst()
      .map(c -> c.args);
  }

  public Optional<String> getStringValue(String commandName) {
    return getValue(commandName).map(l -> l.get(0).get());
  }

  public Optional<Integer> getIntValue(String commandName) {
    return getValue(commandName).map(l -> l.get(0).getInt());
  }

  public void setValue(String commandName) {
    if (getValue(commandName).isEmpty()) {
      this.values.add(new Command(commandName));
    }
  }

  public void setValue(String commandName, String value) {
    Optional<Args> args = getValue(commandName);
    if (args.isPresent()) {
      args.get().set(0, new Arg(value));
    } else {
      this.values.add(Command.args(commandName, value));
    }
  }

  public void setValue(String commandName, int value) {
    Optional<Args> args = getValue(commandName);
    if (args.isPresent()) {
      args.get().set(0, new Arg(value));
    } else {
      this.values.add(new Command(commandName, new Arg(value)));
    }
  }

  public void setValue(String commandName, String value1, String value2) {
    Optional<Args> args = getValue(commandName);
    if (args.isPresent()) {
      args.get().set(0, new Arg(value1));
      args.get().set(1, new Arg(value2));
    } else {
      this.values.add(Command.args(commandName, value1, value2));
    }
  }

  public Boolean hasDamageBitmask() {
    return this.values
      .stream()
      .filter(c -> {
        return WeaponNumericalDamageType.isModCommandANumericalDamageType(c);
      })
      .count() > 0;
  }

  @Override
  public void handleOwnCommand(Command str) {
    try {
      if (str.command.equals("#command")) {
        if (str.args.size() != 1) {
          throw new IllegalArgumentException(
            "#command must have a single arg. Surround the command with quotes if needed."
          );
        }
        this.values.add(str.args.get(0).getCommand());
      } else {
        super.handleOwnCommand(str);
      }
    } catch (IndexOutOfBoundsException e) {
      throw new IllegalArgumentException(
        "Wrong number of arguments for line: " + str,
        e
      );
    }
  }

  public LinkedHashMap<String, String> getHashMap() {
    LinkedHashMap<String, String> map = new LinkedHashMap<>();
    map.put("id#", id + "");
    map.put("#att", "1");
    map.put("shots", "0");
    map.put("rng", "0");
    map.put("att", "0");
    map.put("def", "0");
    map.put("lgt", "0");
    map.put("dmg", "0");
    map.put("2h", "0");

    for (Command command : values) {
      String str = command.command;

      switch (str) {
        case "#blunt":
          map.put("dt_blunt", "1");
          break;
        case "#pierce":
          map.put("dt_pierce", "1");
          break;
        case "#slash":
          map.put("dt_slash", "1");
          break;
        case "#ironarmor":
          map.put("ferrous", "1");
          break;
        case "#secondaryeffectalways":
          map.put("aeff#", command.args.get(0).get());
          break;
        case "#secondaryeffect":
          map.put("eff#", command.args.get(0).get());
          break;
        case "#twohanded":
          map.put("2h", "1");
          break;
        case "#charge":
          map.put("charge", "1");
          break;
        case "#bonus":
          map.put("bonus", "1");
          break;
        case "#dt_cap":
          map.put("dt_cap", "1");
          break;
        case "#magic":
          map.put("magic", "1");
          break;
        case "#ammo":
          map.put("shots", command.args.get(0).get());
          break;
        case "#armorpiercing":
          map.put("ap", "1");
          break;
        case "#armornegating":
          map.put("an", "1");
          break;
        case "#range":
          map.put("rng", command.args.get(0).get());
          break;
        case "#len":
          map.put("lgt", command.args.get(0).get());
          break;
        case "#nratt":
          map.put("#att", command.args.get(0).get());
          break;
        case "#rcost":
          map.put("res", command.args.get(0).get());
          break;
        case "#name":
          if (this.armor) {
            map.put("armorname", command.args.get(0).get());
          } else {
            map.put("weapon_name", command.args.get(0).get());
          }
          break;
        case "#flyspr":
          map.put("flyspr", command.args.get(0).get());
          map.put("animlength", command.args.get(1).get());
          break;
        default:
          map.put(
            command.command.substring(1),
            command.args.isEmpty() ? "1" : command.args.get(0).get()
          );
          break;
      }
    }

    return map;
  }

  public List<String> writeLines() {
    List<String> lines = new ArrayList<>();

    if (armor) {
      lines.add("#newarmor " + id);
    }

    else {
      lines.add("#newweapon " + id);
    }

    for (Command command : this.values) {
      if (command.command.equals("#name")) {
        lines.add(command.toModLine());
      }
    }

    for (Command command : this.values) {
      if (!command.command.equals("#name")) {
        lines.add(command.toModLine());
      }
    }

    lines.add("#end");
    lines.add("");

    return lines;
  }

  public static CustomItem fromItem(Item item, NationGen nationGen) {
    CustomItem customItem = new CustomItem(nationGen);

    // Minimal valid weapon values. Might get overwritten below if already exist
    if (item.armor == false) {
      customItem.setValue("#att", 0);
      customItem.setValue("#len", 0);
      customItem.setValue("#dmg", 0);
    }

    customItem.sprite = item.sprite;
    customItem.mask = item.mask;
    customItem.commands.addAll(item.commands);
    customItem.tags.addAll(item.tags);
    customItem.dependencies.addAll(item.dependencies);
    customItem.setOffsetX(item.getOffsetX());
    customItem.setOffsetY(item.getOffsetY());
    customItem.slot = item.slot;
    customItem.basechance = item.basechance;
    customItem.renderslot = item.renderslot;
    customItem.renderprio = item.renderprio;
    customItem.armor = item.armor;
    customItem.olditem = item;

    return customItem;
  }
}

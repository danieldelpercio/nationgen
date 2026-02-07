package nationGen.items.properties;

import java.util.stream.Stream;
import nationGen.misc.Command;

public enum ArmorProperty {
  NAME("armorname", "#name"),
  TYPE("type", "#type"),
  PROTECTION("prot", "#prot"),
  PROTECTION_PARTS("prot", "#protparts"),
  DEFENCE("def", "#def"),
  ENCUMBRANCE("enc", "#enc"),
  RESOURCE_COST("res", "#rcost"),
  IS_IRON("ferrous", "#ironarmor"),
  IS_MAGIC("magic", "#magicarmor"),
  IS_WOOD("flammable", "#woodenarmor");

  private String dbColumn;
  private String modCommand;
  private Boolean isBooleanProperty = false;

  ArmorProperty(String dbColumn, String modCommand) {
    this.dbColumn = dbColumn;
    this.modCommand = modCommand;
  }

  ArmorProperty(String dbColumn, String modCommand, Boolean isBoolean) {
    this.dbColumn = dbColumn;
    this.modCommand = modCommand;
    this.isBooleanProperty = isBoolean;
  }

  public String toModCommand() {
    return this.modCommand;
  }

  public String toDBColumn() {
    return this.dbColumn;
  }

  public Boolean isBoolean() {
    return this.isBooleanProperty;
  }

  public static ArmorProperty fromCommand(String command) {
    return Stream.of(ArmorProperty.values())
      .filter(prop -> {
        return prop.toModCommand().equals(command);
      })
      .findFirst()
      .orElse(null);
  }

  public static ArmorProperty fromCommand(Command command) {
    return fromCommand(command.command);
  }
}

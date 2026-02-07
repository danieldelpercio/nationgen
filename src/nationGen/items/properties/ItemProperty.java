package nationGen.items.properties;

import java.util.stream.Stream;
import nationGen.misc.Command;

public class ItemProperty {
  public String toModCommand() {
    return this.modCommand;
  }

  public String toDBColumn() {
    return this.dbColumn;
  }

  public Boolean isBoolean() {
    return this.isBooleanProperty;
  }

  public static ItemProperty fromCommand(String command) {
    return Stream.of(ItemProperty.values())
      .filter(prop -> {
        return prop.toModCommand().equals(command);
      })
      .findFirst()
      .orElse(null);
  }

  public static ItemProperty fromCommand(Command command) {
    return fromCommand(command.command);
  }
}

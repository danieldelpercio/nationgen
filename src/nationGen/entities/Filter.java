package nationGen.entities;

import java.util.ArrayList;
import java.util.List;
import nationGen.NationGen;
import nationGen.chances.ChanceInc;
import nationGen.chances.ThemeInc;
import nationGen.misc.Command;

public class Filter extends Entity {

  public List<Command> commands = new ArrayList<>();
  public List<ChanceInc> chanceincs = new ArrayList<>();
  public List<ThemeInc> themeincs = new ArrayList<>();
  public List<String> types = new ArrayList<>();
  public double power = 1;

  //used for descriptions
  public Filter description;
  public List<String> thesaurus = new ArrayList<>();
  public List<Filter> nextDesc = new ArrayList<>();
  public List<Filter> prevDesc = new ArrayList<>();
  public List<Filter> bridgeDesc = new ArrayList<>();
  public String descSet = "";

  public Filter(NationGen nationGen) {
    super(nationGen);
    description = this;
  }

  public List<Command> getCommands() {
    return this.commands;
  }

  public double getPower() {
    return this.power;
  }

  public String getName() {
    return this.name;
  }

  public Boolean isShapeshiftFilter() {
    return this.getCommands()
      .stream()
      .anyMatch(Command::isShapeshiftCommand);
  }

  public Boolean hasType(String type) {
    return this.types.contains(type);
  }

  public Boolean hasCategory(String category) {
    return this.hasType(category);
  }

  public Boolean sharesTypeWith(Filter otherFilter) {
    return sharesTypeWith(List.of(otherFilter));
  }

  public <E extends Filter> Boolean sharesTypeWith(List<E> otherFilters) {
    return otherFilters
      .stream()
      .anyMatch(otherFilter -> {
        return this.types
          .stream()
          .anyMatch(type -> otherFilter.hasType(type));
      });
  }

  public Boolean hasCommand(Command command) {
    String commandBase = command.command;
    return this.getCommands().stream().anyMatch(fc -> fc.command.equals(commandBase));
  }

  public Boolean hasAnyCommand() {
    return this.getCommands().isEmpty() == false;
  }

  @Override
  public void handleOwnCommand(Command command) {
    try {
      switch (command.command) {
        case "#command":
        case "#define":
          if (command.args.size() != 1) {
            throw new IllegalArgumentException(
              "#command or #define must have a single arg. Surround the command with quotes if needed."
            );
          }
          this.commands.add(command.args.get(0).getCommand());
          break;
        case "#themeinc":
          // Sometimes the definition is in quotes, sometimes it's not... -_-' < sigh
          if (command.args.size() == 1) {
            this.themeincs.add(ThemeInc.parse(command.args.get(0).get()));
          } else {
            this.themeincs.add(ThemeInc.from(command.args));
          }
          break;
        case "#type":
        case "#category":
          this.types.add(command.args.get(0).get());
          break;
        case "#chanceinc":
          // Sometimes the definition is in quotes, sometimes it's not... -_-' < sigh
          if (command.args.size() == 1) {
            this.chanceincs.add(ChanceInc.parse(command.args.get(0).get()));
          } else {
            this.chanceincs.add(ChanceInc.from(command.args));
          }
          break;
        case "#power":
          this.power = command.args.get(0).getInt();
          break;
        case "#set":
          this.descSet = command.args.get(0).get();
          break;
        case "#text":
        case "#synonym":
          this.thesaurus.add(command.args.get(0).get());
        default:
          super.handleOwnCommand(command);
          break;
      }
    } catch (Exception e) {
      throw new IllegalArgumentException(
        "Command [" + command + "] couldn't be handled (" + this.name + ")",
        e
      );
    }
  }
}

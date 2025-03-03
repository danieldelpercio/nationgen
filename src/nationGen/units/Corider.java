package nationGen.units;

import java.util.ArrayList;
import java.util.List;
import nationGen.NationGen;
import nationGen.entities.Filter;
import nationGen.misc.Command;

public class Corider extends Filter {

  public Corider(NationGen nationGen) {
    super(nationGen);
  }

  public List<Command> commands = new ArrayList<>();
  boolean nofeedback = false;
  boolean keepname = false;
  boolean nogcost = false;

  public Corider getCopy() {
    Corider mnt = new Corider(nationGen);
    mnt.basechance = basechance;
    mnt.name = name;
    mnt.types.addAll(types);
    mnt.tags.addAll(tags);
    mnt.themes.addAll(themes);
    mnt.nofeedback = nofeedback;
    mnt.keepname = keepname;
    mnt.nogcost = nogcost;
    mnt.commands.addAll(this.commands);
    return mnt;
  }

  @Override
  public void handleOwnCommand(Command command) {
    if (command.command.equals("#keepname")) {
      this.keepname = true;
      // Overrides filter implementation
    } else if (command.command.equals("#command")) {
      if (command.args.size() != 1) {
        throw new IllegalArgumentException(
          "#command or #define must have a single arg. Surround the command with quotes if needed."
        );
      }
      this.commands.add(command.args.get(0).getCommand());
    } else super.handleOwnCommand(command);
  }
}

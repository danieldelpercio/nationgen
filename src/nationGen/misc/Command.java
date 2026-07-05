package nationGen.misc;

import com.elmokki.Generic;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Command {

  public final String command;
  public final Args args;
  public final String comment;

  public Command(String cmd, Args args, String comment) {
    if (Generic.containsSpace(cmd)) {
      throw new IllegalArgumentException("Command name can't contain a space.");
    }

    this.command = cmd;
    this.args = args;
    this.comment = comment;
  }

  public Command(Command commandToCopy) {
    this.command = commandToCopy.command;
    this.args = new Args(commandToCopy.args);
    this.comment = commandToCopy.comment;
  }

  public Optional<CommandType> getType() {
    CommandType type = CommandType.fromRaw(this.command);

    if (type == null) {
      return Optional.empty();
    }

    return Optional.of(type);
  }

  public boolean sameTypeAs(Command other) {
    return this.getType().equals(other.getType());
  }

  public static String parseValueToAddString(int value) {
    String operator = (value > 0) ? "+" : "";
    return operator + value;
  }

  /**
   * Writes a String for how Dominions expects a mod command line to look like, with strings in quotes and comments
   * following dashes.
   * @return A string suitable to be written to a Dominions mod file.
   */
  public String toModLine() {
    return (
      this.command +
      (this.args.isEmpty() ? "" : " ") +
      this.args.stream()
        .map(a ->
          a.isNumeric()
            ? a.get()
            : Generic.quote(a.get().replaceAll("\"", "''"), '"')
        )
        .collect(Collectors.joining(" ")) +
      (this.comment != null ? (" --- " + this.comment) : "")
    );
  }

  public Boolean isShapeshiftCommand() {
    return this.command.equals("#shapechange") ||
      this.command.equals("#firstshape") ||
      this.command.equals("#secondshape") ||
      this.command.equals("#secondtmpshape");
  }

  public Boolean hasArgs() {
    return !this.args.isEmpty() && !this.args.getString(0).isBlank();
  }

  public Boolean isBoolean() {
    return this.args.isEmpty();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Command command1 = (Command) o;
    return command.equals(command1.command) && args.equals(command1.args);
  }

  // Tries to match only the base command string, regardless of actual value
  // i.e. #coldres contains the #coldres 5 command
  public Boolean contains(Command other) {
    return this.command.equals(other.command);
  }

  @Override
  public int hashCode() {
    return Objects.hash(command, args);
  }

  public String toString() {
    return this.command + (this.args.isEmpty() ? "" : " ") + this.args;
  }
}

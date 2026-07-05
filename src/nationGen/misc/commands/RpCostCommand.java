package nationGen.misc.commands;

import nationGen.misc.Arg;
import nationGen.misc.Args;
import nationGen.misc.Command;
import nationGen.misc.Operator;

public class RpCostCommand extends Command {
  public RpCostCommand(String cmd, Args args, String comment) {
    super(cmd, args, comment);
  }

  public RpCostCommand(Command commandToCopy) {
    super(commandToCopy);
  }

  @Override
  public Command combine(Command other) {
    Command combinedCommand = new Command(this);

    for (int i = 0; i < this.args.size(); i++) {
      Arg arg = combinedCommand.args.get(i);
      Arg otherArg = other.args.get(i);

      // By default, if no specific operator is given, set the value
      Operator operator = arg.getOperator().orElse(Operator.SET);
      Arg combinedValue = new Arg(arg.get());

      if (operator == Operator.ADD) {
        combinedValue = this.addArg(arg, otherArg);
      }

      else if (operator == Operator.SUBTRACT) {
        combinedValue = this.subtractArg(arg, otherArg);
      }
      
      else if (operator == Operator.MULTIPLY) {
        combinedValue = this.multiplyArg(arg, otherArg);
      }

      combinedCommand.args.set(i, combinedValue);
    }

    return combinedCommand;
  }

  @Override
  public Arg multiplyArg(Arg multiplierArg, Arg baseArg) {
    try {
      int value;
      double multiplier;
      int recPoints;
      
      if (baseArg.get().startsWith("%")) {
        value = 0;
      }

      else {
        multiplier = multiplierArg.getDouble();
        recPoints = baseArg.getInt();

        /**
         * Values at 1000 or above are special values. They trigger Dominions' AutoCalc.
         * AutoCalc recpoints values work roughly in steps of 499. When going over or under these steps, they
         * wrap around. For example, if 8000 autocalcs a unit's RPs to 13, then 8499 autocalcs to 512, but
         * 8500 autocals to 2 (the minimum RP value). To apply a multiplier to these autocalc values, we can
         * take the thousands (i.e. for 8000, that is 8) and apply the multiplier to that, then add/subtract
         * that resulting value from the total autocalc. It's not perfect, but it should approximate well enough.
         * Can't do it more accurately without working out the exact Dominions' AutoCalc formula.
         */
        if (recPoints >= 1000) {
            double inverse = multiplier - 1;
            int modifier = (int) Math.round(recPoints * 0.001 * inverse);
            value = recPoints + modifier;
        }

        /**
         * Values that are not AutoCalc will simply be multiplied as usual. For example, 30 RPs * 0.75 multiplier.
         */
        else {
            value = (int) Math.round(recPoints * multiplier);
        }
      }

      return new Arg(value);
    }
    
    catch (Exception e) {
      throw new IllegalArgumentException(
        "RpCostcommand MULTIPLY: " +
        multiplierArg +
        " * " +
        baseArg +
        " on '" +
        this.command +
        "' error",
        e
      );
    }
  }
}

package nationGen.nation;

import com.elmokki.Generic;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import nationGen.NationGen;
import nationGen.Settings.SettingsType;
import nationGen.misc.Command;
import nationGen.misc.Tags;
import nationGen.units.Unit;

public class PDSelector {

  private NationGen nationGen;
  private Nation nation;
  private Random random;

  // The amount of "points" we get for every 1 militia/starting unit.
  // This covers both gold and resource cost. If the budget is 20, and
  // the selected unit costs 10 gold and 10 resources, then we get 1 unit.
  private static final int BUDGET_PER_MILITIA_UNIT = 25;

  // The multiplier for the above budget. If the budget above is enough for
  // 1 unit, and this number is 40, then we'll get 40 * 1 = 40 units.
  // If the militia was more expensive and yielded a ratio of 0.5,
  // then we'd get 40 * 0.5 = 20 units.
  private static final int NUMBER_OF_RELATIVE_UNITS = 40;

  // Exponentially increases the budget cost of a unit's resources. More resource
  // cost means more protection which means much lower attrition when expanding,
  // so higher resource costs should be priced higher than low resource costs.
  private static final float RESOURCE_COST_INFLATION = 1.12f;

  public PDSelector(Nation n, NationGen nationGen) {
    this.nationGen = nationGen;
    this.nation = n;
    this.random = new Random(nation.random.nextInt());
  }

  public Unit getStartArmyCommander() {
    return getCommander(1, true);
  }

  private Unit getCommander(int rank, boolean startarmy) {
    if (rank < 1 || rank > 2) {
      throw new IllegalArgumentException(
        "Expected PD commander rank to be between 1 and 2 (inclusive); instead got " + rank
      );
    }

    List<Unit> commanders = nation.listCommanders("commander");

    // Chance to get a priest as potential commander for PD; ~75% of Dom6 nations have a caster (usually priest or mage-priest as PD rank 2)
    // For now, there's a small chance to a T2 - and a very good chance to get a T1 - for rank 2, and much smaller chances a T1 for rank 1
    if (!startarmy) {
      List<Unit> priests = nation.listCommanders("priest");
      double rand = random.nextDouble();
      if (rank == 1 && rand < 0.05) commanders.add(0, priests.get(0));
      else if (rank == 2 && rand < 0.15 && priests.size() > 1) commanders.add(
        1,
        priests.get(1)
      );
      else if (rank == 2 && rand < 0.75) commanders.add(1, priests.get(0));
    }

    List<Unit> others = new ArrayList<>();
    if (startarmy) {
      others.add(getMilitia(1, 1, !startarmy));
      others.add(getMilitia(1, 2, !startarmy));
    } else if (rank == 1) {
      others.add(getMilitia(1, 1, !startarmy));
      others.add(getMilitia(2, 1, !startarmy));
    } else if (rank == 2) {
      others.add(getMilitia(1, 1, !startarmy));
      others.add(getMilitia(2, 1, !startarmy));
    }

    boolean ud_demon = false;
    boolean magicbeing = false;

    for (Unit u : others) for (Command c : u.commands) {
      if (
        c.command.equals("#undead") ||
        c.command.equals("#demon") ||
        c.command.equals("#almostundead")
      ) ud_demon = true;
      else if (c.command.equals("#magicbeing")) magicbeing = true;
    }

    List<String> badprefixes = new ArrayList<>();
    badprefixes.add("no");
    if (startarmy) badprefixes.add("poor");

    // First pass at trying to find a suitable commander
    List<Unit> validComs = new ArrayList<>();
    for (Unit u : commanders) {
      boolean udvalid = false;
      boolean magicvalid = false;
      for (Command c : u.commands) {
        if (ud_demon && c.command.contains("undeadleader")) {
          String leader = c.command.substring(
            1,
            c.command.indexOf("undeadleader")
          );
          if (!badprefixes.contains(leader)) {
            udvalid = true;
          }
        }
        if (magicbeing && c.command.contains("magicleader")) {
          String leader = c.command.substring(
            1,
            c.command.indexOf("magicleader")
          );
          if (!badprefixes.contains(leader)) {
            magicvalid = true;
          }
        }
      }

      if (!ud_demon || udvalid) if (!magicbeing || magicvalid) validComs.add(u);
    }

    boolean failsafe = false;
    if (validComs.size() == 0 || (validComs.size() == 1 && rank == 2)) {
      failsafe = true;
      validComs = commanders;
      if (commanders.size() == 1 && rank == 2) rank = 1;
    }

    Unit com = validComs.get(rank - 1);
    if (failsafe) {
      if (magicbeing) com.commands.add(Command.args("#magiccommand", "40"));
      if (ud_demon) com.commands.add(Command.args("#undcommand", "40"));
    }

    return com;
  }

  public int getStartArmyAmount(Unit u) {
    // Handle filter etc stuff
    double filtermulti = 1;
    Tags tags = Generic.getAllNationTags(nation);
    for (Double multi : tags.getAllDoubles(
      "startarmy_amountmulti"
    )) filtermulti *= multi;

    double amount = militiaAmount(u) * filtermulti;

    return (int) Math.round(amount);
  }
}

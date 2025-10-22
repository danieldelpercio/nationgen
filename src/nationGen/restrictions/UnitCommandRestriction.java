package nationGen.restrictions;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import nationGen.misc.Command;
import nationGen.misc.TestResult;
import nationGen.nation.Nation;
import nationGen.units.Unit;

public class UnitCommandRestriction extends TextBoxListRestriction {

  private List<String> commandRestrictions = new ArrayList<>();

  public UnitCommandRestriction() {
    super(
      "<html>Nation needs to have at least one unit with a command on the right box. If you leave arguments empty, arguments on unit do not matter. " +
      "If you specify arguments, they need to match exactly.</html>",
      "Unit command"
    );
    this.hascombobox = true;
    this.comboboxoptions = new String[] {
      "All",
      "Troops",
      "Commanders",
      "Sacred troops",
      "Cap-only",
      "Normal-rec",
    };

    this.comboselection = "All";
    this.comboboxlabel = "Units to match:";
    this.textFieldLabel = "Command to add:";
    this.textfieldDefaultText = "#flying";
  }

  @Override
  public NationRestriction getRestriction() {
    UnitCommandRestriction res = new UnitCommandRestriction();
    for (
      int i = 0;
      i < chosen.getModel().getSize();
      i++
    ) res.commandRestrictions.add(chosen.getModel().getElementAt(i));

    res.comboselection = this.comboselection;
    return res;
  }

  @Override
  public TestResult doesThisPass(Nation n) {
    if (commandRestrictions.size() == 0) {
      System.out.println("Unit command nation restriction has no races set!");
      return TestResult.pass();
    }

    List<Unit> unitsToCheck = this.gatherUnitsToCheck(n);
    Optional<Unit> unitWithPossibleCommand = unitsToCheck.stream().filter(this::checkUnit).findFirst();

    if (unitWithPossibleCommand.isPresent()) {
      return TestResult.pass();
    }

    return TestResult.fail("Failed " + this.toString() + ": missing at least one of [" + commandRestrictions.toString() + "]");
  }

  private List<Unit> gatherUnitsToCheck(Nation nation) {
    List<Unit> unitsToCheck = new ArrayList<>();
    Boolean checkAll = this.comboselection == "All";

    if (this.comboselection.equals("Cap-only")) {
      unitsToCheck.addAll(nation.selectUnits().filter(Unit::isCapOnly).toList());
    }

    else if (this.comboselection.equals("Normal-rec")) {
      unitsToCheck.addAll(nation.selectUnits().filter(Predicate.not(Unit::isCapOnly)).toList());
    }
    
    else {
      if (this.comboselection == "Troops" || checkAll) {
        unitsToCheck.addAll(nation.selectTroops().toList());
      }

      if (this.comboselection == "Commanders" || checkAll) {
        unitsToCheck.addAll(nation.selectCommanders().toList());
      }

      if (this.comboselection == "Sacred troops" || checkAll) {
        unitsToCheck.addAll(nation.selectTroops("sacred").toList());
      }
    }

    return unitsToCheck;
  }

  private boolean checkUnit(Unit u) {
    for (String str : commandRestrictions) {
      Command oc = Command.parse(str);
      for (Command c : u.getCommands()) {
        if (
          c.command.equals(oc.command) && (oc.args.isEmpty() || c.equals(oc))
        ) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public RestrictionType getType() {
    return RestrictionType.UnitCommand;
  }
}

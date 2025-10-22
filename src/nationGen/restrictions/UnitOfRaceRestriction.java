package nationGen.restrictions;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import nationGen.NationGenAssets;
import nationGen.entities.Race;
import nationGen.misc.Command;
import nationGen.misc.TestResult;
import nationGen.nation.Nation;
import nationGen.units.Unit;

public class UnitOfRaceRestriction
  extends TwoListRestrictionWithComboBox<Race, String> {

  public List<String> possibleRaceNames = new ArrayList<String>();

  private NationGenAssets assets;

  public UnitOfRaceRestriction(NationGenAssets assets) {
    super(
      "Nation needs to have at least one unit of a race on the right box",
      "Unit of race"
    );
    this.assets = assets;
    this.comboboxlabel = "Units to match:";
    this.comboselection = "All";

    this.assets.races
      .getAllValues()
      .stream()
      .sorted(Comparator.comparing(Race::getName))
      .forEach(r -> rmodel.addElement(r));

    this.comboboxoptions = new String[] {
      "All",
      "Troops",
      "Commanders",
      "Sacred troops",
    };
  }

  @Override
  public NationRestriction getRestriction() {
    UnitOfRaceRestriction res = new UnitOfRaceRestriction(assets);
    for (
      int i = 0;
      i < chosen.getModel().getSize();
      i++
    ) res.possibleRaceNames.add(chosen.getModel().getElementAt(i).name);

    res.comboselection = this.comboselection;
    return res;
  }

  @Override
  public TestResult doesThisPass(Nation n) {
    if (possibleRaceNames.size() == 0) {
      System.out.println("Units of race nation restriction has no races set!");
      return TestResult.pass();
    }

    List<Unit> unitsToCheck = this.gatherUnitsToCheck(n);
    Optional<Unit> unitWithPossibleRace = unitsToCheck.stream().filter(this::checkUnit).findFirst();

    if (unitWithPossibleRace.isPresent()) {
      return TestResult.pass();
    }

    return TestResult.fail("Failed " + this.toString() + ": no unit with any possible races [" + possibleRaceNames.toString() + "]");
  }
  
  private List<Unit> gatherUnitsToCheck(Nation nation) {
    List<Unit> unitsToCheck = new ArrayList<>();
    Boolean checkAll = this.comboselection == "All";

    if (this.comboselection == "Troops" || checkAll) {
      unitsToCheck.addAll(nation.selectTroops().toList());
    }

    if (this.comboselection == "Commanders" || checkAll) {
      unitsToCheck.addAll(nation.selectCommanders().toList());
    }

    if (this.comboselection == "Sacred troops" || checkAll) {
      unitsToCheck.addAll(nation.selectTroops("sacred").toList());
    }

    return unitsToCheck;
  }

  private boolean checkUnit(Unit u) {
    return possibleRaceNames.contains(u.race.name);
  }

  @Override
  public RestrictionType getType() {
    return RestrictionType.UnitRace;
  }
}

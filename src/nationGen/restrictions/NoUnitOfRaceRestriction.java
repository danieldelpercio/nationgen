package nationGen.restrictions;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import nationGen.NationGenAssets;
import nationGen.entities.Race;
import nationGen.misc.TestResult;
import nationGen.nation.Nation;
import nationGen.units.Unit;

public class NoUnitOfRaceRestriction
  extends TwoListRestrictionWithComboBox<Race, String> {

  public List<String> blacklistedRaceNames = new ArrayList<>();

  private NationGenAssets assets;

  public NoUnitOfRaceRestriction(NationGenAssets assets) {
    super(
      "Nation needs to not have any units of a race on the right box",
      "No unit of race"
    );

    this.assets = assets;
    this.comboboxlabel = "Units to match:";
    this.comboselection = "All";
    
    assets.races.getAllValues().stream()
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
    NoUnitOfRaceRestriction res = new NoUnitOfRaceRestriction(assets);
    for (
      int i = 0;
      i < chosen.getModel().getSize();
      i++
    ) res.blacklistedRaceNames.add(chosen.getModel().getElementAt(i).name);

    res.comboselection = this.comboselection;
    return res;
  }

  @Override
  public TestResult doesThisPass(Nation n) {
    if (blacklistedRaceNames.size() == 0) {
      System.out.println(
        "No units of race nation restriction has no races set!"
      );
      return TestResult.pass();
    }

    List<Unit> unitsToCheck = this.gatherUnitsToCheck(n);
    Optional<Unit> unitOfBlacklistedRace = unitsToCheck.stream().filter(u -> {
      return blacklistedRaceNames.contains(u.race.name);
    }).findFirst();

    if (unitOfBlacklistedRace.isPresent() == false) {
      return TestResult.pass();
    }

    return TestResult.fail("Failed " + this.toString() + ": nation cannot have units of any race from " + blacklistedRaceNames.toString() + " is blacklisted");
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
      unitsToCheck.addAll(nation.selectTroops().toList());
    }

    return unitsToCheck;
  }

  @Override
  public RestrictionType getType() {
    return RestrictionType.NoUnitOfRace;
  }
}

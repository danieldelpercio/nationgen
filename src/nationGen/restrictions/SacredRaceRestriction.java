package nationGen.restrictions;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import nationGen.NationGenAssets;
import nationGen.entities.Race;
import nationGen.misc.TestResult;
import nationGen.nation.Nation;

public class SacredRaceRestriction extends TwoListRestriction<Race> {

  public List<String> possibleRaceNames = new ArrayList<>();

  private NationGenAssets assets;

  public SacredRaceRestriction(NationGenAssets assets) {
    super(
      "Nation needs to have at least one sacred unit of a race on the right box",
      "Sacred: Race"
    );
    this.assets = assets;

    this.assets.races
      .getAllValues()
      .stream()
      .sorted(Comparator.comparing(Race::getName))
      .forEach(r -> rmodel.addElement(r));
  }

  @Override
  public NationRestriction getRestriction() {
    SacredRaceRestriction res = new SacredRaceRestriction(assets);
    for (int i = 0; i < chosen.getModel().getSize(); i++) {
      res.possibleRaceNames.add(chosen.getModel().getElementAt(i).name);
    }

    return res;
  }

  @Override
  public TestResult doesThisPass(Nation n) {
    if (possibleRaceNames.size() == 0) {
      System.out.println("Sacred race nation restriction has no races set!");
      return TestResult.pass();
    }

    Boolean hasSacredsOfPossibleRace = n.selectTroops("sacred")
      .anyMatch(u -> possibleRaceNames.contains(u.race.name));

    if (hasSacredsOfPossibleRace) {
      return TestResult.pass();
    }

    return TestResult.fail("Failed " + this.toString() + ": no sacred of any of race [" + possibleRaceNames.toString() + "]");
  }

  @Override
  public RestrictionType getType() {
    return RestrictionType.SacredRace;
  }
}

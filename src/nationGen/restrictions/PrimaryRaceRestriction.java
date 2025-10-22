package nationGen.restrictions;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import nationGen.NationGenAssets;
import nationGen.entities.Race;
import nationGen.misc.TestResult;
import nationGen.nation.Nation;

public class PrimaryRaceRestriction extends TwoListRestriction<Race> {

  public List<String> possibleRaceNames = new ArrayList<>();
  private NationGenAssets assets;

  public PrimaryRaceRestriction(NationGenAssets assets) {
    super(
      "Nation needs to have one of the races in the right box as primary race",
      "Primary race"
    );

    this.assets = assets;

    assets.races.getAllValues().stream()
      .sorted(Comparator.comparing(Race::getName))
      .forEach(r -> {
        if (r.isSecondary() == false) {
          rmodel.addElement(r);
        };
      });
  }

  @Override
  public NationRestriction getRestriction() {
    PrimaryRaceRestriction res = new PrimaryRaceRestriction(assets);
    for (int i = 0; i < chosen.getModel().getSize(); i++) {
      res.possibleRaceNames.add(chosen.getModel().getElementAt(i).name);
    }
    return res;
  }

  @Override
  public TestResult doesThisPass(Nation n) {
    if (possibleRaceNames.isEmpty()) {
      System.out.println("Primary Race nation restriction has no races set!");
      return TestResult.pass();
    }

    String primaryRaceName = n.races.get(0).getName();
    Boolean hasRequiredRace = possibleRaceNames.contains(primaryRaceName);

    if (hasRequiredRace) {
      return TestResult.pass();
    }

    return TestResult.fail("Failed " + this.toString() + ": primary race is none of [" + possibleRaceNames.toString() + "]");
  }

  @Override
  public RestrictionType getType() {
    return RestrictionType.PrimaryRace;
  }
}

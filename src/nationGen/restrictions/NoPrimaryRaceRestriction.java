package nationGen.restrictions;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import nationGen.NationGenAssets;
import nationGen.entities.Race;
import nationGen.misc.TestResult;
import nationGen.nation.Nation;

public class NoPrimaryRaceRestriction extends TwoListRestriction<Race> {

  public List<String> blacklistedRaceNames = new ArrayList<String>();

  private NationGenAssets assets;

  public NoPrimaryRaceRestriction(NationGenAssets assets) {
    super(
      "Nation needs to not have one of the races in the right box as primary race",
      "No primary race"
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
    NoPrimaryRaceRestriction res = new NoPrimaryRaceRestriction(assets);
    for (
      int i = 0;
      i < chosen.getModel().getSize();
      i++
    ) res.blacklistedRaceNames.add(chosen.getModel().getElementAt(i).name);
    return res;
  }

  @Override
  public TestResult doesThisPass(Nation n) {
    if (blacklistedRaceNames.size() == 0) {
      System.out.println(
        "No Primary Race nation restriction has no races set!"
      );
      return TestResult.pass();
    }

    String primaryRaceName = n.races.get(0).getName();
    Boolean hasBlacklistedRace = blacklistedRaceNames.contains(primaryRaceName);

    if (hasBlacklistedRace == false) {
      return TestResult.pass();
    }

    return TestResult.fail("Failed " + this.toString() + ": primary race cannot be any of [" + blacklistedRaceNames.toString() + "]");
  }

  @Override
  public RestrictionType getType() {
    return RestrictionType.NoPrimaryRace;
  }

  public String print() {
    return "";
  }
}

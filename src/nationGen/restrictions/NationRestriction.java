package nationGen.restrictions;

import java.awt.LayoutManager;
import javax.swing.JPanel;

import nationGen.misc.TestResult;
import nationGen.nation.Nation;

public interface NationRestriction {
  /*
   * Add your restriction types here.
   */
  public enum RestrictionType {
    MageWithAccess,
    MagicAccess,
    MagicDiversity,
    NationTheme,
    NoPrimaryRace,
    NoUnitOfRace,
    PrimaryRace,
    RecAnywhereSacreds,
    SacredRace,
    UnitCommand,
    UnitFilter,
    UnitRace,
  }

  public void getGUI(JPanel panel);

  public NationRestriction getRestriction();

  public LayoutManager getLayout();

  public TestResult doesThisPass(Nation n);

  public String toString();

  public RestrictionType getType();
}

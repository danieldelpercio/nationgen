package nationGen.restrictions;

import com.elmokki.Generic;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import nationGen.magic.MagicPath;
import nationGen.magic.MagicPathInts;
import nationGen.magic.MagicPathLevel;
import nationGen.misc.TestResult;
import nationGen.nation.Nation;
import nationGen.units.Unit;

public class MageWithAccessRestriction
  extends TwoListRestrictionWithComboBox<String, String> {

  private List<String> neededPaths = new ArrayList<>();

  public MageWithAccessRestriction() {
    super(
      "<html>Nation needs to have 1 in 4 (100% random for 4 paths or better) access to at least one of the paths listed in the right box on a single mage</html>",
      "Magic: Mage with access"
    );
    this.extraTextField = true;
    this.textfieldDefaultText = "1";
    this.textFieldLabel = "Minimum magic level for added paths";

    this.comboboxlabel = "25% probability randoms allowed";
    this.comboboxoptions = new String[] { "True", "False" };

    MagicPath.NON_HOLY
      .stream()
      .sorted(Comparator.comparing(MagicPath::name))
      .forEach(mPath -> rmodel.addElement(mPath.name));
  }

  @Override
  public NationRestriction getRestriction() {
    MageWithAccessRestriction res = new MageWithAccessRestriction();

    for (int i = 0; i < chosen.getModel().getSize(); i++) res.neededPaths.add(
      chosen.getModel().getElementAt(i)
    );

    res.comboselection = this.comboselection;

    return res;
  }

  @Override
  public void actionPerformed(ActionEvent arg0) {
    // Add button
    if (arg0.getSource().equals(addButton) && all.getSelectedIndex() > -1) {
      if (rmodel.getElementAt(all.getSelectedIndex()) != null) {
        if (!rmodel2.contains(rmodel.getElementAt(all.getSelectedIndex()))) {
          if (textfield == null) rmodel2.addElement(
            rmodel.getElementAt(all.getSelectedIndex())
          );
          else if (Generic.isNumeric(textfield.getText())) rmodel2.addElement(
            rmodel.getElementAt(all.getSelectedIndex()) +
            " " +
            textfield.getText()
          );
        }
      }
    }
    // remove button
    if (
      arg0.getSource().equals(removeButton) && chosen.getSelectedIndex() > -1
    ) {
      rmodel2.remove(chosen.getSelectedIndex());
    }
  }

  @Override
  public TestResult doesThisPass(Nation nation) {
    if (neededPaths.size() == 0) {
      System.out.println(
        "Magic: Mage with Access nation restriction has no paths set!"
      );
      return TestResult.pass();
    }

    List<MagicPathLevel> requiredPaths = gatherRequiredPaths();
    Boolean countRandoms = comboselection == null || comboselection.equals("True");
    Optional<Unit> mageWithRequiredPaths = nation.selectCommanders("mage")
      .filter(u -> this.checkMage(u, requiredPaths, countRandoms))
      .findFirst();

    if (mageWithRequiredPaths.isPresent()) {
      return TestResult.pass();
    }

    String formattedRequiredPaths = requiredPaths.stream()
      .map(path -> path.toString())
      .reduce("", (partialString, pathString) -> {
        if (partialString.isBlank()) {
          return pathString;
        }

        return partialString + ", " + pathString;
      });

    return TestResult.fail("Failed " + this.toString() + ": no mage in nation with required paths [" + formattedRequiredPaths + "]");
  }

  private List<MagicPathLevel> gatherRequiredPaths() {
    return neededPaths.stream()
      .map(Generic::parseArgs)
      .map(args ->
        new MagicPathLevel(
          MagicPath.fromName(args.get(0)),
          Integer.parseInt(args.get(1))
        )
      )
      .collect(Collectors.toList());
  }

  private Boolean checkMage(Unit unit, List<MagicPathLevel> requiredPaths, Boolean countRandoms) {
    MagicPathInts magicPicks = unit.getMagicPicks(countRandoms);
    return requiredPaths.stream().allMatch(p -> magicPicks.get(p.path) >= p.level);
  }

  @Override
  public RestrictionType getType() {
    return RestrictionType.MageWithAccess;
  }
}

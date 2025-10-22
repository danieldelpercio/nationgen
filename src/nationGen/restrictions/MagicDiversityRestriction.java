package nationGen.restrictions;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import nationGen.magic.MageGenerator;
import nationGen.magic.MagicPath;
import nationGen.magic.MagicPathInts;
import nationGen.misc.TestResult;
import nationGen.nation.Nation;
import nationGen.units.Unit;

public class MagicDiversityRestriction
  extends DoubleTextBoxListRestriction<RestrictionMagicEntry> {

  public List<RestrictionMagicEntry> possibleRaceNames = new ArrayList<
    RestrictionMagicEntry
  >();

  public MagicDiversityRestriction() {
    super(
      "<html>Nation needs to have x magic paths at at least level y.</html>",
      "Magic: Diversity"
    );
    this.comboboxlabel = "25% probability randoms allowed";
    this.comboboxoptions = new String[] { "True", "False" };
    this.comboselection = this.comboboxoptions[0];

    this.hascombobox = true;
    this.textFieldLabel = "Path amount:";
    this.textfieldDefaultText = "1";
    this.textFieldLabel2 = "Path level:";
    this.textfieldDefaultText2 = "1";
  }

  @Override
  public NationRestriction getRestriction() {
    MagicDiversityRestriction res = new MagicDiversityRestriction();
    for (
      int i = 0;
      i < chosen.getModel().getSize();
      i++
    ) res.possibleRaceNames.add(chosen.getModel().getElementAt(i));

    res.comboselection = this.comboselection;
    return res;
  }

  @Override
  public TestResult doesThisPass(Nation n) {
    if (possibleRaceNames.size() == 0) {
      System.out.println(
        "Magic diversity nation restriction has no races set!"
      );
      return TestResult.pass();
    }

    List<Unit> mages = n.listCommanders("mage");
    MagicPathInts nonRandomPaths = MageGenerator.getAllPicks(mages, false);
    MagicPathInts randomPaths = MageGenerator.getAllPicks(mages, true);

    for (RestrictionMagicEntry res : possibleRaceNames) {
      MagicPathInts paths = (res.randoms) ? randomPaths : nonRandomPaths;
      int diversity = 0;

      for (MagicPath path : MagicPath.values()) {
        if (paths.get(path) >= res.level) {
          diversity++;
        }
      }

      if (diversity >= res.paths) {
        return TestResult.pass();
      }
    }

    return TestResult.fail("Failed " + this.toString() + ": ");
  }

  @Override
  public void actionPerformed(ActionEvent arg0) {
    // Add button
    if (arg0.getSource().equals(addButton)) {
      if (
        textfield.getText().length() > 0 &&
        textfield2.getText().length() > 0 &&
        !rmodel.contains(textfield.getText() + " - " + textfield2.getText())
      ) {
        boolean randoms = comboselection.equals("True");

        rmodel.addElement(
          new RestrictionMagicEntry(
            Integer.parseInt(textfield.getText()),
            Integer.parseInt(textfield2.getText()),
            randoms
          )
        );
      }
    }
    // remove button
    if (
      arg0.getSource().equals(removeButton) && chosen.getSelectedIndex() > -1
    ) {
      rmodel.remove(chosen.getSelectedIndex());
    }
  }

  @Override
  public RestrictionType getType() {
    return RestrictionType.MagicDiversity;
  }
}

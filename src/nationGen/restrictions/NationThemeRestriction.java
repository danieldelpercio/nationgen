package nationGen.restrictions;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import nationGen.NationGenAssets;
import nationGen.entities.Theme;
import nationGen.misc.TestResult;
import nationGen.nation.Nation;

public class NationThemeRestriction extends TwoListRestriction<String> {

  public List<String> possibleThemes = new ArrayList<String>();

  private NationGenAssets assets;

  public NationThemeRestriction(NationGenAssets assets) {
    super(
      "Nation or primary race needs to have a theme named like one of the themes in the right box",
      "Nation or primary race theme"
    );

    this.assets = assets;

    this.assets.themes
      .keySet()
      .stream()
      .sorted()
      .forEach(themeCategory -> {
        this.assets.themes
          .get(themeCategory)
          .stream()
          .sorted(Comparator.comparing(Theme::getName))
          .forEach(t -> {
            rmodel.addElement(themeCategory + " - " + t);
        });
      });

    this.extraTextField = true;
    this.textFieldLabel = "Search:";
  }

  @Override
  public NationRestriction getRestriction() {
    NationThemeRestriction res = new NationThemeRestriction(assets);
    for (
      int i = 0;
      i < chosen.getModel().getSize();
      i++
    ) res.possibleThemes.add(chosen.getModel().getElementAt(i));
    return res;
  }

  @Override
  public TestResult doesThisPass(Nation n) {
    if (possibleThemes.size() == 0) {
      System.out.println(
        "Nation or primary race theme nation restriction has no races set!"
      );
      return TestResult.pass();
    }

    List<Theme> allNationThemes = Stream.concat(
        n.nationthemes.stream(),
        n.races.get(0).themefilters.stream()
      )
      .collect(Collectors.toList());

    for (Theme t : allNationThemes) {
      for (String str : possibleThemes) {
        String name = str.split(" - ")[1];
        if (t.name.equals(name)) {
          return TestResult.pass();
        }
      }
    }

    return TestResult.fail("Failed " + this.toString() + ": missing at least a theme from [" + possibleThemes.toString() + "]");
  }

  @Override
  public RestrictionType getType() {
    return RestrictionType.NationTheme;
  }

  @Override
  protected void textFieldUpdate() {
    rmodel.clear();
    for (String str : assets.themes.keySet()) {
      for (Theme t : assets.themes.get(str)) {
        String add = str + " - " + t.toString();
        if (
          add.contains(textfield.getText()) || textfield.getText().length() == 0
        ) {
          rmodel.addElement(add);
        }
      }
    }
  }
}

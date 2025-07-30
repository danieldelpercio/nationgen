package nationGen.nation.startarmy;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.elmokki.Generic;

import nationGen.Settings;
import nationGen.Settings.SettingsType;
import nationGen.misc.Command;
import nationGen.misc.Tags;
import nationGen.nation.Nation;
import nationGen.nation.pd.Militia;
import nationGen.nation.pd.PDProvinceType;
import nationGen.nation.pd.PDUnitType;
import nationGen.units.LeadershipType;
import nationGen.units.Unit;

/**
 * Militia are all of the national units that will be used for starting armies,
 * PD, and castle defenders. They are directly related to the Dominions' modding
 * commands #defunit1 and #defunit2.
 * 
 * There are four #defunit1 commands: from #defunit1 to #defunit1d.
 * These are the national unit PD in an unforted province that has 20+ PD,
 * or in a forted province from 1 PD.
 *
 * There are two #defunit2 commands: from #defunit2 to #defunit2b.
 * These are the national unit PD in a forted province that has 20+ PD.
 * 
 * Thus, in Dominions 6, a forted province with 20+ PD can have a total of
 * 6 (heh) different (or duplicated) types of national units defending it.
 */
public class StartArmy {
  private Nation nation;
  private Militia militia;

  private List<Unit> armyUnits = new ArrayList<>();
  private Unit armyCommander;
  private Unit scout;

  private Double militiaMultiplierSetting;

  private Double resMultiplierSetting;
  private Double resUpperThreshold;
  private Double resLowerThreshold;
  private Double resMultiplierThresholdSetting;

  private Double resUpperThresholdChangeSetting;
  private Double resLowerThresholdChangeSetting;

  private Double goldUpperThresholdSetting;
  private Double goldLowerThresholdSetting;

  private Double goldUpperThresholdChangeSetting;
  private Double goldLowerThresholdChangeSetting;

  // The amount of "points" of starting army that will determine how many
  // of each unit the nation gets at the beginning.
  // This budget includes both gold and resource cost. The more expensive
  // the units are, the less we'll get.
  private final int ARMY_BUDGET = 400;

  // The #startunittypeX command to set a nation's starting army unit types
  // accepts 2 different unit types; #startunittype1 and #startunittype2
  private final int NUMBER_OF_START_ARMY_UNIT_TYPES = 2;

  public StartArmy(Nation nation, Militia militia, boolean isMontagAllowed, Settings settings) {
      this.nation = nation;
      this.militia = militia;

      this.militiaMultiplierSetting = settings.get(SettingsType.militiaMultiplier);

      this.resMultiplierSetting = settings.get(SettingsType.resMultiplier);
      this.resUpperThreshold = settings.get(SettingsType.resUpperThreshold);
      this.resLowerThreshold = settings.get(SettingsType.resLowerThreshold);
      this.resMultiplierThresholdSetting = settings.get(SettingsType.resMultiplierThreshold);

      this.resUpperThresholdChangeSetting = settings.get(SettingsType.resUpperThresholdChange);
      this.resLowerThresholdChangeSetting = settings.get(SettingsType.resLowerThresholdChange);

      this.goldUpperThresholdSetting = settings.get(SettingsType.goldUpperThreshold);
      this.goldLowerThresholdSetting = settings.get(SettingsType.goldLowerThreshold);

      this.goldUpperThresholdChangeSetting = settings.get(SettingsType.goldUpperThresholdChange);
      this.goldLowerThresholdChangeSetting = settings.get(SettingsType.goldLowerThresholdChange);

      this.armyUnits.addAll(this.generateStartArmyUnits());
  }

  public Unit getArmyCommander() {
    if (this.armyCommander == null) {
      this.armyCommander = this.generateArmyCommander();
    }

    return this.armyCommander;
  }

  public Unit getScout() {
    if (this.scout == null) {
      this.scout = this.generateScout();
    }

    return this.scout;
  }

  /**
   * Gets the number for the #startunitnbrs modding command. This determines
   * how many of the given unit are given to the player as the starting army.
   * @param unit
   * @return
   */
  public int getAmountOfArmyUnit(Unit unit) {
    // Handle filter etc stuff
    double totalMultiplier = 1;
    Tags tags = Generic.getAllNationTags(nation);
    List<Double> pdMultipliers = tags.getAllDoubles("startarmy_amountmulti");

    for (Double multiplier : pdMultipliers) {
      totalMultiplier *= multiplier;
    }

    double amount = calculateArmyUnitAmount(unit) * totalMultiplier;
    return (int) Math.round(amount);
  }

  public List<String> writeModLines() {
    List<String> lines = new ArrayList<>();
    Unit startUnit1;
    Unit startUnit2;

    if (this.armyUnits.size() < NUMBER_OF_START_ARMY_UNIT_TYPES) {
      throw new IllegalStateException(
        "Expected " +
        NUMBER_OF_START_ARMY_UNIT_TYPES +
        " types of units for the nation's starting army; instead only got" +
        this.armyUnits.size()
      );
    }

    else {
      startUnit1 = this.armyUnits.get(0);
      startUnit2 = this.armyUnits.get(1);
    }

    lines.add("#startcom " + this.getArmyCommander().getRootId());
    lines.add("");

    lines.add("#startunittype1 " + startUnit1.getRootId());
    lines.add("#startunitnbrs1 " + this.getAmountOfArmyUnit(startUnit1));
    lines.add("");

    lines.add("#startunittype2 " + startUnit2.getRootId());
    lines.add("#startunitnbrs2 " + this.getAmountOfArmyUnit(startUnit2));
    lines.add("");

    lines.add("#startscout " + this.getScout().getRootId());
    lines.add("");

    return lines;
  }

  private List<Unit> generateStartArmyUnits() {
    // Reverse the selected PD types for less chances of start army being same as start PD
    List<PDUnitType> pdUnitTypes = this.militia.getUsedBasicPdTypes().reversed();
    List<PDUnitType> fortPdUnitTypes = this.militia.getUsedFortPdTypes().reversed();
    List<Unit> selectedUnits = new ArrayList<>();

    Unit firstArmyUnit;
    Unit secondArmyUnit;

    // Try to make both units different from each other, even if sometimes
    // unforted and forted PD units end up having the same unit selected
    for (PDUnitType basicType : pdUnitTypes) {
      firstArmyUnit = this.militia.getMilitiaUnit(basicType);

      for (PDUnitType fortType : fortPdUnitTypes) {
        secondArmyUnit = this.militia.getMilitiaUnit(fortType);

        if (firstArmyUnit.hashCode() != secondArmyUnit.hashCode()) {
          selectedUnits.add(firstArmyUnit);
          selectedUnits.add(secondArmyUnit);
          break;
        }
      }
    }

    return selectedUnits;
  }

  private Unit generateArmyCommander() {
    List<Unit> commanders = this.nation.listCommanders("commander");

    List<Unit> pdUnits = militia.getUsedBasicPdTypes()
      .stream()
      .map(type -> militia.getMilitiaUnit(type))
      .toList();

    return this.selectArmyCommander(commanders, pdUnits);
  }

  private Unit generateScout() {
    List<Unit> scouts = this.nation.listCommanders("scout");

    if (scouts.size() == 0) {
      throw new IllegalStateException(
        "Expected nation to have at least 1 scout; instead found none"
      );
    }

    return scouts.get(0);
  }

  private Unit selectArmyCommander(List<Unit> commanderCandidates, List<Unit> pdUnits) {
    boolean needsUndeadLeadership = pdUnits.stream()
      .filter(u -> u.isUndead() || u.isAlmostUndead() || u.isDemon())
      .findAny()
      .isPresent();

    boolean needsMagicLeadership = pdUnits.stream()
      .filter(u -> u.isMagicBeing())
      .findAny()
      .isPresent();

    List<Unit> validCommanders = commanderCandidates.stream()
      .filter(c -> {
        return (needsUndeadLeadership == false || c.hasLeadership(LeadershipType.UNDEAD)) &&
            (needsMagicLeadership == false || c.hasLeadership(LeadershipType.MAGIC_BEING));
      })
      .toList();

    Unit selectedCommander;

    if (validCommanders.size() == 0) {
      selectedCommander = commanderCandidates.getFirst();

      if (needsUndeadLeadership) {
        selectedCommander.commands.add(Command.args("#undcommand", "40"));
      }

      if (needsMagicLeadership) {
        selectedCommander.commands.add(Command.args("#magiccommand", "40"));
      }
    }
    
    else {
      selectedCommander = validCommanders.getFirst();
    }

    return selectedCommander;
  }

  /**
   * Calculate the number of units of this type for the start army, as per the
   * #startunitnbrs modding command. This is affected by some NationGen settings,
   * which set thresholds and flexible margins for the effective gold and
   * resources cost of a unit as part of the starting army.
   * 
   * This adjusted cost is then used to divide the army budget, which
   * is simply an arbitrary constant. The higher the budget, the bigger the
   * resulting amounts will be. A final result of 20 here means "20 units of
   * this type in the starting army".
   * @param unit
   * @return
   */
  private double calculateArmyUnitAmount(Unit unit) {
    int res = getAdjustedUnitResCost(unit, false);
    int gold = getAdjustedUnitGoldCost(unit);
    double score = gold + res;
    double multiplier = this.ARMY_BUDGET / score;
    multiplier = multiplier * this.militiaMultiplierSetting;
    return multiplier;
  }

  /**
   * Adjust a unit's effective resource cost based on the current NationGen settings.
   * Note this is simply to determine how costly the unit is in terms of its presence
   * in the starting army. It does not perform any changes to the unit's actual recruitment
   * res cost.
   * @param unit
   * @param useSize
   * @return adjusted resource cost for start army amount calculations
   */
  private int getAdjustedUnitResCost(Unit unit, boolean useSize) {
    int res = unit.getResCost(useSize);

    double adjustedUpperResCost = res + this.resUpperThresholdChangeSetting;
    double adjustedLowerResCost = res + this.resLowerThresholdChangeSetting;

    boolean isResCostAboveThreshold = res > this.resUpperThreshold;
    boolean isResCostBelowThreshold = res < this.resLowerThreshold;

    if (isResCostAboveThreshold && this.resUpperThresholdChangeSetting < 0) {
      res = (int) Math.max((adjustedUpperResCost), (0 + this.resUpperThreshold));
    }

    else if (isResCostAboveThreshold && this.resUpperThresholdChangeSetting > 0) {
      res = (int) adjustedUpperResCost;
    }

    if (isResCostBelowThreshold && this.resLowerThresholdChangeSetting > 0) {
      res = (int) Math.min(adjustedLowerResCost, (0 + this.resLowerThreshold));
    }

    else if (isResCostBelowThreshold && this.resLowerThresholdChangeSetting < 0) {
      res = (int) adjustedLowerResCost;
    }
    
    if (res > this.resMultiplierThresholdSetting) {
      res = (int) (
        this.resMultiplierThresholdSetting +
        (res - this.resMultiplierThresholdSetting) *
        this.resMultiplierSetting
      );
    }

    return res;
  }
  
  /**
   * Adjust a unit's effective gold cost based on the current NationGen settings.
   * Note this is simply to determine how costly the unit is in terms of its presence
   * in the starting army. It does not perform any changes to the unit's actual recruitment
   * gold cost.
   * @param unit
   * @param useSize
   * @return adjusted gold cost for start army amount calculations
   */
  private int getAdjustedUnitGoldCost(Unit unit) {
    int gold = unit.getGoldCost();

    double adjustedUpperGoldCost = gold + this.goldUpperThresholdChangeSetting;
    double adjustedLowerGoldCost = gold + this.goldLowerThresholdChangeSetting;

    boolean isGoldCostAboveThreshold = gold > this.goldUpperThresholdSetting;
    boolean isGoldCostBelowThreshold = gold < this.goldLowerThresholdSetting;

    if (isGoldCostAboveThreshold && this.goldUpperThresholdChangeSetting < 0) {
      gold = (int) Math.max((adjustedUpperGoldCost), (0 + this.goldUpperThresholdChangeSetting));
    }

    else if (isGoldCostAboveThreshold && this.goldUpperThresholdChangeSetting > 0) {
      gold = (int) adjustedUpperGoldCost;
    }

    if (isGoldCostBelowThreshold && this.goldLowerThresholdChangeSetting > 0) {
      gold = (int) Math.min(adjustedLowerGoldCost, (0 + this.goldLowerThresholdChangeSetting));
    }

    else if (isGoldCostBelowThreshold && this.goldLowerThresholdChangeSetting < 0) {
      gold = (int) adjustedLowerGoldCost;
    }

    return gold;
  }
}

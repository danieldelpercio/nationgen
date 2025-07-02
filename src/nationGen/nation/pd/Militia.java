package nationGen.nation.pd;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.elmokki.Generic;

import nationGen.Settings;
import nationGen.Settings.SettingsType;
import nationGen.misc.Tags;
import nationGen.nation.Nation;
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
public class Militia {
  private Map<PDUnitType, Unit> militia = new TreeMap<PDUnitType, Unit>();
  private Nation nation;

  private int pdRanks = 2;
  private int pdFortRanks = 1;
  private Double extraPdMultiplier;

  private Double militiaMultiplierSetting;

  private Double resMultiplierSetting;
  private Double resUpperThreshold;
  private Double resLowerThreshold;
  private Double resMultiplierTresholdSetting;

  private Double resUpperThresholdChangeSetting;
  private Double resLowerThresholdChangeSetting;

  private Double goldUpperThresholdSetting;
  private Double goldLowerThresholdSetting;

  private Double goldUpperThresholdChangeSetting;
  private Double goldLowerThresholdChangeSetting;

  // The amount of "points" of militia that will determine how many units
  // the nation gets for every PD point.
  // This budget includes both gold and resource cost. The more expensive
  // the units that were selected for PD are, the less we'll get.
  private final int MILITIA_BUDGET = 400;

  public Militia(Nation nation, boolean isMontagAllowed, Settings settings) {
      this.nation = nation;

      this.militiaMultiplierSetting = settings.get(SettingsType.militiaMultiplier);

      this.resMultiplierSetting = settings.get(SettingsType.resMultiplier);
      this.resUpperThreshold = settings.get(SettingsType.resUpperThreshold);
      this.resLowerThreshold = settings.get(SettingsType.resLowerThreshold);
      this.resMultiplierTresholdSetting = settings.get(SettingsType.resMultiplierTreshold);

      this.resUpperThresholdChangeSetting = settings.get(SettingsType.resUpperThresholdChange);
      this.resLowerThresholdChangeSetting = settings.get(SettingsType.resLowerThresholdChange);

      this.goldUpperThresholdSetting = settings.get(SettingsType.goldUpperThreshold);
      this.goldLowerThresholdSetting = settings.get(SettingsType.goldLowerThreshold);

      this.goldUpperThresholdChangeSetting = settings.get(SettingsType.goldUpperThresholdChange);
      this.goldLowerThresholdChangeSetting = settings.get(SettingsType.goldLowerThresholdChange);

      this.extraPdMultiplier = this.nation.races
        .get(0).tags
        .getDouble("extrapdmulti")
        .orElse(1D);

      if (this.nation.random.nextDouble() < 0.1 * this.extraPdMultiplier) {
        this.increasePdRanks();
        this.increaseFortPdRanks();

        if (this.nation.random.nextDouble() < 0.02 * this.extraPdMultiplier) {
          this.increasePdRanks();
        }
      }

      this.militia.putAll(
        this.generateUnfortedProvinceMilitia(this.pdRanks, isMontagAllowed)
      );

      this.militia.putAll(
        this.generateFortedProvinceMilitia(this.pdFortRanks, isMontagAllowed)
      );
  }

  public void increasePdRanks() {
    this.pdRanks = Math.min(PDUnitNumber.values().length, ++this.pdRanks);
  }

  public void increaseFortPdRanks() {
    this.pdRanks = Math.min(PDUnitNumber.values().length / 2, ++this.pdRanks);
  }

  /**
   * Gets the Unit that was assigned to a given #defunitX command
   * @param type
   * @return
   */
  public Unit getMilitiaUnit(PDUnitType type) {
    // Get the unit from the specified tier and rank
    return this.militia.get(type);
  }

  /**
   * Gets the multiplier for the #defmult modding command. This determines
   * how many of the given unit are created in PD per 10 points of defense.
   * For example, a multiplier of 20 means 2 units per point of PD.
   * @param unit
   * @return
   */
  public int getAmountOfMilitiaUnit(Unit unit) {
    // Handle filter etc stuff
    double totalMultiplier = 1;
    Tags tags = Generic.getAllNationTags(nation);
    List<Double> pdMultipliers = tags.getAllDoubles("pd_amountmulti");

    for (Double multiplier : pdMultipliers) {
      totalMultiplier *= multiplier;
    }

    double amount = calculateMilitiaUnitMultiplier(unit) * totalMultiplier;
    return (int) Math.round(amount);
  }

  /**
   * Select one of the nation's units to become the wall defense.
   * @param isMontagAllowed
   * @return
   */
  public Unit getWallUnit(boolean isMontagAllowed) {
    List<Unit> candidateUnits = nation.combineTroopsToList("ranged");
    List<Unit> goodRangedInfantry = nation.combineTroopsToList("infantry")
      .stream()
      .filter(u -> u.hasSecondaryRangeOfAtLeast(15))
      .toList();

    // Try first with ranged units and infantry with good ranged bonusweapons
    candidateUnits.addAll(goodRangedInfantry);
    removeUnsuitable(isMontagAllowed, candidateUnits);

    // No options: Any infantry with any ranged
    if (candidateUnits.size() == 0) {
      candidateUnits = nation.combineTroopsToList("infantry")
      .stream()
      .filter(u -> u.isSecondaryRanged())
      .toList();

      removeUnsuitable(isMontagAllowed, candidateUnits);
    }

    // No options: Any infantry
    if (candidateUnits.size() == 0) {
      candidateUnits = nation.combineTroopsToList("infantry");
      removeUnsuitable(isMontagAllowed, candidateUnits);
    }

    // Try to get unit
    Unit unit = selectWallUnit(candidateUnits);

    // Failsafe: Just get something
    if (unit == null) {
      candidateUnits = nation.combineTroopsToList("infantry");
      candidateUnits.addAll(nation.combineTroopsToList("mounted"));
      candidateUnits.addAll(nation.combineTroopsToList("ranged"));

      if (isMontagAllowed) {
        candidateUnits.addAll(nation.combineTroopsToList("montagtroops"));
      }

      unit = selectWallUnit(candidateUnits);
    }

    return unit;
  }

  /**
   * Select one of the nation's units to become the gate guards.
   * @param isMontagAllowed
   * @return
   */
  public Unit getGateUnit(boolean isMontagAllowed) {
    List<Unit> candidateUnits = nation.combineTroopsToList("infantry");
    removeUnsuitable(isMontagAllowed, candidateUnits);

    Unit unit = selectGateUnit(candidateUnits);

    // Failsafe: Just get something
    if (unit == null) {
      candidateUnits = nation.combineTroopsToList("infantry");
      candidateUnits.addAll(nation.combineTroopsToList("mounted"));
      candidateUnits.addAll(nation.combineTroopsToList("ranged"));

      if (isMontagAllowed) {
        candidateUnits.addAll(nation.combineTroopsToList("montagtroops"));
      }

      unit = selectGateUnit(candidateUnits);
    }

    return unit;
  }

  /**
   * Get the mult amount for the #wallmult and #guardmult mod commands.
   * @param unit
   * @return
   */
  public int getAmountOfCastleDefenders(Unit unit) {
    // Handle filter etc stuff
    double totalMultiplier = 1;
    Tags tags = Generic.getAllNationTags(nation);
    List<Double> pdMultipliers = tags.getAllDoubles("pd_amountmulti");

    for (Double multiplier : pdMultipliers) {
      totalMultiplier *= multiplier;
    }

    double amount = calculateAmountOfCastleDefenders(unit) * totalMultiplier;
    return (int) Math.round(amount);
  }

  /**
   * Specifically selects up to four of the nation's Units to be the PD defense for the
   * #defunit1 to #defunit1d commands. The minimum are two "ranks", #defunit1 and #defunit1b.
   * Each rank is a new type of unit that increases with each point of PD. More ranks = more
   * units per point of PD, since all types get increased.
   * @param numberOfRanks the number of "ranks", or unit types, that will be part of this PD
   * @param isMontagAllowed
   * @return
   */
  private Map<PDUnitType, Unit> generateUnfortedProvinceMilitia(int numberOfRanks, boolean isMontagAllowed) {
    List<PDUnitType> pdUnitTypes = List.of(
      PDUnitType.FORTED_OR_WITH_20PD_FIRST,
      PDUnitType.FORTED_OR_WITH_20PD_SECOND,
      PDUnitType.FORTED_OR_WITH_20PD_THIRD,
      PDUnitType.FORTED_OR_WITH_20PD_FOURTH
    )
    // Only use as many ranks as the nation rolled into
    .subList(0, numberOfRanks);

    return generateMilitia(pdUnitTypes, 1, 1, isMontagAllowed);
  }

  /**
   * Specifically selects two of the nation's Units to be the PD defense for the
   * #defunit2 and #defunit2a commands. These have higher gold/res multipliers to
   * skew it to select the higher cost units from the nation. The minimum is one "ranks",
   * #defunit2, up to two ranks.
   * Each rank is a new type of unit that increases with each point of PD. More ranks = more
   * units per point of PD, since all types get increased.
   * @param numberOfRanks the number of "ranks", or unit types, that will be part of this PD
   * @param isMontagAllowed
   * @return
   */
  private Map<PDUnitType, Unit> generateFortedProvinceMilitia(int numberOfRanks, boolean isMontagAllowed) {
    List<PDUnitType> pdUnitTypes = List.of(
      PDUnitType.FORTED_WITH_20PD_FIRST,
      PDUnitType.FORTED_WITH_20PD_SECOND
    )
    // Only use as many ranks as the nation rolled into
    .subList(0, numberOfRanks);

    return generateMilitia(pdUnitTypes, 1, 1.2, isMontagAllowed);
  }

  /**
   * Generate a list of "types" of militia, based on Dominions' PD modding commands #defunit1 and #defunit2
   * The former goes from #defunit1 to #defunit1d, for a total of 4 units that comprise the PD of an unforted
   * province (when at 20+ PD value).
   * 
   * The latter goes from #defunit2 to #defunit2b, for a total of 2 units that comprise the PD of a forted
   * province that has 20+ PD value.
   * @param types
   * @param goldMultiplier Multiplier serves to skew the unit picks towards higher (or lower) value units
   * @param resMultiplier Multiplier serves to skew the unit picks towards higher (or lower) value units
   * @param isMontagAllowed Are montags allowed as part of this militia?
   * @return A TreeMap (ordered Map) where each key corresponds to one of the PD commands
   */
  private Map<PDUnitType, Unit> generateMilitia(List<PDUnitType> types, double goldMultiplier, double resMultiplier, boolean isMontagAllowed) {
    TreeMap<PDUnitType, Unit> militia = new TreeMap<PDUnitType, Unit>();
    int rangedUnitsSelected = 0;

    // Create a list of all possible militia units based on the nation's poses
    List<Unit> possibleUnits = this.nation.combineTroopsToList("infantry");
    possibleUnits.addAll(nation.combineTroopsToList("mounted"));
    possibleUnits.addAll(nation.combineTroopsToList("ranged"));

    if (!isMontagAllowed) {
      possibleUnits.addAll(
        nation.combineTroopsToList("montagtroops")
      );
    }

    // Remove unit poses that may be unfit to be militia
    removeUnsuitable(isMontagAllowed, possibleUnits);

    // Determine unit target gold and cost based on all available units
    double exclusionGoldCost = this.getExclusionGoldCost(possibleUnits) * goldMultiplier;
    double exclusionResCost = this.getExclusionResCost(possibleUnits) * resMultiplier;

    // Do the magic
    for (int i = 0; i < types.size(); i++) {
      PDUnitType pdUnitType = types.get(i);

      // Expected several different unit types to fill all PD types, but didn't get enough
      if (possibleUnits.size() == 0) {
        System.out.println(
          "Not enough suitable unit poses to fill all unforted PD ranks (race: " +
          this.nation.races.get(0).name +
          ", nation: " +
          this.nation.nationid +
          ". This is fine (first rank unit will be reused instead), but consider creating more varied poses for this race."
        );
        break;
      }

      // Don't add more than half the units as ranged
      boolean canBeRanged = rangedUnitsSelected < Math.floor(types.size() / 2);

      // Select unit that best fits the target gold/resources
      Unit best = selectBestMilitiaUnit(possibleUnits, exclusionGoldCost, exclusionResCost, canBeRanged);

      // Remove from pool and add to our militia
      possibleUnits.remove(best);
      militia.put(pdUnitType, best);

      if (best.isRanged()) {
        rangedUnitsSelected++;
      }
    }

    // Reuse the first found rank unit to fill up the remaining PD ranks
    for (int j = militia.size(); j < types.size(); j++) {
      PDUnitType unfilledUnitType = types.get(j);
      Unit firstMilitiaUnit = militia.firstEntry().getValue();
      militia.put(unfilledUnitType, firstMilitiaUnit);
    }

    return militia;
  }

  /**
   * Out of a given list of units, selects the best suited to be the wall defense.
   * @param units
   * @return
   */
  private Unit selectWallUnit(List<Unit> units) {
    double totalRes = 0;
    double totalGold = 0;
    for (Unit u : units) {
      totalRes += u.getResCost(true);
      totalGold += u.getGoldCost();
    }
    double exclusionGoldCost = (totalGold / units.size());
    double exclusionResCost = (totalRes / units.size());

    Unit best = units.get(0);
    double bestscore = scoreForMilitia(best, exclusionResCost, exclusionGoldCost);
    for (Unit u : units) {
      double score =
        scoreForMilitia(u, exclusionResCost, exclusionGoldCost) *
        ((u.getCommandValue("#castledef", 0) + 1));
      if (bestscore >= score) {
        bestscore = score;
        best = u;
      }
    }

    return best;
  }
  
  /**
   * Out of a given list of units, selects the best suited to be the gate guards.
   * @param units
   * @return
   */
  private Unit selectGateUnit(List<Unit> units) {
    double totalRes = 0;
    double totalGold = 0;

    for (Unit u : units) {
      totalRes += u.getResCost(true);
      totalGold += u.getGoldCost();
    }

    double exclusionGoldCost = (totalGold / units.size());
    double exclusionResCost = (totalRes / units.size());

    Unit best = units.get(0);
    double bestscore = scoreForMilitia(best, exclusionResCost, exclusionGoldCost);

    for (Unit u : units) {
      double score =
        scoreForMilitia(u, exclusionResCost, exclusionGoldCost) *
        ((u.getCommandValue("#castledef", 0) + 1));

      if (bestscore >= score) {
        bestscore = score;
        best = u;
      }
    }

    return best;
  }

  /**
   * Check for montags and poses that are tagged with #cannot_be_pd
   * and remove those from the list of potential PD units.
   * @param isMontagAllowed
   * @param units
   */
  private void removeUnsuitable(
    boolean isMontagAllowed,
    List<Unit> units
  ) {
    List<Unit> unsuitable = new ArrayList<>();

    for (Unit u : units) {
      boolean cannotBePd = Generic.getAllUnitTags(u).containsName("cannot_be_pd");

      if (cannotBePd) {
        unsuitable.add(u);
      }

      else if (u.isMontag() == true && isMontagAllowed == false) {
        unsuitable.add(u);
      }
    }

    // Remove unsuitable units assuming that there would still be some left to pick
    if (unsuitable.size() < units.size()) {
      units.removeAll(unsuitable);
    }

    // If ALL units were deemed unsuitable, we will make an exception and not remove them,
    // but this should get resolved by making sure each race has a good number of unit poses
    else {
      System.out.println(
        "Nation " +
        this.nation.nationid +
        " (primary race: "+
        this.nation.races.get(0).name +
        ") does not have any suitable units for militia. As a fallback, all of its units will be used as candidates, but consider adding more unit poses to the race");
    }
  }

  /**
   * Get the exclusion Unit gold cost that we want for the nation.
   * This is the average gold cost of all suitable units, plus
   * factoring any relevant national tags, such as #pd_targetrcostmulti.
   * Units close to this cost will not be considered for province defense.
   * @param possibleUnits
   * @return
   */
  private double getExclusionGoldCost(List<Unit> possibleUnits) {
    double totalGold = 0;
    double exclusionGoldCost = 0;
    Tags tags = Generic.getAllNationTags(nation);
    List<Double> goldCostMultipliers = tags.getAllDoubles("pd_targetgcostmulti");

    for (Unit u : possibleUnits) {
      totalGold += u.getGoldCost();
    }

    // Set a fixed target if #pd_targetgcost exists on the nation,
    // or just average all suitable unit gold costs.
    exclusionGoldCost = tags
      .getDouble("pd_targetgcost")
      .orElse(totalGold / possibleUnits.size());

    // Factor in all cost multipliers
    for (Double multiplier : goldCostMultipliers) {
      exclusionGoldCost *= multiplier;
    }

    return exclusionGoldCost;
  }

  /**
   * Get the exclusion Unit resource cost that we want for the nation.
   * This is the average resource cost of all suitable units, plus
   * factoring any relevant national tags, such as #pd_targetrcostmulti.
   * Units close to this cost will not be considered for province defense.
   * @param possibleUnits
   * @return
   */
  private double getExclusionResCost(List<Unit> possibleUnits) {
    double totalRes = 0;
    double exclusionResCost = 0;
    Tags tags = Generic.getAllNationTags(nation);
    List<Double> resCostMultipliers = tags.getAllDoubles("pd_targetrcostmulti");

    for (Unit u : possibleUnits) {
      totalRes += u.getResCost(true);
    }

    // Set a fixed target if #pd_targetgcost exists on the nation,
    // or just average all suitable unit gold costs.
    exclusionResCost = tags
      .getDouble("pd_targetrcost")
      .orElse(totalRes / possibleUnits.size());

    // Factor in all cost multipliers
    for (Double multiplier : resCostMultipliers) {
      exclusionResCost *= multiplier;
    }

    return exclusionResCost;
  }

  /**
   * Select the most suited unit to be province defense from a given list.
   * The more its gold/rest cost deviates from the nation's average, the
   * more suitable it is considered to be.
   * @param possibleUnits
   * @param exclusionGoldCost 
   * @param exclusionResCost
   * @param canBeRanged
   * @return
   */
  private Unit selectBestMilitiaUnit(
    List<Unit> possibleUnits,
    double exclusionGoldCost,
    double exclusionResCost,
    boolean canBeRanged
  ) {
    List<Unit> filteredUnits = possibleUnits
      .stream()
      .filter(u -> canBeRanged || u.isRanged() == false)
      .toList();

    Unit best = filteredUnits.get(0);
    double bestscore = scoreForMilitia(best, exclusionGoldCost, exclusionResCost);

    // Iterate through all units, updating the best one if a better one is found
    for (Unit unit : filteredUnits) {
      double score = scoreForMilitia(unit, exclusionGoldCost, exclusionResCost);

      if (bestscore >= score) {
        bestscore = score;
        best = unit;
      }
    }

    return best;
  }

  /**
   * Assign a score to a Unit to measure how suitable it is to be province defense.
   * This is based on how much their gold/res cost deviates from the nation's average.
   * @param unit
   * @param exclusionGold the lowest gold score point. Further away = better score
   * @param exclusionRes the lowest resource score point. Further away = better score
   * @return
   */
  private double scoreForMilitia(Unit unit, double exclusionGold, double exclusionRes) {
    double goldScore = getUnitGoldScoreForMilitia(unit, exclusionGold);
    double resScore = getUnitResScoreForMilitia(unit, exclusionRes);

    // Gold weighs less than resource cost for this purpose
    return 0.75 * goldScore + resScore;
  }

  /**
   * Give a unit a score based on how far its gold cost is from a given number,
   * ranking it as a good PD candidate. If the exclusionGold is 20, a unit that
   * costs 20 gold will get the lowest possible score, whereas units with much
   * lower or higher gold cost will be deemed more suitable to be PD.
   * 
   * Why is it done this way? Who knows. Below is an interactive graph:
   * https://www.desmos.com/calculator/0pzeul1tie
   * @param unit
   * @param exclusionGold an "avoidance point"
   * @return how suitable this unit's gold cost makes it to be part of PD.
   */
  private double getUnitGoldScoreForMilitia(Unit unit, double exclusionGold) {
    double unitGoldCost = unit.getGoldCost();
    double exponent = 0.7;
    double goldScore = 0;

    if (unitGoldCost < exclusionGold) {
      goldScore = Math.pow(1.2, exclusionGold - unitGoldCost);
    }

    // Higher gold cost's score grows faster than lower gold cost
    if (unitGoldCost >= exclusionGold) {
      goldScore = Math.pow(Math.pow(1.35, unitGoldCost - exclusionGold), exponent);
    }

    return goldScore;
  }

  /**
   * Give a unit a score based on how far its res cost is from a given number,
   * ranking it as a good PD candidate. If the exclusionRes is 20, a unit that
   * costs 20 resources will get the lowest possible score, whereas units with
   * much lower or higher resource cost will be deemed more suitable to be PD.
   * 
   * Why is it done this way? Who knows. Below is an interactive graph:
   * https://www.desmos.com/calculator/wesu16wuj2
   * @param unit
   * @param exclusionRes an "avoidance point"
   * @return how suitable this unit's resource cost makes it to be part of PD.
   */
  private double getUnitResScoreForMilitia(Unit unit, double exclusionRes) {
    double unitResCost = unit.getResCost(false);
    double exponent = 0.7;
    double resScore = 0;

    if (unitResCost < exclusionRes) {
      resScore = Math.pow(1.2, exclusionRes - unitResCost);
    }

    // Higher resource cost's score grows faster than lower resource cost
    if (unitResCost > exclusionRes) {
      resScore = Math.pow(Math.pow(1.4, unitResCost - exclusionRes), exponent);
    }

    return resScore;
  }
  
  /**
   * Calculate the number of units of this type per 10 PD points, as per the
   * #defmult modding command. This is affected by some NationGen settings,
   * which set thresholds and flexible margins for the effective gold and
   * resources cost of a unit as PD.
   * 
   * This adjusted cost is then used to divide the militia budget, which
   * is simply an arbitrary constant. The higher the budget, the bigger the
   * resulting amounts will be. A final result of 20 here means "20 units per
   * 10 PD points", i.e. "2 units of this type per PD point in the province".
   * @param unit
   * @return
   */
  private double calculateMilitiaUnitMultiplier(Unit unit) {
    int res = getAdjustedUnitResCost(unit, false);
    int gold = getAdjustedUnitGoldCost(unit);
    double score = gold + res;
    double multiplier = this.MILITIA_BUDGET / score;
    multiplier = multiplier * this.militiaMultiplierSetting;
    return multiplier;
  }

  /**
   * Calculate the number of units of this type that will be set as gate or wall defenders.
   * This uses the same calculation as the PD units. However, since their scale is different
   * (see #defmult and #wallmult/#guardmult), the final amount is halved here.
   * @param unit
   * @return
   */
  private double calculateAmountOfCastleDefenders(Unit unit) {
    double amount = calculateMilitiaUnitMultiplier(unit);
    return Math.floor(amount * 0.5);
  }

  /**
   * Adjust a unit's effective resource cost based on the current NationGen settings.
   * Note this is simply to determine how costly the unit is in terms of its presence
   * in PD. It does not perform any changes to the unit's actual recruitment res cost.
   * @param unit
   * @param useSize
   * @return adjusted resource cost for PD calculations
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
    
    if (res > this.resMultiplierTresholdSetting) {
      res = (int) (
        this.resMultiplierTresholdSetting +
        (res - this.resMultiplierTresholdSetting) *
        this.resMultiplierSetting
      );
    }

    return res;
  }
  
  /**
   * Adjust a unit's effective gold cost based on the current NationGen settings.
   * Note this is simply to determine how costly the unit is in terms of its presence
   * in PD. It does not perform any changes to the unit's actual recruitment gold cost.
   * @param unit
   * @return adjusted gold cost for PD calculations
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

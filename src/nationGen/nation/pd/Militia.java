package nationGen.nation.pd;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.stream.Collectors;

import com.elmokki.Generic;

import nationGen.Settings;
import nationGen.Settings.SettingsType;
import nationGen.misc.Command;
import nationGen.misc.Tags;
import nationGen.nation.Nation;
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
public class Militia {
  private Nation nation;
  private Random random;

  private Map<PDUnitType, Unit> pdUnits = new TreeMap<PDUnitType, Unit>();

  private Unit pdCommander;
  private Unit fortPdCommander;

  private Unit wallUnit;
  private Unit wallCommander;

  private Unit gateUnit;
  private Unit gateCommander;

  private int pdRanks = 2;
  private int fortPdRanks = 1;

  private Double extraPdMultiplier;
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

  // The amount of "points" of militia that will determine how many units
  // the nation gets for every PD point.
  // This budget includes both gold and resource cost. The more expensive
  // the units that were selected for PD are, the less we'll get.
  private final int MILITIA_BUDGET = 400;

  public Militia(Nation nation, boolean isMontagAllowed, Settings settings) {
      this.nation = nation;
      this.random = new Random(this.nation.random.nextInt());

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

      this.extraPdMultiplier = this.nation.races
        .get(0).tags
        .getDouble("extrapdmulti")
        .orElse(1D);

      if (this.random.nextDouble() < 0.1 * this.extraPdMultiplier) {
        this.increasePdRanks();
        this.increaseFortPdRanks();

        if (this.random.nextDouble() < 0.02 * this.extraPdMultiplier) {
          this.increasePdRanks();
        }
      }

      this.pdUnits.putAll(
        this.generateUnfortedProvinceMilitia(this.pdRanks, isMontagAllowed)
      );

      this.pdUnits.putAll(
        this.generateFortedProvinceMilitia(this.fortPdRanks, isMontagAllowed)
      );
  }

  public List<PDUnitType> getUsedBasicPdTypes() {
    return List.of(PDUnitType.values())
      .stream()
      .filter(u -> u.pdProvinceType == PDProvinceType.FORTED_OR_WITH_20PD)
      .collect(Collectors.toList())
      .subList(0, this.pdRanks);
  }

  public List<PDUnitType> getUsedFortPdTypes() {
    return List.of(PDUnitType.values())
      .stream()
      .filter(u -> u.pdProvinceType == PDProvinceType.FORTED_WITH_20PD)
      .collect(Collectors.toList())
      .subList(0, this.fortPdRanks);
  }
 
  public void increasePdRanks() {
    this.pdRanks = Math.min(PDUnitNumber.values().length, ++this.pdRanks);
  }

  public void increaseFortPdRanks() {
    this.fortPdRanks = Math.min(PDUnitNumber.values().length / 2, ++this.fortPdRanks);
  }

  /**
   * Gets the Unit that was assigned to a given #defunitX command
   * @param type
   * @return
   */
  public Unit getMilitiaUnit(PDUnitType type) {
    // Get the unit from the specified tier and rank
    return this.pdUnits.get(type);
  }

  /**
   * Gets the multiplier for the #defmult modding command. This determines
   * how many of the given unit are created in PD per 10 points of defense.
   * For example, a multiplier of 20 means 2 units per point of PD.
   * @param unit
   * @return
   */
  public int getAmountOfMilitiaUnit(PDUnitType pdUnitType) {
    Unit unit = this.getMilitiaUnit(pdUnitType);
    return getAmountOfMilitiaUnit(unit);
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

  public Unit getPdCommander() {
    if (this.pdCommander == null) {
      this.pdCommander = this.generatePdCommander();
    }

    return this.pdCommander;
  }

  public Unit getFortPdCommander() {
    if (this.fortPdCommander == null) {
      this.fortPdCommander = this.generateFortPdCommander();
    }

    return this.fortPdCommander;
  }

  public Unit getWallUnit() {
    if (this.wallUnit == null) {
      this.wallUnit = this.generateWallUnit(true);
    }

    return this.wallUnit;
  }

  public Unit getWallCommander() {
    if (this.wallCommander == null) {
      this.wallCommander = this.generatePdCommander();
    }

    return this.wallCommander;
  }

  public Unit getGateUnit() {
    if (this.gateUnit == null) {
      this.gateUnit = this.generateGateUnit(true);
    }

    return this.gateUnit;
  }

  public Unit getGateCommander() {
    if (this.gateCommander == null) {
      this.gateCommander = this.generatePdCommander();
    }

    return this.gateCommander;
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

  public List<String> writeModLines() {
    List<String> lines = new ArrayList<>();

    lines.add("#defcom1 " + this.getPdCommander().getRootId());

    this.getUsedBasicPdTypes()
    .forEach(pdUnitType -> {
      lines.add(
        pdUnitType.getModCommand() + " " + this.getMilitiaUnit(pdUnitType).getRootId()
      );
      
      lines.add(
        pdUnitType.getMultiplierModCommand() + " " + this.getAmountOfMilitiaUnit(pdUnitType)
      );
    });

    lines.add("");

    lines.add("#defcom2 " + this.getFortPdCommander().getRootId());

    this.getUsedFortPdTypes()
    .forEach(pdUnitType -> {
      lines.add(
        pdUnitType.getModCommand() + " " + this.getMilitiaUnit(pdUnitType)
      );
      
      lines.add(
        pdUnitType.getMultiplierModCommand() + " " + this.getAmountOfMilitiaUnit(pdUnitType)
      );
    });

    lines.add("");

    // Wall units
    lines.add("#wallcom " + this.getWallCommander().getRootId());
    lines.add("#wallunit " + this.getWallUnit().getRootId());
    lines.add("#wallmult " + this.getAmountOfCastleDefenders(this.getWallUnit()));

    // Gate units
    lines.add("#guardcom " + this.getGateCommander().getRootId());
    lines.add("#guardunit " + this.getGateUnit().getRootId());
    lines.add("#guardmult " + this.getAmountOfCastleDefenders(this.getGateUnit()));

    return lines;
  }

  public List<String> writeDescriptionLines() {
    List<String> lines = new ArrayList<>();

    Unit pdCommander = this.getPdCommander();
    Unit fortPdCommander = this.getFortPdCommander();

    List<PDUnitType> pdUnitTypes = this.getUsedBasicPdTypes();
    List<PDUnitType> fortPdUnitTypes = this.getUsedFortPdTypes();

    lines.add("Province Defence:");
    lines.add("------------------------------------------");
    lines.add("* Commander 1: " + pdCommander.name);
    lines.add("* Commander 2: " + fortPdCommander.name);
    lines.add("");

    pdUnitTypes.forEach(t -> {
      lines.add(
        "* Unforted PD Unit " + t.getModCommand() + ": " +
        this.getMilitiaUnit(t).name + " - " +
        this.getAmountOfMilitiaUnit(t) +
        " per 10 PD"
      );
    });

    fortPdUnitTypes.forEach(t -> {
      lines.add(
        "* Forted PD Unit " + t.getModCommand() + ": " +
        this.getMilitiaUnit(t).name + " - " +
        this.getAmountOfMilitiaUnit(t) +
        " per 10 PD"
      );
    });
    
    return lines;
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
    List<PDUnitType> pdUnitTypes = this.getUsedBasicPdTypes();
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
    List<PDUnitType> pdUnitTypes = this.getUsedFortPdTypes();
    return generateMilitia(pdUnitTypes, 1, 0.8, isMontagAllowed);
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

    // Exclusion gold and res cost are the costs with the least chances to get picked.
    // The less, or the more that units cost relative to the exclusion cost, the better
    // chances they have to get selected as one of the PD units
    double exclusionGoldCost = this.getExclusionGoldCost(possibleUnits) * goldMultiplier;
    double exclusionResCost = this.getExclusionResCost(possibleUnits) * resMultiplier;

    // Do the magic
    for (PDUnitType pdUnitType : types) {
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
    // (if there are any that still remain empty)
    for (int i = militia.size(); i < types.size(); i++) {
      PDUnitType unfilledUnitType = types.get(i);
      Unit firstMilitiaUnit = militia.firstEntry().getValue();
      militia.put(unfilledUnitType, firstMilitiaUnit);
    }

    return militia;
  }

  private Unit generatePdCommander() {
    List<Unit> commanders = this.nation.listCommanders("commander");
    List<Unit> priests = nation.listCommanders("priest");
    double randomChance = this.random.nextDouble();

    if (randomChance < 0.05) {
      commanders.add(0, priests.get(0));
    }

    List<Unit> pdUnits = this.getUsedBasicPdTypes()
      .stream()
      .map(type -> getMilitiaUnit(type))
      .collect(Collectors.toList());

    return this.selectPdCommander(commanders, pdUnits);
  }
  
  private Unit generateFortPdCommander() {
    List<Unit> commanders = this.nation.listCommanders("commander");
    List<Unit> priests = nation.listCommanders("priest");
    double randomChance = this.random.nextDouble();

    // Chance to get a level 2 priest
    if (randomChance < 0.15 && priests.size() > 1) {
      commanders.add(0, priests.get(1));
    }

    // Chance to get a level 1 priest
    else if (randomChance < 0.75) {
      commanders.add(0, priests.get(0));
    }

    List<Unit> pdUnits = this.getUsedFortPdTypes()
      .stream()
      .map(type -> getMilitiaUnit(type))
      .collect(Collectors.toList());

    return this.selectPdCommander(commanders, pdUnits);
  }

  private Unit selectPdCommander(List<Unit> commanderCandidates, List<Unit> pdUnits) {
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
      .collect(Collectors.toList());

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
   * Select one of the nation's units to become the wall defense.
   * @param isMontagAllowed
   * @return
   */
  private Unit generateWallUnit(boolean isMontagAllowed) {
    List<Unit> candidateUnits = nation.combineTroopsToList("ranged");
    List<Unit> goodRangedInfantry = nation.combineTroopsToList("infantry")
      .stream()
      .filter(u -> u.hasSecondaryRangeOfAtLeast(15))
      .collect(Collectors.toList());

    // Try first with ranged units and infantry with good ranged bonusweapons
    candidateUnits.addAll(goodRangedInfantry);
    removeUnsuitable(isMontagAllowed, candidateUnits);

    // No options: Any infantry with any ranged
    if (candidateUnits.size() == 0) {
      candidateUnits = nation.combineTroopsToList("infantry")
      .stream()
      .filter(u -> u.isSecondaryRanged())
      .collect(Collectors.toList());

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
   * Select one of the nation's units to become the gate guards.
   * @param isMontagAllowed
   * @return
   */
  private Unit generateGateUnit(boolean isMontagAllowed) {
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
      .collect(Collectors.toList());

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

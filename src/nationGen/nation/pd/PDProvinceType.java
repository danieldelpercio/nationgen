package nationGen.nation.pd;

public enum PDProvinceType {
  FORTED_OR_WITH_20PD("#defunit1", 4),
  FORTED_WITH_20PD("#defunit2", 2);

  private String baseModCommand;
  private int maxUnitTypes;

  PDProvinceType(String baseModCommand, int maxUnitTypes) {
    this.baseModCommand = baseModCommand;
    this.maxUnitTypes = maxUnitTypes;
  }

  public String getBaseModCommand() {
    return this.baseModCommand;
  }

  public int getMaxUnitTypes() {
    return this.maxUnitTypes;
  }
}

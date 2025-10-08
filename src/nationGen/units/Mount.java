package nationGen.units;

import nationGen.CustomItemsHandler;
import nationGen.NationGen;
import nationGen.entities.Filter;

public class Mount extends Filter {
  private String barding;

  public Mount(NationGen nationGen) {
    super(nationGen);
  }

  public Mount(Mount mount) {
    super(mount);
  }

  public Boolean hasBarding() {
    return super.hasCommand("#armor");
  }

  public Boolean isNamed() {
    return this.hasCommand("#name");
  }

  public String getBardingId() {
    return this.barding;
  }

  public void setBarding(String id) {
    if (CustomItemsHandler.isIdResolved(id)) {
      this.barding = id;
      return;
    }

    String resolvedId = nationGen.GetCustomItemsHandler().getCustomItemId(id);
    this.barding = resolvedId;
  }
}

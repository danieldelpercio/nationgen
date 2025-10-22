package nationGen.magic;

public class MagicPathLevel {

  public final MagicPath path;
  public final int level;

  public MagicPathLevel(MagicPath path, int level) {
    this.path = path;
    this.level = level;
  }

  @Override
  public String toString() {
    return this.path.toString() + " " + this.level;
  }
}

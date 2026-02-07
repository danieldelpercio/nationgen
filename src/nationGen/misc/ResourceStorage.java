package nationGen.misc;

import com.elmokki.Generic;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import nationGen.NationGen;
import nationGen.entities.Entity;

public class ResourceStorage<E extends Entity>
  extends LinkedHashMap<String, List<E>> {

  private static final long serialVersionUID = 1L;

  private Class<E> type;

  public ResourceStorage(Class<E> type) {
    this.type = type;
  }

  public List<E> getAllValues() {
    return this.values()
      .stream()
      .flatMap(List::stream)
      .collect(Collectors.toList());
  }

  public LinkedHashMap<E, Double> getHashMap(String name) {
    List<E> list = this.get(name);
    if (list == null) return null;

    LinkedHashMap<E, Double> set = new LinkedHashMap<E, Double>();
    for (E i : list) {
      set.put(i, i.basechance);
    }
    return set;
  }

  public void load(NationGen nationGen, String file) {
    List<String> lines = FileUtil.readLines(file);

    for (int lineNbr = 0; lineNbr < lines.size(); lineNbr++) {
      String strLine = lines.get(lineNbr);
      List<String> args = Generic.parseArgs(strLine);

      if (args.isEmpty()) {
        continue;
      }

      String commandString = args.get(0);

      // Ignore anything that's not #load in base resource files
      if (!commandString.equals("#load")) {
        continue;
      }

      if (args.size() < 3) {
        throw new IllegalArgumentException("Line " + lineNbr + " in file '" + file + "': #load command expects a list name and a file path to read!");
      }

      String resourceListName = args.get(1);
      String resourceFilePath = args.get(2);

      // TODO: refactor this with some blacklist
      if (resourceListName.equals("clear")) {
        throw new IllegalStateException(
          "File '" + file + "': Resource sets can't be named 'clear'!"
        );
      }

      this.put(args.get(1), Entity.readFile(nationGen, resourceFilePath, type));
    }
  }
}

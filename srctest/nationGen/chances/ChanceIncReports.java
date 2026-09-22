package nationGen.chances;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.junit.Before;
import org.junit.Test;

import nationGen.NationGen;
import nationGen.NationGenAssets;
import nationGen.entities.Filter;
import nationGen.entities.Pose;
import nationGen.entities.Race;
import nationGen.items.Item;
import nationGen.misc.ItemSet;
import nationGen.misc.ResourceStorage;

public class ChanceIncReports {
    private NationGen nationGen;
    private NationGenAssets assets;
    private ResourceStorage<Race> races;
    private ResourceStorage<Filter> filters;

    @Before
    public void setup() {
        nationGen = new NationGen();
        assets = nationGen.getAssets();

        races = assets.races;
        filters = assets.filters;
    }

    @Test
    public void globalIterate() {
        System.out.println("RACES:");
        races.forEach((raceListName, subRaceList) -> {
            System.out.println();
            System.out.println(raceListName);

            subRaceList.forEach((race) -> {
                List<String> themes = race.themes;
                List<Pose> poses = race.poses;
                System.out.println("  " + race.getName());

                System.out.println("    - Themes:");
                themes.forEach((themeName) -> {
                    System.out.println("      " + themeName);
                });

                System.out.println();
                System.out.println("    - Poses:");
                poses.forEach((pose) -> {
                    Set<String> slotNames = pose.getListOfSlots();

                    System.out.println();
                    System.out.println("      " + pose.getName());


                    slotNames.forEach((slotName) -> {
                        ItemSet items = pose.getItems(slotName);
                        
                        System.out.println();
                        System.out.println("      - " + slotName + " slot:");

                        items.forEach((item) -> {
                            System.out.println("        " + item.getName());
                        });
                    });
                });
            });
        });

        System.out.println();
        System.out.println("FILTERS:");
        filters.forEach((filterListName, filterLists) -> {
            System.out.println();
            System.out.println(filterListName);

            filterLists.forEach((filter) -> {
                System.out.println("  " + filter.getName());
            });
        });
    }

    @Test
    public void reportAllUnreachableSubcomponents() {
        List<Pose> allPoses = nationGen.getAssets().poses.getAllValues();
        Map<String, Map<String, List<String>>> unreachableByPose = new TreeMap<>();
        
        for (Pose pose : allPoses) {
            Set<String> slotNames = pose.getListOfSlots();
            Map<String, List<String>> unreachableInPose = new LinkedHashMap<>();
            
            // Check base chance
            if (pose.basechance <= 0) {
                unreachableInPose.computeIfAbsent("pose", k -> new ArrayList<>())
                    .add(pose.name + " (basechance=" + pose.basechance + ")");
            }

            for (String slotName : slotNames) {
                List<Item> itemsInSlot = pose.getItems(slotName);

                for (Item item : itemsInSlot) {
                    if (item.basechance <= 0) {
                        unreachableInPose.computeIfAbsent("items:" + itemsInSlot, k -> new ArrayList<>())
                            .add(item.name + " (basechance=" + item.basechance + ")");
                    }
                }
            }
            
            if (!unreachableInPose.isEmpty()) {
                unreachableByPose.put((pose.name == null) ? "UNNAMED" : pose.name, unreachableInPose);
            }
        }
        
        if (!unreachableByPose.isEmpty()) {
            System.out.println("\n========== UNREACHABLE SUBCOMPONENTS ==========");
            unreachableByPose.forEach((poseName, categories) -> {
                System.out.println("\n" + poseName + ":");
                categories.forEach((category, items) -> {
                    System.out.println("  " + category + ":");
                    items.forEach(item -> System.out.println("    - " + item));
                });
            });
            
            int totalUnreachable = unreachableByPose.values().stream()
                .flatMap(m -> m.values().stream())
                .mapToInt(List::size)
                .sum();
            System.out.println("\nTotal unreachable components: " + totalUnreachable);
        }
    }
}

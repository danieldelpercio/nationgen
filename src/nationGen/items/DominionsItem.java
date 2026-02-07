package nationGen.items;

import java.util.LinkedHashMap;

import nationGen.misc.Command;

public class DominionsItem {

    
    public static LinkedHashMap<String, String> toHashMap(DominionsItem item) {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();

        map.put(ItemProperty.ATTACK.toDBColumn(), "0");
        map.put(ItemProperty.AMMO.toDBColumn(), "0");
        map.put(ItemProperty.RANGE.toDBColumn(), "0");
        map.put(ItemProperty.DEFENCE.toDBColumn(), "0");
        map.put(ItemProperty.LENGTH.toDBColumn(), "0");
        map.put(ItemProperty.DAMAGE.toDBColumn(), "0");
        map.put(ItemProperty.IS_2H.toDBColumn(), "0");

        for (Command command : customItemCommands) {
            String firstValue = command.args.get(0).get();
            ItemProperty property = ItemProperty.fromCommand(command);

            if (property == null) {
                continue;
            }

            else if (property == ItemProperty.FLYSPRITE) {
                String secondValue = command.args.get(1).get();
                map.put(property.toDBColumn(), firstValue);
                map.put(ItemProperty.ANIM_LENGTH.toDBColumn(), secondValue);
            }

            else if (property.isBoolean()) {
                map.put(property.toDBColumn(), "1");
            }

            else {
                map.put(property.toDBColumn(), firstValue);
            }
        }
    }
}

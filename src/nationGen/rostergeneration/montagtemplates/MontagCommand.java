package nationGen.rostergeneration.montagtemplates;

import java.util.stream.Stream;

import nationGen.misc.Arg;
import nationGen.misc.Command;

public enum MontagCommand {
    ADD_MONSTER_TO_POOL("#addmonstertopool", 1),
    BATTLE_START_SUMMON_1("#batstartsum1"),
    BATTLE_START_SUMMON_2("#batstartsum2"),
    BATTLE_START_SUMMON_3("#batstartsum3"),
    BATTLE_START_SUMMON_4("#batstartsum4"),
    BATTLE_START_SUMMON_5("#batstartsum5"),
    BATTLE_START_SUMMON_1D3("#batstartsum1d3"),
    BATTLE_START_SUMMON_1D6("#batstartsum1d6"),
    BATTLE_START_SUMMON_2D6("#batstartsum2d6"),
    BATTLE_START_SUMMON_3D6("#batstartsum3d6"),
    BATTLE_START_SUMMON_4D6("#batstartsum4d6"),
    BATTLE_START_SUMMON_5D6("#batstartsum5d6"),
    BATTLE_START_SUMMON_6D6("#batstartsum6d6"),
    BATTLE_START_SUMMON_7D6("#batstartsum7d6"),
    BATTLE_START_SUMMON_8D6("#batstartsum8d6"),
    BATTLE_START_SUMMON_9D6("#batstartsum9d6"),
    BATTLE_SUMMON_1("#battlesum1"),
    BATTLE_SUMMON_2("#battlesum2"),
    BATTLE_SUMMON_3("#battlesum3"),
    BATTLE_SUMMON_4("#battlesum4"),
    BATTLE_SUMMON_5("#battlesum5"),
    BATTLE_SUMMON_1D2("#battlesum1d2"),
    BATTLE_SUMMON_1D3("#battlesum1d3"),
    BATTLE_SUMMON_WARM("#battlesumwarm"),
    DOM_SUMMON("#domsummon"),
    DOM_SUMMON_HALF("#domsummon2"),
    DOM_SUMMON_TWENTIETH("#domsummon20"),
    DOM_SUMMON_RARE("#raredomsummon"),
    //FIRST_SHAPE("#firstshape"),
    GUARD_SPIRIT("#guardspirit"),
    MAKE_MONSTERS_1("#makemonsters1"),
    MAKE_MONSTERS_2("#makemonsters2"),
    MAKE_MONSTERS_3("#makemonsters3"),
    MAKE_MONSTERS_4("#makemonsters4"),
    MAKE_MONSTERS_5("#makemonsters5"),
    MONSTER_CANNOT_CAST("#notmnr"),
    MONTAG("#montag"),
    NEEDS_MONSTER_TO_RECRUIT("#monpresentrec"),
    NEEDS_OWN_MONSTER_TO_RECRUIT("#ownsmonrec"),
    ONLY_MONSTER_CAN_CAST("#onlymnr"),
    PROPHET_SHAPE("#prophetshape"),
    RAISE_SHAPE("#raiseshape"),
    //SECOND_SHAPE("#secondshape"),
    SECOND_TMP_SHAPE("#secondtmpshape"),
    SUMMON_1("#summon1"),
    SUMMON_2("#summon2"),
    SUMMON_3("#summon3"),
    SUMMON_4("#summon4"),
    SUMMON_5("#summon5"),
    TEMPLE_TRAINER("#templetrainer"),
    XP_SHAPE_MONSTER("xpshapemon");

    private String modCommand;
    private int montagArgumentIndex = 0;

    MontagCommand(String modCommand) {
        this.modCommand = modCommand;
    }

    MontagCommand(String modCommand, int montagArgumentIndex) {
        this.modCommand = modCommand;
        this.montagArgumentIndex = montagArgumentIndex;
    }

    public String toModCommand() {
        return this.modCommand;
    }

    public int getIndexOfMontagArg() {
        return this.montagArgumentIndex;
    }

    public boolean isSameAs(Command command) {
        return this.toModCommand().equals(command.command);
    }

    public static String getMontagArgumentFromCommand(Command command) {
        MontagCommand montagCommand = MontagCommand.fromCommand(command);

        if (montagCommand == null) {
            throw new IllegalArgumentException(
                "Expected command to be a type of montag command; instead got " + command.toString()
            );
        }

        return command.args.getString(montagCommand.getIndexOfMontagArg());
    }

    public static MontagCommand fromCommand(Command command) {
        String commandString = command.command;
        return Stream.of(MontagCommand.values())
            .filter(montagCommand -> {
                return montagCommand.toModCommand().equals(commandString);
            })
            .findFirst()
            .orElse(null);
    }

    public static boolean isMontagCommandType(Command command) {
        if (command == null) {
            return false;
        }

        String commandString = command.command;
        return Stream.of(MontagCommand.values())
            .anyMatch(montagCommand -> {
                return montagCommand.toModCommand().equals(commandString);
            });
    }

    public static boolean hasMontagArgument(Command command) {
        MontagCommand montagCommand = MontagCommand.fromCommand(command);

        if (montagCommand == null) {
            return false;            
        }

        int index = montagCommand.getIndexOfMontagArg();
        Arg arg = command.args.get(index);

        if (arg.isNumeric() && arg.getInt() < 0) {
            return true;
        }

        if (!arg.isNumeric()) {
            return true;
        }

        return false;
    }
}

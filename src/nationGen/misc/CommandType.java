package nationGen.misc;

import java.util.List;
import java.util.stream.Stream;

public enum CommandType {
    BATTLE_START_SUMMON_1("#batstartsum1", new CommandSignature().defineNewArg(ArgType.NUMERIC, ArgType.MONTAG, ArgType.NATIONGEN_ID)),
    BATTLE_START_SUMMON_2("#batstartsum2", new CommandSignature().defineNewArg(ArgType.NUMERIC, ArgType.MONTAG, ArgType.NATIONGEN_ID)),
    BATTLE_START_SUMMON_3("#batstartsum3", new CommandSignature().defineNewArg(ArgType.NUMERIC, ArgType.MONTAG, ArgType.NATIONGEN_ID)),
    BATTLE_START_SUMMON_4("#batstartsum4", new CommandSignature().defineNewArg(ArgType.NUMERIC, ArgType.MONTAG, ArgType.NATIONGEN_ID)),
    BATTLE_START_SUMMON_5("#batstartsum5", new CommandSignature().defineNewArg(ArgType.NUMERIC, ArgType.MONTAG, ArgType.NATIONGEN_ID)),
    BATTLE_START_SUMMON_1D3("#batstartsum1d3", new CommandSignature().defineNewArg(ArgType.NUMERIC, ArgType.MONTAG, ArgType.NATIONGEN_ID)),
    BATTLE_START_SUMMON_1D6("#batstartsum1d6", new CommandSignature().defineNewArg(ArgType.NUMERIC, ArgType.MONTAG, ArgType.NATIONGEN_ID)),
    BATTLE_START_SUMMON_2D6("#batstartsum2d6", new CommandSignature().defineNewArg(ArgType.NUMERIC, ArgType.MONTAG, ArgType.NATIONGEN_ID)),
    BATTLE_START_SUMMON_3D6("#batstartsum3d6", new CommandSignature().defineNewArg(ArgType.NUMERIC, ArgType.MONTAG, ArgType.NATIONGEN_ID)),
    BATTLE_START_SUMMON_4D6("#batstartsum4d6", new CommandSignature().defineNewArg(ArgType.NUMERIC, ArgType.MONTAG, ArgType.NATIONGEN_ID)),
    BATTLE_START_SUMMON_5D6("#batstartsum5d6", new CommandSignature().defineNewArg(ArgType.NUMERIC, ArgType.MONTAG, ArgType.NATIONGEN_ID)),
    BATTLE_START_SUMMON_6D6("#batstartsum6d6", new CommandSignature().defineNewArg(ArgType.NUMERIC, ArgType.MONTAG, ArgType.NATIONGEN_ID)),
    BATTLE_START_SUMMON_7D6("#batstartsum7d6", new CommandSignature().defineNewArg(ArgType.NUMERIC, ArgType.MONTAG, ArgType.NATIONGEN_ID)),
    BATTLE_START_SUMMON_8D6("#batstartsum8d6", new CommandSignature().defineNewArg(ArgType.NUMERIC, ArgType.MONTAG, ArgType.NATIONGEN_ID)),
    BATTLE_START_SUMMON_9D6("#batstartsum9d6", new CommandSignature().defineNewArg(ArgType.NUMERIC, ArgType.MONTAG, ArgType.NATIONGEN_ID)),
    BATTLE_SUMMON_1("#battlesum1", new CommandSignature().defineNewArg(ArgType.NUMERIC, ArgType.MONTAG, ArgType.NATIONGEN_ID)),
    BATTLE_SUMMON_2("#battlesum2", new CommandSignature().defineNewArg(ArgType.NUMERIC, ArgType.MONTAG, ArgType.NATIONGEN_ID)),
    BATTLE_SUMMON_3("#battlesum3", new CommandSignature().defineNewArg(ArgType.NUMERIC, ArgType.MONTAG, ArgType.NATIONGEN_ID)),
    BATTLE_SUMMON_4("#battlesum4", new CommandSignature().defineNewArg(ArgType.NUMERIC, ArgType.MONTAG, ArgType.NATIONGEN_ID)),
    BATTLE_SUMMON_5("#battlesum5", new CommandSignature().defineNewArg(ArgType.NUMERIC, ArgType.MONTAG, ArgType.NATIONGEN_ID)),
    BATTLE_SUMMON_1D2("#battlesum1d2", new CommandSignature().defineNewArg(ArgType.NUMERIC, ArgType.MONTAG, ArgType.NATIONGEN_ID)),
    BATTLE_SUMMON_1D3("#battlesum1d3", new CommandSignature().defineNewArg(ArgType.NUMERIC, ArgType.MONTAG, ArgType.NATIONGEN_ID)),
    BATTLE_SUMMON_WARM("#battlesumwarm", new CommandSignature().defineNewArg(ArgType.NUMERIC, ArgType.MONTAG, ArgType.NATIONGEN_ID)),
    DOM_SUMMON("#domsummon", new CommandSignature().defineNewArg(ArgType.NUMERIC, ArgType.MONTAG, ArgType.NATIONGEN_ID)),
    DOM_SUMMON_HALF("#domsummon2", new CommandSignature().defineNewArg(ArgType.NUMERIC, ArgType.MONTAG, ArgType.NATIONGEN_ID)),
    DOM_SUMMON_TWENTIETH("#domsummon20", new CommandSignature().defineNewArg(ArgType.NUMERIC, ArgType.MONTAG, ArgType.NATIONGEN_ID)),
    DOM_SUMMON_RARE("#raredomsummon", new CommandSignature().defineNewArg(ArgType.NUMERIC, ArgType.MONTAG, ArgType.NATIONGEN_ID)),
    //FIRST_SHAPE("#firstshape", new CommandSignature().defineNewArg(ArgType.NUMERIC, ArgType.MONTAG, ArgType.NATIONGEN_ID)),
    GUARD_SPIRIT("#guardspirit", new CommandSignature().defineNewArg(ArgType.NUMERIC, ArgType.MONTAG, ArgType.NATIONGEN_ID)),
    MAKE_MONSTERS_1("#makemonsters1", new CommandSignature().defineNewArg(ArgType.NUMERIC, ArgType.MONTAG, ArgType.NATIONGEN_ID)),
    MAKE_MONSTERS_2("#makemonsters2", new CommandSignature().defineNewArg(ArgType.NUMERIC, ArgType.MONTAG, ArgType.NATIONGEN_ID)),
    MAKE_MONSTERS_3("#makemonsters3", new CommandSignature().defineNewArg(ArgType.NUMERIC, ArgType.MONTAG, ArgType.NATIONGEN_ID)),
    MAKE_MONSTERS_4("#makemonsters4", new CommandSignature().defineNewArg(ArgType.NUMERIC, ArgType.MONTAG, ArgType.NATIONGEN_ID)),
    MAKE_MONSTERS_5("#makemonsters5", new CommandSignature().defineNewArg(ArgType.NUMERIC, ArgType.MONTAG, ArgType.NATIONGEN_ID)),
    MONSTER_CANNOT_CAST("#notmnr", new CommandSignature().defineNewArg(ArgType.NUMERIC, ArgType.MONTAG, ArgType.NATIONGEN_ID)),
    MONTAG("#montag", new CommandSignature().defineNewArg(ArgType.NUMERIC, ArgType.NATIONGEN_ID)),
    NEEDS_MONSTER_TO_RECRUIT("#monpresentrec", new CommandSignature().defineNewArg(ArgType.NUMERIC, ArgType.MONTAG, ArgType.NATIONGEN_ID)),
    NEEDS_OWN_MONSTER_TO_RECRUIT("#ownsmonrec", new CommandSignature().defineNewArg(ArgType.NUMERIC, ArgType.MONTAG, ArgType.NATIONGEN_ID)),
    ONLY_MONSTER_CAN_CAST("#onlymnr", new CommandSignature().defineNewArg(ArgType.NUMERIC, ArgType.MONTAG, ArgType.NATIONGEN_ID)),
    PROPHET_SHAPE("#prophetshape", new CommandSignature().defineNewArg(ArgType.NUMERIC, ArgType.MONTAG, ArgType.NATIONGEN_ID)),
    RAISE_SHAPE("#raiseshape", new CommandSignature().defineNewArg(ArgType.NUMERIC, ArgType.MONTAG, ArgType.NATIONGEN_ID)),
    //SECOND_SHAPE("#secondshape", new CommandSignature().defineNewArg(ArgType.NUMERIC, ArgType.MONTAG, ArgType.NATIONGEN_ID)),
    SECOND_TMP_SHAPE("#secondtmpshape", new CommandSignature().defineNewArg(ArgType.NUMERIC, ArgType.MONTAG, ArgType.NATIONGEN_ID)),
    SUMMON_1("#summon1", new CommandSignature().defineNewArg(ArgType.NUMERIC, ArgType.MONTAG, ArgType.NATIONGEN_ID)),
    SUMMON_2("#summon2", new CommandSignature().defineNewArg(ArgType.NUMERIC, ArgType.MONTAG, ArgType.NATIONGEN_ID)),
    SUMMON_3("#summon3", new CommandSignature().defineNewArg(ArgType.NUMERIC, ArgType.MONTAG, ArgType.NATIONGEN_ID)),
    SUMMON_4("#summon4", new CommandSignature().defineNewArg(ArgType.NUMERIC, ArgType.MONTAG, ArgType.NATIONGEN_ID)),
    SUMMON_5("#summon5", new CommandSignature().defineNewArg(ArgType.NUMERIC, ArgType.MONTAG, ArgType.NATIONGEN_ID)),
    TEMPLE_TRAINER("#templetrainer", new CommandSignature().defineNewArg(ArgType.NUMERIC, ArgType.MONTAG, ArgType.NATIONGEN_ID)),
    XP_SHAPE_MONSTER("xpshapemon", new CommandSignature().defineNewArg(ArgType.NUMERIC, ArgType.MONTAG, ArgType.NATIONGEN_ID));

    private String baseString;
    private CommandSignature signature;

    CommandType(String commandString, CommandSignature signature) {
        this.baseString = commandString;
        this.signature = signature;
    }

    public String toModCommand() {
        return this.baseString;
    }

    public boolean acceptsArgTypes(ArgType... type) {
        return List.of(type).stream().anyMatch(t -> this.signature.acceptsArgType(t));
    }

    public static CommandType fromCommand(Command command) {
        String commandString = command.command;
        return Stream.of(CommandType.values())
            .filter(type -> {
                return type.toModCommand().equals(commandString);
            })
            .findFirst()
            .orElse(null);
    }
}

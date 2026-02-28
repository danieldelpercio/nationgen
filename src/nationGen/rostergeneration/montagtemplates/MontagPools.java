package nationGen.rostergeneration.montagtemplates;

import java.util.List;

import nationGen.NationGen;
import nationGen.ids.DominionsId;
import nationGen.misc.Arg;
import nationGen.misc.ArgType;
import nationGen.misc.Command;
import nationGen.misc.CommandType;

import java.util.ArrayList;

public class MontagPools {
    private NationGen nationGen;
    private List<MontagPool> pools = new ArrayList<>();
    private List<Command> montagCommandsToResolve = new ArrayList<>();

    public MontagPools(NationGen nationGen) {
        this.nationGen = nationGen;
    }

    public MontagPool addPool(String montagPoolId) {
        MontagPool pool = this.getPool(montagPoolId);

        if (pool == null) {
            this.pools.add(new MontagPool(montagPoolId, nationGen));
            pool = this.pools.getLast();
        }

        return pool;
    }

    public MontagPool getPool(String montagPoolId) {
        return this.pools
            .stream()
            .filter(p -> p.poolId.getNationGenId().equals(montagPoolId))
            .findFirst()
            .orElse(null);
    }

    public void resolvePools() {
        List<Command> resolvedCommands = new ArrayList<>();

        this.pools.forEach(p -> {
            p.assignIngameId();

            this.montagCommandsToResolve.forEach(c -> {
                String montaString = MontagCommand.getMontagArgumentFromCommand(c);
                
                if (montaString.equals(p.poolId.getNationGenId())) {
                    int montagArgIndex = MontagCommand.MONTAG.getIndexOfMontagArg();
                    int ingamePoolId = p.poolId.getIngameId();

                    // Replace the old NationGen id that the command used with the ingame resolved one
                    c.args.set(montagArgIndex, new Arg(ingamePoolId));
                    resolvedCommands.add(c);
                }
            });

            this.montagCommandsToResolve.removeAll(resolvedCommands);
        });
    }

    /*
    public MontagPools handleMontagCommand(Unit unit, Command command) {
        if (!MontagCommand.MONTAG.isSameAs(command)) {
            return this;
        }

        String montaString = MontagCommand.getMontagArgumentFromCommand(command);
        this.addPool(montaString);
        this.montagCommandsToResolve.add(command);
        return this;
    }
    */

    public MontagPools handleMontagCommandType(Command command) {
        CommandType type = CommandType.fromCommand(command);

        if (type == null || type.acceptsArgTypes(ArgType.MONTAG)) {
            return this;
        }

        if (MontagCommand.ADD_MONSTER_TO_POOL.isSameAs(command)) {
            return this.handleAddMonsterCommand(command);
        }

        else {
            String montaString = MontagCommand.getMontagArgumentFromCommand(command);
            this.addPool(montaString);
            this.montagCommandsToResolve.add(command);
            return this;
        }
    }

    public MontagPools handleAddMonsterCommand(Command command) {
        if (!MontagCommand.ADD_MONSTER_TO_POOL.isSameAs(command)) {
            throw new IllegalArgumentException(
                "Expected command to be " +
                MontagCommand.ADD_MONSTER_TO_POOL.toModCommand() +
                "; instead got " +
                command.toString()
            );
        }

        String montaString = MontagCommand.getMontagArgumentFromCommand(command);
        DominionsId monsterId = new DominionsId(command.args.getString(0));
        MontagPool pool = this.addPool(montaString);
        pool.addUnit(monsterId);
        return this;
    }

    public List<String> writePools() {
        List<String> lines = new ArrayList<>();

        this.pools.forEach(p -> {
            lines.addAll(p.writePool());
            lines.add("");
        });

        return lines;
    }
}

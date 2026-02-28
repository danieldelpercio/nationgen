package nationGen.rostergeneration.montagtemplates;

import java.util.ArrayList;
import java.util.List;

import nationGen.NationGen;
import nationGen.ids.DominionsId;

public class MontagPool {

    public DominionsId poolId;
    private NationGen nationGen;
    private List<DominionsId> taggedUnitIds;
    
    public MontagPool(String poolName, NationGen nationGen) {
        this.poolId = new DominionsId(poolName);
        this.nationGen = nationGen;
    }

    public Boolean isPoolIdAssigned() {
        return poolId.isResolved();
    }

    public MontagPool addUnit(DominionsId unitId) {
        if (unitId == null) {
            throw new IllegalArgumentException("MontagPool.addUnit() expected unitId to be an instance of UnitId");
        }

        this.taggedUnitIds.add(unitId);
        return this;
    }

    public MontagPool removeUnit(DominionsId unitId) {
        if (unitId == null) {
            return this;
        }

        this.taggedUnitIds.removeIf(id -> id.equals(unitId));
        return this;
    }

    public void assignIngameId() {
        int poolIngameId = this.nationGen.nextMontagId();
        this.poolId.setIngameId(poolIngameId);
    }

    public List<String> writePool() {
        List<String> lines = new ArrayList<>();

        if (this.poolId.isResolved()) {
            throw new IllegalStateException(
                "Cannot write lines of montag pool " +
                this.poolId.getNationGenId() +
                " because its Dominions id is not assigned yet!"
            );
        }

        lines.add(
            "-- Montag pool " +
            this.poolId.getIngameId() +
            " (" + this.poolId.getNationGenId() + ")"
        );

        taggedUnitIds.forEach(dominionsId -> {
            if (!dominionsId.isResolved()) {
                throw new IllegalStateException(
                    "Unit id " +
                    dominionsId.getNationGenId() +
                    " from montag pool " +
                    poolId.getNationGenId() +
                    " is not resolved!"
                );
            }

            lines.add("#selectmonster " + dominionsId.getIngameId());
            lines.add(MontagCommand.MONTAG.toModCommand() + " " + poolId.getIngameId());
            lines.add("#end");
            lines.add("");
        });

        return lines;
    }
}

package nationGen.items;

import nationGen.CustomItemsHandler;

/**
 * A "Dominions id" is an id above 0 which can or must be written into
 * an #armor or #weapon command. A "basesprite" item does not have a
 * Dominions id, but an equipped Greatsword does. "Item" is a misleading
 * name in NationGen, since it can refer both to the equipped Dominions
 * items as well as to cosmetic sprite parts, such as "hands" or "shadow".
 */
public class DominionsId {
    private String customItemName = "";
    private Integer dominionsId = -1;

    public DominionsId() {}
    public DominionsId(String customItemName) {
        this.customItemName = customItemName;
    }

    public String getCustomItemName() {
        return this.customItemName;
    }

    public DominionsId setCustomItemName(String name) {
        this.customItemName = name;
        return this;
    }

    public Integer getDominionsId() {
        return this.dominionsId;
    }

    public DominionsId setDominionsId(Integer id) {
        this.dominionsId = id;
        return this;
    }

    public Boolean isBlank() {
        return this.customItemName.isBlank() && this.dominionsId == -1;
    }

    public Boolean isResolved() {
        return this.dominionsId > 0;
    }

    public Boolean isDominionsCustomItemId() {
        if (!this.isResolved()) {
            return !this.customItemName.isBlank();
        }

        return CustomItemsHandler.isCustomId(dominionsId);
    }
}

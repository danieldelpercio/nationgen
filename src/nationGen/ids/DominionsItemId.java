package nationGen.ids;

public class DominionsItemId extends DominionsId {
    
    public DominionsItemId() {
        super();
    }

    public boolean isVanillaItem() {
        int id = this.getIngameId();
        boolean isVanillaId = id > 0 && id < IdHandler.MIN_CUSTOM_ARMOR_ID;
        return this.isResolved() && isVanillaId;
    }

    public boolean isCustomItem() {
        return !this.isVanillaItem();
    }

     public boolean isCustomArmor() {
        int id = this.getIngameId();
        boolean isCustomArmorId = id >= IdHandler.MIN_CUSTOM_ARMOR_ID && id <= IdHandler.MAX_CUSTOM_ARMOR_ID;
        return this.isResolved() && isCustomArmorId;
    }

     public boolean isCustomWeapon() {
        int id = this.getIngameId();
        boolean isCustomArmorId = id >= IdHandler.MIN_CUSTOM_WEAPON_ID && id <= IdHandler.MAX_CUSTOM_WEAPON_ID;
        return this.isResolved() && isCustomArmorId;
    }
}

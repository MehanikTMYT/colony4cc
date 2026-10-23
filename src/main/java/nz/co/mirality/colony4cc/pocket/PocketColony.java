package nz.co.mirality.colony4cc.pocket;

import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.pocket.AbstractPocketUpgrade;
import dan200.computercraft.api.pocket.IPocketAccess;
import dan200.computercraft.api.upgrades.UpgradeType;
import net.minecraft.world.item.ItemStackTemplate;
import nz.co.mirality.colony4cc.Colony4CC;
import nz.co.mirality.colony4cc.peripheral.PocketColonyPeripheral;

import javax.annotation.Nullable;

/** PORT(26.1.2): data-driven CC upgrade (UpgradeType.simpleWithCustomItem); the crafting item comes from the JSON. */
public class PocketColony extends AbstractPocketUpgrade {
    public PocketColony(ItemStackTemplate craftingItem) {
        super("upgrade.colony4cc.pocket_colony.adjective", craftingItem);
    }

    @Nullable
    @Override
    public IPeripheral createPeripheral(IPocketAccess access) {
        return new PocketColonyPeripheral();
    }

    @Override
    public void update(IPocketAccess access, @Nullable IPeripheral peripheral) {
        if (!(peripheral instanceof PocketColonyPeripheral colony)) return;
        colony.setEntity(access.getEntity());
        access.setLight(colony.isValid() ? 0x00BA00 : 0xBA0000);
    }

    @Override
    public UpgradeType<PocketColony> getType() {
        return Colony4CC.POCKET_COLONY.get();
    }
}

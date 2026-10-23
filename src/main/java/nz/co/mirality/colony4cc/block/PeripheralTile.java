package nz.co.mirality.colony4cc.block;

import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import nz.co.mirality.colony4cc.Colony4CC;
import nz.co.mirality.colony4cc.peripheral.TileColonyPeripheral;

/** PORT(26.1.2): the peripheral is exposed through CC's PeripheralCapability (registered in Colony4CC). */
public class PeripheralTile extends BlockEntity {
    private final TileColonyPeripheral peripheral;

    public PeripheralTile(BlockPos pos, BlockState state) {
        super(Colony4CC.PERIPHERAL_TILE.get(), pos, state);
        this.peripheral = new TileColonyPeripheral(this);
    }

    public IPeripheral getPeripheral() {
        return this.peripheral;
    }
}

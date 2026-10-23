package nz.co.mirality.colony4cc.peripheral;

import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import nz.co.mirality.colony4cc.block.PeripheralTile;

import javax.annotation.Nullable;

public class TileColonyPeripheral extends ColonyPeripheral {
    public TileColonyPeripheral(PeripheralTile tile) {
        this.tile = tile;
    }

    private final PeripheralTile tile;

    @Override
    public boolean equals(@Nullable IPeripheral other) {
        if (other instanceof TileColonyPeripheral peripheral) {
            return this.tile == peripheral.tile;
        }
        return false;
    }

    @Nullable
    @Override
    public Object getTarget() {
        return this.tile;
    }

    @Nullable
    @Override
    public Level getWorld() {
        return this.tile.getLevel();
    }

    @Override
    public BlockPos getPos() {
        return this.tile.getBlockPos();
    }

    @Override
    @Nullable
    protected IItemHandler getInventory(@Nullable Direction side)
    {
        if (side == null) side = Direction.UP;
        final Level level = getWorld();
        if (level == null) return null;
        // PORT(26.1.2): transactional item capability, read through the legacy IItemHandler view
        final var handler = level.getCapability(Capabilities.Item.BLOCK, getPos().relative(side), side.getOpposite());
        return handler == null ? null : IItemHandler.of(handler);
    }
}

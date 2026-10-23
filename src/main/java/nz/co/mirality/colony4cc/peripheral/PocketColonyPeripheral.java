package nz.co.mirality.colony4cc.peripheral;

import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import nz.co.mirality.colony4cc.Colony4CC;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PocketColonyPeripheral extends ColonyPeripheral {
    private Level world;
    private BlockPos pos = BlockPos.ZERO;
    private Entity tracking;

    public void setEntity(Entity entity) {
        if (entity != null) {
            this.world = entity.level();
            this.pos = entity.blockPosition();
            this.tracking = entity;
        }
        this.securityCheck(entity);
    }

    @Override
    public Level getWorld() {
        return this.world;
    }

    @Override
    public BlockPos getPos() {
        return this.pos;
    }

    @Override
    public boolean equals(@Nullable IPeripheral other) {
        return this == other;
    }

    @Override
    public Object getTarget() {
        return this.tracking;
    }

    @Override
    @Nullable
    protected IItemHandler getInventory(@Nullable final Direction side)
    {
        if (tracking == null) return null;
        // PORT(26.1.2): the holder's inventory through the (void-context) entity item capability
        final var handler = tracking.getCapability(Capabilities.Item.ENTITY, null);
        return handler == null ? null : IItemHandler.of(handler);
    }

    @Override
    protected boolean consumeFuel(@Nonnull final IItemHandler handler, @Nonnull final Item fuel, final int count)
    {
        if (Colony4CC.CONFIG.getFreeCreativePocketPlayerCost() &&
                tracking instanceof ServerPlayer player &&
                player.isCreative()) {
            return true;
        }
        return super.consumeFuel(handler, fuel, count);
    }
}

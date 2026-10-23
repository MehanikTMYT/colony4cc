package nz.co.mirality.colony4cc.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

import javax.annotation.Nullable;

/** PORT(26.1.2): the 1.16 Material/ToolType properties are now a map colour plus the minecraft:mineable/pickaxe tag. */
public class PeripheralBlock extends BaseEntityBlock {
    public static final MapCodec<PeripheralBlock> CODEC = simpleCodec(PeripheralBlock::new);

    public PeripheralBlock(Properties properties) {
        super(properties.mapColor(MapColor.METAL).strength(2.2f, 11.f).sound(SoundType.METAL).requiresCorrectToolForDrops());
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PeripheralTile(pos, state);
    }
}

package nz.co.mirality.colony4cc.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import nz.co.mirality.colony4cc.Colony4CC;

import java.util.List;

/**
 * Server -> client: show holographic overlays around a building for two minutes.
 * PORT(26.1.2): SimpleChannel message -> CustomPacketPayload; same fields and order as the 1.16 packet.
 */
public record SAddBuildingOverlayPacket(Identifier world, BlockPos pos, List<Overlay> overlays) implements CustomPacketPayload
{
    public static final Type<SAddBuildingOverlayPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(Colony4CC.ID, "add_building_overlay"));

    /** One box; {@code strong} overlays are drawn through blocks (the hut marker). */
    public record Overlay(BoundingBox bounds, int color, boolean fill, boolean strong)
    {
        private static final StreamCodec<ByteBuf, BoundingBox> BOUNDS = StreamCodec.of(
                (buf, box) -> {
                    ByteBufCodecs.VAR_INT.encode(buf, box.minX());
                    ByteBufCodecs.VAR_INT.encode(buf, box.minY());
                    ByteBufCodecs.VAR_INT.encode(buf, box.minZ());
                    ByteBufCodecs.VAR_INT.encode(buf, box.maxX());
                    ByteBufCodecs.VAR_INT.encode(buf, box.maxY());
                    ByteBufCodecs.VAR_INT.encode(buf, box.maxZ());
                },
                buf -> new BoundingBox(ByteBufCodecs.VAR_INT.decode(buf), ByteBufCodecs.VAR_INT.decode(buf), ByteBufCodecs.VAR_INT.decode(buf),
                        ByteBufCodecs.VAR_INT.decode(buf), ByteBufCodecs.VAR_INT.decode(buf), ByteBufCodecs.VAR_INT.decode(buf)));

        public static final StreamCodec<ByteBuf, Overlay> STREAM_CODEC = StreamCodec.composite(
                BOUNDS, Overlay::bounds,
                ByteBufCodecs.INT, Overlay::color,
                ByteBufCodecs.BOOL, Overlay::fill,
                ByteBufCodecs.BOOL, Overlay::strong,
                Overlay::new);
    }

    public static final StreamCodec<ByteBuf, SAddBuildingOverlayPacket> STREAM_CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC, SAddBuildingOverlayPacket::world,
            BlockPos.STREAM_CODEC, SAddBuildingOverlayPacket::pos,
            Overlay.STREAM_CODEC.apply(ByteBufCodecs.list()), SAddBuildingOverlayPacket::overlays,
            SAddBuildingOverlayPacket::new);

    public SAddBuildingOverlayPacket {
        overlays = List.copyOf(overlays);
    }

    @Override
    public Type<SAddBuildingOverlayPacket> type()
    {
        return TYPE;
    }
}

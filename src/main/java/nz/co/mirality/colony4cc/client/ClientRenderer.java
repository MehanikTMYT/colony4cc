package nz.co.mirality.colony4cc.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import nz.co.mirality.colony4cc.network.SAddBuildingOverlayPacket;
import org.joml.Matrix4f;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static nz.co.mirality.colony4cc.Colony4CC.ID;
import static nz.co.mirality.colony4cc.Constants.TICKS_PER_MINUTE;

/**
 * Draws building overlays requested by {@code colony.highlightBuilding}: each lives two minutes.
 * PORT(26.1.2): rewritten for the 26.1 render pipeline (RenderLevelStageEvent + buffer source); same colours,
 * strong (through-blocks) overlays first, then depth-tested ones, fills at 20% alpha.
 */
@EventBusSubscriber(modid = ID, value = Dist.CLIENT)
public final class ClientRenderer
{
    private ClientRenderer() {}

    private static final float LINE_WIDTH = 3.0F;
    private static final ConcurrentMap<Identifier, ConcurrentMap<BlockPos, Overlay>> overlays = new ConcurrentHashMap<>();

    private static final class Overlay
    {
        private final List<SAddBuildingOverlayPacket.Overlay> overlays;
        private int lifetime = 2 * TICKS_PER_MINUTE;

        Overlay(final List<SAddBuildingOverlayPacket.Overlay> overlays) { this.overlays = overlays; }

        boolean tick() { return --lifetime > 0; }
    }

    public static void addBuildingOverlay(final SAddBuildingOverlayPacket msg) {
        overlays.computeIfAbsent(msg.world(), k -> new ConcurrentHashMap<>()).put(msg.pos(), new Overlay(msg.overlays()));
    }

    @SubscribeEvent
    public static void registerPipelines(final RegisterRenderPipelinesEvent event) {
        event.registerPipeline(CustomRenderTypes.OVERLAY_LINES_PIPELINE);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void clientTick(final ClientTickEvent.Post event) {
        if (!Minecraft.getInstance().isPaused()) {
            overlays.values().forEach(posMap -> posMap.entrySet().removeIf(e -> !e.getValue().tick()));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void clientDisconnected(final ClientPlayerNetworkEvent.LoggingOut event) {
        overlays.clear();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void renderLevel(final RenderLevelStageEvent.AfterTranslucentFeatures event) {
        final var level = Minecraft.getInstance().level;
        if (level == null) return;
        final ConcurrentMap<BlockPos, Overlay> posMap = overlays.get(level.dimension().identifier());
        if (posMap == null || posMap.isEmpty()) return;

        final PoseStack poseStack = event.getPoseStack();
        final Vec3 camera = event.getLevelRenderState().cameraRenderState.pos;
        final MultiBufferSource.BufferSource buffers = Minecraft.getInstance().renderBuffers().bufferSource();

        for (final boolean strong : new boolean[] { true, false }) {
            final RenderType lines = strong ? CustomRenderTypes.OVERLAY_LINES : CustomRenderTypes.lines();
            for (final Overlay group : posMap.values()) {
                for (final SAddBuildingOverlayPacket.Overlay overlay : group.overlays) {
                    if (overlay.strong() != strong) continue;
                    final var b = overlay.bounds();
                    final AABB box = new AABB(b.minX(), b.minY(), b.minZ(), b.maxX() + 1, b.maxY() + 1, b.maxZ() + 1).inflate(0.002D);
                    final int border = ARGB.opaque(overlay.color());
                    ShapeRenderer.renderShape(poseStack, buffers.getBuffer(lines), Shapes.create(box.move(-camera.x, -camera.y, -camera.z)),
                            0, 0, 0, border, LINE_WIDTH);
                    if (overlay.fill()) {
                        renderBoxFill(poseStack, buffers.getBuffer(CustomRenderTypes.claimArea()), box.move(-camera.x, -camera.y, -camera.z),
                                ARGB.color(0x33, overlay.color()));
                    }
                }
            }
            buffers.endBatch(lines);
            buffers.endBatch(CustomRenderTypes.claimArea());
        }
    }

    private static void renderBoxFill(final PoseStack poseStack, final VertexConsumer builder, final AABB box, final int color) {
        final Matrix4f m = poseStack.last().pose();
        final float x0 = (float) box.minX, y0 = (float) box.minY, z0 = (float) box.minZ;
        final float x1 = (float) box.maxX, y1 = (float) box.maxY, z1 = (float) box.maxZ;
        quad(builder, m, color, x0, y0, z0, x0, y1, z0, x1, y1, z0, x1, y0, z0); // north
        quad(builder, m, color, x0, y0, z1, x1, y0, z1, x1, y1, z1, x0, y1, z1); // south
        quad(builder, m, color, x0, y0, z0, x1, y0, z0, x1, y0, z1, x0, y0, z1); // down
        quad(builder, m, color, x0, y1, z0, x0, y1, z1, x1, y1, z1, x1, y1, z0); // up
        quad(builder, m, color, x0, y0, z0, x0, y0, z1, x0, y1, z1, x0, y1, z0); // west
        quad(builder, m, color, x1, y0, z0, x1, y1, z0, x1, y1, z1, x1, y0, z1); // east
    }

    private static void quad(final VertexConsumer b, final Matrix4f m, final int color,
                             final float ax, final float ay, final float az, final float bx, final float by, final float bz,
                             final float cx, final float cy, final float cz, final float dx, final float dy, final float dz) {
        b.addVertex(m, ax, ay, az).setColor(color);
        b.addVertex(m, bx, by, bz).setColor(color);
        b.addVertex(m, cx, cy, cz).setColor(color);
        b.addVertex(m, dx, dy, dz).setColor(color);
    }
}

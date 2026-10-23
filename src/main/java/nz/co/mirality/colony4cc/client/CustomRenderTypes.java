package nz.co.mirality.colony4cc.client;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.LayeringTransform;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import nz.co.mirality.colony4cc.Colony4CC;

/**
 * PORT(26.1.2): the 1.16 composite render states became pipelines. OVERLAY_LINES keeps the "no depth test"
 * behaviour (hut markers visible through blocks); the claim/footprint fill uses vanilla translucent debug quads.
 */
public final class CustomRenderTypes
{
    private CustomRenderTypes() {}

    public static final RenderPipeline OVERLAY_LINES_PIPELINE = RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
            .withLocation(Identifier.fromNamespaceAndPath(Colony4CC.ID, "pipeline/overlay_lines"))
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false))
            .build();

    public static final RenderType OVERLAY_LINES = RenderType.create("colony4cc_overlay_lines",
            RenderSetup.builder(OVERLAY_LINES_PIPELINE).setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING).createRenderSetup());

    /** Depth-tested lines for footprint/claim frames. */
    public static RenderType lines()
    {
        return RenderTypes.linesTranslucent();
    }

    /** Translucent, double-sided quads for filled boxes. */
    public static RenderType claimArea()
    {
        return RenderTypes.debugQuads();
    }
}

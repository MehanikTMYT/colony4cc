package nz.co.mirality.colony4cc;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Colony4CCConfig
{
    private final ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
    private final ModConfigSpec spec;

    private final ModConfigSpec.BooleanValue freeCreativePocketPlayerCost;
    private final ModConfigSpec.IntValue highlightWorkerCostMultiplier;
    private final ModConfigSpec.IntValue highlightBuildingCostMultiplier;

    public Colony4CCConfig()
    {
        builder.push("balance");

        freeCreativePocketPlayerCost = builder.comment("True if creative players using a Pocket Computer don't get charged item costs")
                .translation("config.colony4cc.freeCreativePocketPlayerCost")
                .define("freeCreativePocketPlayerCost", true);

        highlightWorkerCostMultiplier = builder.comment("0 = highlightWorker is always free; 1 = normal price")
                .translation("config.colony4cc.highlightWorkerCostMultiplier")
                .defineInRange("highlightWorkerCostMultiplier", 1, 0, 2);

        highlightBuildingCostMultiplier = builder.comment("0 = highlightBuilding is always free; 1 = normal price; 2+ = cost multiplier")
                .translation("config.colony4cc.highlightBuildingCostMultiplier")
                .defineInRange("highlightBuildingCostMultiplier", 1, 0, 8);

        builder.pop();

        spec = builder.build();
    }

    public ModConfigSpec getSpec() { return spec; }

    public boolean getFreeCreativePocketPlayerCost() { return freeCreativePocketPlayerCost.get(); }
    public int getHighlightWorkerCostMultiplier() { return highlightWorkerCostMultiplier.get(); }
    public int getHighlightBuildingCostMultiplier() { return highlightBuildingCostMultiplier.get(); }
}

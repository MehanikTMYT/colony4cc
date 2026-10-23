package nz.co.mirality.colony4cc;

import net.minecraft.resources.Identifier;

import static nz.co.mirality.colony4cc.Colony4CC.ID;

public class Constants
{
    private Constants() {}

    public static final int TICKS_PER_SECOND = 20;
    public static final int TICKS_PER_MINUTE = TICKS_PER_SECOND * 60;

    public static final Identifier RESEARCH_FREE_WORKER_HIGHLIGHT
            = Identifier.fromNamespaceAndPath(ID, "effects/free_worker_highlight");
    public static final Identifier RESEARCH_FREE_BUILDING_HIGHLIGHT
            = Identifier.fromNamespaceAndPath(ID, "effects/free_building_highlight");
}

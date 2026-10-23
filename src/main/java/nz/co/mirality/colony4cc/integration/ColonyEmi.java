package nz.co.mirality.colony4cc.integration;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiInfoRecipe;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import nz.co.mirality.colony4cc.Colony4CC;

import java.util.List;

/** PORT(26.1.2): the JEI ingredient info became an EMI info recipe (EMI only in this pack). */
@EmiEntrypoint
public class ColonyEmi implements EmiPlugin {
    @Override
    public void register(EmiRegistry registry) {
        registry.addRecipe(new EmiInfoRecipe(List.of(EmiStack.of(Colony4CC.RGB_CHARGE.get())),
                List.of(Component.translatable("info.colony4cc.rgb_charge")),
                Identifier.fromNamespaceAndPath(Colony4CC.ID, "/info/rgb_charge")));
    }
}

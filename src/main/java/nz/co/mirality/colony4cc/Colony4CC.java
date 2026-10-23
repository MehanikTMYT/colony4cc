package nz.co.mirality.colony4cc;

import dan200.computercraft.api.peripheral.PeripheralCapability;
import dan200.computercraft.api.pocket.IPocketUpgrade;
import dan200.computercraft.api.upgrades.UpgradeType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import nz.co.mirality.colony4cc.block.PeripheralBlock;
import nz.co.mirality.colony4cc.block.PeripheralTile;
import nz.co.mirality.colony4cc.network.Colony4CCPacketHandler;
import nz.co.mirality.colony4cc.pocket.PocketColony;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * PORT(26.1.2): NeoForge registration. The pocket upgrade is a data-driven CC:Tweaked upgrade type
 * (data/colony4cc/computercraft/pocket_upgrade/pocket_colony.json); CC applies it to pocket computers with its own
 * upgrade recipe. The 1.16 "colony + wireless modem" combined upgrades and their PocketServerComputer mixin are gone:
 * CC:Tweaked 1.120 pocket computers have two upgrade slots, so a wireless modem and the colony upgrade fit together.
 */
@Mod(Colony4CC.ID)
public final class Colony4CC {
    public static final String ID = "colony4cc";
    public static final String NAME = "MineColonies for ComputerCraft";
    public static final String CC_MOD_ID = "computercraft";
    public static final String COLONY_MOD_ID = "minecolonies";
    public static final String PERIPHERAL_ID = "colony_peripheral";
    public static final String PERIPHERAL_NAME = "colony";

    public static final Logger LOGGER = LogManager.getLogger();
    public static final Colony4CCConfig CONFIG = new Colony4CCConfig();

    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ID);
    private static final DeferredRegister<BlockEntityType<?>> TILES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ID);
    private static final DeferredRegister<UpgradeType<? extends IPocketUpgrade>> POCKET_UPGRADES =
            DeferredRegister.create(IPocketUpgrade.typeRegistry(), ID);

    public static final DeferredBlock<PeripheralBlock> PERIPHERAL_BLOCK
            = BLOCKS.registerBlock(PERIPHERAL_ID, PeripheralBlock::new);

    public static final DeferredItem<BlockItem> PERIPHERAL_ITEM
            = ITEMS.registerSimpleBlockItem(PERIPHERAL_ID, PERIPHERAL_BLOCK);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PeripheralTile>> PERIPHERAL_TILE
            = TILES.register(PERIPHERAL_ID, () -> new BlockEntityType<>(PeripheralTile::new, PERIPHERAL_BLOCK.get()));

    public static final DeferredItem<Item> RGB_CHARGE = ITEMS.registerSimpleItem("rgb_charge");

    public static final DeferredHolder<UpgradeType<? extends IPocketUpgrade>, UpgradeType<PocketColony>> POCKET_COLONY
            = POCKET_UPGRADES.register("pocket_colony", () -> UpgradeType.simpleWithCustomItem(PocketColony::new));

    private static final ResourceKey<CreativeModeTab> CC_TAB =
            ResourceKey.create(Registries.CREATIVE_MODE_TAB, Identifier.fromNamespaceAndPath(CC_MOD_ID, "tab"));

    public Colony4CC(IEventBus bus, ModContainer container) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        TILES.register(bus);
        POCKET_UPGRADES.register(bus);
        container.registerConfig(ModConfig.Type.SERVER, CONFIG.getSpec());
        bus.addListener(Colony4CCPacketHandler::register);
        bus.addListener(Colony4CC::registerCapabilities);
        bus.addListener(Colony4CC::addToCreativeTab);
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(PeripheralCapability.get(), PERIPHERAL_TILE.get(), (tile, side) -> tile.getPeripheral());
    }

    // The 1.16 version put its items in ComputerCraft's item group; do the same with CC's creative tab.
    private static void addToCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CC_TAB) {
            event.accept(PERIPHERAL_ITEM);
            event.accept(RGB_CHARGE);
        }
    }
}

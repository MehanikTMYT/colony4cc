package nz.co.mirality.colony4cc.network;

import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/** PORT(26.1.2): SimpleChannel -> payload registration. The overlay packet is clientbound only. */
public class Colony4CCPacketHandler
{
    private static final String PROTOCOL_VERSION = "1";

    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);
        registrar.playToClient(SAddBuildingOverlayPacket.TYPE, SAddBuildingOverlayPacket.STREAM_CODEC, (packet, context) -> {
            if (FMLEnvironment.getDist().isClient()) {
                nz.co.mirality.colony4cc.client.ClientRenderer.addBuildingOverlay(packet);
            }
        });
    }
}

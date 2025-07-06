package karrito.mods.tempbansm;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

@EventBusSubscriber(modid = Tempbansm.MODID, bus = EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    // Configuración para mensajes personalizados
    public static final ModConfigSpec.ConfigValue<String> TEMPBAN_MESSAGE = BUILDER
            .comment("Mensaje que se muestra a los jugadores cuando son baneados temporalmente")
            .define("mensajeBaneoTemporal", "§6⏰ Tiempo de juego completado\n" +
                    "§fDebes esperar antes de reconectarte.\n" +
                    "§fTiempo restante: §a{tiempo}\n" +
                    "§b¡Gracias por jugar responsablemente!");

    public static final ModConfigSpec.BooleanValue SHOW_DETAILED_BANLIST = BUILDER
            .comment("Mostrar información detallada en el comando de lista de baneos")
            .define("mostrarListaDetalladaBaneos", true);

    public static final ModConfigSpec SPEC = BUILDER.build();

    public static String tempbanMessage;
    public static boolean showDetailedBanlist;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        tempbanMessage = TEMPBAN_MESSAGE.get();
        showDetailedBanlist = SHOW_DETAILED_BANLIST.get();
    }
}
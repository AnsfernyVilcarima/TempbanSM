package karrito.mods.tempbansm;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    // Configuración para mensajes personalizados
    public static final ModConfigSpec.ConfigValue<String> TEMPBAN_MESSAGE = BUILDER
            .comment("Mensaje que se muestra a los jugadores cuando son baneados temporalmente")
            .define("mensajeBaneoTemporal", """
                    §6⏰ Tiempo de juego completado
                    §fDebes esperar antes de reconectarte.
                    §fTiempo restante: §a{tiempo}
                    §b¡Gracias por jugar responsablemente!""");

    public static final ModConfigSpec.BooleanValue SHOW_DETAILED_BANLIST = BUILDER
            .comment("Mostrar información detallada en el comando de lista de baneos")
            .define("mostrarListaDetalladaBaneos", true);

    public static final ModConfigSpec SPEC = BUILDER.build();

    public static String tempbanMessage;
    public static boolean showDetailedBanlist;

    @SubscribeEvent
    public static void onLoad(final ModConfigEvent event) {
        tempbanMessage = TEMPBAN_MESSAGE.get();
        showDetailedBanlist = SHOW_DETAILED_BANLIST.get();
    }
}
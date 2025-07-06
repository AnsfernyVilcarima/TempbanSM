package karrito.mods.tempbansm;

import net.minecraft.commands.Commands;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@Mod(Tempbansm.MODID)
public class Tempbansm {
    public static final String MODID = "tempbansm";

    public Tempbansm(IEventBus modEventBus, ModContainer modContainer) {
        // Registrar configuración
        modContainer.registerConfig(ModConfig.Type.SERVER, Config.SPEC);

        // Registrar el evento de comandos
        NeoForge.EVENT_BUS.addListener(this::registerCommands);
    }

    public void registerCommands(RegisterCommandsEvent event) {
        if (event.getCommandSelection() != Commands.CommandSelection.INTEGRATED) {
            TempbanCommand.register(event.getDispatcher());
            BetterBanListCommand.register(event.getDispatcher());
        }
    }
}
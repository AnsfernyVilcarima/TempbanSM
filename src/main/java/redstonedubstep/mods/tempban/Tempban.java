package redstonedubstep.mods.tempban;

import net.minecraft.commands.Commands;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@Mod("tempbansm")
public class Tempban {
	public Tempban(IEventBus modEventBus, ModContainer modContainer) {
		NeoForge.EVENT_BUS.addListener(this::registerCommands);
	}

	public void registerCommands(RegisterCommandsEvent event){
		if (event.getCommandSelection() != Commands.CommandSelection.INTEGRATED) {
			BetterBanListCommand.register(event.getDispatcher());
			TempbanCommand.register(event.getDispatcher());
			TempbanIpCommand.register(event.getDispatcher());
		}
	}
}
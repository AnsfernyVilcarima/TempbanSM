package redstonedubstep.mods.tempban;

import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;

import org.apache.commons.lang3.time.DateUtils;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.network.chat.Component;
import net.minecraft.server.commands.BanIpCommands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.IpBanList;
import net.minecraft.server.players.IpBanListEntry;

public class TempbanIpCommand {
	private static final SimpleCommandExceptionType ERROR_INVALID_IP = new SimpleCommandExceptionType(Component.literal("§cDirección IP inválida"));
	private static final SimpleCommandExceptionType FAILED_EXCEPTION = new SimpleCommandExceptionType(Component.literal("§cNo se pudo banear la IP (ya está baneada)"));

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal("tempban-ip").requires(p -> p.hasPermission(3))
				.then(Commands.argument("target", StringArgumentType.word())
						.then(Commands.argument("months", IntegerArgumentType.integer(0))
								.then(Commands.argument("days", IntegerArgumentType.integer(0))
										.then(Commands.argument("hours", IntegerArgumentType.integer(0))
												.executes(TempbanIpCommand::tempbanUsernameOrIp)
												.then(Commands.argument("reason", MessageArgument.message())
														.executes(TempbanIpCommand::tempbanUsernameOrIp)))))));
	}

	private static int tempbanUsernameOrIp(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		return tempbanUsernameOrIp(ctx.getSource(), StringArgumentType.getString(ctx, "target"), IntegerArgumentType.getInteger(ctx, "months"), IntegerArgumentType.getInteger(ctx, "days"), IntegerArgumentType.getInteger(ctx, "hours"), MessageArgument.getMessage(ctx, "reason"));
	}

	private static int tempbanUsernameOrIp(CommandSourceStack source, String username, int monthDuration, int dayDuration, int hourDuration, Component reason) throws CommandSyntaxException {
		Matcher matcher = BanIpCommands.IP_ADDRESS_PATTERN.matcher(username);
		if (matcher.matches()) {
			return tempbanIpAddress(source, username, monthDuration, dayDuration, hourDuration, reason);
		} else {
			ServerPlayer serverplayer = source.getServer().getPlayerList().getPlayerByName(username);
			if (serverplayer != null) {
				return tempbanIpAddress(source, serverplayer.getIpAddress(), monthDuration, dayDuration, hourDuration, reason);
			} else {
				throw ERROR_INVALID_IP.create();
			}
		}
	}

	private static int tempbanIpAddress(CommandSourceStack source, String ip, int monthDuration, int dayDuration, int hourDuration, Component reason) throws CommandSyntaxException {
		IpBanList ipbanlist = source.getServer().getPlayerList().getIpBans();
		Date date = DateUtils.addMonths(DateUtils.addDays(DateUtils.addHours(new Date(), hourDuration), dayDuration), monthDuration);

		if (ipbanlist.isBanned(ip)) {
			throw FAILED_EXCEPTION.create();
		} else {
			List<ServerPlayer> list = source.getServer().getPlayerList().getPlayersWithAddress(ip);
			IpBanListEntry ipbanentry = new IpBanListEntry(ip,null, source.getTextName(), date, reason == null ? null : reason.getString());
			ipbanlist.add(ipbanentry);
			source.sendSuccess(() -> Component.literal("§a✅ IP " + ip + " baneada por " + monthDuration + " meses, " + dayDuration + " días y " + hourDuration + " horas. Razón: " + ipbanentry.getReason()), true);
			if (!list.isEmpty()) {
				source.sendSuccess(() -> Component.literal("§e📢 " + list.size() + " jugadores fueron afectados: " + EntitySelector.joinNames(list).getString()), true);
			}

			for(ServerPlayer serverplayerentity : list) {
				serverplayerentity.connection.disconnect(Component.literal("§6⏰ Tiempo de juego completado\n§fDebes esperar antes de reconectarte.\n§fTiempo restante: §a" + getTimeString(monthDuration, dayDuration, hourDuration) + "\n§b¡Gracias por jugar responsablemente!"));
			}

			return list.size();
		}
	}
	
	private static String getTimeString(int months, int days, int hours) {
		StringBuilder time = new StringBuilder();
		if (months > 0) time.append(months).append(" meses ");
		if (days > 0) time.append(days).append(" días ");
		if (hours > 0) time.append(hours).append(" horas");
		return time.toString().trim();
	}
}
package karrito.mods.tempbansm;

import java.util.Collection;
import java.util.Date;

import org.apache.commons.lang3.time.DateUtils;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.UserBanList;
import net.minecraft.server.players.UserBanListEntry;

public class TempbanCommand {
    private static final SimpleCommandExceptionType ERROR_ALREADY_BANNED =
            new SimpleCommandExceptionType(Component.literal("§cEl jugador ya está baneado"));

    private static final SimpleCommandExceptionType ERROR_INVALID_DURATION =
            new SimpleCommandExceptionType(Component.literal("§cDebe especificar al menos 1 hora, día o mes"));

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("tempban")
                .requires(source -> source.hasPermission(3))
                .then(Commands.argument("targets", GameProfileArgument.gameProfile())
                        .then(Commands.argument("months", IntegerArgumentType.integer(0))
                                .then(Commands.argument("days", IntegerArgumentType.integer(0))
                                        .then(Commands.argument("hours", IntegerArgumentType.integer(0))
                                                .executes(TempbanCommand::tempbanPlayers)
                                                .then(Commands.argument("reason", MessageArgument.message())
                                                        .executes(TempbanCommand::tempbanPlayers)))))));
    }

    private static int tempbanPlayers(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Collection<GameProfile> targets = GameProfileArgument.getGameProfiles(ctx, "targets");
        int months = IntegerArgumentType.getInteger(ctx, "months");
        int days = IntegerArgumentType.getInteger(ctx, "days");
        int hours = IntegerArgumentType.getInteger(ctx, "hours");

        Component reason = null;
        try {
            reason = MessageArgument.getMessage(ctx, "reason");
        } catch (IllegalArgumentException e) {
            // No reason provided, use default
        }

        return tempbanPlayers(ctx.getSource(), targets, months, days, hours, reason);
    }

    private static int tempbanPlayers(CommandSourceStack source, Collection<GameProfile> toBeBanned,
                                      int monthDuration, int dayDuration, int hourDuration, Component reason) throws CommandSyntaxException {

        // Validar que al menos uno de los valores de tiempo sea mayor que 0
        if (monthDuration == 0 && dayDuration == 0 && hourDuration == 0) {
            throw ERROR_INVALID_DURATION.create();
        }

        UserBanList banlist = source.getServer().getPlayerList().getBans();
        int bannedCount = 0;
        Date banExpiry = DateUtils.addMonths(
                DateUtils.addDays(
                        DateUtils.addHours(new Date(), hourDuration),
                        dayDuration
                ),
                monthDuration
        );

        for (GameProfile gameprofile : toBeBanned) {
            if (!banlist.isBanned(gameprofile)) {
                UserBanListEntry banEntry = new UserBanListEntry(
                        gameprofile,
                        null,
                        source.getTextName(),
                        banExpiry,
                        reason == null ? "Baneo temporal" : reason.getString()
                );

                banlist.add(banEntry);
                bannedCount++;

                // Mensaje de confirmación para el admin
                String playerName = gameprofile.getName();
                String timeString = getTimeString(monthDuration, dayDuration, hourDuration);
                source.sendSuccess(() -> Component.literal(
                        "§a✅ Jugador " + playerName +
                                " baneado temporalmente por " + timeString +
                                ". Razón: " + banEntry.getReason()
                ), true);

                // Desconectar al jugador si está online
                ServerPlayer serverplayer = source.getServer().getPlayerList().getPlayer(gameprofile.getId());
                if (serverplayer != null) {
                    serverplayer.connection.disconnect(Component.literal(
                            Config.tempbanMessage.replace("{tiempo}", timeString)
                    ));
                }
            }
        }

        if (bannedCount == 0) {
            throw ERROR_ALREADY_BANNED.create();
        }

        return bannedCount;
    }

    private static String getTimeString(int months, int days, int hours) {
        StringBuilder time = new StringBuilder();
        if (months > 0) time.append(months).append(" mes").append(months > 1 ? "es" : "").append(" ");
        if (days > 0) time.append(days).append(" día").append(days > 1 ? "s" : "").append(" ");
        if (hours > 0) time.append(hours).append(" hora").append(hours > 1 ? "s" : "");
        return time.toString().trim();
    }
}
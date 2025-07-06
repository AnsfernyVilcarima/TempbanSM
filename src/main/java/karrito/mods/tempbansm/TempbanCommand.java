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
        return tempbanPlayers(
                ctx.getSource(),
                GameProfileArgument.getGameProfiles(ctx, "targets"),
                IntegerArgumentType.getInteger(ctx, "months"),
                IntegerArgumentType.getInteger(ctx, "days"),
                IntegerArgumentType.getInteger(ctx, "hours"),
                MessageArgument.getMessage(ctx, "reason")
        );
    }

    private static int tempbanPlayers(CommandSourceStack source, Collection<GameProfile> toBeBanned,
                                      int monthDuration, int dayDuration, int hourDuration, Component reason) throws CommandSyntaxException {

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
                        reason == null ? null : reason.getString()
                );

                banlist.add(banEntry);
                bannedCount++;

                // Mensaje de confirmación para el admin - CORREGIDO
                String playerName = gameprofile.getName();
                source.sendSuccess(() -> Component.literal(
                        "§a✅ Jugador " + playerName +
                                " baneado por " + monthDuration + " meses, " + dayDuration + " días y " +
                                hourDuration + " horas. Razón: " + banEntry.getReason()
                ), true);

                // Desconectar al jugador si está online
                ServerPlayer serverplayer = source.getServer().getPlayerList().getPlayer(gameprofile.getId());
                if (serverplayer != null) {
                    serverplayer.connection.disconnect(Component.literal(
                            "§6⏰ Tiempo de juego completado\n" +
                                    "§fDebes esperar antes de reconectarte.\n" +
                                    "§fTiempo restante: §a" + getTimeString(monthDuration, dayDuration, hourDuration) + "\n" +
                                    "§b¡Gracias por jugar responsablemente!"
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
        if (months > 0) time.append(months).append(" meses ");
        if (days > 0) time.append(days).append(" días ");
        if (hours > 0) time.append(hours).append(" horas");
        return time.toString().trim();
    }
}
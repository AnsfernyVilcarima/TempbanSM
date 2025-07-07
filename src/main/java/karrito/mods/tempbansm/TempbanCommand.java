package karrito.mods.tempbansm;

import java.util.Collection;
import java.util.Date;
import java.text.SimpleDateFormat;

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
                String timeString = getTimeString(monthDuration, dayDuration, hourDuration);

                // CREAR MENSAJE PERSONALIZADO PARA EL BANEO
                String customBanMessage = createCustomLoginMessage(banExpiry, timeString, reason);

                UserBanListEntry banEntry = new UserBanListEntry(
                        gameprofile,
                        null,
                        source.getTextName(),
                        banExpiry,
                        customBanMessage  // Usar nuestro mensaje personalizado
                );

                banlist.add(banEntry);
                bannedCount++;

                // Mensaje de confirmación para el admin
                String playerName = gameprofile.getName();
                source.sendSuccess(() -> Component.literal(
                        "§a✅ Jugador " + playerName +
                                " baneado temporalmente por " + timeString +
                                ". Razón: " + (reason == null ? "Baneo temporal" : reason.getString())
                ), true);

                // Desconectar al jugador si está online
                ServerPlayer serverplayer = source.getServer().getPlayerList().getPlayer(gameprofile.getId());
                if (serverplayer != null) {
                    serverplayer.connection.disconnect(Component.literal(
                            "§6⏰ Tiempo de juego completado\n" +
                                    "§fDebes esperar antes de reconectarte.\n" +
                                    "§fTiempo restante: §a" + timeString + "\n" +
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

    private static String createCustomLoginMessage(Date expiry, String timeString, Component reason) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        StringBuilder message = new StringBuilder();

        // Header simple pero elegante
        message.append("§6✨ SpecialMon ✨\n");
        message.append("§b⏰ Tiempo de Descanso ⏰\n\n");

        // Información de la sesión
        message.append("§aInformación de la Sesión\n");
        message.append("§7Tiempo restante: §e").append(timeString).append("\n");
        message.append("§7Fecha de regreso: §b").append(dateFormat.format(expiry)).append("\n");
        message.append("§7Hora de regreso: §a").append(timeFormat.format(expiry)).append("\n");

        if (reason != null && !reason.getString().equals("Baneo temporal")) {
            message.append("§7Motivo: §e").append(reason.getString()).append("\n");
        }
        message.append("\n");

        // Tips de salud aleatorios
        String[] healthTips = {
                "💧 Mantente hidratado bebiendo agua",
                "👀 Descansa la vista mirando a lo lejos",
                "🤸 Estira el cuerpo y muévete un poco",
                "🌿 Respira aire fresco si es posible",
                "😊 Relájate y disfruta tu descanso",
                "🧠 Dale descanso a tu mente",
                "🍎 Aprovecha para comer algo saludable",
                "💤 Un buen descanso mejora tu rendimiento",
                "🎵 Escucha música relajante",
                "📚 Lee algo interesante"
        };

        // Seleccionar tip aleatorio
        int tipIndex = (int) (System.currentTimeMillis() / 60000) % healthTips.length;
        message.append("§a💡 Consejo saludable:\n");
        message.append("§f").append(healthTips[tipIndex]).append("\n\n");

        // Mensaje final
        message.append("§7Gracias por jugar en §6✨ SpecialMon ✨\n");
        message.append("§d♡ §b¡Cuídate y regresa pronto! §d♡");

        return message.toString();
    }

    private static String getTimeString(int months, int days, int hours) {
        StringBuilder time = new StringBuilder();
        if (months > 0) time.append(months).append(" mes").append(months > 1 ? "es" : "").append(" ");
        if (days > 0) time.append(days).append(" día").append(days > 1 ? "s" : "").append(" ");
        if (hours > 0) time.append(hours).append(" hora").append(hours > 1 ? "s" : "");
        return time.toString().trim();
    }
}
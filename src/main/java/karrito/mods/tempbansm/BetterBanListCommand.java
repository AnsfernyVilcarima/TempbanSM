package karrito.mods.tempbansm;

import java.util.Collection;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.players.BanListEntry;
import net.minecraft.server.players.PlayerList;

public class BetterBanListCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("banlist")
                .requires(source -> source.hasPermission(3))
                .executes(ctx -> {
                    PlayerList playerlist = ctx.getSource().getServer().getPlayerList();
                    return showList(ctx.getSource(), Lists.newArrayList(Iterables.concat(
                            playerlist.getBans().getEntries(),
                            playerlist.getIpBans().getEntries()
                    )));
                })
                .then(Commands.literal("ips")
                        .executes(ctx -> showList(ctx.getSource(),
                                ctx.getSource().getServer().getPlayerList().getIpBans().getEntries())))
                .then(Commands.literal("players")
                        .executes(ctx -> showList(ctx.getSource(),
                                ctx.getSource().getServer().getPlayerList().getBans().getEntries()))));
    }

    private static int showList(CommandSourceStack source, Collection<? extends BanListEntry<?>> banListEntries) {
        if (banListEntries.isEmpty()) {
            source.sendSuccess(() -> Component.literal("§aNo hay jugadores baneados"), false);
        } else {
            source.sendSuccess(() -> Component.literal(
                    "§e📋 Lista de baneados (" + banListEntries.size() + " total):"
            ), false);

            for (BanListEntry<?> entry : banListEntries) {
                Component entryComponent = Component.literal(
                        "§f• " + entry.getDisplayName() + " §7por " + entry.getSource() + " - §6" + entry.getReason()
                );

                if (entry.getExpires() != null) {
                    entryComponent = entryComponent.copy().append(
                            Component.literal(" §8(" + entry.getCreated() + " - " + entry.getExpires() + ")")
                                    .withStyle(ChatFormatting.GRAY)
                    );
                }

                final Component finalComponent = entryComponent;
                source.sendSuccess(() -> finalComponent, false);
            }
        }

        return banListEntries.size();
    }
}
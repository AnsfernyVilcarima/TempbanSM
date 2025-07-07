package karrito.mods.tempbansm.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import net.minecraft.server.players.UserBanListEntry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerLoginPacketListenerImpl.class)
public class LoginMixin {

    @Shadow @Final private MinecraftServer server;
    @Shadow private GameProfile gameProfile;

    @Inject(method = "checkBan", at = @At("HEAD"), cancellable = true)
    private void tempbansm$customBanMessage(CallbackInfoReturnable<Component> cir) {
        if (this.gameProfile != null && this.server != null) {
            UserBanListEntry banEntry = this.server.getPlayerList().getBans().get(this.gameProfile);

            if (banEntry != null) {
                String customMessage = tempbansm$createCustomBanMessage(banEntry);
                cir.setReturnValue(Component.literal(customMessage));
            }
        }
    }

    @Unique
    private String tempbansm$createCustomBanMessage(UserBanListEntry banEntry) {
        StringBuilder message = new StringBuilder();
        message.append("§6⏰ Tiempo de juego completado\n");
        message.append("§fDebes esperar antes de reconectarte.\n");

        if (banEntry.getExpires() != null) {
            // Calcular tiempo restante
            long timeLeft = banEntry.getExpires().getTime() - System.currentTimeMillis();
            if (timeLeft > 0) {
                String timeLeftStr = tempbansm$getTimeLeftString(timeLeft);
                message.append("§fTiempo restante: §a").append(timeLeftStr).append("\n");
            } else {
                message.append("§fTiempo restante: §aExpirado\n");
            }
        } else {
            message.append("§fTipo: §cBaneo permanente\n");
        }

        message.append("§b¡Gracias por jugar responsablemente!");

        return message.toString();
    }

    @Unique
    private String tempbansm$getTimeLeftString(long timeLeftMs) {
        long seconds = timeLeftMs / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) {
            return days + " día" + (days > 1 ? "s" : "");
        } else if (hours > 0) {
            return hours + " hora" + (hours > 1 ? "s" : "");
        } else if (minutes > 0) {
            return minutes + " minuto" + (minutes > 1 ? "s" : "");
        } else {
            return "menos de 1 minuto";
        }
    }
}
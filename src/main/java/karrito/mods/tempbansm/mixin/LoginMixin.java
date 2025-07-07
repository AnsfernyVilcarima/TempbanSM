package karrito.mods.tempbansm.mixin;

import net.minecraft.network.chat.Component;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ServerLoginPacketListenerImpl.class)
public class LoginMixin {

    @ModifyArg(method = "disconnect", at = @At("HEAD"), index = 0)
    private Component tempbansm$modifyDisconnectMessage(Component originalMessage) {
        String messageText = originalMessage.getString();

        // Log para debug
        System.out.println("TempbanSM: Interceptando mensaje de disconnect: " + messageText);

        // Verificar si es un mensaje de baneo
        if (tempbansm$isBanMessage(messageText)) {
            System.out.println("TempbanSM: Mensaje de baneo detectado, aplicando personalización");
            return tempbansm$createCustomBanMessage(messageText);
        }

        return originalMessage;
    }

    @Unique
    private boolean tempbansm$isBanMessage(String message) {
        boolean isBan = message.contains("banned") ||
                message.contains("You are banned") ||
                message.contains("baneado") ||
                message.contains("Reason:") ||
                message.contains("Your ban will be removed") ||
                message.contains("until") ||
                message.contains("Baneo temporal") ||
                message.toLowerCase().contains("ban");

        System.out.println("TempbanSM: ¿Es mensaje de baneo? " + isBan + " - Mensaje: " + message);
        return isBan;
    }

    @Unique
    private Component tempbansm$createCustomBanMessage(String originalMessage) {
        StringBuilder customMessage = new StringBuilder();
        customMessage.append("§6⏰ Tiempo de juego completado\n");
        customMessage.append("§fDebes esperar antes de reconectarte.\n");

        // Intentar extraer información de tiempo del mensaje original
        String timeInfo = tempbansm$extractTimeFromMessage(originalMessage);
        if (!timeInfo.isEmpty()) {
            customMessage.append("§fTiempo restante: §a").append(timeInfo).append("\n");
        }

        customMessage.append("§b¡Gracias por jugar responsablemente!");

        System.out.println("TempbanSM: Mensaje personalizado creado: " + customMessage.toString());

        return Component.literal(customMessage.toString());
    }

    @Unique
    private String tempbansm$extractTimeFromMessage(String message) {
        try {
            // Buscar el patrón específico del mensaje que vemos
            if (message.contains("will be removed on")) {
                String[] parts = message.split("will be removed on");
                if (parts.length > 1) {
                    String datePart = parts[1].trim();
                    return datePart; // Devolver la fecha completa
                }
            }

            return "";

        } catch (Exception e) {
            System.out.println("TempbanSM: Error extrayendo tiempo: " + e.getMessage());
            return "";
        }
    }
}
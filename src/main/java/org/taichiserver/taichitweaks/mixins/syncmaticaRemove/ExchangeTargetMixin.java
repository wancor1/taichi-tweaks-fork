package org.taichiserver.taichitweaks.mixins.syncmaticaRemove;

import ch.endte.syncmatica.Context;
import ch.endte.syncmatica.communication.ExchangeTarget;
import ch.endte.syncmatica.network.PacketType;
import fi.dy.masa.malilib.gui.GuiBase;
import net.minecraft.network.PacketByteBuf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.taichiserver.taichitweaks.config.Configs;

@Mixin(ExchangeTarget.class)
public class ExchangeTargetMixin {
    @Inject(method = "sendPacket", at = @At("HEAD"), cancellable = true)
    private void onRemoveAction(PacketType type, PacketByteBuf byteBuf, Context context, CallbackInfo ci) {
        if (type != PacketType.REMOVE_SYNCMATIC) return;
        if (Configs.Generic.SYNCMATICA_REMOVE_DISABLED.getBooleanValue()) {
            ci.cancel();
        } else if (Configs.Generic.SYNCMATICA_REMOVE_NEED_SHIFT.getBooleanValue()) {
            if (!GuiBase.isShiftDown()) ci.cancel();
        }
    }
}
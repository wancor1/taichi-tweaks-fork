package org.taichiserver.taichitweaks.mixins.selectiveEntityRendering;

import fi.dy.masa.malilib.util.restrictions.UsageRestriction;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.taichiserver.taichitweaks.config.Configs;

@Mixin(EntityRenderer.class)
public class WorldRendererMixin {
    @Inject(method = "shouldRender", at = @At("RETURN"), cancellable = true)
    private <T extends Entity> void shouldRender(T entity, Frustum frustum, double x, double y, double z, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue()) return;
        if (!Configs.Generic.SELECTIVE_ENTITY_RENDERING.getBooleanValue()) return;

        UsageRestriction.ListType type = (UsageRestriction.ListType) Configs.Generic.SELECTIVE_ENTITY_RENDERING_LIST_TYPE.getOptionListValue();
        if (type == UsageRestriction.ListType.NONE) return;

        String targetEntity = entity.getType().toString().replace("entity.minecraft.", "");

        if (type == UsageRestriction.ListType.BLACKLIST) {
            if (Configs.Generic.SELECTIVE_ENTITY_RENDERING_BLACKLIST.getStrings().contains(targetEntity)) {
                cir.setReturnValue(false);
            }
        } else if (type == UsageRestriction.ListType.WHITELIST) {
            if (!Configs.Generic.SELECTIVE_ENTITY_RENDERING_WHITELIST.getStrings().contains(targetEntity)) {
                cir.setReturnValue(false);
            }
        }
    }
}
package org.taichiserver.taichitweaks.mixins.enhancedToolSwitch;

import fi.dy.masa.tweakeroo.util.InventoryUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.taichiserver.taichitweaks.config.Configs;

@Mixin(InventoryUtils.class)
public class InventoryUtilsMixin {
    @Inject(method = "isBetterTool", at = @At("RETURN"), cancellable = true)
    private static void isBetterTool(ItemStack testedStack, ItemStack previousTool, BlockState state, CallbackInfoReturnable<Boolean> cir) {
        if (!Configs.Generic.ENHANCED_TOOL_SWITCH.getBooleanValue()) return;

        if (state.getBlock().getDefaultState().isOf(Blocks.GLASS)
                || state.getBlock().getDefaultState().isOf(Blocks.GLASS_PANE)
                || state.getBlock() instanceof net.minecraft.block.StainedGlassBlock
                || state.getBlock() instanceof net.minecraft.block.StainedGlassPaneBlock) {

            ItemEnchantmentsComponent enchantments = testedStack.getOrDefault(DataComponentTypes.ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT);
            for (RegistryEntry<Enchantment> entry : enchantments.getEnchantments()) {
                if (entry.matchesKey(Enchantments.SILK_TOUCH)) {
                    cir.setReturnValue(true);
                    return;
                }
            }
            cir.setReturnValue(false);
        }
    }
}
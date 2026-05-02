package org.taichiserver.taichitweaks.features;

import fi.dy.masa.malilib.render.MaLiLibPipelines;
import fi.dy.masa.malilib.render.RenderUtils;
import fi.dy.masa.malilib.util.data.Color4f;
import fi.dy.masa.minihud.MiniHUD;
import fi.dy.masa.minihud.config.Configs;
import fi.dy.masa.minihud.config.RendererToggle;
import fi.dy.masa.minihud.mixin.block.IMixinBeaconBlockEntity;
import fi.dy.masa.minihud.renderer.BaseBlockRangeOverlay;
import fi.dy.masa.minihud.renderer.RenderObjectVbo;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.World;

import java.util.HashMap;

public class OverlayRendererLightningRodRange extends BaseBlockRangeOverlay<BeaconBlockEntity> {
    public static final OverlayRendererLightningRodRange INSTANCE = new OverlayRendererLightningRodRange();
    private final HashMap<BlockPos, Integer> positions;

    public OverlayRendererLightningRodRange() {
        super(RendererToggle.OVERLAY_BEACON_RANGE, BlockEntityType.BEACON, BeaconBlockEntity.class);
        this.useCulling = false;
        this.positions = new HashMap<>();
        this.needsNbt = true;
        this.updateDistance = 16;
    }

    @Override
    public String getName() {
        return "TaichiLightningRodRange";
    }

    @Override
    protected void updateBlockRange(World world, BlockPos pos, BeaconBlockEntity be, Vec3d cameraPos, MinecraftClient mc, Profiler profiler) {
        IMixinBeaconBlockEntity beaconBE = (IMixinBeaconBlockEntity) be;
        int level = beaconBE.minihud_getLevel();
        if (level >= 1 && level <= 4) {
            this.positions.put(pos, level);
        } else {
            this.positions.remove(pos);
        }
    }

    @Override
    protected void renderBlockRange(World world, Vec3d cameraPos, MinecraftClient mc, Profiler profiler) {
        this.renderThrough = false;
        if (!this.positions.isEmpty()) {
            this.allocateBuffers(true);
            this.renderQuads(world, cameraPos, mc, profiler);
            this.renderOutlines(world, cameraPos, mc, profiler);
        } else {
            this.clearBuffers();
        }
    }

    @Override
    protected void expireBlockRange(BlockPos pos) {
        this.positions.remove(pos);
    }

    @Override
    protected void resetBlockRange() {
        this.positions.clear();
    }

    private void renderQuads(World world, Vec3d cameraPos, MinecraftClient mc, Profiler profiler) {
        if (mc.world == null || mc.player == null) return;
        double camX = cameraPos.x, camY = cameraPos.y, camZ = cameraPos.z;
        profiler.push("taichi_lightning_rod_quads");
        RenderObjectVbo ctx = (RenderObjectVbo) this.renderObjects.getFirst();
        BufferBuilder builder = ctx.start(() -> "taichi:lightning_rod/quads", MaLiLibPipelines.MINIHUD_SHAPE_OFFSET_NO_CULL);
        this.positions.forEach((pos, level) -> {
            double x = pos.getX() - camX, y = pos.getY() - camY, z = pos.getZ() - camZ;
            Color4f color = getColorForLevel(level);
            int range = level * 10 + 10;
            RenderUtils.drawBoxAllSidesBatchedQuads(
                    (float)(x - range), (float)(y - range), (float)(z - range),
                    (float)(x + range + 1), (float) this.getTopYOverTerrain(world, pos, range), (float)(z + range + 1),
                    color, builder);
        });
        try {
            BuiltBuffer meshData = builder.endNullable();
            if (meshData != null) { ctx.upload(meshData, false); meshData.close(); }
        } catch (Exception e) {
            MiniHUD.LOGGER.error("TaichiLightningRodRange#renderQuads(): {}", e.getMessage());
        }
        profiler.pop();
    }

    private void renderOutlines(World world, Vec3d cameraPos, MinecraftClient mc, Profiler profiler) {
        if (mc.world == null || mc.player == null) return;
        double camX = cameraPos.x, camY = cameraPos.y, camZ = cameraPos.z;
        profiler.push("taichi_lightning_rod_outlines");
        RenderObjectVbo ctx = (RenderObjectVbo) this.renderObjects.get(1);
        BufferBuilder builder = ctx.start(() -> "taichi:lightning_rod/outlines", MaLiLibPipelines.DEBUG_LINES_MASA_SIMPLE_LEQUAL_DEPTH);
        this.positions.forEach((pos, level) -> {
            double x = pos.getX() - camX, y = pos.getY() - camY, z = pos.getZ() - camZ;
            Color4f color = Color4f.fromColor(getColorForLevel(level), 255.0F);
            int range = level * 10 + 10;
            RenderUtils.drawBoxAllEdgesBatchedLines(
                    (float)(x - range), (float)(y - range), (float)(z - range),
                    (float)(x + range + 1), (float) this.getTopYOverTerrain(world, pos, range), (float)(z + range + 1),
                    color, this.glLineWidth, builder);
        });
        try {
            BuiltBuffer meshData = builder.endNullable();
            if (meshData != null) { ctx.upload(meshData, false); meshData.close(); }
        } catch (Exception e) {
            MiniHUD.LOGGER.error("TaichiLightningRodRange#renderOutlines(): {}", e.getMessage());
        }
        profiler.pop();
    }

    public static Color4f getColorForLevel(int level) {
        return switch (level) {
            case 1 -> Configs.Colors.BEACON_RANGE_LVL1_OVERLAY_COLOR.getColor();
            case 2 -> Configs.Colors.BEACON_RANGE_LVL2_OVERLAY_COLOR.getColor();
            case 3 -> Configs.Colors.BEACON_RANGE_LVL3_OVERLAY_COLOR.getColor();
            default -> Configs.Colors.BEACON_RANGE_LVL4_OVERLAY_COLOR.getColor();
        };
    }
}
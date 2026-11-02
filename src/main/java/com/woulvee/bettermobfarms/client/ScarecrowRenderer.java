package com.woulvee.bettermobfarms.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.woulvee.bettermobfarms.block.ScarecrowBlock;
import com.woulvee.bettermobfarms.block.entity.ScarecrowBlockEntity;

import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.skull.SkullModel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.PlayerSkinRenderCache;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

// Draws the player head of a scarecrow, tilted like the pumpkin in models/block/scarecrow_head.json
public class ScarecrowRenderer implements BlockEntityRenderer<ScarecrowBlockEntity, ScarecrowRenderer.State> {
    private static final float HEAD_TILT = -7.5f;
    // The model rescales the tilted head so that it keeps its height
    private static final float HEAD_RESCALE = 1 / (float) Math.cos(Math.toRadians(HEAD_TILT));

    private final SkullModel model;
    private final PlayerSkinRenderCache playerSkinRenderCache;

    public ScarecrowRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new SkullModel(context.entityModelSet().bakeLayer(ModelLayers.PLAYER_HEAD));
        this.playerSkinRenderCache = context.playerSkinRenderCache();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(ScarecrowBlockEntity scarecrow, State state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(scarecrow, state, partialTicks, cameraPosition, breakProgress);
        state.facing = scarecrow.getBlockState().getValue(ScarecrowBlock.FACING);
        state.renderType = scarecrow.hasHead() ? resolveRenderType(scarecrow.getHead().get(DataComponents.PROFILE)) : null;
        // The head is in the upper half
        if (scarecrow.getLevel() != null) {
            state.lightCoords = LevelRenderer.getLightCoords(scarecrow.getLevel(), scarecrow.getBlockPos().above());
        }
    }

    private RenderType resolveRenderType(@Nullable ResolvableProfile profile) {
        return profile != null ? playerSkinRenderCache.getOrDefault(profile).renderType() : SkullBlockRenderer.getSkullRenderType(SkullBlock.Types.PLAYER, null);
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        if (state.renderType == null) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(0.5f, 0, 0.5f);
        poseStack.mulPose(Axis.YP.rotationDegrees(180 - state.facing.toYRot()));
        poseStack.translate(-0.5f, 0, -0.5f);
        // Same pivot as the head in the block model
        poseStack.translate(7 / 16f, 23 / 16f, 7 / 16f);
        poseStack.mulPose(Axis.XP.rotationDegrees(HEAD_TILT));
        poseStack.scale(1, HEAD_RESCALE, HEAD_RESCALE);
        poseStack.translate(1 / 16f, 0, 1 / 16f);
        poseStack.scale(-1, -1, 1);
        SkullBlockRenderer.submitSkull(0, poseStack, submitNodeCollector, state.lightCoords, model, state.renderType, 0, state.breakProgress);
        poseStack.popPose();
    }

    // The head is above the block the scarecrow stands in
    @Override
    public AABB getRenderBoundingBox(ScarecrowBlockEntity scarecrow) {
        return new AABB(scarecrow.getBlockPos()).expandTowards(0, 1, 0);
    }

    public static class State extends BlockEntityRenderState {
        Direction facing = Direction.NORTH;
        @Nullable RenderType renderType;
    }
}

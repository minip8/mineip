package io.github.minip8.mineip.util;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.*;
import org.joml.Matrix4f;
import net.minecraft.client.renderer.rendertype.RenderTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ProjectilePredictionUtil {
    public record ProjectilePhysics(double gravity, double drag,
                                    double waterDrag,
                                    double velocityScale) {
        public static Optional<ProjectilePhysics> create(Item item,
                                                         int useTicks) {
            if (item == null) {
                return Optional.empty();
            }
            boolean validProj = false;
            double gravity = 0.0;
            double drag = 0.0;
            double waterDrag = 0.0;
            double velocityScale = 0.0;

            if (item == Items.BOW) {
                gravity = 0.05;
                drag = 0.99;
                waterDrag = 0.70;
                velocityScale = 3.0 * BowItem.getPowerForTime(useTicks);
                validProj = true;
            }
            if (!validProj) {
                return Optional.empty();
            }
            return Optional.of(new ProjectilePhysics(gravity, drag, waterDrag,
                    velocityScale));
        }
    }

    public record ProjectileStep(Vec3 pos, Vec3 velocity) {
    }

    public record ProjectileFinalState(
            Vec3 pos,
            HitResult hit,
            List<Vec3> path
    ) {
    }

    public static ProjectileStep calcNextStep(ProjectileStep projStep,
                                              ProjectilePhysics projPhysics,
                                              boolean waterDrag) {
        Vec3 nextPos = projStep.pos().add(projStep.velocity());
        Vec3 nextVelocity = projStep.velocity()
                .scale(waterDrag ? projPhysics.waterDrag() : projPhysics.drag())
                .subtract(0.0,
                        projPhysics.gravity(), 0.0);


        return new ProjectileStep(nextPos, nextVelocity);
    }

    static final int SIMULATION_TICKS = 200;

    public static Optional<ProjectileFinalState> calcFinalState(
            Player player
    ) {
        Optional<ProjectilePhysics> projPhysicsOpt = ProjectilePhysics.create(
                player.getMainHandItem().getItem(),
                player.getTicksUsingItem());
        if (projPhysicsOpt.isEmpty()) {
            return Optional.empty();
        }
        ProjectilePhysics projPhysics = projPhysicsOpt.get();
        List<Vec3> path = new ArrayList<>();
        Vec3 initPos = player.getEyePosition(1.0f);
        Vec3 initVelocity = player.getViewVector(1.0f)
                .scale(projPhysics.velocityScale())
                .add(player.getDeltaMovement());

        ProjectileStep currStep = new ProjectileStep(initPos, initVelocity);
        HitResult hit = null;

        for (int i = 0; i < SIMULATION_TICKS; i++) {
            BlockPos blockPos = BlockPos.containing(currStep.pos());
            boolean inAir = player.level().getFluidState(blockPos).isEmpty();

            ProjectileStep nextStep = calcNextStep(currStep, projPhysics,
                    !inAir && i != 0);

            ClipContext clip = new ClipContext(
                    currStep.pos(),
                    nextStep.pos(),
                    ClipContext.Block.COLLIDER,
                    ClipContext.Fluid.NONE,
                    player
            );

            BlockHitResult blockHit = player.level().clip(clip);

            Vec3 entityCheckBoundingPos = blockHit.getType() ==
                    HitResult.Type.MISS ? nextStep.pos() :
                    blockHit.getLocation();

            AABB searchBox = new AABB(currStep.pos(),
                    entityCheckBoundingPos).inflate(0f);

            EntityHitResult entityHit =
                    net.minecraft.world.entity.projectile.ProjectileUtil
                            .getEntityHitResult(
                                    player.level(),
                                    player,
                                    currStep.pos(),
                                    entityCheckBoundingPos,
                                    searchBox,
                                    (entity) -> !entity.isSpectator() &&
                                            entity.isPickable() &&
                                            entity != player,
                                    0.3f
                            );

            hit = Objects.requireNonNullElse(entityHit, blockHit);

            if (hit.getType() != HitResult.Type.MISS) {
//                Vec3 impactPos = (hit instanceof EntityHitResult) ?
//                        entityHit.getEntity()
//                                .getBoundingBox()
//                                .getCenter() : blockHit.getBlockPos()
//                        .getCenter();
                Vec3 impactPos = hit.getLocation();
                path.add(impactPos);
                return Optional.of(
                        new ProjectileFinalState(impactPos, hit, path));
            }

            currStep = nextStep;

            path.add(currStep.pos());
        }

        return Optional.of(new ProjectileFinalState(currStep.pos(), hit,
                path));
    }

    public static void renderTrajectory(PoseStack poseStack,
                                        MultiBufferSource bufferSource,
                                        List<Vec3> path, Vec3 cameraPos) {
        // We use a Line buffer. "Lines" is a standard RenderType in Mojang
        // Mappings.
        VertexConsumer builder = bufferSource.getBuffer(RenderTypes.lines());
        Matrix4f matrix = poseStack.last().pose();

        for (int i = 0; i < path.size() - 1; i++) {
            Vec3 start = path.get(i);
            Vec3 end = path.get(i + 1);

            // Draw the segment (start to end)
            // We subtract cameraPos to convert World Coords to Relative Coords
            builder.addVertex(matrix, (float) (start.x - cameraPos.x),
                            (float) (start.y - cameraPos.y),
                            (float) (start.z - cameraPos.z))
                    .setColor(0.0f, 1.0f, 0.0f, 1.0f) // Green
                    .setNormal(0, 1, 0);

            builder.addVertex(matrix, (float) (end.x - cameraPos.x),
                            (float) (end.y - cameraPos.y),
                            (float) (end.z - cameraPos.z))
                    .setColor(0.0f, 1.0f, 0.0f, 1.0f) // Green
                    .setNormal(0, 1, 0);
        }
    }

    public static boolean render(WorldRenderContext context) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return false;

        RenderUtil renderUtil = RenderUtil.getInstance();
        if (renderUtil == null) return false;

        Optional<ProjectilePredictionUtil.ProjectileFinalState> projFinalStateOpt =
                ProjectilePredictionUtil.calcFinalState(player);
        if (projFinalStateOpt.isEmpty()) return false;

        ProjectilePredictionUtil.ProjectileFinalState projFinalState =
                projFinalStateOpt.get();

        HitResult hit = projFinalState.hit();
        if (hit == null) {
            return false;
        }

        Vec3 impactPos = projFinalState.pos();
        PoseStack matrices = context.matrices();
        Vec3 camera = context.worldState().cameraRenderState.pos;

        matrices.pushPose();
        matrices.translate(impactPos.x() - camera.x, impactPos.y() - camera.y,
                impactPos.z() - camera.z);

        float red, green, blue, alpha;
        switch (hit.getType()) {
            case HitResult.Type.BLOCK -> {
                red = 0.0f;
                green = 255.0f;
                blue = 0.0f;
                alpha = 0.5f;
            }

            case HitResult.Type.ENTITY -> {
                red = 255.0f;
                green = 0.0f;
                blue = 0.0f;
                alpha = 0.5f;
            }
            default -> {
                red = 128.0f;
                green = 128.0f;
                blue = 128.0f;
                alpha = 0.5f;
            }
        }
        red /= 255.0f;
        green /= 255.0f;
        blue /= 255.0f;
        float s = 0.5f;
        renderUtil.renderFilledBox(matrices.last().pose(), -s, -s, -s, s, s, s,
                red,
                green, blue, alpha);
        matrices.popPose();
        return true;
    }
}
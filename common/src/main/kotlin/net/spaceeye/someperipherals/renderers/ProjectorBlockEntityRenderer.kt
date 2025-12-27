package net.spaceeye.someperipherals.renderers

import com.mojang.blaze3d.vertex.PoseStack

import net.minecraft.client.renderer.LevelRenderer
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.spaceeye.someperipherals.LOG

import net.spaceeye.someperipherals.SomePeripheralsRenderTypes
import net.spaceeye.someperipherals.blockentities.ProjectorBlockEntity
import net.spaceeye.someperipherals.blockentities.ProjectorBlockEntity.Companion.SCREEN_RESOLUTION
import net.spaceeye.someperipherals.stuff.utils.toJOML
import org.joml.Matrix4f

import org.joml.Vector3i
import org.joml.Vector3f

import kotlin.math.floor

class ProjectorBlockEntityRenderer(ctx: BlockEntityRendererProvider.Context) : BlockEntityRenderer<ProjectorBlockEntity> {
    override fun render(
        projector: ProjectorBlockEntity,
        partialTicks: Float,
        transform: PoseStack,
        bufferSource: MultiBufferSource,
        lightmapCoord: Int,
        overlayLight: Int
    ) {
        //if (projector.otherProjector == null) return; // Don't render if another projector has not been attached
        if (!projector.doIRender) {return}; // We aren't the rendering instance, so don't render

        transform.pushPose();

        val vc = bufferSource.getBuffer(SomePeripheralsRenderTypes.PROJECTOR_PIXEL);

        val screenPos = projector.screenPos;//projector.blockPos.offset(projector.blockState.getValue(BlockStateProperties.FACING).normal).toJOML()

        val resolution = SCREEN_RESOLUTION; // pixels per block, at 16 it would be 16x16x16 pixels for one block
        val size = 1.0f / resolution;

        val matrix = transform.last().pose();
        //    matrix.translation(Vector3f(screenPos));

        // Iterate over all the voxels that need to be rendered
        for (voxelEntry in projector.voxels) {
            val voxel = voxelEntry.value;  // Do not edit these values, but don't copy them to save on memory and CPU time
            val position = voxelEntry.key;

            // TODO: Implement frustum culling
            if ( // Check if the voxel is visible
                projector.voxels[Vector3i(position.x, position.y, position.z + 1)] != null &&
                projector.voxels[Vector3i(position.x, position.y, position.z - 1)] != null &&
                projector.voxels[Vector3i(position.x - 1, position.y, position.z)] != null &&
                projector.voxels[Vector3i(position.x + 1, position.y, position.z)] != null &&
                projector.voxels[Vector3i(position.x, position.y + 1, position.z)] != null &&
                projector.voxels[Vector3i(position.x, position.y - 1, position.z)] != null
            ) continue;

            val r = voxel.red.toFloat() / 255f;
            val g = voxel.green.toFloat() / 255f;
            val b = voxel.blue.toFloat() / 255f;
            val a = voxel.alpha.toFloat() / 255f;

            var x1 = 0.0f;
            var x2 = size;
            var y1 = 0.0f;
            var y2 = size;
            var z1 = 0.0f;
            var z2 = size;

            // Translate
            // We divide by resolution, because the pixel position is in whole numbers
            x1 += position.x.toFloat() / resolution.toFloat();
            x2 += position.x.toFloat() / resolution.toFloat();

            y1 += position.y.toFloat() / resolution.toFloat();
            y2 += position.y.toFloat() / resolution.toFloat();

            z1 += position.z.toFloat() / resolution.toFloat();
            z2 += position.z.toFloat() / resolution.toFloat();

            val light = LevelRenderer.getLightColor(
                projector.level, BlockPos(
                    screenPos.x + floor(x1).toInt(),
                    screenPos.y + floor(y1).toInt(),
                    screenPos.z + floor(z1).toInt()
                )
            );

            // Front face (+Z)
            if (projector.voxels[Vector3i(position.x, position.y, position.z + 1)] == null) {
                vc.vertex(matrix, x1, y1, z2).color(r, g, b, a).uv2(light).endVertex()
                vc.vertex(matrix, x2, y1, z2).color(r, g, b, a).uv2(light).endVertex()
                vc.vertex(matrix, x2, y2, z2).color(r, g, b, a).uv2(light).endVertex()
                vc.vertex(matrix, x1, y2, z2).color(r, g, b, a).uv2(light).endVertex()
            }

            // Back face (-Z)
            if (projector.voxels[Vector3i(position.x, position.y, position.z - 1)] == null) {
                vc.vertex(matrix, x1, y1, z1).color(r, g, b, a).uv2(light).endVertex()
                vc.vertex(matrix, x1, y2, z1).color(r, g, b, a).uv2(light).endVertex()
                vc.vertex(matrix, x2, y2, z1).color(r, g, b, a).uv2(light).endVertex()
                vc.vertex(matrix, x2, y1, z1).color(r, g, b, a).uv2(light).endVertex()
            }

            // Left face (-X)
            if (projector.voxels[Vector3i(position.x - 1, position.y, position.z)] == null) {
                vc.vertex(matrix, x1, y1, z1).color(r, g, b, a).uv2(light).endVertex()
                vc.vertex(matrix, x1, y1, z2).color(r, g, b, a).uv2(light).endVertex()
                vc.vertex(matrix, x1, y2, z2).color(r, g, b, a).uv2(light).endVertex()
                vc.vertex(matrix, x1, y2, z1).color(r, g, b, a).uv2(light).endVertex()
            }

            // Right face (+X)
            if (projector.voxels[Vector3i(position.x + 1, position.y, position.z)] == null) {
                vc.vertex(matrix, x2, y1, z1).color(r, g, b, a).uv2(light).endVertex()
                vc.vertex(matrix, x2, y2, z1).color(r, g, b, a).uv2(light).endVertex()
                vc.vertex(matrix, x2, y2, z2).color(r, g, b, a).uv2(light).endVertex()
                vc.vertex(matrix, x2, y1, z2).color(r, g, b, a).uv2(light).endVertex()
            }

            // Bottom face (-Y)
            if (projector.voxels[Vector3i(position.x, position.y - 1, position.z)] == null) {
                vc.vertex(matrix, x1, y1, z1).color(r, g, b, a).uv2(light).endVertex()
                vc.vertex(matrix, x2, y1, z1).color(r, g, b, a).uv2(light).endVertex()
                vc.vertex(matrix, x2, y1, z2).color(r, g, b, a).uv2(light).endVertex()
                vc.vertex(matrix, x1, y1, z2).color(r, g, b, a).uv2(light).endVertex()
            }

            // Top face (+Y)
            if (projector.voxels[Vector3i(position.x, position.y + 1, position.z)] == null) {
                vc.vertex(matrix, x1, y2, z1).color(r, g, b, a).uv2(light).endVertex()
                vc.vertex(matrix, x1, y2, z2).color(r, g, b, a).uv2(light).endVertex()
                vc.vertex(matrix, x2, y2, z2).color(r, g, b, a).uv2(light).endVertex()
                vc.vertex(matrix, x2, y2, z1).color(r, g, b, a).uv2(light).endVertex()
            }
        }

        transform.popPose();
    }
}
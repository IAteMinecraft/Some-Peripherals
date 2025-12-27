package net.spaceeye.someperipherals.blocks

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.BaseEntityBlock
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.RenderShape
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityTicker
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.block.state.properties.BooleanProperty

import net.spaceeye.someperipherals.blockentities.CommonBlockEntities
import net.spaceeye.someperipherals.blockentities.ProjectorBlockEntity

val PERIPHERAL_ON: BooleanProperty = BooleanProperty.create("peripheral_on")

class ProjectorBlock(properties: Properties): BaseEntityBlock(properties) {
    init {
        registerDefaultState(this.stateDefinition.any()
            .setValue(BlockStateProperties.FACING, Direction.SOUTH)
            .setValue(PERIPHERAL_ON, false)
        )
    }

    override fun newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity {
        return ProjectorBlockEntity(pos, state)
    }

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        builder
            .add(BlockStateProperties.FACING)
            .add(PERIPHERAL_ON)
    }

    override fun getStateForPlacement(ctx: BlockPlaceContext): BlockState? {
        val direction = if (ctx.player?.isCrouching == true) { ctx.nearestLookingDirection } else { ctx.nearestLookingDirection.opposite }
        return defaultBlockState().setValue(BlockStateProperties.FACING, direction)
    }

    override fun getRenderShape(blockState: BlockState): RenderShape {
        return RenderShape.MODEL
    }

    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in Java")
    override fun onRemove(state: BlockState, level: Level, pos: BlockPos, newState: BlockState, isMoving: Boolean) {
        if (state.block === newState.block) return;

        val tile: BlockEntity? = level.getBlockEntity(pos);
        if (tile is ProjectorBlockEntity) tile.destroy();
        super.onRemove(state, level, pos, newState, isMoving);
    }

    override fun <T : BlockEntity?> getTicker(
        level: Level,
        state: BlockState,
        type: BlockEntityType<T>
    ): BlockEntityTicker<T>? {
        if (level.isClientSide || type != CommonBlockEntities.PROJECTOR.get()) return null
        return BlockEntityTicker { _, _, _, be -> (be as ProjectorBlockEntity).tick() }
    }
}
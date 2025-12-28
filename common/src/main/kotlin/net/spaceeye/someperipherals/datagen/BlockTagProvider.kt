package net.spaceeye.someperipherals.datagen

import net.minecraft.core.HolderLookup
import net.minecraft.core.registries.Registries
import net.minecraft.data.PackOutput
import net.minecraft.data.tags.TagsProvider
import net.minecraft.tags.BlockTags
import net.minecraft.world.level.block.Block

import java.util.concurrent.CompletableFuture

import net.spaceeye.someperipherals.blocks.SomePeripheralsCommonBlocks.BALLISTIC_ACCELERATOR
import net.spaceeye.someperipherals.blocks.SomePeripheralsCommonBlocks.RAYCASTER
import net.spaceeye.someperipherals.blocks.SomePeripheralsCommonBlocks.PROJECTOR
import net.spaceeye.someperipherals.blocks.SomePeripheralsCommonBlocks.GOGGLE_LINK_PORT
import net.spaceeye.someperipherals.blocks.SomePeripheralsCommonBlocks.RADAR
import net.spaceeye.someperipherals.blocks.SomePeripheralsCommonBlocks.DIGITIZER
import net.spaceeye.someperipherals.blocks.SomePeripheralsCommonBlocks.WORLD_SCANNER

class BlockTagProvider(
    output: PackOutput,
    lookupProvider: CompletableFuture<HolderLookup.Provider>
): TagsProvider<Block>(output, Registries.BLOCK, lookupProvider) {
    override fun addTags(provider: HolderLookup.Provider) {
        // Make mineable with a pickaxe
        getOrCreateRawBuilder(BlockTags.MINEABLE_WITH_PICKAXE)
            .addElement(BALLISTIC_ACCELERATOR.id)
            .addElement(RAYCASTER.id)
            .addElement(PROJECTOR.id)
            .addElement(GOGGLE_LINK_PORT.id)
            .addElement(RADAR.id)
            .addElement(DIGITIZER.id)
            .addElement(WORLD_SCANNER.id)

        // Require Iron or greater to drop
        getOrCreateRawBuilder(BlockTags.NEEDS_IRON_TOOL)
            .addElement(BALLISTIC_ACCELERATOR.id)
            .addElement(RAYCASTER.id)
            .addElement(PROJECTOR.id)
            .addElement(GOGGLE_LINK_PORT.id)
            .addElement(RADAR.id)
            .addElement(DIGITIZER.id)
            .addElement(WORLD_SCANNER.id)
    }
}
package net.spaceeye.someperipherals.datagen

import net.minecraft.data.PackOutput
import net.minecraft.data.loot.BlockLootSubProvider
import net.minecraft.data.loot.LootTableProvider
import net.minecraft.data.loot.LootTableSubProvider
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.flag.FeatureFlags
import net.minecraft.world.level.storage.loot.BuiltInLootTables
import net.minecraft.world.level.storage.loot.LootTable
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets
import net.spaceeye.someperipherals.blocks.SomePeripheralsCommonBlocks
import net.spaceeye.someperipherals.blocks.SomePeripheralsCommonBlocks.BALLISTIC_ACCELERATOR
import net.spaceeye.someperipherals.blocks.SomePeripheralsCommonBlocks.DIGITIZER
import net.spaceeye.someperipherals.blocks.SomePeripheralsCommonBlocks.GOGGLE_LINK_PORT
import net.spaceeye.someperipherals.blocks.SomePeripheralsCommonBlocks.PROJECTOR
import net.spaceeye.someperipherals.blocks.SomePeripheralsCommonBlocks.RADAR
import net.spaceeye.someperipherals.blocks.SomePeripheralsCommonBlocks.RAYCASTER
import net.spaceeye.someperipherals.blocks.SomePeripheralsCommonBlocks.WORLD_SCANNER
import java.util.*
import java.util.function.BiConsumer

class LootTableProvider(output: PackOutput): LootTableProvider(output, setOf(), listOf(
    SubProviderEntry({ // This might be some sketchy code
        LootTableSubProvider { add: BiConsumer<ResourceLocation, LootTable.Builder> ->
            BlockLoot().generate(add)
        }
    }, LootContextParamSets.BLOCK))) {

    class BlockLoot: BlockLootSubProvider(setOf(), FeatureFlags.REGISTRY.allFlags()) {
        override fun generate() {
            dropSelf(BALLISTIC_ACCELERATOR.get())
            dropSelf(DIGITIZER.get())
            dropSelf(GOGGLE_LINK_PORT.get())
            dropSelf(PROJECTOR.get())
            dropSelf(RADAR.get())
            dropSelf(RAYCASTER.get())
            dropSelf(WORLD_SCANNER.get())
        }

        override fun generate(biConsumer: BiConsumer<ResourceLocation, LootTable.Builder>) {
            this.generate()
            val set: MutableSet<ResourceLocation> = HashSet<ResourceLocation>()

            for (supplier in SomePeripheralsCommonBlocks.BLOCKS) {
                val block = supplier.get();
                if (block.isEnabled(this.enabledFeatures)) {
                    val resourceLocation = block.lootTable
                    if (resourceLocation !== BuiltInLootTables.EMPTY && set.add(resourceLocation)) {
                        val builder = this.map.remove(resourceLocation) as LootTable.Builder

                        biConsumer.accept(resourceLocation, builder)
                    }
                }
            }

            check(this.map.isEmpty()) { "Created block loot tables for non-blocks: " + this.map.keys }
        }
    }
}
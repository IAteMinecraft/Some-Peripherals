package net.spaceeye.someperipherals.fabric

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator

import net.spaceeye.someperipherals.datagen.BlockTagProvider
import net.spaceeye.someperipherals.datagen.LootTableProvider
import net.spaceeye.someperipherals.datagen.RecipeProvider

class SomePeripheralsDataGen: DataGeneratorEntrypoint {
    override fun onInitializeDataGenerator(fabricDataGenerator: FabricDataGenerator) {
        val pack = fabricDataGenerator.createPack();

        pack.addProvider(::RecipeProvider);
        pack.addProvider(::LootTableProvider);
        pack.addProvider(::BlockTagProvider);
    }
}
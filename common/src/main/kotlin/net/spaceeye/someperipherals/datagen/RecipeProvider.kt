package net.spaceeye.someperipherals.datagen

import net.minecraft.advancements.critereon.InventoryChangeTrigger
import net.minecraft.data.PackOutput
import net.minecraft.data.recipes.FinishedRecipe
import net.minecraft.data.recipes.RecipeCategory
import net.minecraft.data.recipes.RecipeProvider
import net.minecraft.data.recipes.ShapedRecipeBuilder
import net.minecraft.world.item.Items
import net.minecraft.world.level.ItemLike

import java.util.function.Consumer

import net.spaceeye.someperipherals.SomePeripheralsItems.RANGE_GOGGLES
import net.spaceeye.someperipherals.SomePeripheralsItems.STATUS_GOGGLES
import net.spaceeye.someperipherals.blocks.SomePeripheralsCommonBlocks.BALLISTIC_ACCELERATOR
import net.spaceeye.someperipherals.blocks.SomePeripheralsCommonBlocks.RAYCASTER
import net.spaceeye.someperipherals.blocks.SomePeripheralsCommonBlocks.PROJECTOR
import net.spaceeye.someperipherals.blocks.SomePeripheralsCommonBlocks.GOGGLE_LINK_PORT
import net.spaceeye.someperipherals.blocks.SomePeripheralsCommonBlocks.RADAR
import net.spaceeye.someperipherals.blocks.SomePeripheralsCommonBlocks.DIGITIZER
import net.spaceeye.someperipherals.blocks.SomePeripheralsCommonBlocks.WORLD_SCANNER

class RecipeProvider(output: PackOutput): RecipeProvider(output) {
    override fun buildRecipes(writer: Consumer<FinishedRecipe>) {
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, RANGE_GOGGLES.get().asItem())
            .pattern("ISI")
            .pattern("GRG")
            .pattern(" I ")
            .define('I', Items.IRON_INGOT)
            .define('S', Items.STRING)
            .define('R', Items.REDSTONE)
            .define('G', Items.GLASS_PANE)
            .unlockedBy("has_iron", inventoryChange(Items.IRON_INGOT))
            .save(writer)
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, STATUS_GOGGLES.get().asItem())
            .pattern("ISI")
            .pattern("GRG")
            .pattern(" I ")
            .define('I', Items.IRON_INGOT)
            .define('S', Items.STRING)
            .define('R', Items.GLOWSTONE)
            .define('G', Items.GLASS_PANE)
            .unlockedBy("has_iron", inventoryChange(Items.IRON_INGOT))
            .save(writer)
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, BALLISTIC_ACCELERATOR.get().asItem())
            .pattern("III")
            .pattern("IWI")
            .pattern("III")
            .define('I', Items.IRON_INGOT)
            .define('W', Items.WATER_BUCKET)
            .unlockedBy("has_iron", inventoryChange(Items.IRON_INGOT))
            .save(writer)
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, RAYCASTER.get().asItem())
            .pattern("III")
            .pattern("IRG")
            .pattern("III")
            .define('I', Items.IRON_INGOT)
            .define('R', Items.REDSTONE)
            .define('G', Items.GLASS)
            .unlockedBy("has_iron", inventoryChange(Items.IRON_INGOT))
            .save(writer)
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, PROJECTOR.get().asItem())
            .pattern("III")
            .pattern("IRG")
            .pattern("III")
            .define('I', Items.IRON_INGOT)
            .define('R', Items.GLOWSTONE)
            .define('G', Items.GLASS)
            .unlockedBy("has_iron", inventoryChange(Items.IRON_INGOT))
            .save(writer)
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, GOGGLE_LINK_PORT.get().asItem())
            .pattern("IOI")
            .pattern("OCO")
            .pattern("IOI")
            .define('I', Items.IRON_INGOT)
            .define('O', Items.COPPER_INGOT)
            .define('C', Items.COMPASS)
            .unlockedBy("has_iron", inventoryChange(Items.IRON_INGOT))
            .save(writer)
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, RADAR.get().asItem())
            .pattern("III")
            .pattern("ICI")
            .pattern("III")
            .define('I', Items.IRON_INGOT)
            .define('C', Items.COMPASS)
            .unlockedBy("has_iron", inventoryChange(Items.IRON_INGOT))
            .save(writer)
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, DIGITIZER.get().asItem())
            .pattern("IRI")
            .pattern("LCG")
            .pattern("IRI")
            .define('I', Items.IRON_INGOT)
            .define('R', Items.REDSTONE)
            .define('L', Items.REDSTONE_LAMP)
            .define('C', Items.CHEST)
            .define('G', Items.GLASS_PANE)
            .unlockedBy("has_iron", inventoryChange(Items.IRON_INGOT))
            .save(writer)
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, WORLD_SCANNER.get().asItem())
            .pattern("IRI")
            .pattern("RCR")
            .pattern("IRI")
            .define('I', Items.IRON_INGOT)
            .define('R', Items.LIGHTNING_ROD)
            .define('C', Items.COMPASS)
            .unlockedBy("has_iron", inventoryChange(Items.IRON_INGOT))
            .save(writer)
    }

    private fun inventoryChange(vararg stack: ItemLike?): InventoryChangeTrigger.TriggerInstance {
        return InventoryChangeTrigger.TriggerInstance.hasItems(*stack)
    }
}
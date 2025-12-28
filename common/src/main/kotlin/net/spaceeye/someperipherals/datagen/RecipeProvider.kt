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

import net.spaceeye.someperipherals.blocks.SomePeripheralsCommonBlocks.RADAR

class RecipeProvider(output: PackOutput): RecipeProvider(output) {
    override fun buildRecipes(writer: Consumer<FinishedRecipe>) {
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, RADAR.get().asItem())
            .pattern("III")
            .pattern("IRG")
            .pattern("III")
            .define('I', Items.IRON_INGOT)
            .define('R', Items.REDSTONE)
            .define('G', Items.GLASS)
            .unlockedBy("has_iron", inventoryChange(Items.IRON_INGOT))
            .save(writer)
    }

    private fun inventoryChange(vararg stack: ItemLike?): InventoryChangeTrigger.TriggerInstance {
        return InventoryChangeTrigger.TriggerInstance.hasItems(*stack)
    }
}
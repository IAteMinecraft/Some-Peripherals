package net.spaceeye.someperipherals.forge

import dan200.computercraft.impl.Peripherals

import dev.architectury.platform.Platform
import dev.architectury.platform.forge.EventBuses

import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.data.event.GatherDataEvent
import net.minecraftforge.event.RegisterCommandsEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.Mod.EventBusSubscriber
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext

import net.spaceeye.someperipherals.LOG
import net.spaceeye.someperipherals.SomePeripherals
import net.spaceeye.someperipherals.SomePeripheralsCommands
import net.spaceeye.someperipherals.datagen.BlockTagProvider
import net.spaceeye.someperipherals.datagen.LootTableProvider
import net.spaceeye.someperipherals.datagen.RecipeProvider
import net.spaceeye.someperipherals.forge.integrations.cc.SomePeripheralsPeripheralProviderForge

@Mod(SomePeripherals.MOD_ID)
class SomePeripheralsForge {
    init {
        EventBuses.registerModEventBus(SomePeripherals.MOD_ID, FMLJavaModLoadingContext.get().modEventBus)
        SomePeripherals.init()

        MinecraftForge.EVENT_BUS.addListener(::registerCommands);
        //MinecraftForge.EVENT_BUS.addListener(::gatherData);

        if (Platform.isModLoaded("computercraft")) { Peripherals.register(SomePeripheralsPeripheralProviderForge()) }
    }

    private fun registerCommands(event: RegisterCommandsEvent) {
        SomePeripheralsCommands.registerServerCommands(event.dispatcher)
    }

    @EventBusSubscriber(modid = SomePeripherals.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = [Dist.DEDICATED_SERVER, Dist.CLIENT])
    object CommonModEvents {
        @SubscribeEvent
        @JvmStatic fun gatherData(event: GatherDataEvent) {
            LOG("Running Forge DataGen")

            val generator = event.generator;
            val packOutput = generator.packOutput;
            val lookupProvider = event.lookupProvider;

            generator.addProvider(event.includeServer(), RecipeProvider(packOutput));
            generator.addProvider(event.includeServer(), LootTableProvider(packOutput));
            generator.addProvider(event.includeServer(), BlockTagProvider(packOutput, lookupProvider));

            LOG("Forge DataGen complete")
        }
    }

    @EventBusSubscriber(modid = SomePeripherals.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = [Dist.CLIENT])
    object ClientModEvents {
        @SubscribeEvent
        @JvmStatic fun onClientSetup(event: FMLClientSetupEvent?) {
            SomePeripherals.initClient()
        }
    }
}
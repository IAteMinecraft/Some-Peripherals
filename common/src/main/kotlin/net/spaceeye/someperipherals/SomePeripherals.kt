package net.spaceeye.someperipherals

import com.mojang.blaze3d.vertex.DefaultVertexFormat
import dev.architectury.event.events.client.ClientReloadShadersEvent
import dev.architectury.networking.NetworkManager
import dev.architectury.platform.Platform
import dev.architectury.registry.client.rendering.BlockEntityRendererRegistry
import dev.architectury.registry.menu.MenuRegistry
import net.minecraft.client.renderer.ShaderInstance

import net.minecraft.network.FriendlyByteBuf

import net.spaceeye.someperipherals.blockentities.CommonBlockEntities
import net.spaceeye.someperipherals.blockentities.ProjectorBlockEntity
import net.spaceeye.someperipherals.blockentities.ProjectorBlockEntity.PacketType
import net.spaceeye.someperipherals.config.ConfigDelegateRegister
import net.spaceeye.someperipherals.renderers.ProjectorBlockEntityRenderer
import net.spaceeye.someperipherals.stuff.digitizer.DigitizerScreen
import net.spaceeye.someperipherals.stuff.utils.readVector3i
import net.spaceeye.someperipherals.stuff.utils.readVoxel
import net.spaceeye.someperipherals.stuff.utils.readVoxelMap

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.joml.Vector3i

fun LOG(s: String) = SomePeripherals.logger.warn(s)

object SomePeripherals {
    const val MOD_ID = "some_peripherals"
    val logger: Logger = LogManager.getLogger(MOD_ID)!!

    var has_vs: Boolean = false
    var has_arc = false

    @JvmStatic
    fun init() {
        if (Platform.isModLoaded("valkyrienskies")) { has_vs = true}
        if (Platform.isModLoaded("acceleratedraycasting")) { has_arc = true}

        ConfigDelegateRegister.initConfig()

        SomePeripheralsBlocks.register()
        SomePeripheralsBlockEntities.register()
        SomePeripheralsItems.register()
        SomePeripheralsMenu.register()
    }

    @JvmStatic
    fun initClient() {
        MenuRegistry.registerScreenFactory(SomePeripheralsMenu.DIGITIZER_MENU.get()) {it1, it2, it3 -> DigitizerScreen(it1, it2, it3) }

        ClientReloadShadersEvent.EVENT.register { provider, sink ->
            sink.registerShader(
                ShaderInstance(
                    provider,
                    "projector_pixel", // defaults to minecraft: so put in minecraft:shaders/core/
                    DefaultVertexFormat.POSITION_COLOR_LIGHTMAP
                )
            ) { shader ->
                SomePeripheralsRenderTypes.projectorPixelShader = shader
            }
        }

        SomePeripheralsRenderTypes.register()

        // Block Entity Renderers TODO: Move to own Factory
        BlockEntityRendererRegistry.register(CommonBlockEntities.PROJECTOR.get(), ::ProjectorBlockEntityRenderer)

        // Register the S2C packet handler
        NetworkManager.registerReceiver(NetworkManager.s2c(), ProjectorBlockEntity.PROJECTOR_UPDATE_ID) { buf: FriendlyByteBuf, context: NetworkManager.PacketContext ->
            val packetType = buf.readEnum(PacketType::class.java)
            val pos = buf.readBlockPos()

            when (packetType) {
                PacketType.FULL_UPDATE -> { // This should only ever get called when someone enters a chunk where there is an active projector, but still gotta implement split chunks
                    val voxels = buf.readVoxelMap()

                    context.queue {
                        val level = context.player?.level() ?: return@queue
                        val be = level.getBlockEntity(pos) as? ProjectorBlockEntity ?: return@queue

                        be.voxels.clear() // Update client-side values directly
                        be.voxels.putAll(voxels)
                    }
                }

                PacketType.CLEAR_VOXELS -> {
                    context.queue {
                        val level = context.player?.level() ?: return@queue
                        val be = level.getBlockEntity(pos) as? ProjectorBlockEntity ?: return@queue

                        be.voxels.clear()
                    }
                }

                PacketType.VOXEL_UPDATE_ADD -> {
                    val voxelPair = buf.readVoxel()

                    context.queue {
                        val level = context.player?.level() ?: return@queue
                        val be = level.getBlockEntity(pos) as? ProjectorBlockEntity ?: return@queue

                        //be.voxels.remove(voxelPair.first); // Clear the old voxel if it had existed // We don't need to do this as position can't change like this anymore
                        be.voxels[voxelPair.first] = voxelPair.second
                    }
                }

                PacketType.VOXEL_UPDATE_REMOVE -> {
                    val position = buf.readVector3i()

                    context.queue {
                        val level = context.player?.level() ?: return@queue
                        val be = level.getBlockEntity(pos) as? ProjectorBlockEntity ?: return@queue

                        be.voxels.remove(position)
                    }
                }

                PacketType.VOXEL_UPDATE_MOVE -> {
                    val oldPosition = buf.readVector3i()
                    val newPosition = buf.readVector3i()

                    context.queue {
                        val level = context.player?.level() ?: return@queue
                        val be = level.getBlockEntity(pos) as? ProjectorBlockEntity ?: return@queue

                        be.voxels[newPosition] = be.voxels.remove(oldPosition) ?: return@queue // Should never be null, but good to be safe
                    }
                }

                PacketType.ATTACH_PROJECTOR -> {
                    val doIRender = buf.readBoolean()
                    val screenPos = buf.readVector3i()
                    val screenSize = buf.readVector3i()

                    context.queue {
                        val level = context.player?.level() ?: return@queue
                        val be = level.getBlockEntity(pos) as? ProjectorBlockEntity ?: return@queue

                        be.doIRender = doIRender
                        be.screenPos = screenPos
                        be.screenSize = screenSize
                    }
                }

                PacketType.DETACH_PROJECTOR -> {
                    context.queue {
                        val level = context.player?.level() ?: return@queue
                        val be = level.getBlockEntity(pos) as? ProjectorBlockEntity ?: return@queue

                        be.voxels = mutableMapOf()
                        be.otherProjector = null
                        be.doIRender = false
                        be.screenPos = Vector3i()
                        be.screenSize = Vector3i()
                    }
                }

                else -> { throw Exception("This should never happen") }
            }
        }
    }
}
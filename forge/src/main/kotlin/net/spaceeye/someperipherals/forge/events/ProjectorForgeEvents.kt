package net.spaceeye.someperipherals.forge.events

import dev.architectury.networking.NetworkManager

import io.netty.buffer.Unpooled

import net.minecraft.network.FriendlyByteBuf

import net.minecraftforge.event.level.ChunkWatchEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import net.spaceeye.someperipherals.LOG
import net.spaceeye.someperipherals.SomePeripherals

import net.spaceeye.someperipherals.blockentities.ProjectorBlockEntity
import net.spaceeye.someperipherals.blockentities.ProjectorBlockEntity.Companion.PROJECTOR_UPDATE_ID
import net.spaceeye.someperipherals.stuff.utils.writeVoxelMap

@Mod.EventBusSubscriber(modid = SomePeripherals.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
object ProjectorForgeEvents {
    @SubscribeEvent
    fun onChunkWatch(event: ChunkWatchEvent.Watch) {
        val chunk = event.chunk
        val player = event.player

        chunk.blockEntities.forEach { (pos, be) ->
            if (
                be is ProjectorBlockEntity &&
                be.isOn() &&
                be.otherProjector != null &&
                be.doIRender
                ) { // Don't send packet if the projector has not been turned on by an attached computer or we are not the rendering instance
                val buf = FriendlyByteBuf(Unpooled.buffer()).apply {
                    writeBlockPos(pos)
                    writeVoxelMap(be.voxels);
                }

                //TODO send full update in split chunks

                if (buf.capacity() > 1048570) {
                    LOG("WEWOOWEWOO, ClientWatchChunk requested FullUpdate and was larger than 1048570 bytes, big nono :(")
                    return
                }

                NetworkManager.sendToPlayer(player, PROJECTOR_UPDATE_ID, buf)
            }
        }
    }


}
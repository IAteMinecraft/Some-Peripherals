package net.spaceeye.someperipherals.blockentities

import com.google.errorprone.annotations.concurrent.GuardedBy

import dan200.computercraft.api.peripheral.IComputerAccess

import dev.architectury.networking.NetworkManager
import dev.architectury.platform.Platform

import io.netty.buffer.Unpooled

import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.spaceeye.someperipherals.LOG

import net.spaceeye.someperipherals.blocks.PERIPHERAL_ON
import net.spaceeye.someperipherals.blocks.SomePeripheralsCommonBlocks
import net.spaceeye.someperipherals.stuff.utils.Voxel
import net.spaceeye.someperipherals.stuff.utils.findRayIntersection
import net.spaceeye.someperipherals.stuff.utils.greaterThan
import net.spaceeye.someperipherals.stuff.utils.lesserThan
import net.spaceeye.someperipherals.stuff.utils.writeVector3i
import net.spaceeye.someperipherals.stuff.utils.writeVoxel
import net.spaceeye.someperipherals.stuff.utils.writeVoxelMap

import org.joml.Vector3i
import net.spaceeye.someperipherals.stuff.utils.toJOML

class ProjectorBlockEntity(pos: BlockPos, private val state: BlockState): BlockEntity(CommonBlockEntities.PROJECTOR.get(), pos, state) {
    @GuardedBy("computers")
    private val computers: MutableSet<IComputerAccess> = mutableSetOf()
    var voxels: MutableMap<Vector3i, Voxel> = mutableMapOf()
    var otherProjector: ProjectorBlockEntity? = null
    var screenPos = Vector3i()
    var screenSize = Vector3i() // Store as vector because it is mutable
    var doIRender = false

    fun setVoxel(pos: Vector3i, voxel: Voxel): Voxel { // Take the position seperately because we don't store it in the Voxel anymore
        val otherProjector = otherProjector ?: throw NullPointerException("Projector has not been linked")
        if (screenSize == Vector3i() || pos.greaterThan(screenSize) || pos.lesserThan(Vector3i())) throw IndexOutOfBoundsException("Tried to set a voxel in an out of bounds place")
        voxels.remove(pos) // Mutable map handles null entries

        voxels[pos] = voxel
        sendVoxelAddPacket(pos, voxel)
        otherProjector.sendVoxelAddPacket(pos, voxel)

        return voxels[pos]!! // If this is null there is something really broken
    }

    fun removeVoxel(pos: Vector3i): Voxel? {
        val otherProjector = otherProjector ?: throw NullPointerException("Projector has not been linked")
        val voxel = voxels.remove(pos)
        sendVoxelRemovePacket(pos) // We use position, as only one voxel can occupy one space, and we can't reliably send the whole voxel as an object to be removed from the array
        otherProjector.sendVoxelRemovePacket(pos)

        return voxel
    }

    fun updateVoxel(oldPos: Vector3i, newPos: Vector3i): Boolean {
        val otherProjector = otherProjector ?: throw NullPointerException("Projector has not been linked")
        if (screenSize == Vector3i() || newPos.greaterThan(screenSize) || newPos.lesserThan(Vector3i())) throw IndexOutOfBoundsException("Tried to set a voxel in an out of bounds place")
        var hasReplaced = false
        if (voxels[newPos] != null) hasReplaced = true

        voxels[newPos] = voxels.remove(oldPos) ?: throw KotlinNullPointerException("Impossible null in updateVoxel") // Should never be null, but just to be safe; // Don't copy, replace
        sendVoxelMovePacket(oldPos, newPos)
        otherProjector.sendVoxelMovePacket(oldPos, newPos)

        return hasReplaced // has replaced a voxel
    }

    @Deprecated("Use key")
    fun getVoxelIndex(pos: Vector3i): Vector3i? {
        return if (voxels.contains(pos)) pos else null // Return the position or No voxel found with that index
    }

    @Deprecated("Slow Code")
    fun getVoxelEntry(pos: Vector3i): MutableMap.MutableEntry<Vector3i, Voxel>? {
        for (voxelEntry in voxels) {
            if (voxelEntry.key == pos) return voxelEntry
        }

        return null
    }

    fun clearVoxels() {
        val otherProjector = otherProjector ?: throw NullPointerException("Projector has not been linked")
        voxels.clear()
        sendClearVoxelsPacket()
        otherProjector.sendClearVoxelsPacket()
    }

    // Returns true if successfully made a link to the other projector, false otherwise
    fun tryLinkProjector(other: ProjectorBlockEntity): Boolean {
        if (otherProjector != null) return false

        val pos1 = this.blockPos.toJOML()
        val pos2 = other.blockPos.toJOML()
        val dir1 = this.blockState.getValue(BlockStateProperties.FACING)
        val dir2 = other.blockState.getValue(BlockStateProperties.FACING)

        // Get the intersection between the blocks, null if there is none
        val intersectionPos = findRayIntersection(pos1, dir1, pos2, dir2) ?: return false // this will be the center position of the screen

        // Set the references between the projectors
        // they will now share properties
        otherProjector = other
        otherProjector!!.otherProjector = this
        otherProjector!!.screenPos = this.screenPos
        otherProjector!!.screenSize = this.screenSize
        otherProjector!!.voxels = this.voxels
        otherProjector!!.doIRender = false

        // Set the bottom left corner of the screen
        screenPos.x = intersectionPos.x()
        screenPos.y = intersectionPos.y()
        screenPos.z = intersectionPos.z()

        // Set the size of the screen
        // TODO: calculate the size of the screen
        screenSize.x = (1) * SCREEN_RESOLUTION
        screenSize.y = (1) * SCREEN_RESOLUTION
        screenSize.z = (1) * SCREEN_RESOLUTION
        doIRender = true

        // Update the clientSide BlockEntities
        otherProjector!!.sendProjectorAttach()
        sendProjectorAttach()

        return true
    }

    fun unlinkProjector() {
        val otherProjector = otherProjector ?: return

        otherProjector.otherProjector = null
        otherProjector.voxels = mutableMapOf()
        otherProjector.screenSize = Vector3i()
        otherProjector.screenPos = Vector3i()

        this.otherProjector = null
        screenSize = Vector3i()
        screenPos = Vector3i()
        doIRender = false
    }

    fun attach(computer: IComputerAccess) {
        this.computers.add(computer)
        if (otherProjector == null) {
            for (peripheral in computer.availablePeripherals.values) {
                if (peripheral.type == "sp_projector") {
                    if (tryLinkProjector((peripheral.target as? ProjectorBlockEntity)!!)) {// Should never be null
                        break // Don't continue searching for projectors
                    }
                }
            }
        }
        this.level?.setBlockAndUpdate(this.blockPos, this.blockState.setValue(PERIPHERAL_ON, true))
    }

    fun detach(computer: IComputerAccess) {
        this.computers.remove(computer)

        val currentState = level?.getBlockState(blockPos)
        if (currentState!!.`is`(SomePeripheralsCommonBlocks.PROJECTOR.get())) {
            level?.setBlockAndUpdate(blockPos, currentState.setValue(PERIPHERAL_ON, computers.isNotEmpty()))

            if (otherProjector != null) {
                var shouldRelink = true

                for (computer1 in computers) {
                    for (peripheral in computer1.availablePeripherals.values) {
                        if (peripheral.type == "sp_projector") {
                            if (peripheral.target == otherProjector) shouldRelink = false // if the same projector is found, don't relink to another projector
                        }
                    }
                }

                if (shouldRelink) {
                    unlinkProjector()

                    for (computer1 in computers) {
                        for (peripheral in computer1.availablePeripherals.values) {
                            if (peripheral.type == "sp_projector") {
                                if (tryLinkProjector((peripheral.target as? ProjectorBlockEntity)!!)) {// Should never be null
                                    break // Don't continue searching for projectors
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add a server-side tick for periodic sync (fallback for initial sync on Fabric or if no changes occur)
    fun tick() {
        if (Platform.isForge()) return
        if (!Platform.isFabric() && level?.isClientSide != false) return
        if (level!!.gameTime % 60 == 0L) { // Every 3 seconds
            sendFullUpdatePacket()
        }
    }

    fun destroy() {
        unlinkProjector()

        while (computers.isNotEmpty()) {
            detach(computers.last())
        }
    }

    fun isOn(): Boolean {
        return state.getValue(PERIPHERAL_ON)
    }

    override fun saveAdditional(tag: CompoundTag) {
        super.saveAdditional(tag)

        if (otherProjector == null) return

        tag.put("otherPos", CompoundTag().let { compoundTag ->
            compoundTag.putInt("X", otherProjector!!.blockPos.x)
            compoundTag.putInt("Y", otherProjector!!.blockPos.y)
            compoundTag.putInt("Z", otherProjector!!.blockPos.z)

            compoundTag
        })
    }

    override fun load(tag: CompoundTag) {
        super.load(tag)

        if (!tag.contains("otherPos")) return

        val posTag = tag.getCompound("otherPos")

        otherProjector = level!!.getBlockEntity(BlockPos(posTag.getInt("X"), posTag.getInt("Y"), posTag.getInt("Z"))) as? ProjectorBlockEntity
    }

    // Method to send the custom update packet to tracking players
    fun sendFullUpdatePacket() {
        if (level?.isClientSide != false) return
        val serverLevel = level as ServerLevel
        val chunkPos = ChunkPos(blockPos)
        val players = serverLevel.chunkSource.chunkMap.getPlayers(chunkPos, false)
        if (players.isEmpty()) return

        LOG("Warning: Sending Full Update Packet for Platform: ${if (Platform.isFabric()) "Fabric" else if (Platform.isForge()) "Forge" else "Bruh"},\nCalled by: ${Throwable().stackTrace[1]}")

        if ( (/*enum*/1 + /*blockpos/long*/ 8 + /*voxelarray*/ 16 * voxels.size) > 1048570) {  // Don't send a full update if the size is larger than the max
            LOG("WEWOOWEWOO, sendFullUpdatePacket was larger than 1048570 bytes, big nono :(")
            return
        }
        // TODO: Split packets into smaller chunks

        val buf = FriendlyByteBuf(Unpooled.buffer()).apply {
            writeEnum(PacketType.FULL_UPDATE)
            writeBlockPos(blockPos)
            writeVoxelMap(voxels)
        }

        if (buf.capacity() > 1048570) {
            LOG("WEWOOWEWOO, sendFullUpdatePacket was somehow larger than 1048570 bytes, big nono :(")
            return
        }

        NetworkManager.sendToPlayers(players, PROJECTOR_UPDATE_ID, buf)
    }

    // Just tell the client that all voxels were removed, instead of sending all the extra trash
    fun sendClearVoxelsPacket() {
        if (level?.isClientSide != false) return
        val serverLevel = level as ServerLevel
        val chunkPos = ChunkPos(blockPos)
        val players = serverLevel.chunkSource.chunkMap.getPlayers(chunkPos, false)
        if (players.isEmpty()) return

        val buf = FriendlyByteBuf(Unpooled.buffer()).apply { // This literally can't ever be larger than the max available bytes
            writeEnum(PacketType.CLEAR_VOXELS)
            writeBlockPos(blockPos)
        }

        NetworkManager.sendToPlayers(players, PROJECTOR_UPDATE_ID, buf)
    }

    // Only send changed voxel to clients
    fun sendVoxelAddPacket(pos: Vector3i, voxel: Voxel) {
        if (level?.isClientSide != false) return
        val serverLevel = level as ServerLevel
        val chunkPos = ChunkPos(blockPos)
        val players = serverLevel.chunkSource.chunkMap.getPlayers(chunkPos, false)
        if (players.isEmpty()) return

        val buf = FriendlyByteBuf(Unpooled.buffer()).apply {
            writeEnum(PacketType.VOXEL_UPDATE_ADD)
            writeBlockPos(blockPos)
            writeVoxel(pos, voxel)
        }
        NetworkManager.sendToPlayers(players, PROJECTOR_UPDATE_ID, buf)
    }

    // tell clients a voxel has been removed
    fun sendVoxelRemovePacket(pos: Vector3i) {
        if (level?.isClientSide != false) return
        val serverLevel = level as ServerLevel
        val chunkPos = ChunkPos(blockPos)
        val players = serverLevel.chunkSource.chunkMap.getPlayers(chunkPos, false)
        if (players.isEmpty()) return

        val buf = FriendlyByteBuf(Unpooled.buffer()).apply {
            writeEnum(PacketType.VOXEL_UPDATE_REMOVE)
            writeBlockPos(blockPos)
            writeVector3i(pos)
        }
        NetworkManager.sendToPlayers(players, PROJECTOR_UPDATE_ID, buf)
    }

    // tell clients a voxel has been removed
    fun sendVoxelMovePacket(oldPos: Vector3i, newPos: Vector3i) {
        if (level?.isClientSide != false) return
        val serverLevel = level as ServerLevel
        val chunkPos = ChunkPos(blockPos)
        val players = serverLevel.chunkSource.chunkMap.getPlayers(chunkPos, false)
        if (players.isEmpty()) return

        val buf = FriendlyByteBuf(Unpooled.buffer()).apply {
            writeEnum(PacketType.VOXEL_UPDATE_MOVE)
            writeBlockPos(blockPos)
            writeVector3i(oldPos)
            writeVector3i(newPos)
        }
        NetworkManager.sendToPlayers(players, PROJECTOR_UPDATE_ID, buf)
    }

    fun sendProjectorAttach() {
        if (otherProjector == null) throw NullPointerException("Then why did you call this")
        if (level?.isClientSide != false) return

        val serverLevel = level as ServerLevel
        val chunkPos = ChunkPos(blockPos)
        val players = serverLevel.chunkSource.chunkMap.getPlayers(chunkPos, false)
        if (players.isEmpty()) return

        val buf = FriendlyByteBuf(Unpooled.buffer()).apply {
            writeEnum(PacketType.ATTACH_PROJECTOR)
            writeBlockPos(blockPos)
            writeBoolean(doIRender)
            writeVector3i(screenPos)
            writeVector3i(screenSize)
        }
        NetworkManager.sendToPlayers(players, PROJECTOR_UPDATE_ID, buf)
    }

    enum class PacketType {
        FULL_UPDATE,
        CLEAR_VOXELS,
        VOXEL_UPDATE_ADD,
        VOXEL_UPDATE_REMOVE,
        VOXEL_UPDATE_MOVE,
        ATTACH_PROJECTOR
    }

    // Define the packet ID
    companion object {
        val PROJECTOR_UPDATE_ID = ResourceLocation("someperipherals", "projector_update")
        val SCREEN_RESOLUTION = 16
    }
}
package net.spaceeye.someperipherals.integrations.cc.peripherals

import dan200.computercraft.api.lua.IArguments
import dan200.computercraft.api.lua.IDynamicLuaObject
import dan200.computercraft.api.lua.ILuaContext
import dan200.computercraft.api.lua.LuaException
import dan200.computercraft.api.lua.LuaFunction
import dan200.computercraft.api.lua.MethodResult
import dan200.computercraft.api.peripheral.IComputerAccess
import dan200.computercraft.api.peripheral.IPeripheral

import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.properties.BlockStateProperties

import net.spaceeye.someperipherals.blockentities.ProjectorBlockEntity
import net.spaceeye.someperipherals.blocks.SomePeripheralsCommonBlocks
import net.spaceeye.someperipherals.stuff.utils.Voxel

import org.joml.Vector3i
import org.valkyrienskies.core.util.component1
import org.valkyrienskies.core.util.component2
import org.valkyrienskies.core.util.component3

class ProjectorPeripheral(private val level: Level, private val pos: BlockPos, private var be: BlockEntity): IPeripheral {
    @LuaFunction
    fun clearVoxels() {
        (be as? ProjectorBlockEntity)?.clearVoxels()
    }

    @LuaFunction
    fun setVoxel(x: Int, y: Int, z: Int, r: Int, g: Int, b: Int, a: Int): Any {
        // TODO: Calculate the blocks where voxels are allowed to be, but do this in the BlockEntity
        // Clamp RGBA values
        return VoxelWrapper((be as? ProjectorBlockEntity)!!,Vector3i(x, y, z),(be as? ProjectorBlockEntity)?.setVoxel(
            Vector3i(x, y, z),
            Voxel(
                r.coerceAtMost(255).coerceAtLeast(0).toUByte(),
                g.coerceAtMost(255).coerceAtLeast(0).toUByte(),
                b.coerceAtMost(255).coerceAtLeast(0).toUByte(),
                a.coerceAtMost(255).coerceAtLeast(0).toUByte()
            )
        )!!) // Will never be null
    }

    @LuaFunction
    fun removeVoxel(x: Int, y: Int, z: Int): MutableMap<String, Any> {
        return (be as? ProjectorBlockEntity)?.removeVoxel(Vector3i(x, y, z))!!.toMap(Vector3i(x, y, z))
    }

    @LuaFunction
    fun getVoxel(x: Int, y: Int, z: Int): Any? {
        val projector = (be as ProjectorBlockEntity)
        val pos = Vector3i(x, y, z)
        return VoxelWrapper(projector, pos.clone() as Vector3i, projector.voxels[pos] ?: return null)
    }

    @LuaFunction
    fun getVoxels(): ArrayList<Any> {
        val voxelList = ArrayList<Any>()

        for (voxelEntry in (be as? ProjectorBlockEntity)?.voxels!!) {
            voxelList.add(VoxelWrapper((be as? ProjectorBlockEntity)!!, voxelEntry.key.clone() as Vector3i, voxelEntry.value))
        }

        return voxelList
    }

    @LuaFunction
    fun isLinked() = (be as? ProjectorBlockEntity)!!.otherProjector != null

    //@LuaFunction
    //fun doIRender() = (be as? ProjectorBlockEntity)!!.doIRender;

    //@LuaFunction
    //fun getScreenPos(): Array<Any> {(be as? ProjectorBlockEntity)!!.screenPos.let { (i, i1, i2) -> return arrayOf(i, i1, i2) }}

    @LuaFunction
    fun getScreenSize(): Array<Any> {(be as? ProjectorBlockEntity)!!.screenSize.let { (i, i1, i2) -> return arrayOf(i, i1, i2) }}

    @LuaFunction
    fun linkProjector(computer: IComputerAccess, name: String): Boolean {
        val otherProjector = computer.getAvailablePeripheral(name) ?: throw LuaException("No peripheral with the name \"$name\"")
        if (otherProjector.type != "sp_projector") throw LuaException("Not a Projector peripheral")

        return (be as? ProjectorBlockEntity)!!.tryLinkProjector(otherProjector.target as ProjectorBlockEntity)
    }

    @LuaFunction
    fun unlinkProjector(): Boolean {
        if ((be as? ProjectorBlockEntity)!!.otherProjector == null) return false
        (be as? ProjectorBlockEntity)!!.unlinkProjector()

        return true
    }

    @LuaFunction
    fun getFacingDirection() = be.blockState.getValue(BlockStateProperties.FACING).getName()!!

    override fun getTarget() = (be as? ProjectorBlockEntity)
    override fun getType() = "sp_projector"
    override fun equals(p0: IPeripheral?) = level.getBlockState(pos).`is`(SomePeripheralsCommonBlocks.PROJECTOR.get()) // TODO: Fix this

    override fun attach(computer: IComputerAccess) {
        (be as? ProjectorBlockEntity)?.attach(computer)
    }

    override fun detach(computer: IComputerAccess) {
        (be as? ProjectorBlockEntity)?.detach(computer)
    }
}

class VoxelWrapper(private val projector: ProjectorBlockEntity, private var voxelPos: Vector3i, private val voxel: Voxel) : IDynamicLuaObject {
    override fun getMethodNames(): Array<String> = arrayOf(
        "getRed", "setRed",
        "getGreen", "setGreen",
        "getBlue", "setBlue",
        "getAlpha", "setAlpha",
        "getPosition"
    )

    override fun callMethod(context: ILuaContext?, method: Int, arguments: IArguments): MethodResult {
        return MethodResult.of(
            when (method) {
                0 -> arrayOf(voxel.red.toInt())   // getRed
                1 -> {                            // setRed
                    val r = arguments.getInt(0).coerceAtMost(255).coerceAtLeast(0)
                    voxel.red = r.toUByte()

                    projector.sendVoxelAddPacket(voxelPos, voxel)

                    null
                }
                2 -> arrayOf(voxel.green.toInt()) // getGreen
                3 -> {                            // setGreen
                    val g = arguments.getInt(0).coerceAtMost(255).coerceAtLeast(0)
                    voxel.green = g.toUByte()

                    projector.sendVoxelAddPacket(voxelPos, voxel)

                    null
                }
                4 -> arrayOf(voxel.blue.toInt())  // getBlue
                5 -> {                            // setBlue
                    val b = arguments.getInt(0).coerceAtMost(255).coerceAtLeast(0)
                    voxel.blue = b.toUByte()

                    projector.sendVoxelAddPacket(voxelPos, voxel)

                    null
                }
                6 -> arrayOf(voxel.alpha.toInt()) // getAlpha
                7 -> {                            // setAlpha
                    val a = arguments.getInt(0).coerceAtMost(255).coerceAtLeast(0)
                    voxel.alpha = a.toUByte()

                    projector.sendVoxelAddPacket(voxelPos, voxel)

                    null
                }
                8 -> PositionWrapper(projector, voxelPos) // getPosition
                else -> throw LuaException("Invalid method")
            }
        )
    }
}

class PositionWrapper(private val projector: ProjectorBlockEntity, private var voxelPos: Vector3i) : IDynamicLuaObject {
    override fun getMethodNames(): Array<String> = arrayOf(
        "getX", "setX",
        "getY", "setY",
        "getZ", "setZ",
        "set",  "get"
    )

    override fun callMethod(context: ILuaContext?, method: Int, arguments: IArguments): MethodResult {
        when (method) { // Don't exit if a voxel is in the new pos, just replace it
            0 -> return MethodResult.of(voxelPos.x) // getX
            1 -> {                                // setX
                val newX = arguments.getInt(0)
                val newPos = Vector3i(newX, voxelPos.y, voxelPos.z)

                projector.updateVoxel(voxelPos, newPos) // Sends UpdatePacket automatically
                voxelPos.set(newPos)

                return MethodResult.of(true) // Successfully changed voxelPos
            }
            2 -> return MethodResult.of(voxelPos.y) // getY
            3 -> {                                // setY
                val newY = arguments.getInt(0)
                val newPos = Vector3i(voxelPos.x, newY, voxelPos.z)

                projector.updateVoxel(voxelPos, newPos) // Sends UpdatePacket automatically
                voxelPos.set(newPos)

                return MethodResult.of(true) // Successfully changed voxelPos
            }
            4 -> return MethodResult.of(voxelPos.z) // getZ
            5 -> {                                // setZ
                val newZ = arguments.getInt(0)
                val newPos = Vector3i(voxelPos.x, voxelPos.y, newZ)

                projector.updateVoxel(voxelPos, newPos) // Sends UpdatePacket automatically
                voxelPos.set(newPos)

                return MethodResult.of(true) // Successfully changed voxelPos
            }
            6 -> {                                // set
                val newX = arguments.getInt(0)
                val newY = arguments.getInt(1)
                val newZ = arguments.getInt(2)
                val newPos = Vector3i(newX, newY, newZ)

                projector.updateVoxel(voxelPos, newPos) // Sends UpdatePacket automatically
                voxelPos.set(newPos)

                return MethodResult.of(true) // Successfully changed voxelPos
            }
            7 -> return MethodResult.of(voxelPos.x, voxelPos.y, voxelPos.z)

            else -> throw LuaException("Invalid method")
        }
    }
}
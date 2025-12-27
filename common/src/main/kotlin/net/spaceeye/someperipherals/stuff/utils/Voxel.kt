package net.spaceeye.someperipherals.stuff.utils

import net.minecraft.core.Direction

import org.joml.Vector3i

data class Voxel(
//    var position: Vector3i = Vector3i(0), // Don't store position, as it is stored by the map
    var red:      UByte    = 0u,
    var green:    UByte    = 0u,
    var blue:     UByte    = 0u,
    var alpha:    UByte    = 0u
){
    public fun toMap(pos: Vector3i): MutableMap<String, Any>  {
        val list = mutableMapOf<String, Any>() ;

        list["position"] = mutableMapOf(
            Pair("x", pos.x),
            Pair("y", pos.y),
            Pair("z", pos.z)
        );
        list["red"]   = red  .toInt();
        list["green"] = green.toInt();
        list["blue"]  = blue .toInt();
        list["alpha"] = alpha.toInt();

        return list;
    }
}

public fun isVisibleFrom(voxelMap: MutableMap<Vector3i, Voxel>, pos: Vector3i, lookingFrom: Direction): Boolean { // assume opaque voxels (for now)
    when (lookingFrom) { // Check if position value is the highest or lowest in the respective axis
        Direction.UP -> { // max y value
            for (mapPos in voxelMap.keys) {
                if (mapPos.y > pos.y) return true;
            }
        }

        Direction.DOWN -> { // min y value
            for (mapPos in voxelMap.keys) {
                if (mapPos.y < pos.y) return true;
            }
        }

        Direction.NORTH -> { // max x value
            for (mapPos in voxelMap.keys) {
                if (mapPos.x > pos.x) return true;
            }
        }

        Direction.SOUTH -> { // min x value
            for (mapPos in voxelMap.keys) {
                if (mapPos.x < pos.x) return true;
            }
        }

        Direction.WEST -> { // max z value
            for (mapPos in voxelMap.keys) {
                if (mapPos.z > pos.z) return true;
            }
        }

        Direction.EAST -> { // min z value
            for (mapPos in voxelMap.keys) {
                if (mapPos.z < pos.z) return true;
            }
        }
    }

    return false;
}

public fun getVisibleFor(voxelMap: MutableMap<Vector3i, Voxel>, lookingFrom: Direction): MutableMap<Vector3i, Voxel> {
    val visibleVoxels = mutableMapOf<Vector3i, Voxel>();

    for (pos in voxelMap.keys) {
        if (isVisibleFrom(voxelMap, pos, lookingFrom)) {
            visibleVoxels[pos] = voxelMap[pos]!!; // cannot ever be null
        }
    }

    return visibleVoxels;
}

public fun hollow(voxelMap: MutableMap<Vector3i, Voxel>): MutableMap<Vector3i, Voxel> {
    val newMap = mutableMapOf<Vector3i, Voxel>();

    for (pos in voxelMap.keys) { // add only voxels that have access to air
        if ( // Front
            voxelMap[Vector3i(pos.x, pos.y, pos.z + 1)] == null
        ) {newMap[pos] = voxelMap[pos]!!; continue;}
        if ( // Back
            voxelMap[Vector3i(pos.x, pos.y, pos.z - 1)] == null
        ) {newMap[pos] = voxelMap[pos]!!; continue;}
        if ( // Left
            voxelMap[Vector3i(pos.x - 1, pos.y, pos.z)] == null
        ) {newMap[pos] = voxelMap[pos]!!; continue;}
        if ( // Right
            voxelMap[Vector3i(pos.x + 1, pos.y, pos.z)] == null
        ) {newMap[pos] = voxelMap[pos]!!; continue;}
        if ( // Up
            voxelMap[Vector3i(pos.x, pos.y + 1, pos.z)] == null
        ) {newMap[pos] = voxelMap[pos]!!; continue;}
        if ( // Down
            voxelMap[Vector3i(pos.x, pos.y - 1, pos.z)] == null
        ) {newMap[pos] = voxelMap[pos]!!; continue;}
    }

    return newMap;
}

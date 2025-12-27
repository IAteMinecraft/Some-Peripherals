package net.spaceeye.someperipherals.stuff.utils

import io.netty.handler.codec.DecoderException

import net.minecraft.network.FriendlyByteBuf

import org.joml.Vector3i

public fun FriendlyByteBuf.writeVector3i(vector3i: Vector3i) { // 12 bytes
    this.writeInt(vector3i.x); // 4 bytes
    this.writeInt(vector3i.y); // 4 bytes
    this.writeInt(vector3i.z); // 4 bytes
}

public fun FriendlyByteBuf.readVector3i(): Vector3i { // 12 bytes
    return Vector3i(
        this.readInt(),
        this.readInt(),
        this.readInt()
    )
}

public fun FriendlyByteBuf.writeVoxel(pos: Vector3i, voxel: Voxel) { // 16 bytes
    this.writeVector3i(pos); // 12 bytes

    this.writeByte(voxel.red  .toInt()); // 1 byte
    this.writeByte(voxel.green.toInt()); // 1 byte
    this.writeByte(voxel.blue .toInt()); // 1 byte
    this.writeByte(voxel.alpha.toInt()); // 1 byte
}

public fun FriendlyByteBuf.readVoxel(): Pair<Vector3i, Voxel> {
    return Pair(
        this.readVector3i(),
        Voxel(
            this.readByte().toUByte(),
            this.readByte().toUByte(),
            this.readByte().toUByte(),
            this.readByte().toUByte()
        )
    );
}

public fun FriendlyByteBuf.writeVoxelMap(voxelMap: MutableMap<Vector3i, Voxel>): FriendlyByteBuf {
    this.writeVarInt(voxelMap.size);

    voxelMap.forEach { (pos, voxel) ->
        this.writeVoxel(pos, voxel);
    }

    return this;
}

public fun FriendlyByteBuf.readVoxelMap(): MutableMap<Vector3i, Voxel> {
    return this.readVoxelMap(null as MutableMap<Vector3i, Voxel>?);
}

public fun FriendlyByteBuf.readVoxelMap(map: MutableMap<Vector3i, Voxel>?): MutableMap<Vector3i, Voxel> {
    return this.readVoxelMap(map, this.readableBytes() / 16);
}

public fun FriendlyByteBuf.readVoxelMap(map: MutableMap<Vector3i, Voxel>?, maxLength: Int): MutableMap<Vector3i, Voxel> {
    var map = map;
    val length: Int = this.readVarInt();

    if (map == null || map.size != length) {
        if (length > maxLength) {
            throw DecoderException("VoxelArray with size $length is bigger than allowed $maxLength")
        }

        map = mutableMapOf()
    }

    for (i in 0 until length) {
        this.readVoxel().let { (pos, voxel) -> map[pos] = voxel }
    }

    return map;
}
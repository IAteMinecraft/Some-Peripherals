package net.spaceeye.someperipherals.stuff.utils

import net.minecraft.core.Direction
import org.joml.Vector3i
import org.joml.Vector3ic

@Deprecated("Use set", replaceWith = ReplaceWith("set(vector)"))
fun Vector3i.copy(vector: Vector3i) {
    this.x = vector.x
    this.y = vector.y
    this.z = vector.z
}

fun Vector3i.greaterThan(vec: Vector3i): Boolean {
    return  this.x > vec.x ||
            this.y > vec.y ||
            this.z > vec.z
}

fun Vector3i.lesserThan(vec: Vector3i): Boolean {
    return  this.x < vec.x ||
            this.y < vec.y ||
            this.z < vec.z
}

/**
 * Calculates the intersection point of two axis-aligned rays in 3D space.
 * Returns the intersection point if the rays intersect (considering their directions),
 * or null if they do not intersect or if they are parallel (same axis, no unique point).
 *
 * @param pos1 Starting position of the first ray.
 * @param dir1 Direction of the first ray.
 * @param pos2 Starting position of the second ray.
 * @param dir2 Direction of the second ray.
 * @return The intersection point as Vector3i, or null if no intersection.
 */
fun findRayIntersection(pos1: Vector3ic, dir1: Direction, pos2: Vector3ic, dir2: Direction): Vector3i? {
    // Don't need floating point precision as divisions/multiplications will always return whole numbers
    if (dir1.axis == dir2.axis) {
        return null // Parallel rays along the same axis; no unique intersection point.
    }

    // Different axes; determine the fixed axis (the one neither ray varies along).
    val axis1 = dir1.axis
    val axis2 = dir2.axis
    val fixedAxis = Direction.Axis.entries.first { it != axis1 && it != axis2 }

    // Check if positions match on the fixed axis
    if (fixedAxis.choose(pos1.x(), pos1.y(), pos1.z()) - fixedAxis.choose(pos2.x(), pos2.y(), pos2.z()) != 0) {
        return null
    }

    // Compute parameters t and s.
    val offset1 = dir1.axisDirection.step
    val c1Axis1 = axis1.choose(pos1.x(), pos1.y(), pos1.z())
    val c2Axis1 = axis1.choose(pos2.x(), pos2.y(), pos2.z())
    val t = (c2Axis1 - c1Axis1) / offset1

    val offset2 = dir2.axisDirection.step
    val c1Axis2 = axis2.choose(pos1.x(), pos1.y(), pos1.z())
    val c2Axis2 = axis2.choose(pos2.x(), pos2.y(), pos2.z())
    val s = (c1Axis2 - c2Axis2) / offset2

    // Check if both parameters are non-negative
    if (t >= 0 && s >= 0) {
        // Calculate the intersection point using the first ray.
        val dx = t * dir1.stepX
        val dy = t * dir1.stepY
        val dz = t * dir1.stepZ
        return Vector3i(pos1).add(dx, dy, dz)
    }

    return null
}
package org.simbrain.util.projection

import org.simbrain.util.UserParameter
import kotlin.math.pow

class SammonProjection2: ProjectionMethod2() {

    val downstairsInitializationMethod = CoordinateProjection2()

    @UserParameter(label = "Epsilon", minimumValue = 0.0, increment = .1)
    var epsilon = 0.1

    var numPoints = 0

    override fun project(dataset: Dataset2) {
        // fun List<List<Double>>.thing() = joinToString("\n") {
        //     it.joinToString(", ") {
        //         it.format(2)
        //     }
        // }
        upstairsDistances = dataset.computeUpstairsDistances()
        upstairsDistanceSum = upstairsDistances?.sumOf { it.sum() }
        // downstairsDistances = dataset.computeDownstairsDistances()
        // println("UP")
        // upstairsDistances?.let { println(it.thing()) }
        // println("DOWN")
        // downstairsDistances?.let { println(it.thing()) }
    }

    override fun initializeDownstairsPoint(dataset: Dataset2, point: DataPoint2) {
        downstairsInitializationMethod.initializeDownstairsPoint(dataset, point)
    }

    var upstairsDistances: List<List<Double>>? = null
    var downstairsDistances: List<List<Double>>? = null
    var upstairsDistanceSum: Double? = null

    fun iterate(dataset: Dataset2) {
        if (dataset.kdTree.size < 2) return
        downstairsDistances = dataset.computeDownstairsDistances()
        dataset.kdTree.forEachIndexed { j, p1 ->
            var partialSum = 0.0
            (0 until p1.downstairsPoint.size).forEach { d ->
                dataset.kdTree.forEachIndexed { i, p2 ->
                    if (i != j) {
                        partialSum += (
                                (upstairsDistances!![i][j] - downstairsDistances!![i][j])
                                        * (p2.downstairsPoint[d] - p1.downstairsPoint[d])
                                ) / upstairsDistances!![i][j] / downstairsDistances!![i][j]
                    }
                }

                p1.downstairsPoint[d] = p1.downstairsPoint[d] - ((epsilon * 2 * partialSum) / upstairsDistanceSum!!)
            }
        }

        // Computes Closeness
        error = 0.0
        for (i in 0 until dataset.kdTree.size) {
            for (j in i + 1 until dataset.kdTree.size) {
                error += (upstairsDistances!![i][j] - downstairsDistances!![i][j]).pow(2) / upstairsDistances!![i][j]
            }
        }
        // println(e / upstairsDistanceSum!!)
    }

    var error = 0.0

    override val name = "Sammon"

    override fun copy() = SammonProjection2()

    // Kotlin hack to support "static method in superclass"
    companion object {
        @JvmStatic
        fun getTypes(): List<Class<*>> {
            return ProjectionMethod2.getTypes()
        }
    }
}
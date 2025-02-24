/*
 * Part of Simbrain--a java-based neural network kit Copyright (C) 2005,2007 The
 * Authors. See http://www.simbrain.net/credits This program is free software;
 * you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version. This program is
 * distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details. You
 * should have received a copy of the GNU General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 59 Temple Place
 * - Suite 330, Boston, MA 02111-1307, USA.
 */
package org.simbrain.network.subnetworks

import org.simbrain.network.core.Network
import org.simbrain.network.core.randomizeBiases
import org.simbrain.network.groups.Subnetwork
import org.simbrain.network.matrix.NeuronArray
import org.simbrain.network.matrix.WeightMatrix
import org.simbrain.network.util.Direction
import org.simbrain.network.util.offsetNeuronGroup
import org.simbrain.util.stats.distributions.UniformRealDistribution
import java.awt.geom.Point2D

/**
 * A standard feed-forward network, as a succession of [NeuronArray] and [WeightMatrix] objects.
 *
 * @author Jeff Yoshimi
 */
open class FeedForward(
    network: Network,
    /**
     * Integers 1...n correspond to number of nodes in layers 1..n
     */
    nodesPerLayer: IntArray,
    /**
     * Center location for network.
     */
    initialPosition: Point2D?) : Subnetwork(network) {

    var betweenLayerInterval = 250

    /**
     * Ordered reference to [NeuronArray]'s maintained in [Subnetwork.modelList]
     */
    val layerList: MutableList<NeuronArray> = ArrayList()

    val wmList: MutableList<WeightMatrix> = ArrayList()

    var inputLayer: NeuronArray
        private set

    var outputLayer: NeuronArray
        private set

    init {
        label = "Layered Network"
        inputLayer = NeuronArray(network, nodesPerLayer[0])
        addModel(inputLayer)
        layerList.add(inputLayer)

        // Memory of last layer created
        var lastLayer = inputLayer

        // Make hidden layers and output layer
        for (i in 1 until nodesPerLayer.size) {
            val hiddenLayer = NeuronArray(network, nodesPerLayer[i])
            addModel(hiddenLayer)
            layerList.add(hiddenLayer)
            offsetNeuronGroup(
                lastLayer,
                hiddenLayer,
                Direction.NORTH,
                (betweenLayerInterval / 2).toDouble(),
                100.0,
                200.0
            )

            // Add weight matrix
            val wm = WeightMatrix(parentNetwork, lastLayer, hiddenLayer)
            wm.randomize()
            addModel(wm)
            wmList.add(wm)

            // Reset last layer
            lastLayer = hiddenLayer
        }
        if (initialPosition != null) {
            setLocation(initialPosition.x, initialPosition.y)
        }
        outputLayer = lastLayer
    }

    override val name: String
        get() = "Feedforward"

    override fun onCommit() {}

    val randomizer = UniformRealDistribution(-1.0, 1.0)

    override fun randomize() {
        wmList.forEach { wm -> wm.randomize() }
        (layerList - inputLayer).forEach { it.randomizeBiases() }
    }

    override fun update() {
        inputLayer.updateInputs()
        inputLayer.update()
        for (i in 1 until layerList.size - 1) {
            layerList[i].updateInputs()
            layerList[i].update()
        }
        outputLayer.updateInputs()
        outputLayer.update()
    }
}
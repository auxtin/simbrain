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
import org.simbrain.network.neuron_update_rules.LinearRule
import org.simbrain.network.trainers.BackpropTrainer2
import org.simbrain.network.trainers.MatrixDataset
import org.simbrain.network.trainers.Trainable2
import org.simbrain.network.trainers.createDiagonalDataset
import java.awt.geom.Point2D
import kotlin.math.min

/**
 * Backprop network.
 *
 * @author Jeff Yoshimi
 */
open class BackpropNetwork(network: Network, nodesPerLayer: IntArray, initialPosition: Point2D?) :
    FeedForward(network, nodesPerLayer, initialPosition), Trainable2 {

    override lateinit var trainingSet: MatrixDataset

    override val trainer by lazy {
        BackpropTrainer2(this)
    }

    init {
        layerList.forEach { it.updateRule = LinearRule() }
        inputLayer.isClamped = true
        val nin = nodesPerLayer.first()
        val nout = nodesPerLayer.last()
        trainingSet = createDiagonalDataset(nin, nout, min(nin,nout))
        label = "Backprop"
    }
}
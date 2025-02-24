/*
 * Part of Simbrain--a java-based neural network kit
 * Copyright (C) 2005,2007 The Authors.  See http://www.simbrain.net/credits
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.simbrain.network.updaterules

import org.simbrain.network.core.Layer
import org.simbrain.network.core.Network
import org.simbrain.network.core.Neuron
import org.simbrain.network.matrix.NeuronArray
import org.simbrain.network.neuron_update_rules.AbstractSigmoidalRule
import org.simbrain.network.util.BiasedMatrixData
import org.simbrain.network.util.BiasedScalarData
import org.simbrain.util.add

/**
 * Discrete sigmoidal provides various implementations of a standard sigmoidal neuron.
 *
 * @author Zoë Tosi
 * @author Jeff Yoshimi
 */
class SigmoidalRule : AbstractSigmoidalRule() {

    override fun getTimeType(): Network.TimeType {
        return Network.TimeType.DISCRETE
    }

    override fun apply(neuron: Neuron, data: BiasedScalarData) {
        var weightedInput = neuron.input + data.bias
        if (addNoise) {
            weightedInput += noiseGenerator.sampleDouble()
        }
        neuron.activation = sFunction.valueOf(weightedInput, upperBound, lowerBound, slope)
    }

    override fun apply(arr: Layer, data: BiasedMatrixData) {
        val array = arr as NeuronArray
        val weightedInputs = array.inputs.add(data.biases)
        if (addNoise) {
            weightedInputs.add(noiseGenerator.sampleDouble(array.size()))
        }
        array.activations = sFunction.valueOf(weightedInputs, lowerBound, upperBound, slope)
    }

    override fun deepCopy(): SigmoidalRule {
        var sr = SigmoidalRule()
        sr = super.baseDeepCopy(sr) as SigmoidalRule
        return sr
    }

    override fun getDerivative(input: Double): Double {
        return sFunction.derivVal(input, upperBound, lowerBound, upperBound - lowerBound)
    }

    override val name: String
        get() = "Sigmoidal (Discrete)"

}
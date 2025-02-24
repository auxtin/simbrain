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
package org.simbrain.network.neuron_update_rules;

import org.simbrain.network.core.Layer;
import org.simbrain.network.core.Network.TimeType;
import org.simbrain.network.core.Neuron;
import org.simbrain.network.core.NeuronUpdateRule;
import org.simbrain.network.util.BiasedMatrixData;
import org.simbrain.network.util.BiasedScalarData;
import org.simbrain.network.util.MatrixDataHolder;
import org.simbrain.network.util.ScalarDataHolder;
import org.simbrain.util.UserParameter;

import java.util.Random;

/**
 * <b>BinaryNeuron</b> takes one of two values.
 */
public class BinaryRule extends NeuronUpdateRule {

    private static final double DEFAULT_CEILING = 1.0;

    private static final double DEFAULT_FLOOR = -1.0;

    /**
     * Threshold for binary neurons.
     */
    @UserParameter(
            label = "Threshold",
            description = "Threshold for binary neurons.",
            increment = .1, order = 1)
    private double threshold = .5;

    @UserParameter(
            label = "On Value",
            description = "The value that turns on neuron.",
            increment=.1, order = 2)
    private double ceiling = DEFAULT_CEILING;

    @UserParameter(
            label = "Off Value",
            description = "The value that turns off neuron.",
            increment=.1, order = 3)
    private double floor = DEFAULT_FLOOR;

    /**
     * Bias for binary neurons.
     */
    @UserParameter(
            label = "Bias",
            description = "Bias for binary neurons.",
            increment = .1, order = 4)
    private double bias = 0;

    public BinaryRule() {
    }

    public BinaryRule(double floor, double ceil, double threshold) {
        this.ceiling = ceil;
        this.floor = floor;
        this.threshold = threshold;
    }

    @Override
    public BinaryRule deepCopy() {
        BinaryRule bn = new BinaryRule();
        bn.setThreshold(getThreshold());
        bn.setCeiling(getUpperBound());
        bn.setFloor(getLowerBound());
        return bn;
    }

    @Override
    public void apply(Layer array, MatrixDataHolder data) {
        for (int i = 0; i < array.getOutputs().nrow() ; i++) {
            array.getOutputs().set(i, 0, binaryRule(array.getInputs().get(i, 0), ((BiasedMatrixData)data).getBiases().get(i, 0)));
        }
    }

    @Override
    public void apply(Neuron neuron, ScalarDataHolder data) {
        neuron.setActivation(binaryRule(neuron.getInput(), ((BiasedScalarData)data).getBias()));
    }

    public double binaryRule(double in, double bias) {
        double wtdInput = in + bias;
        if (wtdInput > threshold) {
            return getUpperBound();
        } else {
            return getLowerBound();
        }
    }

    @Override
    public MatrixDataHolder createMatrixData(int size) {
        return new BiasedMatrixData(size);
    }

    @Override
    public ScalarDataHolder createScalarData() {
        return new BiasedScalarData();
    }

    @Override
    public double getRandomValue() {
        Random rand = new Random();
        return rand.nextBoolean() ? getUpperBound() : getLowerBound();
    }

    public void setThreshold(final double threshold) {
        this.threshold = threshold;
    }

    public void setBias(final double bias) {
        this.bias = bias;
    }

    @Override
    public String getName() {
        return "Binary";
    }

    // TODO: Get rid of redundant wording.   Have not cleaned it up yet
    // for fear of xstream problems.

    public double getUpperBound() {
        return ceiling;
    }

    public void setUpperBound(double ub) {
        this.ceiling = ub;
    }

    public void setCeiling(double ceiling) {
        this.ceiling = ceiling;
    }

    public double getLowerBound() {
        return floor;
    }

    public void setLowerBound(double lb) {
        this.floor = lb;
    }

    public void setFloor(double floor) {
        this.floor = floor;
    }

    @Override
    public void contextualIncrement(Neuron n) {
        n.forceSetActivation(ceiling);
    }

    @Override
    public void contextualDecrement(Neuron n) {
        n.forceSetActivation(floor);
    }

    @Override
    public double getGraphicalLowerBound() {
        return floor - 1;
    }

    @Override
    public double getGraphicalUpperBound() {
        return ceiling + 1;
    }

    @Override
    public TimeType getTimeType() {
        return TimeType.DISCRETE;
    }

    public double getBias() {
        return bias;
    }

    public double getThreshold() {
        return threshold;
    }

}

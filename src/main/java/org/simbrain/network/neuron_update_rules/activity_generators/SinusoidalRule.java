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
package org.simbrain.network.neuron_update_rules.activity_generators;

import org.simbrain.network.core.Network.TimeType;
import org.simbrain.network.core.Neuron;
import org.simbrain.network.core.NeuronUpdateRule;
import org.simbrain.network.neuron_update_rules.interfaces.ActivityGenerator;
import org.simbrain.network.updaterules.interfaces.ClippedUpdateRule;
import org.simbrain.network.updaterules.interfaces.NoisyUpdateRule;
import org.simbrain.network.util.ScalarDataHolder;
import org.simbrain.util.UserParameter;
import org.simbrain.util.stats.ProbabilityDistribution;
import org.simbrain.util.stats.distributions.UniformRealDistribution;

/**
 * <b>SinusoidalNeuron</b> produces a sine wave.
 */
public class SinusoidalRule extends NeuronUpdateRule implements ActivityGenerator, ClippedUpdateRule, NoisyUpdateRule {

    /**
     * Phase.
     */
    @UserParameter(
            label = "Phase",
            description = "The phase tells us where we start in a period of the sinusoidal oscillation.",
            order = 1)
    private double phase = 1;

    /**
     * Frequency.
     */
    @UserParameter(
            label = "Frequency",
            description = "The frequency tells us how frequently the activation oscillates.",
            order = 2)
    private double frequency = .1;

    /**
     * The upper boundary of the activation.
     */
    private double ceiling = 1.0;

    /**
     * The lower boundary of the activation.
     */
    private double floor = -1.0;

    /**
     * Noise generator.
     */
    private ProbabilityDistribution noiseGenerator = new UniformRealDistribution();

    /**
     * Add noise to the neuron.
     */
    private boolean addNoise = false;

    /**
     * Bounded update rule is automatically clippable.  It is not needed here since sigmoids automatically respect
     * upper and lower bounds but can still be turned on to constrain contextual increment and decrement.
     */
    private boolean isClipped = false;

    @Override
    public final TimeType getTimeType() {
        return TimeType.DISCRETE;
    }

    @Override
    public final SinusoidalRule deepCopy() {
        SinusoidalRule sn = new SinusoidalRule();
        sn.setPhase(getPhase());
        sn.setFrequency(getFrequency());
        sn.setAddNoise(getAddNoise());
        sn.noiseGenerator = noiseGenerator.deepCopy();
        return sn;
    }

    @Override
    public void apply(Neuron neuron, ScalarDataHolder data) {
        double upperBound = getUpperBound();
        double lowerBound = getLowerBound();
        double range = upperBound - lowerBound;
        double val = ((range / 2) * Math.sin(frequency * neuron.getNetwork().getTime() + phase)) + ((upperBound + lowerBound) / 2);

        if (addNoise) {
            val += noiseGenerator.sampleDouble();
        }

        neuron.setActivation(val);
    }

    @Override
    public ProbabilityDistribution getNoiseGenerator() {
        return noiseGenerator;
    }

    @Override
    public void setNoiseGenerator(final ProbabilityDistribution noise) {
        this.noiseGenerator = noise;
    }

    public boolean getAddNoise() {
        return addNoise;
    }

    public void setAddNoise(final boolean addNoise) {
        this.addNoise = addNoise;
    }

    public double getPhase() {
        return phase;
    }

    public void setPhase(final double phase) {
        this.phase = phase;
    }

    public double getFrequency() {
        return frequency;
    }

    public void setFrequency(final double frequency) {
        this.frequency = frequency;
    }

    @Override
    public String getName() {
        return "Sinusoidal";
    }

    @Override
    public void setUpperBound(double ceiling) {
        this.ceiling = ceiling;
    }

    @Override
    public void setLowerBound(double floor) {
        this.floor = floor;
    }

    @Override
    public double getRandomValue() {
        double rand = (2 * Math.PI) * Math.random();
        double range = getUpperBound() - getLowerBound();
        return ((range / 2) * Math.sin(frequency * rand + phase)) + ((getUpperBound() + getLowerBound()) / 2);
    }

    @Override
    public double getUpperBound() {
        return ceiling;
    }

    @Override
    public double getLowerBound() {
        return floor;
    }

    @Override
    public boolean isClipped() {
        return isClipped;
    }

    @Override
    public void setClipped(boolean clipped) {
        isClipped = clipped;
    }
}

package org.simbrain.util.math.ProbDistributions;

import org.simbrain.util.UserParameter;
import org.simbrain.util.math.ProbabilityDistribution;

public class NormalDistribution extends ProbabilityDistribution {

    @UserParameter(
            label = "Mean (\u03BC)",
            description = "The expected value or center of the distribution.",
            order = 1)
    private double mean = 1.0;

    @UserParameter(
            label = "Std. Dev. (\u03C3)",
            description = "The average squared distance from the mean.",
            order = 2)
    private double standardDeviation = 0.5;

    /**
     * For all but uniform, upper bound is only used in conjunction with
     * clipping, to truncate the distribution. So if clipping is false this
     * value is not used.
     */
    @UserParameter(
            label = "Floor",
            description = "An artificial minimum value set by the user.",
            order = 3)
    private double floor = Double.NEGATIVE_INFINITY;

    /**
     * For all but uniform, lower bound is only used in conjunction with
     * clipping, to truncate the distribution. So if clipping is false this
     * value is not used.
     */
    @UserParameter(
            label = "Ceiling",
            description = "An artificial minimum value set by the user.",
            order = 4)
    private double ceil = Double.POSITIVE_INFINITY;

    @UserParameter(
            label = "Clipping",
            description = "When clipping is enabled, the randomizer will reject outside the floor and ceiling values.",
            order = 5)
    private boolean clipping = false;

    /**
     * Backing for this distribution.
     */
    private org.apache.commons.math3.distribution.NormalDistribution dist;


    /**
     * Public constructor for reflection-based creation. You are encouraged to use
     * the builder pattern provided for ProbabilityDistributions.
     */
    public NormalDistribution() {
        dist = new org.apache.commons.math3.distribution.NormalDistribution(randomGenerator, mean, standardDeviation);
    }

    /**
     * Create a normal dist with specified mean and stdev
     */
    public NormalDistribution(double mean, double stdev) {
        dist = new org.apache.commons.math3.distribution.NormalDistribution(randomGenerator, mean, stdev);
        this.mean = mean;
        this.standardDeviation = stdev;
    }

    public NormalDistribution(double mean, double stdev, double floor, double ceil) {
        dist = new org.apache.commons.math3.distribution.NormalDistribution(randomGenerator, mean, stdev);
        this.mean = mean;
        this.standardDeviation = stdev;
        this.floor = floor;
        this.ceil = ceil;
    }

    public double nextDouble() {
        return clipping(this, dist.sample(), floor, ceil);
    }

    public int nextInt() {
        return (int) nextDouble();
    }

    public double getMean() {
        return mean;
    }

    public void setMean(double mean) {
        this.mean = mean;
        dist = new org.apache.commons.math3.distribution.NormalDistribution(randomGenerator, mean, standardDeviation);
    }

    public double getStandardDeviation() {
        return standardDeviation;
    }

    public void setStandardDeviation(double standardDeviation) {
        this.standardDeviation = standardDeviation;
        dist = new org.apache.commons.math3.distribution.NormalDistribution(randomGenerator, mean, standardDeviation);
    }

    public String getName() {
        return "Normal";
    }

    @Override
    public String toString() {
        return "Normal";
    }

    @Override
    public NormalDistribution deepCopy() {
        NormalDistribution cpy = new NormalDistribution();
        cpy.mean = this.mean;
        cpy.standardDeviation = this.standardDeviation;
        cpy.ceil = this.ceil;
        cpy.floor = this.floor;
        cpy.clipping = this.clipping;
        return cpy;
    }

    @Override
    public void setClipping(boolean clipping) {
        this.clipping = clipping;
    }

    @Override
    public void setUpperBound(double ceiling) {
        this.ceil = ceiling;
    }

    @Override
    public void setLowerBound(double floor) {
        this.floor = floor;
    }

    public static NormalDistributionBuilder builder() {
        return new NormalDistributionBuilder();
    }

    public static NormalDistribution create() {
        return new NormalDistribution();
    }

    public static class NormalDistributionBuilder
        extends ProbabilityDistributionBuilder<
            NormalDistributionBuilder,
            NormalDistribution> {

        NormalDistribution product = new NormalDistribution();

        public NormalDistributionBuilder mean(double mean) {
            product.setMean(mean);
            return this;
        }

        public NormalDistributionBuilder standardDeviation(double standardDeviation) {
            product.setStandardDeviation(standardDeviation);
            return this;
        }

        @Override
        public NormalDistribution build() {
            return product;
        }

        @Override
        protected NormalDistribution product() {
            return product;
        }
    }

}
package org.simbrain.custom_sims.simulations.sorn;

import org.simbrain.network.core.Neuron;
import org.simbrain.network.neuron_update_rules.SpikingThresholdRule;
import org.simbrain.network.updaterules.interfaces.NoisyUpdateRule;
import org.simbrain.network.util.ScalarDataHolder;
import org.simbrain.util.stats.ProbabilityDistribution;
import org.simbrain.util.stats.distributions.NormalDistribution;

/**
 * An implementation of the specific type of threshold neuron used in Lazar,
 * Pipa, & Triesch (2009).
 *
 * @author Zoë Tosi
 *
 */
public class SORNNeuronRule extends SpikingThresholdRule implements
        NoisyUpdateRule {

    /** The noise generating randomizer. */
    private ProbabilityDistribution noiseGenerator = new NormalDistribution(0, .05);

    /** Whether or not to add noise to the inputs . */
    private boolean addNoise;

    /** The target rate. */
    private double hIP = 0.01;

    /** The learning rate for homeostatic plasticity. */
    private double etaIP = 0.001;

    /** The maximum value the threshold is allowed to take on. */
    private double maxThreshold = 1;

    private double refractoryPeriod = 0;

    @Override
    public SORNNeuronRule deepCopy() {
        SORNNeuronRule snr = new SORNNeuronRule();
        snr.setAddNoise(addNoise);
        snr.setNoiseGenerator(noiseGenerator);
        snr.setEtaIP(etaIP);
        snr.sethIP(hIP);
        snr.setMaxThreshold(maxThreshold);
        snr.setThreshold(getThreshold());
        snr.setRefractoryPeriod(getRefractoryPeriod());
        return snr;
    }

    @Override
    public void apply(Neuron neuron, ScalarDataHolder data) {
        // Synaptic Normalization
        SORN.normalizeExcitatoryFanIn(neuron);
        // Sum inputs including noise and applied (external) inputs
        double input = neuron.getInput()
                + (addNoise ? noiseGenerator.sampleDouble() : 0);
        // TODO: There used to be "applied input here" but it is no longer used
        // Check that we're not still in the refractory period
        boolean outOfRef = neuron.getNetwork().getTime()
            > neuron.getLastSpikeTime()+refractoryPeriod;
        // We fire a spike if input exceeds threshold and we're
        // not in the refractory period
        boolean spk = outOfRef && (input >= getThreshold());
        neuron.setSpike(spk);
        neuron.setActivation(2*(input-getThreshold()));
        plasticUpdate(neuron);
    }

    /**
     * Homeostatic plasticity of the default SORN network. {@inheritDoc}
     */
    public void plasticUpdate(Neuron neuron) {
        setThreshold(getThreshold() + (etaIP * ((neuron.isSpike()?1:0) - hIP)));
//        if (getThreshold() > maxThreshold) {
//            setThreshold(maxThreshold);
//        }
    }

    @Override
    public ProbabilityDistribution getNoiseGenerator() {
        return noiseGenerator;
    }

    @Override
    public void setNoiseGenerator(ProbabilityDistribution rand) {
        noiseGenerator = rand.deepCopy();
    }

    @Override
    public boolean getAddNoise() {
        return addNoise;
    }

    @Override
    public void setAddNoise(boolean noise) {
        this.addNoise = noise;
    }

    public double getMaxThreshold() {
        return maxThreshold;
    }

    public void setMaxThreshold(double maxThreshold) {
        this.maxThreshold = maxThreshold;
    }

    public double gethIP() {
        return hIP;
    }

    public void sethIP(double hIP) {
        this.hIP = hIP;
    }

    public double getEtaIP() {
        return etaIP;
    }

    public void setEtaIP(double etaIP) {
        this.etaIP = etaIP;
    }

    public void setRefractoryPeriod(double refP) {
        this.refractoryPeriod = refP;
    }

    public double getRefractoryPeriod() {
        return refractoryPeriod;
    }

}

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
package org.simbrain.network.gui.dialogs

import org.simbrain.network.connections.RadialProbabilistic
import org.simbrain.network.core.Network
import org.simbrain.network.core.Neuron
import org.simbrain.network.core.Synapse
import org.simbrain.plot.histogram.HistogramModel
import org.simbrain.plot.histogram.HistogramPanel
import org.simbrain.util.LabelledItemPanel
import org.simbrain.util.SimbrainConstants.Polarity
import org.simbrain.util.displayInDialog
import org.simbrain.util.math.ProbDistributions.UniformDistribution
import org.simbrain.util.math.ProbabilityDistribution
import org.simbrain.util.math.ProbabilityDistribution.Randomizer
import org.simbrain.util.math.SimbrainMath
import org.simbrain.util.propertyeditor.AnnotatedPropertyEditor
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.GridLayout
import java.awt.Insets
import java.util.*
import javax.swing.*

/**
 * Panel for editing collections of synapses.
 *
 * TODO: In need of some optimizations eventually... possibly after 3.0. Suggestions for this: Don't allow polarity
 * shifts and keep separate lists of excitatory/inhibitory synapses, also allow to preview and keep values in
 * numerical array and THEN change synapse strengths.
 *
 * @author Zoë Tosi
 * @author Jeff Yoshimi
 */
class SynapseAdjustmentPanel(val synapses: List<Synapse>) : JPanel() {

    /**
     * Random source for randomizing inhibitory synapses.
     */
    private val inhibitoryRandomizer: ProbabilityDistribution = UniformDistribution.builder()
        .polarity(Polarity.INHIBITORY)
        .build()

    /**
     * Random source for randomizing excitatory synapses.
     */
    private val excitatoryRandomizer: ProbabilityDistribution = UniformDistribution.builder()
        .polarity(Polarity.EXCITATORY)
        .build()

    // TODO: Nonpolar randomizer?

    /**
     * A collection of the selected synaptic weights, such that the first row
     * represents excitatory weights and the 2nd row represents inhibitory
     * weights. All inhibitory weights are stored as their absolute value. Note
     * that this array is only used internally, to display stats and the
     * histogram.
     */
    private val weights = arrayOfNulls<DoubleArray>(2)

    private val chooseRandomizer = Randomizer() // To select current randomzier
    private val chooseRandomizerPanel = AnnotatedPropertyEditor(chooseRandomizer)
    private val excitatoryPanel = AnnotatedPropertyEditor(excitatoryRandomizer)
    private val inhibitoryPanel = AnnotatedPropertyEditor(inhibitoryRandomizer)
    private val randomizeButton = JButton("Apply")

    private val perturber: ProbabilityDistribution = UniformDistribution.create()
    private val perturberRandomizer = Randomizer()
    private val perturberPanel = AnnotatedPropertyEditor(perturberRandomizer)
    private val perturbButton = JButton("Apply")

    /**
     * A combo box for selecting which kind of synapses should have their stats
     * displayed and/or what kind of display.
     */
    private val synTypeSelector = JComboBox(SynapseView.values())

    /**
     * Calculates some basic statistics about the private final StatisticsBlock
     */
    var statCalculator = StatisticsBlock()

    /**
     * A histogram plotting the strength of synapses over given intervals (bins)
     * against their frequency.
     */
    private val histogramPanel = HistogramPanel(HistogramModel(2))

    /**
     * A panel displaying basic statistics about the synapses, including: number
     * of synapses, number of inhibitory and excitatory synapses, and mean,
     * median, and standard deviation of the strengths of selected type of
     * synapses.
     */
    private val statsPanel = JPanel()

    private val meanLabel = JLabel()
    private val medianLabel = JLabel()
    private val sdLabel = JLabel()
    private val numSynsLabel = JLabel()
    private val numExSynsLabel = JLabel()
    private val numInSynsLabel = JLabel()

    init {

        // Extract weight values in usable form by internal methods
        extractWeightValues(synapses)

        perturberRandomizer.probabilityDistribution = perturber
        inhibitoryRandomizer.setUpperBound(0.0)
        excitatoryRandomizer.setLowerBound(0.0)
        histogramPanel.setxAxisName("Synapse Strength")
        histogramPanel.setyAxisName("# of Synapses")

        layout = GridBagLayout()
        val synTypePanel = JPanel().apply {
            border = BorderFactory.createTitledBorder("Synapse" + " Stats")
            layout = GridLayout(3, 2)
            add(numSynsLabel)
            add(meanLabel)
            add(numExSynsLabel)
            add(medianLabel)
            add(numInSynsLabel)
            add(sdLabel)
        }
        val gbc = GridBagConstraints().apply {
            weightx = 1.0
            weighty = 0.0
            fill = GridBagConstraints.HORIZONTAL
            insets = Insets(5, 5, 5, 5)
            anchor = GridBagConstraints.NORTHWEST
            gridx = 0
            gridy = 0
            gridwidth = HistogramPanel.GRID_WIDTH - 1
            gridheight = 1
        }
        this.add(synTypePanel, gbc)
        gbc.apply {
            gridwidth = 1
            anchor = GridBagConstraints.CENTER
            gridx = HistogramPanel.GRID_WIDTH - 1
        }
        this.add(synTypeSelector, gbc)
        gbc.apply {
            weighty = 1.0
            fill = GridBagConstraints.BOTH
            anchor = GridBagConstraints.NORTHWEST
            gridwidth = HistogramPanel.GRID_WIDTH
            gridheight = HistogramPanel.GRID_HEIGHT
            gridy = 1
            gridx = 0
        }
        this.add(histogramPanel, gbc)
        gbc.gridy += HistogramPanel.GRID_HEIGHT
        gbc.gridheight = 1
        val bottomPanel = JTabbedPane()
        val randTab = JPanel()
        val perturbTab = JPanel()
        val prunerTab = JPanel()
        val scalerTab = JPanel()
        randTab.layout = GridBagLayout()
        perturbTab.layout = GridBagLayout()
        val c = GridBagConstraints().apply {
            gridwidth = 2
            gridx = 0
            gridy = 0
            weightx = 1.0
            weighty = 0.0
            fill = GridBagConstraints.HORIZONTAL
            anchor = GridBagConstraints.CENTER
        }
        randTab.add(chooseRandomizerPanel, c)
        perturbTab.add(perturberPanel, c)
        scalerTab.add(SynapseScalerPanel(), c)
        prunerTab.add(PrunerPanel(), c)
        c.apply {
            gridwidth = 1
            gridx = 1
            gridy = 1
            weightx = 0.0
            weighty = 0.0
            fill = GridBagConstraints.NONE
            anchor = GridBagConstraints.NORTHEAST
            insets = Insets(5, 0, 5, 10)
        }
        randTab.add(randomizeButton, c)
        perturbTab.add(perturbButton, c)
        bottomPanel.apply {
            addTab("Randomizer", randTab)
            addTab("Perturber", perturbTab)
            addTab("Pruner", prunerTab)
            addTab("Scaler", scalerTab)
        }
        this.add(bottomPanel, gbc)

        updateStats()
        updateHistogram()
        addActionListeners()
    }

    /**
     * Adds all the action listeners to the panel. Currently includes listeners
     * for: The perturb button, randomize button, and the synapse kind selector
     * combo box.
     */
    fun addActionListeners() {
        perturbButton.addActionListener {
            chooseRandomizerPanel.commitChanges()
            val view = synTypeSelector.selectedItem as SynapseView
            for (synapse in synapses) {
                if (view.synapseIsAdjustable(synapse)) {
                    synapse.forceSetStrength(synapse.strength + perturber.random)
                }
            }
            fullUpdate()
        }
        randomizeButton.addActionListener {
            chooseRandomizerPanel.commitChanges()
            val view = synTypeSelector.selectedItem as SynapseView
            // Set the randomizer
            // TODO: Deal with changes in polarity...or at leat allow non polar randomization
            when (view) {
                SynapseView.ALL -> {
                    excitatoryPanel.commitChanges()
                    inhibitoryPanel.commitChanges()
                }
                SynapseView.OVERLAY -> {
                    excitatoryPanel.commitChanges()
                    inhibitoryPanel.commitChanges()
                }
                SynapseView.INHIBITORY -> inhibitoryPanel.commitChanges()
                SynapseView.EXCITATORY -> excitatoryPanel.commitChanges()
            }
            // Randomize synapses appropriately
            synapses.filter { s -> view.synapseIsAdjustable(s) }.forEach { s ->
                when (view) {
                    SynapseView.ALL -> s.forceSetStrength(chooseRandomizer.random)
                    SynapseView.OVERLAY -> {
                        if (SynapseView.INHIBITORY.synapseIsAdjustable(s)) {
                            s.forceSetStrength(inhibitoryRandomizer.random)
                        }
                        if (SynapseView.EXCITATORY.synapseIsAdjustable(s)) {
                            s.forceSetStrength(excitatoryRandomizer.random)
                        }
                    }
                    SynapseView.EXCITATORY -> s.forceSetStrength(excitatoryRandomizer.random)
                    SynapseView.INHIBITORY -> s.forceSetStrength(inhibitoryRandomizer.random)
                }
            }
            // Update the histogram, stats panel, etc
            fullUpdate()
        }
        // Show stats and histogram only for selected type(s)...
        synTypeSelector.addActionListener {
            updateHistogram()
            updateStats()
            parent.revalidate()
            parent.repaint()
            when (synTypeSelector.selectedItem as SynapseView) {
                SynapseView.ALL -> {
                    excitatoryPanel.fillFieldValues()
                    inhibitoryPanel.fillFieldValues()
                }
                SynapseView.OVERLAY -> {
                    excitatoryPanel.fillFieldValues()
                    inhibitoryPanel.fillFieldValues()
                }
                SynapseView.INHIBITORY -> {
                    inhibitoryPanel.fillFieldValues()
                }
                SynapseView.EXCITATORY -> {
                    excitatoryPanel.fillFieldValues()
                }
            }
        }
    }

    /**
     * Extracts weight values and organizes them by synapse type (inhibitory or
     * excitatory). Inhibitory values are represented by their absolute value.
     */
    private fun extractWeightValues(synapses: List<Synapse>) {
        var exWeights = 0
        var inWeights = 0

        // TODO: Get rid of this... replace with separate arraylists that
        // are preallocated. It should actually be more efficient.

        // Inefficient but necessary due to lack of support for collections of
        // primitive types.
        for (s in synapses) {
            val w = s.strength
            if (w > 0) {
                exWeights++
            } else {
                inWeights++
            }
        }
        weights[0] = DoubleArray(exWeights)
        weights[1] = DoubleArray(inWeights)
        exWeights = 0
        inWeights = 0
        if (weights[0]!!.isNotEmpty()) {
            // Inefficient but necessary due to lack of support for collections
            // of
            // primitive types.
            for (s in synapses) {
                val w = s.strength
                if (w > 0) {
                    weights[0]!![exWeights++] = w
                } else {
                    weights[1]!![inWeights++] = w
                }
            }
        }
    }

    /**
     * Fully updates the histogram based on the status of the synapses in
     * question.
     */
    private fun fullUpdate() {
        extractWeightValues(synapses)
        updateHistogram()
        updateStats()
        parent.revalidate()
        parent.repaint()
    }

    /**
     * Updates the histogram based on the selected synapses and selected
     * options. Can plot combined excitatory and absolute inhibitory, overlaid
     * excitatory/absolute inhibitory, only excitatory, or only inhibitory.
     * Histogram must be initialized prior to invocation. Red is used to
     * represent excitatory values, blue is used for inhibitory.
     */
    fun updateHistogram() {
        val data: MutableList<DoubleArray?> = ArrayList()
        val names: MutableList<String> = ArrayList()
        when (synTypeSelector.selectedItem as SynapseView) {
            SynapseView.ALL -> {
                run {

                    // Send the histogram the excitatory and absolute inhibitory
                    // synapse values as separate data series.
                    val hist1 = weights[0]
                    val hist2 = weights[1]
                    // The names of both series
                    names.add(SynapseView.EXCITATORY.toString())
                    names.add(SynapseView.INHIBITORY.toString())
                    data.add(hist1)
                    data.add(hist2)
                }
            }
            SynapseView.OVERLAY -> {
                run {

                    // Send the histogram the excitatory and absolute inhibitory
                    // synapse values as separate data series.
                    val hist1 = weights[0]
                    val hist2 = DoubleArray(weights[1]!!.size)
                    var i = 0
                    val n = hist2.size
                    while (i < n) {
                        hist2[i] = Math.abs(weights[1]!![i])
                        i++
                    }
                    // The names of both series
                    names.add(SynapseView.EXCITATORY.toString())
                    names.add(SynapseView.INHIBITORY.toString())
                    data.add(hist1)
                    data.add(hist2)
                }
            }
            SynapseView.EXCITATORY -> {
                run {

                    // Send the histogram only excitatory weights as a single series
                    val hist = weights[0]
                    // Name the series
                    names.add(SynapseView.EXCITATORY.toString())
                    data.add(hist)
                }
            }
            SynapseView.INHIBITORY -> {
                run {

                    // Send the histogram only inhibitory weights as a single series
                    val hist = weights[1]
                    // Name the series
                    names.add(SynapseView.INHIBITORY.toString())
                    data.add(hist)
                }
            }
            else -> {
                throw IllegalArgumentException("Invalid Synapse" + " Selection.")
            }
        }

        // Send the histogram the new data and re-draw it.
        histogramPanel.model.resetData(data, names)
        histogramPanel.model.setSeriesColor(SynapseView.ALL.toString(), HistogramPanel.getDefault_Pallet()[0])
        histogramPanel.model.setSeriesColor(SynapseView.EXCITATORY.toString(), HistogramPanel.getDefault_Pallet()[0])
        histogramPanel.model.setSeriesColor(SynapseView.INHIBITORY.toString(), HistogramPanel.getDefault_Pallet()[1])
        histogramPanel.reRender()
    }

    /**
     * Updates the values in the stats panel (number of synapses, excitatory
     * synapses, inhibitory synapses, and mean, median and standard deviation of
     * selected synapses. Extract data should be used prior to this.
     */
    fun updateStats() {
        statCalculator.calcStats()
        meanLabel.text = "Mean: " + SimbrainMath.roundDouble(statCalculator.mean, 5)
        medianLabel.text = "Median: " + SimbrainMath.roundDouble(statCalculator.median, 5)
        sdLabel.text = "Std. Dev: " + SimbrainMath.roundDouble(statCalculator.stdDev, 5)
        val tot = weights[0]!!.size + weights[1]!!.size
        numSynsLabel.text = "Synapses: " + Integer.toString(tot)
        numExSynsLabel.text = "Excitatory : " + Integer.toString(weights[0]!!.size)
        numInSynsLabel.text = "Inhibitory: " + Integer.toString(weights[1]!!.size)
        statsPanel.revalidate()
        statsPanel.repaint()
    }

    /**
     * @author Zoë
     */
    inner class StatisticsBlock {
        var mean = 0.0
            private set
        var median = 0.0
            private set
        var stdDev = 0.0
            private set

        /**
         * Gets the basic statistics: mean, median, and standard deviation of
         * the synapse weights based on which group of synapses is selected.
         *
         * @return an An array where the first element is the mean, the 2nd
         * element is the median, and the 3rd element is the standard
         * deviation.
         */
        fun calcStats() {
            var data: DoubleArray? = null
            var tot = 0
            val type = synTypeSelector.selectedItem as SynapseView
            var runningVal = 0.0
            if (weights[0]!!.size == 0 && weights[1]!!.size == 0) {
                return
            }

            // Determine selected type(s) and collect data accordingly...
            if (type == SynapseView.ALL) {
                tot = weights[0]!!.size + weights[1]!!.size
                data = DoubleArray(tot)
                var c = 0
                for (i in 0..1) {
                    var j = 0
                    val m = weights[i]!!.size
                    while (j < m) {
                        val `val` = weights[i]!![j]
                        runningVal += `val`
                        data[c] = `val`
                        c++
                        j++
                    }
                }
            } else if (type == SynapseView.OVERLAY) {
                tot = weights[0]!!.size + weights[1]!!.size
                data = DoubleArray(tot)
                var c = 0
                for (i in 0..1) {
                    var j = 0
                    val m = weights[i]!!.size
                    while (j < m) {
                        val `val` = Math.abs(weights[i]!![j])
                        runningVal += `val`
                        data[c] = `val`
                        c++
                        j++
                    }
                }
            } else if (type == SynapseView.EXCITATORY && weights[0]!!.size != 0) {
                tot = weights[0]!!.size
                data = DoubleArray(tot)
                for (j in 0 until tot) {
                    val `val` = Math.abs(weights[0]!![j])
                    runningVal += `val`
                    data[j] = `val`
                }
            } else if (type == SynapseView.INHIBITORY && weights[1]!!.size != 0) {
                tot = weights[1]!!.size
                data = DoubleArray(tot)
                for (j in 0 until tot) {
                    val `val` = weights[1]!![j]
                    runningVal += `val`
                    data[j] = `val`
                }
            }
            if (data != null) {
                mean = runningVal / tot
                Arrays.sort(data)
                median = if (tot % 2 == 0) {
                    (data[tot / 2] + data[tot / 2 - 1]) / 2
                } else {
                    data[Math.floor((tot / 2).toDouble()).toInt()]
                }
                runningVal = 0.0
                for (i in 0 until tot) {
                    runningVal += Math.pow(mean - data[i], 2.0)
                }
                runningVal /= tot
                stdDev = Math.sqrt(runningVal)
            }
        }
    }

    /**
     * Panel for scaling synapses.
     */
    inner class SynapseScalerPanel() : LabelledItemPanel() {
        /**
         * Percentage to increase or decrease indicated synapses.
         */
        private val tfIncreaseDecrease = JTextField(".1")
        private val increaseButton = JButton("Increase")
        private val decreaseButton = JButton("Decrease")

        init {
            addItem("Percent to change", tfIncreaseDecrease)
            addItem("Increase", increaseButton)
            increaseButton.addActionListener {
                val amount = tfIncreaseDecrease.text.toDouble()
                val view = synTypeSelector.selectedItem as SynapseView
                for (synapse in synapses) {
                    if (view.synapseIsAdjustable(synapse)) {
                        synapse.forceSetStrength(synapse.strength + synapse.strength * amount)
                    }
                }
                fullUpdate()
            }
            addItem("Decrease", decreaseButton)
            decreaseButton.addActionListener {
                val amount = tfIncreaseDecrease.text.toDouble()
                val view = synTypeSelector.selectedItem as SynapseView
                for (synapse in synapses) {
                    if (view.synapseIsAdjustable(synapse)) {
                        synapse.forceSetStrength(synapse.strength - synapse.strength * amount)
                    }
                }
                fullUpdate()
            }
        }
    }

    /**
     * Panel for pruning synapses.
     */
    inner class PrunerPanel() : LabelledItemPanel() {
        /**
         * Threshold. If synapse strength above absolute value of this value
         * prune the synapse when the prune button is pressed.
         */
        private val tfThreshold = JTextField(".1")

        init {
            val pruneButton = JButton("Prune")
            addItem("Prune", pruneButton)
            addItem("Threshold", tfThreshold)
            pruneButton.addActionListener {
                val threshold = tfThreshold.text.toDouble()
                val view = synTypeSelector.selectedItem as SynapseView
                for (synapse in synapses) {
                    if (view.synapseIsAdjustable(synapse)) {
                        if (Math.abs(synapse.strength) < threshold) {
                            synapse.delete()
                        }
                    }
                }
                fullUpdate()
            }
        }
    }

    enum class SynapseView {
        ALL {
            override fun toString(): String {
                return "All"
            }

            override fun synapseIsAdjustable(s: Synapse): Boolean {
                return true
            }
        },
        OVERLAY {
            override fun toString(): String {
                return "Overlay"
            }

            override fun synapseIsAdjustable(s: Synapse): Boolean {
                return true
            }
        },
        EXCITATORY {
            override fun toString(): String {
                return "Excitatory"
            }

            override fun synapseIsAdjustable(s: Synapse): Boolean {
                return s.strength >= 0
            }
        },
        INHIBITORY {
            override fun toString(): String {
                return "Inhibitory"
            }

            override fun synapseIsAdjustable(s: Synapse): Boolean {
                return s.strength < 0
            }
        };

        abstract fun synapseIsAdjustable(s: Synapse): Boolean
    }
}

fun createSynapseAdjustmentPanel(synapses: List<Synapse>): SynapseAdjustmentPanel? {
    val sap = SynapseAdjustmentPanel(synapses)
    if (synapses.isEmpty()) {
        JOptionPane.showMessageDialog(null, "No synapses to display", "Warning",
            JOptionPane.WARNING_MESSAGE);
        return null
    }
    return sap
}

fun main() {

    val net = Network()
    val neurons = (0..20).map { Neuron(net) }
    // val neurons = mutableListOf<Neuron>() // To test empty list case
    val conn = RadialProbabilistic()
    val syns = conn.connectNeurons(net, neurons, neurons)
    createSynapseAdjustmentPanel(syns)?.displayInDialog()
}
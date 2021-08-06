package org.simbrain.network.gui.nodes

import net.miginfocom.swing.MigLayout
import org.piccolo2d.nodes.PPath
import org.piccolo2d.nodes.PText
import org.piccolo2d.util.PBounds
import org.simbrain.network.NetworkComponent
import org.simbrain.network.NetworkModel
import org.simbrain.network.gui.NetworkPanel
import org.simbrain.network.gui.dialogs.DataPanel
import org.simbrain.network.smile.SmileClassifier
import org.simbrain.network.smile.classifiers.SVMClassifier
import org.simbrain.util.*
import org.simbrain.util.propertyeditor.AnnotatedPropertyEditor
import org.simbrain.util.table.NumericTable
import java.awt.Dialog.ModalityType
import java.awt.geom.Point2D
import java.awt.geom.Rectangle2D
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JToolBar

class SmileClassifierNode(val np: NetworkPanel, val smileClassifier: SmileClassifier) : ScreenElement(np) {

    private val initialWidth = 200.0
    private val initialHeight = 100.0

    /**
     * Square shape around array node.
     */
    private val borderBox = PPath.createRectangle(0.0, 0.0, initialWidth, initialHeight).also {
        addChild(it)
        pickable = true
    }

    /**
     * Text showing info about the array.
     */
    private val infoText = PText().also {
        it.setFont(NeuronArrayNode.INFO_FONT)
        addChild(it)
        it.offset(8.0, 8.0)
    }

    init {

        smileClassifier.events.apply {
            onDeleted { removeFromParent() }
            onUpdated {
                updateInfoText()
            }
            onLocationChange { pullViewPositionFromModel() }
        }
        updateInfoText()
        pushViewPositionToModel()
    }

    fun pullViewPositionFromModel() {
        val point: Point2D = smileClassifier.location.minus(Point2D.Double(width / 2, height / 2))
        this.globalTranslation = point
    }

    override fun offset(dx: kotlin.Double, dy: kotlin.Double) {
        pushViewPositionToModel()
        super.offset(dx, dy)
    }

    fun pushViewPositionToModel() {
        val p = this.globalTranslation
        smileClassifier.location = point(p.x + width / 2, p.y + height / 2)
    }


    /**
     * Update status text.
     */
    private fun updateInfoText() {
        infoText.text = "Output: (" +
                Utils.doubleArrayToString(smileClassifier.outputs.col(0), 2) + ")" +
                "\n\nInput: (" + Utils.doubleArrayToString(smileClassifier.inputs.col(0), 2) + ")"
        val pb: PBounds = infoText.bounds
        // Sets border box to size of text
        borderBox.setBounds(pb.x - 5, pb.y - 5, pb.width + 20, pb.height + 20)
        setBounds(borderBox.bounds)
    }

    override fun setBounds(newBounds: Rectangle2D?): Boolean {
        smileClassifier.bounds = newBounds as Rectangle2D.Double?
        return super.setBounds(newBounds)
    }

    override fun getModel(): NetworkModel {
        return smileClassifier
    }

    override fun isSelectable(): Boolean {
        return true
    }

    override fun isDraggable(): Boolean {
        return true
    }

    override fun acceptsSourceHandle(): Boolean {
        return true
    }

    override fun getPropertyDialog() = StandardDialog().apply {

        // TODO: Move?

        title = "Smile Classifier"
        modalityType = ModalityType.MODELESS // Set to modeless so the dialog can be left open

        val mainPanel = JPanel()
        val statsLabel = JLabel("Score:")
        contentPane = mainPanel
        mainPanel.apply {

            layout = MigLayout()

            add(AnnotatedPropertyEditor(smileClassifier), "wrap")

            // Data Panels
            val inputs = DataPanel().apply {
                table.setData(smileClassifier.trainingInputs)
                // events.onApply { data -> println(data.contentDeepToString()) }
                addClosingTask {
                    applyData()
                    smileClassifier.trainingInputs = this.table.as2DDoubleArray()
                }
            }
            val targets = DataPanel().apply {
                table.setData(smileClassifier.targets.map { doubleArrayOf(it.toDouble()) }.toTypedArray())
                addClosingTask {
                    applyData()
                    smileClassifier.targets = this.table.as2DDoubleArray().map { it[0].toInt() }.toIntArray();
                }
            }


            val toolbar = JToolBar().apply {
                // Add row
                add(JButton().apply {
                    icon = ResourceManager.getImageIcon("menu_icons/AddTableRow.png")
                    toolTipText = "Insert a row"
                    addActionListener {
                        inputs.table.insertRow(inputs.jTable.selectedRow)
                        targets.table.insertRow(inputs.jTable.selectedRow)
                    }
                })
                // Delete row
                // TODO: Delete selected rows. For that abstract out table code
                add(JButton().apply {
                    icon = ResourceManager.getImageIcon("menu_icons/DeleteRowTable.png")
                    toolTipText = "Delete last row"
                    addActionListener {
                        inputs.table.removeRow(inputs.jTable.rowCount - 1)
                        targets.table.removeRow(targets.jTable.rowCount - 1)
                    }
                })
            }
            add(toolbar, "wrap")

            // Stats label
            add(statsLabel, "wrap")

            // Training Button
            add(JButton("Train").apply {
                addActionListener {
                    smileClassifier.train(inputs.table.as2DDoubleArray(), targets.table.firstColumnAsIntArray())
                    statsLabel.text = "Stats: " + smileClassifier.classifier.stats
                }
            }, "wrap")

            // Add the data panels
            add(inputs, "growx")
            add(targets, "growx")
        }

    }

}

fun NumericTable.firstColumnAsIntArray(): IntArray {
    val returnList = IntArray(rowCount)
    for (i in 0 until rowCount) {
        returnList[i] = this.getLogicalValueAt(i, 0).toInt()
    }
    return returnList
}

fun main() {
    val networkComponent = NetworkComponent("net 1")
    val np = NetworkPanel(networkComponent)
    val classifier = with(networkComponent.network) {
        val classifier = SmileClassifier(this, SVMClassifier(), 2, 1, 4)
        addNetworkModel(classifier)
        classifier
    }
    SmileClassifierNode(np, classifier).propertyDialog.run { makeVisible() }
}
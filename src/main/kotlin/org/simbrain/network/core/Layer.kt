package org.simbrain.network.core

import org.simbrain.network.LocatableModel
import org.simbrain.network.events.LocationEvents2
import org.simbrain.util.toDoubleArray
import org.simbrain.workspace.AttributeContainer
import org.simbrain.workspace.Producible
import smile.math.matrix.Matrix
import java.awt.geom.Point2D
import java.awt.geom.Rectangle2D

/**
 * Superclass for network models involved in, roughly speaking, array based computations. Simbrain layers are connected
 * to each other by [Connector]s. Subclassses include neuron arrays, collections of neurons, and deep networks,
 * backed by different data structures, including java arrays, Smile Matrices, and Tensor Flow tensors.
 * <br></br>
 * This class maintains connectors, id, and events, etc.
 * <br></br>
 * Input and output functions must be provided in Smile matrix format to support communication between different types
 * of layer. Smile matrices are the "lingua franca" of layers.
 * <br></br>
 * However, specific subclasses can communicate in other ways to support fast or custom communication. For example,
 * tensor-based layers and connectors could communicate using tensor flow operations, or specialized layers with
 * multiple arrays could be joined by special connectors making use of those arrays.
 *
 * @author Jeff Yoshimi
 * @author Yulin Li
 */
abstract class Layer : LocatableModel(), AttributeContainer {
    // TODO: Currently Smile Matrices are the "lingua Franca" for different layers.
    //  Keep an eye on Kotlin's Multik as a possible alternative
    /**
     * "Fan-in" of incoming connectors.
     */
    val incomingConnectors: MutableList<Connector> = ArrayList()

    /**
     * "Fan-out" of outgoing connectors.
     */
    val outgoingConnectors: MutableList<Connector> = ArrayList()

    /**
     * Collects inputs from other network models using arrays. Cannot be set directly. Use [addInputs] instead.
     */
    abstract val inputs: Matrix

    /**
     * Add inputs to input vector. Performed in first pass of [org.simbrain.network.update_actions.BufferedUpdate]
     * Asynchronous buffered update assumes that inputs are aggregated in one pass then updated in a second pass.
     * Thus setting inputs directly is a dangerous operation and so is not allowed.
     */
    abstract fun addInputs(inputs: Matrix)

    /**
     * A column vector of output values. For "single-layer" layers this is activations. For multi-layer cases it is the
     * output layer.
     */
    abstract val outputs: Matrix

    @get:Producible
    val outputActivations: DoubleArray
        get() = outputs.toDoubleArray()

    /**
     * x coordinate of center of layer.
     */
    var x = 0.0
        private set

    /**
     * y coordinate of center of layer.
     */
    var y = 0.0
        private set

    /**
     * Width of layer. Mainly used by graphica arrows drawn to represent [Connector]s.
     */
    open var width = 0.0
        set(width) {
            field = width
            events.locationChanged.fireAndForget()
        }

    /**
     * Height of layer
     */
    open var height = 0.0
        set(height) {
            field = height
            events.locationChanged.fireAndForget()
        }

    /**
     * Event support.
     */
    @Transient
    override var events = LocationEvents2()
        protected set

    /**
     * Returns the output size for this layer.
     */
    abstract fun outputSize(): Int

    /**
     * Returns the input size for this layer.
     */
    abstract fun inputSize(): Int

    abstract val network: Network

    /**
     * Needed so arrow can be set correctly
     */
    abstract val bound: Rectangle2D

    fun addIncomingConnector(connector: Connector) {
        incomingConnectors.add(connector)
    }

    fun removeIncomingConnector(connector: Connector) {
        incomingConnectors.remove(connector)
    }

    fun addOutgoingConnector(connector: Connector) {
        outgoingConnectors.add(connector)
    }

    fun removeOutgoingConnector(connector: Connector) {
        outgoingConnectors.remove(connector)
    }

    override fun postOpenInit() {
        if (events == null) {
            events = LocationEvents2()
        }
    }

    override fun delete() {
        events.deleted.fireAndForget(this)
    }

    /**
     * See [org.simbrain.workspace.serialization.WorkspaceComponentDeserializer]
     */
    open fun readResolve(): Any? {
        events = LocationEvents2()
        return this
    }

    override var location: Point2D
        get() = Point2D.Double(x, y)
        set(location) {
            x = location.x
            y = location.y
            events.locationChanged.fireAndForget()
        }
}

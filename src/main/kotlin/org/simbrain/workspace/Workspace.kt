package org.simbrain.workspace

import kotlinx.coroutines.*
import org.pmw.tinylog.Logger
import org.simbrain.util.SimpleIdManager
import org.simbrain.util.Utils
import org.simbrain.workspace.couplings.Coupling
import org.simbrain.workspace.couplings.CouplingManager
import org.simbrain.workspace.events.WorkspaceEvents2
import org.simbrain.workspace.serialization.WorkspaceSerializer
import org.simbrain.workspace.updater.UpdateAction
import org.simbrain.workspace.updater.WorkspaceUpdater
import org.simbrain.workspace.updater.updateAction
import java.io.*
import java.util.*

/**
 * A collection of components which interact via couplings. Neural networks,
 * simulated environments, data-tables, plots and gauges are examples of
 * components in a Simbrain workspace. Essentially, an instance of a workspace
 * corresponds to a single simulation, that can be run with or without a
 * graphical view of it. The main visualization of a workspace is [ ].
 *
 * To create a new type of workspace component, extend [ ], and [DesktopComponent]. The
 * latter is a gui representation of the former. Follow the pattern in [ ] to register this mapping.  The workspace component
 * holds all the model objects, and manages couplings. Usually there is some
 * central model object, like [org.simbrain.world.odorworld.OdorWorld] or
 * [org.simbrain.network.core.Network] that the workspace component
 * creates and wraps.  The gui component is a [javax.swing.JPanel]  and
 * can either manage the graphics or (more typically) hold custom panels etc.
 * that do.
 *
 * De-serialization has a lot of steps, but the main things to be aware
 * of are to handle custom model deserializing in a readresolve method in the
 * main model object (e.g. Network or OdorWorld) and that if any special
 * graphical syncing is needed that it can be done the guicomponent constructor
 * by overriding [DesktopComponent.postAddInit]. Other init can happen in
 * overrides of [WorkspaceComponent.save] and in a
 * static open method that must also be created. An example is [org.simbrain.world.odorworld.OdorWorldComponent.open]
 * <br></br>
 * For instructions on setting up serialization see [WorkspaceSerializer].
 *
 * @author Jeff Yoshimi
 * @author Matt Watson
 * @author Tim Shea
 */
class Workspace: CoroutineScope {

    @Transient
    private var job = SupervisorJob()

    @Transient
    override var coroutineContext = Dispatchers.Default + job

    @Transient
    private val _componentList = ArrayList<WorkspaceComponent>()
    val componentList: List<WorkspaceComponent> get() = Collections.unmodifiableList(_componentList)

    /**
     * Component factory should be used to create new workspace and gui
     * components.
     */
    @Transient
    val componentFactory = AbstractComponentFactory(this)

    /**
     * Flag to indicate workspace has been changed since last save.
     */
    @Transient
    private var workspaceChanged = false

    /**
     * Current workspace file.
     */
    @Transient
    var currentFile: File? = null

    /**
     * A persistence representation of the time (the updater's state is not
     * persisted).
     */
    var savedTime = 0
        private set

    /**
     * Listeners on this workspace. The CopyOnWriteArrayList is not a problem
     * because writes to this list are uncommon.
     */
    @Transient
    val events = WorkspaceEvents2()

    @Transient
    lateinit var idManager: SimpleIdManager

    /**
     * Delay in milliseconds between update cycles. Used to artificially slow
     * down simulation (sometimes useful in teaching).
     */
    var updateDelay = 0

    @Transient
    var couplingManager = CouplingManager(this)
        private set

    @Transient
    val updater = WorkspaceUpdater(this)

    init {
        initIdManager()
    }

    /**
     * Adds a workspace component to the workspace.
     *
     * @param component The component to add.
     */
    fun addWorkspaceComponent(component: WorkspaceComponent) {
        Logger.debug("adding component: $component")
        _componentList.add(component)
        component.workspace = this
        component.setChangedSinceLastSave(false)
        setWorkspaceChanged(true)

        // If there is no custom name, use the id manager to produce a default name
        if (component.name.isEmpty()) {
            component.name = idManager.getAndIncrementId(component.javaClass)
        }

        runBlocking {
            events.componentAdded.fire(component)
        }
        component.events.attributeContainerRemoved.on { attributeContainer: AttributeContainer? ->
            couplingManager.removeAttributeContainer(
                attributeContainer!!
            )
        }
    }

    /**
     * Remove the specified component.
     *
     * @param component The component to remove.
     */
    fun removeWorkspaceComponent(component: WorkspaceComponent) {
        Logger.debug("removing component: $component")

        // Remove all couplings associated with this component
        // this.getCouplingManager().removeCouplings(component);
        _componentList.remove(component)
        setWorkspaceChanged(true)
        events.componentRemoved.fireAndForget(component)
    }

    /**
     * Should be called when updating is stopped.
     */
    fun updateStopped() {
        synchronized(componentList) {
            for (component in componentList) {
                component.doStopped()
            }
        }
    }

    /**
     * Iterates all couplings on all components until halted by user.
     */
    fun run() {
        for (wc in componentList) {
            wc.start()
        }
        launch {
            updater.run()
        }
    }

    /**
     * Stops iteration of all couplings on all components.
     */
    fun stop() {
        for (wc in componentList) {
            wc.stop()
        }
        updater.stop()
        updateStopped()
    }

    /**
     * Update the workspace a single time.
     */
    fun iterate() {
        for (wc in componentList) {
            wc.start()
        }
        launch {
            updater.runOnce()
        }
        stop()
    }

    /**
     * Iterate for a specified number of iterations. Block until all iterations are complete then
     * run an optional finishing task.
     *
     * TODO: This is a temporary solution until suspend functions are used here
     */
    @JvmOverloads
    fun iterate(numIterations: Int, finishingTask: () -> Unit = {}) {
        for (wc in componentList) {
            wc.start()
        }
        launch {
            updater.iterate(numIterations, finishingTask)
        }
        stop()
    }

    suspend fun iterateSuspend(numIterations: Int) {
        for (wc in componentList) {
            wc.start()
        }
        updater.iterate(numIterations)
        stop()
    }

    suspend fun iterateWhile(predicate: () -> Boolean) {
        for (wc in componentList) {
            wc.start()
        }
        updater.iterateWhile(predicate)
        stop()
    }

    /**
     * Simple non-synchronized updater for non-GUI applications running
     * in a single thread.
     */
    fun simpleIterate() {
        updater.runBlocking()
    }

    /**
     * Remove all components (networks, worlds, etc.) from this workspace.
     */
    fun clearWorkspace() {
        stop()
        removeAllComponents()
        resetTime()
        setWorkspaceChanged(false)
        currentFile = null
        couplingManager = CouplingManager(this)
        events.workspaceCleared.fireAndForget()
        updater.updateManager.setDefaultUpdateActions()
    }

    /**
     * Disposes all Simbrain Windows.
     */
    fun removeAllComponents() {
        val toRemove: MutableList<WorkspaceComponent> = ArrayList()
        synchronized(componentList) {
            for (component in componentList) {
                toRemove.add(component)
            }
            for (component in toRemove) {
                removeWorkspaceComponent(component)
            }
        }
    }

    /**
     * Check whether there have been changes in the workspace or its
     * components.
     *
     * @return true if changes exist, false otherwise
     */
    fun changesExist(): Boolean {
        return if (workspaceChanged) {
            true
        } else {
            return componentList.any { it.hasChangedSinceLastSave() }
        }
    }

    /**
     * Sets whether the workspace has been changed.
     *
     * @param workspaceChanged Has workspace been changed value
     */
    fun setWorkspaceChanged(workspaceChanged: Boolean) {
        this.workspaceChanged = workspaceChanged
    }
    /**
     * @return the currentDirectory
     */
    /**
     * @param currentDirectory the currentDirectory to set
     */
    var currentDirectory = Utils.USER_DIR + Utils.FS + "simulations" + Utils.FS + "workspaces"

    /**
     * Get a component using its id.
     */
    fun getComponent(id: String?): WorkspaceComponent? {
        return componentList.firstOrNull { it.name.equals(id, ignoreCase = true) }
    }

    override fun toString(): String {
        val builder = StringBuilder(
            """
    Number of components: ${componentList.size}
    
    """.trimIndent()
        )
        var i = 0
        synchronized(componentList) {
            for (component in componentList) {
                builder.append(
                    """
    Component ${++i}:${component.name}
    
    """.trimIndent()
                )
            }
        }
        return builder.toString()
    }

    /**
     * Returns all components of the specified type, e.g. all
     * WorkspaceComponents of type NetworkComponent.class.
     *
     * @param componentType the type of the component, in the sense of its
     * class
     * @return list of components
     */
    fun getComponentList(componentType: Class<*>): Collection<WorkspaceComponent> {
        val returnList: MutableList<WorkspaceComponent> = ArrayList()
        for (component in componentList) {
            if (component.javaClass == componentType) {
                returnList.add(component)
            }
        }
        return returnList
    }

    /**
     * Returns global time.
     *
     * @return the time
     */
    val time: Int
        get() = updater.time

    /**
     * Reset time.
     */
    fun resetTime() {
        updater.resetTime()
    }

    /**
     * Actions required prior to proper serialization.
     */
    fun preSerializationInit() {
        /*
         * TODO: A bit of a hack. Currently just moves trainer components to the
         * back of the list, so they are serialized last, and hence deserialized
         * last.
         */
        Collections.sort(_componentList) { c1, c2 -> Integer.compare(c1.serializePriority, c2.serializePriority) }
        savedTime = time
    }

    /**
     * Open a workspace from a file.
     *
     * @param theFile the file to try to open
     */
    fun openWorkspace(theFile: File?) {
        stop()
        val serializer = WorkspaceSerializer(this)
        try {
            if (theFile != null) {
                clearWorkspace()
                serializer.deserialize(FileInputStream(theFile))
                currentFile = theFile
                setWorkspaceChanged(false)
                events.workspaceOpened.fireAndForget()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * Helper method to save a specified file.
     *
     * @param file file to save.
     */
    @JvmOverloads
    fun save(file: File?, headless: Boolean = false) {
        if (file != null) {
            try {
                val ostream = FileOutputStream(file)
                try {
                    val serializer = WorkspaceSerializer(this)
                    serializer.serialize(ostream, headless)
                    setWorkspaceChanged(false)
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    ostream.close()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Returns a "flat" representation of the workspace as a byte array from the zipped representation
     * [WorkspaceSerializer] produces.
     */
    val zipData: ByteArray?
        get() {
            try {
                val serializer = WorkspaceSerializer(this)
                val bas = ByteArrayOutputStream()
                serializer.serialize(bas)
                bas.close()
                return bas.toByteArray()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return null
        }

    /**
     * Open a workspace from the flat representation provided by [.getZipData] }.
     */
    fun openFromZipData(zipData: ByteArray?) {
        try {
            clearWorkspace()
            val serializer = WorkspaceSerializer(this)
            val bis = ByteArrayInputStream(zipData)
            serializer.deserialize(bis)
            bis.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * Convenience method for adding an update action to the workspace's action
     * list (the sequence of actions invoked on each iteration of the
     * workspace).
     *
     * @param action new action
     */
    fun addUpdateAction(action: UpdateAction?) {
        updater.updateManager.addAction(action)
    }

    fun addUpdateAction(description: String, longDescription: String = description, action: suspend () -> Unit) {
        updater.updateManager.addAction(updateAction(description, longDescription, action))
    }

    fun addNonRemovableAction(description: String, longDescription: String = description, action: suspend () -> Unit) {
        updater.updateManager.addNonRemovableAction(updateAction(description, longDescription, action))
    }

    /**
     * Convenience method which gets the couplings the coupling manager stores.
     */
    val couplings: Collection<Coupling>
        get() = couplingManager.couplings

    fun initIdManager() {
        idManager = SimpleIdManager(
            initIdFunction = { cls -> _componentList.count { comp -> comp.javaClass == cls } },
            baseNameGenerator = { cls -> cls.simpleName.removeSuffix("Component") },
            delimeter = " "
        )
    }
}
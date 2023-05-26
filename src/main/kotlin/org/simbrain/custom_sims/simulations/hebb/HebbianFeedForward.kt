package org.simbrain.custom_sims.simulations

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.simbrain.custom_sims.createControlPanel
import org.simbrain.custom_sims.newSim
import org.simbrain.network.NetworkComponent
import org.simbrain.util.Utils.FS
import org.simbrain.workspace.serialization.WorkspaceSerializer
import org.simbrain.world.odorworld.OdorWorldComponent
import java.io.FileInputStream

/**
 * Demo for studying Hebbian feed-forward pattern association
 */
val hebbianFeedForward = newSim {

    // Basic setup
    workspace.clearWorkspace()
    withContext(Dispatchers.IO) {
        WorkspaceSerializer(workspace).deserialize(
            FileInputStream("simulations" + FS + "workspaces"+ FS + "hebbFF.zip"))
    }
    val network = (workspace.getComponent("Network 1") as NetworkComponent).network
    val world = (workspace.getComponent("OdorWorld 1") as OdorWorldComponent).world

    withGui {
        createControlPanel("Control Panel", 5, 10) {
            addButton("Training Mode (clamped nodes)") {
                network.freeNeurons.forEach{n -> n.isClamped = true }
                network.freezeSynapses(false)
            }.apply {
                toolTipText = "Clamps nodes and unclamps weights"
            }
            addButton("Test Mode (clamped weights)") {
                network.freeNeurons.forEach{n -> n.isClamped = false }
                network.freezeSynapses(true)
            }.apply {
                toolTipText = "Clamps weights and unclamps nodes"
            }
        }
    }



}
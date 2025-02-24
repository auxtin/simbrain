// /*
//  * Part of Simbrain--a java-based neural network kit
//  * Copyright (C) 2005,2007 The Authors.  See http://www.simbrain.net/credits
//  *
//  * This program is free software; you can redistribute it and/or modify
//  * it under the terms of the GNU General Public License as published by
//  * the Free Software Foundation; either version 2 of the License, or
//  * (at your option) any later version.
//  *
//  * This program is distributed in the hope that it will be useful,
//  * but WITHOUT ANY WARRANTY; without even the implied warranty of
//  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  * GNU General Public License for more details.
//  *
//  * You should have received a copy of the GNU General Public License
//  * along with this program; if not, write to the Free Software
//  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
//  */
// package org.simbrain.network.gui.nodes.subnetworkNodes;
//
// import org.simbrain.network.gui.NetworkPanel;
// // import org.simbrain.network.gui.dialogs.network.LMSEditorDialog;
// import org.simbrain.network.gui.dialogs.network.LMSTrainingDialog;
// import org.simbrain.network.gui.nodes.SubnetworkNode;
// // import org.simbrain.network.gui.trainer.IterativeTrainingPanel;
// import org.simbrain.network.gui.trainer.TrainerGuiActions;
// import org.simbrain.network.gui.trainer.subnetworkTrainingPanels.LMSOfflineTrainingPanel;
// import org.simbrain.network.subnetworks.LMSNetwork;
// import org.simbrain.network.trainers.LMSOffline;
// import org.simbrain.util.ResourceManager;
// import org.simbrain.util.StandardDialog;
//
// import javax.swing.*;
// import java.awt.*;
// import java.awt.event.ActionEvent;
//
// /**
//  * PNode representation of a group of a LMS network.
//  *
//  * @author jyoshimi
//  */
// public class LMSNetworkNode extends SubnetworkNode {
//
//     // TODO
//     final LMSNetwork lms;
//
//     /**
//      * Create a layered network.
//      *
//      * @param networkPanel parent panel
//      */
//     public LMSNetworkNode(NetworkPanel networkPanel, LMSNetwork lms) {
//         super(networkPanel, lms);
//         this.lms = lms;
//         setContextMenu();
//     }
//
//     /**
//      * Sets custom menu.
//      */
//     private void setContextMenu() {
//         JPopupMenu menu = new JPopupMenu();
//         editAction.putValue("Name", "Edit / Train LMS...");
//         menu.add(editAction);
//         menu.add(renameAction);
//         menu.add(removeAction);
//         menu.addSeparator();
//         JMenu dataActions = new JMenu("View / Edit Data");
//         final LMSNetwork network = (LMSNetwork) getSubnetwork();
//         dataActions.add(TrainerGuiActions.getEditCombinedDataAction(getNetworkPanel(), network));
//         dataActions.addSeparator();
//         // dataActions.add(TrainerGuiActions.getEditDataAction(getNetworkPanel(), network.getInputNeurons(), network.getTrainingSet().getInputDataMatrix(), "Input"));
//         // dataActions.add(TrainerGuiActions.getEditDataAction(getNetworkPanel(), network.getOutputNeurons(), network.getTrainingSet().getTargetDataMatrix(), "Target"));
//         menu.add(dataActions);
//
//         setContextMenu(menu);
//     }
//
//     @Override
//     public StandardDialog getPropertyDialog() {
//         return new LMSTrainingDialog(getNetworkPanel(), lms);
//     }
//
//     /**
//      * Action to train LMS Iteratively.  No longer used.
//      */
//     Action trainIterativelyAction = new AbstractAction() {
//
//         // Initialize
//         {
//             putValue(SMALL_ICON, ResourceManager.getImageIcon("menu_icons/Trainer.png"));
//             putValue(NAME, "Train iteratively...");
//             putValue(SHORT_DESCRIPTION, "Train iteratively...");
//         }
//
//         @Override
//         public void actionPerformed(ActionEvent arg0) {
//             System.out.println("LMSNetworkNode.actionPerformed");
//             // LMSNetwork network = (LMSNetwork) getSubnetwork();
//             // IterativeTrainingPanel trainingPanel = new IterativeTrainingPanel(getNetworkPanel(), new LMSIterative(network));
//             // Window frame = getNetworkPanel().displayPanelInWindow(trainingPanel, "Trainer");
//             // trainingPanel.setFrame(frame);
//         }
//     };
//
//     /**
//      * Action to train LMS Offline. No longer used.
//      */
//     Action trainOfflineAction = new AbstractAction() {
//
//         // Initialize
//         {
//             putValue(SMALL_ICON, ResourceManager.getImageIcon("menu_icons/Trainer.png"));
//             putValue(NAME, "Train offline...");
//             putValue(SHORT_DESCRIPTION, "Train offline...");
//         }
//
//         @Override
//         public void actionPerformed(ActionEvent arg0) {
//             LMSNetwork network = (LMSNetwork) getSubnetwork();
//             JDialog frame = getNetworkPanel().displayPanelInWindow(new JPanel(), "Trainer"); // hack
//             LMSOfflineTrainingPanel trainingPanel = new LMSOfflineTrainingPanel(getNetworkPanel(), new LMSOffline(network), (Window) frame);
//             frame.setContentPane(trainingPanel);
//             frame.pack();
//         }
//     };
//
// }

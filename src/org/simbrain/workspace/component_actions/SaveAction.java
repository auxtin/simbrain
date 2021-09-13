// Commenting out since component save / open is being treated as import / export and should be discouraged

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
// package org.simbrain.workspace.component_actions;
//
// import java.awt.Toolkit;
// import java.awt.event.ActionEvent;
// import java.awt.event.KeyEvent;
//
// import javax.swing.AbstractAction;
// import javax.swing.KeyStroke;
//
// import org.simbrain.resource.ResourceManager;
// import org.simbrain.workspace.gui.GuiComponent;
//
// /**
//  * Save component action.
//  */
// public final class SaveAction extends AbstractAction {
//
//     /** Network panel. */
//     private final GuiComponent guiComponent;
//
//     /**
//      * Create a new save component action with the specified.
//      *
//      * @param guiComponent networkPanel, must not be null
//      */
//     public SaveAction(final GuiComponent guiComponent) {
//
//         super("Save...");
//
//         if (guiComponent == null) {
//             throw new IllegalArgumentException("component must not be null");
//         }
//
//         putValue(SMALL_ICON, ResourceManager.getImageIcon("Save.png"));
//
//         this.putValue(this.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
//                 KeyEvent.VK_S, Toolkit.getDefaultToolkit()
//                         .getMenuShortcutKeyMask()));
//         putValue(SHORT_DESCRIPTION, "Save this component");
//
//         this.guiComponent = guiComponent;
//     }
//
//     /** @see AbstractAction
//      * @param event
//      */
//     public void actionPerformed(final ActionEvent event) {
//         guiComponent.save();
//     }
// }
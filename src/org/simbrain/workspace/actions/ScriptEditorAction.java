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
package org.simbrain.workspace.actions;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;

import org.simbrain.resource.ResourceManager;
import org.simbrain.util.SFileChooser;
import org.simbrain.util.SimbrainPreferences;
import org.simbrain.util.SimbrainPreferences.PropertyNotFoundException;
import org.simbrain.util.genericframe.GenericJInternalFrame;
import org.simbrain.workspace.gui.SimbrainDesktop;
import org.simbrain.workspace.gui.SimbrainScriptEditor;

import static org.simbrain.util.Utils.FS;
import static org.simbrain.util.Utils.USER_DIR;

/**
 * Open a script editor.
 */
public final class ScriptEditorAction extends AbstractAction {

    /** Reference to Simbrain desktop. */
    private SimbrainDesktop desktop;

    /**
     * Open a script editor.
     * @param desktop
     */
    public ScriptEditorAction(final SimbrainDesktop desktop) {
        super("Edit / Run Script...");
        this.desktop = desktop;
        putValue(SMALL_ICON, ResourceManager.getImageIcon("ScriptEditor.png"));
        putValue(SHORT_DESCRIPTION, "Edit / Run Script...");
    }

    /** @see AbstractAction 
     * @param event
     */
    public void actionPerformed(final ActionEvent event) {
        String scriptDirectory = USER_DIR + FS + "scripts";
        SFileChooser fileChooser = new SFileChooser(scriptDirectory,
                "Run Script", "bsh");
        File scriptFile = fileChooser.showOpenDialog();
        if (scriptFile != null) {
            GenericJInternalFrame frame = SimbrainScriptEditor.getInternalFrame(
                    desktop, scriptFile);
            desktop.addInternalFrame(frame);
            frame.setResizable(true);
            frame.setClosable(true);
            frame.setMaximizable(true);
            frame.setIconifiable(true);
            frame.setVisible(true);
            frame.pack();
        }

    }

}
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
package org.simbrain.workspace.gui;

import org.simbrain.workspace.Workspace;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Workspace key adapter.
 */
class WorkspaceKeyAdapter extends KeyAdapter {

    /**
     * Reference to workspace.
     */
    private final Workspace workspace;

    /**
     * Construct the key adapter.
     *
     * @param workspace
     */
    WorkspaceKeyAdapter(Workspace workspace) {
        this.workspace = workspace;
    }

    /**
     * Responds to key pressed events.
     *
     * @param e Key event
     */
    public void keyPressed(final KeyEvent e) {
        int keycode = e.getKeyCode();
        switch (keycode) {
            case KeyEvent.VK_U:
                workspace.iterate();
                break;
            default:
                break;
        }
    }

}

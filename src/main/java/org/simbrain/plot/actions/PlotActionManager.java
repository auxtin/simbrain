/*
 * Part of Simbrain--a java-based neural network kit
 * Copyright (C) 2006 Jeff Yoshimi <www.jeffyoshimi.net>
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
package org.simbrain.plot.actions;

import org.simbrain.workspace.gui.DesktopComponent;
import org.simbrain.workspace.gui.SimbrainDesktop;

import javax.swing.*;
import java.util.Arrays;
import java.util.List;

/**
 * Manages the actions for the plot components.
 */
public class PlotActionManager {

    /**
     * Open component action.
     */
    private final Action openPlotAction;

    /**
     * Save component action.
     */
    private final Action savePlotAction;

    /**
     * Plot component action manager.
     *
     * @param component Gui component.
     */
    @SuppressWarnings("unchecked")
    public PlotActionManager(DesktopComponent component) {

        openPlotAction = SimbrainDesktop.INSTANCE.getActionManager().createImportAction(component);
        savePlotAction = SimbrainDesktop.INSTANCE.getActionManager().createExportAction(component);
    }

    /**
     * @return the open/save plot actions.
     */
    public List<Action> getOpenSavePlotActions() {
        return Arrays.asList(new Action[]{openPlotAction, savePlotAction});
    }

    /**
     * @return the openPlotAction
     */
    public Action getOpenPlotAction() {
        return openPlotAction;
    }

    /**
     * @return the savePlotAction
     */
    public Action getSavePlotAction() {
        return savePlotAction;
    }

}

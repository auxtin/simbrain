/*
 * Part of Simbrain--a java-based neural network kit Copyright (C) 2005,2007 The
 * Authors. See http://www.simbrain.net/credits This program is free software;
 * you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version. This program is
 * distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details. You
 * should have received a copy of the GNU General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 59 Temple Place
 * - Suite 330, Boston, MA 02111-1307, USA.
 */
package org.simbrain.network.gui.trainer;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

import org.simbrain.network.core.Neuron;
import org.simbrain.network.gui.NetworkPanel;
import org.simbrain.network.trainers.InvalidDataException;
import org.simbrain.network.trainers.IterableTrainer;
import org.simbrain.network.trainers.Trainable;
import org.simbrain.network.trainers.Trainer;
import org.simbrain.resource.ResourceManager;
import org.simbrain.util.SFileChooser;
import org.simbrain.util.SimbrainPreferences;
import org.simbrain.util.SimbrainPreferences.PropertyNotFoundException;
import org.simbrain.util.math.NumericMatrix;
import org.simbrain.util.propertyeditor.gui.ReflectivePropertyEditor;
import org.simbrain.util.table.NumericTable;
import org.simbrain.util.table.SimbrainJTable;
import org.simbrain.util.table.TableDataException;

import static org.simbrain.util.Utils.FS;
import static org.simbrain.util.Utils.USER_DIR;

/**
 * Contains actions for use in Trainer GUI.
 *
 * @author jyoshimi
 */
public class TrainerGuiActions {

    /**
     * Action for viewing data in a table that correlate with a set of neurons.
     * It's a bit of a pain, but to use this you must create an instance of a
     * DataHolder, which is basically just a reference to an object with getData
     * and setData methods.
     *
     * @param networkPanel the parent network panel
     * @param neurons the list of neurons to which the columns correspond
     * @param dataHolder the object that holds the data (with a getData and
     *            setData method)
     * @param name the name of the data (for use in display)
     * @return an action for opening this table
     */
    public static Action getEditDataAction(final NetworkPanel networkPanel,
        final List<Neuron> neurons, final NumericMatrix dataHolder,
        final String name) {
        return new AbstractAction() {

            // Initialize
            {
                putValue(SMALL_ICON, ResourceManager.getImageIcon("Table.png"));
                putValue(NAME, "Edit " + name + " Data...");
                putValue(SHORT_DESCRIPTION, "Edit" + name + " Data...");
            }

            /**
             * {@inheritDoc}
             */
            public void actionPerformed(ActionEvent arg0) {
                DataPanel panel = new DataPanel(neurons, dataHolder, 5, name);
                JDialog frame =
                    networkPanel.displayPanelInWindow(panel, "Edit "
                        + name);
                panel.setFrame(frame);
            }

        };
    }

    /**
     * Action for viewing two datatables, one for input data; the other for
     * training data.
     *
     * @param networkPanel the parent network panel.
     * @param trainable the trainable object providing access to input and
     *            output neurons
     * @return the action
     */
    public static Action getEditCombinedDataAction(
        final NetworkPanel networkPanel, final Trainable trainable) {
        return new AbstractAction() {

            // Initialize
            {
                putValue(SMALL_ICON, ResourceManager.getImageIcon("Table.png"));
                putValue(NAME, "Edit Training Set...");
                putValue(SHORT_DESCRIPTION, "Edit Training Set...");
            }

            /**
             * {@inheritDoc}
             */
            public void actionPerformed(ActionEvent arg0) {
                TrainingSetPanel combinedPanel = new TrainingSetPanel(
                    trainable, 5);
                JDialog frame = networkPanel.displayPanelInWindow(
                    combinedPanel, "Edit Training Set");
                combinedPanel.setFrame(frame);
            }

        };
    }

    /**
     * Return the current data directory.
     *
     * @return return the data directory
     */
    public static String getDataDirectory() {
        return USER_DIR + FS + "simulations" + FS + "tables";
    }

    /**
     * Action for opening data comma separated value file. Replaces the default
     * simbrainjtable action for this, so that the trainer and trainer panel can
     * be updated as appropriate.
     *
     * @param table the simbrain jtable
     * @param dataHolder the object holding the data
     * @return the action for opening csv files
     */
    public static Action getOpenCSVAction(final SimbrainJTable table,
        final NumericMatrix dataHolder) {
        return new AbstractAction() {

            // Initialize
            {
                putValue(SMALL_ICON, ResourceManager.getImageIcon("Open.png"));
                putValue(NAME, "Open data (.csv)");
                putValue(SHORT_DESCRIPTION, "Open .csv data...");
            }

            /**
             * {@inheritDoc}
             */
            public void actionPerformed(ActionEvent arg0) {
                SFileChooser chooser = new SFileChooser(getDataDirectory(),
                    "comma-separated-values (csv)", "csv");
                File theFile = chooser.showOpenDialog();
                if (theFile != null) {
                    try {
                        ((NumericTable) table.getData()).readData(theFile,
                            true, false);
                        dataHolder.setData(((NumericTable) table.getData())
                            .asDoubleArray());
                    } catch (InvalidDataException exception) {
                        JOptionPane.showMessageDialog(null,
                            exception.getMessage(), "Error",
                            JOptionPane.ERROR_MESSAGE);
                    } catch (TableDataException e) {
                        JOptionPane.showOptionDialog(null, e.getMessage(),
                            "Warning", JOptionPane.DEFAULT_OPTION,
                            JOptionPane.WARNING_MESSAGE, null, null, null);
                    }
                }
            }

        };
    }

    /**
     * Show properties dialog for the indicated trainer.
     *
     * @param trainer the trainer
     * @return the action
     */
    public static AbstractAction
        getPropertiesDialogAction(final Trainer trainer) {
        return new AbstractAction() {

            // Initialize
            {
                putValue(SMALL_ICON, ResourceManager.getImageIcon("Prefs.png"));
                putValue(NAME, "Properties");
                putValue(SHORT_DESCRIPTION, "Edit Properties");
            }

            /**
             * {@inheritDoc}
             */
            public void actionPerformed(ActionEvent arg0) {
                ReflectivePropertyEditor editor =
                    new ReflectivePropertyEditor();
                editor.setExcludeList(new String[] { "iteration",
                    "updateCompleted" });
                editor.setObject(trainer);
                JDialog dialog = editor.getDialog();
                dialog.setModal(true);
                dialog.pack();
                dialog.setLocationRelativeTo(null);
                dialog.setVisible(true);
            }

        };
    }

    /**
     * Show an error plot for this trainer.
     *
     * @param panel the network panel in which to display the plot
     * @param trainer the trainer
     * @return the action
     */
    public static AbstractAction getShowPlotAction(final NetworkPanel panel,
        final IterableTrainer trainer) {
        return new AbstractAction() {

            // Initialize
            {
                putValue(SMALL_ICON,
                    ResourceManager.getImageIcon("CurveChart.png"));
                putValue(NAME, "Show error plot");
                putValue(SHORT_DESCRIPTION, "Show error plot");
            }

            /**
             * {@inheritDoc}
             */
            public void actionPerformed(ActionEvent arg0) {
                ErrorPlotPanel errorPanel = new ErrorPlotPanel(trainer);
                panel.displayPanel(errorPanel, "Error plot");
            }

        };
    }

}

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
package org.simbrain.util.projection;

import com.Ostermiller.util.CSVParser;
import org.apache.log4j.Logger;
import org.simbrain.util.SimbrainPreferences;
import org.simbrain.workspace.AttributeContainer;
import org.simbrain.workspace.Producible;

import javax.xml.crypto.Data;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

/**
 * <b>Projector</b> is a the main class of this package, which provides an
 * interface for projecting high dimensional data to 2 dimensions.
 * <br>
 * Contains two {@link Dataset}s: an "upstairs" of high-d data, a "downstairs" of 2-d data, and a {@link
 * ProjectionMethod} that projects between them. Other state information about the projector is also stored here. The
 * data in the upstairs dataset is stored in the same order as the data in the downstairs dataset.
 * <br>
 * Cf. {@url https://en.wikipedia.org/wiki/Dimensionality_reduction}
 */
public class Projector implements AttributeContainer {

    /**
     * Log4j logger.
     */
    private static transient Logger logger = Logger.getLogger(Projector.class);

    /**
     * Listener list.
     */
    private transient List<ProjectorListener> listeners;

    /**
     * A set of hi-d datapoints, each of which is an array of doubles The data
     * A set of hi-d datapoints, each of which is an array of doubles The data to be projected.
     */
    protected Dataset upstairs;

    /**
     * A set of low-d datapoints, each of which is an array of doubles The projection of the upstairs data.
     */
    protected Dataset downstairs;

    /**
     * Reference to current "hot" point.
     */
    private DataPoint currentPoint;
    /**
     * Default number of sources. This is the dimensionality of the hi D projectionModel
     */
    private final static int DEFAULT_NUMBER_OF_DIMENSIONS = 25;

    /**
     * Default projection method.
     */
    private final static String DEFAULT_PROJECTION_METHOD = "PCA";

    /**
     * Distance within which added points are considered old and are thus not added.
     */
    protected double tolerance;

    /**
     * References to projection objects.
     */
    private ProjectionMethod projectionMethod;

    /**
     * Set to false to turn off color manager and use custom point coloring, as in, e.g., the use of the {@link Halo}
     * tool.
     */
    private boolean useColorManager = true;

    /**
     * List of Neuron update rules; used in Gui Combo boxes.
     */
    private final HashMap<Class<?>, String> projectionMethods = new LinkedHashMap<Class<?>, String>();

    /**
     * One-step ahead prediction used for Bayesian datapoint coloring
     */
    private OneStepPrediction predictor = new OneStepPrediction();

    /**
     * Probability of the current state relative to the {@link #predictor} object.
     */
    private double currentStateProbabilty = 0;

    // Initialization
    {
        listeners = new ArrayList<ProjectorListener>();
        colorManager = new DataColoringManager(this);

        projectionMethods.put(ProjectCoordinate.class, "Coordinate Projection");
        projectionMethods.put(ProjectNNSubspace.class, "NN Subspace");
        projectionMethods.put(ProjectPCA.class, "PCA");
        projectionMethods.put(ProjectTriangulate.class, "Triangulation");
        projectionMethods.put(ProjectSammon.class, "Sammon Map");

        tolerance = SimbrainPreferences.getDouble("projectorTolerance");
    }

    /**
     * Manages coloring the datapoints.
     */
    private final DataColoringManager colorManager;

    /**
     * Default constructor for projector.
     */
    public Projector() {
        setProjectionMethod(DEFAULT_PROJECTION_METHOD);
        init(DEFAULT_NUMBER_OF_DIMENSIONS);
    }

    /**
     * Default constructor for projector.
     *
     * @param dimension dimensionality of data to be projected
     */
    public Projector(int dimension) {
        setProjectionMethod(DEFAULT_PROJECTION_METHOD);
        init(dimension);
    }

    /**
     * Add a projector listener.
     *
     * @param projectorListener the listener to add
     */
    public void addListener(final ProjectorListener projectorListener) {
        if (listeners == null) {
            listeners = new ArrayList<ProjectorListener>();
        }
        listeners.add(projectorListener);
    }

    /**
     * Initialize projector to accept data of a specified dimension.
     *
     * @param dims dimensionality of the high dimensional dataset
     */
    public void init(final int dims) {
        // TODO: This seems to be called twice when adding a projection component.
        upstairs = new Dataset(dims);
        downstairs = new Dataset(2);
        fireProjectorDataChanged();
    }

    /**
     * Updates datasets from persistent forms of data.
     */
    public void postOpenInit() {
        logger = Logger.getLogger(ProjectionMethod.class);
        listeners = new ArrayList<ProjectorListener>();
        upstairs.postOpenInit();
        downstairs.postOpenInit();
    }

    /**
     * Add a new point to the dataset, using the currently selected add method.
     *
     * @param point the upstairs point to add
     */
    public void addDatapoint(final DataPointColored point) {

        logger.debug("addDatapoint called");
        if (point.getDimension() != this.getDimensions() || (projectionMethod == null) || (getUpstairs() == null)) {
            return;
        }

        //point.setData(SimbrainMath.roundVec(point.getData(), 1));

        // Iterable functions to be re-initialized when new data is added
        if (projectionMethod.isIterable()) {
            ((IterableProjectionMethod) projectionMethod).setNeedsReInit(true);
        }

        // Add the point directly to the upstairs dataset. If the point already
        // exists just change colors and return. If the point is new. add a
        // point downstairs, and call the projection algorithm.
        DataPoint existingPoint = upstairs.addPoint(point, tolerance);
        if (existingPoint != null) {
            // That point was already in the dataset
            currentPoint = existingPoint;
        } else {
            // It's a new point
            currentPoint = point;
            DataPoint newPoint;
            if (point.getDimension() == 1) {
                // For 1-d datasets plot points on a horizontal line
                newPoint = new DataPoint(new double[]{point.get(0), 0});
            } else {
                newPoint = new DataPoint(new double[]{point.get(0), point.get(1)});
            }
            downstairs.addPoint(newPoint);
            projectionMethod.project();
            fireDataPointAdded();
        }

        if (useColorManager) {
            if (colorManager.getColoringMethod() == DataColoringManager.ColoringMethod.Bayesian) {
                // Update predictor
                if ((upstairs.getLastPoint() != null) && (upstairs.getCurrentPoint() != null)) {
                    currentStateProbabilty = predictor.addSourceTargetPair(
                            (DataPointColored) upstairs.getLastPoint(),
                            (DataPointColored) upstairs.getCurrentPoint());
                } else {
                    currentStateProbabilty = 0;
                }
                colorManager.updateBayes();
            } else {
                colorManager.updateDataPointColors(upstairs);
            }
        }
    }

    /**
     * Change the current projection method and perform and other needed initialization.
     *
     * @param method the new projection algorithm
     */
    public void setProjectionMethod(final ProjectionMethod method) {
        projectionMethod = method;
        method.init();
        this.fireProjectionMethodChanged();
        projectionMethod.project();
    }

    /**
     * @param projName the name of the projection algorithm to switch to
     */
    public void setProjectionMethod(final String projName) {
        if (projName == null) {
            return;
        }
        for (Class<?> method : projectionMethods.keySet()) {
            if (projName.equalsIgnoreCase(projectionMethods.get(method))) {
                try {
                    ProjectionMethod projMethod;
                    projMethod = (ProjectionMethod) method.getConstructor(new Class[]{Projector.class}).newInstance(new Object[]{this});
                    setProjectionMethod(projMethod);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Add new high-d datapoints and reinitialize the datasets.
     *
     * @param theFile file containing the high-d data, forwarded to a dataset method
     */
    public void importData(final File theFile) {
        try {
            CSVParser theParser = new CSVParser(new FileInputStream(theFile), "", "", "#");

            // # is a comment delimeter in net files
            String[][] values = theParser.getAllValues();
            String[] line;
            double[] vector;

            int dimension = values[0].length;
            init(dimension);

            for (int i = 0; i < values.length; i++) {
                line = values[i];
                vector = new double[values[0].length];

                for (int j = 0; j < line.length; j++) {
                    vector[j] = Double.parseDouble(line[j]);
                }
                addDatapoint(new DataPointColored(vector));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        projectionMethod.init();
        projectionMethod.project();
        fireProjectorDataChanged();
    }

    /**
     * Used to get the String associated with the current projection method. Used by a combo box in the gui.
     *
     * @return the String associated with current projection method.
     */
    public String getCurrentMethodString() {
        return projectionMethods.get(projectionMethod.getClass());
    }

    /**
     * Number of dimensions of the underlying data.
     *
     * @return dimensions of the underlying data
     */
    public int getDimensions() {
        if (upstairs == null) {
            return 0;
        }
        return upstairs.getDimensions();
    }

    /**
     * Notify listeners that a new datapoint has been added to the projector.
     */
    public void fireDataPointAdded() {
        for (ProjectorListener listener : listeners) {
            listener.datapointAdded();
        }
    }

    /**
     * Notify listeners that the projection method has been changed.
     */
    public void fireProjectionMethodChanged() {
        for (ProjectorListener listener : listeners) {
            listener.projectionMethodChanged();
        }
    }

    /**
     * Notify listeners that the colors of some datapoints have changed but nothing else.
     */
    public void fireProjectorColorsChanged() {
        for (ProjectorListener listener : listeners) {
            listener.projectorColorsChanged();
        }
    }

    /**
     * Notify listeners that data (in particular the underlying points) have been changed.
     */
    public void fireProjectorDataChanged() {
        for (ProjectorListener listener : listeners) {
            listener.projectorDataChanged();
        }
    }

    /**
     * @return the current projection algorithm
     */
    public ProjectionMethod getProjectionMethod() {
        return projectionMethod;
    }

    /**
     * Convenience method to get upstairs dataset.
     *
     * @return hi-dimensional dataset associated with current projector
     */
    public Dataset getUpstairs() {
        return upstairs;
    }

    /**
     * Convenience method to get downstairs dataset.
     *
     * @return low-dimensional dataset associated with current projector
     */
    public Dataset getDownstairs() {
        return downstairs;
    }

    /**
     * Iterate the dataset once.
     */
    public void iterate() {
        if (projectionMethod.isIterable()) {
            ((IterableProjectionMethod) projectionMethod).iterate();
        }

    }

    /**
     * Reset the projector. Clear the underlying datasets.
     */
    public void reset() {
        upstairs.clear();
        downstairs.clear();
        fireProjectorDataChanged();
        predictor.clear();
        // getCurrentProjectionMethod().resetColorIndices();
    }

    /**
     * Reset the colors of all colored data points.
     */
    public void resetColors() {
        for (int i = 0; i < upstairs.getNumPoints(); i++) {
            DataPointColored point = (DataPointColored) upstairs.getPoint(i);
            point.resetActivation();
        }
        if (useColorManager) {
            colorManager.updateDataPointColors(upstairs);
        }
    }

    /**
     * Returns the size of the dataset.
     *
     * @return size of dataset.
     */
    public int getNumPoints() {
        return downstairs.getNumPoints();
    }

    @Override
    public String toString() {
        String ret = "Projection Method " + projectionMethod + "\n";
        return ret + "Number of Points: " + this.getNumPoints() + "\n-----------------------\n High Dimensional Data \n" + upstairs.toString() + "-----------------------\nProjected Data \n" + downstairs.toString();
    }

    public double getTolerance() {
        return tolerance;
    }

    public void setTolerance(double tolerance) {
        this.tolerance = tolerance;
    }

    /**
     * Randomize the low-dimensional data. Used with iterative projection methods to "restart" the iteration.
     *
     * @param upperBound the upper bound of randomization
     */
    public void randomize(int upperBound) {
        downstairs.randomize(upperBound);
        this.fireProjectorDataChanged();
    }

    public DataColoringManager getColorManager() {
        return colorManager;
    }

    /**
     * Check the integrity of the two datasets by checking: (1) That the low-d set is at least 2 dimensions (2) That the
     * low d space is lower dimensional than the hi d space (3) That both datasets have the same number of points.
     *
     * @return true if low dimensions are lower than hi dimensions and low dimension is less than one
     */
    public boolean compareDatasets() {
        if (downstairs.getDimensions() < 1) {
            System.out.println("WARNING: The dimension of the low dimensional data set");
            System.out.println("cannot be less than 1");

            return false;
        }

        if (downstairs.getDimensions() > upstairs.getDimensions()) {
            System.out.println("WARNING: The dimension of the low dimensional data set");
            System.out.println("cannot be greater than the dimension of the hi");
            System.out.println("dimensional data set.\n");
            System.out.println("hiDimension = " + upstairs.getDimensions() + "\n");
            System.out.println("lowD = " + downstairs.getDimensions());
            return false;
        }
        if (downstairs.getNumPoints() != upstairs.getNumPoints()) {
            System.out.println("WARNING: The number of points in the hi-d set (" + upstairs.getNumPoints() + "" + ") does not match that in the low-d set (" + downstairs.getNumPoints() + ")\n");

            return false;
        }
        return true;
    }

    public HashMap<Class<?>, String> getProjectionMethods() {
        return projectionMethods;
    }

    public DataPoint getCurrentPoint() {
        return currentPoint;
    }

    public boolean isUseColorManager() {
        return useColorManager;
    }

    public void setUseColorManager(boolean useColorManager) {
        this.useColorManager = useColorManager;
    }

    public OneStepPrediction getPredictor() {
        return predictor;
    }

    @Producible
    public double getCurrentStateProbability() {
        return currentStateProbabilty;
    }

    @Override
    public String getId() {
        return "Projector";
    }
}

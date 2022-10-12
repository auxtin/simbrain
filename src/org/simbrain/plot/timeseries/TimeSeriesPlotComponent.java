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
package org.simbrain.plot.timeseries;

import org.simbrain.plot.ChartListener;
import org.simbrain.workspace.AttributeType;
import org.simbrain.workspace.PotentialConsumer;
import org.simbrain.workspace.WorkspaceComponent;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents time series data.
 */
public class TimeSeriesPlotComponent extends WorkspaceComponent {

    /** The data model. */
    private final TimeSeriesModel model;

    /** Time Series consumer type. */
    private AttributeType timeSeriesConsumerType;

    /**
     * Create new time series plot component.
     *
     * @param name name
     */
    public TimeSeriesPlotComponent(final String name) {
        super(name);
        model = new TimeSeriesModel();
        initializeAttributes();
        addListener();
        model.defaultInit();
    }

    /**
     * Creates a new time series component from a specified model. Used in
     * deserializing.
     *
     * @param name chart name
     * @param model chart model
     */
    public TimeSeriesPlotComponent(final String name,
            final TimeSeriesModel model) {
        super(name);
        this.model = model;
        initializeAttributes();
        addListener();
    }

    /**
     * Initializes a JFreeChart with specific number of data sources.
     *
     * @param name name of component
     * @param numDataSources number of data sources to initialize plot with
     */
    public TimeSeriesPlotComponent(final String name, final int numDataSources) {
        super(name);
        model = new TimeSeriesModel(numDataSources);
        initializeAttributes();
        addListener();
    }

    /**
     * Initialize consuming attributes.
     */
    private void initializeAttributes() {
        timeSeriesConsumerType = new AttributeType(this, "Series", "setValue",
                double.class, true);
        addConsumerType(timeSeriesConsumerType);
    }

    @Override
    public List<PotentialConsumer> getPotentialConsumers() {
        List<PotentialConsumer> returnList = new ArrayList<PotentialConsumer>();
        if (timeSeriesConsumerType.isVisible()) {
            for (int i = 0; i < model.getDataset().getSeriesCount(); i++) {
                String description = timeSeriesConsumerType
                        .getSimpleDescription("Time Series " + (i + 1));
                PotentialConsumer consumer = getAttributeManager()
                        .createPotentialConsumer(this, "setValue",
                                new Class[] { double.class, Integer.class },
                                new Object[] { i });
                consumer.setCustomDescription(description);
                returnList.add(consumer);
            }
        }
        return returnList;
    }

    /**
     * Add chart listener to model.
     */
    private void addListener() {

        model.addListener(new ChartListener() {

            public void dataSourceAdded(final int index) {
                firePotentialAttributesChanged();
            }

            public void dataSourceRemoved(final int index) {
                firePotentialAttributesChanged();
            }

            public void chartInitialized(int numSources) {
                // No implementation yet (not used in this component thus far).
            }

        });
    }

    /**
     * @return the model.
     */
    public TimeSeriesModel getModel() {
        return model;
    }

    @Override
    public Object getObjectFromKey(String objectKey) {
        return this;
    }

    /**
     * Standard method call made to objects after they are deserialized. See:
     * http://java.sun.com/developer/JDCTechTips/2002/tt0205.html#tip2
     * http://xstream.codehaus.org/faq.html
     *
     * @return Initialized object.
     */
    private Object readResolve() {
        return this;
    }

    /**
     * Opens a saved time series plot.
     *
     * @param input stream
     * @param name name of file
     * @param format format
     * @return bar chart component to be opened
     */
    public static TimeSeriesPlotComponent open(final InputStream input,
            final String name, final String format) {
        TimeSeriesModel dataModel = (TimeSeriesModel) TimeSeriesModel
                .getXStream().fromXML(input);
        return new TimeSeriesPlotComponent(name, dataModel);
    }

    @Override
    public void save(final OutputStream output, final String format) {
        TimeSeriesModel.getXStream().toXML(model, output);
    }

    @Override
    public boolean hasChangedSinceLastSave() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void closing() {
        // TODO Auto-generated method stub
    }

    @Override
    public void update() {
        model.update();
    }

    @Override
    public String getXML() {
        return TimeSeriesModel.getXStream().toXML(model);
    }

    /**
     * Set the value of a specified data source (one curve in the time series
     * plot). This is the main method for updating the data in a time series
     * chart.
     *
     * @param value the current "y-axis" value for the time series
     * @param index which time series curve to set.
     */
    public void setValue(final double value, final Integer index) {
        // TODO: Throw exception if index out of current bounds
        model.addData(index, TimeSeriesPlotComponent.this.getWorkspace()
                .getTime(), value);
    }

}

package org.simbrain.world.odorworld.gui;

import org.piccolo2d.nodes.PPath;
import org.piccolo2d.nodes.PText;
import org.simbrain.util.Utils;
import org.simbrain.util.math.SimbrainMath;
import org.simbrain.world.odorworld.entities.OdorWorldEntity;
import org.simbrain.world.odorworld.sensors.ObjectSensor;

import java.awt.*;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;

public class ObjectSensorNode extends EntityAttributeNode {

    private static final int SENSOR_RADIUS = 4;

    private final ObjectSensor sensor;

    /**
     * The shape of this node
     */
    private final PPath shape;

    /**
     * Dotted circle around sensor
     */
    private PPath dispersionCircle;

    /**
     * The text graphical object
     */
    private final PText labelText;

    /**
     * The text label location
     */
    private final Point2D.Float labelBottomCenterLocation = new Point2D.Float(0, -5);

    public ObjectSensorNode(ObjectSensor sensor) {
        this.sensor = sensor;
        GeneralPath diamondPath = new GeneralPath();
        diamondPath.moveTo(-SENSOR_RADIUS, 0);
        diamondPath.lineTo(0, -SENSOR_RADIUS);
        diamondPath.lineTo(SENSOR_RADIUS, 0);
        diamondPath.lineTo(0, SENSOR_RADIUS);
        diamondPath.closePath();
        this.shape = new PPath.Float(diamondPath);
        setPickable(false);
        shape.setPickable(false);
        addChild(shape);
        labelText = new PText();
        labelText.setPickable(false);
        labelText.setFont(labelText.getFont().deriveFont(9.0f));
        updateLabel();
        shape.addChild(labelText);

        updateDispersionCircle();

        sensor.getEvents().onPropertyChange(() -> {
            updateLabel();
            updateDispersionCircle();
        });

    }

    @Override
    public void update(OdorWorldEntity entity) {
        shape.setOffset(
                SimbrainMath.add(
                        sensor.computeRelativeLocation(entity),
                        new Point2D.Double(entity.getWidth() / 2, entity.getHeight() / 2)
                )
        );
        float saturation = (float) SimbrainMath.rescale(sensor.getCurrentValue(), 0, sensor.getBaseValue(),
                0, 1);
        shape.setPaint(Color.getHSBColor(maxColor, saturation, 1));
    }

    public void updateLabel() {
        // TODO: If there is more than one sensor in one spot, labels are on top of each others
        if (sensor.isShowLabel()) {
            labelText.setText(Utils.getWrapAroundString(sensor.getLabel(), 10));
            labelText.setOffset(
                    labelBottomCenterLocation.getX() - labelText.getWidth() / 2,
                    labelBottomCenterLocation.getY() - labelText.getHeight()
            );
        }
        labelText.setVisible(sensor.isShowLabel());
    }

    public void updateDispersionCircle() {
        shape.removeChild(dispersionCircle);
        if (sensor.isShowDispersion()) {
            var dispersion = sensor.getDecayFunction().getDispersion();
            dispersionCircle = PPath.createEllipse(
                    -dispersion / 2,
                    -dispersion / 2,
                    dispersion,
                    dispersion
            );
            dispersionCircle.setPaint(null);
            Stroke dashed = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
                    0, new float[]{3}, 0);
            dispersionCircle.setStroke(dashed);
            dispersionCircle.setStrokePaint(Color.gray);
            shape.addChild(dispersionCircle);
        }
    }
}

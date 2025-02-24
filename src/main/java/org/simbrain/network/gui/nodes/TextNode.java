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

package org.simbrain.network.gui.nodes;

import kotlin.Unit;
import org.jetbrains.annotations.Nullable;
import org.piccolo2d.extras.nodes.PStyledText;
import org.piccolo2d.util.PBounds;
import org.simbrain.network.core.NetworkTextObject;
import org.simbrain.network.events.LocationEvents2;
import org.simbrain.network.gui.NetworkPanel;
import org.simbrain.network.gui.actions.SetTextPropertiesAction;
import org.simbrain.network.gui.actions.edit.CopyAction;
import org.simbrain.network.gui.actions.edit.CutAction;
import org.simbrain.network.gui.actions.edit.PasteAction;
import org.simbrain.util.SwingKt;
import org.simbrain.util.TextUtilitiesKt;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.event.ActionEvent;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.stream.Collectors;

import static org.simbrain.util.GeomKt.minus;
import static org.simbrain.util.GeomKt.plus;

/**
 * An editable text element, which wraps a PStyledText object.
 */
public class TextNode extends ScreenElement implements PropertyChangeListener {

    /**
     * The text object.
     */
    private final PStyledText pStyledText;

    /**
     * Underlying model text object.
     */
    private final NetworkTextObject textObject;

    /**
     * Construct text object at specified location.
     *
     * @param netPanel reference to networkPanel
     * @param text     the network text object
     */
    public TextNode(final NetworkPanel netPanel, final NetworkTextObject text) {
        super(netPanel);
        this.textObject = text;
        pStyledText = new PStyledText();
        pStyledText.setDocument(new DefaultStyledDocument());
        this.addChild(pStyledText);
        this.setBounds(pStyledText.getBounds());
        addPropertyChangeListener(PROPERTY_FULL_BOUNDS, this);

        LocationEvents2 events = text.getEvents();
        events.getDeleted().on(n -> removeFromParent());
        events.getLocationChanged().on(this::pullViewPositionFromModel);

        update();
        pushViewPositionToModel();
    }

    @Override
    public boolean isSelectable() {
        return true;
    }

    @Override
    public boolean isDraggable() {
        return true;
    }

    @Override
    public JPopupMenu getContextMenu() {
        JPopupMenu contextMenu = new JPopupMenu();

        contextMenu.add(new CutAction(getNetworkPanel()));
        contextMenu.add(new CopyAction(getNetworkPanel()));
        contextMenu.add(new PasteAction(getNetworkPanel()));
        contextMenu.addSeparator();

        final var textNodes = getNetworkPanel().getSelectionManager().getSelection().stream()
                .filter(TextNode.class::isInstance)
                .map(TextNode.class::cast)
                .collect(Collectors.toSet());
        textNodes.add(this);

        if (textNodes.size() == 1) {
            contextMenu.add(new AbstractAction() {

                {
                    putValue(Action.NAME, "Edit Text...");
                }

                @Override
                public void actionPerformed(ActionEvent e) {
                    SwingKt.display(TextUtilitiesKt.textEntryDialog(getTextObject().getText(), "Edit Text", 20, 5, (text) -> {
                        getTextObject().setText(text);
                        update();
                        return Unit.INSTANCE;
                    }));
                }
            });
        }

        contextMenu.add(new SetTextPropertiesAction(getNetworkPanel(), textNodes));

        contextMenu.addSeparator();
        contextMenu.add(getNetworkPanel().getNetworkActions().getDeleteAction());

        return contextMenu;
    }

    @Override
    public NetworkTextObject getModel() {
        return getTextObject();
    }

    @Override
    public void propertyChange(PropertyChangeEvent arg0) {
        setBounds(pStyledText.getBounds());
    }

    public PStyledText getPStyledText() {
        return pStyledText;
    }

    /**
     * Update the styled text object based on the model object.
     */
    public void update() {
        try {
            AttributeSet as = TextNode.createAttributeSet(textObject.getFontName(), textObject.getFontSize(), textObject.isItalic(), textObject.isBold());
            pStyledText.getDocument().remove(0, pStyledText.getDocument().getLength());
            pStyledText.getDocument().insertString(0, textObject.getText(), as);
            pStyledText.syncWithDocument();
            pullViewPositionFromModel();
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    public NetworkTextObject getTextObject() {
        return textObject;
    }

    /**
     * Update the position of the model text object based on the global
     * coordinates of this pnode.
     */
    public void pushViewPositionToModel() {
        var p = textObject.getLocation();
        this.setGlobalTranslation(p);
    }

    @Override
    public void offset(double dx, double dy) {
        textObject.setLocation(plus(textObject.getLocation(), new Point2D.Double(dx, dy)));
        pullViewPositionFromModel();
    }

    /**
     * Updates the position of the view text based on the position of the model
     * text object.
     */
    private void pullViewPositionFromModel() {
        PBounds bound = this.getFullBounds();
        Point2D point = minus(
                getTextObject().getLocation(),
                new Point2D.Double(bound.getWidth() / 2, bound.getHeight() / 2)
        );
        this.setGlobalTranslation(point);
    }

    /**
     * Creates an attribute set of the specified kind.
     *
     * @param fontName name of font in attribute set
     * @param fontSize size of font in attribute set
     * @param italic   italic or not
     * @param bold     bold or not
     * @return the resulting attribute set
     * @author Aaron Dixon
     */
    public static SimpleAttributeSet createAttributeSet(String fontName, int fontSize, boolean italic, boolean bold) {
        SimpleAttributeSet as = new SimpleAttributeSet();
        as.addAttribute(StyleConstants.CharacterConstants.FontFamily, fontName);
        as.addAttribute(StyleConstants.CharacterConstants.FontSize, fontSize);
        as.addAttribute(StyleConstants.CharacterConstants.Italic, italic);
        as.addAttribute(StyleConstants.CharacterConstants.Bold, bold);
        as.addAttribute(StyleConstants.ALIGN_RIGHT, true);
        return as;
    }

    @Nullable
    @Override
    public JDialog getPropertyDialog() {
        return TextUtilitiesKt.textEntryDialog(getTextObject().getText(), "Edit Text", 20, 5, (text) -> {
            getTextObject().setText(text);
            update();
            return Unit.INSTANCE;
        });
    }
}

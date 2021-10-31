/*
 * MIT License
 *
 * Copyright (c) 2019 Udo Borkowski, (ub@abego.org)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.abego.guitesting.swing.internal.snapshotreview;

import org.abego.guitesting.swing.internal.util.Widget;
import org.abego.guitesting.swing.internal.util.prop.IProp;
import org.abego.guitesting.swing.internal.util.prop.PropService;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Font;

import static javax.swing.BorderFactory.createLineBorder;
import static org.abego.guitesting.swing.internal.util.prop.PropService.newProp;
import static org.abego.guitesting.swing.internal.util.SwingUtil.DEFAULT_FLOW_GAP;
import static org.abego.guitesting.swing.internal.util.SwingUtil.flowLeft;

class ImagesLegendWidget implements Widget {

    //region State/Model
    //region @Prop public Integer expectedImageIndex = 0
    @SuppressWarnings("DuplicateStringLiteralInspection")
    private final IProp<Integer> expectedImageIndexProp =
            newProp(0, this, "expectedImageIndex");

    public Integer getExpectedImageIndex() {
        return expectedImageIndexProp.get();
    }

    @SuppressWarnings("unused")
    public void setExpectedImageIndex(Integer value) {
        expectedImageIndexProp.set(value);
    }

    public void bindExpectedImageIndexTo(IProp<Integer> prop) {
        expectedImageIndexProp.bindTo(prop);
    }

    //endregion
    //region @Prop public Color expectedBorderColor = Color.green
    @SuppressWarnings("DuplicateStringLiteralInspection")
    private final IProp<Color> expectedBorderColorProp = PropService.newProp(Color.green, this, "expectedBorderColor");

    public Color getExpectedBorderColor() {
        return expectedBorderColorProp.get();
    }

    public void setExpectedBorderColor(Color color) {
        expectedBorderColorProp.set(color);
    }

    //endregion
    //region @Prop public Color actualBorderColor = Color.red
    @SuppressWarnings("DuplicateStringLiteralInspection")
    private final IProp<Color> actualBorderColorProp = PropService.newProp(Color.red, this, "actualBorderColor");

    public Color getActualBorderColor() {
        return actualBorderColorProp.get();
    }

    public void setActualBorderColor(Color color) {
        actualBorderColorProp.set(color);
    }

    //endregion
    //region @Prop public Color differenceBorderColor = Color.black
    @SuppressWarnings("DuplicateStringLiteralInspection")
    private final IProp<Color> differenceBorderColorProp = PropService.newProp(Color.black, this, "differenceBorderColor");

    public Color getDifferenceBorderColor() {
        return differenceBorderColorProp.get();
    }

    public void setDifferenceBorderColor(Color color) {
        differenceBorderColorProp.set(color);
    }

    //endregion
    //endregion
    //region Components
    private final JLabel expectedLabel = legendLabel(" Expected ");//NON-NLS
    private final JLabel actualLabel = legendLabel(" Actual ");//NON-NLS
    private final JLabel differenceLabel = legendLabel(" Difference ");//NON-NLS
    private final JLabel[] labels = new JLabel[]{
            expectedLabel, actualLabel, differenceLabel
    };
    private final JComponent content = flowLeft(DEFAULT_FLOW_GAP, 0);

    //endregion
    //region Construction
    private ImagesLegendWidget() {
        initBindings();
        setLegendLabelBorderColor(differenceLabel, getDifferenceBorderColor());
    }

    public static ImagesLegendWidget imagesLegendWidget() {
        return new ImagesLegendWidget();
    }

    //endregion
    //region Widget related
    public JComponent getContent() {
        return content;
    }


    private void updateContent() {
        SwingUtilities.invokeLater(() -> {
            content.removeAll();
            content.add(labels[(3 - getExpectedImageIndex()) % 3]);
            content.add(labels[(4 - getExpectedImageIndex()) % 3]);
            content.add(labels[(5 - getExpectedImageIndex()) % 3]);
            content.validate();
        });
    }

    private static JLabel legendLabel(String title) {
        JLabel label = new JLabel(title);
        label.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 12));
        label.setForeground(Color.GRAY);
        //noinspection StringConcatenation
        label.setToolTipText(
                title.trim() + " images (see below) have a border in this color and are located at this position in the sequence."); //NON-NLS
        return label;
    }

    //endregion
    //region Style related
    private static final int LEGEND_BORDER_SIZE = 2;

    private static void setLegendLabelBorderColor(JLabel label, Color color) {
        label.setBorder(createLineBorder(color, LEGEND_BORDER_SIZE));
    }
    //endregion
    //region Binding related
    private void initBindings() {
        expectedImageIndexProp.runDependingCode(this::updateContent);

        expectedBorderColorProp.runDependingCode(() ->
                setLegendLabelBorderColor(expectedLabel, getExpectedBorderColor()));
        actualBorderColorProp.runDependingCode(() ->
                setLegendLabelBorderColor(actualLabel, getActualBorderColor()));
        differenceBorderColorProp.runDependingCode(() ->
                setLegendLabelBorderColor(differenceLabel, getDifferenceBorderColor()));
    }

    //endregion

}

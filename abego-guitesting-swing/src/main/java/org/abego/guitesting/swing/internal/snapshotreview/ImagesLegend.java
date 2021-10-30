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

import org.abego.guitesting.swing.internal.util.prop.Prop;
import org.abego.guitesting.swing.internal.util.prop.PropBindable;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Font;

import static javax.swing.BorderFactory.createLineBorder;
import static org.abego.guitesting.swing.internal.util.prop.PropBindable.newPropBindable;
import static org.abego.guitesting.swing.internal.util.SwingUtil.DEFAULT_FLOW_GAP;
import static org.abego.guitesting.swing.internal.util.SwingUtil.flowLeft;

class ImagesLegend implements Widget {

    private static final int LEGEND_BORDER_SIZE = 2;

    private final JLabel expectedLabel = legendLabel(" Expected ");//NON-NLS
    private final JLabel actualLabel = legendLabel(" Actual ");//NON-NLS
    private final JLabel differenceLabel = legendLabel(" Difference ");//NON-NLS
    private final JLabel[] labels = new JLabel[]{
            expectedLabel, actualLabel, differenceLabel
    };
    private final JComponent content = flowLeft(DEFAULT_FLOW_GAP, 0);
    private Color expectedBorderColor = Color.green;
    private Color actualBorderColor = Color.red;
    private Color differenceBorderColor = Color.black;

    public static ImagesLegend imagesLegend() {
        return new ImagesLegend();
    }

    //region expectedImageIndex
    @SuppressWarnings("DuplicateStringLiteralInspection")
    private final PropBindable<Integer> expectedImageIndexProp =
            newPropBindable(0, this, "expectedImageIndex", f -> onExpectedImageIndexChanged());

    public Integer getExpectedImageIndex() {
        return expectedImageIndexProp.get();
    }

    public void setExpectedImageIndex(Integer value) {
        expectedImageIndexProp.set(value);
    }

    public void bindExpectedImageIndexTo(Prop<Integer> prop) {
        expectedImageIndexProp.bindTo(prop);
    }

    //endregion

    public Color getExpectedBorderColor() {
        return expectedBorderColor;
    }

    public void setExpectedBorderColor(Color color) {
        this.expectedBorderColor = color;

        onExpectedBorderColorChanged();
    }

    public Color getActualBorderColor() {
        return actualBorderColor;
    }

    public void setActualBorderColor(Color color) {
        this.actualBorderColor = color;

        onActualBorderColorChanged();
    }

    public Color getDifferenceBorderColor() {
        return differenceBorderColor;
    }

    public void setDifferenceBorderColor(Color color) {
        this.differenceBorderColor = color;

        onDifferenceBorderColorChanged();
    }

    public JComponent getComponent() {
        return content;
    }

    private ImagesLegend() {
        onExpectedImageIndexChanged();
        onExpectedBorderColorChanged();
        onActualBorderColorChanged();
        onDifferenceBorderColorChanged();
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

    private void onExpectedImageIndexChanged() {
        SwingUtilities.invokeLater(() -> {
            content.removeAll();
            content.add(labels[(3 - getExpectedImageIndex()) % 3]);
            content.add(labels[(4 - getExpectedImageIndex()) % 3]);
            content.add(labels[(5 - getExpectedImageIndex()) % 3]);
            content.validate();
        });
    }

    private void onExpectedBorderColorChanged() {
        setLegendLabelBorderColor(expectedLabel, getExpectedBorderColor());
    }

    private void onActualBorderColorChanged() {
        setLegendLabelBorderColor(actualLabel, getActualBorderColor());
    }

    private void onDifferenceBorderColorChanged() {
        setLegendLabelBorderColor(differenceLabel, getDifferenceBorderColor());
    }

    private static void setLegendLabelBorderColor(JLabel label, Color color) {
        label.setBorder(createLineBorder(color, LEGEND_BORDER_SIZE));
    }


}

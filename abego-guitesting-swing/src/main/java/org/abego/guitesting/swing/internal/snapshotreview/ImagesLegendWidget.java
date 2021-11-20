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

import org.abego.guitesting.swing.internal.util.prop.Bindings;
import org.abego.guitesting.swing.internal.util.prop.Prop;
import org.abego.guitesting.swing.internal.util.prop.PropService;
import org.abego.guitesting.swing.internal.util.prop.PropServices;
import org.abego.guitesting.swing.internal.util.widget.HStackWidget;
import org.abego.guitesting.swing.internal.util.widget.LabelWidget;
import org.abego.guitesting.swing.internal.util.widget.Widget;

import javax.swing.JComponent;
import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

import static org.abego.guitesting.swing.internal.util.boxstyle.BoxStyle.newBoxStyle;
import static org.abego.guitesting.swing.internal.util.widget.HStackWidget.hStackWidget;

class ImagesLegendWidget implements Widget {

    private static final int GAP = 5;

    //region State/Model
    private final PropService propService = PropServices.getDefault();
    //region prop expectedImageIndex: Integer
    @SuppressWarnings("DuplicateStringLiteralInspection")
    private final Prop<Integer> expectedImageIndexProp =
            propService.newProp(0, this, "expectedImageIndex");

    public Integer getExpectedImageIndex() {
        return expectedImageIndexProp.get();
    }

    @SuppressWarnings("unused")
    public void setExpectedImageIndex(Integer value) {
        getExpectedImageIndexProp().set(value);
    }

    public Prop<Integer> getExpectedImageIndexProp() {
        return expectedImageIndexProp;
    }

    //endregion
    //region prop expectedBorderColor: Color = Color.green
    @SuppressWarnings("DuplicateStringLiteralInspection")
    private final Prop<Color> expectedBorderColorProp = propService.newProp(Color.green, this, "expectedBorderColor");

    public Color getExpectedBorderColor() {
        return expectedBorderColorProp.get();
    }

    public void setExpectedBorderColor(Color color) {
        expectedBorderColorProp.set(color);
    }

    //endregion
    //region prop actualBorderColor: Color = Color.red
    @SuppressWarnings("DuplicateStringLiteralInspection")
    private final Prop<Color> actualBorderColorProp = propService.newProp(Color.red, this, "actualBorderColor");

    public Color getActualBorderColor() {
        return actualBorderColorProp.get();
    }

    public void setActualBorderColor(Color color) {
        actualBorderColorProp.set(color);
    }

    //endregion
    //region prop differenceBorderColor: Color  = Color.black
    @SuppressWarnings("DuplicateStringLiteralInspection")
    private final Prop<Color> differenceBorderColorProp = propService.newProp(Color.black, this, "differenceBorderColor");

    public Color getDifferenceBorderColor() {
        return differenceBorderColorProp.get();
    }

    public void setDifferenceBorderColor(Color color) {
        differenceBorderColorProp.set(color);
    }

    //endregion
    //endregion
    //region Components
    private final LabelWidget expectedLabel = legendLabel(" Expected ");//NON-NLS
    private final LabelWidget actualLabel = legendLabel(" Actual ");//NON-NLS
    private final LabelWidget differenceLabel = legendLabel(" Difference ");//NON-NLS
    private final LabelWidget[] labels = new LabelWidget[]{
            expectedLabel, actualLabel, differenceLabel
    };
    private final HStackWidget contentWidget = hStackWidget();

    //endregion
    //region Construction
    private ImagesLegendWidget() {
        contentWidget.setSpacing(GAP);
        initBindings();
        setLegendLabelBorderColor(differenceLabel, getDifferenceBorderColor());
    }

    public static ImagesLegendWidget imagesLegendWidget() {
        return new ImagesLegendWidget();
    }

    //endregion
    //region Widget related
    public JComponent getContent() {
        return contentWidget.getContent();
    }

    public void close() {
        bindings.close();
    }

    private void updateContent() {
        //TODO: make this class and ExpectedActualDifferenceImageWidget use
        // same approach to define and update the content
        List<Widget> items = new ArrayList<>();
        items.add(labels[(3 - getExpectedImageIndex()) % 3]);
        items.add(labels[(4 - getExpectedImageIndex()) % 3]);
        items.add(labels[(5 - getExpectedImageIndex()) % 3]);
        contentWidget.setItems(items);
    }

    private static LabelWidget legendLabel(String title) {
        LabelWidget label = LabelWidget.labelWidget();
        label.setText(title);
        label.getContent().setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 12));
        label.getContent().setForeground(Color.GRAY);
        //noinspection StringConcatenation
        label.getContent().setToolTipText(
                title.trim() + " images (see below) have a border in this color and are located at this position in the sequence."); //NON-NLS
        return label;
    }

    //endregion
    //region Style related
    private static final int LEGEND_BORDER_SIZE = 2;

    private static void setLegendLabelBorderColor(LabelWidget label, Color color) {
        label.setBoxStyle(newBoxStyle().border(LEGEND_BORDER_SIZE, color));
    }

    //endregion
    //region Binding related
    private final Bindings bindings = propService.newBindings();

    private void initBindings() {
        bindings.bindSwingCode(this::updateContent, expectedImageIndexProp);

        bindings.bindSwingCode(() ->
                setLegendLabelBorderColor(expectedLabel, getExpectedBorderColor()), expectedBorderColorProp);
        bindings.bindSwingCode(() ->
                setLegendLabelBorderColor(actualLabel, getActualBorderColor()), actualBorderColorProp);
        bindings.bindSwingCode(() ->
                setLegendLabelBorderColor(differenceLabel, getDifferenceBorderColor()), differenceBorderColorProp);
    }

    //endregion

}

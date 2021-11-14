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

import org.abego.guitesting.swing.ScreenCaptureSupport.SnapshotIssue;
import org.abego.guitesting.swing.internal.util.SwingUtil;
import org.abego.guitesting.swing.internal.util.prop.Bindings;
import org.abego.guitesting.swing.internal.util.prop.DependencyCollector;
import org.abego.guitesting.swing.internal.util.prop.Prop;
import org.abego.guitesting.swing.internal.util.prop.PropComputedNullable;
import org.abego.guitesting.swing.internal.util.prop.PropNullable;
import org.abego.guitesting.swing.internal.util.prop.PropService;
import org.abego.guitesting.swing.internal.util.prop.PropServices;
import org.abego.guitesting.swing.internal.util.widget.Widget;
import org.eclipse.jdt.annotation.Nullable;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Rectangle;

import static java.lang.Boolean.FALSE;
import static java.lang.Math.max;
import static org.abego.guitesting.swing.internal.snapshotreview.SnapshotImages.snapshotImages;
import static org.abego.guitesting.swing.internal.util.SwingUtil.addAll;
import static org.abego.guitesting.swing.internal.util.SwingUtil.onComponentResized;

class ExpectedActualDifferenceImageWidget implements Widget {

    //region State/Model
    private final PropService propService = PropServices.getDefault();
    //region @Prop public Boolean shrinkToFit = FALSE
    @SuppressWarnings("DuplicateStringLiteralInspection")
    private final Prop<Boolean> shrinkToFitProp =
            propService.newProp(FALSE, this, "shrinkToFit");

    public Boolean getShrinkToFit() {
        return shrinkToFitProp.get();
    }

    @SuppressWarnings("unused")
    public void setShrinkToFit(Boolean value) {
        shrinkToFitProp.set(value);
    }

    public Prop<Boolean> getShrinkToFitProp() {return shrinkToFitProp;}

    //endregion
    //region @Prop private @Nullable SnapshotImages snapshotImages
    private final PropComputedNullable<SnapshotImages> snapshotImagesProp =
            propService.newPropComputedNullable(this::getSnapshotImages);

    private @Nullable SnapshotImages getSnapshotImages(DependencyCollector collector) {
        @Nullable SnapshotIssue issue = snapshotIssueProp.get(collector);
        if (issue == null) {
            return null;
        }
        return snapshotImages(issue, imagesAreaProp.get(collector));
    }

    private @Nullable SnapshotImages getSnapshotImages() {
        return snapshotImagesProp.get();
    }
    //endregion

    //region @Prop public @Nullable SnapshotIssue snapshotIssue
    private final PropNullable<SnapshotIssue> snapshotIssueProp =
            propService.newPropNullable(null, this, "snapshotIssue");

    @Nullable
    public SnapshotIssue getSnapshotIssue() {
        return snapshotIssueProp.get();
    }

    public void setSnapshotIssue(@Nullable SnapshotIssue value) {
        snapshotIssueProp.set(value);
    }

    public PropNullable<SnapshotIssue> getSnapshotIssueProp() {
        return snapshotIssueProp;
    }

    //endregion
    //region @Prop public Integer expectedImageIndex = 0
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
    //region @Prop public Color expectedBorderColor = Color.green
    @SuppressWarnings("DuplicateStringLiteralInspection")
    private final Prop<Color> expectedBorderColorProp =
            propService.newProp(Color.green, this, "expectedBorderColor");

    public Color getExpectedBorderColor() {
        return expectedBorderColorProp.get();
    }

    public void setExpectedBorderColor(Color value) {
        getExpectedBorderColorProp().set(value);
    }

    public Prop<Color> getExpectedBorderColorProp() {
        return expectedBorderColorProp;
    }

    //endregion
    //region @Prop public Color actualBorderColor = Color.red
    @SuppressWarnings("DuplicateStringLiteralInspection")
    private final Prop<Color> actualBorderColorProp =
            propService.newProp(Color.red, this, "actualBorderColor");

    public Color getActualBorderColor() {
        return actualBorderColorProp.get();
    }

    public void setActualBorderColor(Color value) {
        getActualBorderColorProp().set(value);
    }

    public Prop<Color> getActualBorderColorProp() {
        return actualBorderColorProp;
    }

    //endregion
    //region @Prop public Color differenceBorderColor = Color.black
    @SuppressWarnings("DuplicateStringLiteralInspection")
    private final Prop<Color> differenceBorderColorProp =
            propService.newProp(Color.black, this, "differenceBorderColor");

    public Color getDifferenceBorderColor() {
        return differenceBorderColorProp.get();
    }

    public void setDifferenceBorderColor(Color value) {
        getDifferenceBorderColorProp().set(value);
    }

    public Prop<Color> getDifferenceBorderColorProp() {
        return differenceBorderColorProp;
    }

    //endregion
    //region @Prop public Color differenceBorderColor = Color.black
    private final PropComputedNullable<Dimension> imagesAreaProp =
            propService.newPropComputedNullable(this::calcImagesArea);


    @Nullable
    private Dimension getImagesArea() {
        return imagesAreaProp.get();
    }

    public PropNullable<Dimension> getImagesAreaProp() {
        return imagesAreaProp;
    }

    private @Nullable Dimension calcImagesArea(DependencyCollector dependencyCollector) {

        if (shrinkToFitProp.get(dependencyCollector)) {
            Rectangle visibleRect = content.getVisibleRect();
            int w = visibleRect.width - 4 * SwingUtil.DEFAULT_FLOW_GAP - 6 * BORDER_SIZE;
            int h = visibleRect.height - 2 * SwingUtil.DEFAULT_FLOW_GAP - 2 * BORDER_SIZE;
            return new Dimension(max(MIN_IMAGE_SIZE, w), max(MIN_IMAGE_SIZE, h));
        } else {
            return null;
        }
    }

    //endregion
    //endregion
    //region Components
    private final JLabel[] contentLabels =
            new JLabel[]{new JLabel(), new JLabel(), new JLabel()};
    private final JComponent content = new JPanel();

    //endregion
    //region Construction/Closing
    public static ExpectedActualDifferenceImageWidget expectedActualDifferenceImageWidget() {
        return new ExpectedActualDifferenceImageWidget();
    }

    private ExpectedActualDifferenceImageWidget() {
        styleComponents();
        layoutComponents();
        initBinding();
    }

    public void close() {
        bindings.close();
    }

    //endregion
    //region Widget related
    @Override
    public JComponent getContent() {
        return content;
    }

    private static final int MIN_IMAGE_SIZE = 16;

    private void updateContentLabels() {
        @Nullable SnapshotImages images = getSnapshotImages();
        if (images != null) {
            setIconAndLinedBorder(
                    contentLabels[(getExpectedImageIndex()) % 3],
                    images.getExpectedImage(),
                    getExpectedBorderColor());
            setIconAndLinedBorder(
                    contentLabels[(getExpectedImageIndex() + 1) % 3],
                    images.getActualImage(),
                    getActualBorderColor());
            setIconAndLinedBorder(
                    contentLabels[(getExpectedImageIndex() + 2) % 3],
                    images.getDifferenceImage(),
                    getDifferenceBorderColor());
        }
        SwingUtil.setVisible(images != null, contentLabels);
    }

    private static void setIconAndLinedBorder(JLabel label, ImageIcon icon, Color borderColor) {
        label.setIcon(icon);
        label.setBorder(SwingUtil.lineBorder(borderColor, BORDER_SIZE));
    }

    //endregion
    //region Style related
    private static final int BORDER_SIZE = 3;

    private void styleComponents() {
        content.setOpaque(true);
        content.setBackground(Color.white);
        content.setBorder(null);
    }

    //endregion Style
    //region Layout related
    private void layoutComponents() {
        content.setLayout(new FlowLayout(FlowLayout.LEADING));
        addAll(content, contentLabels);
    }

    //endregion
    //region Binding related
    private Bindings bindings = propService.newBindings();

    private void initBinding() {
        onComponentResized(content, e -> imagesAreaProp.compute());

        bindings.bindSwingCode(this::updateContentLabels,
                snapshotImagesProp,
                expectedImageIndexProp,
                expectedBorderColorProp,
                actualBorderColorProp,
                differenceBorderColorProp);
    }

    //endregion
}

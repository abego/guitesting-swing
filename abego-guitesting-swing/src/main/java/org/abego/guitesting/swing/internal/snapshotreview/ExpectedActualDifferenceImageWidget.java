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
import org.abego.guitesting.swing.internal.util.boxstyle.BoxStyle;
import org.abego.guitesting.swing.internal.util.prop.Bindings;
import org.abego.guitesting.swing.internal.util.prop.DependencyCollector;
import org.abego.guitesting.swing.internal.util.prop.Prop;
import org.abego.guitesting.swing.internal.util.prop.PropComputedNullable;
import org.abego.guitesting.swing.internal.util.prop.PropNullable;
import org.abego.guitesting.swing.internal.util.prop.PropService;
import org.abego.guitesting.swing.internal.util.prop.PropServices;
import org.abego.guitesting.swing.internal.util.widget.BorderedWidget;
import org.abego.guitesting.swing.internal.util.widget.GUIKitForSwing;
import org.abego.guitesting.swing.internal.util.widget.HStackWidget;
import org.abego.guitesting.swing.internal.util.widget.ImageWidget;
import org.abego.guitesting.swing.internal.util.widget.Widget;
import org.abego.guitesting.swing.internal.util.widget.WidgetUtil;
import org.eclipse.jdt.annotation.Nullable;

import javax.swing.JComponent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;

import static java.lang.Boolean.FALSE;
import static java.lang.Math.max;
import static org.abego.guitesting.swing.internal.snapshotreview.SnapshotImages.snapshotImages;
import static org.abego.guitesting.swing.internal.util.SwingUtil.onComponentResized;
import static org.abego.guitesting.swing.internal.util.boxstyle.BoxStyle.newBoxStyle;
import static org.abego.guitesting.swing.internal.util.widget.BorderedWidget.borderedWidget;

class ExpectedActualDifferenceImageWidget implements Widget {

    //region State/Model
    private final PropService propService = PropServices.getDefault();
    //region prop shrinkToFit: Boolean
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
    //region prop snapshotIssue: SnapshotIssue?
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
    //region prop actualBorderColorColor: Color = Color.red
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
    //region prop differenceBorderColor: Color = Color.black
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
    //region prop imagesArea: Dimension {calcImagesArea()}
    private final PropComputedNullable<Dimension> imagesAreaProp =
            propService.newPropComputedNullable(this::calcImagesArea,this, "imagesArea");

    @Nullable
    private Dimension getImagesArea() {
        return imagesAreaProp.get();
    }

    public PropComputedNullable<Dimension> getImagesAreaProp() {
        return imagesAreaProp;
    }

    private @Nullable Dimension calcImagesArea(DependencyCollector dependencyCollector) {

        if (shrinkToFitProp.get(dependencyCollector)) {
            Rectangle visibleRect = contentWidget.getVisibleRect();
            // from the full avaiable visible rect calculate how large is
            // the area available for the images. To do so we need to subtract
            // - the border around the images (of size BORDER_SIZE),
            // - the extra PADDING around the (bordered) images,
            // - the GAP between an (bordered) image and the image right to it.
            int w = visibleRect.width - 2 * PADDING - 2 * GAP - 6 * BORDER_SIZE;
            int h = visibleRect.height - 2 * PADDING - 2 * BORDER_SIZE;

            // to avoid weird effects make sure the dimension is not below
            // a certain size (MIN_IMAGE_SIZE each side)
            return new Dimension(max(MIN_IMAGE_SIZE, w), max(MIN_IMAGE_SIZE, h));
        } else {
            return null;
        }
    }

    //endregion
    //region private prop snapshotImages : SnapshotImages?
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
    //endregion
    //region Components
    private final ImageWidget[] imageWidgets =
            new ImageWidget[]{new ImageWidget(), new ImageWidget(), new ImageWidget()};
    private final HStackWidget threeImages = GUIKitForSwing.getDefault().hStackWidget();
    private final BorderedWidget contentWidget = borderedWidget();

    //endregion
    //region Construction
    public static ExpectedActualDifferenceImageWidget expectedActualDifferenceImageWidget() {
        return new ExpectedActualDifferenceImageWidget();
    }

    private ExpectedActualDifferenceImageWidget() {
        layoutComponents();
        styleComponents();
        initBinding();
    }

    private void layoutComponents() {
        threeImages.setItems(imageWidgets);
        contentWidget.center(threeImages);
    }

    //endregion
    //region Widget related
    @Override
    public JComponent getContent() {
        return contentWidget.getContent();
    }

    public void close() {
        bindings.close();
    }

    private static final int MIN_IMAGE_SIZE = 16;
    private static final int BORDER_SIZE = 3;

    private void updateImageWidgets() {
        //TODO: make this class and ImagesLegendWidget use
        // same approach to define and update the content
        @Nullable SnapshotImages images = getSnapshotImages();
        if (images != null) {
            ImageWidget expected = imageWidgets[(getExpectedImageIndex()) % 3];
            expected.setImage(images.getExpectedImage());
            expected.setBoxStyle(borderWithColor(getExpectedBorderColor()));

            ImageWidget actual = imageWidgets[(getExpectedImageIndex() + 1) % 3];
            actual.setImage(images.getActualImage());
            actual.setBoxStyle(borderWithColor(getActualBorderColor()));

            ImageWidget diff = imageWidgets[(getExpectedImageIndex() + 2) % 3];
            diff.setImage(images.getDifferenceImage());
            diff.setBoxStyle(borderWithColor(getDifferenceBorderColor()));
        }

        WidgetUtil.setVisible(images != null, imageWidgets);
    }

    private static BoxStyle.Factory borderWithColor(Color color) {
        return newBoxStyle().border(BORDER_SIZE, color);
    }

    //endregion
    //region Style related
    /**
     * the GAP between an (bordered) image and the image right to it.
     */
    private final static int GAP = 5;

    /**
     * the extra PADDING around the (bordered) images
     */
    private final static int PADDING = 5;

    private void styleComponents() {
        threeImages.setSpacing(GAP);
        threeImages.setBoxStyle(newBoxStyle()
                .padding(PADDING)
                .background(Color.WHITE));
    }

    //endregion Style
    //region Binding related
    private final Bindings bindings = propService.newBindings();

    private void initBinding() {
        onComponentResized(contentWidget.getContent(),
                e -> getImagesAreaProp().compute());

        bindings.bindSwingCode(this::updateImageWidgets,
                snapshotImagesProp,
                expectedImageIndexProp,
                expectedBorderColorProp,
                actualBorderColorProp,
                differenceBorderColorProp);
    }

    //endregion
}

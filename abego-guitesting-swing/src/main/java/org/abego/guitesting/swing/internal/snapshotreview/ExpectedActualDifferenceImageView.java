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
import org.abego.guitesting.swing.internal.util.Widget;
import org.abego.guitesting.swing.internal.util.prop.Prop;
import org.abego.guitesting.swing.internal.util.prop.PropBindable;
import org.abego.guitesting.swing.internal.util.prop.PropNullable;
import org.abego.guitesting.swing.internal.util.prop.PropNullableBindable;
import org.abego.guitesting.swing.internal.util.SwingUtil;
import org.eclipse.jdt.annotation.Nullable;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.Boolean.FALSE;
import static java.lang.Math.max;
import static org.abego.guitesting.swing.internal.snapshotreview.SnapshotImages.snapshotImages;
import static org.abego.guitesting.swing.internal.util.prop.PropBindable.newPropBindable;
import static org.abego.guitesting.swing.internal.util.prop.PropNullableBindable.newPropNullableBindable;
import static org.abego.guitesting.swing.internal.util.SwingUtil.flowLeft;
import static org.abego.guitesting.swing.internal.util.SwingUtil.invokeLaterOnce;
import static org.abego.guitesting.swing.internal.util.SwingUtil.onComponentResized;

class ExpectedActualDifferenceImageView implements Widget {
    private static final int BORDER_SIZE = 3;
    private static final int MIN_IMAGE_SIZE = 16;

    private final AtomicBoolean mustUpdateLabelsForImages = new AtomicBoolean();

    private final JLabel[] labelsForImages =
            new JLabel[]{new JLabel(), new JLabel(), new JLabel()};
    //TODO: extra class? HList?
    private final JComponent content = flowLeft(c -> {
        c.setOpaque(true);
        c.setBackground(Color.white);
        c.setBorder(null);
    }, labelsForImages);

    public static ExpectedActualDifferenceImageView expectedActualDifferenceImageView() {
        return new ExpectedActualDifferenceImageView();
    }

    //region shrinkToFit
    @SuppressWarnings("DuplicateStringLiteralInspection")
    private final PropBindable<Boolean> shrinkToFitProp =
            newPropBindable(FALSE, this, "shrinkToFit", f -> updateLabelsForImages());

    public Boolean getShrinkToFit() {
        return shrinkToFitProp.get();
    }

    public void setShrinkToFit(Boolean value) {
        shrinkToFitProp.set(value);
    }

    public void bindShrinkToFitTo(Prop<Boolean> prop) {
        shrinkToFitProp.bindTo(prop);
    }

    //endregion
    //region snapshotIssue
    private final PropNullableBindable<SnapshotIssue> snapshotIssueProp =
            newPropNullableBindable(null, this, "snapshotIssue", f -> updateLabelsForImages());

    @Nullable
    public SnapshotIssue getSnapshotIssue() {
        return snapshotIssueProp.get();
    }

    public void setSnapshotIssue(@Nullable SnapshotIssue value) {
        snapshotIssueProp.set(value);
    }

    public void bindSnapshotIssueTo(PropNullable<SnapshotIssue> prop) {
        snapshotIssueProp.bindTo(prop);
    }

    //endregion
    //region expectedImageIndex
    @SuppressWarnings("DuplicateStringLiteralInspection")
    private final PropBindable<Integer> expectedImageIndexProp =
            newPropBindable(0, this, "expectedImageIndex", f -> updateLabelsForImages());

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
    //region expectedBorderColor
    private final PropBindable<Color> expectedBorderColorProp =
            newPropBindable(Color.green, this, "expectedBorderColor", f -> updateLabelsForImages());

    public Color getExpectedBorderColor() {
        return expectedBorderColorProp.get();
    }

    public void setExpectedBorderColor(Color value) {
        expectedBorderColorProp.set(value);
    }

    public void bindExpectedBorderColorTo(Prop<Color> prop) {
        expectedBorderColorProp.bindTo(prop);
    }

    //endregion
    //region actualBorderColor
    private final PropBindable<Color> actualBorderColorProp =
            newPropBindable(Color.red, this, "actualBorderColor", f -> updateLabelsForImages());

    public Color getActualBorderColor() {
        return actualBorderColorProp.get();
    }

    public void setActualBorderColor(Color value) {
        actualBorderColorProp.set(value);
    }

    public void bindActualBorderColorTo(Prop<Color> prop) {
        actualBorderColorProp.bindTo(prop);
    }

    //endregion
    //region differenceBorderColor
    private final PropBindable<Color> differenceBorderColorProp =
            newPropBindable(Color.black, this, "differenceBorderColor", f -> updateLabelsForImages());

    public Color getDifferenceBorderColor() {
        return differenceBorderColorProp.get();
    }

    public void setDifferenceBorderColor(Color value) {
        differenceBorderColorProp.set(value);
    }

    public void bindDifferenceBorderColorTo(Prop<Color> prop) {
        differenceBorderColorProp.bindTo(prop);
    }

    //endregion

    @Override
    public JComponent getContent() {
        return content;
    }

    private ExpectedActualDifferenceImageView() {
        onComponentResized(content, e -> onContentResized());
    }

    private void onContentResized() {
        if (getShrinkToFit()) {
            updateLabelsForImages();
        }
    }

    private void updateLabelsForImages() {
        invokeLaterOnce(mustUpdateLabelsForImages, () -> {
            @Nullable SnapshotImages images = getSnapshotImages();
            if (images != null) {
                setIconAndLinedBorder(
                        labelsForImages[(getExpectedImageIndex()) % 3],
                        images.getExpectedImage(),
                        getExpectedBorderColor());
                setIconAndLinedBorder(
                        labelsForImages[(getExpectedImageIndex() + 1) % 3],
                        images.getActualImage(),
                        getActualBorderColor());
                setIconAndLinedBorder(
                        labelsForImages[(getExpectedImageIndex() + 2) % 3],
                        images.getDifferenceImage(),
                        getDifferenceBorderColor());
            }
            SwingUtil.setVisible(images != null, labelsForImages);
        });
    }

    private void setIconAndLinedBorder(JLabel label, ImageIcon icon, Color borderColor) {
        label.setIcon(icon);
        label.setBorder(SwingUtil.lineBorder(borderColor, BORDER_SIZE));
    }

    private @Nullable Dimension getImagesArea() {
        if (getShrinkToFit()) {
            Rectangle visibleRect = content.getVisibleRect();
            int w = visibleRect.width - 4 * SwingUtil.DEFAULT_FLOW_GAP - 6 * BORDER_SIZE;
            int h = visibleRect.height - 2 * SwingUtil.DEFAULT_FLOW_GAP - 2 * BORDER_SIZE;
            return new Dimension(max(MIN_IMAGE_SIZE, w), max(MIN_IMAGE_SIZE, h));
        } else {
            return null;
        }
    }

    private @Nullable SnapshotImages getSnapshotImages() {
        @Nullable SnapshotIssue issue = getSnapshotIssue();
        if (issue == null) {
            return null;
        }
        return snapshotImages(issue, getImagesArea());
    }

}

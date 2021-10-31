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

import org.abego.event.EventService;
import org.abego.event.EventServices;
import org.abego.guitesting.swing.ScreenCaptureSupport.SnapshotIssue;
import org.abego.guitesting.swing.internal.util.Widget;
import org.abego.guitesting.swing.internal.util.prop.Prop;
import org.abego.guitesting.swing.internal.util.SwingUtil;
import org.abego.guitesting.swing.internal.util.prop.PropNullable;
import org.abego.guitesting.swing.internal.util.prop.PropService;
import org.abego.guitesting.swing.internal.util.prop.PropServices;
import org.abego.guitesting.swing.internal.util.prop.SourceOfTruthNullable;
import org.eclipse.jdt.annotation.Nullable;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.Boolean.FALSE;
import static java.lang.Math.max;
import static org.abego.guitesting.swing.internal.snapshotreview.SnapshotImages.snapshotImages;
import static org.abego.guitesting.swing.internal.util.SwingUtil.addAll;
import static org.abego.guitesting.swing.internal.util.SwingUtil.invokeLaterOnce;
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

    public void bindShrinkToFitTo(Prop<Boolean> prop) {
        shrinkToFitProp.bindTo(prop);
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

    public void bindSnapshotIssueTo(SourceOfTruthNullable<SnapshotIssue> prop) {
        snapshotIssueProp.bindTo(prop);
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
        expectedImageIndexProp.set(value);
    }

    public void bindExpectedImageIndexTo(Prop<Integer> prop) {
        expectedImageIndexProp.bindTo(prop);
    }

    //endregion
    //region @Prop public Color expectedBorderColor = Color.green
    private final Prop<Color> expectedBorderColorProp =
            propService.newProp(Color.green, this, "expectedBorderColor");

    public Color getExpectedBorderColor() {
        return expectedBorderColorProp.get();
    }

    public void setExpectedBorderColor(Color value) {
        expectedBorderColorProp.set(value);
    }

    @SuppressWarnings("unused")
    public void bindExpectedBorderColorTo(Prop<Color> prop) {
        expectedBorderColorProp.bindTo(prop);
    }

    //endregion
    //region @Prop public Color actualBorderColor = Color.red
    private final Prop<Color> actualBorderColorProp =
            propService.newProp(Color.red, this, "actualBorderColor");

    public Color getActualBorderColor() {
        return actualBorderColorProp.get();
    }

    public void setActualBorderColor(Color value) {
        actualBorderColorProp.set(value);
    }

    @SuppressWarnings("unused")
    public void bindActualBorderColorTo(Prop<Color> prop) {
        actualBorderColorProp.bindTo(prop);
    }

    //endregion
    //region @Prop public Color differenceBorderColor = Color.black
    private final Prop<Color> differenceBorderColorProp =
            propService.newProp(Color.black, this, "differenceBorderColor");

    public Color getDifferenceBorderColor() {
        return differenceBorderColorProp.get();
    }

    public void setDifferenceBorderColor(Color value) {
        differenceBorderColorProp.set(value);
    }

    @SuppressWarnings("unused")
    public void bindDifferenceBorderColorTo(Prop<Color> prop) {
        differenceBorderColorProp.bindTo(prop);
    }

    //endregion
    //endregion
    //region Components
    private final JLabel[] labelsForImages =
            new JLabel[]{new JLabel(), new JLabel(), new JLabel()};
    private final JComponent content = new JPanel();

    //endregion
    //region Construction
    public static ExpectedActualDifferenceImageWidget expectedActualDifferenceImageView() {
        return new ExpectedActualDifferenceImageWidget();
    }

    private ExpectedActualDifferenceImageWidget() {
        styleComponents();
        layoutComponents();
        initBinding();
    }

    //endregion
    //region Widget related
    @Override
    public JComponent getContent() {
        return content;
    }

    private static final int MIN_IMAGE_SIZE = 16;

    private @Nullable SnapshotImages getSnapshotImages() {
        @Nullable SnapshotIssue issue = getSnapshotIssue();
        if (issue == null) {
            return null;
        }
        return snapshotImages(issue, getImagesArea());
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

    //endregion
    //region Style related
    private static final int BORDER_SIZE = 3;

    private void styleComponents() {
        content.setOpaque(true);
        content.setBackground(Color.white);
        content.setBorder(null);
    }

    private void setIconAndLinedBorder(JLabel label, ImageIcon icon, Color borderColor) {
        label.setIcon(icon);
        label.setBorder(SwingUtil.lineBorder(borderColor, BORDER_SIZE));
    }

    //endregion Style
    //region Layout related
    private void layoutComponents() {
        content.setLayout(new FlowLayout(FlowLayout.LEADING));
        addAll(content, labelsForImages);
    }

    //endregion
    //region Binding related
    private final AtomicBoolean mustUpdateLabelsForImages = new AtomicBoolean();

    private void initBinding() {
        onComponentResized(content, e -> onContentResized());

        shrinkToFitProp.runDependingCode(this::updateLabelsForImages);
        snapshotIssueProp.runDependingCode(this::updateLabelsForImages);
        expectedImageIndexProp.runDependingCode(this::updateLabelsForImages);
        expectedBorderColorProp.runDependingCode(this::updateLabelsForImages);
        actualBorderColorProp.runDependingCode(this::updateLabelsForImages);
        differenceBorderColorProp.runDependingCode(this::updateLabelsForImages);
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
    //endregion
}

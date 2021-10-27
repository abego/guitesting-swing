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
import org.abego.guitesting.swing.internal.util.Prop;
import org.abego.guitesting.swing.internal.util.PropBindable;
import org.abego.guitesting.swing.internal.util.SwingUtil;
import org.eclipse.jdt.annotation.Nullable;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;

import static java.lang.Boolean.FALSE;
import static java.lang.Math.max;
import static javax.swing.SwingUtilities.invokeLater;
import static org.abego.guitesting.swing.internal.snapshotreview.SnapshotImages.snapshotImages;
import static org.abego.guitesting.swing.internal.util.PropBindable.newPropBindable;
import static org.abego.guitesting.swing.internal.util.SwingUtil.flowLeft;
import static org.abego.guitesting.swing.internal.util.SwingUtil.onComponentResized;

class ExpectedActualDifferenceImageView implements Widget {
    private static final int BORDER_SIZE = 3;
    private static final int MIN_IMAGE_SIZE = 16;

    private Color expectedBorderColor = Color.green;
    private Color actualBorderColor = Color.red;
    private Color differenceBorderColor = Color.black;

    private PropBindable<Boolean> shrinkToFitProp =
            newPropBindable(FALSE, f -> updateLabelsForImages());

    private final JLabel[] labelsForImages =
            new JLabel[]{new JLabel(), new JLabel(), new JLabel()};
    //TODO: extra class? HList?
    private final JComponent content = flowLeft(c -> {
        c.setOpaque(true);
        c.setBackground(Color.white);
        c.setBorder(null);
    }, labelsForImages);

    private int expectedImageIndex = 0;
    private @Nullable SnapshotIssue snapshotIssue;

    public static ExpectedActualDifferenceImageView expectedActualDifferenceImageView() {
        return new ExpectedActualDifferenceImageView();
    }

    public @Nullable SnapshotIssue getSnapshotIssue() {
        return snapshotIssue;
    }

    public void setSnapshotIssue(@Nullable SnapshotIssue snapshotIssue) {
        this.snapshotIssue = snapshotIssue;
        updateLabelsForImages();
    }

    public boolean getShrinkToFit() {
        return shrinkToFitProp.get();
    }

    public void setShrinkToFit(boolean value) {
        this.shrinkToFitProp.set(value);
    }

    public void bindShrinkToFitTo(Prop<Boolean> prop) {
        shrinkToFitProp.bindTo(prop);
    }

    public int getExpectedImageIndex() {
        return expectedImageIndex;
    }

    public void setExpectedImageIndex(int expectedImageIndex) {
        this.expectedImageIndex = expectedImageIndex;
        updateLabelsForImages();
    }

    public Color getExpectedBorderColor() {return expectedBorderColor;}

    public void setExpectedBorderColor(Color expectedBorderColor) {
        this.expectedBorderColor = expectedBorderColor;
        updateLabelsForImages();
    }

    public Color getActualBorderColor() {return actualBorderColor;}

    public void setActualBorderColor(Color actualBorderColor) {
        this.actualBorderColor = actualBorderColor;
        updateLabelsForImages();
    }

    public Color getDifferenceBorderColor() {return differenceBorderColor;}

    public void setDifferenceBorderColor(Color differenceBorderColor) {
        this.differenceBorderColor = differenceBorderColor;
        updateLabelsForImages();
    }

    @Override
    public JComponent getComponent() {
        return content;
    }

    private ExpectedActualDifferenceImageView() {
        onComponentResized(content, e -> onContentResized());
    }

    private void onContentResized() {
        if (getShrinkToFit()) {
            onImagesAreaMayHaveChanged();
        }
    }

    private void onImagesAreaMayHaveChanged() {
        updateLabelsForImages();
    }

    // dependsOn (snapshotIssue != null ? (shrinkToFit ? content.visibleRect : null) : null)
    // ? expectedImageIndex, expectedBorderColor, actualBorderColor,  differenceBorderColor: null
    private void updateLabelsForImages() {
        invokeLater(() -> {
            @Nullable SnapshotImages images = getSnapshotImages();
            if (images != null) {
                setIconAndLinedBorder(
                        labelsForImages[(expectedImageIndex) % 3],
                        images.getExpectedImage(),
                        getExpectedBorderColor());
                setIconAndLinedBorder(
                        labelsForImages[(expectedImageIndex + 1) % 3],
                        images.getActualImage(),
                        getActualBorderColor());
                setIconAndLinedBorder(
                        labelsForImages[(expectedImageIndex + 2) % 3],
                        images.getDifferenceImage(),
                        getDifferenceBorderColor());
            }
            SwingUtil.setVisible(images != null, labelsForImages);
        });
    }

    // changes: label.icon, label.border
    private void setIconAndLinedBorder(JLabel label, ImageIcon icon, Color borderColor) {
        label.setIcon(icon);
        label.setBorder(SwingUtil.lineBorder(borderColor, BORDER_SIZE));
    }

    // dependsOn: shrinkToFit ? content.visibleRect : null
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

    // dependsOn: snapshotIssue != null ? (shrinkToFit ? content.visibleRect : null) : null
    private @Nullable SnapshotImages getSnapshotImages() {
        @Nullable SnapshotIssue issue = getSnapshotIssue();
        if (issue == null) {
            return null;
        }
        return snapshotImages(issue, getImagesArea());
    }

}

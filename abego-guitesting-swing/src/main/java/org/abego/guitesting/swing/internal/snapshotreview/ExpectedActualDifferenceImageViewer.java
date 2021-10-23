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

import org.abego.guitesting.swing.ScreenCaptureSupport;
import org.abego.guitesting.swing.internal.util.SwingUtil;
import org.eclipse.jdt.annotation.Nullable;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.Objects;

import static java.lang.Math.max;
import static javax.swing.SwingUtilities.invokeLater;
import static org.abego.guitesting.swing.internal.util.SwingUtil.flowLeft;
import static org.abego.guitesting.swing.internal.util.SwingUtil.onComponentResized;

class ExpectedActualDifferenceImageViewer implements Widget {
    private static final Color EXPECTED_BORDER_COLOR = new Color(0x59A869);
    private static final Color ACTUAL_BORDER_COLOR = new Color(0xC64D3F);
    private static final Color DIFFERENCE_BORDER_COLOR = new Color(0x6E6E6E);
    private static final int BORDER_SIZE = 3;

    private final JLabel[] labelsForImages =
            new JLabel[]{new JLabel(), new JLabel(), new JLabel()};
    private final JComponent imagesContainer = flowLeft(c -> {
        c.setOpaque(true);
        c.setBackground(Color.white);
        c.setBorder(null);
    }, labelsForImages);

    private @Nullable SnapshotImages snapshotImages;
    private boolean shrinkToFit = true;
    private int expectedImageIndex;
    private ScreenCaptureSupport.@Nullable SnapshotIssue snapshotIssue;

    public static ExpectedActualDifferenceImageViewer expectedActualDifferenceImageViewer() {
        return new ExpectedActualDifferenceImageViewer();
    }

    public ScreenCaptureSupport.@Nullable SnapshotIssue getSnapshotIssue() {
        return snapshotIssue;
    }

    public void setSnapshotIssue(ScreenCaptureSupport.@Nullable SnapshotIssue snapshotIssue) {
        this.snapshotIssue = snapshotIssue;
        updateLabelsForImages();
        updateImagesContainer();
    }

    public boolean getShrinkToFit() {
        return shrinkToFit;
    }

    public void setShrinkToFit(boolean shrinkToFit) {
        this.shrinkToFit = shrinkToFit;
        updateLabelsForImages();
    }

    public int getExpectedImageIndex() {
        return expectedImageIndex;
    }

    public void setExpectedImageIndex(int expectedImageIndex) {
        this.expectedImageIndex = expectedImageIndex;
        updateLabelsForImages();
    }

    public Color getExpectedBorderColor() {return EXPECTED_BORDER_COLOR;}

    public Color getActualBorderColor() {return ACTUAL_BORDER_COLOR;}

    public Color getDifferenceBorderColor() {return DIFFERENCE_BORDER_COLOR;}

    @Override
    public JComponent getComponent() {
        return imagesContainer;
    }

    private ExpectedActualDifferenceImageViewer() {
        onComponentResized(imagesContainer, e -> onImagesContainerVisibleRectChanged());
    }

    private void onImagesContainerVisibleRectChanged() {
        if (getShrinkToFit()) {
            invokeLater(this::updateLabelsForImages);
        }
    }

    private void updateLabelsForImages() {
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

            onLabelsForImagesChanged();
        }
    }

    private void updateImagesContainer() {
        SwingUtil.setVisible(getSnapshotIssue() != null, labelsForImages);
    }

    private void setIconAndLinedBorder(JLabel label, ImageIcon icon, Color borderColor) {
        label.setIcon(icon);
        label.setBorder(SwingUtil.lineBorder(borderColor, BORDER_SIZE));
    }

    private @Nullable Dimension getImagesArea() {
        if (getShrinkToFit()) {
            Rectangle visibleRect = imagesContainer.getVisibleRect();
            int w = visibleRect.width - 4 * SwingUtil.DEFAULT_FLOW_GAP - 6 * BORDER_SIZE;
            int h = visibleRect.height - 2 * SwingUtil.DEFAULT_FLOW_GAP - 2 * BORDER_SIZE;
            return new Dimension(max(0, w), max(0, h));
        } else {
            return null;
        }
    }

    private void onLabelsForImagesChanged() {
        invokeLater(this::updateImagesContainer);
    }

    private @Nullable SnapshotImages getSnapshotImages() {
        ScreenCaptureSupport.@Nullable SnapshotIssue issue = getSnapshotIssue();
        if (issue == null) {
            return null;
        }
        @Nullable SnapshotImages images = snapshotImages;
        if (images == null
                || !Objects.equals(images.getIssue(), issue)
                || !Objects.equals(getImagesArea(), images.getArea())) {
            images = new SnapshotImages(issue, getImagesArea());
            snapshotImages = images;
        }
        return images;
    }

}

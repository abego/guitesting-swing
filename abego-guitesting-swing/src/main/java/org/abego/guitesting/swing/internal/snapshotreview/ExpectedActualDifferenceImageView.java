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
import org.eclipse.jdt.annotation.Nullable;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;

import static java.lang.Math.max;
import static javax.swing.SwingUtilities.invokeLater;
import static org.abego.guitesting.swing.internal.snapshotreview.SnapshotImages.snapshotImages;
import static org.abego.guitesting.swing.internal.util.SwingUtil.flowLeft;
import static org.abego.guitesting.swing.internal.util.SwingUtil.onComponentResized;

//TODO: review the dependency management (...Changes, on..., update...)
class ExpectedActualDifferenceImageView implements Widget {
    private static final Color EXPECTED_BORDER_COLOR = new Color(0x59A869);
    private static final Color ACTUAL_BORDER_COLOR = new Color(0xC64D3F);
    private static final Color DIFFERENCE_BORDER_COLOR = new Color(0x6E6E6E);
    private static final int BORDER_SIZE = 3;
    private static final int MIN_IMAGE_SIZE = 16;

    private final JLabel[] labelsForImages =
            new JLabel[]{new JLabel(), new JLabel(), new JLabel()};
    //TODO: extra class? HList?
    private final JComponent content = flowLeft(c -> {
        c.setOpaque(true);
        c.setBackground(Color.white);
        c.setBorder(null);
    }, labelsForImages);

    private boolean shrinkToFit = true;
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
    // ? expectedImageIndex,final expectedBorderColor,final actualBorderColor, final differenceBorderColor: null
    private void updateLabelsForImages() {
        @Nullable SnapshotImages images = getSnapshotImages();
        invokeLater(() -> {
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

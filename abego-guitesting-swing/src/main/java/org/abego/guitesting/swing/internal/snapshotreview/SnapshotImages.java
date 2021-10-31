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

import org.eclipse.jdt.annotation.Nullable;

import javax.swing.ImageIcon;
import java.awt.Dimension;
import java.awt.Image;
import java.util.Objects;

import static java.lang.Math.max;
import static org.abego.commons.io.FileUtil.toFile;
import static org.abego.guitesting.swing.ScreenCaptureSupport.SnapshotIssue;
import static org.abego.guitesting.swing.internal.util.DimensionUtil.shrinkToFitFactor;
import static org.abego.guitesting.swing.internal.util.SwingUtil.icon;

/**
 * Provides the images of a {@link SnapshotIssue} ("expected", "actual",
 * "difference"), optionally scaled down to fit into a given area.
 */
final class SnapshotImages {
    private static @Nullable SnapshotImages cachedSnapshotImages;

    private final SnapshotIssue issue;
    private final @Nullable Dimension area;
    private final ImageIcon expectedImage;
    private final ImageIcon actualImage;
    private final ImageIcon differenceImage;

    private SnapshotImages(
            SnapshotIssue issue,
            @Nullable Dimension area) {
        this.issue = issue;
        this.area = area;

        ImageIcon rawExpectedImage = icon(toFile(issue.getExpectedImage()));
        ImageIcon rawActualImage = icon(toFile(issue.getActualImage()));
        ImageIcon rawDifferenceImage = icon(toFile(issue.getDifferenceImage()));

        double scaleFactor = 1.0;
        if (area != null) {
            Dimension imagesTotalSize = totalSize(
                    rawExpectedImage, rawActualImage, rawDifferenceImage);
            scaleFactor = shrinkToFitFactor(imagesTotalSize, area);
        }
        if (scaleFactor != 1.0) {
            expectedImage = scaledImageIcon(rawExpectedImage, scaleFactor);
            actualImage = scaledImageIcon(rawActualImage, scaleFactor);
            differenceImage = scaledImageIcon(rawDifferenceImage, scaleFactor);
        } else {
            expectedImage = rawExpectedImage;
            actualImage = rawActualImage;
            differenceImage = rawDifferenceImage;
        }
    }

    public static SnapshotImages snapshotImages(
            SnapshotIssue issue, @Nullable Dimension area) {
        @Nullable SnapshotImages images = cachedSnapshotImages;
        if (images != null
                && Objects.equals(images.getIssue(), issue)
                && Objects.equals(images.getArea(), area)) {
            return images;
        }
        cachedSnapshotImages = new SnapshotImages(issue, area);
        return cachedSnapshotImages;
    }

    @Nullable
    public Dimension getArea() {
        return area;
    }

    public ImageIcon getExpectedImage() {
        return expectedImage;
    }

    public ImageIcon getActualImage() {
        return actualImage;
    }

    public ImageIcon getDifferenceImage() {
        return differenceImage;
    }

    public SnapshotIssue getIssue() {
        return issue;
    }

    private static ImageIcon scaledImageIcon(ImageIcon icon, double scaleFactor) {
        Image image = icon.getImage();
        int w = icon.getIconWidth();
        return new ImageIcon(
                image.getScaledInstance((int) (w * scaleFactor), -1, Image.SCALE_SMOOTH));
    }

    /**
     * Returns the size the imageIcons occupy when placed from left to right and
     * top aligned.
     */
    private static Dimension totalSize(ImageIcon... imageIcons) {
        int totalWidth = 0;
        int maxHeight = 0;
        for (ImageIcon image : imageIcons) {
            totalWidth += image.getIconWidth();
            maxHeight = max(maxHeight, image.getIconHeight());
        }
        return new Dimension(totalWidth, maxHeight);
    }
}

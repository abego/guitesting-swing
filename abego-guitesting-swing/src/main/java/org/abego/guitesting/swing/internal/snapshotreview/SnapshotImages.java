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
import java.util.function.Consumer;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static org.abego.commons.io.FileUtil.toFile;
import static org.abego.guitesting.swing.ScreenCaptureSupport.SnapshotIssue;
import static org.abego.guitesting.swing.internal.util.Util.icon;

final class SnapshotImages {
    private final SnapshotIssue issue;
    private final @Nullable Dimension area;
    //TODO: do we need this?
    private final Consumer<SnapshotImages> onImagesLoadedCallback;
    private final ImageIcon rawExpectedImage;
    private final ImageIcon rawActualImage;
    private final ImageIcon rawDifferenceImage;
    private final ImageIcon expectedImage;
    private final ImageIcon actualImage;
    private final ImageIcon differenceImage;
    SnapshotImages(
            SnapshotIssue issue,
            @Nullable Dimension area,
            Consumer<SnapshotImages> onImagesLoadedCallback) {
        this.issue = issue;
        this.area = area;
        this.onImagesLoadedCallback = onImagesLoadedCallback;

        rawExpectedImage = icon(toFile(issue.getExpectedImage()));
        rawActualImage = icon(toFile(issue.getActualImage()));
        rawDifferenceImage = icon(toFile(issue.getDifferenceImage()));

        double scaleFactor = 1.0;
        if (area != null) {
            scaleFactor = getShrinkToFitScaleFactor(area);
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

    private static ImageIcon scaledImageIcon(ImageIcon icon, double scaleFactor) {
        Image image = icon.getImage();
        int w = icon.getIconWidth();
        return new ImageIcon(
                image.getScaledInstance((int) (w * scaleFactor), -1, Image.SCALE_SMOOTH));
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

    /**
     * Returns the factor the images must be scaled down to fit in the given
     * rectangle, or {@code 1.0} when the images fit into the rectangle
     * without scaling.
     *
     * <p>The images are assumed to be place left to right and top aligned.</p>
     */
    private double getShrinkToFitScaleFactor(Dimension area) {
        int totalWidth = 0;
        int maxHeight = 0;
        for (ImageIcon image : new ImageIcon[]{rawExpectedImage, rawActualImage, rawDifferenceImage}) {
            totalWidth += image.getIconWidth();
            maxHeight = max(maxHeight, image.getIconHeight());
        }
        double scaleFactor = 1.0;
        if (area.getHeight() > 0 && maxHeight > area.getHeight()) {
            scaleFactor = area.getHeight() / maxHeight;
        }
        if (area.getWidth() > 0 && totalWidth > area.getWidth()) {
            scaleFactor = min(scaleFactor,  area.getWidth() / totalWidth);
        }
        return scaleFactor;
    }

    public SnapshotIssue getIssue() {
        return issue;
    }
}

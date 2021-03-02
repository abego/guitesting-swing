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

package org.abego.guitesting.swing.internal;

import org.abego.commons.timeout.TimeoutSupplier;
import org.abego.guitesting.swing.GuiTestingException;
import org.abego.guitesting.swing.ScreenCaptureSupport;
import org.eclipse.jdt.annotation.Nullable;

import javax.imageio.ImageIO;
import java.awt.Component;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.time.Duration;

import static org.abego.guitesting.swing.internal.SwingUtil.toScreenCoordinates;

class ScreenCaptureSupportImpl implements ScreenCaptureSupport {
    private final Robot robot;
    private final TimeoutSupplier timeoutProvider;
    private boolean generateSnapshotIfMissing = true;
    private Duration delayBeforeNewSnapshot = Duration.ofMillis(200);

    private ScreenCaptureSupportImpl(Robot robot, TimeoutSupplier timeoutProvider) {
        this.robot = robot;
        this.timeoutProvider = timeoutProvider;
    }

    public static ScreenCaptureSupport newScreenCaptureSupport(
            Robot robot, TimeoutSupplier timeoutProvider) {
        return new ScreenCaptureSupportImpl(robot, timeoutProvider);
    }

    @Override
    public BufferedImage captureScreen(Rectangle screenRect) {
        return robot.createScreenCapture(screenRect);
    }

    @Override
    public BufferedImage captureScreen(Component component, @Nullable Rectangle rectangle) {
        Rectangle componentRect = new Rectangle(component.getSize());
        if (rectangle == null) {
            rectangle = componentRect;
        }
        Rectangle rect = rectangle.intersection(componentRect);
        return captureScreen(toScreenCoordinates(component, rect));
    }

    @Override
    public BufferedImage captureScreen(Component component) {
        return captureScreen(component,null);
    }

    @Override
    public ImageDifference difference(Image imageA, Image imageB) {
        throw new GuiTestingException("Not yet implemented"); //TODO: implement
    }

    @Override
    public void waitUntilScreenshotMatchesImage(Component component, @Nullable Rectangle rectangle, Image... expectedImages) {
        throw new GuiTestingException("Not yet implemented"); //TODO: implement
    }

    @Override
    public void waitUntilScreenshotMatchesImage(Component component, Image... expectedImages) {
        throw new GuiTestingException("Not yet implemented"); //TODO: implement
    }

    @Override
    public boolean getGenerateSnapshotIfMissing() {
        return generateSnapshotIfMissing;
    }

    @Override
    public void setGenerateSnapshotIfMissing(boolean value) {
        generateSnapshotIfMissing = value;
    }

    @Override
    public Duration getDelayBeforeNewSnapshot() {
        return delayBeforeNewSnapshot;
    }

    @Override
    public void setDelayBeforeNewSnapshot(Duration duration) {
        delayBeforeNewSnapshot = duration;
    }

    @Override
    public void waitUntilScreenshotMatchesSnapshot(Component component, @Nullable Rectangle rectangle, String snapshotName) throws UndefinedSnapshotException, ImageNotMatchingSnapshotException {
        throw new GuiTestingException("Not yet implemented"); //TODO: implement
    }

    @Override
    public void waitUntilScreenshotMatchesSnapshot(Component component, String snapshotName) throws UndefinedSnapshotException, ImageNotMatchingSnapshotException {
        throw new GuiTestingException("Not yet implemented"); //TODO: implement
    }

    @Override
    public void writeImage(RenderedImage image, File file) {
        checkIsPngFilename(file);
        try {
            ImageIO.write(image, "png", file);
        } catch (IOException e) {
            throw new GuiTestingException(
                    "Error when writing image file "+file.getAbsolutePath(),e);
        }
    }

    private static void checkIsPngFilename(File file) {
        if (!file.getName().toLowerCase().endsWith(".png")) {
            throw new GuiTestingException("Only PNG files supported");
        }
    }

    @Override
    public Image readImage(File file) {
        checkIsPngFilename(file);
        try {
            return ImageIO.read(file);
        } catch (IOException e) {
            throw new GuiTestingException(
                    "Error when reading image file "+file.getAbsolutePath(),e);
        }
    }

    @Override
    public Duration timeout() {
        return timeoutProvider.timeout();
    }
}

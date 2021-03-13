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

import org.abego.commons.timeout.TimeoutUncheckedException;
import org.abego.guitesting.swing.GuiTestingException;
import org.abego.guitesting.swing.PollingSupport;
import org.abego.guitesting.swing.ScreenCaptureSupport;
import org.eclipse.jdt.annotation.Nullable;
import org.opentest4j.AssertionFailedError;

import javax.imageio.ImageIO;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.time.Duration;

import static org.abego.guitesting.swing.internal.SwingUtil.toScreenCoordinates;

class ScreenCaptureSupportImpl implements ScreenCaptureSupport {
    private static final Duration DELAY_BEFORE_NEW_SNAPSHOT_DEFAULT = Duration.ofMillis(200);

    private final Robot robot;
    private final PollingSupport pollingSupport;
    private boolean generateSnapshotIfMissing = true;
    private Duration delayBeforeNewSnapshot = DELAY_BEFORE_NEW_SNAPSHOT_DEFAULT;

    private ScreenCaptureSupportImpl(Robot robot, PollingSupport pollingSupport) {
        this.robot = robot;
        this.pollingSupport = pollingSupport;
    }

    public static ScreenCaptureSupport newScreenCaptureSupport(
            Robot robot, PollingSupport pollingSupport) {
        return new ScreenCaptureSupportImpl(robot, pollingSupport);
    }

    private static void checkIsPngFilename(File file) {
        if (!file.getName().toLowerCase().endsWith(".png")) {
            throw new GuiTestingException("Only PNG files supported");
        }
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
        return captureScreen(component, null);
    }


    @Override
    public ImageDifference imageDifference(BufferedImage imageA, BufferedImage imageB) {
        ImageCompare compare = ImageCompare.newImageCompare();
        @Nullable BufferedImage diff = compare.differenceMask(imageA, imageB);
        if (diff != null) {
            return new ImageDifferenceImpl(true, imageA, imageB, diff);
        } else {
            return new ImageDifferenceImpl(false, imageA, imageB, compare.transparentImage(imageA));
        }
    }


    private File getOutputDirectory() {
        return new File("target/guitesting-reports");
    }

    @Override
    public BufferedImage waitUntilScreenshotMatchesImage(
            Component component, @Nullable Rectangle rectangle, BufferedImage... expectedImages) {
        if (expectedImages.length == 0) {
            throw new IllegalArgumentException("No expectedImages specified");
        }
        CaptureScreenAndCompare csc = new CaptureScreenAndCompare(
                component, rectangle, expectedImages);

        try {
            return pollingSupport.poll(
                    () -> csc.capture(),
                    image -> csc.imageMatchesAnyExpectedImage(image));
        } catch (TimeoutUncheckedException e) {
            BufferedImage actualImage = csc.getLastScreenshot();
            if (actualImage == null) {
                throw new AssertionFailedError("Timeout before first screenshot", e);
            }

            String name = getTestMethodName(e.getStackTrace());
            File actualImageFile = new File(name + "-actualImage.png");
            writeImage(actualImage, actualImageFile);

            int i = 1;
            for (BufferedImage expectedImage : expectedImages) {
                File expectedImageFile = new File(name + "-expectedImage" + i + ".png");
                writeImage(expectedImage, expectedImageFile);
                File differenceImageFile = new File(name + "-differenceImage" + i + ".png");
                ImageDifference diff = imageDifference(expectedImage, actualImage);
                writeImage(diff.getDifferenceMask(), differenceImageFile);

                i++;
                //TODO: generate report with images
            }
            throw new AssertionFailedError("Screenshot does not match expected image (Timeout)", e);
        }
    }

    private String getTestMethodName(StackTraceElement[] stackTrace) {
        return "org.example.SomeTestClass.mytestmethod";
    }

    @Override
    public BufferedImage waitUntilScreenshotMatchesImage(Component component, BufferedImage... expectedImages) {
        return waitUntilScreenshotMatchesImage(component, null, expectedImages);
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
                    "Error when writing image file " + file.getAbsolutePath(), e);
        }
    }

    @Override
    public BufferedImage readImage(File file) {
        checkIsPngFilename(file);
        try {
            return ImageIO.read(file);
        } catch (IOException e) {
            throw new GuiTestingException(
                    "Error when reading image file " + file.getAbsolutePath(), e);
        }
    }

    @Override
    public Duration timeout() {
        return pollingSupport.timeout();
    }

    private static class ImageDifferenceImpl implements ImageDifference {
        private final boolean imagesAreDifferent;
        private final BufferedImage imageA;
        private final BufferedImage imageB;
        private final BufferedImage differenceMask;

        private ImageDifferenceImpl(
                boolean imagesAreDifferent,
                BufferedImage imageA,
                BufferedImage imageB,
                BufferedImage differenceMask) {

            this.imagesAreDifferent = imagesAreDifferent;
            this.imageA = imageA;
            this.imageB = imageB;
            this.differenceMask = differenceMask;
        }

        @Override
        public boolean imagesAreDifferent() {
            return imagesAreDifferent;
        }

        @Override
        public BufferedImage getImageA() {
            return imageA;
        }

        @Override
        public BufferedImage getImageB() {
            return imageB;
        }

        @Override
        public BufferedImage getDifferenceMask() {
            return differenceMask;
        }
    }

    private class CaptureScreenAndCompare {
        private final Component component;
        private final @Nullable Rectangle rectangle;
        private final BufferedImage[] expectedImages;

        private @Nullable BufferedImage lastScreenshot;

        private CaptureScreenAndCompare(
                Component component,
                @Nullable Rectangle rectangle,
                BufferedImage[] expectedImages) {
            this.component = component;
            this.rectangle = rectangle;
            this.expectedImages = expectedImages;
        }

        private boolean imageMatchesAnyExpectedImage(BufferedImage image) {
            for (BufferedImage expectedImage : expectedImages) {
                ImageDifference diff = imageDifference(image, expectedImage);
                if (!diff.imagesAreDifferent()) {
                    return true;
                }
            }
            return false;
        }

        private @Nullable BufferedImage capture() {
            BufferedImage result = captureScreen(component, rectangle);
            this.lastScreenshot = result;
            return result;
        }

        @Nullable
        public BufferedImage getLastScreenshot() {
            return lastScreenshot;
        }
    }
}

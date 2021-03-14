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

package org.abego.guitesting.swing.internal.screencapture;

import org.abego.commons.io.FileUtil;
import org.abego.commons.timeout.TimeoutUncheckedException;
import org.abego.guitesting.swing.GuiTestingException;
import org.abego.guitesting.swing.PollingSupport;
import org.abego.guitesting.swing.ScreenCaptureSupport;
import org.abego.guitesting.swing.WaitSupport;
import org.abego.guitesting.swing.internal.ImageCompare;
import org.abego.guitesting.swing.internal.SwingUtil;
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
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import static java.util.logging.Logger.getLogger;
import static org.abego.guitesting.swing.internal.SwingUtil.getCaller;
import static org.abego.guitesting.swing.internal.SwingUtil.getFullMethodName;
import static org.abego.guitesting.swing.internal.SwingUtil.urlToFile;

public class ScreenCaptureSupportImpl implements ScreenCaptureSupport {
    private static final Logger LOGGER = getLogger(ScreenCaptureSupportImpl.class.getName());
    private static final Duration DELAY_BEFORE_NEW_SNAPSHOT_DEFAULT = Duration.ofMillis(200);
    private static final String SNAP_SHOTS_DIRECTORY_NAME = "snap-shots";

    private final Robot robot;
    private final PollingSupport pollingSupport;
    private final WaitSupport waitSupport;
    private boolean generateSnapshotIfMissing = true;
    private Duration delayBeforeNewSnapshot = DELAY_BEFORE_NEW_SNAPSHOT_DEFAULT;

    private ScreenCaptureSupportImpl(
            Robot robot, PollingSupport pollingSupport, WaitSupport waitSupport) {
        this.robot = robot;
        this.pollingSupport = pollingSupport;
        this.waitSupport = waitSupport;
    }

    public static ScreenCaptureSupport newScreenCaptureSupport(
            Robot robot, PollingSupport pollingSupport, WaitSupport waitSupport) {
        return new ScreenCaptureSupportImpl(robot, pollingSupport, waitSupport);
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
        return captureScreen(SwingUtil.toScreenCoordinates(component, rect));
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
            return ImageDifferenceImpl.of(true, imageA, imageB, diff);
        } else {
            return ImageDifferenceImpl.of(false, imageA, imageB, compare.transparentImage(imageA));
        }
    }

    @Override
    public BufferedImage imageDifferenceMask(BufferedImage imageA, BufferedImage imageB) {
        return imageDifference(imageA, imageB).getDifferenceMask();
    }

    @Override
    public BufferedImage waitUntilScreenshotMatchesImage(
            Component component, @Nullable Rectangle rectangle, BufferedImage... expectedImages) {
        return waitUntilScreenshotMatchesImageHelper(
                component, rectangle, expectedImages, null, getCaller("waitUntilScreenshotMatchesImage")
        );
    }

    private File getOutputDirectory() {
        return new File("target/guitesting-reports");
    }

    private BufferedImage waitUntilScreenshotMatchesImageHelper(
            Component component, @Nullable Rectangle rectangle,
            BufferedImage[] expectedImages,
            @Nullable File newImageFile, StackTraceElement caller) {

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

            File report = writeUnmatchedScreenshotReport(
                    actualImage, expectedImages, e, caller, newImageFile);
            throw new AssertionFailedError(
                    "Screenshot does not match expected image (Timeout).\n" +
                            "For details see:\n- " + report.getAbsolutePath(), e);
        }
    }

    private File writeUnmatchedScreenshotReport(
            BufferedImage actualImage,
            BufferedImage[] expectedImages,
            Exception exception,
            StackTraceElement caller,
            @Nullable File newImageFileForResources) {

        ScreenshotCompareReportData reportData = generateReportData(
                actualImage, expectedImages, exception, caller, newImageFileForResources);
        return ScreenshotCompareHtmlReport.of(reportData).writeReportFile();
    }

    private ScreenshotCompareReportData generateReportData(
            BufferedImage actualImage,
            BufferedImage[] expectedImages,
            Exception exception,
            StackTraceElement caller,
            @Nullable File newImageFileForResources) {

        File outputDir = getOutputDirectory();
        String methodName = getFullMethodName(caller);
        Date timestamp = new Date();
        String actualImageFileName = methodName + "-actualImage.png";

        File imagesDir = new File(outputDir, "images");
        FileUtil.ensureDirectoryExists(imagesDir);
        File actualImageFile = new File(imagesDir, actualImageFileName);
        writeImage(actualImage, actualImageFile);

        List<ExpectedAndDifferenceFile> expectedAndDifferenceFiles = new ArrayList<>();
        for (BufferedImage expectedImage : expectedImages) {
            int i = expectedAndDifferenceFiles.size() + 1;
            String expectedImageFileName = methodName + "-expectedImage" + i + ".png";
            File expectedImageFile = new File(imagesDir, expectedImageFileName);
            writeImage(expectedImage, expectedImageFile);

            String differenceImageFileName = methodName + "-differenceImage" + i + ".png";
            File differenceImageFile = new File(imagesDir, differenceImageFileName);
            writeImage(
                    imageDifferenceMask(expectedImage, actualImage),
                    differenceImageFile);

            expectedAndDifferenceFiles.add(
                    new ExpectedAndDifferenceFile(
                            expectedImageFileName, differenceImageFileName));
        }

        return ScreenshotCompareReportData.of(outputDir, methodName, timestamp, actualImageFileName, expectedAndDifferenceFiles, exception, newImageFileForResources);
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
    public BufferedImage waitUntilScreenshotMatchesSnapshot(
            Component component,
            @Nullable Rectangle rectangle,
            String snapshotName)
            throws GuiTestingException {
        StackTraceElement testMethod = getCaller("waitUntilScreenshotMatchesSnapshot");
        BufferedImage[] snapshotImages = getImagesOfSnapshot(testMethod, snapshotName);
        // Calculate the file we would use to store a new screenshot image,
        // e.g. if no existing snapshot image matches the current screenshot.
        File newImageFile = getSnapshotImageFile(
                testMethod, snapshotName, snapshotImages.length);
        if (snapshotImages.length == 0) {
            // No snapshot image exists

            if (getGenerateSnapshotIfMissing()) {
                return captureAndWriteInitialSnapshotImage(
                        component, rectangle, snapshotName, newImageFile);
            } else {
                throw new GuiTestingException(String.format(
                        "No images defined for snapshot '%s' of %s",
                        snapshotName, getFullMethodName(testMethod)));
            }

        } else {
            // Snapshot images already exist.
            return waitUntilScreenshotMatchesImageHelper(
                    component, rectangle, snapshotImages, newImageFile, testMethod);
        }
    }

    @Override
    public BufferedImage[] getImagesOfSnapshot(String name) {
        StackTraceElement testMethod = getCaller("getImagesOfSnapshot");
        return getImagesOfSnapshot(testMethod, name);
    }

    @Override
    public void writeImage(RenderedImage image, File file) {
        SwingUtil.checkIsPngFilename(file);
        try {
            ImageIO.write(image, "png", file);
        } catch (IOException e) {
            throw new GuiTestingException(
                    "Error when writing image to " + file.getAbsolutePath(), e);
        }
    }

    @Override
    public BufferedImage readImage(File file) {
        SwingUtil.checkIsPngFilename(file);
        try {
            return ImageIO.read(file);
        } catch (Exception e) {
            throw new GuiTestingException(
                    "Error when reading image from " + file.getAbsolutePath(), e);
        }
    }

    @Override
    public BufferedImage readImage(URL url) {
        try {
            return ImageIO.read(url);
        } catch (Exception e) {
            throw new GuiTestingException(
                    "Error when reading image from " + url, e);
        }
    }

    @Override
    public Duration timeout() {
        return pollingSupport.timeout();
    }

    private BufferedImage captureAndWriteInitialSnapshotImage(
            Component component, @Nullable Rectangle rectangle,
            String snapshotName, File imageFile) {

        waitSupport.waitFor(getDelayBeforeNewSnapshot());
        BufferedImage image = captureScreen(component, rectangle);
        writeImage(image, imageFile);
        LOGGER.info(String.format("Initial snapshot image written: '%s'", imageFile.getAbsolutePath()));
        return image;
    }

    private File getSnapshotImageFile(StackTraceElement caller, String snapshotName, int index) {
        File dir = getDirectoryForSnapshotImageResources(SwingUtil.getClass(caller));
        String imageName = getSnapshotImageName(caller, snapshotName, index);
        return new File(dir, imageName);
    }

    private File getDirectoryForSnapshotImageResources(Class<?> testClass) {
        URL url = testClass.getResource("");
        if (!url.getProtocol().equals("file")) {
            throw new GuiTestingException("Can write snapshot image only to file system ('file:/...'). Got " + url);
        }
        String urlText = url.toString();
        String testClassesPattern = "/target/test-classes/";
        if (!urlText.contains(testClassesPattern)) {
            throw new GuiTestingException(
                    String.format("Standard Maven directory structure required (%s not found in %s)", testClassesPattern, urlText));
        }
        String inTestResourcesDirURL = urlText.replace(
                testClassesPattern, "/src/test/resources/");
        File snapshotsResourcesDir = urlToFile(inTestResourcesDirURL + "/" + SNAP_SHOTS_DIRECTORY_NAME);
        FileUtil.ensureDirectoryExists(snapshotsResourcesDir);
        return snapshotsResourcesDir;
    }

    private BufferedImage[] getImagesOfSnapshot(
            StackTraceElement testMethod, String snapshotName) {

        List<BufferedImage> result = new ArrayList<>();
        int i = 0;
        @Nullable URL imageURL;
        do {
            imageURL = getSnapshotImageURL(testMethod, snapshotName, i);
            if (imageURL != null) {
                result.add(readImage(imageURL));
            }
            i++;
        } while (imageURL != null);

        return result.toArray(new BufferedImage[0]);
    }

    @Nullable
    private URL getSnapshotImageURL(StackTraceElement caller, String snapshotName, int i) {
        String imageName = getSnapshotImageName(caller, snapshotName, i);
        return SwingUtil.getClass(caller).getResource("snap-shots" + "/" + imageName);
    }

    private String getSnapshotImageName(StackTraceElement caller, String snapshotName, int i) {
        return getSimpleClassAndMethodName(caller) + "-" + snapshotName + "@" + i + ".png";
    }

    private String getSimpleClassAndMethodName(StackTraceElement caller) {
        return SwingUtil.getClass(caller).getSimpleName() + "." + caller.getMethodName();
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

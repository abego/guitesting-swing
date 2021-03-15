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
import org.abego.guitesting.swing.internal.GuiTestingUtil;
import org.eclipse.jdt.annotation.Nullable;
import org.opentest4j.AssertionFailedError;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static java.util.logging.Logger.getLogger;
import static org.abego.guitesting.swing.internal.GuiTestingUtil.checkIsPngFilename;
import static org.abego.guitesting.swing.internal.GuiTestingUtil.getCaller;
import static org.abego.guitesting.swing.internal.GuiTestingUtil.getFullMethodName;
import static org.abego.guitesting.swing.internal.GuiTestingUtil.toScreenCoordinates;
import static org.abego.guitesting.swing.internal.GuiTestingUtil.urlToFile;

public class ScreenCaptureSupportImpl implements ScreenCaptureSupport {
    private static final Logger LOGGER = getLogger(ScreenCaptureSupportImpl.class.getName());
    private static final Duration DELAY_BEFORE_NEW_SNAPSHOT_DEFAULT = Duration.ofSeconds(1);
    private static final String SNAP_SHOTS_DIRECTORY_NAME = "snap-shots"; //NON-NLS
    private static final String TEST_RESOURCES_DIRECTORY_PATH_DEFAULT = "src/test/resources"; //NON-NLS
    private static final boolean AUTO_ADJUST_JFRAME_RECTANGLE = false;

    private final Robot robot;
    private final PollingSupport pollingSupport;
    private final WaitSupport waitSupport;
    private boolean generateSnapshotIfMissing = true;
    private Duration delayBeforeNewSnapshot = DELAY_BEFORE_NEW_SNAPSHOT_DEFAULT;
    private String testResourcesDirectoryPath = TEST_RESOURCES_DIRECTORY_PATH_DEFAULT;
    private int imageCompareTolerancePercentage = ImageCompare.TOLERANCE_PERCENTAGE_DEFAULT;

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
    public int getImageCompareTolerancePercentage() {
        return imageCompareTolerancePercentage;
    }

    @Override
    public void setImageCompareTolerancePercentage(int value) {
        imageCompareTolerancePercentage = value;
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
        ImageCompare compare = newImageCompare();
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
    public void writeImage(RenderedImage image, File file) {
        checkIsPngFilename(file);
        try {
            ImageIO.write(image, "png", file); //NON-NLS
        } catch (IOException e) {
            throw new GuiTestingException(
                    String.format("Error when writing image to %s", file.getAbsolutePath()), e); //NON-NLS
        }
    }

    @Override
    public BufferedImage readImage(File file) {
        checkIsPngFilename(file);
        try {
            return ImageIO.read(file);
        } catch (Exception e) {
            throw new GuiTestingException(
                    getReadImageErrorMessage(file.getAbsolutePath()), e);
        }
    }

    @Override
    public BufferedImage readImage(URL url) {
        try {
            return ImageIO.read(url);
        } catch (Exception e) {
            throw new GuiTestingException(getReadImageErrorMessage(url.toString()), e);
        }
    }

    @Override
    public Duration timeout() {
        return pollingSupport.timeout();
    }

    @Override
    public BufferedImage waitUntilScreenshotMatchesImage(
            Component component, @Nullable Rectangle rectangle, BufferedImage... expectedImages) {
        rectangle = adjustRectangleForScreenCapture(component, rectangle);
        return waitUntilScreenshotMatchesImageHelper(
                component, rectangle, expectedImages, null, getCaller("waitUntilScreenshotMatchesImage")
        );
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
    public String getTestResourcesDirectoryPath() {
        return testResourcesDirectoryPath;
    }

    @Override
    public void setTestResourcesDirectoryPath(String path) {
        testResourcesDirectoryPath = path;
    }

    @Override
    public BufferedImage[] getImagesOfSnapshot(String name) {
        StackTraceElement testMethod = getCaller("getImagesOfSnapshot");
        return getImagesOfSnapshot(testMethod, name);
    }

    @Override
    public BufferedImage waitUntilScreenshotMatchesSnapshot(
            Component component,
            @Nullable Rectangle rectangle,
            String snapshotName)
            throws GuiTestingException {
        StackTraceElement testMethod = getCaller("waitUntilScreenshotMatchesSnapshot");
        rectangle = adjustRectangleForScreenCapture(component, rectangle);

        BufferedImage[] snapshotImages = getImagesOfSnapshot(testMethod, snapshotName);
        // Calculate the file we would use to store a new screenshot image,
        // e.g. if no existing snapshot image matches the current screenshot.
        File newImageFile = getSnapshotImageFile(
                testMethod, snapshotName, snapshotImages.length);
        if (snapshotImages.length == 0) {
            // No snapshot image exists

            if (getGenerateSnapshotIfMissing()) {
                return captureAndWriteInitialSnapshotImage(
                        component, rectangle, newImageFile);
            } else {
                throw new GuiTestingException(String.format(
                        "No images defined for snapshot '%s' of %s", //NON-NLS
                        snapshotName, getFullMethodName(testMethod)));
            }

        } else {
            // Snapshot images already exist.
            return waitUntilScreenshotMatchesImageHelper(
                    component, rectangle, snapshotImages, newImageFile, testMethod);
        }
    }

    private ImageCompare newImageCompare() {
        return ImageCompare.newImageCompare(imageCompareTolerancePercentage);
    }

    private static String getReadImageErrorMessage(String source) {
        return String.format("Error when reading image from %s", source); //NON-NLS
    }

    /**
     * Returns a rectangle suited for a screen capture image of the
     * component.
     *
     * <p>When AUTO_ADJUST_JFRAME_RECTANGLE is true:
     * The bounds of a {@link JFrame} are slightly larger than the
     * "obvious" area covered on the screen as the bounds also includes the
     * area occupied by the "drop shadow" painted around the frame. As the drop
     * shadow is semi-transparent pixels from the desktop "shine through".
     * Because of this a screenshot of the frame with its bound would also
     * include pixels not in the control of the frame and may change randomly.
     * To get more reproducible results we use smaller rectangle for JFrame
     * screenshot (when no rectangle is explicitly specified). This includes the
     * root pane and the menu bar.</p>
     */
    @Nullable
    private Rectangle adjustRectangleForScreenCapture(
            Component component, @Nullable Rectangle rectangle) {
        if (component instanceof JFrame && rectangle == null && AUTO_ADJUST_JFRAME_RECTANGLE) {
            rectangle = ((JFrame) component).getRootPane().getBounds();
            rectangle.x = (rectangle.x+1)/2;
            rectangle.height += rectangle.y;
            rectangle.y = 0;
        }
        return rectangle;
    }

    private BufferedImage captureAndWriteInitialSnapshotImage(
            Component component, @Nullable Rectangle rectangle,
            File imageFile) {

        waitSupport.waitFor(getDelayBeforeNewSnapshot());
        BufferedImage image = captureScreen(component, rectangle);
        writeImage(image, imageFile);
        LOGGER.info(String.format("Initial snapshot image written: '%s'", imageFile.getAbsolutePath())); //NON-NLS
        return image;
    }

    private File getSnapshotImageFile(StackTraceElement caller, String snapshotName, int index) {
        File dir = getDirectoryForSnapshotImageResources(GuiTestingUtil.getClass(caller));
        String imageName = getSnapshotImageName(caller, snapshotName, index);
        return new File(dir, imageName);
    }

    private File getDirectoryForSnapshotImageResources(Class<?> testClass) {
        URL url = testClass.getResource("");
        //noinspection CallToSuspiciousStringMethod
        if (!url.getProtocol().equals("file")) { //NON-NLS
            throw new GuiTestingException(String.format("Can write snapshot image only to file system ('file:/...'). Got %s", url)); //NON-NLS
        }
        String urlText = url.toString();
        String testClassesPattern = "/target/test-classes/"; //NON-NLS
        if (!urlText.contains(testClassesPattern)) {
            throw new GuiTestingException(
                    String.format("Standard Maven directory structure required (%s not found in %s)", testClassesPattern, urlText)); //NON-NLS
        }
        String inTestResourcesDirURL = urlText.replace(
                testClassesPattern, String.format("/%s/", getTestResourcesDirectoryPath())); //NON-NLS
        File snapshotsResourcesDir = urlToFile(String.format("%s/%s", inTestResourcesDirURL, SNAP_SHOTS_DIRECTORY_NAME)); //NON-NLS
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
        return GuiTestingUtil.getClass(caller).getResource(SNAP_SHOTS_DIRECTORY_NAME + "/" + imageName);
    }

    private String getSnapshotImageName(StackTraceElement caller, String snapshotName, int i) {
        return String.format("%s-%s@%d.png", getSimpleClassAndMethodName(caller), snapshotName, i); //NON-NLS
    }

    private String getSimpleClassAndMethodName(StackTraceElement caller) {
        return GuiTestingUtil.getClass(caller).getSimpleName() + "." + caller.getMethodName();
    }

    private File getOutputDirectory() {
        return new File("target/guitesting-reports");
    }

    private BufferedImage waitUntilScreenshotMatchesImageHelper(
            Component component, @Nullable Rectangle rectangle,
            BufferedImage[] expectedImages,
            @Nullable File newImageFile, StackTraceElement caller) {

        if (expectedImages.length == 0) {
            throw new IllegalArgumentException("No expectedImages specified"); //NON-NLS
        }

        CaptureScreenAndCompare csc = new CaptureScreenAndCompare(
                component, rectangle, expectedImages);
        try {
            return pollingSupport.poll(
                    csc::capture,
                    csc::imageMatchesAnyExpectedImage);
        } catch (TimeoutUncheckedException e) {

            @Nullable BufferedImage actualImage = csc.getLastScreenshot();
            if (actualImage == null) {
                throw new AssertionFailedError("Timeout before first screenshot", e); //NON-NLS
            }

            File report = writeUnmatchedScreenshotReport(
                    actualImage, expectedImages, e, caller, newImageFile);
            throw new AssertionFailedError(
                    String.format("Screenshot does not match expected image (Timeout).\nFor details see:\n- %s", report.getAbsolutePath()), e); //NON-NLS
        }
    }

    private File writeUnmatchedScreenshotReport(
            BufferedImage actualImage,
            BufferedImage[] expectedImages,
            Exception exception,
            StackTraceElement caller,
            @Nullable File newImageFileForResources) {

        ScreenshotCompareReportData reportData = generateScreenshotCompareReportData(
                actualImage, expectedImages, exception, caller, newImageFileForResources);
        return ScreenshotCompareHtmlReport.of(reportData).writeReportFile();
    }

    private ScreenshotCompareReportData generateScreenshotCompareReportData(
            BufferedImage actualImage,
            BufferedImage[] expectedImages,
            Exception exception,
            StackTraceElement caller,
            @Nullable File newImageFileForResources) {

        File outputDir = getOutputDirectory();
        String methodName = getFullMethodName(caller);
        String timestamp = Instant.now().toString();
        String actualImageFileName = String.format("%s-actualImage.png", methodName); //NON-NLS

        String imagesDirName = "images"; //NON-NLS
        File imagesDir = new File(outputDir, imagesDirName);
        FileUtil.ensureDirectoryExists(imagesDir);
        File actualImageFile = new File(imagesDir, actualImageFileName);
        writeImage(actualImage, actualImageFile);

        List<ExpectedAndDifferenceFile> expectedAndDifferenceFiles = new ArrayList<>();
        for (BufferedImage expectedImage : expectedImages) {
            int i = expectedAndDifferenceFiles.size() + 1;
            String expectedImageFileName = String.format("%s-expectedImage%d.png", methodName, i); //NON-NLS
            File expectedImageFile = new File(imagesDir, expectedImageFileName);
            writeImage(expectedImage, expectedImageFile);

            String differenceImageFileName = String.format("%s-differenceImage%d.png", methodName, i); //NON-NLS
            File differenceImageFile = new File(imagesDir, differenceImageFileName);
            writeImage(
                    imageDifferenceMask(expectedImage, actualImage),
                    differenceImageFile);

            expectedAndDifferenceFiles.add(
                    new ExpectedAndDifferenceFile(
                            String.format("%s/%s", imagesDirName, expectedImageFileName), //NON-NLS
                            String.format("%s/%s", imagesDirName, differenceImageFileName))); //NON-NLS
        }

        return ScreenshotCompareReportData.of(outputDir, methodName,
                String.format("%s/%s", imagesDirName, actualImageFileName), expectedAndDifferenceFiles, exception, newImageFileForResources, timestamp); //NON-NLS
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

        private BufferedImage capture() {
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

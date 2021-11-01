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
import org.abego.commons.seq.Seq;
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
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

import static java.util.logging.Logger.getLogger;
import static org.abego.commons.lang.StringUtil.replaceRange;
import static org.abego.guitesting.swing.internal.GuiTestingUtil.checkIsPngFilename;
import static org.abego.guitesting.swing.internal.GuiTestingUtil.getNameDefiningCall;
import static org.abego.guitesting.swing.internal.GuiTestingUtil.getReadImageErrorMessage;
import static org.abego.guitesting.swing.internal.GuiTestingUtil.toScreenCoordinates;
import static org.abego.guitesting.swing.internal.GuiTestingUtil.urlToFile;
import static org.abego.guitesting.swing.internal.screencapture.SnapshotIssueSupport.newSnapshotIssueSupport;

public class ScreenCaptureSupportImpl implements ScreenCaptureSupport {
    static final String SCREENSHOT_IMAGES_DIRECTORY_NAME_DEFAULT = "images"; //NON-NLS
    static final String SNAP_SHOTS_DIRECTORY_NAME = "/snap-shots"; //NON-NLS
    private static final Logger LOGGER = getLogger(ScreenCaptureSupportImpl.class.getName());
    private static final Duration DELAY_BEFORE_NEW_SNAPSHOT_DEFAULT = Duration.ofSeconds(1);
    @SuppressWarnings("DuplicateStringLiteralInspection")
    private static final String TEST_RESOURCES_DIRECTORY_PATH_DEFAULT = "src/test/resources"; //NON-NLS
    private final Robot robot;
    private final PollingSupport pollingSupport;
    private final WaitSupport waitSupport;
    private File snapshotReportDirectory = new File("target/guitesting-reports");
    private boolean generateSnapshotIfMissing = true;
    private boolean useInnerJFrameBounds = false;
    private Duration delayBeforeNewSnapshot = DELAY_BEFORE_NEW_SNAPSHOT_DEFAULT;
    private File testResourcesDirectory = new File(TEST_RESOURCES_DIRECTORY_PATH_DEFAULT);
    private int imageDifferenceTolerancePercentage = ImageCompare.TOLERANCE_PERCENTAGE_DEFAULT;

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
    public boolean getUseInnerJFrameBounds() {
        return useInnerJFrameBounds;
    }

    @Override
    public void setUseInnerJFrameBounds(boolean value) {
        this.useInnerJFrameBounds = value;
    }

    @Override
    public BufferedImage captureScreen(@Nullable Rectangle screenRect) {
        return robot.createScreenCapture(screenRect != null
                ? screenRect
                : new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
    }

    @Override
    public BufferedImage captureScreen(
            @Nullable Component component, @Nullable Rectangle rectangle) {
        if (component == null) {
            return captureScreen(rectangle);
        }

        Rectangle componentRect = new Rectangle(component.getSize());
        if (rectangle == null) {
            rectangle = adjustRectangleForScreenCapture(component, null);
        }
        if (rectangle == null) {
            rectangle = componentRect;
        }
        Rectangle rect = rectangle.intersection(componentRect);
        return captureScreen(toScreenCoordinates(component, rect));
    }

    @Override
    public BufferedImage captureScreen(@Nullable Component component) {
        return captureScreen(component, null);
    }

    @Override
    public int getImageDifferenceTolerancePercentage() {
        return imageDifferenceTolerancePercentage;
    }

    @Override
    public void setImageDifferenceTolerancePercentage(int value) {
        imageDifferenceTolerancePercentage = value;
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
            FileUtil.ensureDirectoryExists(file.getParentFile());

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
        return GuiTestingUtil.readImage(url);
    }

    @Override
    public Duration timeout() {
        return pollingSupport.timeout();
    }

    @Override
    public BufferedImage waitUntilScreenshotMatchesImage(
            Component component, @Nullable Rectangle rectangle, BufferedImage... expectedImages) {
        rectangle = adjustRectangleForScreenCapture(component, rectangle);
        SnapshotInfo snapshotInfo = new SnapshotInfo(
                null, "waitUntilScreenshotMatchesImage", getTestResourcesDirectory());
        return waitUntilScreenshotMatchesImageHelper(
                component, rectangle, expectedImages, null, snapshotInfo);
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
    public File getTestResourcesDirectory() {
        return testResourcesDirectory;
    }

    @Override
    public void setTestResourcesDirectory(File directory) {
        testResourcesDirectory = directory;
    }

    private static String resolveSnapshotName(
            @Nullable String snapshotName, String calleeName) {
        // absolute snapshot names are used "as is"
        if (snapshotName != null && snapshotName.startsWith("/")) {
            return snapshotName;
        }

        // when no "absolute" snapshot name is given derive the name from
        // the method (and class) calling the snapshotting method
        if (snapshotName == null) {
            snapshotName = SNAPSHOT_NAME_DEFAULT;
        }

        // When no absolute snapshot name is given use the test class and
        // test method name as prefix to the (relative) snapshot name.
        StackTraceElement method = getNameDefiningCall(calleeName);
        return String.format("/%s.%s-%s",  //NON-NLS
                method.getClassName().replaceAll("\\.", "/"),
                method.getMethodName(),
                snapshotName);
    }

    @Override
    public BufferedImage[] getImagesOfSnapshot(String name) {
        SnapshotInfo info = new SnapshotInfo(name, "getImagesOfSnapshot", getTestResourcesDirectory());
        return info.getImagesOfSnapshot();
    }

    @Override
    public BufferedImage waitUntilScreenshotMatchesSnapshot(
            Component component,
            @Nullable Rectangle rectangle,
            String snapshotName)
            throws GuiTestingException {
        SnapshotInfo info = new SnapshotInfo(
                snapshotName, "waitUntilScreenshotMatchesSnapshot", getTestResourcesDirectory());

        rectangle = adjustRectangleForScreenCapture(component, rectangle);

        BufferedImage[] snapshotImages = info.getImagesOfSnapshot();
        // Calculate the file we would use to store a new screenshot image,
        // e.g. if no existing snapshot image matches the current screenshot.
        File newImageFile = info.getSnapshotImageFile(snapshotImages.length);
        if (snapshotImages.length == 0) {
            // No snapshot image exists

            if (getGenerateSnapshotIfMissing()) {
                return captureAndWriteInitialSnapshotImage(
                        component, rectangle, newImageFile);
            } else {
                throw new GuiTestingException(
                        info.getNoSnapshotImagesFoundMessage());
            }

        } else {
            // Snapshot images already exist.
            return waitUntilScreenshotMatchesImageHelper(
                    component, rectangle, snapshotImages, newImageFile, info);
        }
    }

    private BufferedImage waitUntilScreenshotMatchesImageHelper(
            Component component, @Nullable Rectangle rectangle,
            BufferedImage[] expectedImages,
            @Nullable File newImageFile, SnapshotInfo snapshotInfo) {

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
                    actualImage, expectedImages, e, snapshotInfo, newImageFile);
            throw new AssertionFailedError(
                    String.format("Screenshot does not match expected image (Timeout).\nFor details see:\n- %s", report.getAbsolutePath()), e); //NON-NLS
        }
    }

    private ImageCompare newImageCompare() {
        return ImageCompare.newImageCompare(imageDifferenceTolerancePercentage);
    }

    @Override
    public String getSnapshotName(@Nullable String name) {
        return resolveSnapshotName(name, "getSnapshotName");
    }

    @Override
    public File getSnapshotReportDirectory() {
        return snapshotReportDirectory;
    }

    @Override
    public void setSnapshotReportDirectory(File directory) {
        snapshotReportDirectory = directory;
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

    private File writeUnmatchedScreenshotReport(
            BufferedImage actualImage,
            BufferedImage[] expectedImages,
            Exception exception,
            SnapshotInfo snapshotInfo,
            @Nullable File newImageFileForResources) {

        ScreenshotCompareReportData reportData = generateScreenshotCompareReportData(
                actualImage, expectedImages, exception, snapshotInfo, newImageFileForResources);
        return ScreenshotCompareHtmlReport.of(reportData).writeReportFile();
    }

    /**
     * Returns a rectangle suited for a screen capture image of the
     * component, especially taking care of the {@code useInnerJFrameBounds} property.
     */
    @Nullable
    private Rectangle adjustRectangleForScreenCapture(
            Component component, @Nullable Rectangle rectangle) {
        if (component instanceof JFrame && rectangle == null && getUseInnerJFrameBounds()) {
            rectangle = ((JFrame) component).getRootPane().getBounds();
            rectangle.x = (rectangle.x + 1) / 2;
            rectangle.height += rectangle.y - 1;
            rectangle.y = 1;
            if (GuiTestingUtil.isMacOS()) {
                // some more adjustments for Macs, to avoid capturing the
                // "round corners" of the Mac windows (would capture stuff
                // from behind the window).
                // (values found experimentally).
                rectangle.height -= 2;
            }
        }
        return rectangle;
    }

    private ScreenshotCompareReportData generateScreenshotCompareReportData(
            BufferedImage actualImage,
            BufferedImage[] expectedImages,
            Exception exception,
            SnapshotInfo snapshotInfo,
            @Nullable File newImageFileForResources) {

        File outputDir = getSnapshotReportDirectory();
        String timestamp = Instant.now().toString();

        String imagesDirName = SCREENSHOT_IMAGES_DIRECTORY_NAME_DEFAULT; //NON-NLS
        File imagesDir = new File(outputDir, imagesDirName);
        FileUtil.ensureDirectoryExists(imagesDir);
        File actualImageFile = new File(imagesDir, snapshotInfo.getActualImageFileName());
        writeImage(actualImage, actualImageFile);

        List<ExpectedAndDifferenceFile> expectedAndDifferenceFiles = new ArrayList<>();
        for (BufferedImage expectedImage : expectedImages) {
            int i = expectedAndDifferenceFiles.size();
            String expectedImageFileName = snapshotInfo.getExpectedImageFileName(i); //NON-NLS
            File expectedImageFile = new File(imagesDir, expectedImageFileName);
            writeImage(expectedImage, expectedImageFile);

            String differenceImageFileName = snapshotInfo.getDifferenceImageFileName(i); //NON-NLS
            File differenceImageFile = new File(imagesDir, differenceImageFileName);
            writeImage(
                    imageDifferenceMask(expectedImage, actualImage),
                    differenceImageFile);

            expectedAndDifferenceFiles.add(
                    new ExpectedAndDifferenceFile(
                            String.format("%s/%s", imagesDirName, expectedImageFileName), //NON-NLS
                            String.format("%s/%s", imagesDirName, differenceImageFileName))); //NON-NLS
        }

        return ScreenshotCompareReportData.of(
                outputDir,
                snapshotInfo,
                String.format("%s/%s", imagesDirName, snapshotInfo.getActualImageFileName()), //NON-NLS
                expectedAndDifferenceFiles,
                exception,
                newImageFileForResources,
                timestamp); //NON-NLS
    }

    @Override
    public Seq<SnapshotIssue> getSnapshotIssues() {
        return newSnapshotIssueSupport(getSnapshotReportDirectory(),
                getTestResourcesDirectory()).findSnapshotIssues();
    }

    static class SnapshotInfo {

        /**
         * The (absolute) snapshot name, with "/" as delimiters
         */
        private final String absoluteSnapshotName;
        private final File testResourcesDirectory;
        private final URL urlToTestClass;

        SnapshotInfo(
                @Nullable String snapshotName,
                String calleeName,
                File testResourcesDirectory) {
            this.absoluteSnapshotName =
                    resolveSnapshotName(snapshotName, calleeName);
            this.testResourcesDirectory = testResourcesDirectory;
            this.urlToTestClass = urlToTestClass(calleeName);
        }

        private static URL urlToTestClass(String calleeName) {
            StackTraceElement method = getNameDefiningCall(calleeName);
            Class<?> type = GuiTestingUtil.getClass(method);
            return Objects.requireNonNull(type.getResource(""));
        }


        String getNoSnapshotImagesFoundMessage() {
            return String.format(
                    "No images defined for %s", //NON-NLS
                    absoluteSnapshotName);
        }

        BufferedImage[] getImagesOfSnapshot() {
            List<BufferedImage> result = new ArrayList<>();
            int i = 0;
            @Nullable URL imageURL;
            do {
                imageURL = getSnapshotImageURL(i);
                if (imageURL != null) {
                    result.add(GuiTestingUtil.readImage(imageURL));
                }
                i++;
            } while (imageURL != null);

            return result.toArray(new BufferedImage[0]);
        }

        File getSnapshotImageFile(int index) {
            File dir = getTestResourcesDirectory();
            String imageName = getAbsoluteSnapshotImageResourceName(index)
                    .substring(1); // remove the initial "/"
            return new File(dir, imageName);
        }

        private String getDifferenceImageFileName(int i) {
            return String.format("%s-differenceImage@%d.png", getSnapshotSimpleName(), i); //NON-NLS
        }

        private String getExpectedImageFileName(int i) {
            return String.format("%s-expectedImage@%d.png", getSnapshotSimpleName(), i); //NON-NLS
        }

        private String getActualImageFileName() {
            return String.format("%s-actualImage.png", getSnapshotSimpleName()); //NON-NLS
        }

        @Nullable
        private URL getSnapshotImageURL(int i) {
            return ScreenCaptureSupportImpl.class.getResource(
                    getAbsoluteSnapshotImageResourceName(i));
        }

        private String getAbsoluteSnapshotImageResourceName(int index) {
            //noinspection MagicCharacter
            int i = absoluteSnapshotName.lastIndexOf('/');

            // Insert the "snap-shots" directory before the absoluteSnapshotName
            //noinspection StringConcatenation
            String rawSnapshotName = i < 0
                    ? SNAP_SHOTS_DIRECTORY_NAME + absoluteSnapshotName
                    : replaceRange(absoluteSnapshotName, i, i, SNAP_SHOTS_DIRECTORY_NAME);
            return String.format("%s@%d.png", rawSnapshotName, index); //NON-NLS
        }

        private File getTestResourcesDirectory() {
            //noinspection StringConcatenation
            String testResourcesDirectoryURL =
                    getMavenProjectDirectoryURL() + "/" + testResourcesDirectory.getPath(); //NON-NLS

            File testResourcesDir = urlToFile(testResourcesDirectoryURL); //NON-NLS
            FileUtil.ensureDirectoryExists(testResourcesDir);
            return testResourcesDir;
        }

        private String getMavenProjectDirectoryURL() {
            //noinspection CallToSuspiciousStringMethod
            if (!urlToTestClass.getProtocol().equals("file")) { //NON-NLS
                throw new GuiTestingException(
                        String.format("Can write snapshot image only to file system ('file:/...'). Got %s", //NON-NLS
                                urlToTestClass)); //NON-NLS
            }

            String urlText = urlToTestClass.toString();
            String testClassesPattern = "/target/test-classes/"; //NON-NLS
            int i = urlText.indexOf(testClassesPattern);
            if (i < 0) {
                throw new GuiTestingException(
                        String.format("Standard Maven directory structure required (%s not found in %s)", //NON-NLS
                                testClassesPattern, urlText));
            }
            return urlText.substring(0, i);
        }


        /**
         * Returns the snapshot name as a "simple" name, i.e. the leading "/"
         * removed and all remaining "/" replaced by ".".
         *
         * @return the simple name of the snapshot
         */
        public String getSnapshotSimpleName() {
            return absoluteSnapshotName.substring(1).replaceAll("/", ".");
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

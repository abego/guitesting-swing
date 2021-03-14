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

import org.abego.commons.io.FileUtil;
import org.abego.commons.timeout.TimeoutUncheckedException;
import org.abego.guitesting.swing.GuiTestingException;
import org.abego.guitesting.swing.PollingSupport;
import org.abego.guitesting.swing.ScreenCaptureSupport;
import org.abego.guitesting.swing.WaitSupport;
import org.eclipse.jdt.annotation.NonNull;
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
import java.io.PrintStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;
import java.util.logging.Logger;

import static java.util.logging.Logger.getLogger;
import static org.abego.guitesting.swing.internal.SwingUtil.toScreenCoordinates;

class ScreenCaptureSupportImpl implements ScreenCaptureSupport {
    private final static Logger LOGGER = getLogger(ScreenCaptureSupportImpl.class.getName());
    private final static Duration DELAY_BEFORE_NEW_SNAPSHOT_DEFAULT = Duration.ofMillis(200);

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

    private static void checkIsPngFilename(File file) {
        if (!file.getName().toLowerCase(Locale.ENGLISH).endsWith(".png")) {
            throw new GuiTestingException("Only 'png' files supported. Got " + file);
        }
    }

    @Nullable
    private static StackTraceElement findCaller(
            StackTraceElement[] stackTrace,
            Predicate<StackTraceElement> isCallee) {
        boolean foundCaller = false;
        for (StackTraceElement element : stackTrace) {
            if (isCallee.test(element)) {
                foundCaller = true;
            } else {
                // The first method after a callee method that is not itself
                // a callee method is the caller we are looking for
                if (foundCaller) {
                    return element;
                }
            }
        }
        return null;
    }

    @Nullable
    private static StackTraceElement findCaller(
            Predicate<StackTraceElement> isCallee) {
        return findCaller(Thread.currentThread().getStackTrace(), isCallee);
    }

    @NonNull
    private static Class getClass(@NonNull StackTraceElement stackTraceElement) {
        try {
            return Class.forName(stackTraceElement.getClassName());
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid class in stackTraceElement", e);
        }
    }

    @NonNull
    private static File urlToFile(String snapshotsDirURL) {
        try {
            return new File(new URL(snapshotsDirURL).toURI());
        } catch (Exception e) {
            throw new RuntimeException(e);
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
    public BufferedImage imageDifferenceMask(BufferedImage imageA, BufferedImage imageB) {
        ImageCompare compare = ImageCompare.newImageCompare();
        @Nullable BufferedImage diff = compare.differenceMask(imageA, imageB);
        return diff != null ? diff : compare.transparentImage(imageA);
    }

    @Override
    public BufferedImage waitUntilScreenshotMatchesImage(
            Component component, @Nullable Rectangle rectangle, BufferedImage... expectedImages) {
        return waitUntilScreenshotMatchesImageHelper(
                getCaller("waitUntilScreenshotMatchesImage"), null,
                component, rectangle, expectedImages);
    }

    private BufferedImage waitUntilScreenshotMatchesImageHelper(
            StackTraceElement caller, @Nullable File newImageFileForResources,
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

            File report = writeUnmatchedScreenshotReport(actualImage, expectedImages, e, caller, newImageFileForResources);

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

        UnmatchedScreenshotReportData reportData = generateReportData(actualImage, expectedImages, exception, caller, newImageFileForResources);
        return writeReportFile(reportData);
    }

    private UnmatchedScreenshotReportData generateReportData(
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

        return UnmatchedScreenshotReportData.of(outputDir, methodName, timestamp, actualImageFileName, expectedAndDifferenceFiles, exception, newImageFileForResources);
    }

    @NonNull
    private String getFullMethodName(StackTraceElement caller) {
        return getClass(caller).getName() + "." + caller.getMethodName();
    }

    private File writeReportFile(UnmatchedScreenshotReportData reportData) {

        File reportFile = new File(reportData.getOutputDirectory(), reportData.getMethodName() + "-failed.html");
        String actualImagePath = "images/" + reportData.getActualImageFileName();
        try (PrintStream report = new PrintStream(reportFile, StandardCharsets.UTF_8.name())) {
            report.println("" +
                    "<!DOCTYPE html>\n" +
                    "<html lang=\"en\">\n" +
                    "<head>\n" +
                    "    <meta charset=\"UTF-8\">\n" +
                    "    <title>" + reportData.getMethodName() + " failed</title>\n" +
                    "</head>\n" +
                    "<body>\n" +
                    "<h1>" + reportData.getMethodName() + " failed</h1>\n" +
                    reportData.getTimestamp() + "\n" +
                    "<h2>Actual</h2>\n" +
                    "<img src=\"" + actualImagePath + "\" alt=\"actual image\">\n");

            if (reportData.getNewImageFileForResources() != null) {
                File actualImageFile = new File(reportData.getOutputDirectory(), actualImagePath);
                report.println("" +
                        "<h3>Choices</h3>\n" +
                        "<h4>To make the image an additional option of the snapshot run the following in a bash terminal:</h4>\n" +
                        "<pre>\n" +
                        "cp " + actualImageFile.getAbsolutePath() + " " + reportData.getNewImageFileForResources().getAbsolutePath() + "\n" +
                        "</pre>\n");
            }

            int n = reportData.getExpectedAndDifferenceFiles().size();
            for (int i = 1; i <= n; i++) {
                ExpectedAndDifferenceFile item = reportData.getExpectedAndDifferenceFiles().get(i - 1);
                report.println("" +
                        "<h2>Expected (Option " + i + " of " + n + ")</h2>\n" +
                        "<img src=\"images/" + item.expectedImageFileName + "\" alt=\"expected image " + i + "\">\n" +
                        "<h3>Difference</h3>\n" +
                        "<img src=\"images/" + item.differenceImageFileName + "\" alt=\"difference image " + i + "\">\n");
            }

            report.println("<h2>Stack</h2>\n<pre>");
            reportData.getException().printStackTrace(report);
            report.println("" +
                    "</pre>\n" +
                    "</body>\n" +
                    "</html>");
            return reportFile;
        } catch (Exception e) {
            throw new GuiTestingException(
                    "Error when writing report file " + reportFile.getAbsolutePath(), e);
        }
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
        StackTraceElement caller = getCaller("waitUntilScreenshotMatchesSnapshot");
        BufferedImage[] snapshotImages = findSnapshotImages(caller, snapshotName);
        if (snapshotImages.length == 0) {
            // This is the first snapshot

            if (getGenerateSnapshotIfMissing()) {
                return writeInitialSnapshotImage(component, rectangle, snapshotName, caller);
            } else {
                throw new GuiTestingException(
                        String.format(
                                "No images defined for snapshot '%s' of %s",
                                snapshotName, getFullMethodName(caller)));
            }

        } else {
            File newImageFileForResources = getSnapshotImageFile(
                    caller, snapshotName, snapshotImages.length);

            return waitUntilScreenshotMatchesImageHelper(
                    caller, newImageFileForResources,
                    component, rectangle, snapshotImages);
        }
    }

    @NonNull
    private BufferedImage writeInitialSnapshotImage(Component component, @Nullable Rectangle rectangle, String snapshotName, StackTraceElement caller) {
        waitSupport.waitFor(getDelayBeforeNewSnapshot());
        BufferedImage image = captureScreen(component, rectangle);
        File imageFile = writeSnapshotImage(image, caller, snapshotName, 0);
        LOGGER.info(String.format("Initial snapshot image written: '%s'", imageFile.getAbsolutePath()));
        return image;
    }

    @NonNull
    private StackTraceElement getCaller(String calleeName) {
        @Nullable StackTraceElement caller =
                findCaller(e -> e.getMethodName().equals(calleeName));
        if (caller == null) {
            throw new IllegalStateException("Cannot identify calling test method");
        }
        return caller;
    }

    @Override
    public BufferedImage[] getImagesOfSnapshot(String name) {
        StackTraceElement caller = getCaller("getImagesOfSnapshot");
        return findSnapshotImages(caller, name);
    }

    private File writeSnapshotImage(BufferedImage image, StackTraceElement caller, String snapshotName, int index) {
        File imageFile = getSnapshotImageFile(caller, snapshotName, index);
        writeImage(image, imageFile);
        return imageFile;
    }

    @NonNull
    private File getSnapshotImageFile(StackTraceElement caller, String snapshotName, int index) {
        File dir = getDirectoryForSnapshotImagesInResources(getClass(caller));
        String imageName = getSnapshotImageName(caller, snapshotName, index);
        return new File(dir, imageName);
    }

    private File getDirectoryForSnapshotImagesInResources(Class testClass) {
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
        File snapshotsDir = urlToFile(inTestResourcesDirURL + "/snap-shots");
        FileUtil.ensureDirectoryExists(snapshotsDir);
        return snapshotsDir;
    }

    private BufferedImage[] findSnapshotImages(StackTraceElement caller, String snapshotName) {
        List<BufferedImage> result = new ArrayList<>();
        int i = 0;
        URL imageURL = null;
        do {
            imageURL = getSnapshotImageURL(caller, snapshotName, i);
            if (imageURL != null) {
                result.add(readImage(imageURL));
            }
            i++;
        } while (imageURL != null);
        return result.toArray(new BufferedImage[0]);
    }

    private URL getSnapshotImageURL(StackTraceElement caller, String snapshotName, int i) {
        String imageName = getSnapshotImageName(caller, snapshotName, i);
        return getClass(caller).getResource("snap-shots/" + imageName);
    }

    @NonNull
    private String getSnapshotImageName(StackTraceElement caller, String snapshotName, int i) {
        return getSimpleClassAndMethodName(caller) + "-" + snapshotName + "@" + i + ".png";
    }

    @NonNull
    private String getSimpleClassAndMethodName(StackTraceElement caller) {
        return getClass(caller).getSimpleName() + "." + caller.getMethodName();
    }

    @Override
    public void writeImage(RenderedImage image, File file) {
        checkIsPngFilename(file);
        try {
            ImageIO.write(image, "png", file);
        } catch (IOException e) {
            throw new GuiTestingException(
                    "Error when writing image to " + file.getAbsolutePath(), e);
        }
    }

    @Override
    public BufferedImage readImage(File file) {
        checkIsPngFilename(file);
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

    private static class ExpectedAndDifferenceFile {
        final String expectedImageFileName;
        final String differenceImageFileName;

        private ExpectedAndDifferenceFile(String expectedImageFileName, String differenceImageFileName) {
            this.expectedImageFileName = expectedImageFileName;
            this.differenceImageFileName = differenceImageFileName;
        }
    }

    private static class UnmatchedScreenshotReportData {
        private final File outputDirectory;
        private final String methodName;
        private final Date timestamp;
        private final String actualImageFileName;
        private final List<ExpectedAndDifferenceFile> expectedAndDifferenceFiles;
        private final Exception exception;

        private final @Nullable File newImageFileForResources;

        private UnmatchedScreenshotReportData(File outputDirectory, String methodName, Date timestamp, String actualImageFileName, List<ExpectedAndDifferenceFile> expectedAndDifferenceFiles, Exception exception, @Nullable File newImageFileForResources) {
            this.outputDirectory = outputDirectory;
            this.methodName = methodName;
            this.timestamp = timestamp;
            this.actualImageFileName = actualImageFileName;
            this.expectedAndDifferenceFiles = expectedAndDifferenceFiles;
            this.exception = exception;
            this.newImageFileForResources = newImageFileForResources;
        }

        private static UnmatchedScreenshotReportData of(
                File outputDirectory,
                String methodName,
                Date timestamp,
                String actualImageFileName,
                List<ExpectedAndDifferenceFile> expectedAndDifferenceFiles,
                Exception exception,
                @Nullable File newImageFileForResources) {
            return new UnmatchedScreenshotReportData(outputDirectory, methodName, timestamp, actualImageFileName, expectedAndDifferenceFiles, exception, newImageFileForResources);
        }

        public File getOutputDirectory() {
            return outputDirectory;
        }

        public String getMethodName() {
            return methodName;
        }

        public Date getTimestamp() {
            return timestamp;
        }

        public String getActualImageFileName() {
            return actualImageFileName;
        }

        public List<ExpectedAndDifferenceFile> getExpectedAndDifferenceFiles() {
            return expectedAndDifferenceFiles;
        }

        public Exception getException() {
            return exception;
        }

        public File getNewImageFileForResources() {
            return newImageFileForResources;
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

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
import org.abego.guitesting.swing.GT;
import org.abego.guitesting.swing.GuiTesting;
import org.abego.guitesting.swing.SnapshotReviewService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.swing.JCheckBox;
import javax.swing.JFrame;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.io.File;

import static org.abego.commons.io.FileUtil.emptyFile;
import static org.abego.commons.io.FileUtil.mkdirs;
import static org.abego.guitesting.swing.SnapshotReviewService.SNAPSHOT_REVIEW_FRAME_NAME;
import static org.abego.guitesting.swing.internal.screencapture.ScreenCaptureSupportImpl.SCREENSHOT_IMAGES_DIRECTORY_NAME_DEFAULT;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

final class SnapshotReviewServiceTest {
    @Test
    void showIssuesSnapshotReview(@TempDir File tempDir) {
        File reportsDir = mkdirs(tempDir, "reports");
        File testResourcesDir = mkdirs(tempDir, "test-resources");
        File myResourcesDir = new File(testResourcesDir.getAbsolutePath() + "/org/abego/guitesting/swing/snap-shots");

        // fill the "reports" directory.
        FileUtil.copyResourcesToDirectoryFlat(new File(reportsDir, SCREENSHOT_IMAGES_DIRECTORY_NAME_DEFAULT),
                "/org/abego/guitesting/swing/internal/review-sample/images/",
                "org.abego.guitesting.swing.GTTest.waitUntilScreenshotMatchesImage_timeout-snapshot-actualImage.png",
                "org.abego.guitesting.swing.GTTest.waitUntilScreenshotMatchesImage_timeout-snapshot-differenceImage@0.png",
                "org.abego.guitesting.swing.GTTest.waitUntilScreenshotMatchesImage_timeout-snapshot-differenceImage@1.png",
                "org.abego.guitesting.swing.GTTest.waitUntilScreenshotMatchesImage_timeout-snapshot-expectedImage@0.png",
                "org.abego.guitesting.swing.GTTest.waitUntilScreenshotMatchesImage_timeout-snapshot-expectedImage@1.png",
                "org.abego.guitesting.swing.GTTest.waitUntilScreenshotMatchesSnapshot_unmatchedScreenshot-snapshot-actualImage.png",
                "org.abego.guitesting.swing.GTTest.waitUntilScreenshotMatchesSnapshot_unmatchedScreenshot-snapshot-differenceImage@0.png",
                "org.abego.guitesting.swing.GTTest.waitUntilScreenshotMatchesSnapshot_unmatchedScreenshot-snapshot-expectedImage@0.png");

        // fill the testResources directory (with dummy file)
        emptyFile(new File(myResourcesDir,
                "GTTest.waitUntilScreenshotMatchesImage_timeout-snapshot@0.png"));
        emptyFile(new File(myResourcesDir,
                "GTTest.waitUntilScreenshotMatchesSnapshot_unmatchedScreenshot-snapshot@0.png"));

        GT gtForReview = GuiTesting.newGT();
        gtForReview.setSnapshotReportDirectory(reportsDir);
        gtForReview.setTestResourcesDirectory(testResourcesDir);

        SnapshotReviewService review = gtForReview.newSnapshotReviewService();
        review.showSnapshotReviewFrame(frame -> frame.setSize(800, 360));

        GT gt = GuiTesting.newGT();
        // more configuration for "modern" windows with round corners and
        // translucent border
        //TODO: extract as helper code?
        gt.setImageDifferenceIgnoredBorderSize(1);
        gt.setImageDifferenceIgnoredCornerSize(10);

        JFrame frame = gt.waitForWindowNamed(
                JFrame.class, SNAPSHOT_REVIEW_FRAME_NAME);

        assertAll(
                () -> gt.waitUntilScreenshotMatchesSnapshot(frame.getContentPane(), "start"),

                // rotate
                () -> {
                    gt.typeKeycode(KeyEvent.VK_RIGHT);
                    gt.waitUntilScreenshotMatchesSnapshot(frame.getContentPane(), "rotate1");
                },
                () -> {
                    gt.typeKeycode(KeyEvent.VK_RIGHT);
                    gt.waitUntilScreenshotMatchesSnapshot(frame.getContentPane(), "rotate2");
                },
                () -> {
                    gt.typeKeycode(KeyEvent.VK_RIGHT);
                    gt.waitUntilScreenshotMatchesSnapshot(frame.getContentPane(), "start");
                },
                () -> {
                    // select next issue
                    gt.typeKeycode(KeyEvent.VK_DOWN);
                    gt.waitUntilScreenshotMatchesSnapshot(frame.getContentPane(), "line2");
                },
                () -> {
                    // Un-"Shrink to fit"
                    gt.componentWith(JCheckBox.class, c -> true).doClick();
                    gt.waitUntilScreenshotMatchesSnapshot(frame.getContentPane(), "noShrink");
                },
                () -> {
                    // "Shrink to fit"
                    gt.componentWith(JCheckBox.class, c -> true).doClick();
                    gt.waitUntilScreenshotMatchesSnapshot(frame.getContentPane(), "line2");
                },
                () -> {
                    // ignore item (ESC)
                    gt.typeKeycode(KeyEvent.VK_ESCAPE);
                    gt.waitUntilScreenshotMatchesSnapshot(frame.getContentPane(), "deleted");
                },
                () -> {
                    // Alternative
                    gt.typeKeycode(KeyEvent.VK_A);
                    gt.waitUntilScreenshotMatchesSnapshot(frame.getContentPane(), "alternative");
                },
                () -> {
                    // Overwrite
                    gt.typeKeycode(KeyEvent.VK_O);
                    gt.waitUntilScreenshotMatchesSnapshot(frame.getContentPane(), "overwrite");
                },

                // check for the overwritten file
                () -> assertEquals(239, new File(myResourcesDir,
                        "GTTest.waitUntilScreenshotMatchesImage_timeout-snapshot@0.png").length()),

                // check for the alternative file
                () -> assertEquals(0, new File(myResourcesDir,
                        "GTTest.waitUntilScreenshotMatchesSnapshot_unmatchedScreenshot-snapshot@0.png").length()),
                () -> assertEquals(239, new File(myResourcesDir,
                        "GTTest.waitUntilScreenshotMatchesSnapshot_unmatchedScreenshot-snapshot@1.png").length())
        );
    }


}

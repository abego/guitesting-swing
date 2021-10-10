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

import org.abego.commons.seq.Seq;
import org.abego.guitesting.swing.GT;
import org.abego.guitesting.swing.GuiTesting;
import org.abego.guitesting.swing.ScreenCaptureSupport.SnapshotIssue;
import org.abego.guitesting.swing.SnapshotReview;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;

final class SnapshotReviewTest {
    //TODO: to commons
    private static File mkdir(File parentDir, String directoryName) {
        File dir = new File(parentDir, directoryName);
        if (!dir.mkdirs()) {
            throw new IllegalStateException("Error creating directory: " +
                    directoryName);
        }
        return dir;
    }

    @Test
    @Disabled
    void showIssuesSnapshotReview(@TempDir File tempDir) {
        GT gt = GuiTesting.newGT();
//        File reportsDir = mkdir(tempDir, "reports");
//        File testresourcesDir = mkdir(tempDir, "test_resources");
//
//        JLabel label = new JLabel();
//        label.setPreferredSize(new Dimension(80,30));
//        label.setOpaque(true);
//        label.setBackground(Color.ORANGE);
//        label.setText("foo");
//        gt.showInFrame(label);
//       // gt.waitUntilScreenshotMatchesSnapshot(label, "orange");
//        label.setBackground(Color.BLUE);
//        gt.waitUntilScreenshotMatchesSnapshot(label, "blue");
//        gt.setTestResourcesDirectoryPath(testresourcesDir.getAbsolutePath());
//        gt.setSnapshotReportDirectory(reportsDir);
//
        SnapshotReview review = gt.newSnapshotReview();
        review.showIssues();
        gt.pause();
    }


}

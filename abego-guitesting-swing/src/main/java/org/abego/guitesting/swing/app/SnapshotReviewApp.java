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

package org.abego.guitesting.swing.app;

import org.abego.guitesting.swing.GT;
import org.abego.guitesting.swing.GuiTesting;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SnapshotReviewApp {
    private static final Logger LOGGER = Logger.getLogger(SnapshotReviewApp.class.getName());
    private final GT gt = GuiTesting.newGT();

    private SnapshotReviewApp(String[] args) {
        int argc = args.length;

        // When no testResourcesDirectory is explicitly specified in the
        // arguments and the default one does not exist check if we find
        // something when we assume test sources are places below "test_src".
        if (argc == 0 && !getTestResourcesDirectory().isDirectory()) {
            File dir = new File("test_src/resources");
            if (dir.isDirectory()) {
                gt.setTestResourcesDirectoryPath(dir.getAbsolutePath());
            }
        }

        if (argc > 1) {
            gt.setTestResourcesDirectoryPath(args[0]);
        }
        if (argc > 2) {
            gt.setSnapshotReportDirectory(new File(args[1]));
        }

        LOGGER.log(Level.INFO,
                "SnapshotReviewApp(testResourcesDirectoryPath={0};SnapshotReportDirectory={1})", //NON-NLS
                new Object[]{
                        getTestResourcesDirectory().getAbsolutePath(),
                        getSnapshotReportDirectory().getAbsolutePath()});
    }

    public static SnapshotReviewApp newSnapshotReviewApp(String[] args) {
        return new SnapshotReviewApp(args);
    }

    public void run() {
        if (!getTestResourcesDirectory().isDirectory()) {
            throw new IllegalArgumentException(
                    String.format("Test resource directory not found: %s", //NON-NLS
                            getTestResourcesDirectory().getAbsolutePath()));
        }
        if (!getSnapshotReportDirectory().isDirectory()) {
            throw new IllegalArgumentException(
                    String.format("Reports directory not found: %s", //NON-NLS
                            getSnapshotReportDirectory().getAbsolutePath()));
        }
        gt.newSnapshotReview().showIssues();
    }

    private File getTestResourcesDirectory() {
        return new File(gt.getTestResourcesDirectoryPath());
    }

    private File getSnapshotReportDirectory() {
        return gt.getSnapshotReportDirectory();
    }
}

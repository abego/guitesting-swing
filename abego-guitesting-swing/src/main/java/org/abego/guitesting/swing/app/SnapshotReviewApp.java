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
import org.eclipse.jdt.annotation.Nullable;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SnapshotReviewApp {
    private static final Logger LOGGER = Logger.getLogger(SnapshotReviewApp.class.getName());
    @SuppressWarnings("DuplicateStringLiteralInspection")
    private static final String[] testResourcesDirectoryOptions = new String[]{
            "src/test/resources", //NON-NLS
            "test_resources", //NON-NLS
            "test_src/resources", //NON-NLS
    };
    private final GT gt = GuiTesting.newGT();

    private SnapshotReviewApp(String[] args) {
        int argc = args.length;
        @Nullable String testResourcesDirectory = argc > 0 ? args[0] : null;
        @Nullable String snapshotReportDirectory = argc > 1 ? args[1] : null;

        // When no testResourcesDirectory is explicitly specified in the
        // arguments and the default one does not exist check if one of the
        // testResourcesDirectoryOptions exists. If yes, use it.
        if (testResourcesDirectory == null && !getTestResourcesDirectory().isDirectory()) {
            testResourcesDirectory = findExistingTestResourcesDirectory();
        }

        if (testResourcesDirectory != null) {
            gt.setTestResourcesDirectory(new File(testResourcesDirectory));
        }
        if (snapshotReportDirectory != null) {
            gt.setSnapshotReportDirectory(new File(snapshotReportDirectory));
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

    @Nullable
    private String findExistingTestResourcesDirectory() {
        for (String path : testResourcesDirectoryOptions) {
            File dir = new File(path);
            if (dir.isDirectory()) {
                return dir.getAbsolutePath();
            }
        }
        return null;
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
        return gt.getTestResourcesDirectory();
    }

    private File getSnapshotReportDirectory() {
        return gt.getSnapshotReportDirectory();
    }
}

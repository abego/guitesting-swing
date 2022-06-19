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

package org.abego.guitesting.swing.internal.snapshotreview.app;

import org.abego.guitesting.swing.GT;
import org.abego.guitesting.swing.GuiTesting;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import javax.swing.JFrame;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.abego.commons.io.FileUtil.findExistingDirectory;

public class SnapshotReviewApp {
    private static final Logger LOGGER = Logger.getLogger(SnapshotReviewApp.class.getName());
    private final GT gt = GuiTesting.newGT();

    private SnapshotReviewApp(String... args) {
        int argc = args.length;
        @Nullable File testResourcesDirectory = argc > 0 ? new File(args[0]) : null;
        @Nullable File snapshotReportDirectory = argc > 1 ? new File(args[1]) : null;

        gt.adjustTestResourcesDirectory(testResourcesDirectory);
        gt.adjustSnapshotReportDirectory(snapshotReportDirectory);

        LOGGER.log(Level.INFO,
                "SnapshotReviewApp(testResourcesDirectoryPath={0};SnapshotReportDirectory={1})", //NON-NLS
                new Object[]{
                        getTestResourcesDirectory().getAbsolutePath(),
                        getSnapshotReportDirectory().getAbsolutePath()});
    }

    /**
     * Returns a new SnapshotReviewApp.
     * <p>
     * Two optional arguments may be used to configure the SnapshotReviewApp:
     * <ol>
     *     <li>testResourcesDirectory</li>
     *     <li>snapshotReportDirectory</li>
     * </ol>
     */
    public static SnapshotReviewApp newSnapshotReviewApp(String... args) {
        return new SnapshotReviewApp(args);
    }

    public void run() {
        showSnapshotReviewFrame();
    }

    public void runAndWait() {
        JFrame frame = showSnapshotReviewFrame();

        // wait until the review window is closed
        waitUntilWindowIsClosed(frame);
    }

    private static void waitUntilWindowIsClosed(Window window) {
        Object lock = new Object();
        window.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                synchronized (lock) {
                    lock.notify();
                }
            }
        });
        synchronized (lock) {
            try {
                lock.wait();
            } catch (InterruptedException e) {
                // nothing to do
            }
        }
    }

    @NonNull
    private JFrame showSnapshotReviewFrame() {
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
        return gt.newSnapshotReviewService().showSnapshotReviewFrame();
    }

    private File getTestResourcesDirectory() {
        return gt.getTestResourcesDirectory();
    }

    public File getSnapshotReportDirectory() {
        return gt.getSnapshotReportDirectory();
    }

    public boolean hasSnapshotIssues() {
        return gt.newSnapshotReviewService().hasSnapshotIssues();
    }
}

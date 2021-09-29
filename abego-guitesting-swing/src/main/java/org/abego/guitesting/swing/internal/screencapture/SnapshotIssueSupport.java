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
import org.abego.guitesting.swing.ScreenCaptureSupport.SnapshotIssue;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Integer.parseInt;
import static java.nio.file.Files.walkFileTree;
import static org.abego.commons.io.FileUtil.toURL;
import static org.abego.commons.seq.SeqUtil.newSeq;
import static org.abego.guitesting.swing.internal.screencapture.ScreenCaptureSupportImpl.SNAP_SHOTS_DIRECTORY_NAME;

final class SnapshotIssueSupport {
    /**
     * The {@link Pattern} to match the file name of an "expectedImage".
     * <p>
     * Groups
     * <ul>
     *     <li>1 - packageName</li>
     *     <li>2 - fileName</li>
     *     <li>3 - index</li>
     * </ul>
     **/
    private final static Pattern EXPECTED_IMAGE_FILE_NAME_PATTERN =
            Pattern.compile("([-a-z.\\\\]+)([^@]*?)-expectedImage@(\\d+)\\.png");
    private final List<Issue> items = new ArrayList<>();
    private final File guitestingReportsDir;
    private final File testResourcesDir;

    private SnapshotIssueSupport(File guitestingReportsDir, File testResourcesDir) {
        this.guitestingReportsDir = guitestingReportsDir;
        this.testResourcesDir = testResourcesDir;
    }

    public static SnapshotIssueSupport newSnapshotIssueSupport(File guitestingReportsDir, File testResourcesDir) {
        return new SnapshotIssueSupport(guitestingReportsDir, testResourcesDir);
    }

    public Seq<? extends SnapshotIssue> findSnapshotIssues() {
        items.clear();

        SimpleFileVisitor<Path> visitor = new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

                String input = file.getFileName().toString();
                Matcher m = EXPECTED_IMAGE_FILE_NAME_PATTERN.matcher(input);
                if (m.matches()) {
                    String packageName = m.group(1);
                    String fileName = m.group(2);
                    int index = parseInt(m.group(3));
                    items.add(new Issue(packageName, fileName, index));
                }

                return super.visitFile(file, attrs);
            }
        };

        try {
            Path root = guitestingReportsDir.toPath();
            if (root.toFile().isDirectory()) {
                walkFileTree(root, visitor);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        return newSeq(items);
    }

    private final class Issue implements SnapshotIssue {

        private final String packageName;
        private final String fileName;
        private final int index;

        private Issue(String packageName, String fileName, int index) {

            this.packageName = packageName;
            this.fileName = fileName;
            this.index = index;
        }

        @Override
        public String getSnapshotName() {
            //noinspection StringConcatenation
            return packageName + fileName;
        }

        @Override
        public int getIndex() {
            return index;
        }

        @Override
        public String getLabel() {
            String s = getSnapshotName();
            int i = getIndex();
            //noinspection StringConcatenation
            return i > 0 ? s + "@" + i : s;
        }

        @Override
        public URL getActualImage() {
            return toURL(getFileInReportsImagesDir("-actualImage.png")); //NON-NLS
        }

        @Override
        public URL getExpectedImage() {
            //noinspection StringConcatenation
            return toURL(getFileInReportsImagesDir("-expectedImage@" + getIndex() + ".png")); //NON-NLS
        }

        @Override
        public URL getDifferenceImage() {
            //noinspection StringConcatenation
            return toURL(getFileInReportsImagesDir("-differenceImage@" + getIndex() + ".png")); //NON-NLS
        }

        @Override
        public URL getOverwriteURL() {
            return toURL(getSnapshotFile(getIndex()));
        }

        @Override
        public URL getAddAlternativeURL() {
            return toURL(getSnapshotFileToAdd());
        }

        private File getSnapshotFileToAdd() {
            File result;
            int i = 0;
            do {
                result = getSnapshotFile(i++);
            } while (result.exists());
            return result;
        }

        private File getSnapshotFile(int i) {
            return new File(
                    testResourcesDir,
                    packageName.replace(".", "/") + SNAP_SHOTS_DIRECTORY_NAME + "/" + fileName + "@" + i + ".png");
        }

        private File getFileInReportsImagesDir(String suffix) {
            return new File(
                    guitestingReportsDir,
                    "images/" + packageName + fileName + suffix);
        }
    }
}

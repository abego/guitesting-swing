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

import org.eclipse.jdt.annotation.Nullable;

import java.io.File;
import java.util.Date;
import java.util.List;

class ScreenshotCompareReportData {
    private final File outputDirectory;
    private final String methodName;
    private final Date timestamp;
    private final String actualImageFileName;
    private final List<ExpectedAndDifferenceFile> expectedAndDifferenceFiles;
    private final Exception exception;

    private final @Nullable File newImageFileForResources;

    private ScreenshotCompareReportData(File outputDirectory, String methodName, Date timestamp, String actualImageFileName, List<ExpectedAndDifferenceFile> expectedAndDifferenceFiles, Exception exception, @Nullable File newImageFileForResources) {
        this.outputDirectory = outputDirectory;
        this.methodName = methodName;
        this.timestamp = timestamp;
        this.actualImageFileName = actualImageFileName;
        this.expectedAndDifferenceFiles = expectedAndDifferenceFiles;
        this.exception = exception;
        this.newImageFileForResources = newImageFileForResources;
    }

    static ScreenshotCompareReportData of(
            File outputDirectory,
            String methodName,
            Date timestamp,
            String actualImageFileName,
            List<ExpectedAndDifferenceFile> expectedAndDifferenceFiles,
            Exception exception,
            @Nullable File newImageFileForResources) {
        return new ScreenshotCompareReportData(outputDirectory, methodName, timestamp, actualImageFileName, expectedAndDifferenceFiles, exception, newImageFileForResources);
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

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

import org.abego.commons.io.PrintStreamToBuffer;
import org.eclipse.jdt.annotation.Nullable;

import java.io.File;
import java.util.List;

import static org.abego.commons.io.PrintStreamToBuffer.newPrintStreamToBuffer;

class ScreenshotCompareReportData {
    private final File outputDirectory;
    private final String methodName;
    private final String actualImageFileName;
    private final List<ExpectedAndDifferenceFile> expectedAndDifferenceFiles;
    private final Exception exception;
    private final String timestamp;

    private final @Nullable File newImageFile;

    private ScreenshotCompareReportData(File outputDirectory, String methodName, String actualImageFileName, List<ExpectedAndDifferenceFile> expectedAndDifferenceFiles, Exception exception, @Nullable File newImageFile, String timestamp) {
        this.outputDirectory = outputDirectory;
        this.methodName = methodName;
        this.actualImageFileName = actualImageFileName;
        this.expectedAndDifferenceFiles = expectedAndDifferenceFiles;
        this.exception = exception;
        this.newImageFile = newImageFile;
        this.timestamp = timestamp;
    }

    static ScreenshotCompareReportData of(
            File outputDirectory,
            String methodName,
            String actualImageFileName,
            List<ExpectedAndDifferenceFile> expectedAndDifferenceFiles,
            Exception exception,
            @Nullable File newImageFile,
            String timestamp) {
        return new ScreenshotCompareReportData(outputDirectory, methodName, actualImageFileName, expectedAndDifferenceFiles, exception, newImageFile, timestamp);
    }

    public File getOutputDirectory() {
        return outputDirectory;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getActualImageFilePath() {
        return actualImageFileName;
    }

    public String getActualImageAbsoluteFilePath() {
        return new File(getOutputDirectory(), getActualImageFilePath()).getAbsolutePath();
    }

    public List<ExpectedAndDifferenceFile> getExpectedAndDifferenceFiles() {
        return expectedAndDifferenceFiles;
    }

    public String getStackTrace() {
        PrintStreamToBuffer result = newPrintStreamToBuffer();
        exception.printStackTrace(result);
        return result.text();
    }

    public String getNewImageAbsoluteFilePath() {
        return newImageFile != null ? newImageFile.getAbsolutePath() : null;
    }

}

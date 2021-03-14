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

import org.abego.guitesting.swing.GuiTestingException;

import java.io.File;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

public class ScreenshotCompareHtmlReport {
    private final ScreenshotCompareReportData reportData;

    private ScreenshotCompareHtmlReport(ScreenshotCompareReportData reportData) {
        this.reportData = reportData;
    }

    public static ScreenshotCompareHtmlReport of(ScreenshotCompareReportData reportData) {
        return new ScreenshotCompareHtmlReport(reportData);
    }

    public File writeReportFile() {

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


}

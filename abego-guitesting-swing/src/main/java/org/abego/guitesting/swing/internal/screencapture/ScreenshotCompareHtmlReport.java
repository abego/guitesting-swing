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
import org.abego.guitesting.swing.internal.GuiTestingUtil;

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

    private static String copyFileStatement(String source, String target) {
        return GuiTestingUtil.isMacOS()
                ? "cp -rf " + source + " " + target
                : "copy /Y " + source + " " + target;
    }

    @SuppressWarnings({"StringConcatenation", "HardCodedStringLiteral"})
    public File writeReportFile() {

        File reportFile = new File(reportData.getOutputDirectory(), reportData.getMethodName() + "-failed.html");
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
                    "<img src=\"" + reportData.getActualImageFilePath() + "\" alt=\"actual image\">\n");

            if (reportData.getNewImageAbsoluteFilePath() != null) {
                printCopyFileStatement(report,
                        "To make the image an additional option of the snapshot run the following in a command line terminal:",
                        reportData.getActualImageAbsoluteFilePath(),
                        reportData.getNewImageAbsoluteFilePath());
            }

            int n = reportData.getExpectedAndDifferenceFiles().size();
            int i = 1;
            for (ExpectedAndDifferenceFile item : reportData.getExpectedAndDifferenceFiles()) {
                report.println("" +
                        "<h2>Expected (Option " + i + " of " + n + ")</h2>\n" +
                        "<img src=\"" + item.expectedImageFilePath + "\" alt=\"expected image " + i + "\">\n" +
                        "<h3>Difference</h3>\n" +
                        "<img src=\"" + item.differenceImageFilePath + "\" alt=\"difference image " + i + "\">\n");
                printCopyFileStatement(report,
                        "To replace/overwrite the expected image with the actual image run the following in a command line terminal:",
                        reportData.getActualImageAbsoluteFilePath(),
                        reportData.getExpectedImageAbsoluteFilePath(i - 1));
                i++;
            }

            report.println("<h2>Stack</h2>\n<pre>");
            report.println(reportData.getStackTrace());
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

    private void printCopyFileStatement(PrintStream report, String caption, String source, String target) {
        String copyStmt = copyFileStatement(source, target);
        report.println("" +
                "<h4>" + caption + "</h4>\n" +
                "<pre>\n" +
                copyStmt + "\n" +
                "</pre>\n" +
                "<button onclick=\"navigator.clipboard.writeText('" + copyStmt + "');\">Copy to clipboard</button>\n"
        );
    }


}

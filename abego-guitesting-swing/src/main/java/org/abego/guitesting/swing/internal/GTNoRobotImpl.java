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

package org.abego.guitesting.swing.internal;

import org.abego.commons.seq.Seq;
import org.abego.guitesting.swing.GT;
import org.abego.guitesting.swing.GuiTestingException;
import org.abego.guitesting.swing.SnapshotReviewService;
import org.abego.guitesting.swing.HeadlessGuiTestingException;
import org.eclipse.jdt.annotation.Nullable;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import java.awt.Color;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.PrintStream;
import java.net.URL;
import java.time.Duration;

/**
 * An implementation of GT that throws {@link HeadlessGuiTestingException}
 * for operations that require a display, mouse or keyboard,
 * i.e. that don't work in a headless environment.
 */
public class GTNoRobotImpl extends GTHeadlessImpl implements GT {
    private GTNoRobotImpl() {
    }

    public static GT newGTNoRobot() {
        return new GTNoRobotImpl();
    }

    @Override
    public void keyPress(int keycode) {
        throw new HeadlessGuiTestingException();
    }

    @Override
    public void keyRelease(int keycode) {
        throw new HeadlessGuiTestingException();
    }

    @Override
    public void mouseMove(int x, int y) {
        throw new HeadlessGuiTestingException();
    }

    @Override
    public void mousePress(int buttonsMask) {
        throw new HeadlessGuiTestingException();
    }

    @Override
    public void mouseRelease(int buttonsMask) {
        throw new HeadlessGuiTestingException();
    }

    @Override
    public void mouseWheel(int notchCount) {
        throw new HeadlessGuiTestingException();
    }

    @Override
    public void dumpAllComponents(PrintStream out) {
        throw new HeadlessGuiTestingException();
    }

    @Override
    public void dumpAllComponents() {
        throw new HeadlessGuiTestingException();
    }

    @Override
    public @Nullable Component focusOwner() {
        throw new HeadlessGuiTestingException();
    }

    @Override
    public void waitUntilAnyFocus() {
        throw new HeadlessGuiTestingException();
    }

    @Override
    public void waitUntilInFocus(Component component) {
        throw new HeadlessGuiTestingException();
    }

    @Override
    public void setFocusOwner(Component component) {
        throw new HeadlessGuiTestingException();
    }

    @Override
    public void focusNext() {
        throw new HeadlessGuiTestingException();
    }

    @Override
    public void focusPrevious() {
        throw new HeadlessGuiTestingException();
    }

    @Override
    public BufferedImage waitUntilPopupMenuScreenshotMatchesSnapshot(JMenu menu, String snapshotName) {
        throw new HeadlessGuiTestingException();
    }

    @Override
    public void waitUntilAllMenuRelatedScreenshotsMatchSnapshot(JMenuBar menubar, String snapshotName) {
        throw new HeadlessGuiTestingException();
    }

    @Override
    public SnapshotReviewService newSnapshotReviewService() {
        throw new HeadlessGuiTestingException();
    }

    @Override
    public void type(String text) {
        throw new HeadlessGuiTestingException();
    }

    @Override
    public void typeKeycode(int keycode) {
        throw new HeadlessGuiTestingException();
    }

    @Override
    public void releaseAllKeys() {
        throw new HeadlessGuiTestingException();
    }

    @Override
    public void click(int buttonsMask, int x, int y, int clickCount) {
        throw new HeadlessGuiTestingException();
    }

    @Override
    public void click(int buttonsMask, Component component, int x, int y, int clickCount) {
        throw new HeadlessGuiTestingException();
    }

    @Override
    public void drag(int buttonsMask, int x1, int y1, int x2, int y2) {
        throw new HeadlessGuiTestingException();
    }

    @Override
    public void drag(int buttonsMask, Component component, int x1, int y1, int x2, int y2) {
        throw new HeadlessGuiTestingException();
    }

    @Override
    public Color getPixelColor(int x, int y) {
        throw new HeadlessGuiTestingException();
    }

    @Override
    public BufferedImage createScreenCapture(Rectangle rectangle) {
        throw new HeadlessGuiTestingException();
    }

    @Override
    public void delay(int milliseconds) {
        throw new HeadlessGuiTestingException();
    }

    @Override
    public boolean isAutoWaitForIdle() {
        throw new HeadlessGuiTestingException();
    }

    @Override
    public void setAutoWaitForIdle(boolean value) {
        throw new HeadlessGuiTestingException();
    }

    @Override
    public int getAutoDelay() {
        throw new HeadlessGuiTestingException();
    }

    @Override
    public void setAutoDelay(int milliseconds) {
        throw new HeadlessGuiTestingException();
    }

    @Override
    public boolean getUseInnerJFrameBounds() {
        throw new HeadlessGuiTestingException();
    }

    @Override
    public void setUseInnerJFrameBounds(boolean value) {
        throw new HeadlessGuiTestingException();
    }

    @Override
    public BufferedImage captureScreen(@Nullable Rectangle screenRect) {
        throw new HeadlessGuiTestingException();
    }

    @Override
    public BufferedImage captureScreen(@Nullable Component component, @Nullable Rectangle rectangle) {
        throw new HeadlessGuiTestingException();
    }

    @Override
    public BufferedImage captureScreen(@Nullable Component component) {
        throw new HeadlessGuiTestingException();
    }

    @Override
    public int getImageDifferenceTolerancePercentage() {
        throw new HeadlessGuiTestingException();
    }

    @Override
    public void setImageDifferenceTolerancePercentage(int value) {
        throw new HeadlessGuiTestingException();
    }

    @Override
    public int getImageDifferenceIgnoredBorderSize() {
        throw new HeadlessGuiTestingException();
    }

    @Override
    public void setImageDifferenceIgnoredBorderSize(int value) {
        throw new HeadlessGuiTestingException();
    }

    @Override
    public int getImageDifferenceIgnoredCornerSize() {
        throw new HeadlessGuiTestingException();
    }

    @Override
    public void setImageDifferenceIgnoredCornerSize(int value) {
        throw new HeadlessGuiTestingException();
    }

    @Override
    public ImageDifference imageDifference(BufferedImage imageA, BufferedImage imageB) {
        throw new HeadlessGuiTestingException();
    }

    @Override
    public BufferedImage imageDifferenceMask(BufferedImage imageA, BufferedImage imageB) {
        throw new HeadlessGuiTestingException();
    }

    @Override
    public void writeImage(RenderedImage image, File file) {
        throw new HeadlessGuiTestingException();
    }

    @Override
    public BufferedImage readImage(File file) {
        throw new HeadlessGuiTestingException();
    }

    @Override
    public BufferedImage readImage(URL url) {
        throw new HeadlessGuiTestingException();
    }

    @Override
    public BufferedImage waitUntilScreenshotMatchesImage(Component component, @Nullable Rectangle rectangle, BufferedImage... expectedImages) {
        throw new HeadlessGuiTestingException();
    }

    @Override
    public BufferedImage waitUntilScreenshotMatchesImage(Component component, BufferedImage... expectedImages) {
        throw new HeadlessGuiTestingException();
    }

    @Override
    public boolean getGenerateSnapshotIfMissing() {
        throw new HeadlessGuiTestingException();
    }

    @Override
    public void setGenerateSnapshotIfMissing(boolean value) {
        throw new HeadlessGuiTestingException();
    }

    @Override
    public Duration getDelayBeforeNewSnapshot() {
        throw new HeadlessGuiTestingException();
    }

    @Override
    public void setDelayBeforeNewSnapshot(Duration duration) {
        throw new HeadlessGuiTestingException();
    }

    @Override
    public File getTestResourcesDirectory() {
        throw new HeadlessGuiTestingException();
    }

    @Override
    public void setTestResourcesDirectory(File directory) {
        throw new HeadlessGuiTestingException();
    }

    @Override
    public String getSnapshotName(@Nullable String name) {
        throw new HeadlessGuiTestingException();
    }

    @Override
    public BufferedImage[] getImagesOfSnapshot(String name) {
        throw new HeadlessGuiTestingException();
    }

    @Override
    public BufferedImage waitUntilScreenshotMatchesSnapshot(Component component, @Nullable Rectangle rectangle, String snapshotName) throws GuiTestingException {
        throw new HeadlessGuiTestingException();
    }

    @Override
    public File getSnapshotReportDirectory() {
        throw new HeadlessGuiTestingException();
    }

    @Override
    public void setSnapshotReportDirectory(File directory) {
        throw new HeadlessGuiTestingException();
    }

    @Override
    public void resetScreenCaptureSupport() {
        throw new HeadlessGuiTestingException();
    }

    @Override
    public void waitForIdle() {
        throw new HeadlessGuiTestingException();
    }

    @Override
    public <T extends Window> Seq<T> allWindowsIncludingInvisibleOnes(Class<T> windowClass) {
        throw new HeadlessGuiTestingException();
    }
}

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
import org.abego.guitesting.swing.ComponentBaseSupport;
import org.abego.guitesting.swing.FocusSupport;
import org.abego.guitesting.swing.GT;
import org.abego.guitesting.swing.GuiTestingException;
import org.abego.guitesting.swing.KeyboardSupport;
import org.abego.guitesting.swing.MouseSupport;
import org.abego.guitesting.swing.ScreenCaptureSupport;
import org.abego.guitesting.swing.SnapshotReviewService;
import org.abego.guitesting.swing.WaitForIdleSupport;
import org.abego.guitesting.swing.WindowBaseSupport;
import org.abego.guitesting.swing.internal.snapshotreview.SnapshotReviewServices;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.function.Executable;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import java.awt.Color;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.PrintStream;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;
import static org.abego.guitesting.swing.internal.ComponentSupportImpl.newComponentSupport;
import static org.abego.guitesting.swing.internal.FocusSupportImpl.newFocusSupport;
import static org.abego.guitesting.swing.internal.KeyboardSupportImpl.newKeyboardSupport;
import static org.abego.guitesting.swing.internal.MouseSupportImpl.newMouseSupport;
import static org.abego.guitesting.swing.internal.WaitForIdleSupportImpl.newWaitForIdleSupport;
import static org.abego.guitesting.swing.internal.WindowSupportImpl.newWindowSupport;
import static org.abego.guitesting.swing.internal.screencapture.ScreenCaptureSupportImpl.newScreenCaptureSupport;
import static org.junit.jupiter.api.Assertions.assertAll;

public final class GTImpl extends GTHeadlessImpl implements GT {

    private final Robot robot;
    private final WaitForIdleSupport waitForIdleSupport;
    private final KeyboardSupport keyboardSupport;
    private final MouseSupport mouseSupport;
    private final WindowBaseSupport windowSupport;
    private final ComponentBaseSupport componentSupport;
    private final FocusSupport focusSupport;
    private final ScreenCaptureSupport screenCaptureSupport;

    private GTImpl(Robot robot) {
        this.robot = robot;
        this.waitForIdleSupport = newWaitForIdleSupport(robot);
        this.keyboardSupport = newKeyboardSupport(robot, waitForIdleSupport);
        this.mouseSupport = newMouseSupport(robot, waitForIdleSupport);
        this.windowSupport = newWindowSupport();
        this.componentSupport = newComponentSupport(windowSupport::allWindows);
        this.focusSupport = newFocusSupport(this, this, keyboardSupport);
        this.screenCaptureSupport = newScreenCaptureSupport(robot, this, this);
    }

    public static GT newGTImpl(Robot robot) {
        return new GTImpl(robot);
    }

    // ======================================================================
    // FocusSupport
    // ======================================================================

    @Nullable
    @Override
    public Component focusOwner() {
        return focusSupport.focusOwner();
    }

    @Override
    public void waitUntilAnyFocus() {
        focusSupport.waitUntilAnyFocus();
    }

    @Override
    public void waitUntilInFocus(Component component) {
        focusSupport.waitUntilInFocus(component);
    }

    @Override
    public void setFocusOwner(Component component) {
        focusSupport.setFocusOwner(component);
    }

    @Override
    public void focusNext() {
        focusSupport.focusNext();
    }

    @Override
    public void focusPrevious() {
        focusSupport.focusPrevious();
    }

    // ======================================================================
    // KeyboardSupport
    // ======================================================================

    @Override
    public void keyPress(int keycode) {
        keyboardSupport.keyPress(keycode);
    }

    @Override
    public void keyRelease(int keycode) {
        keyboardSupport.keyRelease(keycode);
    }

    @Override
    public void type(String text) {
        keyboardSupport.type(text);
    }

    @Override
    public void typeKeycode(int keycode) {
        keyboardSupport.typeKeycode(keycode);
    }

    @Override
    public void releaseAllKeys() {
        keyboardSupport.releaseAllKeys();
    }

    // ======================================================================
    // MouseSupport
    // ======================================================================

    @Override
    public void click(int buttonsMask, int x, int y, int clickCount) {
        mouseSupport.click(buttonsMask, x, y, clickCount);
    }

    @Override
    public void click(int buttonsMask, Component component, int x, int y, int clickCount) {
        mouseSupport.click(buttonsMask, component, x, y, clickCount);
    }

    @Override
    public void drag(int buttonsMask, int x1, int y1, int x2, int y2) {
        mouseSupport.drag(buttonsMask, x1, y1, x2, y2);
    }

    @Override
    public void drag(int buttonsMask, Component component, int x1, int y1, int x2, int y2) {
        mouseSupport.drag(buttonsMask, component, x1, y1, x2, y2);
    }

    @Override
    public void mouseMove(int x, int y) {
        mouseSupport.mouseMove(x, y);
    }

    @Override
    public void mousePress(int buttonsMask) {
        mouseSupport.mousePress(buttonsMask);
    }

    @Override
    public void mouseRelease(int buttonsMask) {
        mouseSupport.mouseRelease(buttonsMask);
    }

    @Override
    public void mouseWheel(int notchCount) {
        mouseSupport.mouseWheel(notchCount);
    }

    // ======================================================================
    // RobotAPI
    // ======================================================================

    @Override
    public Color getPixelColor(int x, int y) {
        return robot.getPixelColor(x, y);
    }

    @Override
    public BufferedImage createScreenCapture(Rectangle rectangle) {
        return robot.createScreenCapture(rectangle);
    }

    @Override
    public void delay(int milliseconds) {
        robot.delay(milliseconds);
    }

    @Override
    public boolean isAutoWaitForIdle() {
        return robot.isAutoWaitForIdle();
    }

    @Override
    public void setAutoWaitForIdle(boolean isOn) {
        robot.setAutoWaitForIdle(isOn);
    }

    @Override
    public int getAutoDelay() {
        return robot.getAutoDelay();
    }

    @Override
    public void setAutoDelay(int ms) {
        robot.setAutoDelay(ms);
    }


    // ======================================================================
    // ScreenCaptureSupport API
    // ======================================================================

    @Override
    public boolean getUseInnerJFrameBounds() {
        return screenCaptureSupport.getUseInnerJFrameBounds();
    }

    @Override
    public void setUseInnerJFrameBounds(boolean value) {
        screenCaptureSupport.setUseInnerJFrameBounds(value);
    }

    @Override
    public BufferedImage captureScreen(@Nullable Rectangle screenRect) {
        return screenCaptureSupport.captureScreen(screenRect);
    }

    @Override
    public BufferedImage captureScreen(@Nullable Component component, @Nullable Rectangle rectangle) {
        return screenCaptureSupport.captureScreen(component, rectangle);
    }

    @Override
    public BufferedImage captureScreen(@Nullable Component component) {
        return screenCaptureSupport.captureScreen(component);
    }

    @Override
    public int getImageDifferenceTolerancePercentage() {
        return screenCaptureSupport.getImageDifferenceTolerancePercentage();
    }

    @Override
    public void setImageDifferenceTolerancePercentage(int value) {
        screenCaptureSupport.setImageDifferenceTolerancePercentage(value);
    }

    @Override
    public int getImageDifferenceIgnoredBorderSize() {
        return screenCaptureSupport.getImageDifferenceIgnoredBorderSize();
    }

    @Override
    public void setImageDifferenceIgnoredBorderSize(int value) {
        screenCaptureSupport.setImageDifferenceIgnoredBorderSize(value);
    }

    @Override
    public int getImageDifferenceIgnoredCornerSize() {
        return screenCaptureSupport.getImageDifferenceIgnoredCornerSize();
    }

    @Override
    public void setImageDifferenceIgnoredCornerSize(int value) {
        screenCaptureSupport.setImageDifferenceIgnoredCornerSize(value);

    }

    @Override
    public ImageDifference imageDifference(BufferedImage imageA, BufferedImage imageB) {
        return screenCaptureSupport.imageDifference(imageA, imageB);
    }

    @Override
    public BufferedImage imageDifferenceMask(BufferedImage imageA, BufferedImage imageB) {
        return screenCaptureSupport.imageDifferenceMask(imageA, imageB);
    }

    @Override
    public BufferedImage waitUntilScreenshotMatchesImage(Component component, @Nullable Rectangle rectangle, BufferedImage... expectedImages) {
        return screenCaptureSupport.waitUntilScreenshotMatchesImage(component, rectangle, expectedImages);
    }

    @Override
    public BufferedImage waitUntilScreenshotMatchesImage(Component component, BufferedImage... expectedImages) {
        return screenCaptureSupport.waitUntilScreenshotMatchesImage(component, expectedImages);
    }

    @Override
    public boolean getGenerateSnapshotIfMissing() {
        return screenCaptureSupport.getGenerateSnapshotIfMissing();
    }

    @Override
    public void setGenerateSnapshotIfMissing(boolean value) {
        screenCaptureSupport.setGenerateSnapshotIfMissing(value);
    }

    @Override
    public Duration getDelayBeforeNewSnapshot() {
        return screenCaptureSupport.getDelayBeforeNewSnapshot();
    }

    @Override
    public void setDelayBeforeNewSnapshot(Duration duration) {
        screenCaptureSupport.setDelayBeforeNewSnapshot(duration);
    }

    @Override
    public File getTestResourcesDirectory() {
        return screenCaptureSupport.getTestResourcesDirectory();
    }

    @Override
    public void setTestResourcesDirectory(File directory) {
        screenCaptureSupport.setTestResourcesDirectory(directory);
    }

    @Override
    public String getSnapshotName(@Nullable String name) {
        return screenCaptureSupport.getSnapshotName(name);
    }

    @Override
    public BufferedImage waitUntilScreenshotMatchesSnapshot(Component component, @Nullable Rectangle rectangle, String snapshotName) throws GuiTestingException {
        return screenCaptureSupport.waitUntilScreenshotMatchesSnapshot(component, rectangle, snapshotName);
    }

    @Override
    public BufferedImage waitUntilScreenshotMatchesSnapshot(Component component, String snapshotName) throws GuiTestingException {
        return screenCaptureSupport.waitUntilScreenshotMatchesSnapshot(component, snapshotName);
    }

    @Override
    public BufferedImage waitUntilPopupMenuScreenshotMatchesSnapshot(JMenu menu, String snapshotName) {
        if (menu.getMenuComponentCount() == 0) {
            // When a menu has no items take a snapshot of the menu itself
            return waitUntilScreenshotMatchesSnapshot(menu, snapshotName);
        }

        return withPopupMenuVisibleGet(menu, () -> {
            JPopupMenu jPopupMenu = waitForComponentWith(
                    JPopupMenu.class, c -> c.getInvoker() == menu);
            return waitUntilScreenshotMatchesSnapshot(jPopupMenu, snapshotName);
        });
    }

    @Override
    public void waitUntilAllMenuRelatedScreenshotsMatchSnapshot(
            JMenuBar menubar, String snapshotName) {

        List<Executable> allTests = new ArrayList<>();

        //noinspection StringConcatenation
        allTests.add(() ->
                waitUntilScreenshotMatchesSnapshot(menubar, snapshotName + "-menubar")); //NON-NLS

        //noinspection StringConcatenation
        allTests.add(() -> withAltKeyPressedRun(() ->
                waitUntilScreenshotMatchesSnapshot(menubar, snapshotName + "-menubar-mnemonics"))); //NON-NLS

        for (int i = 0; i < menubar.getMenuCount(); i++) {
            int index = i;
            allTests.add(() -> waitUntilMenuScreenshotWithAltKeyMatchesSnapshotItems(menubar, snapshotName, index));
        }

        assertAll(allTests);
    }

    private void waitUntilMenuScreenshotWithAltKeyMatchesSnapshotItems(
            JMenuBar menubar, String snapshotName, int index) {
        //noinspection StringConcatenation
        withAltKeyPressedRun(() ->
                waitUntilMenuScreenshotsMatchSnapshot(
                        menubar.getMenu(index), snapshotName + "-menu-" + index));//NON-NLS
    }

    @Override
    public BufferedImage[] getImagesOfSnapshot(String name) {
        return screenCaptureSupport.getImagesOfSnapshot(name);
    }

    @Override
    public void writeImage(RenderedImage image, File file) {
        screenCaptureSupport.writeImage(image, file);
    }

    @Override
    public BufferedImage readImage(File file) {
        return screenCaptureSupport.readImage(file);
    }

    @Override
    public BufferedImage readImage(URL url) {
        return screenCaptureSupport.readImage(url);
    }

    private void waitUntilMenuScreenshotsMatchSnapshot(
            JMenu menu, String menuSnapshotName) {
        List<Executable> allTests = new ArrayList<>();

        allTests.add(() -> waitUntilPopupMenuScreenshotMatchesSnapshot(menu, menuSnapshotName));
        for (int i = 0; i < menu.getItemCount(); i++) {
            int index = i;
            allTests.add(() -> waitUntilMenuScreenshotMatchSnapshotHelper(
                    menu, menuSnapshotName, index));
        }
        assertAll(allTests);
    }

    private void waitUntilMenuScreenshotMatchSnapshotHelper(JMenu menu, String menuSnapshotName, int i) {
        JMenuItem menuItem = menu.getItem(i);
        if (menuItem instanceof JMenu) {
            JMenu subMenu = (JMenu) menuItem;
            menu.setPopupMenuVisible(true);
            try {
                //noinspection StringConcatenation
                waitUntilMenuScreenshotsMatchSnapshot(subMenu, menuSnapshotName + "." + i);
            } finally {
                menu.setPopupMenuVisible(false);
            }
        }
    }

    private void withAltKeyPressedRun(Runnable runnable) {
        keyPress(KeyEvent.VK_ALT);
        try {
            runnable.run();
        } finally {
            keyRelease(KeyEvent.VK_ALT);

            // On Windows releasing the ALT key may leave the menu in
            // a "selected" state.
            // Also the "show mnemonics" mode is not cleared.
            //
            // "Press and release ALT key" again fixes the problem.
            keyPress(KeyEvent.VK_ALT);
            keyRelease(KeyEvent.VK_ALT);
        }
    }

    private static <T> T withPopupMenuVisibleGet(JMenu menu, Supplier<T> supplier) {
        menu.setPopupMenuVisible(true);
        try {
            return supplier.get();
        } finally {
            menu.setPopupMenuVisible(false);
        }
    }


    @Override
    public Seq<SnapshotIssue> getSnapshotIssues() {
        return screenCaptureSupport.getSnapshotIssues();
    }

    @Override
    public File getSnapshotReportDirectory() {
        return screenCaptureSupport.getSnapshotReportDirectory();
    }

    @Override
    public void setSnapshotReportDirectory(File directory) {
        screenCaptureSupport.setSnapshotReportDirectory(directory);
    }

    @Override
    public void resetScreenCaptureSupport() {
        screenCaptureSupport.resetScreenCaptureSupport();
    }

    // ======================================================================
    // SnapshotReviewService
    // ======================================================================

    @Override
    public SnapshotReviewService newSnapshotReviewService() {
        return SnapshotReviewServices.newSnapshotReviewService(
                this::getSnapshotIssues);
    }


    // ======================================================================
    // WaitForIdleSupport
    // ======================================================================

    @Override
    public void waitForIdle() {
        waitForIdleSupport.waitForIdle();
    }


    // ======================================================================
    // WindowSupport
    // ======================================================================

    @Override
    public <T extends Window> Seq<T> allWindowsIncludingInvisibleOnes(Class<T> windowClass) {
        return windowSupport.allWindowsIncludingInvisibleOnes(windowClass);
    }
    // ======================================================================
    // Reset / Cleanup
    // ======================================================================

    @Override
    public void reset() {
        waitForIdle();
        super.reset();
        resetMouse();
        resetScreenCaptureSupport();
        releaseAllKeys();
    }

    @Override
    public void cleanup() {
        super.cleanup();
        disposeAllWindows();
    }

    private void disposeAllWindows() {
        allWindowsIncludingInvisibleOnes().forEach(Window::dispose);

        // wait until all windows are gone.
        waitUntil(() -> allWindows().isEmpty());
    }


    // ======================================================================
    // Debug Support
    // ======================================================================

    @Override
    public void dumpAllComponents(PrintStream out) {
        DebugSupport.dumpAllComponents(windowSupport::allWindows, out);
    }

    @Override
    public void dumpAllComponents() {
        dumpAllComponents(requireNonNull(System.out));
    }

}

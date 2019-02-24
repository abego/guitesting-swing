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

package org.abego.guitesting.internal;

import org.abego.commons.blackboard.Blackboard;
import org.abego.commons.seq.Seq;
import org.abego.guitesting.AssertRetryingSupport;
import org.abego.guitesting.ComponentSupport;
import org.abego.guitesting.DialogAndFrameSupport;
import org.abego.guitesting.EDTSupport;
import org.abego.guitesting.FocusSupport;
import org.abego.guitesting.GuiTesting;
import org.abego.guitesting.KeyboardSupport;
import org.abego.guitesting.MouseSupport;
import org.abego.guitesting.Pause;
import org.abego.guitesting.PauseUntilFunction;
import org.abego.guitesting.PollingSupport;
import org.abego.guitesting.TimeoutSupport;
import org.abego.guitesting.WaitForIdleSupport;
import org.abego.guitesting.WindowSupport;

import javax.annotation.Nullable;
import javax.swing.JFrame;
import java.awt.AWTException;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.time.Duration;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static org.abego.commons.blackboard.Blackboard.newBlackboard;
import static org.abego.commons.lang.exception.UncheckedException.uncheckedException;
import static org.abego.guitesting.internal.AssertRetryingSupportImpl.newAssertRetryingSupport;
import static org.abego.guitesting.internal.ComponentSupportImpl.newComponentSupport;
import static org.abego.guitesting.internal.DialogAndFrameSupportImpl.newDialogAndFrameSupport;
import static org.abego.guitesting.internal.EDTSupportImpl.newEDTSupport;
import static org.abego.guitesting.internal.FocusSupportImpl.newFocusSupport;
import static org.abego.guitesting.internal.KeyboardSupportImpl.newKeyboardSupport;
import static org.abego.guitesting.internal.MouseSupportImpl.newMouseSupport;
import static org.abego.guitesting.internal.PauseImpl.newPause;
import static org.abego.guitesting.internal.PollingSupportImpl.newPollingSupport;
import static org.abego.guitesting.internal.TimeoutSupportImpl.newTimeoutSupport;
import static org.abego.guitesting.internal.WaitForIdleSupportImpl.newWaitForIdleSupport;
import static org.abego.guitesting.internal.WindowSupportImpl.newWindowSupport;

public final class GuiTestingImpl implements GuiTesting {

    private static final String COULD_NOT_CREATE_ROBOT_INSTANCE_MESSAGE = "Could not create Robot instance"; //NON-NLS
    private final Robot robot = newRobot();
    private final Blackboard<Object> blackboard = newBlackboard();
    private final TimeoutSupport timeoutSupport = newTimeoutSupport();
    private final PauseUntilFunction pauseUntilFunction = pause();
    private final AssertRetryingSupport assertRetryingSupport = newAssertRetryingSupport(timeoutSupport);
    private final DialogAndFrameSupport dialogAndFrameSupport = newDialogAndFrameSupport();
    private final EDTSupport edtSupport = newEDTSupport();
    private final WaitForIdleSupport waitForIdleSupport = newWaitForIdleSupport(robot);
    private final KeyboardSupport keyboardSupport = newKeyboardSupport(robot, waitForIdleSupport);
    private final MouseSupport mouseSupport = newMouseSupport(robot, waitForIdleSupport);
    private final PollingSupport pollingSupport = newPollingSupport(timeoutSupport);
    private final WindowSupport windowSupport = newWindowSupport();
    private final ComponentSupport componentSupport = newComponentSupport(windowSupport::allWindows);
    private final FocusSupport focusSupport = newFocusSupport(timeoutSupport, pauseUntilFunction, keyboardSupport);

    private GuiTestingImpl() {
    }

    private static Robot newRobot() {
        try {
            return new Robot();
        } catch (AWTException e) {
            throw uncheckedException(COULD_NOT_CREATE_ROBOT_INSTANCE_MESSAGE, e);
        }
    }

    public static GuiTesting newGuiTesting() {
        return new GuiTestingImpl();
    }

    // ======================================================================
    // AssertRetryingSupport
    // ======================================================================

    @Override
    public <T> void assertEqualsRetrying(T expected, Supplier<T> actualSupplier, @Nullable String message) {
        assertRetryingSupport.assertEqualsRetrying(
                expected, actualSupplier, message);
    }


    // ======================================================================
    // ComponentSupport
    // ======================================================================

    @Override
    public <T extends Component> Seq<T> allComponentsWith(
            Class<T> componentClass, Seq<Component> roots, Predicate<T> condition) {

        return componentSupport.allComponentsWith(componentClass, roots, condition);

    }

    @Override
    public <T extends Component> Seq<T> allComponentsWith(
            Class<T> componentClass, Predicate<T> condition) {

        return componentSupport.allComponentsWith(componentClass, condition);
    }

    // ======================================================================
    // DialogAndFrameSupport
    // ======================================================================

    @Override
    public void showInDialog(Component component) {
        dialogAndFrameSupport.showInDialog(component);
    }

    @Override
    public void showInDialogTitled(String title, Component component) {
        dialogAndFrameSupport.showInDialogTitled(title, component);
    }

    @Override
    public JFrame showInFrame(Component component, @Nullable Point position, @Nullable Dimension size) {
        return dialogAndFrameSupport.showInFrame(component, position, size);
    }

    @Override
    public JFrame showInFrameTitled(String title, @Nullable Component component, @Nullable Point position, @Nullable Dimension size) {
        return dialogAndFrameSupport.showInFrameTitled(title, component, position, size);
    }

    // ======================================================================
    // EDTSupport
    // ======================================================================

    @Override
    public void runInEDT(Runnable runnable) {
        edtSupport.runInEDT(runnable);
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
    public void pauseUntilAnyFocus() {
        focusSupport.pauseUntilAnyFocus();
    }

    @Override
    public void pauseUntilInFocus(Component component) {
        focusSupport.pauseUntilInFocus(component);
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
    // Polling
    // ======================================================================

    @Override
    public <T> T poll(Supplier<T> functionToPoll, Predicate<T> isResult, Duration timeout) {
        return pollingSupport.poll(functionToPoll, isResult, timeout);
    }

    @Override
    public <T> T pollNoFail(Supplier<T> functionToPoll, Predicate<T> isResult, Duration timeout) {
        return pollingSupport.pollNoFail(functionToPoll, isResult, timeout);
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
    // Timeout Support
    // ======================================================================

    @Override
    public Duration initialTimeout() {
        return timeoutSupport.initialTimeout();
    }

    @Override
    public void setInitialTimeout(Duration duration) {
        timeoutSupport.setInitialTimeout(duration);
    }

    @Override
    public void setTimeout(Duration duration) {
        timeoutSupport.setTimeout(duration);
    }

    @Override
    public void runWithTimeout(Duration timeoutDuration, Runnable runnable) {

        timeoutSupport.runWithTimeout(timeoutDuration, runnable);
    }

    @Override
    public Duration timeout() {
        return timeoutSupport.timeout();
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
    // Blackboard
    // ======================================================================

    @Override
    public Blackboard<Object> blackboard() {
        return blackboard;
    }


    // ======================================================================
    // Pausing
    // ======================================================================


    @Override
    public Pause pause() {
        return newPause(this);
    }


    // ======================================================================
    // Reset / Cleanup
    // ======================================================================

    @Override
    public void reset() {
        waitForIdle();
        setTimeout(initialTimeout());
        blackboard().clear();
        releaseAllKeys();
    }

    @Override
    public void cleanup() {
        reset();
        disposeAllWindows();
    }

    private void disposeAllWindows() {
        allWindowsIncludingInvisibleOnes().forEach(Window::dispose);

        // wait until all windows are gone.
        pause().whileTrue(() -> !allWindows().isEmpty());
    }

}

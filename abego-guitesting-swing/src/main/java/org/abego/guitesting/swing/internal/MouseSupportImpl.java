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


import org.abego.guitesting.swing.MouseSupport;
import org.abego.guitesting.swing.WaitForIdleSupport;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.MouseEvent;
import java.text.MessageFormat;
import java.time.Duration;

import static java.awt.event.MouseEvent.MOUSE_PRESSED;
import static java.awt.event.MouseEvent.MOUSE_RELEASED;
import static java.time.Duration.ofMillis;
import static org.abego.commons.lang.ThreadUtil.sleep;
import static org.abego.commons.polling.PollingUtil.pollNoFail;
import static org.abego.guitesting.swing.internal.GuiTestingUtil.toScreenCoordinates;

final class MouseSupportImpl implements MouseSupport {
    private static final int MULTI_CLICK_INTERVAL_MILLIS_DEFAULT = 500;
    private static final int NEAR_DISTANCE = 4;
    private static final Duration MAX_WAIT_TIME_FOR_MOUSE_EVENT = ofMillis(100);

    // Both lastClickTime and lastClickPos need to be static, i.e. be shared
    // by all instances of MouseSupportImpl, to make sure subsequent clicks at
    // the same location are not interpreted as double clicks, even
    // when the second click is handled by a new MouseSupportImpl instance.
    // (This may happen when the JUnit test class creates a new XY/
    // MouseSupportImpl instance for every test).
    private static long lastClickTime = 0;
    private static Point lastClickPos = new Point();

    private final Robot robot;
    private final WaitForIdleSupport waitForIdleSupport;
    private final int multiClickIntervalMillis = multiClickIntervalMillisDefault();
    private volatile boolean gotMouseWheelEvent = false;

    private MouseSupportImpl(Robot robot, WaitForIdleSupport waitForIdleSupport) {
        this.robot = robot;
        this.waitForIdleSupport = waitForIdleSupport;
    }

    private static int multiClickIntervalMillisDefault() {
        Integer i = (Integer) Toolkit.getDefaultToolkit().
                getDesktopProperty("awt.multiClickInterval"); //NON-NLS
        return i != null ? i : MULTI_CLICK_INTERVAL_MILLIS_DEFAULT;
    }

    static MouseSupport newMouseSupport(Robot robot, WaitForIdleSupport waitForIdleSupport) {
        return new MouseSupportImpl(robot, waitForIdleSupport);
    }

    private static void runAndWaitForMouseAt(Point globalLocation, Runnable runnable) {
        new MouseLocationObserver().runAndWaitForMouseAt(globalLocation, runnable);
    }

    private static Point mousePos() {
        return MouseInfo.getPointerInfo().getLocation();
    }

    private static long getLastClickTime() {
        // keep the following code in one line to workaround
        // code coverage issue in IntelliJ
        // @formatter:off
        synchronized (MouseSupportImpl.class) { return lastClickTime; }
        // @formatter:on
    }

    private static void setLastClickTime(long time) {
        synchronized (MouseSupportImpl.class) {
            lastClickTime = time;
        }
    }

    private static Point getLastClickPos() {
        // keep the following code in one line to workaround
        // code coverage issue in IntelliJ
        // @formatter:off
        synchronized (MouseSupportImpl.class) { return lastClickPos; }
        // @formatter:on
    }

    private static void setLastClickPos(Point position) {
        synchronized (MouseSupportImpl.class) {
            lastClickPos = position;
        }
    }

    @Override
    public void click(int buttonsMask, int x, int y, int clickCount) {

        if (clickCount <= 0) {
            throw new IllegalArgumentException("clickCount must be > 0"); //NON-NLS
        }

        mouseMove(x, y);

        avoidMultiClickEvent(x, y);

        for (int i = 0; i < clickCount; i++) {
            mousePress(buttonsMask);
            mouseRelease(buttonsMask);
            setLastClickTime(System.currentTimeMillis());
            sleep(multiClickIntervalMillis / 2);
        }

        setLastClickPos(new Point(x, y));

        waitForIdle();
    }

    @Override
    public void click(int buttonsMask, Component component, int x, int y, int clickCount) {
        Point p = toScreenCoordinates(component, x, y);
        click(buttonsMask, p.x, p.y, clickCount);
    }

    public void drag(int buttonsMask, int x1, int y1, int x2, int y2) {
        Point startPos = new Point(x1, y1);
        Point endPos = new Point(x2, y2);

        waitForIdle();

        // make sure the start of a drag is not mistaken for a double click
        // (delay the drag start if necessary)
        avoidMultiClickEvent(x1, y1);

        // Move mouse to the start position (if necessary) and press the mouse
        runAndWaitForMouseAt(startPos, () -> {
            mouseMove(startPos);
            mousePress(buttonsMask);
        });

        mouseMove(endPos);
        runAndWaitForMouseAt(endPos, () ->
                mouseRelease(buttonsMask));

        setLastClickPos(endPos);
        setLastClickTime(System.currentTimeMillis());
    }


    private void avoidMultiClickEvent(int x, int y) {
        int dx = getLastClickPos().x - x;
        int dy = getLastClickPos().y - y;
        if (Math.abs(dx) <= NEAR_DISTANCE && Math.abs(dy) <= NEAR_DISTANCE) {
            // make sure a new "click sequence" starts not earlier than
            // 2 * multiClickIntervalMillis to avoid recognition as a double click
            long delay = (getLastClickTime() + 2L * multiClickIntervalMillis)
                    - System.currentTimeMillis();
            if (delay > 0) {
                sleep(delay);
            }
        }
    }

    @Override
    public void drag(
            int buttonsMask,
            Component component,
            int x1,
            int y1,
            int x2,
            int y2) {
        Point p1 = toScreenCoordinates(component, x1, y1);
        Point p2 = toScreenCoordinates(component, x2, y2);

        drag(buttonsMask, p1.x, p1.y, p2.x, p2.y);
    }

    private void waitForIdle() {
        waitForIdleSupport.waitForIdle();
    }

    @Override
    public void mouseMove(int x, int y) {
        Point newMousePos = new Point(x, y);

        if (!mousePos().equals(newMousePos)) {
            robot.mouseMove(newMousePos.x, newMousePos.y);
            waitForIdle();
        }

        // The mouse is not always immediately at the expected position. So wait...
        Point currentMousePos = pollNoFail(
                MouseSupportImpl::mousePos, v -> v.equals(newMousePos), MAX_WAIT_TIME_FOR_MOUSE_EVENT);

        if (!currentMousePos.equals(newMousePos)) {
            throw new InternalError(MessageFormat.format(
                    "Error in mouseMove: expected position {0}, got {1}", //NON-NLS
                    newMousePos, currentMousePos));
        }
    }

    @Override
    public void mousePress(int buttonsMask) {
        waitForIdle();
        runAndWaitForMouseAt(mousePos(), () -> robot.mousePress(buttonsMask));
    }

    @Override
    public void mouseRelease(int buttonsMask) {
        waitForIdle();
        runAndWaitForMouseAt(mousePos(), () -> robot.mouseRelease(buttonsMask));

    }

    @Override
    public void mouseWheel(int notchCount) {
        waitForIdle();

        gotMouseWheelEvent = false;
        AWTEventListener listener = e -> gotMouseWheelEvent = true;

        Toolkit.getDefaultToolkit().addAWTEventListener(listener, AWTEvent.MOUSE_WHEEL_EVENT_MASK);
        try {
            robot.mouseWheel(notchCount);

            pollNoFail(() -> gotMouseWheelEvent, v -> v, MAX_WAIT_TIME_FOR_MOUSE_EVENT);
        } finally {
            Toolkit.getDefaultToolkit().removeAWTEventListener(listener);
        }
    }

    /**
     * Because there is a delay between calling a Robot mouse method and its
     * effect in the environment (e.g. a changes mouse position) we sometimes
     * need to wait for the change state, e.g. check the current mouse position.
     * However, this is not always sufficient, as e.g. some events will be
     * posted even after the state change. Therefore, we need to observe the
     * events, too. This is what this class is for.
     */
    private static class MouseLocationObserver {
        private Point lastGlobalMouseEventPos = new Point(-1, -1);

        private void runAndWaitForMouseAt(Point globalLocation, Runnable runnable) {

            AWTEventListener listener = event -> {
                if (event instanceof MouseEvent) {
                    MouseEvent me = (MouseEvent) event;
                    if (me.getID() == MOUSE_PRESSED || me.getID() == MOUSE_RELEASED) {
                        setLastGlobalMouseEventPos(new Point(me.getXOnScreen(), me.getYOnScreen()));
                    }
                }
            };

            Toolkit.getDefaultToolkit().addAWTEventListener(listener, AWTEvent.MOUSE_EVENT_MASK);

            try {

                runnable.run();

                // Wait for a mouse pressed/release event with the given
                // globalLocation. In some situations, e.g. when moving a frame
                // by dragging in its title bar, no events will be posted.
                // In these cases the timeout will be used.
                pollNoFail(this::getLastGlobalMouseEventPos,
                        p -> p.equals(globalLocation), MAX_WAIT_TIME_FOR_MOUSE_EVENT);

            } finally {
                Toolkit.getDefaultToolkit().removeAWTEventListener(listener);
            }

        }

        private Point getLastGlobalMouseEventPos() {
            // keep the following code in one line to workaround
            // code coverage issue in IntelliJ
            // @formatter:off
            synchronized (this) { return lastGlobalMouseEventPos; }
            // @formatter:on
        }

        private void setLastGlobalMouseEventPos(Point lastGlobalMouseEventPos) {
            synchronized (this) {
                this.lastGlobalMouseEventPos = lastGlobalMouseEventPos;
            }
        }
    }

}

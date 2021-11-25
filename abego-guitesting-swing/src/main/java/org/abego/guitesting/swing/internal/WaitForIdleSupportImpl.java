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

import org.abego.guitesting.swing.WaitForIdleSupport;
import org.eclipse.jdt.annotation.Nullable;

import javax.swing.SwingUtilities;
import java.awt.Robot;
import java.awt.Toolkit;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.abego.commons.lang.ObjectUtil.ignore;
import static org.abego.commons.lang.exception.UncheckedException.newUncheckedException;

final class WaitForIdleSupportImpl implements WaitForIdleSupport {
    private static @Nullable Runnable realSyncRunnable = null;
    private final Robot robot;

    private WaitForIdleSupportImpl(Robot robot) {
        this.robot = robot;
    }

    static WaitForIdleSupport newWaitForIdleSupport(Robot robot) {
        return new WaitForIdleSupportImpl(robot);
    }

    private static void realSync() {
        // The SunToolkit provides a method "realSync" that can increase
        // the reliability of "waitForIdle" because it checks for additional
        // queues, not just the event queue. E.g. the actual rendering of an
        // image may be buffered. So if possible, use this method.

        // As the SunToolkit may not be available in all runtime environments
        // we need to use reflection.
        if (realSyncRunnable == null) {
            realSyncRunnable = sunToolkitReadSyncAsRunnableOrNull();

            if (realSyncRunnable == null) {
                realSyncRunnable = () -> {}; // do nothing by default
            }
        }

        runIfNotNull(realSyncRunnable);
    }

    private static void runIfNotNull(@Nullable Runnable runnable) {
        if (runnable != null) {
            runnable.run();
        }
    }

    @Nullable
    private static Runnable sunToolkitReadSyncAsRunnableOrNull() {
        try {
            Toolkit toolkit = Toolkit.getDefaultToolkit();

            Class<?> sunToolkitClass = Class.forName("sun.awt.SunToolkit");
            if (sunToolkitClass.isInstance(toolkit)) {
                Method m = sunToolkitClass.getMethod("realSync");

                return () -> {
                    try {
                        m.invoke(toolkit);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw newUncheckedException(e);
                    }
                };
            }
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            ignore(e);
        }
        return null;
    }

    @Override
    public void waitForIdle() {
        // Just using "Robot.waitForIdle" and "realSync" is not always sufficient.
        // E.g. the Event queue may be empty but the EventThread may still be busy
        // processing the last Event just grabbed from the queue. To make the
        // "waitForIdle" more reliable we want to wait until we are fairly sure the
        // EventThread is not busy. Therefore, we add an own event now and wait until
        // this event is processed. Then all "previous" events are also processed.
        AtomicBoolean done = new AtomicBoolean(false);
        SwingUtilities.invokeLater(() -> done.set(true));

        robot.waitForIdle();
        realSync();

        while (!done.get()) {
            try {
                //noinspection BusyWait
                Thread.sleep(1);
            } catch (InterruptedException e) {
                done.set(true);
            }
        }
    }

}

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

import org.abego.commons.blackboard.Blackboard;
import org.abego.commons.polling.PollingService;
import org.abego.commons.seq.Seq;
import org.abego.commons.test.AssertRetryingService;
import org.abego.commons.timeout.Timeout;
import org.abego.commons.timeout.TimeoutService;
import org.abego.guitesting.swing.ComponentBaseSupport;
import org.abego.guitesting.swing.DialogAndFrameSupport;
import org.abego.guitesting.swing.EDTSupport;
import org.abego.guitesting.swing.GT;
import org.abego.guitesting.swing.WaitSupport;
import org.abego.guitesting.swing.WindowBaseSupport;
import org.eclipse.jdt.annotation.Nullable;

import javax.swing.JFrame;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.time.Duration;
import java.util.OptionalLong;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static org.abego.commons.blackboard.BlackboardDefault.newBlackboardDefault;
import static org.abego.commons.lang.LongUtil.parseLong;
import static org.abego.commons.polling.Polling.newPollingService;
import static org.abego.commons.test.AssertRetrying.newAssertRetryingService;
import static org.abego.guitesting.swing.internal.ComponentSupportImpl.newComponentSupport;
import static org.abego.guitesting.swing.internal.DialogAndFrameSupportImpl.newDialogAndFrameSupport;
import static org.abego.guitesting.swing.internal.EDTSupportImpl.newEDTSupport;
import static org.abego.guitesting.swing.internal.WaitSupportImpl.newWaitSupport;
import static org.abego.guitesting.swing.internal.WindowSupportImpl.newWindowSupport;

abstract class GTHeadlessImpl implements GT {

    private final Blackboard<Object> blackboard;
    private final TimeoutService timeoutService;
    private final WaitSupport waitSupport;
    private final AssertRetryingService assertRetryingService;
    private final DialogAndFrameSupport dialogAndFrameSupport;
    private final EDTSupport edtSupport;
    private final PollingService pollingService;
    private final WindowBaseSupport windowSupport;
    private final ComponentBaseSupport componentSupport;

    protected GTHeadlessImpl() {
        this.blackboard = newBlackboardDefault();
        this.timeoutService = Timeout.newTimeoutService();
        this.waitSupport = newWaitSupport(timeoutService);
        this.assertRetryingService = newAssertRetryingService(timeoutService);
        this.dialogAndFrameSupport = newDialogAndFrameSupport();
        this.edtSupport = newEDTSupport();
        this.pollingService = newPollingService(timeoutService);
        this.windowSupport = newWindowSupport();
        this.componentSupport = newComponentSupport(windowSupport::allWindows);
    }

    // ======================================================================
    // AssertRetryingService
    // ======================================================================

    @Override
    public <T> void assertEqualsRetrying(T expected, Supplier<T> actualSupplier, @Nullable String message) {
        assertRetryingService.assertEqualsRetrying(
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
    // Polling
    // ======================================================================

    @Override
    public <T> T poll(Supplier<T> functionToPoll, Predicate<T> isResult, Duration timeout) {
        return pollingService.poll(functionToPoll, isResult, timeout);
    }

    @Override
    public <T> T pollNoFail(Supplier<T> functionToPoll, Predicate<T> isResult, Duration timeout) {
        return pollingService.pollNoFail(functionToPoll, isResult, timeout);
    }

    // ======================================================================
    // Timeout Support
    // ======================================================================

    @Override
    public Duration initialTimeout() {
        return timeoutService.initialTimeout();
    }

    @Override
    public void setInitialTimeout(Duration duration) {
        timeoutService.setInitialTimeout(duration);
    }

    @Override
    public void setTimeout(Duration duration) {
        timeoutService.setTimeout(duration);
    }

    @Override
    public void runWithTimeout(Duration timeoutDuration, Runnable runnable) {

        timeoutService.runWithTimeout(timeoutDuration, runnable);
    }

    @Override
    public Duration timeout() {
        return timeoutService.timeout();
    }

    // ======================================================================
    // Blackboard
    // ======================================================================

    @Override
    public Blackboard<Object> blackboard() {
        return blackboard;
    }


    // ======================================================================
    // Reset / Cleanup
    // ======================================================================

    @Override
    public void reset() {
        resetTimeout();
        blackboard().clear();
        resetMouse();
        resetScreenCaptureSupport();
    }

    @Override
    public void cleanup() {
        reset();
    }

    @Override
    public void readSystemProperties() {
        String s = System.getProperties().getProperty(
                SYSTEM_PROPERTY_TIMEOUT_MILLIS);
        if (s != null) {
            OptionalLong millis = parseLong(s);
            millis.ifPresent(i -> {
                Duration timeout = Duration.ofMillis(i);
                setInitialTimeout(timeout);
                setTimeout(timeout);
            });
        }
    }

    // ======================================================================
    // Wait Support
    // ======================================================================

    @Override
    public void waitFor(Duration duration) {
        waitSupport.waitFor(duration);
    }

    @Override
    public void waitForUser(@Nullable String message) {
        waitSupport.waitForUser(message);
    }

    @Override
    public void waitUntil(BooleanSupplier condition) {
        waitSupport.waitUntil(condition);
    }

}

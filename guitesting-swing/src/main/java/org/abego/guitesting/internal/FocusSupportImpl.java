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

import org.abego.commons.timeout.TimeoutSupplier;
import org.abego.guitesting.BasicKeyboardSupport;
import org.abego.guitesting.FocusSupport;
import org.abego.guitesting.WaitUntilFunction;
import org.eclipse.jdt.annotation.Nullable;

import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.time.Duration;

import static org.abego.guitesting.internal.SwingUtil.runInEDT;

final class FocusSupportImpl implements FocusSupport {
    private final TimeoutSupplier timeoutProvider;
    private final WaitUntilFunction waitUntilFunction;
    private final BasicKeyboardSupport basicKeyboardSupport;

    private FocusSupportImpl(
            TimeoutSupplier timeoutProvider,
            WaitUntilFunction waitUntilFunction,
            BasicKeyboardSupport basicKeyboardSupport) {
        this.timeoutProvider = timeoutProvider;
        this.waitUntilFunction = waitUntilFunction;
        this.basicKeyboardSupport = basicKeyboardSupport;
    }

    static FocusSupport newFocusSupport(
            TimeoutSupplier timeoutProvider,
            WaitUntilFunction waitUntilFunction,
            BasicKeyboardSupport basicKeyboardSupport) {

        return new FocusSupportImpl(timeoutProvider, waitUntilFunction, basicKeyboardSupport);
    }

    @Override
    @Nullable
    public Component focusOwner() {
        return KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
    }

    @Override
    public void waitUntilAnyFocus() {
        waitUntilFunction.waitUntil(() -> focusOwner() != null);
    }

    @Override
    public void waitUntilInFocus(Component component) {
        waitUntilFunction.waitUntil(component::hasFocus);
    }

    @Override
    public void setFocusOwner(final Component component) {
        if (focusOwner() != component) {

            runInEDT(component::requestFocusInWindow);
            // we can ignore the result of requestFocusInWindow as the `false`
            // case is also handled by the following statement ("timeout")

            waitUntilInFocus(component);
        }
    }

    @Override
    public void focusNext() {
        moveFocus(Direction.NEXT);
    }

    @Override
    public void focusPrevious() {
        moveFocus(Direction.PREVIOUS);
    }

    private void moveFocus(Direction direction) {
        Component oldOwner = focusOwner();

        // Move the focus to the next/previous component by typing
        // "Tab" or "Shift Tab".
        boolean withShift = direction == Direction.PREVIOUS;

        if (withShift) basicKeyboardSupport.keyPress(KeyEvent.VK_SHIFT);
        basicKeyboardSupport.keyPress(KeyEvent.VK_TAB);
        if (withShift) basicKeyboardSupport.keyRelease(KeyEvent.VK_SHIFT);

        waitUntilFunction.waitUntil(() -> {
            Component newOwner = focusOwner();
            return newOwner != null && !newOwner.equals(oldOwner);
        });
    }

    @Override
    public Duration timeout() {
        return timeoutProvider.timeout();
    }

    private enum Direction {NEXT, PREVIOUS}

}

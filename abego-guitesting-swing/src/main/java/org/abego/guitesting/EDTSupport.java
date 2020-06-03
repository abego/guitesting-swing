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

package org.abego.guitesting;

import javax.swing.SwingUtilities;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Event Dispatch Thread Support
 */
public interface EDTSupport {
    /**
     * Runs the {@code runnable} in the Event Dispatch Thread (EDT) and
     * waits until the runnable is finished.
     * <p>
     * The method may also called from within the EventDispatchThread. In that
     * case the runnable runs immediately.
     *
     * @param runnable the {@link Runnable} to run in the Event Dispatch Thread.
     */
    void runInEDT(Runnable runnable);

    /**
     * Runs the {@code actionListener} with the given {@code event} in the
     * Event Dispatch Thread (EDT) (using action.actionPerformed) and
     * waits until the actionListener is finished.
     * <p>
     * The method may also called from within the EventDispatchThread. In that
     * case the actionListener runs immediately.
     *
     * @param actionListener the {@link ActionListener} to run in the Event
     *                       Dispatch Thread.
     * @param event          the {@link ActionEvent} passed to the actionListener
     */
    default void runInEDT(ActionListener actionListener, ActionEvent event) {
        runInEDT(() -> actionListener.actionPerformed(event));
    }

    /**
     * Runs the {@code actionListener} with an
     * {@link ActionEvent#ACTION_PERFORMED} event and
     * waits until the actionListener is finished.
     * <p>
     * The method may also called from within the EventDispatchThread. In that
     * case the actionListener runs immediately.
     *
     * @param actionListener the {@link ActionListener} to run in the Event
     *                       Dispatch Thread.
     */
    default void runInEDT(ActionListener actionListener) {
        runInEDT(actionListener,
                new ActionEvent(this, ActionEvent.ACTION_PERFORMED, ""));
    }

    /**
     * Returns {@code true} when the current thread is the Event Dispatch
     * Thread, {@code false} otherwise.
     *
     * @return {@code true} when the current thread is the Event Dispatch
     * Thread, {@code false} otherwise
     */
    default boolean isEDT() {
        return SwingUtilities.isEventDispatchThread();
    }
}

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
     * Run the <code>runnable</code> in the Event Dispatch Thread (EDT).
     * <p>
     * Process the event queue before running the runnable. Return when the
     * runnable is finished.
     * <p>
     * When called by the EventDispatchThread execute the runnable immediately.
     */
    void runInEDT(Runnable runnable);

    /**
     * Run the <code>actionListener</code> with the event (using
     * action.actionPerformed) in the Event Dispatch Thread (EDT).
     * <p>
     * Process the event queue before running the actionListener. Return when the
     * runnable is finished.
     * <p>
     * When called by the EventDispatchThread execute the runnable immediately.
     */
    default void runInEDT(ActionListener actionListener, ActionEvent event) {
        runInEDT(() -> actionListener.actionPerformed(event));
    }

    /**
     * See {@link #runInEDT(ActionListener, ActionEvent)}
     */
    default void runInEDT(ActionListener actionListener) {
        runInEDT(actionListener,
                new ActionEvent(this, ActionEvent.ACTION_PERFORMED, ""));
    }

    /**
     * Return <code>true</code> when the current thread is the Event Dispatch
     * Thread, <code>false</code> otherwise.
     */
    default boolean isEDT() {
        return SwingUtilities.isEventDispatchThread();
    }
}

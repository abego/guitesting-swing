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


import org.abego.commons.timeout.TimeoutSupplier;
import org.abego.commons.timeout.Timeoutable;
import org.eclipse.jdt.annotation.Nullable;

import java.time.Duration;
import java.util.function.BooleanSupplier;

/**
 * Provide various ways to pause the execution of the current thread.
 */
public interface Pause extends TimeoutSupplier, PauseUntilFunction {
    /**
     * Pause the execution while <code>condition</code> is true.
     */
    @Timeoutable
    default void whileTrue(BooleanSupplier condition) {
        until(() -> !condition.getAsBoolean());
    }

    /**
     * Pause the execution for <code>duration</code>.
     */
    void forDuration(Duration duration);

    /**
     * Give the user an option to signal he likes to "un-pause" and pause until
     * the user actually signals the "un-pause".
     *
     * @param message text to show to the user. When <code>null</code> show no
     *                text to the user [Default: <code>null</code>]
     */
    void interactively(@Nullable String message);

    /**
     * Give the user an option to signal he likes to "un-pause" and pause until
     * the user actually signals the "un-pause".
     */
    default void interactively() {
        interactively(null);
    }
}

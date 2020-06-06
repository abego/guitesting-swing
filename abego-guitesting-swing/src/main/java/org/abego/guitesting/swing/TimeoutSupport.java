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

package org.abego.guitesting.swing;

import org.abego.commons.timeout.TimeoutSupplier;

import java.time.Duration;

public interface TimeoutSupport extends TimeoutSupplier {

    /**
     * Sets the {@code timeout} property, as returned by {@link #timeout()}, to
     * the given {@code duration}.
     *
     * @param duration the timeout duration
     */
    void setTimeout(Duration duration);

    /**
     * Sets the {@code timeout} property, as returned by {@link #timeout()}, to
     * the given {@code durationInMillis}.
     *
     * @param durationInMillis the duration of the timeout, in milli seconds
     */
    default void setTimeoutMillis(long durationInMillis) {
        setTimeout(Duration.ofMillis(durationInMillis));
    }

    /**
     * Returns the duration of the initial timeout, i.e. the value
     * {@link #timeout()} returns initially or after a "reset".
     *
     * <p>See {@link #timeout()}</p>
     */
    Duration initialTimeout();

    /**
     * Sets the {@code initialTimeout} property, as returned by
     * {@link #initialTimeout()}.
     *
     * @param duration the new duration
     */
    void setInitialTimeout(Duration duration);

    /**
     * Runs the {@code runnable}, with the timeout temporarily set
     * to the {@code timeoutDuration}.
     *
     * @param timeoutDuration the duration of the timeout
     * @param runnable        the runnable to run
     */
    void runWithTimeout(Duration timeoutDuration, Runnable runnable);

}

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
 * Provide various ways to wait, i.e. pause the execution of the current thread.
 */
public interface WaitSupport extends TimeoutSupplier, WaitUntilFunction {
    /**
     * Wait while {@code condition} is true.
     */
    @Timeoutable
    default void waitWhile(BooleanSupplier condition) {
        waitUntil(() -> !condition.getAsBoolean());
    }

    /**
     * Wait for the given {@code duration}.
     */
    void waitFor(Duration duration);

    /**
     * Wait for {@code n} milli seconds.
     */
    default void waitForMillis(long n) {
        waitFor(Duration.ofMillis(n));
    }

    /**
     * Give the user a way to signal he likes to "continue" and wait until
     * the user actually signals "continue".
     *
     * @param message text to show to the user. When {@code null} show no
     *                text to the user [Default: {@code null}]
     */
    void waitForUser(@Nullable String message);

    /**
     * Give the user a way to signal he likes to "continue" and wait until
     * the user actually signals "continue".
     */
    default void waitForUser() {
        waitForUser(null);
    }

    /**
     * Alias for {@link #waitForUser()}.
     */
    default void pause() {
        waitForUser();
    }
}

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
import org.abego.commons.timeout.Timeoutable;
import org.eclipse.jdt.annotation.Nullable;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import static java.lang.Boolean.TRUE;
import static org.abego.commons.lang.ThrowableUtil.messageOrClassName;

/**
 * Assert methods with retries and timeout, for testing multi-threading code
 * like in Swing applications.
 * <p>
 * When testing multi-threading code "assert..." methods in the test methods
 * may need to check states that are affected by other, parallel threads.
 * <p>
 * For example in Swing most methods are not thread safe and therefore need
 * to run in the Event Dispatch Thread (EDT). Because of that a test method
 * typically will make its "action" part run in the EDT, e.g. using the
 * {@link javax.swing.SwingUtilities#invokeLater(Runnable)} method, and will do
 * the checks/assertions in the "main" (/test) thread. As both threads run
 * independently we may run into an assertion before the code in the parallel
 * EDT had a chance to change the state to the expected value.
 * <p>
 * An assertion methods implemented according to the following approach avoids
 * this multi-threading issue:
 * <ol>
 *     <li>Start a timer for a timeout.</li>
 *     <li>If the actual state is equal to the expected state return from
 *     this assertion method.</li>
 *     <li>Otherwise if the assertion method is running longer than defined by
 *     the timeout throw an {@link AssertionError}.
 *     </li>
 *     <li>Otherwise wait for a while and then "retry" the check again by
 *     continuing with step 2.
 *     </li>
 * </ol>
 * <p>
 * The "assert...Retrying" methods (like
 * {@link #assertEqualsRetrying(Object, Supplier)} or
 * {@link #assertTrueRetrying(BooleanSupplier)}) are implemented this way.
 */
public interface AssertRetryingSupport extends TimeoutSupplier {
    @Timeoutable
    <T> void assertEqualsRetrying(T expected,
                                  Supplier<T> actualSupplier,
                                  @Nullable String message);

    @Timeoutable
    default <T> void assertEqualsRetrying(T expected, Supplier<T> actualSupplier) {
        assertEqualsRetrying(expected, actualSupplier, null);
    }

    @Timeoutable
    default void assertTrueRetrying(BooleanSupplier actualSupplier,
                                    @Nullable String message) {
        assertEqualsRetrying(TRUE, actualSupplier::getAsBoolean, message);
    }

    @Timeoutable
    default void assertTrueRetrying(BooleanSupplier actualSupplier) {
        assertTrueRetrying(actualSupplier, null);
    }

    @Timeoutable
    default void assertSuccessRetrying(Runnable runnable) {
        String expectedValue = "Success"; //NON-NLS
        assertEqualsRetrying(expectedValue, () -> {
            try {
                runnable.run();
                return expectedValue; // running without error/failure is a success
            } catch (Exception e) {
                return messageOrClassName(e); // A failure/error is no success
            }
        });
    }


}

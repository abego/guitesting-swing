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

import org.abego.commons.timeout.TimeoutSupplier;
import org.abego.commons.timeout.TimeoutUncheckedException;
import org.abego.guitesting.swing.AssertRetryingSupport;
import org.eclipse.jdt.annotation.Nullable;

import java.time.Duration;
import java.util.Objects;
import java.util.function.Supplier;

import static org.abego.commons.lang.StringUtil.joinWithEmptyStringForNull;
import static org.abego.commons.polling.PollingUtil.poll;
import static org.junit.jupiter.api.Assertions.assertEquals;

final class AssertRetryingSupportImpl implements AssertRetryingSupport {
    private static final String ASSERT_EQUALS_RETRYING_TIMEOUT_MESSAGE_PREFIX = "[Timeout]"; //NON-NLS
    private final TimeoutSupplier timeoutProvider;

    private AssertRetryingSupportImpl(TimeoutSupplier timeoutProvider) {
        this.timeoutProvider = timeoutProvider;
    }

    static AssertRetryingSupport newAssertRetryingSupport(TimeoutSupplier timeoutProvider) {
        return new AssertRetryingSupportImpl(timeoutProvider);
    }


    @Override
    public <T> void assertEqualsRetrying(T expected,
                                         Supplier<T> actualSupplier,
                                         @Nullable String message) {
        T actual;
        @Nullable String finalMessage = message;

        try {
            actual = poll(actualSupplier, a -> Objects.equals(expected, a), timeout());
        } catch (TimeoutUncheckedException e) {
            actual = actualSupplier.get();
            finalMessage = joinWithEmptyStringForNull(" ", ASSERT_EQUALS_RETRYING_TIMEOUT_MESSAGE_PREFIX, message);
        }

        assertEquals(expected, actual, finalMessage);
    }

    @Override
    public Duration timeout() {
        return timeoutProvider.timeout();
    }
}

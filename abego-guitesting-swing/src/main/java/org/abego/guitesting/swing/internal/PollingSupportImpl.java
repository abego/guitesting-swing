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

import org.abego.commons.polling.PollingUtil;
import org.abego.commons.timeout.TimeoutSupplier;
import org.abego.commons.timeout.TimeoutUncheckedException;
import org.abego.guitesting.swing.PollingSupport;

import java.time.Duration;
import java.util.function.Predicate;
import java.util.function.Supplier;

final class PollingSupportImpl implements PollingSupport {

    private final TimeoutSupplier timeoutProvider;

    private PollingSupportImpl(TimeoutSupplier timeoutProvider) {
        this.timeoutProvider = timeoutProvider;
    }

    static PollingSupport newPollingSupport(TimeoutSupplier timeoutProvider) {
        return new PollingSupportImpl(timeoutProvider);
    }

    @Override
    public <T> T poll(
            Supplier<T> functionToPoll, Predicate<T> isResult, Duration timeout) {

        return PollingUtil.poll(functionToPoll, isResult, timeout);
    }

    @Override
    public <T> T pollNoFail(
            Supplier<T> functionToPoll, Predicate<T> isResult, Duration timeout) {

        try {
            return PollingUtil.poll(functionToPoll, isResult, timeout);
        } catch (TimeoutUncheckedException e) {
            return functionToPoll.get();
        }
    }

    @Override
    public Duration timeout() {
        return timeoutProvider.timeout();
    }
}

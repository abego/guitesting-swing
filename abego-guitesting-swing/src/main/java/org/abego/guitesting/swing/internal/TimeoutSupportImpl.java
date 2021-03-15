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


import org.abego.guitesting.swing.TimeoutSupport;

import java.time.Duration;

import static java.time.Duration.ofSeconds;

final class TimeoutSupportImpl implements TimeoutSupport {

    private static final Duration INITIAL_TIMEOUT_DEFAULT = ofSeconds(10);
    private Duration initialTimeout = INITIAL_TIMEOUT_DEFAULT;
    private Duration timeout = initialTimeout;

    private TimeoutSupportImpl() {
    }

    static TimeoutSupport newTimeoutSupport() {
        return new TimeoutSupportImpl();
    }

    @Override
    public Duration timeout() {
        return timeout;
    }

    @Override
    public Duration initialTimeout() {
        return initialTimeout;
    }

    @Override
    public void setInitialTimeout(Duration duration) {
        initialTimeout = duration;
    }

    @Override
    public void setTimeout(Duration duration) {
        timeout = duration;
    }

    @Override
    public void runWithTimeout(Duration timeout, Runnable runnable) {

        Duration oldTimeout = timeout();
        try {
            setTimeout(timeout);
            runnable.run();
        } finally {
            setTimeout(oldTimeout);
        }
    }
}

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

import org.abego.commons.polling.PollingUtil;
import org.abego.commons.timeout.TimeoutSupplier;
import org.abego.guitesting.Pause;

import javax.annotation.Nullable;
import java.time.Duration;
import java.util.function.BooleanSupplier;

import static org.abego.commons.lang.ThreadUtil.sleep;
import static org.abego.guitesting.internal.PauseUI.pauseUI;

/**
 * Provide a way to pause execution, e.g. until a certain condition is matched,
 * some time passed etc.
 *
 * <p>Some methods may throw an Exception when the operation times out.
 * The duration of the timeout is defined by {@link #timeout()}</p>
 */
final class PauseImpl implements Pause {

    private final TimeoutSupplier timeoutProvider;

    private PauseImpl(TimeoutSupplier timeoutProvider) {
        this.timeoutProvider = timeoutProvider;
    }

    /**
     * Return a Pause object with a timeout defined by the given
     * <code>timeoutSupplier</code>.
     */
    static Pause newPause(TimeoutSupplier timeoutProvider) {
        return new PauseImpl(timeoutProvider);
    }

    @Override
    public Duration timeout() {
        return timeoutProvider.timeout();
    }

    @Override
    public void until(BooleanSupplier condition) {
        PollingUtil.poll(condition::getAsBoolean, b -> b, timeout());
    }

    @Override
    public void forDuration(Duration duration) {
        sleep(duration.toMillis());
    }

    @Override
    public void interactively(@Nullable String message) {
        pauseUI().showPauseWindowAndWaitForUser(message);
    }

}

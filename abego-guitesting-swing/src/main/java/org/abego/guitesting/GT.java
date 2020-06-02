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

import static org.abego.commons.lang.ThrowableUtil.messageOrClassName;

import java.awt.Component;
import java.awt.Window;
import java.util.Objects;
import java.util.function.Predicate;

import org.abego.commons.blackboard.Blackboard;
import org.abego.commons.seq.Seq;
import org.abego.commons.timeout.Timeoutable;
import org.junit.jupiter.api.Assertions;

public interface GT extends
        AssertRetryingSupport,
        ComponentSupport,
        DebugSupport,
        DialogAndFrameSupport,
        EDTSupport,
        FocusSupport,
        KeyboardSupport,
        MouseSupport,
        PollingSupport,
        RobotAPI,
        TimeoutSupport,
        WaitForIdleSupport,
        WaitSupport,
        WindowSupport {

    /**
     * see {@link #readSystemProperties()}.
     */
    static final String SYSTEM_PROPERTY_TIMEOUT_MILLIS = "GT.timeoutMillis"; //NON-NLS

    // ======================================================================
    // Blackboard
    // ======================================================================

    Blackboard<Object> blackboard();

    // ======================================================================
    // "waitFor..." Window Support
    // ======================================================================

    @Override
    @Timeoutable
    default <T extends Window> T waitForWindowWith(Class<T> windowClass, Predicate<T> condition) {
        Seq<T> windows = poll(() -> allWindowsWith(windowClass, condition), w -> !w.isEmpty());
        return windows.singleItem();
    }

    @Override
    @Timeoutable
    default Window waitForWindowWith(Predicate<Window> condition) {
        return waitForWindowWith(Window.class, condition);
    }

    @Override
    @Timeoutable
    default <T extends Window> T waitForWindowNamed(Class<T> windowClass, String name) {
        return waitForWindowWith(windowClass, w -> Objects.equals(w.getName(), name));
    }

    @Override
    @Timeoutable
    default Window waitForWindowNamed(String name) {
        return waitForWindowNamed(Window.class, name);
    }

    // ======================================================================
    // "waitFor..." Component Support
    // ======================================================================

    @Override
    @Timeoutable
    default <T extends Component> T waitForComponentWith(Class<T> componentClass,
                                                         Predicate<T> condition) {
        Seq<T> seq = poll(() -> allComponentsWith(componentClass, condition), a -> !a.isEmpty());
        return seq.singleItem();
    }

    @Override
    @Timeoutable
    default <T extends Component> T waitForComponentNamed(Class<T> componentClass, String name) {
        try {
            return waitForComponentWith(componentClass, c -> Objects.equals(c.getName(), name));
        } catch (Exception e) {
            Assertions.fail(String.format(
                    "Error when looking for %s named '%s': %s", //NON-NLS
                    componentClass.getName(),
                    name,
                    messageOrClassName(e)),
                    e);

            // never reached.
            // `fail` will not return, but the compiler does not know that.
            // (We don't throw an 'AssertionException' as we would then
            // introduce a dependency to the 'hidden' exception class)
            throw new IllegalStateException();
        }
    }

    // ======================================================================
    // Reset
    // ======================================================================

    /**
     * Reset this GT instance.
     * <p>
     * I.e.:
     * <ul>
     * <li>Reset timeout to the {@link #initialTimeout()}.</li>
     * <li>Clear the blackboard (see {@link Blackboard#clear()}).</li>
     * <li>"Release" all keys (in case a key was pressed and
     * not yet released).</li>
     * </ul>
     */
    void reset();

    /**
     * Reset this GT instance and dispose all windows.
     */
    void cleanup();

    /**
     * Reads the system properties and applies them to this GT.
     *
     * <p>The following properties are supported:
     * <ul>
     *     <li>"GT.timeoutMillis": time duration in milliseconds, used for
     *     initialTimeout and timeout.</li>
     * </ul>
     * </p>
     */
    void readSystemProperties();

    // ======================================================================
    // Feature group access
    // ======================================================================

    default AssertRetryingSupport _assert() {
        return this;
    }

    default ComponentBaseSupport _component() {
        return this;
    }

    default DebugSupport _debug() {
        return this;
    }

    default DialogAndFrameSupport _dialog() {
        return this;
    }

    default EDTSupport _edt() {
        return this;
    }

    default FocusSupport _focus() {
        return this;
    }

    default DialogAndFrameSupport _frame() {
        return this;
    }

    default KeyboardSupport _keyboard() {
        return this;
    }

    default MouseSupport _mouse() {
        return this;
    }

    default PollingSupport _poll() {
        return this;
    }

    default RobotAPI _robotAPI() {
        return this;
    }

    default TimeoutSupport _timeout() {
        return this;
    }

    default WaitForIdleSupport _idle() {
        return this;
    }

    default WaitSupport _wait() {
        return this;
    }

    default WindowSupport _window() {
        return this;
    }
}

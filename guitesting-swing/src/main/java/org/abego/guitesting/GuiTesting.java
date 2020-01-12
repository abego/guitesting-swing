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

import org.abego.commons.blackboard.Blackboard;
import org.abego.commons.seq.Seq;
import org.abego.commons.timeout.Timeoutable;
import org.abego.guitesting.internal.GuiTestingImpl;

import java.awt.Component;
import java.awt.Window;
import java.io.PrintStream;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

public interface GuiTesting extends
        AssertRetryingSupport,
        ComponentSupport,
        DialogAndFrameSupport,
        EDTSupport,
        FocusSupport,
        KeyboardSupport,
        MouseSupport,
        PollingSupport,
        RobotAPI,
        TimeoutSupport,
        WaitForIdleSupport,
        WindowSupport {

    static GuiTesting newGuiTesting() {
        return GuiTestingImpl.newGuiTesting();
    }

    // ======================================================================
    // More Window Support
    // ======================================================================

    @Timeoutable
    default <T extends Window> T waitForWindowWith(Class<T> windowClass, Predicate<T> condition) {
        Seq<T> windows = poll(() -> allWindowsWith(windowClass, condition), w -> !w.isEmpty());
        return windows.singleItem();
    }

    @Timeoutable
    default Window waitForWindowWith(Predicate<Window> condition) {
        return waitForWindowWith(Window.class, condition);
    }

    @Timeoutable
    default <T extends Window> T waitForWindowNamed(Class<T> windowClass, String name) {
        return waitForWindowWith(windowClass, w -> Objects.equals(w.getName(), name));
    }

    @Timeoutable
    default Window waitForWindowNamed(String name) {
        return waitForWindowNamed(Window.class, name);
    }

    // ======================================================================
    // Blackboard
    // ======================================================================

    Blackboard<Object> blackboard();


    // ======================================================================
    // Pausing
    // ======================================================================

    /**
     * Return a {@link Pause} object with <code>timeout()</code> as timeout.
     *
     * <p>The actual "pausing" occurs when calling methods like
     * {@link Pause#until(BooleanSupplier)} etc. See {@link Pause} for details.</p>
     */
    Pause pause();

    // ======================================================================
    // Reset
    // ======================================================================

    /**
     * Reset this GuiTesting instance.
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
     * Reset this GuiTesting instance and dispose all windows.
     */
    void cleanup();

    // ======================================================================
    // More Component Support
    // ======================================================================

    @Timeoutable
    default <T extends Component> T waitForComponentWith(Class<T> componentClass,
                                                         Predicate<T> condition) {
        Seq<T> seq = poll(() -> allComponentsWith(componentClass, condition), a -> !a.isEmpty());
        return seq.singleItem();
    }

    @Timeoutable
    default <T extends Component> T waitForComponentNamed(Class<T> componentClass, String name) {
        return waitForComponentWith(componentClass, c -> Objects.equals(c.getName(), name));
    }

    // ======================================================================
    // Debug Support
    // ======================================================================

    /**
     * Dump all components to {@code out}, one per line.
     *
     * <p>The actual format of the output is not fixed, but it will typically include the
     * classname of the component, its name (if defined) and its title/label/text (if defined).</p>
     *
     * <p>Beside the components also the window containing the components is dumped.</p>
     */
    void dumpAllComponents(PrintStream out);

    /**
     * Dump all components to {@link System#out}, one per line.
     *
     * <p>(For details see {@link GuiTesting#dumpAllComponents(PrintStream)})</p>
     */
    void dumpAllComponents();
}

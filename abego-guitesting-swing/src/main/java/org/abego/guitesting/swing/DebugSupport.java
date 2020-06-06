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

import java.io.PrintStream;

/**
 * Features useful when debugging a failing GUI test, like dumping all available
 * {@link java.awt.Component}s to evaluate why a component-related assertion
 * failed.
 */
public interface DebugSupport {

    /**
     * Dump all components to {@code out}, one per line.
     *
     * <p>The actual format of the output is not fixed, but it will typically include the
     * classname of the component, its name (if defined) and its title/label/text (if defined).</p>
     *
     * <p>Beside the components also the window containing the components is dumped.</p>
     *
     * @param out the {@link PrintStream} to dump to.
     */
    void dumpAllComponents(PrintStream out);

    /**
     * Dump all components to {@link System#out}, one per line.
     *
     * <p>(For details see {@link DebugSupport#dumpAllComponents(PrintStream)})</p>
     */
    void dumpAllComponents();
}

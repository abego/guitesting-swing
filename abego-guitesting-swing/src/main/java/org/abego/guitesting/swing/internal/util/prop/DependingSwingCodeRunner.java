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

package org.abego.guitesting.swing.internal.util.prop;

public interface DependingSwingCodeRunner {
    /**
     * Run the {@code code} now and whenever the object changes its value.
     * <p>
     * The {@code code} may operate on Swing components. Therefore, all but the
     * initial run are executed in the Event Dispatch Thread. The initial run
     * is performed immediately, in the current thread. To be thread-safe
     * make sure to run #runDependingSwingCode from the Event Dispatch Thread
     * (e.g. via {@link javax.swing.SwingUtilities#invokeLater(Runnable)} or,
     *  before the Swing components the {@code code} operates on are realized
     * (http://www.javapractices.com/topic/TopicAction.do?Id=153).
     * <p>
     * Typically this method is used to update Swing components that depend
     * on the value of this object. Then first run of the {@code code}, directly
     * performed with the invocation of #runDependingSwingCode, initializes the
     * related aspects of the Swing components (e.g. their "text"). After that
     * the {@code code} is re-run when the object's value changed.<p>
     * <p>
     * After a change the {@code code} is executed, but there may be some delay
     * between the change and the execution of the {@code code}. Also multiple
     * changes may result in just one code execution, covering all changes that
     * occured since the last code run.
     */
    void runDependingSwingCode(Runnable code);
}

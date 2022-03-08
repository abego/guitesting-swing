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

import org.eclipse.jdt.annotation.NonNull;

import java.awt.AWTException;
import java.awt.GraphicsEnvironment;
import java.awt.Robot;

import static org.abego.commons.lang.exception.UncheckedException.newUncheckedException;
import static org.abego.guitesting.swing.internal.GTImpl.newGTImpl;
import static org.abego.guitesting.swing.internal.GTNoRobotImpl.newGTNoRobot;

/**
 * The entry to the GuiTesting Swing library, containing the factory method for
 * {@link GT} instances, {@link #newGT()}.
 */
public class GuiTesting {
    private static final String COULD_NOT_CREATE_ROBOT_INSTANCE_MESSAGE = "Could not create Robot instance"; //NON-NLS

    /**
     * Returns a new instance of {@link GT}, the main interface for GUI testing.
     *<p>
     * When running in a headless environment
     * (see {@link GraphicsEnvironment#isHeadless()}) methods of the returned GT
     * will fail with a {@link HeadlessGuiTestingException} when the operation
     * dependents on the existance of a display, keyboard, or mouse.
     *
     * @return a new instance of {@link GT}
     */
    public static GT newGT() {
        if (GraphicsEnvironment.isHeadless()) {
            return newGTNoRobot();
        } else {
            return newGTImpl(newRobot());
        }
    }

    @NonNull
    private static Robot newRobot() {
        try {
            return new Robot();
        } catch (AWTException e) {
            throw newUncheckedException(COULD_NOT_CREATE_ROBOT_INSTANCE_MESSAGE, e);
        }
    }
}

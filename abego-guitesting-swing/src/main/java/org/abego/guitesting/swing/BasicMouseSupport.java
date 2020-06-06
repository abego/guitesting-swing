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

public interface BasicMouseSupport {
    /**
     * Move mouse pointer to {@code (x,y)} (in screen coordinates).
     *
     * @param x the horizontal position of the mouse pointer, in screen coordinates
     * @param y the vertical position of the mouse pointer, in screen coordinates
     */
    void mouseMove(int x, int y);

    /**
     * Press the mouse buttons given by the {@code buttonsMask}.
     *
     * @param buttonsMask defines the mouse buttons as a
     *                    bitwise combination of {@code InputEvent.BUTTON1_DOWN_MASK},
     *                    {@code InputEvent.BUTTON2_DOWN_MASK}, or {@code InputEvent.BUTTON3_DOWN_MASK}
     **/
    void mousePress(int buttonsMask);

    /**
     * Release the mouse buttons given by the {@code buttonsMask}.
     *
     * @param buttonsMask defines the mouse buttons as a
     *                    bitwise combination of {@code InputEvent.BUTTON1_DOWN_MASK},
     *                    {@code InputEvent.BUTTON2_DOWN_MASK}, or {@code InputEvent.BUTTON3_DOWN_MASK}
     */
    void mouseRelease(int buttonsMask);

    /**
     * Rotate the scroll wheel of the mouse.
     *
     * @param notchCount number of "notches" to move the mouse wheel,
     *                   negative indicating "down"/"towards the user" movement.
     */
    void mouseWheel(int notchCount);

}

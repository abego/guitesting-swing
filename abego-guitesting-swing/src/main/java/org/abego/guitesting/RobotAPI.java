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

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

public interface RobotAPI extends
        BasicMouseSupport,
        BasicKeyboardSupport,
        WaitForIdleSupport {

    /**
     * Return the color of the pixel at {@code (x,y)} (in screen coordinates).
     */
    Color getPixelColor(int x, int y);

    /**
     * Return an image of the rectangle of the screen (in screen coordinates).
     * <p>
     * Don't include the mouse cursor in the image.
     */
    BufferedImage createScreenCapture(Rectangle rectangle);

    /**
     * Sleep for {@code milliseconds} milli seconds.
     */
    void delay(int milliseconds);

    /**
     * Return {@code true} when {@code waitForIdle} is automatically
     * called after this Robot generated an event, {@code false} otherwise.
     */
    boolean isAutoWaitForIdle();

    /**
     * See {@link #isAutoWaitForIdle()}.
     */
    void setAutoWaitForIdle(boolean isOn);

    /**
     * Return the number of milliseconds this Robot sleeps after generating an event.
     */
    int getAutoDelay();

    /**
     * See {@link #getAutoDelay()}.
     */
    void setAutoDelay(int ms);

}

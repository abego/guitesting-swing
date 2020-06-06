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

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

/**
 * Operations similar to the ones from {@link java.awt.Robot}.
 */
public interface RobotAPI extends
        BasicMouseSupport,
        BasicKeyboardSupport,
        WaitForIdleSupport {

    /**
     * Returns the color of the pixel at {@code (x,y)}.
     *
     * @param x the x-coordinate of the pixel (in screen coordinates)
     * @param y the y-coordinate of the pixel (in screen coordinates)
     * @return the color of the pixel at {@code (x,y)}
     */
    Color getPixelColor(int x, int y);

    /**
     * Returns an image/screenshot of the rectangle of the screen.
     * <p>
     * Doesn't include the mouse cursor in the image.
     *
     * @param rectangle a {@link Rectangle} on the screen (in screen coordinates)
     * @return an image of the rectangle of the screen.
     */
    BufferedImage createScreenCapture(Rectangle rectangle);

    /**
     * Sleeps (the current thread) for {@code milliseconds} milli seconds.
     *
     * @param milliseconds the time in milli seconds
     */
    void delay(int milliseconds);

    /**
     * Returns {@code true} when {@code waitForIdle} is automatically
     * called after this Robot generated an event, {@code false} otherwise.
     *
     * @return {@code true} when {@code waitForIdle} is automatically
     * called after this Robot generated an event, {@code false} otherwise.
     */
    boolean isAutoWaitForIdle();

    /**
     * Sets the {@code autoWaitForIdle} property to the given {@code value}.
     * <p>
     * See {@link #isAutoWaitForIdle()}.
     *
     * @param value the new value of the property
     */
    void setAutoWaitForIdle(boolean value);

    /**
     * Returns the number of milliseconds this Robot sleeps after generating an event.
     *
     * @return the number of milliseconds this Robot sleeps after generating an event.
     */
    int getAutoDelay();

    /**
     * Sets the {@code autoDelay} property to the given {@code milliseconds}.
     * <p>
     * See {@link #getAutoDelay()}.
     *
     * @param milliseconds the new value of the property
     */
    void setAutoDelay(int milliseconds);
}

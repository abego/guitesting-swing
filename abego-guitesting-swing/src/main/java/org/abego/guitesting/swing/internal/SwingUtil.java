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

import org.abego.commons.lang.exception.MustNotInstantiateException;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Component;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;

import static java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment;
import static org.abego.commons.lang.ClassUtil.resource;

public final class SwingUtil {

    SwingUtil() {
        throw new MustNotInstantiateException();
    }

    /**
     * Return the bounds of the screen.
     * <p>
     * (On mac OS the returned rectangle does not contain the menubar)
     */
    static Rectangle screenBounds() {
        GraphicsEnvironment env = getLocalGraphicsEnvironment();
        return env.getMaximumWindowBounds();
    }

    @SuppressWarnings("SameParameterValue")
    static Icon iconFromResource(Class<?> theClass, String name) {
        return new ImageIcon(resource(theClass, name));
    }

    public static boolean isGreenish(Color color) {
        int threshold = color.getGreen() / 2;
        return color.getRed() < threshold && color.getBlue() < threshold;
    }

    public static boolean isBlueish(Color color) {
        int threshold = color.getBlue() / 2;
        return color.getRed() < threshold && color.getGreen() < threshold;
    }

    public static boolean isRedish(Color color) {
        int threshold = color.getRed() / 2;
        return color.getGreen() < threshold && color.getBlue() < threshold;
    }


    static boolean isMacOS() {
        //noinspection StringToUpperCaseOrToLowerCaseWithoutLocale
        return System.getProperty("os.name").toLowerCase().contains("mac"); //NON-NLS NON-NLS
    }

    /**
     * Converts a point (x,y) given in coordinates relative to the component
     * into "global" screen coordinates.
     *
     * @param x if < 0 offset is taken from the right of the component
     * @param y if < 0 offset is taken from the bottom of the component
     */
    public static Point toScreenCoordinates(
            Component component,
            int x,
            int y) {

        if (x < 0) {
            x = component.getWidth() + x;
        }
        if (y < 0) {
            y = component.getHeight() + y;
        }
        Point p = new Point(x, y);

        SwingUtilities.convertPointToScreen(p, component);

        return p;
    }

    //TODO move to ComponentUtil
    public static Rectangle toScreenCoordinates(Component component, Rectangle rectangle) {
        Rectangle result = rectangle.getBounds();
        Point p = result.getLocation();
        Point locationOnScreen = toScreenCoordinates(component, p.x, p.y);
        result.translate(locationOnScreen.x, locationOnScreen.y);
        return result;
    }

}

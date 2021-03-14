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
import org.abego.guitesting.swing.GuiTestingException;
import org.eclipse.jdt.annotation.Nullable;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.File;
import java.net.URL;
import java.util.function.Predicate;

import static java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment;
import static org.abego.commons.lang.ClassUtil.resource;

public final class GuiTestingUtil {

    GuiTestingUtil() {
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
        return System.getProperty("os.name").toLowerCase().contains("mac"); //NON-NLS NON-NLS
    }

    /**
     * Converts a point (x,y) given in coordinates relative to the component
     * into "global" screen coordinates.
     *
     * @param component the component used as a reference
     * @param x         if &lt; 0 offset is taken from the right of the component
     * @param y         if &lt; 0 offset is taken from the bottom of the component
     * @return the point in screen coordinates
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

    /**
     * Converts a rectangle given in coordinates relative to the component
     * into "global" screen coordinates.
     *
     * @param component the component used as a reference
     * @param rectangle the {@link Rectangle} to convert
     * @return the rectangle in screen coordinates
     */
    public static Rectangle toScreenCoordinates(Component component, Rectangle rectangle) {
        Rectangle result = rectangle.getBounds();
        Point p = result.getLocation();
        Point locationOnScreen = toScreenCoordinates(component, p.x, p.y);
        result.translate(locationOnScreen.x, locationOnScreen.y);
        return result;
    }

    /**
     * Returns the StackTraceElement of the first caller of a callee
     * that is not itself a callee (in the given stackTrace).
     *
     * @param stackTrace the stacktrace to consider for the call chain.
     * @param isCallee   returns true when the StackTraceElement passed in is a
     *                   `callee`, false otherwise.
     * @return the StackTraceElement of the first caller of a callee
     * that is not itself a callee
     */
    @Nullable
    public static StackTraceElement findCaller(
            StackTraceElement[] stackTrace,
            Predicate<StackTraceElement> isCallee) {
        boolean foundCaller = false;
        for (StackTraceElement element : stackTrace) {
            if (isCallee.test(element)) {
                foundCaller = true;
            } else {
                // The first method after a callee method that is not itself
                // a callee method is the caller we are looking for
                if (foundCaller) {
                    return element;
                }
            }
        }
        return null;
    }

    /**
     * Returns the StackTraceElement of the first caller of a callee
     * that is not itself a callee (in the current thread's stacktrace).
     *
     * @param isCallee returns true when the StackTraceElement passed in is a
     *                 `callee`, false otherwise.
     * @return the StackTraceElement of the first caller of a callee
     * that is not itself a callee
     */
    @Nullable
    public static StackTraceElement findCaller(
            Predicate<StackTraceElement> isCallee) {
        return findCaller(Thread.currentThread().getStackTrace(), isCallee);
    }

    /**
     * Returns the StackTraceElement of the first caller of a method with
     * the given calleeName (in the current thread's stacktrace).
     *
     * @param calleeName the name of the method being called.
     * @return the StackTraceElement of the caller
     */
    public static StackTraceElement getCaller(String calleeName) {
        @Nullable StackTraceElement caller =
                findCaller(e -> e.getMethodName().equals(calleeName));
        if (caller == null) {
            throw new IllegalStateException(String.format("Cannot find method calling %s", calleeName)); //NON-NLS
        }
        return caller;
    }

    /**
     * Returns the {@link Class} defining the method identified by the
     * stackTraceElement.
     *
     * @param stackTraceElement the {@link StackTraceElement} to consider
     * @return the {@link Class} defining the method identified by the
     * stackTraceElement
     */
    public static Class<?> getClass(StackTraceElement stackTraceElement) {
        try {
            return Class.forName(stackTraceElement.getClassName());
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid class in stackTraceElement", e); //NON-NLS
        }
    }

    /**
     * Returns the {@link File} for the given urlString.
     *
     * @param urlString a String representing an {@link URL}
     * @return the {@link File} for the given urlString
     */
    public static File urlToFile(String urlString) {
        try {
            return new File(new URL(urlString).toURI());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Throws a {@link GuiTestingException} when the file is not a "png" file name.
     *
     * @param file the {@link File} to check
     */
    public static void checkIsPngFilename(File file) {
        //noinspection StringToUpperCaseOrToLowerCaseWithoutLocale
        if (!file.getName().toLowerCase().endsWith(".png")) {
            throw new GuiTestingException(String.format("Only 'png' files supported. Got %s", file)); //NON-NLS
        }
    }

    /**
     * Returns the fully qualified name of the method identified
     * by the stackTraceElement.
     *
     * @param stackTraceElement the {@link StackTraceElement} to check.
     * @return the fully qualified name of the method identified
     * by the stackTraceElement
     */
    public static String getFullMethodName(StackTraceElement stackTraceElement) {
        return getClass(stackTraceElement).getName() + "." + stackTraceElement.getMethodName();
    }

    /**
     * Returns the size of the image.
     *
     * @param image a (completely loaded) Image
     * @return the size of the image
     */
    static Dimension getSize(Image image) {
        Dimension size = new Dimension(
                image.getWidth(null),
                image.getHeight(null));
        if (size.getWidth() < 0 || size.getHeight() < 0) {
            throw new IllegalArgumentException("Image is not loaded completely."); //NON-NLS
        }
        return size;
    }
}

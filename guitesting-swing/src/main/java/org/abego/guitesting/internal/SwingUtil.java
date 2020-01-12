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

package org.abego.guitesting.internal;

import org.abego.commons.lang.exception.MustNotInstantiateException;
import org.eclipse.jdt.annotation.Nullable;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.lang.reflect.InvocationTargetException;

import static java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment;
import static javax.swing.SwingUtilities.invokeAndWait;
import static javax.swing.SwingUtilities.isEventDispatchThread;
import static org.abego.commons.lang.ClassUtil.resource;
import static org.abego.commons.lang.exception.UncheckedException.newUncheckedException;

public final class SwingUtil {

    SwingUtil() {
        throw new MustNotInstantiateException();
    }

    /**
     * @param title    when null the frame will have not title bar. As this also
     *                 removes the "close window" button in most platforms you need
     *                 to make sure there is an alternative to close the frame.
     * @param position top left position of the frame. when null the frame is "centered"
     * @param size     the size of the frame, when null the frame is "packed"
     */
    static JFrame showInFrame(
            @Nullable String title, @Nullable Component component, @Nullable Point position, @Nullable Dimension size) {
        JFrame frame = new JFrame();
        runInEDT(runnableForShowFrame(frame, title, component, position, size));
        return frame;

    }

    static void showInDialog(@Nullable String title, Component component) {
        final JDialog dlg = new JDialog();
        dlg.setModal(true);

        if (title != null)
            dlg.setTitle(title);

        dlg.getContentPane().add(component);
        dlg.pack();

        registerCloseOnEscapeAction(dlg);

        centerOnScreen(dlg);

        dlg.setVisible(true);
        dlg.dispose();
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

    private static Runnable runnableForShowFrame(
            JFrame frame,
            @Nullable String title,
            @Nullable Component component,
            @Nullable Point position,
            @Nullable Dimension size) {

        return () -> {
            if (title == null) {
                frame.setUndecorated(true);
            } else {
                frame.setTitle(title);
            }

            if (component != null) {
                frame.getContentPane().add(component);
            }

            if (size != null) {
                frame.setSize(size);
            } else {
                frame.pack();
            }

            if (position != null) {
                frame.setLocation(position);
            } else {
                centerOnScreen(frame);
            }

            frame.setVisible(true);
        };
    }

    /**
     * Center the <code>window</code> on the screen.
     */
    private static void centerOnScreen(Window window) {
        window.setLocationRelativeTo(null);
    }

    /**
     * Make sure pressing the escape key closes/disposes the dialog
     */
    private static void registerCloseOnEscapeAction(final JDialog dialog) {
        dialog.getRootPane().registerKeyboardAction(
                e -> dialog.dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
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


    static void runInEDT(Runnable runnable) {
        if (isEventDispatchThread()) {
            runnable.run();
        } else {
            try {
                invokeAndWait(runnable);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw newUncheckedException("Interrupted in runInEDT", e); // NON-NLS
            } catch (InvocationTargetException e) {
                throw newUncheckedException("Error in runInEDT", e); // NON-NLS
            }
        }
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
    static Point toScreenCoordinates(
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

}

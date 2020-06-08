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

import org.eclipse.jdt.annotation.Nullable;

import javax.swing.JFrame;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;

/**
 * Operations dealing with {@link JFrame}s and {@link java.awt.Dialog}s.
 */
public interface DialogAndFrameSupport {
    /**
     * Show the {@code component} in a new modal dialog.
     *
     * <p>The method returns when the dialog is closed.
     *
     * @param component the {@link Component} to display in the dialog
     */
    void showInDialog(Component component);

    /**
     * Show the {@code component} in a new modal dialog with the given
     * {@code title}.
     *
     * <p>The method returns when the dialog is closed.
     *
     * @param title     the title of the dialog
     * @param component the {@link Component} to display in the dialog
     */
    void showInDialogTitled(String title, Component component);

    /**
     * Show the {@code component} in a new JFrame.
     *
     * @param component the {@link Component} to display in the frame
     * @param position  the position of the frame; when {@code null} the
     *                  frame is centered on the screen
     * @param size      the size of the frame; when {@code null} the
     *                  frame is "packed"
     * @return the newly created {@link JFrame}
     */
    JFrame showInFrame(Component component, @Nullable Point position, @Nullable Dimension size);

    /**
     * Show the {@code component} in a new {@link JFrame}, with the frame
     * centered on the screen.
     *
     * @param component the {@link Component} to display in the frame
     * @return the newly created {@link JFrame}
     */
    @SuppressWarnings("UnusedReturnValue")
    default JFrame showInFrame(Component component) {
        return showInFrame(component, null, null);
    }

    /**
     * Show the {@code component} in a new JFrame with the given
     * {@code title}.
     *
     * @param title     the title of the frame
     * @param component the {@link Component} to display in the frame;
     *                  when {@code null} the frame is empty
     * @param position  the position of the frame; when {@code null} the
     *                  frame is centered on the screen
     * @param size      the size of the frame; when {@code null} the
     *                  frame is "packed"
     * @return the newly created {@link JFrame}
     */
    JFrame showInFrameTitled(String title, @Nullable Component component,
                             @Nullable Point position, @Nullable Dimension size);

    /**
     * Show the {@code component} in a new JFrame with the given
     * {@code title}, with the frame centered on the screen.
     *
     * @param title     the title of the frame
     * @param component the {@link Component} to display in the frame.
     * @return the newly created {@link JFrame}
     */
    @SuppressWarnings("UnusedReturnValue")
    default JFrame showInFrameTitled(String title, Component component) {
        return showInFrameTitled(title, component, null, null);
    }


}

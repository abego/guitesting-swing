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

package org.abego.guitesting.swing.internal.util;

import org.eclipse.jdt.annotation.Nullable;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;
import java.awt.event.ActionEvent;
import java.util.function.Consumer;

final class ActionWithEventHandler extends AbstractAction {
    private static final long serialVersionUID = 0L;
    private final Consumer<ActionEvent> action;

    private ActionWithEventHandler(
            String text,
            KeyStroke accelerator,
            @Nullable String description,
            @Nullable Icon smallIcon,
            Consumer<ActionEvent> eventHandler) {

        super(text, null);
        this.action = eventHandler;
        putValue(ACCELERATOR_KEY, accelerator);
        if (description != null) {
            putValue(Action.SHORT_DESCRIPTION,description);
        }
        if (smallIcon != null) {
            putValue(SMALL_ICON, smallIcon);
        }
    }

    public static Action newAction(
            String text,
            KeyStroke accelerator,
            Consumer<ActionEvent> action) {
        return new ActionWithEventHandler(text, accelerator, null, null,action);
    }

    public static Action newAction(
            String text,
            KeyStroke accelerator,
            ImageIcon smallIcon,
            Consumer<ActionEvent> action) {
        return new ActionWithEventHandler(text, accelerator, null, smallIcon, action);
    }

    public static Action newAction(
            String text,
            KeyStroke accelerator,
            String description,
            ImageIcon smallIcon,
            Consumer<ActionEvent> action) {
        return new ActionWithEventHandler(text, accelerator, description, smallIcon, action);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        action.accept(e);
    }
}

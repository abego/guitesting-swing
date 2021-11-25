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

package org.abego.guitesting.swing.internal.util.widget;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

import static javax.swing.Action.ACCELERATOR_KEY;
import static org.abego.commons.swing.KeyStrokeUtil.acceleratorText;
import static org.abego.guitesting.swing.internal.util.SwingUtil.toolbarButton;

public final class ToolbarButtonWidget implements Widget {
    private final JButton content = toolbarButton();

    private ToolbarButtonWidget() {}

    public static ToolbarButtonWidget toolbarButtonWidget() {
        return new ToolbarButtonWidget();
    }

    @Override
    public JComponent getContent() {
        return content;
    }

    @Override
    public void close() {
    }

    public void setAction(Action action) {
        content.setAction(action);

        Object k = action.getValue(ACCELERATOR_KEY);
        if (k instanceof KeyStroke) {
            KeyStroke keyStroke = (KeyStroke) k;
            String aText = acceleratorText(keyStroke);
            if (!aText.isEmpty()) {
                content.setToolTipText(String.format("%s [%s]", //NON-NLS
                        content.getToolTipText(), aText));
            }
        }
    }
}

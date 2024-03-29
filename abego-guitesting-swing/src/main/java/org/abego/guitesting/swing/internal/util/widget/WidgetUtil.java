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

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.util.function.Consumer;

import static org.abego.commons.swing.WindowUtil.onWindowClosed;

//TODO: do we need this public?
public final class WidgetUtil {
    WidgetUtil() {
        throw new UnsupportedOperationException("Must not instantiate");
    }

    public static JFrame showWidgetInJFrame(
            Widget widget,
            String title,
            String name,
            Consumer<JFrame> preShowCode) {

        JFrame frame = new JFrame(title); //NON-NLS

        frame.setName(name);
        frame.setContentPane(widget.getContent());
        onWindowClosed(frame, e -> widget.close());

        preShowCode.accept(frame);
        SwingUtilities.invokeLater(() -> frame.setVisible(true));

        return frame;
    }

    public static void setVisible(boolean flag, Widget... widgets) {
        for (Widget w : widgets) {
            w.getContent().setVisible(flag);
        }
    }
}

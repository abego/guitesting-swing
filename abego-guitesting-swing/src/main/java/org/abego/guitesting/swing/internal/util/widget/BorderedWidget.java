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

import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.BorderLayout;

public class BorderedWidget implements Widget {
    private final JPanel content = new JPanel(new BorderLayout());

    private BorderedWidget() {}

    public static BorderedWidget borderedWidget() {
        return new BorderedWidget();
    }

    @Override
    public JComponent getContent() {
        return content;
    }

    @Override
    public void close() {

    }

    public BorderedWidget top(JComponent component) {
        content.add(component, BorderLayout.PAGE_START);
        return this;
    }

    public BorderedWidget left(JComponent component) {
        content.add(component, BorderLayout.LINE_START);
        return this;
    }

    public BorderedWidget bottom(JComponent component) {
        content.add(component, BorderLayout.PAGE_END);
        return this;
    }

    public BorderedWidget right(JComponent component) {
        content.add(component, BorderLayout.LINE_END);
        return this;
    }

    public BorderedWidget center(JComponent component) {
        content.add(component, BorderLayout.CENTER);
        return this;
    }

    public BorderedWidget top(Widget widget) {
        return top(widget.getContent());
    }

    public BorderedWidget left(Widget widget) {
        return left(widget.getContent());
    }

    public BorderedWidget bottom(Widget widget) {
        return bottom(widget.getContent());
    }

    public BorderedWidget right(Widget widget) {
        return right(widget.getContent());
    }

    public BorderedWidget center(Widget widget) {
        return center(widget.getContent());
    }
}

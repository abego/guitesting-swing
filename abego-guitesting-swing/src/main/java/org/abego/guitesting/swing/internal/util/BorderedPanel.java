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

import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.BorderLayout;

public final class BorderedPanel extends JPanel {
    private BorderedPanel() {
        super(new BorderLayout());
    }

    public static BorderedPanel newBorderedPanel() {
        return new BorderedPanel();
    }

    public BorderedPanel left(JComponent component) {
        return west(component);
    }

    public BorderedPanel west(JComponent component) {
        add(component, BorderLayout.LINE_START);
        return this;
    }

    public BorderedPanel right(JComponent component) {
        return east(component);
    }

    public BorderedPanel east(JComponent component) {
        add(component, BorderLayout.LINE_END);
        return right(component);
    }

    public BorderedPanel top(JComponent component) {
        return north(component);
    }

    public BorderedPanel north(JComponent component) {
        add(component, BorderLayout.PAGE_START);
        return this;
    }

    public BorderedPanel bottom(JComponent component) {
        return south(component);
    }

    public BorderedPanel south(JComponent component) {
        add(component, BorderLayout.PAGE_END);
        return this;
    }

    public BorderedPanel center(JComponent component) {
        add(component, BorderLayout.CENTER);
        return this;
    }
}

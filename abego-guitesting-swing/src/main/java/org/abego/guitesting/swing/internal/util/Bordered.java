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
import java.util.function.Consumer;

public final class Bordered {
    private final JComponent component;

    private Bordered(JComponent component) {
        this.component = component;
        component.setLayout(new BorderLayout());
    }

    public static Bordered bordered(JComponent component, Consumer<JComponent> initCode) {
        Bordered bordered = new Bordered(component);
        initCode.accept(component);
        return bordered;
    }

    public static Bordered bordered(JComponent component) {
        return bordered(component, c -> {});
    }

    public static Bordered bordered() {
        return bordered(new JPanel());
    }

    public static Bordered bordered(Consumer<JComponent> initCode) {
        return bordered(new JPanel(), initCode);
    }

    public Bordered left(JComponent component) {
        return west(component);
    }

    public Bordered west(JComponent component) {
        this.component.add(component, BorderLayout.LINE_START);
        return this;
    }

    public Bordered right(JComponent component) {
        return east(component);
    }

    public Bordered east(JComponent component) {
        this.component.add(component, BorderLayout.LINE_END);
        return this;
    }

    public Bordered top(JComponent component) {
        return north(component);
    }

    public Bordered north(JComponent component) {
        this.component.add(component, BorderLayout.PAGE_START);
        return this;
    }

    public Bordered bottom(JComponent component) {
        return south(component);
    }

    public Bordered south(JComponent component) {
        this.component.add(component, BorderLayout.PAGE_END);
        return this;
    }

    public Bordered center(JComponent component) {
        this.component.add(component, BorderLayout.CENTER);
        return this;
    }

    public JComponent component() {return component;}
}

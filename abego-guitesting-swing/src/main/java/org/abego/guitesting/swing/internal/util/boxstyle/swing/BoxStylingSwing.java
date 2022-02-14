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

package org.abego.guitesting.swing.internal.util.boxstyle.swing;

import org.abego.commons.lang.exception.MustNotInstantiateException;
import org.abego.guitesting.swing.internal.util.boxstyle.BoxStyle;
import org.eclipse.jdt.annotation.Nullable;

import javax.swing.JComponent;

import java.awt.Color;
import java.awt.Font;

import static java.awt.Transparency.BITMASK;
import static org.abego.guitesting.swing.internal.util.boxstyle.swing.BoxBorder.boxBorder;

public final class BoxStylingSwing {

    BoxStylingSwing() {
        throw new MustNotInstantiateException();
    }

    public static void setBoxStyle(JComponent component, BoxStyle style) {
        component.setBorder(boxBorder(style.getTop(),
                style.getRight(), style.getBottom(), style.getLeft()));

        @Nullable Color color = style.getColorOrNull();
        if (color != null) {
            component.setForeground(color);
        }

        @Nullable Color bg = style.getBackgroundOrNull();
        if (bg != null && bg.getTransparency() != BITMASK) {
            component.setOpaque(true);
            component.setBackground(bg);
        } else {
            component.setOpaque(false);
        }

        @Nullable Font font = style.getFontOrNull();
        if (font != null) {
            component.setFont(font);
        }
    }

    public static void setBoxStyle(JComponent component, BoxStyle.Factory styleFactory) {
        setBoxStyle(component, styleFactory.create());
    }
}

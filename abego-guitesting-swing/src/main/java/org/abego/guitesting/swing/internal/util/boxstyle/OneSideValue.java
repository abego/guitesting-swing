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

package org.abego.guitesting.swing.internal.util.boxstyle;

import org.abego.guitesting.swing.internal.util.boxstyle.BoxStyle.Style;
import org.eclipse.jdt.annotation.Nullable;

import java.awt.Color;

class OneSideValue {
    final int border ;
    final Style style;
    final @Nullable Color color;
    final int padding;
    final int margin;

    private OneSideValue(int border, Style style, @Nullable Color color, int padding, int margin) {
        this.border = border;
        this.style = style;
        this.color = color;
        this.padding = padding;
        this.margin = margin;
    }

    private OneSideValue(OneSide oneSide) {
        this(oneSide.border, oneSide.style, oneSide.color, oneSide.padding, oneSide.margin);
    }

    private OneSideValue() {
        this(0, Style.NONE, null, 0, 0);
    }

    public static OneSideValue oneSideValue(OneSide oneSide) {
        return new OneSideValue(oneSide);
    }

    public int totalSize() {
        return border + padding + margin;
    }
}

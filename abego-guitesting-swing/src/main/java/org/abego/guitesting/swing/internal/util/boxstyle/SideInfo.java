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

import org.abego.guitesting.swing.internal.util.boxstyle.BoxStyle.BorderStyle;
import org.eclipse.jdt.annotation.Nullable;

import java.awt.Color;
import java.util.Objects;

public final class SideInfo {
    final int border;
    final BorderStyle style;
    final @Nullable Color color;
    final int padding;
    final int margin;

    private SideInfo(int border, BorderStyle style, @Nullable Color color, int padding, int margin) {
        this.border = border;
        this.style = style;
        this.color = color;
        this.padding = padding;
        this.margin = margin;
    }

    private SideInfo(OneSide oneSide) {
        this(oneSide.border, oneSide.style, oneSide.color, oneSide.padding, oneSide.margin);
    }

    private SideInfo() {
        this(0, BorderStyle.NONE, null, 0, 0);
    }

    static SideInfo sideInfo(OneSide oneSide) {
        return new SideInfo(oneSide);
    }

    public int totalSize() {
        return border + padding + margin;
    }

    public int getBorder() {
        return border;
    }

    public BorderStyle getStyle() {
        return style;
    }

    public Color getColor() {
        return color;
    }

    public int getPadding() {
        return padding;
    }

    public int getMargin() {
        return margin;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SideInfo sideInfo = (SideInfo) o;
        return border == sideInfo.border && padding == sideInfo.padding && margin == sideInfo.margin && style == sideInfo.style && Objects.equals(color, sideInfo.color);
    }

    @Override
    public int hashCode() {
        return Objects.hash(border, style, color, padding, margin);
    }
}

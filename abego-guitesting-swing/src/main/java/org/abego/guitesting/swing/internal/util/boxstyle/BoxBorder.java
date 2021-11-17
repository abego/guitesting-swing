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

import javax.swing.border.EmptyBorder;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;

import static org.abego.guitesting.swing.internal.util.boxstyle.BoxStyle.Style.SOLID;


final class BoxBorder extends EmptyBorder {
    private OneSideValue top;
    private OneSideValue left;
    private OneSideValue bottom;
    private OneSideValue right;

    private BoxBorder(OneSideValue top, OneSideValue right, OneSideValue bottom, OneSideValue left) {
        super(top.totalSize(), left.totalSize(), bottom.totalSize(), right.totalSize());
        this.top = top;
        this.left = left;
        this.bottom = bottom;
        this.right = right;
    }

    static BoxBorder boxBorder(OneSideValue top, OneSideValue right, OneSideValue bottom, OneSideValue left) {
        return new BoxBorder(top, right, bottom, left);
    }

    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        g.translate(x, y);

        Color oldColor = g.getColor();

        // drow the border in this order: left, right, top, bottom
        // this way the horizontal lines "overwrite" the vertical lines
        if (left.color != null && left.style == SOLID) {
            g.setColor(left.color);
            g.fillRect(left.margin, top.margin, left.border, height - top.margin - bottom.margin);
        }
        if (right.color != null && right.style == SOLID) {
            g.setColor(right.color);
            g.fillRect(width - right.margin-right.border, top.margin, right.border, height - top.margin - bottom.margin);
        }
        if (top.color != null && top.style == SOLID) {
            g.setColor(top.color);
            g.fillRect(left.margin, top.margin, width - left.margin - right.margin, top.border);
        }
        if (bottom.color != null && bottom.style == SOLID) {
            g.setColor(bottom.color);
            g.fillRect(left.margin, height - bottom.margin - bottom.border, width - left.margin - right.margin, bottom.border);
        }
        g.translate(-x, -y);
        g.setColor(oldColor);
    }

    @Override
    public Insets getBorderInsets(Component c) {
        return new Insets(top.totalSize(), left.totalSize(), bottom.totalSize(), right.totalSize());
    }
}




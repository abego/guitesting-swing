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

import javax.swing.JComponent;

import java.awt.Color;

import static org.abego.guitesting.swing.internal.util.boxstyle.BoxBorder.boxBorder;
import static org.abego.guitesting.swing.internal.util.boxstyle.OneSide.oneSide;
import static org.abego.guitesting.swing.internal.util.boxstyle.OneSideValue.oneSideValue;

public class BoxStyle {

    private final OneSideValue top;
    private final OneSideValue right;
    private final OneSideValue bottom;
    private final OneSideValue left;

    public enum Style {
        NONE,
        SOLID
    }

    //TODO: generate code
    public static final class Factory {
        private OneSide top = oneSide();
        private OneSide right = oneSide();
        private OneSide bottom = oneSide();
        private OneSide left = oneSide();

        private Factory() {}

        public BoxStyle create() {
            return new BoxStyle(top, right, bottom, left);
        }

        public void applyTo(JComponent component) {
            create().applyTo(component);
        }

        public Factory border(int size, Style style, Color color) {
            borderTop(size, style, color);
            borderRight(size, style, color);
            borderBottom(size, style, color);
            borderLeft(size, style, color);
            return this;
        }

        public Factory borderTop(int size, Style style, Color color) {
            top.border = size;
            top.style = style;
            top.color = color;
            return this;
        }

        public Factory borderRight(int size, Style style, Color color) {
            right.border = size;
            right.style = style;
            right.color = color;
            return this;
        }

        public Factory borderBottom(int size, Style style, Color color) {
            bottom.border = size;
            bottom.style = style;
            bottom.color = color;
            return this;
        }

        public Factory borderLeft(int size, Style style, Color color) {
            left.border = size;
            left.style = style;
            left.color = color;
            return this;
        }

        public Factory marginTop(int size) {
            top.margin = size;
            return this;
        }

        public Factory marginRight(int size) {
            right.margin = size;
            return this;
        }

        public Factory marginBottom(int size) {
            bottom.margin = size;
            return this;
        }

        public Factory marginLeft(int size) {
            left.margin = size;
            return this;
        }

        public Factory margin(int size) {
            top.margin = bottom.margin = right.margin = left.margin = size;
            return this;
        }

        public Factory margin(int topBottomSize, int leftRightSize) {
            top.margin = bottom.margin = topBottomSize;
            right.margin = left.margin = leftRightSize;
            return this;
        }

        public Factory margin(int topSize, int leftRightSize, int bottomSize) {
            top.margin = topSize;
            right.margin = left.margin = leftRightSize;
            bottom.margin = bottomSize;
            return this;
        }

        public Factory margin(int topSize, int rightSize, int bottomSize, int leftSize) {
            top.margin = topSize;
            right.margin = rightSize;
            bottom.margin = bottomSize;
            left.margin = leftSize;
            return this;
        }

        public Factory paddingTop(int size) {
            top.padding = size;
            return this;
        }

        public Factory paddingRight(int size) {
            right.padding = size;
            return this;
        }

        public Factory paddingBottom(int size) {
            bottom.padding = size;
            return this;
        }

        public Factory paddingLeft(int size) {
            left.padding = size;
            return this;
        }

        public Factory padding(int size) {
            top.padding = bottom.padding = right.padding = left.padding = size;
            return this;
        }

        public Factory padding(int topBottomSize, int leftRightSize) {
            top.padding = bottom.padding = topBottomSize;
            right.padding = left.padding = leftRightSize;
            return this;
        }

        public Factory padding(int topSize, int leftRightSize, int bottomSize) {
            top.padding = topSize;
            right.padding = left.padding = leftRightSize;
            bottom.padding = bottomSize;
            return this;
        }

        public Factory padding(int topSize, int rightSize, int bottomSize, int leftSize) {
            top.padding = topSize;
            right.padding = rightSize;
            bottom.padding = bottomSize;
            left.padding = leftSize;
            return this;
        }
    }

    private BoxStyle(OneSide top, OneSide right, OneSide bottom, OneSide left) {
        this.top = oneSideValue(top);
        this.left = oneSideValue(left);
        this.bottom = oneSideValue(bottom);
        this.right = oneSideValue(right);
    }

    public static Factory style() {
        return new Factory();
    }

    public void applyTo(JComponent component) {
        component.setBorder(boxBorder(top, right, bottom, left));
    }
}

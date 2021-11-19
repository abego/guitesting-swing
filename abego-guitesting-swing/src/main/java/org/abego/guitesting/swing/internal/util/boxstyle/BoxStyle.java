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

import org.eclipse.jdt.annotation.Nullable;

import java.awt.Color;
import java.awt.Font;

import static org.abego.guitesting.swing.internal.util.boxstyle.OneSide.oneSide;
import static org.abego.guitesting.swing.internal.util.boxstyle.SideInfo.sideInfo;

public class BoxStyle {
    //TODO: provide a method that returns only the attributes with non-default
    //  values, or that are not explicitly set to the default value, e.g. as
    //  a list of (key,value) pairs, with the keys as the string in CSS naming
    //  convention. This may be useful when working on large objects with many
    //  styles, with only few of the attributes customized. With the current
    //  approach we would have to set all attributes of all theses objects,
    //  even if most of them are the same as their already set default value.

    private final SideInfo top;
    private final SideInfo right;
    private final SideInfo bottom;
    private final SideInfo left;
    private final @Nullable Color background;
    private final @Nullable Color color;
    private final @Nullable Font font;

    public enum Style {
        NONE,
        SOLID
    }

    //TODO: generate code
    public static final class Factory {
        private final OneSide top = oneSide();
        private final OneSide right = oneSide();
        private final OneSide bottom = oneSide();
        private final OneSide left = oneSide();
        private @Nullable Color background;
        private @Nullable Color color;
        private @Nullable Font font;

        private Factory() {}

        public BoxStyle create() {
            return new BoxStyle(top, right, bottom, left, background, color, font);
        }

        public Factory background(Color color) {
            this.background = color;
            return this;
        }

        public Factory color(Color color) {
            this.color = color;
            return this;
        }

        public Factory font(Font font) {
            this.font = font;
            return this;
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

    private BoxStyle(OneSide top, OneSide right, OneSide bottom, OneSide left,
                     @Nullable Color background, @Nullable Color color,
                     @Nullable Font font) {
        this.top = sideInfo(top);
        this.left = sideInfo(left);
        this.bottom = sideInfo(bottom);
        this.right = sideInfo(right);
        this.background = background;
        this.color = color;
        this.font = font;
    }

    public static Factory newBoxStyle() {
        return new Factory();
    }

    @Nullable
    public Color getBackground() {
        return background;
    }

    @Nullable
    public Color getColor() {
        return color;
    }

    @Nullable
    public Font getFont() {
        return font;
    }

    public SideInfo getTop() {
        return top;
    }

    public SideInfo getRight() {
        return right;
    }

    public SideInfo getBottom() {
        return bottom;
    }

    public SideInfo getLeft() {
        return left;
    }


}

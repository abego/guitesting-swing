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

package org.abego.guitesting.swing.internal.snapshotreview;


import org.abego.commons.swing.ImageIconUtil;

import javax.swing.ImageIcon;


class Resources {
    public static ImageIcon copyIcon() {
        return iconFromResource("copy-16.png"); //NON-NLS
    }

    public static ImageIcon overwriteIcon() {
        return iconFromResource("overwrite2-16.png"); //NON-NLS
    }

    public static ImageIcon alternativeIcon() {
        return iconFromResource("alternative2-16.png"); //NON-NLS
    }

    public static ImageIcon ignoreIcon() {
        return iconFromResource("red-x-16.png"); //NON-NLS
    }

    @SuppressWarnings("unused")
    public static ImageIcon rotateLeftIcon() {
        return iconFromResource("rotate-left-16.png"); //NON-NLS
    }

    public static ImageIcon rotateRightIcon() {
        return iconFromResource("rotate-right-16.png"); //NON-NLS
    }

    private static ImageIcon iconFromResource(String name) {
        return ImageIconUtil.iconFromResource(name, Resources.class);
    }
}

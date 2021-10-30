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

import java.awt.Dimension;

import static java.lang.Math.min;

public final class DimensionUtil {

    DimensionUtil() {
        throw new IllegalArgumentException("Must not instantiate"); //NON-NLS
    }

    /**
     * Returns the factor one must scale the {@code dimension} to fit
     * it in the {@code boundingSize}, or {@code 1.0} if it already fits.
     *<p>
     * When the height of {@code boundingSize} is {@code <= 0} the height does
     * not affect the factor. When the width of {@code boundingSize} is
     * {@code <= 0} the width does not affect the factor.
     */
    public static double shrinkToFitFactor(Dimension sizeToFit, Dimension boundingSize) {
        double scaleFactor = 1.0;
        if (boundingSize.getHeight() > 0 && sizeToFit.getHeight() > boundingSize.getHeight()) {
            scaleFactor = boundingSize.getHeight() / sizeToFit.getHeight();
        }
        if (boundingSize.getWidth() > 0 && sizeToFit.getWidth() > boundingSize.getWidth()) {
            scaleFactor = min(scaleFactor, boundingSize.getWidth() / sizeToFit.getWidth());
        }
        return scaleFactor;
    }

    /**
     * Returns {@code true} if {@code dimension} entirely contains the
     * {@code otherDimension}, {@code false} otherwise.
     */
    public static boolean contains(Dimension dimension, Dimension otherDimension) {
        return dimension.getWidth() >= otherDimension.getWidth() &&
                dimension.getHeight() >= otherDimension.getHeight();
    }
}

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

package org.abego.guitesting.swing.internal;

import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.UncheckedIOException;

import static org.abego.guitesting.swing.internal.ImageCompare.newImageCompare;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ImageCompareTest {

    @Test
    void differenceMask_differentContent() throws IOException {
        BufferedImage image1 = getColorsImage();
        BufferedImage image2 = getColors2Image();

        ImageCompare imageCompare = newImageCompare();

        @Nullable
        BufferedImage diffMask =
                imageCompare.differenceMask(image1, image2);
        assertNotNull(diffMask);

        BufferedImage expectedDiffMask = getColorsColors2DifferenceMask();
        @Nullable
        BufferedImage diffMask2 =
                imageCompare.differenceMask(expectedDiffMask, diffMask);

        assertNull(diffMask2);
    }

    public static BufferedImage getColorsColorsDifferenceMask() {
        return readResourceImage("colors-colors-difference.png");
    }


    public static BufferedImage getColorsColors2DifferenceMask() {
        return readResourceImage("colors-colors2-difference.png");
    }

    public static BufferedImage getColors2Image() {
        return readResourceImage("colors2.png");
    }

    public static BufferedImage getColorsImage() {
        return readResourceImage("colors.png");
    }

    public static BufferedImage getColorsAtScreen1Image() {
        return readResourceImage("colors@screen1.png");
    }

    public static BufferedImage getColorsColorsLargerDifferenceMask() {
        return readResourceImage("colors-colorsLarger-difference.png");
    }

    public static BufferedImage getColorsLargerImage() {
        return readResourceImage("colorsLarger.png");
    }

    private static BufferedImage readResourceImage(String name) {
        try {
            return ImageIO.read(ImageCompareTest.class.getResource(
                    "/org/abego/guitesting/swing/" + name));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Test
    void differenceMask_differentSize() throws IOException {
        BufferedImage image1 = getColorsImage();
        BufferedImage image2 = getColorsLargerImage();

        ImageCompare imageCompare = newImageCompare();

        @Nullable
        BufferedImage diffMask =
                imageCompare.differenceMask(image1, image2);
        assertNotNull(diffMask);

        BufferedImage expectedDiffMask = getColorsColorsLargerDifferenceMask();
        @Nullable
        BufferedImage diffMask2 =
                imageCompare.differenceMask(expectedDiffMask, diffMask);

        assertNull(diffMask2);
    }

}

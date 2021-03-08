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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;

public final class ImageCompare {

    private final Context context;

    private ImageCompare(Context context) {
        this.context = context;
    }

    public static ImageCompare newImageCompare(Context context) {
        return new ImageCompare(context);
    }

    public static ImageCompare newImageCompare() {
        return newImageCompare(new Context() {
        });
    }

    /**
     * @return the size of the image, or null when the image is not
     * loaded (completely)
     */
    private static @Nullable Dimension getSize(Image image) {
        Dimension size = new Dimension(
                image.getWidth(null),
                image.getHeight(null));
        return size.getWidth() < 0 || size.getHeight() < 0 ? null : size;
    }

    /**
     * @param pixels array of pixels in INT_ARGB format (e.g. as returned by
     *               PixelGrabber.getPixels())
     */
    private static BufferedImage getImageFromPixels(
            int[] pixels, int width, int height) {

        if (pixels.length != width * height) {
            throw new IllegalArgumentException(
                    "pixels must contain width*height items (" + pixels.length
                            + " != " + width + "*" + height + ")");
        }
        BufferedImage image = new BufferedImage(
                width, height, BufferedImage.TYPE_INT_ARGB);
        image.setRGB(0, 0, width, height, pixels, 0, width);
        return image;
    }

    /**
     * @param image [must be loaded completely]
     * @return array of pixels in INT_ARGB format
     */
    private static int[] getPixels(Image image) {
        int width = image.getWidth(null);
        int height = image.getHeight(null);
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException(
                    "image must be loaded completely before pixels can be retrieved");
        }
        int[] pixels = new int[width * height];
        PixelGrabber pg = new PixelGrabber(
                image, 0, 0, width, height, pixels, 0, width);
        try {
            pg.grabPixels();
        } catch (InterruptedException e) {
            throw new IllegalStateException(
                    "Unexpected interrupt when retrieving image pixels", e);
        }
        return pixels;
    }

    /**
     * @param pixel in INT_ARGB format
     */
    private static Color getColorOfPixel(int pixel) {
        int alpha = (pixel >> 24) & 0xff;
        int red = (pixel >> 16) & 0xff;
        int green = (pixel >> 8) & 0xff;
        int blue = (pixel) & 0xff;
        return new Color(red, green, blue, alpha);
    }

    private static Dimension max(Dimension size1, Dimension size2) {
        return new Dimension(
                Math.max(size1.width, size2.width),
                Math.max(size1.height, size2.height));
    }

    /**
     * @return pixel in INT_ARGB format
     */
    private static int getPixel(int red, int green, int blue, int alpha) {
        return ((alpha << 24) & 0xff000000) | ((red << 16) & 0xff0000)
                | ((green << 8) & 0xff00) | ((blue) & 0xff);
    }

    /**
     * @return pixel in INT_ARGB format
     */
    private static int getPixel(Color color) {
        return getPixel(
                color.getRed(),
                color.getGreen(),
                color.getBlue(),
                color.getAlpha());
    }

    private boolean arePixelsSimilar(int pixelA, int pixelB) {

        if (pixelA != pixelB) {
            Color colorA = getColorOfPixel(pixelA);
            Color colorB = getColorOfPixel(pixelB);

            return context.colorsAreSimilar(colorA, colorB);
        }

        return true;
    }

    /**
     * Returns an image marking the differences between imageA and imageV with
     * {@link Color#black} pixels on a (transparent) white canvas,
     * or {@code null} when imageA and imageB are not different.
     *
     * <p>The images are compared pixel by pixel and all pixel that are not
     * similar as defines {@link Context#colorsAreSimilar(Color, Color)}
     * or that only exist in one image are marked {@link Color#black}.</p>
     */
    @Nullable
    public BufferedImage differenceMask(Image imageA, Image imageB) {

        @Nullable
        Dimension sizeA = getSize(imageA);
        @Nullable
        Dimension sizeB = getSize(imageB);

        if (sizeA == null || sizeB == null) {
            throw new IllegalArgumentException(
                    "images must be loaded completely before they can be compared");
        }

        Dimension size = max(sizeA, sizeB);
        int h = size.height;
        int w = size.width;

        int whiteTransparentPixel = getPixel(new Color(255, 255, 255, 0));
        int blackPixel = getPixel(Color.black);
        int[] pixelsA = getPixels(imageA);
        int[] pixelsB = getPixels(imageB);
        int[] pixelsResult = new int[size.width * size.height];

        boolean imagesDiffer = false;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int color;
                boolean hasPixelA = x < sizeA.width && y < sizeA.height;
                boolean hasPixelB = x < sizeB.width && y < sizeB.height;
                int pixelA = hasPixelA ? pixelsA[y * sizeA.width + x] : 0;
                int pixelB = hasPixelB ? pixelsB[y * sizeB.width + x] : 0;
                if (hasPixelA && hasPixelB && arePixelsSimilar(pixelA, pixelB)) {
                    color = whiteTransparentPixel;
                } else {
                    imagesDiffer = true;
                    color = blackPixel;
                }
                pixelsResult[y * w + x] = color;
            }
        }

        return imagesDiffer ? getImageFromPixels(pixelsResult, w, h) : null;
    }

    public interface Context {
        /**
         * Returns true when colorA is similar to colorB.
         *
         * <p>By default both colors are similar when they are equal.</p>
         */
        default boolean colorsAreSimilar(Color colorA, Color colorB) {
            return colorA.equals(colorB);
        }
    }
}
